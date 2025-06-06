/*
 * Copyright (c) 2023. Bernard Bou
 */
package com.bbou.download.coroutines

import android.content.Context
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.CallSuper
import androidx.core.net.toUri
import com.bbou.download.DownloadData
import com.bbou.download.Keys
import com.bbou.download.Keys.DOWNLOAD_TO_FILE_ARG
import com.bbou.download.Notifier
import com.bbou.download.common.R
import com.bbou.download.coroutines.core.DummyTask
import java.io.File

/**
 * Storage fragment
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class DummyFragment : DownloadBaseFragment() {

    // R E S O U R C E S

    /**
     * Layout id
     */
    override val layoutId: Int
        get() = R.layout.fragment_download

    /**
     * Destination file or dir
     */
    private var toFile: File? = null

    /**
     * Unzip dir
     */
    private var unzipDir: File? = null

    // A R G U M E N T S

    override fun unmarshal() {
        super.unmarshal()

        // arguments
        val toFileArg = arguments?.getString(DOWNLOAD_TO_FILE_ARG)
        val unzipToArg = arguments?.getString(Keys.THEN_UNZIP_TO_ARG)

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
            targetView.text = if (parent != null) parent.parent else ""
            targetView2.text = if (parent != null) parent.name else ""
            targetView3.text = if (toFile != null) toFile!!.name else ""
        } else {
            targetView.text = if (toFile != null) toFile!!.absolutePath else ""
        }
        if (targetView4 != null) {
            val deployTo: CharSequence = if (unzipDir != null) SpannableStringBuilder(getText(R.string.deploy_dest)).append(unzipDir!!.parent).append('/') else ""
            targetView4.text = deployTo
        }
        if (targetView5 != null) {
            targetView5.text = unzipDir?.name ?: ""
        }
    }

    /**
     * onViewCreated
     *
     * @param view view
     * @param savedInstanceState saved instance state
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusView = view.findViewById<TextView>(R.id.status)
        statusView.visibility = View.VISIBLE
        statusView.text = getString(com.bbou.download.coroutines.R.string.dummy)
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
        DummyTask.start(fromUrl, toFile, observer) { b ->
            onDownloadDone(
                when (b) {
                    true -> Status.STATUS_SUCCEEDED
                    false -> Status.STATUS_FAILED
                    null -> Status.STATUS_CANCELLED
                }, null
            )
        }
    }

    /**
     * Cancel download
     */
    override fun cancel() {
        DummyTask.cancel()
    }

    // A B S T R A C T

    /**
     * Cleanup download
     */
    override fun cleanup() {
        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT).show()
    }

    /**
     * Deploy
     */
    override fun deploy() {
        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT).show()
    }

    /**
     * MD5 check
     */
    override fun md5() {
        Toast.makeText(requireContext(), "Not implemented", Toast.LENGTH_SHORT).show()
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

    // S T A T U S

    companion object {

        private const val TAG = "DownloadDummyF"
    }
}
