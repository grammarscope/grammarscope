/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.core.content.edit
import org.grammarscope.annotations.paint.Palette

class AnnotationsSettings @JvmOverloads constructor(val context: Context, nightMode: Boolean = isNightMode(context)) {

    internal val sharedPrefs = getSharedPreferences(context)

    fun initializePrefs(): Boolean {
        // initialize
        sharedPrefs.edit(commit = true) {

        }
        return true
    }

    /**
     * Reset settings
     */
    fun reset() {
        Log.d(TAG, "Reset")

        val editor = sharedPrefs.edit()
        editor.clear()
        tryCommit(editor)

        Palette.setColorsFromResources(context)
        Palette.setColorsFromPreferences(context)
      }

    companion object {
        private const val TAG = "AnnotationsSettings"

        private const val PREFERENCES_ANNOTATIONS = "annotations"

        private const val PREF_FILE_NIGHT_SUFFIX = "_night"

        const val PREF_IGNORE_RELATIONS = "ignore_relations"

        const val PREF_BOX_WORDS = "box_words"

        const val PREF_BOX_EDGES = "box_edges"

        const val PREF_EDGE_COLOR = "edge_color"

        const val PREF_LABEL_COLOR = "label_color"

        const val PREF_ROOT_COLOR = "root_color"

        const val PREF_POS_COLOR = "pos_color"

        const val PREF_META_COLOR = "meta_color"

        const val PREF_RELATION_COLORS = "relation_colors"

        fun getPreferenceFile(context: Context): String {
            return getPreferenceFile(isNightMode(context))
        }

        fun getPreferenceFile(nightMode: Boolean): String {
            var name = PREFERENCES_ANNOTATIONS
            if (nightMode) {
                name += PREF_FILE_NIGHT_SUFFIX
            }
            return name
        }

        fun getSharedPreferences(context: Context): SharedPreferences {
            return context.getSharedPreferences(getPreferenceFile(context), Context.MODE_PRIVATE)
        }

        /**
         * Test whether in night mode.
         *
         * @param context context
         * @return -1 if in night mode, 1 in day mode
         */
        private fun isNightMode(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        }


        /**
         * Try to commit
         *
         * @param editor editor editor
         */
        @SuppressLint("CommitPrefEdits", "ApplySharedPref")
        fun tryCommit(editor: SharedPreferences.Editor) {
            try {
                editor.apply()
            } catch (_: AbstractMethodError) {
                // The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
                editor.commit()
            }
        }
    }
}
