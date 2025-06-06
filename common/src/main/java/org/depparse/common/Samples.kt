package org.depparse.common

import android.content.Context
import org.depparse.Storage.getAppStorage
import java.io.File
import java.io.IOException

object Samples {

    fun read(context: Context): Array<String>? {
        val dir = getAppStorage(context)
        val file = File(dir, "samples")
        try {
            return file.readLines().toTypedArray()
        } catch (_: IOException) {
        }
        return null
    }

    fun ellipsize(str: String, max: Int): String {
        return if (str.length <= max) {
            str
        } else str.substring(0, max) + '…'
    }
}
