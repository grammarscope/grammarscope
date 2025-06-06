/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.download.coroutines.utils

import android.app.AlertDialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bbou.coroutines.BaseTask
import com.bbou.download.common.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.function.BiConsumer

/**
 * Resources downloader task
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
@Suppress("unused")
class ResourcesDownloader : BaseTask<ResourcesDownloader.Params, Collection<Array<String>>?>() {

    /**
     * Parameters
     *
     * @property resourcesUrl resources url
     * @property filter filter
     */
    data class Params(val resourcesUrl: String, val filter: String?)

    /**
     * Exception while executing
     */
    var exception: Exception? = null

    override suspend fun doJob(params: Params): Collection<Array<String>>? {

        val lineFilter = params.filter
        var httpConnection: HttpURLConnection? = null
        try {
            // connect
            val url = URL(params.resourcesUrl)
            Log.d(TAG, "Get $url")
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
                        return reader.lineSequence()
                            .map { it.trim { it2 -> it2 <= ' ' } }
                            .filter { it.isNotEmpty() }
                            .filterNot { it.startsWith("#") }
                            .filter { lineFilter.isNullOrEmpty() || it.matches(lineFilter.toRegex()) }
                            .map { it.split("\\s+".toRegex()).dropLastWhile { it2 -> it2.isEmpty() }.toTypedArray() }
                            .also { yield() }
                            .toMutableList()
                    }
                }
            }
        } catch (e: Exception) {
            exception = e
            Log.e(TAG, "While downloading", e)
        } finally {
            httpConnection?.disconnect()
        }
        return null
    }

    companion object {

        private const val TAG = "ResourcesDownloader"

        /**
         * Get and show resources
         *
         * @param activity
         */
        @Suppress("unused")
        fun showResources(activity: AppCompatActivity) {

            val url = activity.getString(R.string.resources_directory)
            val filter = activity.getString(R.string.resources_directory_filter)
            activity.lifecycleScope.launch {
                val resources = ResourcesDownloader().run(
                    Dispatchers.IO,
                    Params(url, filter)
                )
                show(activity, resources, url)
            }
        }

        /**
         * Show resources
         *
         * @param activity
         * @param resources resources
         * @param url url
         */
        private fun show(activity: AppCompatActivity, resources: Collection<Array<String>>?, url: String) {
            if (resources == null) {
                AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.action_directory) + " of " + url)
                    .setMessage(R.string.status_task_failed)
                    .show()
            } else {
                val sb = SpannableStringBuilder()
                sb.append('\n')
                for (row in resources) {
                    sb.append(java.lang.String.join(" ", *row))
                    sb.append('\n')
                    sb.append('\n')
                }
                AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.resource_directory) + ' ' + url)
                    .setMessage(sb)
                    .show()
            }
        }

        /**
         * Populate radio group with resources

         * @param context context
         * @param consumer of lists of labels and values
         */
        private suspend fun populateLists(context: Context, consumer: BiConsumer<List<String>, List<String>>) {
            val url = context.getString(R.string.resources_directory)
            val filter = context.getString(R.string.resources_directory_filter)
            val resources = ResourcesDownloader()
                .run(Dispatchers.IO, Params(url, filter))

            if (resources != null) {
                val values: MutableList<String> = ArrayList()
                val labels: MutableList<String> = ArrayList()
                for (row in resources) {
                    // ewn	OEWN	2023	Bitbucket	https://bitbucket.org/semantikos2/semantikos22/raw/53e04fe21bc901ee15631873972445c2c8725652	zipped
                    val value = row[4]
                    val label = String.format("%s %s (%s %s)", row[1], row[2], row[3], row[5])
                    values.add(value)
                    labels.add(label)
                }
                consumer.accept(values, labels)
            }
        }

        /**
         * Populate radio group with resources

         * @param context context
         * @param radioGroup radio group
         */
        private suspend fun populateRadioGroup(context: Context, radioGroup: RadioGroup) {
            populateLists(context) { values: List<String>, labels: List<String> ->
                val n = values.size.coerceAtMost(labels.size)
                for (i in 0 until n) {
                    val value: CharSequence = values[i]
                    val label: CharSequence = labels[i]
                    val radioButton = RadioButton(context)
                    radioButton.text = label
                    radioButton.tag = value
                    radioButton.isEnabled = true
                    radioGroup.addView(radioButton)
                }
            }
        }

        /**
         * To radio group with resources

         * @param context context
         * @return radio group
         */
        @Suppress("unused")
        suspend fun toRadioGroup(context: Context): RadioGroup {
            val radioGroup = RadioGroup(context)
            populateRadioGroup(context, radioGroup)
            return radioGroup
        }
    }
}
