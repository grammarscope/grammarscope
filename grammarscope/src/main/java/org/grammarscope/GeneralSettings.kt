package org.grammarscope

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import androidx.core.content.edit

class GeneralSettings(context: Context) {

    private val sharedPrefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    // I N I T I A L I Z E

    fun initializePrefs(): Boolean {

        val initialized = sharedPrefs.getBoolean(PREF_INITIALIZED, false)
        if (initialized) {
            return false
        }
        // initialize
        sharedPrefs.edit(commit = true) {
            this
                .putBoolean(PREF_AS_GRAPH, false)
                .putBoolean(PREF_AS_GRAPHS, true)
                .putBoolean(PREF_AS_ANNOTATION, true)
                .putBoolean(PREF_SENTENCE_BOUNDARY_DETECTION, true)
                .putBoolean(PREF_INITIALIZED, true)
        }
        return true
    }

    companion object {

        private const val PREF_INITIALIZED = "pref_initialized_settings"

        const val PREF_AS_GRAPH = "as_graph"
        const val PREF_AS_GRAPHS = "as_graphs"
        const val PREF_AS_ANNOTATION = "as_annotation"
        const val PREF_SENTENCE_BOUNDARY_DETECTION = "sentence_boundary_detection"

        /**
         * Test whether in night mode. Detect if the system is in dark theme
         *
         * @param context context
         * @return true if in night mode
         */
        @Suppress("unused")
        private fun isNightMode(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        }
    }
}
