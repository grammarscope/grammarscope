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
#include <regex>
#include <iomanip>   // std::setfill, std::setw, std::hex  ← required

#include <android/log.h>

#include "utf8.h"

#include "udpipe/iface_h.h"

#define LOG_TAG    "UDPIPE_JNI"

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
T CheckNotNull(JNIEnv *env, T &&t) {
    if (t == nullptr) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "");
        return nullptr;
    }
    return std::forward<T>(t);
}

// S P L I T

void split(const string &s, const char c, vector<string> &v) {
    string::size_type i = 0;
    string::size_type j = s.find(c);
    while (j != string::npos) {
        v.push_back(s.substr(i, j - i));
        i = ++j;
        j = s.find(c, j);
        if (j == string::npos)
            v.push_back(s.substr(i, s.length()));
    }
}

// Decode UTF-8 bytes into code points, then print each as hex
string to_hex_codepoints(const std::string &s) {
    std::ostringstream oss;
    oss << std::hex << std::setfill('0');

    size_t i = 0;
    while (i < s.size()) {
        unsigned char c = static_cast<unsigned char>(s[i]);
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
            unsigned char cc = static_cast<unsigned char>(s[i + k]);
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
vector<string> jniStringArrayToVector(JNIEnv *env, jobjectArray string_array) {
    int count = env->GetArrayLength(string_array);
    vector<string> result;
    for (int i = 0; i < count; i++) {
        auto jstr = reinterpret_cast<jstring>(env->GetObjectArrayElement(string_array, i));
        const char *raw_str = env->GetStringUTFChars(jstr, JNI_FALSE);
        result.emplace_back(raw_str);
        env->ReleaseStringUTFChars(jstr, raw_str);

        string hex = to_hex_codepoints(result[0]);
        LOGD("Input %d Codepoints cps = %s", i, hex.c_str());
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
 * @param mwttoken_class java class of mwtoken
 * @param mwttoken_ctor java constructor of mwtoken
 * @return Sentence with tokens field being Array<Token!!>!! otherwise
 * @throws IllegalStateException whenever CheckNull encounters a null value, that is
 * -word is null (should not happen)
 * -category, label, tag are guarded against being null by being set to empty string at token level
 * -text, docids are guarded against being null by being set to empty string at sentence level
 */
jobject
toJavaSentence(
        JNIEnv *env,
        const sentence_t &parsed_sentence,
        int sentenceIndex,
        jclass sentence_class,
        jmethodID sentence_ctor,
        jclass token_class,
        jmethodID token_ctor,
        jclass mwtoken_class,
        jmethodID mwtoken_ctor) {
    int nTokens = static_cast<int>(parsed_sentence.size());

    // make java array of tokens Token[] to be field of Sentence class and return back to Java

    jobjectArray jtoken_array = CheckNotNull(env, env->NewObjectArray(nTokens - 1, token_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    int sentence_start = 0;

    // Sentence text as token[0] (text as token[0]["text"], docid as token[0]["docid"]

    const token_t &token0 = parsed_sentence[0];

    const string &text = token0.at("text");

    auto it0 = token0.find("docid");
    const string &docid = it0 != token0.end() ? it0->second : "";

    it0 = token0.find("start");
    const string &s_start = it0 != token0.end() ? it0->second : "-1";
    int s_istart = static_cast<int>(strtol(s_start.c_str(), nullptr, 10));

    it0 = token0.find("end");
    const string &s_end = it0 != token0.end() ? it0->second : "-1";
    int s_iend = static_cast<int>(strtol(s_end.c_str(), nullptr, 10));

    // Tokens

    for (int j = 1; j < nTokens; j++) {
        // collect token data

        const token_t &token = parsed_sentence[j];

        auto it = token.find("word");
        const string &word = it != token.end() ? it->second : "";

        it = token.find("category");
        const string &category = it != token.end() ? it->second : "";

        string tag;
        it = token.find("upostag");
        const string &upostag = it != token.end() ? it->second : "";
        if (!upostag.empty()) {
            tag += "name: 'upostag' value: '";
            tag += upostag;
            tag += '\'';
        }
        it = token.find("xpostag");
        const string &xpostag = it != token.end() ? it->second : "";
        if (!xpostag.empty()) {
            if (!tag.empty())
                tag += ' ';
            tag += "name: 'xpostag' value: '";
            tag += xpostag;
            tag += '\'';
        }
        it = token.find("lemma");
        const string &lemma = it != token.end() ? it->second : "";
        if (!lemma.empty()) {
            if (!tag.empty())
                tag += ' ';
            tag += "name: 'lemma' value: '";
            tag += lemma;
            tag += '\'';
        }
        it = token.find("feats");
        const string &feats = it != token.end() ? it->second : "";
        if (!feats.empty()) {
            if (!tag.empty())
                tag += ' ';
            //tag += "name: 'feats' value: '";
            //tag += feats;
            //tag += "'";
            vector<string> features;
            split(feats, '|', features);
            for (const auto &feature: features) {
                vector<string> name_value;
                split(feature, '=', name_value);
                if (name_value.size() == 2) {
                    string &name = name_value[0];
                    string &value = name_value[1];
                    name[0] = static_cast<char>(tolower(name[0]));
                    tag += "name: '";
                    tag += name;
                    tag += "' value: '";
                    tag += value;
                    tag += '\'';
                }
            }
        }

        it = token.find("head");
        const string &head = it != token.end() ? it->second : "-1";

        it = token.find("label");
        const string &label = it != token.end() ? it->second : "";

        it = token.find("start");
        const string &t_start = it != token.end() ? it->second : "-1";

        it = token.find("end");
        const string &t_end = it != token.end() ? it->second : "-1";

        // mwt
        jobject jmwtoken = nullptr;
        it = token.find("mwt_misc");
        const string &mwt_misc = it != token.end() ? it->second : "";
        if (mwt_misc != "") {
            it = token.find("mwt_first");
            const string &mwt_first = it != token.end() ? it->second : "-1";
            int mwt_ifirst = static_cast<int>(strtol(mwt_first.c_str(), nullptr, 10));
            it = token.find("mwt_last");
            const string &mwt_last = it != token.end() ? it->second : "-1";
            int mwt_ilast = static_cast<int>(strtol(mwt_last.c_str(), nullptr, 10));
            it = token.find("mwt_form");
            const string &mwt_form = it != token.end() ? it->second : "";
            int mwt_istart = -1;
            int mwt_iend = -1;
            if (mwt_misc != "") {
                std::regex pattern(R"(TokenRange=(\d+):(\d+))");
                std::smatch matches;
                // Use std::regex_search if the pattern is embedded in a larger string, or std::regex_match if the entire string must match the pattern.
                if (std::regex_search(mwt_misc, matches, pattern)) {
                    // matches[0] contains the entire matched string ("TokenRange:10,25")
                    // matches[1] contains the first capture group (start)
                    // matches[2] contains the second capture group (end)
                    string mwt_start = matches[1].str();
                    string mwt_end = matches[2].str();
                    mwt_istart = static_cast<int>(strtol(mwt_start.c_str(), nullptr, 10));
                    mwt_iend = static_cast<int>(strtol(mwt_end.c_str(), nullptr, 10));
                }
            }
            jstring jmwt_form = CheckNotNull(env, env->NewStringUTF(mwt_form.c_str()));
            if (env->ExceptionCheck()) {
                return nullptr;
            }
            jmwtoken = CheckNotNull(env, env->NewObject(mwtoken_class, mwtoken_ctor,
                                                        sentenceIndex, //sentenceIndex: Int
                                                        -1,// index: Int, // possibly -1
                                                        jmwt_form, // word: String, // not null
                                                        mwt_istart, // start: Int
                                                        mwt_iend, // end: Int, inclusive, relative to sentence text
                                                        mwt_ifirst, // firstToken: Int
                                                        mwt_ilast // lastToken: Int, // inclusive
            ));
            if (env->ExceptionCheck()) {
                return nullptr;
            }
        }

        it = token.find("breaklevel");
        const string &breaklevel = it != token.end() ? it->second : "-1";

        it = token.find("deps");
        const string &deps = it != token.end() ? it->second : "";

        LOGD("Token #%d '%s' [%s-%s] label=%s tag=%s head=%s x=<%s> mwt=%s\n", j, word.c_str(), t_start.c_str(), t_end.c_str(), label.c_str(), tag.c_str(), head.c_str(), deps.c_str(), "none");

        // token constructor parameters

        jstring jword = CheckNotNull(env, env->NewStringUTF(word.c_str()));
        int t_istart = static_cast<int>(strtol(t_start.c_str(), nullptr, 10));
        int t_iend = static_cast<int>(strtol(t_end.c_str(), nullptr, 10));
        if (t_iend != -1)
            t_iend--;
        if (j == 1 && t_istart != 0) {
            sentence_start = t_istart;
        }
        t_istart -= sentence_start;
        t_iend -= sentence_start;
        jstring jcategory = CheckNotNull(env, env->NewStringUTF(category.c_str()));
        jstring jtag = CheckNotNull(env, env->NewStringUTF(tag.c_str()));
        jstring jlabel = CheckNotNull(env, env->NewStringUTF(label.c_str()));
        int ihead = static_cast<int>(strtol(head.c_str(), nullptr, 10));
        if (ihead > 0) // O-based
            ihead--;
        int ibreaklevel = static_cast<int>(strtol(breaklevel.c_str(), nullptr, 10));
        jstring jdeps = CheckNotNull(env, env->NewStringUTF(deps.c_str()));

        // make java token
        // jword: String!!, possibly ""
        // istart: Int!!, possibly -1
        // iend: Int!!, possibly -1
        // jcategory!!: String!!, possibly ""
        // jtag: String!!
        // ihead: Int!!, possibly -1
        // jlabel: String!!, possibly ""
        // ibreaklevel: Int!!, possibly -1
        jobject jtoken = CheckNotNull(env, env->NewObject(token_class, token_ctor, sentenceIndex, j - 1, jword,
                                                          t_istart, t_iend, jcategory, jtag, ihead, jlabel, ibreaklevel,
                                                          jdeps, jmwtoken));
        if (env->ExceptionCheck()) {
            return nullptr;
        }

        // set token in array

        env->SetObjectArrayElement(jtoken_array, j - 1, jtoken);
    }

    // make sentence

    jstring jtext = CheckNotNull(env, env->NewStringUTF(text.c_str()));
    jstring jdocid = CheckNotNull(env, env->NewStringUTF(docid.c_str()));
    jobject jsentence = env->NewObject(sentence_class, sentence_ctor, jtext, s_istart, s_iend, jtoken_array, nullptr, jdocid);
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
    jclass mwtoken_class = CheckNotNull(env, env->FindClass(mwtokenClass));
    if (env->ExceptionCheck()) {
        return nullptr;
    }
    jmethodID mwtoken_ctor = CheckNotNull(env, env->GetMethodID(mwtoken_class, "<init>", mwtokenCtor));
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
        jobject jsentence = toJavaSentence(env, parsed_sentence, i, sentence_class, sentence_ctor, token_class, token_ctor, mwtoken_class, mwtoken_ctor);
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
JNICALL Java_org_udpipe_JNI_version(
        JNIEnv *env,
        jobject type) {
    (void) env;
    (void) type;
    int version = udpipe_version();
    return version;
}

/**
 * Native modelPath function callable from Java
 */
extern "C" JNIEXPORT
jstring
JNICALL Java_org_udpipe_JNI_modelPath(
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
JNICALL Java_org_udpipe_JNI_load(
        JNIEnv *env,
        jobject type,
        jstring j_model_path) {
    (void) type;
    model_path = jniStringToString(env, j_model_path);

    long handle = udpipe_load_h(model_path.c_str());
    return handle;
}

/**
 * Native unload function callable from Java
 */
extern "C" JNIEXPORT
void
JNICALL Java_org_udpipe_JNI_unload(
        JNIEnv *env,
        jobject type,
        jlong handle) {
    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot free null handle");
        return;
    }
    udpipe_unload_h(static_cast<long>(handle));
}

// p a r s e

/**
 * Native parse function callable from Java
 */
extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_udpipe_JNI_parse(
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
    udpipe_parse_h(static_cast<long>(handle), texts, parsed_sentences);
    LOGD("Parsed %zu sentences\n", parsed_sentences.size());

    // interpret
    jobjectArray sentence_array = toJavaSentences(env, parsed_sentences);

    LOGD("Parsing done\n");
    return sentence_array;
}
