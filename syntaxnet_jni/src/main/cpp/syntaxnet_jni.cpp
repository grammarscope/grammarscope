/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#include <jni.h>

#include <string>
#include <vector>
#include <unistd.h>

#include <codecvt>
#include <cwchar>
#include <iostream>
#include <sstream>
#include <locale>
#include <regex>
#include <iomanip>   // std::setfill, std::setw, std::hex  ← required

#include <android/log.h>

#include "syntaxnet/glue.h"

#define LOG_TAG    "SYNTAXNET_JNI"

//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace std;

constexpr char kIllegalStateException[] = "java/lang/IllegalStateException";

constexpr char sentenceClass[] = "org/depparse/Sentence";
constexpr char tokenClass[] = "org/depparse/Token";
constexpr char mwtokenClass[] = "org/depparse/MWToken";
constexpr char sentenceCtor[] = "(Ljava/lang/String;II[Lorg/depparse/Token;[Lorg/depparse/MWToken;Ljava/lang/String;)V";
constexpr char tokenCtor[] = "(IILjava/lang/String;IILjava/lang/String;Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;Lorg/depparse/MWToken;)V";
constexpr char mwtokenCtor[] = "(IILjava/lang/String;IIII)V";

// C H E C K   H E L P E R S

/**
 * Check nullity and throw an IllegalStateException if the object is null
 * @tparam T object type
 * @param env environment
 * @param t object to check
 * @return object
 */
template<typename T>
T check_not_null(JNIEnv *env, T &&t) {
    if (t == nullptr) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "");
        return nullptr;
    }
    return std::forward<T>(t);
}

// C O N V E R S I O N   H E L P E R S

/**
 * Decode UTF-8 bytes into code points, then output each as hex
 */
static string to_hex_codepoints(const std::string &s) {
    std::ostringstream oss;
    oss << std::hex << std::setfill('0');

    size_t i = 0;
    while (i < s.size()) {
        auto c = static_cast<unsigned char>(s[i]);
        char32_t cp;
        size_t len;

        if ((c & 0x80) == 0x00) {
            cp = c;
            len = 1;
        } // 0xxxxxxx
        else if ((c & 0xE0) == 0xC0) {
            cp = c & 0x1F;
            len = 2;
        } // 110xxxxx
        else if ((c & 0xF0) == 0xE0) {
            cp = c & 0x0F;
            len = 3;
        } // 1110xxxx
        else if ((c & 0xF8) == 0xF0) {
            cp = c & 0x07;
            len = 4;
        } // 11110xxx
        else { throw std::runtime_error("invalid UTF-8"); }

        for (size_t k = 1; k < len; ++k) {
            const auto cc = static_cast<unsigned char>(s[i + k]);
            if ((cc & 0xC0) != 0x80) throw std::runtime_error("invalid UTF-8");
            cp = (cp << 6) | (cc & 0x3F);
        }

        oss << std::setw(len == 1 ? 2 : (len == 2 ? 3 : (len == 3 ? 4 : 5)))
            << static_cast<unsigned>(cp)
            << ' ';
        i += len;
    }
    return oss.str();
}

// F R O M   J A V A

extern
vector<string> jniStringArrayToVector(JNIEnv *env, jobjectArray j_strs) {
    int count = env->GetArrayLength(j_strs);
    vector<string> result;
    for (int i = 0; i < count; i++) {
        auto j_str = reinterpret_cast<jstring>(env->GetObjectArrayElement(j_strs, i));
        if (j_str != nullptr) {
            const char *raw_str = env->GetStringUTFChars(j_str, JNI_FALSE);
            if (raw_str != nullptr) {
                result.emplace_back(raw_str);
                env->ReleaseStringUTFChars(j_str, raw_str);
            }
            env->DeleteLocalRef(j_str);
        }

        string hex = to_hex_codepoints(result[i]);
        LOGD("Input %d Codepoints cps = %s", i, hex.c_str());
    }
    return result;
}

string jniStringToString(JNIEnv *env, jstring j_str) {
    const char *raw_str = env->GetStringUTFChars(j_str, JNI_FALSE);
    string result(raw_str);
    env->ReleaseStringUTFChars(j_str, raw_str);
    return result;
}

// T O   J A V A

/**
 * Returns a Java sentence
 *
 * @param env environment
 * @param parsed_sentence parsed C sentence, non-null
 * @param sentenceIndex sentence index
 * @param j_sentence_class java class of sentence
 * @param j_sentence_ctor java constructor
 * @param j_token_class java class of token
 * @param j_token_ctor java constructor of token
 * @return Sentence with tokens field being Array<Token!!>!! otherwise
 * @throws IllegalStateException whenever CheckNull encounters a null value, that is
 * -word is null (should not happen)
 * -category, label, tag are guarded against being null by being set to empty string at token level
 * -text, docIds are guarded against being null by being set to empty string at sentence level
 */
