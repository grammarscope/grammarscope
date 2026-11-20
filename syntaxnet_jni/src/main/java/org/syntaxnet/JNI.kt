package org.syntaxnet

import org.depparse.Sentence

object JNI {

    fun init() {
        System.loadLibrary("syntaxnet_inference")
        System.loadLibrary("syntaxnet_jni")
    }

    external fun version(): Int
    external fun modelPath(): String
    external fun load(modelPath: String): Long
    external fun unload(handle: Long)

    external fun parse1(handle: Long, inputText: String): Sentence
    external fun splitParse1(handle: Long, inputText: String): Array<Sentence>
    external fun segment1(handle: Long, inputText: String): Sentence

    external fun parse(handle: Long, inputTexts: Array<String>): Array<Sentence>
    external fun splitParse(handle: Long, inputTexts: Array<String>): Array<Sentence>
    external fun segment(handle: Long, inputTexts: Array<String>): Array<Sentence>

    external fun parseProtobufs(handle: Long, inputTexts: Array<String>): Array<Sentence>
    external fun splitParseProtobufs(handle: Long, inputTexts: Array<String>): Array<Sentence>
    external fun segmentProtobufs(handle: Long, inputTexts: Array<String>): Array<Sentence>
}
