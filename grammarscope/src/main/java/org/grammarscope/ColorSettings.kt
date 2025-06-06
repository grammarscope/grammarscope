package org.grammarscope

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.core.content.edit
import glue.Color
import org.depparse.common.Colors
import org.depparse.common.Colors.computeForeColor

class ColorSettings @JvmOverloads constructor(context: Context, nightMode: Boolean = isNightMode(context)) {

    private val sharedPrefs: SharedPreferences

    init {
        val name = getPreferenceFile(nightMode)
        sharedPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE)
    }

    // I N I T I A L I Z E

    @Suppress("SameReturnValue")
    fun initializePrefs(): Boolean {
        sharedPrefs.edit(commit = true) {
            putInt(PREF_ROOT_COLOR, Colors.rootBackColor)
                .putInt(PREF_LABEL_COLOR, Colors.labelBackColor)
                .putInt(PREF_ENHANCED_LABEL_COLOR, Colors.enhancedLabelBackColor)
                .putInt(PREF_HEAD_COLOR, Colors.headBackColor)
                .putInt(PREF_DEPENDENT_COLOR, Colors.dependentBackColor)
                .putInt(PREF_PREDICATE_COLOR, Colors.predicateBackColor)
                .putInt(PREF_SUBJECT_COLOR, Colors.subjectBackColor)
                .putInt(PREF_OBJECT_COLOR, Colors.objectBackColor)
                .putInt(PREF_TERM_MODIFYING_SUBPREDICATE_COLOR, Colors.termModifierPredicateBackColor)
                .putInt(PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR, Colors.predicateModifierPredicateBackColor)
        }
        return true
    }

    fun reset() {
        sharedPrefs.edit { clear() }
    }

    fun makeColor(key: String?, defaultColor: Color): Color {
        val c = makeColorOrNull(key)
        return c ?: defaultColor
    }

    private fun makeColorOrNull(key: String?): Color? {
        if (!sharedPrefs.contains(key)) {
            return null
        }
        val color = sharedPrefs.getInt(key, -1)
        return Color(color)
    }

    companion object {

        private const val PREFERENCES_COLORS = "org.grammarscope_preferences_colors"
        private const val PREF_FILE_NIGHT_SUFFIX = "_night"
        const val PREF_ROOT_COLOR = "root_color"
        const val PREF_LABEL_COLOR = "label_color"
        const val PREF_ENHANCED_LABEL_COLOR = "enhanced_label_color"
        const val PREF_HEAD_COLOR = "head_color"
        const val PREF_DEPENDENT_COLOR = "dependent_color"
        const val PREF_PREDICATE_COLOR = "predicate_color"
        const val PREF_SUBJECT_COLOR = "subject_color"
        const val PREF_OBJECT_COLOR = "object_color"
        const val PREF_TERM_MODIFYING_SUBPREDICATE_COLOR = "term_modifying_subpredicate_color"
        const val PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR = "predicate_modifying_subpredicate_color"

        fun getPreferenceFile(context: Context): String {
            return getPreferenceFile(isNightMode(context))
        }

        fun getPreferenceFile(nightMode: Boolean): String {
            var name = PREFERENCES_COLORS
            if (nightMode) {
                name += PREF_FILE_NIGHT_SUFFIX
            }
            return name
        }

        fun setColorsFromPreferences(context: Context) {
            val sharedPrefs = context.getSharedPreferences(getPreferenceFile(context), Context.MODE_PRIVATE)
            if (sharedPrefs.contains(PREF_ROOT_COLOR)) {
                val color = sharedPrefs.getInt(PREF_ROOT_COLOR, -1)
                Colors.rootBackColor = color
                Colors.rootColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_LABEL_COLOR)) {
                val color = sharedPrefs.getInt(PREF_LABEL_COLOR, -1)
                Colors.labelBackColor = color
                Colors.labelColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_ENHANCED_LABEL_COLOR)) {
                val color = sharedPrefs.getInt(PREF_ENHANCED_LABEL_COLOR, -1)
                Colors.enhancedLabelBackColor = color
                Colors.enhancedLabelColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_HEAD_COLOR)) {
                val color = sharedPrefs.getInt(PREF_HEAD_COLOR, -1)
                Colors.headBackColor = color
                Colors.headColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_DEPENDENT_COLOR)) {
                val color = sharedPrefs.getInt(PREF_DEPENDENT_COLOR, -1)
                Colors.dependentBackColor = color
                Colors.dependentColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_PREDICATE_COLOR)) {
                val color = sharedPrefs.getInt(PREF_PREDICATE_COLOR, -1)
                Colors.predicateBackColor = color
                Colors.predicateColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_SUBJECT_COLOR)) {
                val color = sharedPrefs.getInt(PREF_SUBJECT_COLOR, -1)
                Colors.subjectBackColor = color
                Colors.subjectColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_OBJECT_COLOR)) {
                val color = sharedPrefs.getInt(PREF_OBJECT_COLOR, -1)
                Colors.objectBackColor = color
                Colors.objectColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_TERM_MODIFYING_SUBPREDICATE_COLOR)) {
                val color = sharedPrefs.getInt(PREF_TERM_MODIFYING_SUBPREDICATE_COLOR, -1)
                Colors.termModifierPredicateBackColor = color
                Colors.termModifierPredicateColor = computeForeColor(color)
            }
            if (sharedPrefs.contains(PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR)) {
                val color = sharedPrefs.getInt(PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR, -1)
                Colors.predicateModifierPredicateBackColor = color
                Colors.predicateModifierPredicateColor = computeForeColor(color)
            }
        }

        @Suppress("unused")
        fun clearPrefs(context: Context) {
            val name: String = getPreferenceFile(context)
            clearPrefs(context, name)
        }

        private fun clearPrefs(context: Context, name: String) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit {
                    remove(PREF_ROOT_COLOR)
                        .remove(PREF_LABEL_COLOR)
                        .remove(PREF_ENHANCED_LABEL_COLOR)
                        .remove(PREF_HEAD_COLOR)
                        .remove(PREF_DEPENDENT_COLOR)
                        .remove(PREF_PREDICATE_COLOR)
                        .remove(PREF_SUBJECT_COLOR)
                        .remove(PREF_OBJECT_COLOR)
                        .remove(PREF_TERM_MODIFYING_SUBPREDICATE_COLOR)
                        .remove(PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR)
                }
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
    }
}
