package org.grammarscope.udpipe

import android.util.Log
import com.bbou.coroutines.Task
import org.udpipe.JNI

/**
 * Loader
 */
class Loader : Task<String?, Void?, Long?>() {

    init {
        val v = JNI.version()
        Log.d(TAG, "JNI version ${Integer.toHexString(v)}" )
    }

    override suspend fun doJob(params: String?): Long? {
        try {
            val handle = JNI.load("$params/model.udpipe")
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
