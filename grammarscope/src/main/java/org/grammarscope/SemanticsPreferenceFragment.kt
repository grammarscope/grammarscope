package org.grammarscope

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceDialogFragmentCompat
import androidx.preference.PreferenceFragmentCompat
import org.depparse.common.AppContext
import org.depparse.common.Colors
import org.depparse.common.Colors.computeForeColor
import org.grammarscope.ColorSettings.Companion.getPreferenceFile
import org.jung.colors.ColorPreference
import org.jung.colors.chooser.ColorChooserPreference
import org.jung.colors.chooser.ColorChooserPreferenceDialogFragment
import org.jung.colors.picker.ColorPickerPreference
import org.jung.colors.picker.ColorPickerPreferenceDialogFragment
import org.grammarscope.common.R

/**
 * This fragment shows semantics preferences only.
 */
class SemanticsPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Define the settings file to use by this settings fragment
        val manager = this.preferenceManager
        manager.setSharedPreferencesName(getPreferenceFile(AppContext.context))
        manager.setSharedPreferencesMode(Context.MODE_PRIVATE)

        // Add the preferences defined from resources
        addPreferencesFromResource(R.xml.pref_semantics)

        // Bind the style factories' colors to their values.
        var pref = findPreference<Preference>(ColorSettings.PREF_PREDICATE_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.predicateBackColor = color
                Colors.predicateColor = computeForeColor(color)
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_SUBJECT_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.subjectBackColor = color
                Colors.subjectColor = computeForeColor(color)
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_OBJECT_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.objectBackColor = color
                Colors.objectColor = computeForeColor(color)
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_TERM_MODIFYING_SUBPREDICATE_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.termModifierPredicateBackColor = color
                Colors.termModifierPredicateColor = computeForeColor(color)
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_PREDICATE_MODIFYING_SUBPREDICATE_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.predicateModifierPredicateBackColor = color
                Colors.predicateModifierPredicateColor = computeForeColor(color)
            }
            true
        }
    }

    override fun onDisplayPreferenceDialog(preference: Preference) {
        val manager = parentFragmentManager
        val dialogFragment: PreferenceDialogFragmentCompat =
            when (preference) {
                is ColorChooserPreference -> ColorChooserPreferenceDialogFragment.newInstance(preference.key)
                is ColorPickerPreference -> ColorPickerPreferenceDialogFragment.newInstance(preference.key)
                else -> {
                    super.onDisplayPreferenceDialog(preference)
                    return
                }
            }
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

    companion object {

        private const val TAG = "SemanticsPrefF"
        private const val REQUEST_CODE = 456
    }
}
