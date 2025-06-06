/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#include <jni.h>
#include <string>
#include <vector>
#include <unistd.h>
#include <iostream>
#include <unistd.h>
#include <codecvt>
#include <locale>

#include <android/log.h>

#include "syntaxnet2/iface_h.h"

#define LOG_TAG    "SYNTAXNET_JNI"

//#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
//#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

using namespace std;

const char kIllegalStateException[] = "java/lang/IllegalStateException";

const char sentenceClass[] = "org/depparse/Sentence";
const char tokenClass[] = "org/depparse/Token";
const char sentenceCtor[] = "(Ljava/lang/String;II[Lorg/depparse/Token;Ljava/lang/String;)V";
const char tokenCtor[] = "(IILjava/lang/String;IILjava/lang/String;Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;)V";

// C H E C K   H E L P E R S

/**
 * Check nullity and throw an IllegalStateException if the object is null
 * @tparam T object type
 * @param env environment
 * @param t object to check
 * @return object
 */
template<typename T>
T CheckNotNull(JNIEnv *env, T &&t) {
    if (t == nullptr) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "");
        return nullptr;
    }
    return std::forward<T>(t);
}

// T O   C H A R   I N D E X   H E L P E R

/**
 * Creates a mapping from UTF-8 byte positions to character indices.
 *
 * @param text The UTF-8 encoded string to process
 * @return A vector where each index represents a byte position, and the value is the corresponding character index
 */
vector<int> getCharIndices(const string &text) {
    // Get the byte size of the UTF-8 string
    size_t byteSize = text.size();

    // Create vector for byte-to-char index mapping (size + 1 to include position after last byte)
    vector<int> byteToCharIndex(byteSize + 1);

    // Convert to wstring to work with Unicode code points properly
    wstring_convert<codecvt_utf8<wchar_t>> converter;
    wstring wideText = converter.from_bytes(text);

    // Iterate through each Unicode character
    size_t bytePos = 0;
    for (size_t i = 0; i < wideText.size(); ++i) {
        wchar_t ch = wideText[i];

        // Convert this single character back to UTF-8 to get its byte count
        string charBytes = converter.to_bytes(ch);
        size_t charByteCount = charBytes.size();

        // Fill in the byte-to-char mapping for each byte in this character
        for (size_t j = 0; j < charByteCount; ++j) {
            if (bytePos + j < byteToCharIndex.size()) {
                byteToCharIndex[bytePos + j] = static_cast<int>(i);
            }
        }
        bytePos += charByteCount;
    }

    // Set the final position
    if (bytePos < byteToCharIndex.size()) {
        byteToCharIndex[bytePos] = static_cast<int>(wideText.size());
    }

    return byteToCharIndex;
}

// C O N V E R S I O N   H E L P E R S

/*
inline
void stringVectorToCStringArray(const vector<string> &texts, const char *c_texts[], const int n) {
    for (int i = 0; i < n; i++) {
        c_texts[i] = texts[i].c_str();
    }
}
*/

// F R O M   J A V A

extern
vector<string> jniStringArrayToVector(JNIEnv *env, jobjectArray string_array) {
    int count = env->GetArrayLength(string_array);
    vector<string> result;
    for (int i = 0; i < count; i++) {
        auto jstr = reinterpret_cast<jstring>(env->GetObjectArrayElement(string_array, i));
        const char *raw_str = env->GetStringUTFChars(jstr, JNI_FALSE);
        result.emplace_back(raw_str);
        env->ReleaseStringUTFChars(jstr, raw_str);
    }
    return result;
}

string jniStringToString(JNIEnv *env, jstring jstr) {
    const char *raw_str = env->GetStringUTFChars(jstr, JNI_FALSE);
    string result(raw_str);
    env->ReleaseStringUTFChars(jstr, raw_str);
    return result;
}

// T O   J A V A

