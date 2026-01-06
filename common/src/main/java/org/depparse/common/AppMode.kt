/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common
import android.content.Context
import android.content.res.Configuration

object AppMode {
    fun isNightMode(context: Context): Boolean {
        val uiMode = context.resources.configuration.uiMode
        return (uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}