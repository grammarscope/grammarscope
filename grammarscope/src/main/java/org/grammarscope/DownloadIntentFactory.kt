/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.bbou.deploy.coroutines.Deploy
import com.bbou.download.Keys.DOWNLOAD_ENTRY_ARG
import com.bbou.download.Keys.DOWNLOAD_FROM_ARG
import com.bbou.download.Keys.DOWNLOAD_MODE_ARG
import com.bbou.download.Keys.DOWNLOAD_TARGET_FILE_ARG
import com.bbou.download.Keys.DOWNLOAD_TO_DIR_ARG
import com.bbou.download.Keys.DOWNLOAD_TO_FILE_ARG
import com.bbou.download.Keys.THEN_UNZIP_TO_ARG
import com.bbou.download.coroutines.DownloadActivity
import com.bbou.download.preference.Settings
import com.bbou.download.preference.Settings.Mode.Companion.getModePref
import com.bbou.download.preference.Settings.getCachePref
import com.bbou.download.preference.Settings.getDatapackDir
import com.bbou.download.preference.Settings.getDatapackSource
import com.bbou.download.preference.Settings.getDatapackSourceType
import com.bbou.download.preference.Settings.getRepoPref
import org.depparse.common.ModelInfo.Companion.read

object DownloadIntentFactory {

    fun makeIntent(context: Context): Intent {
        var mode = getModePref(context)
        if (mode == null) {
            mode = Settings.Mode.DOWNLOAD_ZIP
        }
        return when (mode) {
            Settings.Mode.DOWNLOAD_ZIP -> makeIntentZipDownload(context)
            Settings.Mode.DOWNLOAD_ZIP_THEN_UNZIP -> makeIntentDownloadThenDeploy(context)
            else -> throw RuntimeException(mode.toString())
        }
    }

    private fun makeIntentZipDownload(context: Context): Intent {
        val intent = Intent(context, DownloadActivity::class.java)
        intent.putExtra(DOWNLOAD_MODE_ARG, Settings.Mode.DOWNLOAD_ZIP.toString()) // zipped transfer
        intent.putExtra(DOWNLOAD_ENTRY_ARG, null as String?) // all zip entry
        intent.putExtra(DOWNLOAD_TO_DIR_ARG, getDatapackDir(context)) // dest directory
        return intent
    }

    private fun makeIntentDownloadThenDeploy(context: Context): Intent {
        val destDir = getDatapackDir(context)
        val intent = Intent(context, DownloadActivity::class.java)
        intent.putExtra(DOWNLOAD_MODE_ARG, Settings.Mode.DOWNLOAD_ZIP_THEN_UNZIP.toString()) // plain transfer of zip file then
        intent.putExtra(THEN_UNZIP_TO_ARG, destDir) // unzip destination directory
        return intent
    }

    fun makeIntentUpdate(context: Context): Intent {
        val downloadSourceType = getDatapackSourceType(context)
        var downloadSourceUrl: String? = null
        if ("download" == downloadSourceType) {
            downloadSourceUrl = getDatapackSource(context)
        } else {
            val info = read(context)
            if (info != null) {
                downloadSourceUrl = getRepoPref(context) + '/' + info.name + Deploy.ZIP_EXTENSION
            }
        }
        if (downloadSourceUrl == null) {
            throw RuntimeException("No source url")
        }
        if (!downloadSourceUrl.endsWith(Deploy.ZIP_EXTENSION)) {
            throw RuntimeException("Not a zip file $downloadSourceUrl")
        }

        // source has zip extension
        var mode = getModePref(context)
        if (mode == null) {
            mode = Settings.Mode.DOWNLOAD_ZIP
        }
        return when (mode) {
            Settings.Mode.DOWNLOAD_ZIP -> {
                val intent = makeIntentZipDownload(context)
                intent.putExtra(DOWNLOAD_FROM_ARG, downloadSourceUrl) // source archive
                intent
            }

            Settings.Mode.DOWNLOAD_ZIP_THEN_UNZIP -> {
                val name = downloadSourceUrl.toUri().lastPathSegment
                val cache = getCachePref(context)
                val cachedZipPath = "$cache/$name"
                val intent = makeIntentDownloadThenDeploy(context)
                intent.putExtra(DOWNLOAD_FROM_ARG, downloadSourceUrl) // source archive
                intent.putExtra(DOWNLOAD_TO_FILE_ARG, cachedZipPath) // destination archive
                intent.putExtra(DOWNLOAD_TARGET_FILE_ARG, cachedZipPath) // target file
                intent
            }

            Settings.Mode.DOWNLOAD -> throw RuntimeException(mode.toString())
        }
    }
}
