/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.deploy.coroutines

import android.app.AlertDialog
import android.content.Context
import android.widget.TextView
import androidx.core.util.Consumer
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.bbou.coroutines.observe.TaskDialogObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Md5 async task
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object MD5 {

    // T A R G E T E D

    /**
     * MD5 of file
     *
     * @param activity activity
     * @param path file path
     */
    fun md5(activity: FragmentActivity, path: String) {
        md5(activity, path) {
            // consumer
            md5Dialog(activity, it ?: "null", path)
        }
    }

    /**
     * MD5 async task
     *
     * @param activity activity
     * @param path path
     * @param consumer consumer
     */
    fun md5(activity: FragmentActivity, path: String, consumer: Consumer<String?>?) {
        if (activity.isFinishing || activity.isDestroyed) {
            return
        }
        val task = FileTasks(1000).md5FromFile()
        val observer = TaskDialogObserver<Pair<Number, Number>>(activity.supportFragmentManager, task)
            .setTitle(activity.getString(R.string.action_md5))
            .setMessage(path)
            .setStatus(activity.getString(R.string.status_md5_checking))
            .show()
        activity.lifecycleScope.launch {
            val result = task.runObserved(Dispatchers.Default, path, observer)
            observer.dismiss(result != null)
            consumer?.accept(result)
        }
    }

    // R E S U L T

    /**
     * Result dialog
     *
     * @param context context
     * @param result md5
     * @param sourceFile source file
     */
    fun md5Dialog(context: Context, result: CharSequence, sourceFile: String) {
        val resultView = TextView(context)
        resultView.text = result
        resultView.setPadding(35, 20, 35, 20)
        resultView.setTextIsSelectable(true)
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.action_md5_of_what, sourceFile))
            .setView(resultView) //.setMessage(sb)
            .show()
    }
}
