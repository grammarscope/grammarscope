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
import org.depparse.common.Colors.getColors
import org.grammarscope.ColorSettings.Companion.getPreferenceFile
import org.grammarscope.common.R
import org.jung.colors.ColorPreference
import org.jung.colors.chooser.ColorChooserPreference
import org.jung.colors.chooser.ColorChooserPreferenceDialogFragment
import org.jung.colors.picker.ColorPickerPreference
import org.jung.colors.picker.ColorPickerPreferenceDialogFragment
import org.depparse.common.R as CommonR

/**
 * This fragment shows text preferences only.
 */
class TextPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Define the settings file to use by this settings fragment
        val manager = preferenceManager
        manager.setSharedPreferencesName(getPreferenceFile(AppContext.context))
        manager.setSharedPreferencesMode(Context.MODE_PRIVATE)

        // Add the preferences defined from resources
        addPreferencesFromResource(R.xml.pref_text)

        // Bind the style factories' colors to their values.
        var pref = findPreference<Preference>(ColorSettings.PREF_ROOT_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.rootBackColor = color
                Colors.rootColor = computeForeColor(color)
            } else {
                val colors = getColors(requireContext(), CommonR.color.rootColor, CommonR.color.rootBackColor)
                Colors.rootBackColor = colors[1]
                Colors.rootColor = colors[0]
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_LABEL_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.labelBackColor = color
                Colors.labelColor = computeForeColor(color)
            } else {
                val colors = getColors(requireContext(), CommonR.color.labelColor, CommonR.color.labelBackColor)
                Colors.labelBackColor = colors[1]
                Colors.labelColor = colors[0]
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_ENHANCED_LABEL_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.enhancedLabelBackColor = color
                Colors.enhancedLabelColor = computeForeColor(color)
            } else {
                val colors = getColors(requireContext(), CommonR.color.enhancedLabelColor, CommonR.color.enhancedLabelBackColor)
                Colors.enhancedLabelBackColor = colors[1]
                Colors.enhancedLabelColor = colors[0]
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_HEAD_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.headBackColor = color
                Colors.headColor = computeForeColor(color)
            } else {
                val colors = getColors(requireContext(), CommonR.color.headColor, CommonR.color.headBackColor)
                Colors.headBackColor = colors[1]
                Colors.headColor = colors[0]
            }
            true
        }
        pref = findPreference(ColorSettings.PREF_DEPENDENT_COLOR)!!
        pref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _: Preference?, value: Any? ->
            if (value != null) {
                val color = value as Int
                Colors.dependentBackColor = color
                Colors.dependentColor = computeForeColor(color)
            } else {
                val colors = getColors(requireContext(), CommonR.color.dependentColor, CommonR.color.dependentBackColor)
                Colors.dependentBackColor = colors[1]
                Colors.dependentColor = colors[0]
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
        preference as ColorPreference
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
        dialogFragment.show(manager, "org.grammarscope.TextPreferenceFragment.DIALOG")
    }

    companion object {

        private const val TAG = "TextPrefF"
        private const val REQUEST_CODE = 123
    }
}
