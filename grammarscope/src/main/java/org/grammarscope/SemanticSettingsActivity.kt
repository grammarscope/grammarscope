/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.depparse.common.BaseSettingsActivity
import org.grammarscope.graph.SemanticSettings
import org.grammarscope.graph.SettingsFragments.SemanticGraphPreferenceFragment

/**
 * A PreferenceActivity that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, forActivity category headers shown to the left of
 * the list of settings.
 *
 *
 * See [
 * Android Design: Settings](http://developer.android.com/design/patterns/settings.html) for design guidelines and the [Settings
 * API Guide](http://developer.android.com/guide/topics/ui/settings.html) for more information on developing a Settings UI.
 */
class SemanticSettingsActivity : BaseSettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize if needed
        SemanticSettings(this).initializePrefs()
    }

    /**
     * Make initial fragment
     *
     * @return initial fragment
     */
    override fun makeFragment(): Fragment {
        return SemanticGraphPreferenceFragment()
    }

    /**
     * Reset settings
     */
    override fun resetSettings() {
        SemanticSettings(this).reset()
    }
}
