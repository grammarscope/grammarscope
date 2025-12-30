/*
 * Copyright (c) 2023. Bernard Bou
 */
package com.bbou.download.coroutines

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.CallSuper
import com.bbou.download.BaseDownloadFragment
import com.bbou.download.DownloadData
import com.bbou.download.Notifier
import com.bbou.download.preference.Settings
import java.util.concurrent.atomic.AtomicBoolean
import java.util.function.Consumer

/**
 * Base download fragment. Handles
 * - status
 * - notification
 * - cancel intent receiver
 * - kill and new data emitter
 * - work info livedata observer that observes work info status and data change
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
abstract class DownloadBaseFragment : BaseDownloadFragment() {

    init {
        status = null
    }

    // C O N T R OL

    private val downloading = AtomicBoolean(false)

    /**
     * Start download
     */
    @CallSuper
    override fun start() {

        Log.d(TAG, "Starting")
        if (downloading.getAndSet(true)) // prevent recursion
            throw RuntimeException("Already downloading")
        return
    }

    /**
     * UI updater
     */
    @SuppressLint("SetTextI18n")
    val observer = Consumer<Pair<Long, Long>> {

        status = Status.STATUS_RUNNING

        println("progress is ${it.first} ${it.second} ${Thread.currentThread()}")
        progress = it.first to it.second

        // UI
        updateUI()

        // notification
        val progressPercent = progress!!.first.toFloat() / progress!!.second
        fireNotification(appContext, notificationId, Notifier.NotificationType.UPDATE, progressPercent)
    }

    // E V E N T S

    /**
     * onStart callback
     */
    override fun onDownloadStart() {
        super.onDownloadStart()

        status = Status.STATUS_PENDING

        fireNotification(appContext, notificationId, Notifier.NotificationType.START)
    }

    /**
     * Event sink for download events fired by downloader
     *
     * @param status download status
     * @param downloadData download data
     */
    @CallSuper
    override fun onDownloadDone(status: Status, downloadData: DownloadData?) {
        super.onDownloadDone(status, downloadData)

        when (status) {
            Status.STATUS_SUCCEEDED -> {
                if (downloadData != null) {
                    Settings.recordDatapackSource(appContext, downloadData, mode?.toString())
                }
                fireNotification(appContext, notificationId, Notifier.NotificationType.FINISH, true)
            }

            Status.STATUS_FAILED -> {
                fireNotification(appContext, notificationId, Notifier.NotificationType.FINISH, false)
            }

            Status.STATUS_CANCELLED -> {
                fireNotification(appContext, notificationId, Notifier.NotificationType.CANCEL, false)
            }

            else -> {}
        }
    }

    // S T A T U S

    override val reason: String?
        get() {
            return null
        }

    companion object {

        private const val TAG = "DownloadBase"
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
    abstract fun fireNotification(context: Context, notificationId: Int, type: Notifier.NotificationType, vararg args: Any)
}
