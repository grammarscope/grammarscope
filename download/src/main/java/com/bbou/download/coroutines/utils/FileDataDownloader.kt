/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.download.coroutines.utils

import android.util.Log
import com.bbou.coroutines.BaseTask
import com.bbou.download.utils.FileData
import java.net.HttpURLConnection
import java.net.URL

/**
 * File data downloader
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class FileDataDownloader : BaseTask<String, FileData?>() {

    /**
     * Exception while executing
     */
    var exception: Exception? = null

    override suspend fun doJob(params: String): FileData? {
        var date: Long = -1
        var size: Long = -1
        var etag: String? = null
        var version: String? = null
        var staticVersion: String? = null
        var httpConnection: HttpURLConnection? = null
        try {
            // url
            val url = URL(params)
            Log.d(TAG, "Getting $url")

            // connection
            var connection = url.openConnection()

            // handle redirect
            if (connection is HttpURLConnection) {
                httpConnection = connection
                httpConnection.instanceFollowRedirects = false
                HttpURLConnection.setFollowRedirects(false)
                val status = httpConnection.responseCode
                Log.d(TAG, "Response Code ... $status")
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    // headers
                    date = connection.lastModified // Date(date)
                    size = connection.contentLength.toLong()
                    etag = connection.getHeaderField("etag")
                    version = connection.getHeaderField("x-version")
                    staticVersion = connection.getHeaderField("x-static-version")

                    // get redirect url from "location" header field
                    val newUrl = httpConnection.getHeaderField("Location")

                    // close
                    httpConnection.inputStream.close()

                    // disconnect
                    httpConnection.disconnect()

                    // open the new connection again
                    httpConnection = URL(newUrl).openConnection() as HttpURLConnection
                    connection = httpConnection
                    httpConnection.instanceFollowRedirects = true
                    HttpURLConnection.setFollowRedirects(true)
                    Log.d(TAG, "Redirect to URL : $newUrl")
                }
            }

            // connect
            connection.connect()

            // expect HTTP 200 OK, so we don't mistakenly save error report instead of the file
            if (connection is HttpURLConnection) {
                if (httpConnection!!.responseCode != HttpURLConnection.HTTP_OK) {
                    val message = "server returned HTTP " + httpConnection.responseCode + " " + httpConnection.responseMessage
                    throw RuntimeException(message)
                }
            }
            val name = url.file
            if (date <= 0) {
                date = connection.lastModified // new Date(date));
            }
            if (size <= 0) {
                size = connection.contentLength.toLong()
            }
            if (etag == null) {
                etag = connection.getHeaderField("etag")
            }
            if (version == null) {
                version = connection.getHeaderField("x-version")
            }
            if (staticVersion == null) {
                staticVersion = connection.getHeaderField("x-static-version")
            }

            // close
            connection.getInputStream().close()
            return FileData(name, date, size, etag, version, staticVersion)
        } catch (e: Exception) {
            Log.e(TAG, "While downloading", e)
            exception = e
        } finally {
            httpConnection?.disconnect()
        }
        return null
    }

    companion object {

        private const val TAG = "FileDataDownloader"
    }
}
