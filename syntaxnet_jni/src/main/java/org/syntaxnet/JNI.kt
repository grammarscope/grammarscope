package org.syntaxnet

import org.depparse.Sentence

object JNI {

    fun init() {
        System.loadLibrary("syntaxnet_inference_android")
        System.loadLibrary("syntaxnet_jni")
    }

    external fun version(): Int
    external fun modelPath(): String
    external fun load(modelPath: String): Long
    external fun unload(handle: Long)

    external fun parse(handle: Long, inputTexts: Array<String>): Array<Sentence>

    @Suppress("unused")
    external fun splitParse(handle: Long, inputTexts: Array<String>): Array<Sentence>

    @Suppress("unused")
    external fun segment(handle: Long, inputTexts: Array<String>): Array<Sentence>
}
