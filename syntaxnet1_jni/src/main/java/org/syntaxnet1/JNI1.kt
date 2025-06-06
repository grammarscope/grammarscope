package org.syntaxnet1

import org.depparse.Sentence

object JNI1 {

    fun init() {
        System.loadLibrary("syntaxnet_inference")
        System.loadLibrary("syntaxnet_jni")
    }

    external fun infoJNI(): String
    external fun loadJNI(modelPath: String): Long
    external fun unloadJNI(handle: Long)

    external fun predictJNI(handle: Long, inputTexts: Array<String>): Array<Sentence>
}
