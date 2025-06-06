/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <unistd.h>
#include <iostream>

#ifdef USE_C
#include "syntaxnet_c/iface2.h"
#else

#include "syntaxnet1/iface2.h"

#endif

#define  LOG_TAG    "SYNTAXNET_JNI"

//#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
//#define  LOGW(...)  __android_log_print(ANDROID_LOG_WARN,LOG_TAG,__VA_ARGS__)
#define  LOGD(...)  __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
//#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

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

// I N T E R F A C E

string model_path;

extern "C" JNIEXPORT
jstring
JNICALL Java_org_syntaxnet1_JNI1_infoJNI(
        JNIEnv *env,
        jobject type) {

    (void) type;
    string s = "Version ";
    int v = SNIversion();
    s += to_string(v);
    s += "\nPath ";
    s += model_path;
    return env->NewStringUTF(s.c_str());
}

// L O A D / U N L O A D

extern "C" JNIEXPORT
jlong
JNICALL Java_org_syntaxnet1_JNI1_loadJNI(
        JNIEnv *env,
        jobject type,
        jstring j_model_path) {

    (void) type;
    model_path = jniStringToString(env, j_model_path);

    long handle = SNIload_h(model_path.c_str());
    return handle;
}

extern "C" JNIEXPORT
void
JNICALL Java_org_syntaxnet1_JNI1_unloadJNI(
        JNIEnv *env,
        jobject type,
        jlong handle) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot free null handle");
        return;
    }
    SNIunload_h(static_cast<long>(handle));
}

// P R E D I C T

//unused
/*
string
parsed_to_string(sentence_t &parsed_sentence) {
    string result = parsed_sentence[0]["text"] + "\n";

    long n = parsed_sentence.size();
    for (int i = 1; i < n; i++) {
        token_t &token = parsed_sentence[i];

        result += "\t" + token["word"];

        result += " (";
        result += token["start"];
        result += "-";
        result += token["end"];
        result += ")";

        result += "\n";

        auto it = token.find("category");
        if (it != token.end()) {
            result += "\t\t" + it->second + "\n";
        }
        it = token.find("tag");
        if (it != token.end()) {
            result += "\t\t" + it->second + "\n";
        }
        it = token.find("head");
        if (it != token.end()) {
            result += "\t\t" + it->second + "\n";
        }
        it = token.find("breaklevel");
        if (it != token.end()) {
            result += "\t\t" + it->second + "\n";
        }
        it = token.find("label");
        if (it != token.end()) {
            result += "\t\t" + it->second + "\n";
        }
    }
    return result;
}
*/

extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet1_JNI1_predictJNI(
        JNIEnv *env,
        jobject type,
        jlong handle,
        jobjectArray input_texts) {

    (void) type;
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot predict with null handle");
        return nullptr;
    }

    // input
    const vector<string> in = jniStringArrayToVector(env, input_texts);
    int n = (int) in.size();

    // parse
    sentence_t parsed_sentences[n];
    SNIinfer_h(static_cast<long>(handle), in, parsed_sentences);
    LOGD("Predicted %d sentences", n);

    // check
    for (int i = 0; i < n; i++) {
        LOGD("Predicted sentence #%d: %zu tokens", i, parsed_sentences[i].size());
        if (parsed_sentences[i].empty()) {
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

    // Sentence[] to return back to Java
    jobjectArray sentence_array = CheckNotNull(env, env->NewObjectArray(n, sentence_class, nullptr));
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    for (int i = 0; i < n; i++) {
        const sentence_t &parsed_sentence = parsed_sentences[i];
        int nTokens = (int) parsed_sentence.size();

        // Token[] to return back to Java
        jobjectArray jtoken_array = CheckNotNull(env, env->NewObjectArray(nTokens - 1, token_class, nullptr));
        if (env->ExceptionCheck()) {
            return nullptr;
        }

        for (int j = 1; j < nTokens; j++) {
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
            const string &start = it != token.end() ? it->second : "-1";

            it = token.find("end");
            const string &end = it != token.end() ? it->second : "-1";

            it = token.find("breaklevel");
            const string &breaklevel = it != token.end() ? it->second : "-1";

            LOGD("Predicted token #%s %s %s %s %s", word.c_str(), label.c_str(), category.c_str(), tag.c_str(), head.c_str());

            jstring jword = CheckNotNull(env, env->NewStringUTF(word.c_str()));
            int istart = strtol(start.c_str(), nullptr, 10);
            int iend = strtol(end.c_str(), nullptr, 10);
            jstring jcategory = CheckNotNull(env, env->NewStringUTF(category.c_str()));
            jstring jtag = CheckNotNull(env, env->NewStringUTF(tag.c_str()));
            jstring jlabel = CheckNotNull(env, env->NewStringUTF(label.c_str()));
            int ihead = strtol(head.c_str(), nullptr, 10);
            int ibreaklevel = strtol(breaklevel.c_str(), nullptr, 10);

            jobject jtoken = CheckNotNull(env, env->NewObject(token_class, token_ctor, i, j - 1, jword, istart, iend, jcategory, jtag, ihead, jlabel, ibreaklevel, nullptr));
            if (env->ExceptionCheck()) {
                return nullptr;
            }
            env->SetObjectArrayElement(jtoken_array, j - 1, jtoken);
        }

        // Sentence as token0 (text as token[0]["text"], docid as token[0]["docid"]
        const token_t &token0 = parsed_sentence[0];

        const string &text = token0.at("text");

        auto it0 = token0.find("docid");
        const string &docid = it0 != token0.end() ? it0->second : "";

        it0 = token0.find("start");
        const string &start = it0 != token0.end() ? it0->second : "-1";
        int istart = strtol(start.c_str(), nullptr, 10);

        it0 = token0.find("end");
        const string &end = it0 != token0.end() ? it0->second : "-1";
        int iend = strtol(end.c_str(), nullptr, 10);

        jstring jtext = CheckNotNull(env, env->NewStringUTF(text.c_str()));
        jstring jdocid = CheckNotNull(env, env->NewStringUTF(docid.c_str()));
        jobject jsentence = env->NewObject(sentence_class, sentence_ctor, jtext, istart, iend, jtoken_array, jdocid);
        env->SetObjectArrayElement(sentence_array, i, jsentence);
    }

    LOGD("Predicted done");
    return sentence_array;
}

/*
 public class org.depparse.Sentence {
     public org.depparse.Sentence(java.lang.String, int, int, org.depparse.Token[], java.lang.String);
     descriptor: (Ljava/lang/String;II[Lorg/depparse/Token;Ljava/lang/String;)V
 }
 */

/*
public class org.depparse.Token {
  public Token(final String word, final int start, final int end, final String category, final String tag, final int head, final String label, final int breaklevel)
  public org.depparse.Token(java.lang.String, int, int, java.lang.String, java.lang.String, int, java.lang.String, int);
  descriptor: (Ljava/lang/String;IILjava/lang/String;Ljava/lang/String;ILjava/lang/String;ILjava/lang/String;)V
}
*/

