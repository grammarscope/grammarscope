/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope.annotations

import android.os.Bundle
import androidx.fragment.app.Fragment
import org.depparse.common.BaseSettingsActivity

/**
 * Dependency Preference Activity
 */
class AnnotationsSettingsActivity : BaseSettingsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize if needed
        AnnotationsSettings(this).initializePrefs()
    }

    /**
     * Make initial fragment
     *
     * @return initial fragment
     */
    override fun makeFragment(): Fragment {
        return AnnotationsPreferenceFragment()
    }

    /**
     * Reset settings
     */
    override fun resetSettings() {
        AnnotationsSettings(this).reset()
    }
}
