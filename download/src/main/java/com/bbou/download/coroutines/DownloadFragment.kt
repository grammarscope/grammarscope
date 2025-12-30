/*
 * Copyright (c) 2023. Bernard Bou
 */
package com.bbou.download.coroutines

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bbou.coroutines.observe.TaskDialogObserver
import com.bbou.deploy.coroutines.Deploy
import com.bbou.deploy.coroutines.Deploy.emptyDirectory
import com.bbou.deploy.coroutines.FileTasks
import com.bbou.deploy.coroutines.MD5
import com.bbou.download.DownloadData
import com.bbou.download.Keys.DOWNLOAD_TO_FILE_ARG
import com.bbou.download.Keys.THEN_UNZIP_TO_ARG
import com.bbou.download.Notifier
import com.bbou.download.common.R
import com.bbou.download.coroutines.core.DownloadTask
import com.bbou.download.coroutines.utils.MD5Downloader
import com.bbou.download.preference.Settings
import com.bbou.download.storage.ReportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.function.Consumer

/**
 * Download fragment using DownloadWork.
 * Interface between work and activity.
 * Cancel messages are to be sent to this fragment's receiver.
 * Signals completion through the OnComplete callback in the activity.
 * This fragment uses a file downloader core (file end-to-end downloads
 * with option of md5 checking it and zip expanding it to another
 * location (in settings or files dir by default) if a zip file.
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class DownloadFragment : DownloadBaseFragment() {

    // R E S O U R C E S

    /**
     * Layout id
     */
    override val layoutId: Int
        get() = R.layout.fragment_download

    // A R G U M E N T S

    /**
     * Destination file or dir
     */
    private var toFile: File? = null

    /**
     * Unzip dir
     */
    private var unzipDir: File? = null

    override fun unmarshal() {
        super.unmarshal()

        // arguments
        val toFileArg = arguments?.getString(DOWNLOAD_TO_FILE_ARG)
        val unzipToArg = arguments?.getString(THEN_UNZIP_TO_ARG)

        // download dest data
        toFile = if (toFileArg != null) File(toFileArg) else null
        unzipDir = if (unzipToArg != null) File(unzipToArg) else null
    }

    // S E T   D E S T I N A T I O N

    override fun setDestination(view: View) {
        val targetView = view.findViewById<TextView>(R.id.target)
        val targetView2 = view.findViewById<TextView>(R.id.target2)
        val targetView3 = view.findViewById<TextView>(R.id.target3)
        val targetView4 = view.findViewById<TextView>(R.id.target4)
        val targetView5 = view.findViewById<TextView>(R.id.target5)

        if (targetView2 != null && targetView3 != null) {
            val parent = if (toFile != null) toFile!!.parentFile else null
            var left = parent?.parent ?: ""
            left = "$left/"
            var mid = parent?.name ?: ""
            mid = "$mid/"
            targetView.text = left
            targetView2.text = mid
            targetView3.text = toFile?.name ?: ""
        } else {
            targetView.text = toFile?.absolutePath ?: ""
        }
        if (targetView4 != null) {
            val deployTo: CharSequence = if (unzipDir != null) SpannableStringBuilder(getText(R.string.deploy_dest)).append(unzipDir!!.parent).append('/') else ""
            targetView4.text = deployTo
        }
        if (targetView5 != null) {
            targetView5.text = unzipDir?.name ?: ""
        }
    }

    // C O N T R O L

    /**
     * Start download
     */
    override fun start() {

        super.start()

        // args
        val from = downloadUrl!!
        val to = toFile!!.absolutePath

        // start job
        start(from, to)
        return
    }

    /**
     * Start work
     *
     * @param fromUrl source zip url
     * @param toFile  destination file
     */
    private fun start(fromUrl: String, toFile: String) {
        DownloadTask.start(fromUrl, toFile, observer) { result ->
            val status = when {
                result == null -> Status.STATUS_CANCELLED
                result.first -> Status.STATUS_SUCCEEDED
                else -> Status.STATUS_FAILED
            }
            onDownloadDone(status, result?.second)
        }
    }

    /**
     * Cancel download
     */
    override fun cancel() {
        DownloadTask.cancel()
    }

    // A B S T R A C T

    /**
     * Cleanup download
     */
    override fun cleanup() {}

    /**
     * Deploy
     */
    override fun deploy() {

        // guard against no downloaded file
        if (toFile == null) {
            Log.e(TAG, "Deploy failure: no downloaded file")
            return
        }

        // guard against no unzip dir
        if (unzipDir == null) {
            val datapackDir = Settings.getDatapackDir(appContext)
            unzipDir = if (datapackDir == null) null else File(datapackDir)
        }
        if (unzipDir == null) {
            Log.e(TAG, "Null datapack dir, aborting deployment")
            return
        }

        // log
        Log.d(TAG, "Deploying $toFile to $unzipDir")

        // make sure unzip directory is clean
        emptyDirectory(unzipDir!!)

        // kill request
        if (requestKill != null) {
            requestKill!!.run()
        }

        // observer to proceed with record, cleanup and broadcast on successful task termination
        val activity: FragmentActivity? = activity
        val task = FileTasks(1000).unzipFromArchiveFile(unzipDir!!.absolutePath)
        val observer = if (activity != null) TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
            .setTitle(appContext.getString(R.string.action_unzip_datapack_from_archive))
            .setMessage(toFile!!.absolutePath)
            .setStatus(appContext.getString(R.string.status_unzipping))
            .show()
        else
            Consumer { p -> Log.d(TAG, p.toString()) }

        // run task
        lifecycleScope.launch {
            val result = task.runObserved(Dispatchers.Default, toFile!!.absolutePath, observer)
            if (result) {

                // delete downloaded file
                if (toFile != null) {
                    // cleanup
                    toFile!!.delete()
                }

                // rename
                if (renameFrom != null && renameTo != null && renameFrom != renameTo) {
                    // rename
                    val renameFromFile = File(unzipDir, renameFrom!!)
                    val renameToFile = File(unzipDir, renameTo!!)
                    val result2 = renameFromFile.renameTo(renameToFile)
                    Log.d(TAG, "Rename $renameFromFile to $renameToFile : $result2")
                }

                // new datapack
                if (requestNew != null) {
                    requestNew!!.run()
                }

                // record
                recordTarget()
            }

            // signal
            fireComplete(result)
        }
    }

    /**
     * MD5 check
     */
    override fun md5() {
        val from = downloadUrl + Deploy.MD5_EXTENSION
        val uri = downloadUrl!!.toUri()
        val sourceFile = uri.lastPathSegment
        val targetFile = if (toFile == null) "?" else toFile!!.name

        lifecycleScope.launch {

            val downloadedResult = MD5Downloader().run(Dispatchers.IO, MD5Downloader.Params(from, sourceFile!!))
            if (downloadedResult == null) {
                showMD5DownloadFailed(requireActivity(), targetFile)
            } else {
                val localPath = toFile!!.absolutePath
                MD5.md5(requireActivity(), localPath) { result: String? ->
                    showMD5(requireActivity(), downloadedResult, result, targetFile!!)
                }
            }
        }
    }

    private fun showMD5DownloadFailed(activity: Activity, targetFile: String) {
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.action_md5_of_what, targetFile))
            .setMessage(R.string.status_task_failed)
            .show()
    }

    private fun showMD5(activity: Activity, downloadedResult: String, result: String?, targetFile: String) {
        val success = downloadedResult == result
        val sb = SpannableStringBuilder()
        ReportUtils.appendHeader(sb, getString(R.string.md5_downloaded))
        sb.append('\n')
        sb.append(downloadedResult)
        sb.append('\n')
        ReportUtils.appendHeader(sb, getString(R.string.md5_computed))
        sb.append('\n')
        sb.append(result ?: getString(R.string.status_task_failed))
        sb.append('\n')
        ReportUtils.appendHeader(sb, getString(R.string.md5_compared))
        sb.append('\n')
        sb.append(getString(if (success) R.string.status_task_success else R.string.status_task_failed))
        AlertDialog.Builder(activity)
            .setTitle(getString(R.string.action_md5_of_what, targetFile))
            .setMessage(sb)
            .show()
    }

    // E V E N T S

    /**
     * Event sink for download events fired by downloader
     *
     * @param status download status
     * @param downloadData download data
     */
    @CallSuper
    override fun onDownloadDone(status: Status, downloadData: DownloadData?) {
        Log.d(TAG, "OnDone $status")

        // deploy
        val requiresDeploy = unzipDir != null

        // UI
        requireActivity().runOnUiThread {

            endUI(status)

            // md5
            md5Button?.visibility = if (status == Status.STATUS_SUCCEEDED) View.VISIBLE else View.GONE

            // deploy button to complete task
            deployButton?.visibility = if (status == Status.STATUS_SUCCEEDED && requiresDeploy) View.VISIBLE else View.GONE
        }

        // invalidate
        if (status != Status.STATUS_SUCCEEDED) {
            toFile = null
        }

        // record
        if (Status.STATUS_SUCCEEDED == status && !requiresDeploy) {
            recordTarget()
        }

        // super
        super.onDownloadDone(status, downloadData)

        // complete
        if (status != Status.STATUS_SUCCEEDED || !requiresDeploy) {
            fireComplete(status == Status.STATUS_SUCCEEDED)
        }
    }

    // N O T I F I C A T I O N

    /**
     * Fire UI notification
     *
     * @param context        context
     * @param notificationId notification id
     * @param type           notification
     * @param args           arguments
     */
    override fun fireNotification(context: Context, notificationId: Int, type: Notifier.NotificationType, vararg args: Any) {
        val from = downloadUrl!!.toUri().host
        val to = if (toFile == null) context.getString(R.string.result_deleted) else toFile!!.name
        val contentText = "$from→$to"
        Notifier.fireNotification(context, notificationId, type, contentText, *args)
    }

    companion object {

        private const val TAG = "DownloadF"
    }
}
