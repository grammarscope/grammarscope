/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse.common

import android.content.Context
import androidx.preference.PreferenceManager

object Settings {

    const val PREF_STRIP_ACCENTS = "strip_accents"

    fun isStripAccentsEnabled(context: Context): Boolean {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPrefs.getBoolean(PREF_STRIP_ACCENTS, false)
    }
}