/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.deploy.coroutines

import android.app.AlertDialog
import android.content.ContentResolver
import android.content.DialogInterface
import android.net.Uri
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bbou.coroutines.Task
import com.bbou.coroutines.observe.TaskDialogObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL

/**
 * File async tasks with launchers
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class FileTasks(
    private val progressRate: Int,
) {

    // CORE

    /**
     * Copy from file
     *
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineCopyFromFile(
        private val dest: String,
        private val progressRate: Int,
    ) : Task<String, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: String): Boolean {
            return DeployOps.copyFromFile(params, dest, this, progressRate)
        }
    }

    /**
     * Copy from Uri
     *
     * @property dest destination
     * @property resolver
     * @property progressRate progress rate
     */
    class AsyncCoroutineCopyFromUri(
        private val dest: String,
        private val resolver: ContentResolver,
        private val progressRate: Int,
    ) : Task<Uri, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: Uri): Boolean {
            return DeployOps.copyFromUri(params, resolver, dest, this, progressRate)
        }
    }

    /**
     * Copy from URL
     *
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineCopyFromUrl(
        private val dest: String,
        private val progressRate: Int,
    ) : Task<URL, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: URL): Boolean {
            return DeployOps.copyFromUrl(params, dest, this, progressRate)
        }
    }

    /**
     * Unzip all entries from zip file
     *
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipFromArchiveFile(
        private val dest: String,
        private val progressRate: Int,
    ) : Task<String, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: String): Boolean {
            return DeployOps.unzipFromArchiveFile(params, dest, this, progressRate)
        }
    }

    /**
     * Unzip all entries from zip Uri
     *
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipFromArchiveUrl(
        private val dest: String,
        private val progressRate: Int,
    ) : Task<URL, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: URL): Boolean {
            return DeployOps.unzipFromArchiveUrl(params, dest, this, progressRate)
        }
    }

    /**
     * Unzip all entries from URL
     *
     * @property dest destination
     * @property resolver
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipFromArchiveUri(
        private val dest: String,
        private val resolver: ContentResolver,
        private val progressRate: Int,
    ) : Task<Uri, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: Uri): Boolean {
            return DeployOps.unzipFromArchiveUri(params, resolver, dest, this, progressRate)
        }
    }

    /**
     * Unzip an entry from zip file
     *
     * @property entry
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipEntryFromArchiveFile(
        private val entry: String,
        private val dest: String,
        private val progressRate: Int,
    ) : Task<String, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: String): Boolean {
            return DeployOps.unzipEntryFromArchiveFile(params, entry, dest, this, progressRate)
        }
    }

    /**
     * Unzip an entry from a zio Uri
     *
     * @property entry
     * @property dest destination
     * @property resolver
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipEntryFromArchiveUri(
        private val entry: String,
        private val dest: String,
        private val resolver: ContentResolver,
        private val progressRate: Int,
    ) : Task<Uri, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: Uri): Boolean {
            return DeployOps.unzipEntryFromArchiveUri(params, entry, resolver, dest, this, progressRate)
        }
    }

    /**
     * Unzip entry from a zip URL
     *
     * @property entry
     * @property dest destination
     * @property progressRate progress rate
     */
    class AsyncCoroutineUnzipEntryFromArchiveUrl(
        private val entry: String,
        private val dest: String,
        private val progressRate: Int,
    ) : Task<URL, Pair<Number, Number>, Boolean>() {

        override suspend fun doJob(params: URL): Boolean {
            return DeployOps.unzipEntryFromArchiveUrl(params, entry, dest, this, progressRate)
        }
    }

    /**
     * Compute MD5 sum of a file
     *
     * @property progressRate progress rate
     */
    class AsyncCoroutineMd5FromFile(
        private val progressRate: Int,
    ) : Task<String, Pair<Number, Number>, String?>() {

        override suspend fun doJob(params: String): String? {
            return DeployOps.md5FromFile(params, this, progressRate)
        }
    }

    /**
     * Compute MD sum of a Uri
     *
     * @property resolver
     * @property progressRate progress rate
     */
    class AsyncCoroutineMd5FromUri(
        private val resolver: ContentResolver,
        private val progressRate: Int,
    ) : Task<Uri, Pair<Number, Number>, String?>() {

        override suspend fun doJob(params: Uri): String? {
            return DeployOps.md5FromUri(params, resolver, this, progressRate)
        }
    }

    /**
     * Compute MD5 of a URL
     *
     * @property progressRate progress rate
     */
    class AsyncCoroutineMd5FromUrl(
        private val progressRate: Int,
    ) : Task<URL, Pair<Number, Number>, String?>() {

        override suspend fun doJob(params: URL): String? {
            return DeployOps.md5FromUrl(params, this, progressRate)
        }
    }

    // FACTORIES

    // copy

    /**
     * Task factory for copy from source file
     *
     * @param dest destination
     * @return task
     */
    fun copyFromFile(dest: String): Task<String, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineCopyFromFile(dest, progressRate)
    }

    /**
     * Task factory for copy from uri
     *
     * @param resolver content resolver
     * @param dest destination
     * @return task
     */
    fun copyFromUri(resolver: ContentResolver, dest: String): Task<Uri, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineCopyFromUri(dest, resolver, progressRate)
    }

    /**
     * Task factory for copy from url
     *
     * @param dest destination
     * @return task
     */
    fun copyFromUrl(dest: String): Task<URL, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineCopyFromUrl(dest, progressRate)
    }

    // unzip

    /**
     * Task factory for expand all from zip file
     *
     * @param dest destination
     * @return task
     */
    fun unzipFromArchiveFile(dest: String): Task<String, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipFromArchiveFile(dest, progressRate)
    }

    /**
     * Task factory for expand all from zip uri
     *
     * @param resolver content resolver
     * @param dest destination
     * @return task
     */
    fun unzipFromArchiveUri(resolver: ContentResolver, dest: String): Task<Uri, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipFromArchiveUri(dest, resolver, progressRate)
    }

    /**
     * Task factory for expand all from zip url
     *
     * @param dest destination
     * @return task
     */
    fun unzipFromArchiveUrl(dest: String): Task<URL, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipFromArchiveUrl(dest, progressRate)
    }

    /**
     * Task factory for expand entry from zip file
     *
     * @param dest destination
     * @return task
     */
    fun unzipEntryFromArchiveFile(entry: String, dest: String): Task<String, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipEntryFromArchiveFile(entry, dest, progressRate)
    }

    /**
     * Task factory for expand entry from zip uri
     *
     * @param resolver content resolver
     * @param entry entry
     * @param dest destination
     * @return task
     */
    fun unzipEntryFromArchiveUri(resolver: ContentResolver, entry: String, dest: String): Task<Uri, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipEntryFromArchiveUri(entry, dest, resolver, progressRate)
    }

    /**
     * Task factory for expand entry from zip uri
     *
     * @param dest destination
     * @return task
     */
    fun unzipEntryFromArchiveUrl(entry: String, dest: String): Task<URL, Pair<Number, Number>, Boolean> {
        return AsyncCoroutineUnzipEntryFromArchiveUrl(entry, dest, progressRate)
    }

    // md5

    /**
     * Task factory for Md5 check sum of file
     *
     * @return task
     */
    fun md5FromFile(): Task<String, Pair<Number, Number>, String?> {
        return AsyncCoroutineMd5FromFile(progressRate)
    }

    /**
     * Task factory for Md5 check sum of uri

     * @param resolver content resolver
     * @return task
     */
    fun md5FromUri(resolver: ContentResolver): Task<Uri, Pair<Number, Number>, String?> {
        return AsyncCoroutineMd5FromUri(resolver, progressRate)
    }

    /**
     * Task factory for Md5 check sum of url
     *
     * @return task
     */
    fun md5FromUrl(): Task<URL, Pair<Number, Number>, String?> {
        return AsyncCoroutineMd5FromUrl(progressRate)
    }

    companion object {

        // L A U N C H E R S

        // copy

        /**
         * Launch copy
         *
         * @param activity activity
         * @param sourceFile source file
         * @param dest destination
         */
        fun launchCopy(activity: FragmentActivity, sourceFile: String, dest: String) {
            val task = FileTasks(1000).copyFromFile(dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_copy_datapack_from_file))
                .setMessage(sourceFile)
                .setStatus(activity.getString(R.string.status_copying))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceFile, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch copy
         *
         * @param activity activity
         * @param sourceUri source uri
         * @param dest destination
         */
        @Suppress("unused")
        fun launchCopy(activity: FragmentActivity, sourceUri: Uri, dest: String) {
            val task = FileTasks(1000).copyFromUri(activity.contentResolver, dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_copy_datapack_from_file))
                .setMessage(sourceUri.toString())
                .setStatus(activity.getString(R.string.status_copying))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUri, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch copy from url
         *
         * @param activity activity
         * @param sourceUrl source url
         * @param dest destination
         */
        @Suppress("unused")
        fun launchCopy(activity: FragmentActivity, sourceUrl: URL, dest: String) {
            val task = FileTasks(1000).copyFromUrl(dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_copy_datapack_from_file))
                .setMessage(sourceUrl.toString())
                .setStatus(activity.getString(R.string.status_copying))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUrl, observer)
                observer.dismiss(result)
            }
        }

        // unzip

        /**
         * Launch unzipping archive file
         *
         * @param activity activity
         * @param sourceFile source zip file
         * @param dest destination
         */
        fun launchUnzip(activity: FragmentActivity, sourceFile: String, dest: String) {
            val task = FileTasks(1000).unzipFromArchiveFile(dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceFile)
                .setStatus(activity.getString(R.string.status_unzipping))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceFile, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch unzipping archive uri
         *
         * @param activity  activity
         * @param sourceUri source zip uri
         * @param dest destination
         */
        @Suppress("unused")
        fun launchUnzip(activity: FragmentActivity, sourceUri: Uri, dest: String) {
            val task = FileTasks(1000).unzipFromArchiveUri(activity.contentResolver, dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceUri.toString())
                .setStatus(activity.getString(R.string.status_unzipping))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUri, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch unzipping archive url
         *
         * @param activity activity
         * @param sourceUrl source zip url
         * @param dest destination
         */
        @Suppress("unused")
        fun launchUnzip(activity: FragmentActivity, sourceUrl: URL, dest: String) {
            val task = FileTasks(1000).unzipFromArchiveUrl(dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceUrl.toString())
                .setStatus(activity.getString(R.string.status_unzipping))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUrl, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch unzipping of entry in archive file
         *
         * @param activity activity
         * @param sourceFile source zip file
         * @param zipEntry zip entry
         * @param dest destination
         */
        fun launchUnzip(activity: FragmentActivity, sourceFile: String, zipEntry: String, dest: String) {
            val task = FileTasks(1000).unzipEntryFromArchiveFile(zipEntry, dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceFile)
                .setStatus(activity.getString(R.string.status_unzipping) + ' ' + zipEntry)
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceFile, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch unzipping of entry in archive uri
         *
         * @param activity activity
         * @param sourceUri source zip uri
         * @param zipEntry zip entry
         * @param dest destination
         */
        @Suppress("unused")
        fun launchUnzip(activity: FragmentActivity, sourceUri: Uri, zipEntry: String, dest: String) {
            val task = FileTasks(1000).unzipEntryFromArchiveUri(activity.contentResolver, zipEntry, dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceUri.toString())
                .setStatus(activity.getString(R.string.status_unzipping) + ' ' + zipEntry)
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUri, observer)
                observer.dismiss(result)
            }
        }

        /**
         * Launch unzipping of entry in archive url
         *
         * @param activity activity
         * @param sourceUrl source zip url
         * @param zipEntry zip entry
         * @param dest destination
         */
        @Suppress("unused")
        fun launchUnzip(activity: FragmentActivity, sourceUrl: URL, zipEntry: String, dest: String) {
            val task = FileTasks(1000).unzipEntryFromArchiveUrl(zipEntry, dest)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_unzip_datapack_from_archive))
                .setMessage(sourceUrl.toString())
                .setStatus(activity.getString(R.string.status_unzipping) + ' ' + zipEntry)
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.IO, sourceUrl, observer)
                observer.dismiss(result)
            }
        }

        // md5

        private fun getMD5Consumer(activity: FragmentActivity, whenDone: Runnable?): Consumer<String?> {
            return Consumer { md5: String? ->
                val alert = AlertDialog.Builder(activity)
                if (md5 != null) {
                    alert.setMessage(md5)
                } else {
                    alert.setMessage(R.string.result_fail)
                }
                alert.setOnDismissListener { _: DialogInterface? -> whenDone?.run() }
                alert.show()
            }
        }

        /**
         * Launch computation of MD5
         *
         * @param activity activity
         * @param file source file
         * @param whenDone to run when done
         */
        fun launchMd5(activity: FragmentActivity, file: String, whenDone: Runnable?) {
            val consumer = getMD5Consumer(activity, whenDone)
            val task = FileTasks(1000).md5FromFile()
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_md5))
                .setMessage(file)
                .setStatus(activity.getString(R.string.status_md5_checking))
                .show()

            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.Default, file, observer)
                observer.dismiss(result != null)
                consumer.accept(result)
            }
        }

        /**
         * Launch computation of MD5
         *
         * @param activity activity
         * @param uri uri
         * @param whenDone to run when done
         */
        @Suppress("unused")
        fun launchMd5(activity: FragmentActivity, uri: Uri, whenDone: Runnable?) {
            val consumer = getMD5Consumer(activity, whenDone)
            val task = FileTasks(1000).md5FromUri(activity.contentResolver)
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_md5))
                .setMessage(uri.toString())
                .setStatus(activity.getString(R.string.status_md5_checking))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.Default, uri, observer)
                observer.dismiss(result != null)
                consumer.accept(result)
            }
        }

        /**
         * Launch computation of MD5
         *
         * @param activity activity
         * @param url url
         * @param whenDone to run when done
         */
        @Suppress("unused")
        fun launchMd5(activity: FragmentActivity, url: URL, whenDone: Runnable?) {
            val consumer = getMD5Consumer(activity, whenDone)
            val task = FileTasks(1000).md5FromUrl()
            val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
                .setTitle(activity.getString(R.string.action_md5))
                .setMessage(url.toString())
                .setStatus(activity.getString(R.string.status_md5_checking))
                .show()
            activity.lifecycleScope.launch {
                val result = task.runObserved(Dispatchers.Default, url, observer)
                observer.dismiss(result != null)
                consumer.accept(result)
            }
        }
    }
}