/**
 * Returns a Java sentence
 *
 * @param env environment
 * @param parsed_sentence parsed C sentence, non-null
 * @param sentenceIndex sentence index
 * @param sentence_class java class of sentence
 * @param sentence_ctor java constructor
 * @param token_class java class of token
 * @param token_ctor java constructor of token
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
        jclass sentence_class,
        jmethodID sentence_ctor,
        jclass token_class,
        jmethodID token_ctor) {

    int nTokens = (int) parsed_sentence.size();

    // make java array of tokens Token[] to be field of Sentence class and return back to Java

    jobjectArray jtoken_array = CheckNotNull(env, env->NewObjectArray(nTokens - 1, token_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // Sentence text as token[0] (text as token[0]["text"], docid as token[0]["docid"]

    const token_t &token0 = parsed_sentence[0];

    const string &text = token0.at("text");
    const vector<int> toCharIndices = getCharIndices(text);

    auto it0 = token0.find("docid");
    const string &docid = it0 != token0.end() ? it0->second : "";

    it0 = token0.find("start");
    const string &s_start = it0 != token0.end() ? it0->second : "-1";
    int s_istart = strtol(s_start.c_str(), nullptr, 10);
    if (s_istart != -1)
        s_istart = toCharIndices[s_istart];

    it0 = token0.find("end");
    const string &s_end = it0 != token0.end() ? it0->second : "-1";
    int s_iend = strtol(s_end.c_str(), nullptr, 10);
    if (s_iend != -1)
        s_iend = toCharIndices[s_iend];

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

        jstring jword = CheckNotNull(env, env->NewStringUTF(word.c_str()));
        int t_istart = strtol(t_start.c_str(), nullptr, 10);
        int t_iend = strtol(t_end.c_str(), nullptr, 10);
        if (t_istart != -1)
            t_istart = toCharIndices[t_istart];
        if (t_iend != -1)
            t_iend = toCharIndices[t_iend];
        jstring jcategory = CheckNotNull(env, env->NewStringUTF(category.c_str()));
        jstring jtag = CheckNotNull(env, env->NewStringUTF(tag.c_str()));
        jstring jlabel = CheckNotNull(env, env->NewStringUTF(label.c_str()));
        int ihead = strtol(head.c_str(), nullptr, 10);
        int ibreaklevel = strtol(breaklevel.c_str(), nullptr, 10);

        // make java token
        // jword: String!!, possibly ""
        // istart: Int!!, possibly -1
        // iend: Int!!, possibly -1
        // jcategory!!: String!!, possibly ""
        // jtag: String!!
        // ihead: Int!!, possibly -1
        // jlabel: String!!, possibly ""
        // ibreaklevel: Int!!, possibly -1
        jobject jtoken = CheckNotNull(env, env->NewObject(token_class, token_ctor, sentenceIndex, j - 1, jword, t_istart, t_iend, jcategory, jtag, ihead, jlabel, ibreaklevel, nullptr));
        if (env->ExceptionCheck()) {
            return nullptr;
        }

        // set token in array

        env->SetObjectArrayElement(jtoken_array, j - 1, jtoken);
    }

    // make sentence

    jstring jtext = CheckNotNull(env, env->NewStringUTF(text.c_str()));
    jstring jdocid = CheckNotNull(env, env->NewStringUTF(docid.c_str()));
    jobject jsentence = env->NewObject(sentence_class, sentence_ctor, jtext, s_istart, s_iend, jtoken_array, jdocid);
    return jsentence;
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

    jclass sentence_class = CheckNotNull(env, env->FindClass(sentenceClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID sentence_ctor = CheckNotNull(env, env->GetMethodID(sentence_class, "<init>", sentenceCtor));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jclass token_class = CheckNotNull(env, env->FindClass(tokenClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID token_ctor = CheckNotNull(env, env->GetMethodID(token_class, "<init>", tokenCtor));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // make Array<Sentence> to return back to Java

    int n = static_cast<int>(parsed_sentences.size());
    jobjectArray sentence_array = CheckNotNull(env, env->NewObjectArray(n, sentence_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // fill Array<Sentence> to return back to Java

    i = 0;
    for (const auto &parsed_sentence: parsed_sentences) {
        jobject jsentence = toJavaSentence(env, parsed_sentence, i, sentence_class, sentence_ctor, token_class, token_ctor);
        env->SetObjectArrayElement(sentence_array, i, jsentence);
        i++;
    }
    return sentence_array;
}

// N A T I V E   I N T E R F A C E

string model_path;

// u t i l s

/**
 * Native version function callable from Java
 */
extern "C" JNIEXPORT
jint
JNICALL Java_org_syntaxnet2_JNI2_version(
        JNIEnv *env,
        jobject type) {

    (void) env;
    (void) type;
    int version = sni_version();
    return version;
}

/**
 * Native modelPath function callable from Java
 */
extern "C" JNIEXPORT
jstring
JNICALL Java_org_syntaxnet2_JNI2_modelPath(
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
JNICALL Java_org_syntaxnet2_JNI2_load(
        JNIEnv *env,
        jobject type,
        jstring j_model_path) {

    (void) type;
    model_path = jniStringToString(env, j_model_path);

    long handle = sni_load_h(model_path.c_str());
    return handle;
}

/**
 * Native unload function callable from Java
 */
extern "C" JNIEXPORT
void
JNICALL Java_org_syntaxnet2_JNI2_unload(
        JNIEnv *env,
        jobject type,
        jlong handle) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot free null handle");
        return;
    }
    sni_unload_h(static_cast<long>(handle));
}

// p a r s e

/**
 * Native parse function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_parse(
        JNIEnv *env,
        jobject type,
        jlong handle,
        jobjectArray input_texts) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<sentence_t> parsed_sentences;
    sni_parse_h(static_cast<long>(handle), texts, parsed_sentences);
    LOGD("Parsed %zu sentences\n", parsed_sentences.size());

    // interpret
    jobjectArray sentence_array = toJavaSentences(env, parsed_sentences);

    LOGD("Parsing done\n");
    return sentence_array;
}

/**
 * Native splitParse function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_splitParse(
        JNIEnv *env,
        jobject type,
        jlong handle,
        jobjectArray input_texts) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<sentence_t> parsed_sentences;
    sni_parse_h(static_cast<long>(handle), texts, parsed_sentences);
    LOGD("Parsed %zu sentences\n", parsed_sentences.size());

    // interpret
    jobjectArray sentence_array = toJavaSentences(env, parsed_sentences);

    LOGD("Parsing done\n");
    return sentence_array;
}

/**
 * Native segment function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_segment(
        JNIEnv *env,
        jobject type,
        jlong handle,
        jobjectArray input_texts) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<sentence_t> segmented_sentences;
    sni_segment_h(static_cast<long>(handle), texts, segmented_sentences);
    LOGD("Segmented %zu sentences\n", segmented_sentences.size());

    // interpret
    jobjectArray sentence_array = toJavaSentences(env, segmented_sentences);

    LOGD("Segmenting done\n");
    return sentence_array;
}
