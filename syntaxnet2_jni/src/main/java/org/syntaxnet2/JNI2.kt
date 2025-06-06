package org.syntaxnet2

import org.depparse.Sentence

object JNI2 {

    fun init() {
        System.loadLibrary("syntaxnet_inference2")
        System.loadLibrary("syntaxnet_jni2")
    }

    external fun version(): Int

    external fun load(modelPath: String): Long

    external fun unload(handle: Long)

    external fun parse(handle: Long, inputTexts: Array<String>): Array<Sentence>

    @Suppress("unused")
    external fun splitParse(handle: Long, inputTexts: Array<String>): Array<Sentence>

    @Suppress("unused")
    external fun segment(handle: Long, inputTexts: Array<String>): Array<Sentence>
}
