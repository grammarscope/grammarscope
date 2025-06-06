package org.grammarscope.corenlp

import android.util.Log
import com.bbou.coroutines.Task
import org.corenlp.CoreNlp

/**
 * Loader
 */
class Loader(val neural: Boolean) : Task<String?, Void?, Long?>() {

    init {
        Log.d(TAG, "Version " + Integer.toHexString(CoreNlp.version()))
    }

    override suspend fun doJob(params: String?): Long? {
        try {
            val handle = CoreNlp.load("$params", neural)
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
