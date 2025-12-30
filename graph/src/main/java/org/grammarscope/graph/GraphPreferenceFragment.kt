package org.grammarscope.graph

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.core.content.edit
import androidx.preference.Preference
import androidx.preference.Preference.OnPreferenceChangeListener
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_LAYOUT
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_BALLOON
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_RADIAL
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_RBALLOON
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_RRADIAL
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_RTREE
import edu.uci.ics.jung.settings.BaseSettings.Companion.PREF_VALUE_LAYOUT_TREE
import edu.uci.ics.jung.settings.GraphSettingsPreferenceFragment
import org.depparse.common.AppContext
import org.grammarscope.graph.CommonSettings.Companion.PREF_EDGE_REVERSE_DIRECTION
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_ICON
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_BACK_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_LABEL_COLOR
import org.grammarscope.graph.CommonSettings.Companion.PREF_ROOT_VERTEX_SHAPE

/**
 * This fragment shows vertex preferences only.
 */
abstract class GraphPreferenceFragment : GraphSettingsPreferenceFragment() {

    protected abstract fun getPreferenceFile(context: Context): String

    override lateinit var preferenceFile: String

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceFile = getPreferenceFile(AppContext.context)

        super.onCreatePreferences(savedInstanceState, rootKey)

        val manager = preferenceManager
        manager.setSharedPreferencesName(preferenceFile)
        manager.setSharedPreferencesMode(Context.MODE_PRIVATE)

        findPreference<Preference>(PREF_ROOT_VERTEX_SHAPE)!!.setSummaryProvider { pref -> getSummary(pref, preferenceManager) }
        findPreference<Preference>(PREF_ROOT_VERTEX_ICON)!!.setSummaryProvider { pref -> getSummary(pref, preferenceManager) }
        findPreference<Preference>(PREF_ROOT_VERTEX_COLOR)!!.setSummaryProvider { pref -> getColorOrNoneSummary(pref, preferenceManager) }
        findPreference<Preference>(PREF_ROOT_VERTEX_LABEL_COLOR)!!.setSummaryProvider { pref -> getColorOrNoneSummary(pref, preferenceManager) }
        findPreference<Preference>(PREF_ROOT_VERTEX_LABEL_BACK_COLOR)!!.setSummaryProvider { pref -> getColorOrNoneSummary(pref, preferenceManager) }

        // reverse edge direction preference
        val reverseEdgeDirectionPref = findPreference<Preference>(PREF_EDGE_REVERSE_DIRECTION)!!
        // master preference listener
        reverseEdgeDirectionPref.onPreferenceChangeListener =
            OnPreferenceChangeListener { preference, newValue ->
                Log.d(TAG, "new value: $newValue")
                setReverseEdgeDirectionDependentPreferences(sharedPreferences)
                true
            }
    }

    private fun reverseEdgeDirectionDependentLayoutOrNull(sharedPreferences: SharedPreferences): String? {
        val layout = sharedPreferences.getString(PREF_LAYOUT, null)
        return when (layout) {
            PREF_VALUE_LAYOUT_TREE -> PREF_VALUE_LAYOUT_RTREE
            PREF_VALUE_LAYOUT_RADIAL -> PREF_VALUE_LAYOUT_RRADIAL
            PREF_VALUE_LAYOUT_BALLOON -> PREF_VALUE_LAYOUT_RBALLOON
            PREF_VALUE_LAYOUT_RTREE -> PREF_VALUE_LAYOUT_TREE
            PREF_VALUE_LAYOUT_RRADIAL -> PREF_VALUE_LAYOUT_RADIAL
            PREF_VALUE_LAYOUT_RBALLOON -> PREF_VALUE_LAYOUT_BALLOON

            else -> null
        }
    }

    fun setReverseEdgeDirectionDependentPreferences(sharedPreferences: SharedPreferences) {
        val layoutValue = reverseEdgeDirectionDependentLayoutOrNull(sharedPreferences) ?: return
        sharedPreferences.edit(commit = true) {
            putString(PREF_LAYOUT, layoutValue)
        }
    }

    companion object {
        const val TAG = "GraphPreferenceFragment"
    }
}
