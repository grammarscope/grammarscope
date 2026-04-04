/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.SpannableStringBuilder

object Version {

    fun appVersion(context: Context): CharSequence {
        val sb = SpannableStringBuilder()
        sb.apply {
            val packageName = context.applicationInfo.packageName
            append(packageName)
            append('\n')
            val pInfo: PackageInfo
            try {
                pInfo = context.packageManager.getPackageInfo(packageName, 0)
                val code = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) pInfo.longVersionCode else @Suppress("DEPRECATION") pInfo.versionCode.toLong()
                append("version: ")
                append(code.toString())
                append('\n')
            } catch (e: PackageManager.NameNotFoundException) {
                append("package info: ")
                append(e.message)
                append('\n')
            }
            append("api: ")
            append(Build.VERSION.SDK_INT.toString())
            append(' ')
            append(Build.VERSION.CODENAME)
            append('\n')

            val app = context.applicationContext as AbstractApplication
            append("build time: ")
            append(app.buildTime())
            append('\n')
            append("git commit hash: ")
            append(app.gitHash())
            append('\n')
        }
        return sb
    }
}