/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.depparse.common.BaseSettingsActivity
import org.grammarscope.graph.DependencySettings
import org.grammarscope.graph.SettingsFragments.DependencyGraphPreferenceFragment

/**
 * Dependency Preference Activity
 */
class DependencySettingsActivity : BaseSettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize if needed
        DependencySettings(this).initializePrefs()
    }

    /**
     * Make initial fragment
     *
     * @return initial fragment
     */
    override fun makeFragment(): Fragment {
        return DependencyGraphPreferenceFragment()
    }

    /**
     * Reset settings
     */
    override fun resetSettings() {
        DependencySettings(this).reset()
    }
}
