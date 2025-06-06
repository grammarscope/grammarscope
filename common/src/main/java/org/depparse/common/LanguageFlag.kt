package org.depparse.common

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.graphics.drawable.IconCompat
import org.depparse.Storage.getAppStorage
import java.io.File

object LanguageFlag {

    fun getDrawable(context: Context): Drawable? {
        try {
            val dir = getAppStorage(context)
            val file = File(dir, "flag.png")
            if (file.exists()) {
                val uri = Uri.fromFile(file)
                val icon = IconCompat.createWithContentUri(uri)
                return icon.loadDrawable(context)
            }
        } catch (_: Exception) {
        }
        return null
    }
}
