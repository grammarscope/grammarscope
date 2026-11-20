/* Copyright 2026
 * Bernard Bou
 * 1313ou@gmail.com */

#include <jni.h>
#include <string>
#include <vector>
#include <unistd.h>
#include <iostream>

#include "syntaxnet/glue.h"
#include "syntaxnet/glue_protobuf.h"

#define  LOG_TAG    "SYNTAXNET_JNI"

//#define  LOGE(...)  printf(__VA_ARGS__)
//#define  LOGW(...)  printf(__VA_ARGS__)
#define  LOGD(...)   printf(__VA_ARGS__)
//#define  LOGI(...)  printf(__VA_ARGS__)

using namespace std;

const char illegalStateExceptionClass[] = "java/lang/IllegalStateException";

// C H E C K   H E L P E R S

template<typename T>
T check_not_null(JNIEnv *env, T &&t) {
    if (t == nullptr) {
        if (!env->ExceptionCheck()) {
            env->ThrowNew(env->FindClass(illegalStateExceptionClass), "");
        }
        return nullptr;
    }
    return std::forward<T>(t);
}

// C O N V E R S I O N   H E L P E R S

extern
vector<string> jniStringArrayToVector(JNIEnv *env, jobjectArray j_strs);

jobjectArray toJavaByteArray(JNIEnv *env, const vector<string> &protos) {
    // element class
    jclass j_byte_array_class = check_not_null(env, env->FindClass("[B")); // '[' for array of, 'B' for byte
    if (env->ExceptionCheck() || j_byte_array_class == nullptr) {
        return nullptr;
    }

    // allocate array
    int n = (int) protos.size();
    jobjectArray j_array_of_byte_arrays = check_not_null(env, env->NewObjectArray(n, j_byte_array_class, nullptr));
    if (env->ExceptionCheck() || j_array_of_byte_arrays == nullptr) {
        env->DeleteLocalRef(j_byte_array_class);
        return nullptr;
    }

    for (int i = 0; i < n; i++) {
        // element byte array
        int s = (int) protos[i].size();
        jbyteArray j_byte_array = check_not_null(env, env->NewByteArray(s));
        if (env->ExceptionCheck() || j_byte_array == nullptr) {
            env->DeleteLocalRef(j_byte_array_class);
            env->DeleteLocalRef(j_array_of_byte_arrays);
            return nullptr;
        }

        // copy data
        const auto *data = reinterpret_cast<const jbyte *>(protos[i].data());
        env->SetByteArrayRegion(j_byte_array, 0, s, data);

        // array element
        env->SetObjectArrayElement(j_array_of_byte_arrays, i, j_byte_array);
        env->DeleteLocalRef(j_byte_array);
    }
    env->DeleteLocalRef(j_byte_array_class);
    return j_array_of_byte_arrays;
}

// P A R S E

extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_parseProtobufs(JNIEnv *env, jobject /*thiz*/, jlong j_handle, jobjectArray j_input_texts) {
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(illegalStateExceptionClass), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // parse
    vector<string> parsed_sentence_protos;
    sni_parse_h_pb(static_cast<long>(j_handle), texts, parsed_sentence_protos);
    LOGD("Parsed %zu sentences\n", parsed_sentence_protos.size());

    // interpret
    jobjectArray j_sentence_proto_array = toJavaByteArray(env, parsed_sentence_protos);

    LOGD("Parsing done\n");
    return j_sentence_proto_array;
}

// S P L I T P A R S E

extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_splitParseProtobufs(JNIEnv *env, jobject /*thiz*/, jlong j_handle, jobjectArray j_input_texts) {
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(illegalStateExceptionClass), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // parse
    vector<string> split_parsed_sentence_protos;
    sni_split_parse_h_pb(static_cast<long>(j_handle), texts, split_parsed_sentence_protos);
    LOGD("Parsed %zu sentences\n", split_parsed_sentence_protos.size());

    // interpret
    jobjectArray j_sentence_proto_array = toJavaByteArray(env, split_parsed_sentence_protos);

    LOGD("Parsing done\n");
    return j_sentence_proto_array;
}

// S E G M E N T

extern "C" JNIEXPORT
jobjectArray
JNICALL Java_org_syntaxnet_JNI_segmentProtobufs(JNIEnv *env, jobject /*thiz*/, jlong j_handle, jobjectArray j_input_texts) {
    if (j_handle == 0) {
        env->ThrowNew(env->FindClass(illegalStateExceptionClass), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, j_input_texts);
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    // parse
    vector<string> segmented_sentence_protos;
    sni_segment_h_pb(static_cast<long>(j_handle), texts, segmented_sentence_protos);
    LOGD("Segmented %zu sentences\n", segmented_sentence_protos.size());

    // result
    jobjectArray j_sentence_proto_array = toJavaByteArray(env, segmented_sentence_protos);

    LOGD("Segmenting done\n");
    return j_sentence_proto_array;
}

