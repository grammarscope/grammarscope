package org.grammarscope

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.grammarscope.common.R

/**
 * This fragment shows system preferences only.
 */
class SystemPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_system)
        val pref = findPreference<Preference>("pref_system_arch")!!
        pref.setSummary(System.getProperty("os.arch"))
    }
}
