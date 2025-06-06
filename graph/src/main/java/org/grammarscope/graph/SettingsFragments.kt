package org.grammarscope.graph

import android.content.Context
import org.grammarscope.graph.CommonSettings.Companion.isNightMode

class SettingsFragments {

    /**
     * This fragment shows dependency graph preferences only.
     */
    class DependencyGraphPreferenceFragment : GraphPreferenceFragment() {

        override fun getPreferenceFile(context: Context): String {
            return DependencySettings.getPreferenceFile(isNightMode(context))
        }
    }

    /**
     * This fragment shows semantic graph preferences only.
     */
    class SemanticGraphPreferenceFragment : GraphPreferenceFragment() {

        override fun getPreferenceFile(context: Context): String {
            return SemanticSettings.getPreferenceFile(isNightMode(context))
        }
    }
}
