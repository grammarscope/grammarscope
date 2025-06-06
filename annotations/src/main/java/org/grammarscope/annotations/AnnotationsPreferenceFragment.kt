/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.util.Log
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_EDGE_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_IGNORE_RELATIONS
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_LABEL_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_META_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_POS_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_RELATION_COLORS
import org.grammarscope.annotations.AnnotationsSettings.Companion.PREF_ROOT_COLOR
import org.grammarscope.annotations.AnnotationsSettings.Companion.getPreferenceFile
import org.grammarscope.annotations.paint.ColorMapPreference
import org.grammarscope.annotations.paint.ColorsJson.getColorMap
import org.grammarscope.annotations.paint.Palette
import org.jung.colors.ColorPreference
import org.jung.colors.chooser.ColorChooserPreference
import org.jung.colors.chooser.ColorChooserPreferenceDialogFragment
import org.jung.colors.picker.ColorPickerPreference
import org.jung.colors.picker.ColorPickerPreferenceDialogFragment

/**
 * This fragment shows semantics preferences only.
 */
class AnnotationsPreferenceFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Define the settings file to use by this settings fragment
        preferenceManager.apply {
            setSharedPreferencesName(getPreferenceFile(requireContext()))
            setSharedPreferencesMode(Context.MODE_PRIVATE)
        }

        // Add the preferences defined from resources
        addPreferencesFromResource(R.xml.pref_annotations)

        // Custom summary provider
        val ignoreRelationsPreference = findPreference<MultiSelectListPreference>(PREF_IGNORE_RELATIONS)!!
        ignoreRelationsPreference.summaryProvider = SummaryProvider<MultiSelectListPreference> { preference ->
            val selectedValues = preference.values // This is a Set<String>
            Log.d(TAG, "Selected values: $selectedValues")
            if (selectedValues.isNullOrEmpty()) {
                requireContext().getString(R.string.pref_description_ignore_relations_none)
            } else {
                val entries = preference.entries ?: arrayOf<CharSequence>() // Handle null entries
                val entryValues = preference.entryValues ?: arrayOf<CharSequence>() // Handle null entryValues
                val selectedEntries = selectedValues.mapNotNull { value ->
                    val index = entryValues.indexOf(value)
                    if (index >= 0 && index < entries.size) {
                        entries[index]
                    } else {
                        Log.w(TAG, "Value '$value' not found in entryValues or entries out of sync.")
                        null // value selected but no corresponding entry text
                    }
                }.joinToString(", ")

                if (selectedEntries.isEmpty() && selectedValues.isNotEmpty()) {
                    // values were selected but none matched entryValues
                    "${selectedValues.size} raw item(s) selected"
                } else if (selectedEntries.isEmpty()) {
                    requireContext().getString(R.string.pref_description_ignore_relations_none)
                } else {
                    requireContext().getString(R.string.pref_description_ignore_relations, selectedEntries)
                }
            }
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val manager = parentFragmentManager
        val dialogFragment: PreferenceDialogFragmentCompat =
            when (preference) {
                is ColorChooserPreference -> ColorChooserPreferenceDialogFragment.newInstance(preference.key)
                is ColorPickerPreference -> ColorPickerPreferenceDialogFragment.newInstance(preference.key)

                is ColorMapPreference -> {
                    ColorPickerPreferenceDialogFragment.newInstance(preference.key)
                    return
                }

                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }
        // ColorChooserPreference or ColorPickerPreference
        manager.setFragmentResultListener(ColorPreference.COLOR_RESULT_KEY, this) { key: String, bundle: Bundle ->
            run {
                if (key == ColorPreference.COLOR_RESULT_KEY) {
                    val color = bundle.getInt(ColorPreference.COLOR_RESULT_KEY)
                    Log.d(TAG, "Result color $color through FragmentResultListener()")
                    preference.color = color
                }
            }
        }
        @Suppress("DEPRECATION")
        dialogFragment.setTargetFragment(this, REQUEST_CODE) // Optional for back button handling
        dialogFragment.show(manager, "org.grammarscope.SemanticsPreferenceFragment.DIALOG")
    }

    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences?, key: String?) {
        when (key) {
            PREF_EDGE_COLOR -> {
                val color = prefs?.getInt(PREF_EDGE_COLOR, -1)!!
                if (color != -1) {
                    Palette.edgeColor = color
                    Palette.arrowTipColor = color
                    Palette.arrowStartColor = color
                } else {
                    Palette.edgeColor = Palette.DEFAULT_EDGE_COLOR
                    Palette.arrowTipColor = Palette.DEFAULT_ARROW_TIP_COLOR
                    Palette.arrowStartColor = Palette.DEFAULT_ARROW_START_COLOR
                }
            }

            PREF_LABEL_COLOR -> {
                val color = prefs?.getInt(PREF_LABEL_COLOR, -1)!!
                if (color != -1) {
                    Palette.labelColor = color
                } else {
                    Palette.labelColor = Palette.DEFAULT_LABEL_COLOR
                }
            }

            PREF_ROOT_COLOR -> {
                val color = prefs?.getInt(PREF_ROOT_COLOR, -1)!!
                if (color != -1) {
                    Palette.rootColor = color
                } else {
                    Palette.rootColor = Palette.DEFAULT_ROOT_COLOR
                }
            }

            PREF_POS_COLOR -> {
                val color = prefs?.getInt(PREF_POS_COLOR, -1)!!
                if (color != -1) {
                    Palette.posColor = color
                } else {
                    Palette.posColor = Palette.DEFAULT_POS_COLOR
                }
            }

            PREF_META_COLOR -> {
                val color = prefs?.getInt(PREF_META_COLOR, -1)!!
                if (color != -1) {
                    Palette.overflowColor = color
                    Palette.spanColor = color
                } else {
                    Palette.overflowColor = Palette.DEFAULT_OVERFLOW_COLOR
                    Palette.spanColor = Palette.DEFAULT_OVERFLOW_COLOR
                }
            }

            PREF_RELATION_COLORS -> {
                val preferenceMap: Map<String, Int>? = getColorMap(prefs)
                if (preferenceMap != null)
                    Palette.colorMap.putAll(preferenceMap)
            }

            //PREF_BOX_EDGES -> {
            //    val flag = prefs?.getBoolean(PREF_BOX_EDGES, false)
            //    boxEdges = flag
            //}

            //PREF_BOX_WORDS -> {
            //    val flag = prefs?.getBoolean(PREF_BOX_WORDS, false)
            //    boxWords = flag
            //}
        }
    }

    companion object {

        private const val TAG = "AnnotationsPrefF"
        private const val REQUEST_CODE = 457
    }
}
