/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.download.coroutines.utils

import android.util.Log
import com.bbou.coroutines.BaseTask
import kotlinx.coroutines.yield
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * MD5 downloader task, reads remote file with targets and md5sums, one per line
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
internal class MD5Downloader : BaseTask<MD5Downloader.Params, String?>() {

    /**
     * Params
     *
     * @property md5UrlArg url of file that contains reference md5 digest
     * @property targetArg target file to evaluate md5 digest of
     */
    data class Params(val md5UrlArg: String, val targetArg: String?)

    /**
     * Exception while executing
     */
    var exception: Exception? = null

    override suspend fun doJob(params: Params): String? {
        val md5Arg = params.md5UrlArg
        val targetArg = params.targetArg

        var httpConnection: HttpURLConnection? = null
        try {
            // connect
            val url = URL(md5Arg)
            Log.d(TAG, "Getting $url")
            val connection = url.openConnection()
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
            if (connection is HttpURLConnection) {
                httpConnection = connection
                if (httpConnection.responseCode != HttpURLConnection.HTTP_OK) {
                    val message = "server returned HTTP " + httpConnection.responseCode + " " + httpConnection.responseMessage
                    throw RuntimeException(message)
                }
            }
            connection.getInputStream().use { `is` ->
                InputStreamReader(`is`).use { isr ->
                    BufferedReader(isr).use { reader ->
                        reader.lineSequence().forEach { it ->
                            if (targetArg?.let { it1 -> it.contains(it1) } == true) {
                                val fields = it.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                                val result = fields[0].trim { it <= ' ' }
                                Log.d(TAG, "Completed $result")
                                return result
                            }
                            // cooperative exit
                            yield()
                        }
                    }
                }
            }
            return null
        } catch (e: Exception) {
            exception = e
            Log.e(TAG, "While downloading", e)
        } finally {
            httpConnection?.disconnect()
        }
        return null
    }

    companion object {

        private const val TAG = "MD5Downloader"
    }
}
