package org.grammarscope

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import org.depparse.common.BaseSettingsActivity
import org.depparse.common.Colors.setColorsFromResources
import org.grammarscope.Application.Companion.clearSettings
import org.grammarscope.Application.Companion.initSettings
import org.grammarscope.common.R
import org.grammarscope.graph.DependencySettings
import org.grammarscope.graph.SemanticSettings

/**
 * Preference Activity
 */
class SettingsActivity : BaseSettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize if needed
        GeneralSettings(this).initializePrefs()
        ColorSettings(this).initializePrefs()
        DependencySettings(this).initializePrefs()
        SemanticSettings(this).initializePrefs()
    }

    /**
     * Make initial fragment
     *
     * @return initial fragment
     */
    override fun makeFragment(): Fragment {
        return HeaderFragment()
    }

    /**
     * Reset settings
     */
    override fun resetSettings() {
        clearSettings(this)
        initSettings(this)
        setColorsFromResources(this)
    }


    // H E A D E R   F R A G M E N T

    class HeaderFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.pref_headers, rootKey)
        }
    }
}
