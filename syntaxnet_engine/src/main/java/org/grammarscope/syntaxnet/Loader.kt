package org.grammarscope.syntaxnet

import android.util.Log
import com.bbou.coroutines.Task
import org.syntaxnet.JNI

/**
 * Loader
 */
class Loader : Task<String, Void, Long?>() {

    init {
        Log.d(TAG, "JNI version " + JNI.version())
    }

    override suspend fun doJob(params: String): Long? {
        try {
            val handle = JNI.load(params)
            return if (handle != 0L) {
                handle
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load $params", e)
        }
        return null
    }

    companion object {

        private const val TAG = "Loader"
    }
}
