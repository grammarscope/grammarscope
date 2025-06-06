package org.udpipe

import org.depparse.Sentence

object JNI {

    fun init() {
        System.loadLibrary("udpipe_inference")
        System.loadLibrary("udpipe_jni")
    }

    external fun version(): Int
    external fun load(modelPath: String): Long
    external fun unload(handle: Long)

    external fun parse(handle: Long, inputTexts: Array<String>): Array<Sentence>
}
