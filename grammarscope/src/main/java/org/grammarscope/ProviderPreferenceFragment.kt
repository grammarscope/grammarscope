package org.grammarscope

import android.os.Bundle
import android.util.Log
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import org.grammarscope.ProviderManager.requestKill
import org.grammarscope.ProviderManager.requestNew
import org.grammarscope.common.R

/**
 * This fragment shows service preferences only.
 */
class ProviderPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.pref_provider)

        val pref = findPreference<Preference>("pref_provider")!!
        pref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        pref.onPreferenceChangeListener = OnPreferenceChangeListener { preference: Preference, value: Any? ->

            if (value == null) {
                return@OnPreferenceChangeListener false
            }
            Log.d(TAG, "Provider changed $value")

            // consume
            requestKill(preference.context.applicationContext)

            // new
            requestNew(preference.context.applicationContext)
            true
        }
    }

    companion object {

        private const val TAG = "ProviderPreference"
    }
}
