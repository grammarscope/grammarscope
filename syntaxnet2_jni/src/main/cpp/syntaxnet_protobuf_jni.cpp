/* Copyright 2018
 * Bernard Bou
 * 1313ou@gmail.com */

#include <jni.h>
#include <string>
#include <vector>
#include <unistd.h>
#include <iostream>

#include "syntaxnet2/iface_h.h"
#include "syntaxnet2/iface_hp.h"

#define  LOG_TAG    "SYNTAXNET_JNI"

//#define  LOGE(...)  printf(__VA_ARGS__)
//#define  LOGW(...)  printf(__VA_ARGS__)
#define  LOGD(...)   printf(__VA_ARGS__)
//#define  LOGI(...)  printf(__VA_ARGS__)

using namespace std;

const char kIllegalStateException[] = "java/lang/IllegalStateException";

// C H E C K   H E L P E R S

template<typename T>
T CheckNotNull(JNIEnv *env, T &&t) {
    if (t == nullptr) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "");
        return nullptr;
    }
    return std::forward<T>(t);
}

// C O N V E R S I O N   H E L P E R S

extern
vector<string> jniStringArrayToVector(JNIEnv *env, jobjectArray string_array);

jobjectArray toJavaByteArray(JNIEnv *env, const vector<string> &protos) {
    // element class
    jclass byte_array_class = CheckNotNull(env, env->FindClass("[B")); // '[' for array of, 'B' for byte

    // allocate array
    int n = (int) protos.size();
    jobjectArray array_of_byte_arrays = CheckNotNull(env, env->NewObjectArray(n, byte_array_class, nullptr));

    for (int i = 0; i < n; i++) {
        // element byte array
        int s = (int) protos[i].size();
        jbyteArray byte_array = env->NewByteArray(s);

        // copy data
        const auto *data = reinterpret_cast<const jbyte *>(protos[i].data());
        env->SetByteArrayRegion(byte_array, 0, s, data);

        // array element
        env->SetObjectArrayElement(array_of_byte_arrays, i, byte_array);
    }
    return array_of_byte_arrays;
}

// P A R S E

extern "C" JNIEXPORT jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_parseProtos(JNIEnv *env, jobject /*thiz*/, jlong handle, jobjectArray input_texts) {
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<string> parsed_sentence_protos;
    sni_parse_hp(handle, texts, parsed_sentence_protos);
    LOGD("Parsed %zu sentences\n", parsed_sentence_protos.size());

    // interpret
    jobjectArray sentence_proto_array = toJavaByteArray(env, parsed_sentence_protos);

    LOGD("Parsing done\n");
    return sentence_proto_array;
}

// S P L I T P A R S E

extern "C" JNIEXPORT jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_splitParseProtos(JNIEnv *env, jobject /*thiz*/, jlong handle, jobjectArray input_texts) {
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<string> split_parsed_sentence_protos;
    sni_split_parse_hp(handle, texts, split_parsed_sentence_protos);
    LOGD("Parsed %zu sentences\n", split_parsed_sentence_protos.size());

    // interpret
    jobjectArray sentence_proto_array = toJavaByteArray(env, split_parsed_sentence_protos);

    LOGD("Parsing done\n");
    return sentence_proto_array;
}

// S E G M E N T

extern "C" JNIEXPORT jobjectArray
JNICALL Java_org_syntaxnet2_JNI2_segmentProtos(JNIEnv *env, jobject /*thiz*/, jlong handle, jobjectArray input_texts) {
    if (handle == 0) {
        env->ThrowNew(env->FindClass(kIllegalStateException), "Cannot parse with null handle");
        return nullptr;
    }

    // input
    const vector<string> texts = jniStringArrayToVector(env, input_texts);

    // parse
    vector<string> segmented_sentence_protos;
    sni_segment_hp(handle, texts, segmented_sentence_protos);
    LOGD("Segmented %zu sentences\n", segmented_sentence_protos.size());

    // result
    jobjectArray sentence_proto_array = toJavaByteArray(env, segmented_sentence_protos);

    LOGD("Segmenting done\n");
    return sentence_proto_array;
}

