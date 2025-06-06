package org.syntaxnet1

import android.util.Log
import org.depparse.IEngine
import org.depparse.Sentence

class Syntaxnet1Engine : IEngine<Array<Sentence>> {

    private var handle: Long? = null
    override val isEmbedded: Boolean
        get() = false

    override fun load(modelPath: String) {
        Log.d(TAG, "loading ")
        handle = JNI1.loadJNI(modelPath)
        Log.d(TAG, "loaded $handle")
    }

    override fun unload() {
        if (handle == null) {
            Log.e(TAG, "Unloading exception (not initialized)")
            return
        }
        Log.d(TAG, "unloading $handle")
        JNI1.unloadJNI(handle!!)
        Log.d(TAG, "unloaded $handle")
    }

    @Throws(IllegalStateException::class)
    override fun process(args: Array<String>): Array<Sentence> {
        if (handle == null) {
            Log.e(TAG, "Trying to process while not initialized.")
            throw IllegalStateException("Trying to process while not initialized.")
        }
        Log.d(TAG, "predicting $handle")
        val sentences = JNI1.predictJNI(handle!!, args)
        Log.d(TAG, "predicted $handle")
        return sentences
    }

    override fun kill() {
        unload()
    }

    override fun version(): String {
        return JNI1.infoJNI()
    }

    override fun getStatus(): Int {
        return if (handle != null) 2 else 1
    }

    override fun getVersion(): String {
        return version()
    }

    companion object {

        private const val TAG = "Core1"

        init {
            Log.d(TAG, "Core1 setting up")
            JNI1.init()
            Log.d(TAG, "Core1 set up")
        }
    }
}
