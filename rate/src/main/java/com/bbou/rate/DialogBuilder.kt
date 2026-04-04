/*
 * Copyright (c) 2016. Shintaro Katafuchi hotchemi
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.rate

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.widget.Toast

internal object DialogBuilder {

    @SuppressLint("NewApi")
    private fun getDialogBuilder(context: Context): AlertDialog.Builder {
        return AlertDialog.Builder(context)
    }

    fun build(context: Context, options: DialogOptions): Dialog {
        return getDialogBuilder(context)
            // message
            .setMessage(options.getMessageText(context))
            // title
            .apply {
                if (options.shouldShowTitle()) {
                    setTitle(options.getTitleText(context))
                }
            }
            // view
            .apply {
                val view = options.view
                if (view != null) {
                    setView(view)
                }
            }
            // cancelable
            .setCancelable(options.cancelable)
            // positive button
            .setPositiveButton(options.getPositiveText(context)) { _: DialogInterface?, _: Int ->
                val intentToAppstore = options.storeType.getIntent(context)
                try {
                    context.startActivity(intentToAppstore)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }
                PreferenceHelper.setAgreeShowDialog(context, false)
            }
            // neutral button
            .apply {
                if (options.shouldShowNeutralButton()) {
                    setNeutralButton(options.getNeutralText(context)) { _: DialogInterface?, _: Int -> PreferenceHelper.setRemindInterval(context) }
                }
            }
            // negative button
            .apply {
                if (options.shouldShowNegativeButton()) {
                    setNegativeButton(options.getNegativeText(context)) { _: DialogInterface?, _: Int -> PreferenceHelper.setAgreeShowDialog(context, false) }
                }
            }
            .create()
    }
}