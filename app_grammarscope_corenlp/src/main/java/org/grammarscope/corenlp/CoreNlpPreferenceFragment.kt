/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.corenlp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import androidx.preference.Preference.SummaryProvider
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import org.grammarscope.ProviderManager.requestKill
import org.grammarscope.ProviderManager.requestNew

class CoreNlpPreferenceFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Define the settings file to use by this settings fragment
        preferenceManager.apply {
            setSharedPreferencesName(PREF_FILE)
            setSharedPreferencesMode(Context.MODE_PRIVATE)
        }

        addPreferencesFromResource(R.xml.pref_corenlp)

        val prefNeural = findPreference<SwitchPreferenceCompat>(PREF_NEURAL)!!
        prefNeural.summaryProvider = BOOL_SUMMARY_PROVIDER
        prefNeural.onPreferenceChangeListener = OnPreferenceChangeListener { preference: Preference, value: Any? ->
            if (value == null) {
                return@OnPreferenceChangeListener false
            }
            Log.d(TAG, "Neural/constituency changed $value")

            // consume
            requestKill(preference.context.applicationContext)

            // new
            requestNew(preference.context.applicationContext)
            true
        }
    }

    companion object {

        private const val TAG = "CoreNlpPreference"

        private const val PREF_FILE = "corenlp"

        private const val PREF_NEURAL = "neural" // repeated here to avoid dependency, used in CoreNlpEngine

        private val BOOL_SUMMARY_PROVIDER = SummaryProvider { preference: SwitchPreferenceCompat ->
            preference.context.getString(
                if (preference.isChecked)
                    R.string.pref_neural_value_on
                else
                    R.string.pref_neural_value_off
            )
        }
    }
}