jobject
toJavaSentence(
        JNIEnv *env,
        const sentence_t &parsed_sentence,
        int sentenceIndex,
        jclass j_sentence_class,
        jmethodID j_sentence_ctor,
        jclass j_token_class,
        jmethodID j_token_ctor,
        jclass j_mwtoken_class,
        jmethodID j_mwtoken_ctor
        ) {

    int nTokens = (int) parsed_sentence.size();

    // make java array of tokens Token[] to be field of Sentence class and return back to Java

    jobjectArray j_token_array = check_not_null(env, env->NewObjectArray(nTokens - 1, j_token_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // Sentence text as token[0] (text as token[0]["text"], docid as token[0]["docid"]

    const token_t &token0 = parsed_sentence[0];

    const string &text = token0.at("text");

    auto it0 = token0.find("docid");
    const string &docid = it0 != token0.end() ? it0->second : "";

    it0 = token0.find("start");
    const string &s_start = it0 != token0.end() ? it0->second : "-1";
    const int s_istart = strtol(s_start.c_str(), nullptr, 10);

    it0 = token0.find("end");
    const string &s_end = it0 != token0.end() ? it0->second : "-1";
    const int s_iend = strtol(s_end.c_str(), nullptr, 10);

    // Tokens

    for (int j = 1; j < nTokens; j++) {

        // collect token data

        const token_t &token = parsed_sentence[j];

        const string &word = token.at("word");

        auto it = token.find("category");
        const string &category = it != token.end() ? it->second : "";

        it = token.find("tag");
        const string &tag = it != token.end() ? it->second : "";

        it = token.find("head");
        const string &head = it != token.end() ? it->second : "-1";

        it = token.find("label");
        const string &label = it != token.end() ? it->second : "";

        it = token.find("start");
        const string &t_start = it != token.end() ? it->second : "-1";

        it = token.find("end");
        const string &t_end = it != token.end() ? it->second : "-1";

        it = token.find("breaklevel");
        const string &breaklevel = it != token.end() ? it->second : "-1";

        LOGD("Token #%d '%s' l=%s t=%s h=%s\n", j, word.c_str(), label.c_str(), tag.c_str(), head.c_str());

        // token constructor parameters

        jstring j_word = check_not_null(env, env->NewStringUTF(word.c_str()));
        const int t_istart = strtol(t_start.c_str(), nullptr, 10);
        const int t_iend = strtol(t_end.c_str(), nullptr, 10);
        jstring j_category = check_not_null(env, env->NewStringUTF(category.c_str()));
        jstring j_tag = check_not_null(env, env->NewStringUTF(tag.c_str()));
        jstring j_label = check_not_null(env, env->NewStringUTF(label.c_str()));
        int ihead = strtol(head.c_str(), nullptr, 10);
        int ibreaklevel = strtol(breaklevel.c_str(), nullptr, 10);

        // make java token
        // j_word: String!!, possibly ""
        // istart: Int!!, possibly -1
        // iend: Int!!, possibly -1
        // j_category!!: String!!, possibly ""
        // j_tag: String!!
        // ihead: Int!!, possibly -1
        // j_label: String!!, possibly ""
        // ibreaklevel: Int!!, possibly -1
        // deps: String?, nullable (nullptr)
        // mwt: MWToken?, nullable (nullptr)
        jobject j_token = check_not_null(env, env->NewObject(j_token_class, j_token_ctor, sentenceIndex, j - 1, j_word, t_istart, t_iend, j_category, j_tag, ihead, j_label, ibreaklevel, nullptr, nullptr));
        if (env->ExceptionCheck() || j_token == nullptr) {
            env->DeleteLocalRef(j_word);
            env->DeleteLocalRef(j_category);
            env->DeleteLocalRef(j_tag);
            env->DeleteLocalRef(j_label);
            return nullptr;
        }

        // set token in array

        env->SetObjectArrayElement(j_token_array, j - 1, j_token);

        env->DeleteLocalRef(j_word);
        env->DeleteLocalRef(j_category);
        env->DeleteLocalRef(j_tag);
        env->DeleteLocalRef(j_label);
        env->DeleteLocalRef(j_token);
    }

    // make sentence

    jstring j_text = check_not_null(env, env->NewStringUTF(text.c_str()));
    if (env->ExceptionCheck() || j_text == nullptr) {
        return nullptr;
    }
    jstring j_docid = check_not_null(env, env->NewStringUTF(docid.c_str()));
    if (env->ExceptionCheck() || j_docid == nullptr) {
        env->DeleteLocalRef(j_text);
        return nullptr;
    }
    jobject j_sentence = env->NewObject(j_sentence_class, j_sentence_ctor, j_text, s_istart, s_iend, j_token_array, nullptr, j_docid);
    env->DeleteLocalRef(j_text);
    env->DeleteLocalRef(j_docid);
    env->DeleteLocalRef(j_token_array);
    return j_sentence;
}

/**
 * Returns an array of Java sentences
 *
 * @param env environment
 * @param parsed_sentences non-null array of parsed sentences
 * @return array of java sentences, Array<Array<Token!!>!!>!! or an exception is thrown
 * @throws IllegalStateException whenever
 * - classes Sentence and Token and their constructors could not be retrieved
 * - array of sentences could not be created
 * - one parsed sentence has no token
 */
jobjectArray
toJavaSentences(
        JNIEnv *env,
        const vector<sentence_t> &parsed_sentences) {

    // check
    int i = 0;
    for (const auto &parsed_sentence: parsed_sentences) {
        LOGD("Sentence #%d: %zu tokens\n", i++, parsed_sentence.size());
        if (parsed_sentence.empty()) {
            env->ThrowNew(env->FindClass(kIllegalStateException), "No token in sentence");
            return nullptr;
        }
    }

    // classes and constructors

    jclass j_sentence_class = check_not_null(env, env->FindClass(sentenceClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID j_sentence_ctor = check_not_null(env, env->GetMethodID(j_sentence_class, "<init>", sentenceCtor));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jclass j_token_class = check_not_null(env, env->FindClass(tokenClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID j_token_ctor = check_not_null(env, env->GetMethodID(j_token_class, "<init>", tokenCtor));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jclass j_mwtoken_class = check_not_null(env, env->FindClass(mwtokenClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID j_mwtoken_ctor = check_not_null(env, env->GetMethodID(j_mwtoken_class, "<init>", mwtokenCtor));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // make Array<Sentence> to return back to Java

    int n = static_cast<int>(parsed_sentences.size());
    jobjectArray j_sentence_array = check_not_null(env, env->NewObjectArray(n, j_sentence_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // fill Array<Sentence> to return back to Java

    i = 0;
    for (const auto &parsed_sentence: parsed_sentences) {
        jobject j_sentence = toJavaSentence(env, parsed_sentence, i, j_sentence_class, j_sentence_ctor, j_token_class, j_token_ctor, j_mwtoken_class, j_mwtoken_ctor);
        if (env->ExceptionCheck() || j_sentence == nullptr) {
            env->DeleteLocalRef(j_sentence_array);
            return nullptr;
        }
        env->SetObjectArrayElement(j_sentence_array, i, j_sentence);
        env->DeleteLocalRef(j_sentence);
        i++;
    }
    return j_sentence_array;
}

// N A T I V E   I N T E R F A C E

string model_path;

// u t i l s

/**
 * Native version function callable from Java
 */
extern "C" JNIEXPORT
jint
JNICALL Java_org_syntaxnet_JNI_version__(
        JNIEnv *env,
        jobject j_type) {

    (void) env;
    (void) j_type;
    int version = sni_version();
    return version;
}

/**
 * Native modelPath function callable from Java
 */
extern "C" JNIEXPORT
jstring
JNICALL Java_org_syntaxnet_JNI_modelPath(
        JNIEnv *env,
        jobject /* thiz */) {

    const string &s = model_path;
    return env->NewStringUTF(s.c_str());
}

// l o a d / u n l o a d

/**
 * Native load function callable from Java
 */
extern "C" JNIEXPORT
jlong
JNICALL Java_org_syntaxnet_JNI_load(
        JNIEnv *env,
        jobject j_type,
        jstring j_model_path) {

    (void) j_type;
    model_path = jniStringToString(env, j_model_path);

    long handle = sni_load_h(model_path.c_str());
    return handle;
}

/**
 * Native unload function callable from Java
 */
extern "C" JNIEXPORT
void
JNICALL Java_org_syntaxnet_JNI_unload(
        JNIEnv *env,
        jobject j_type,
        jlong j_handle) {

    (void) j_type;
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot free null handle");
        return;
    }
    sni_unload_h(static_cast<long>(j_handle));
}

// p a r s e

/**
 * Native parse function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_parse(
        JNIEnv *env,
        jobject j_type,
        jlong j_handle,
        jobjectArray j_input_texts) {

    (void) j_type;
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);

    // parse
    vector<sentence_t> parsed_sentences;
    sni_parse_h(static_cast<long>(j_handle), texts, parsed_sentences);
    LOGD("Parsed %zu sentences\n", parsed_sentences.size());

    // interpret
    jobjectArray j_sentence_array = toJavaSentences(env, parsed_sentences);

    LOGD("Parsing done\n");
    return j_sentence_array;
}

/**
 * Native splitParse function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_splitParse(
        JNIEnv *env,
        jobject j_type,
        jlong j_handle,
        jobjectArray j_input_texts) {

    (void) j_type;
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);

    // parse
    vector<sentence_t> parsed_sentences;
    sni_parse_h(static_cast<long>(j_handle), texts, parsed_sentences);
    LOGD("Parsed %zu sentences\n", parsed_sentences.size());

    // interpret
    jobjectArray j_sentence_array = toJavaSentences(env, parsed_sentences);

    LOGD("Parsing done\n");
    return j_sentence_array;
}

/**
 * Native segment function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_segment__J_3Ljava_lang_String_2(
        JNIEnv *env,
        jobject j_type,
        jlong j_handle,
        jobjectArray j_input_texts) {

    (void) j_type;
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);

    // parse
    vector<sentence_t> segmented_sentences;
    sni_segment_h(static_cast<long>(j_handle), texts, segmented_sentences);
    LOGD("Segmented %zu sentences\n", segmented_sentences.size());

    // interpret
    jobjectArray j_sentence_array = toJavaSentences(env, segmented_sentences);

    LOGD("Segmenting done\n");
    return j_sentence_array;
}
