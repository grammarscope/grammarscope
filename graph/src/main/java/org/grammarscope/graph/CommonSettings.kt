package org.grammarscope.graph

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import androidx.annotation.CallSuper
import androidx.core.content.edit
import edu.uci.ics.jung.settings.Settings

abstract class CommonSettings protected constructor(
    context: Context, preferenceFile: String
) : Settings(context, preferenceFile) {

    var reverseDirection = false

    // I N I T I A L I Z E

    /**
     * Initialize
     *
     * @return true if successful, false if fail or already done
     */
    @CallSuper
    open fun initializePrefs(): Boolean {
        val initialized = sharedPrefs.getBoolean(PREF_INITIALIZED, false)
        if (initialized) {
            return false
        }
        // initialize
        sharedPrefs.edit(commit = true) {
            this
                .remove(PREF_BACK_COLOR)
                // vertex
                .putString(PREF_LAYOUT, defaultLayout)
                .putString(PREF_VERTEX_SHAPE, defaultVertexShape)
                .putString(PREF_VERTEX_ICON, PREF_VALUE_ICON_NONE)
                .putString(PREF_VERTEX_ICON_SET, PREF_VALUE_ICON_SET_BASE)
                .putInt(PREF_VERTEX_COLOR, GraphColors.vertexColor.rgb)
                .putInt(PREF_VERTEX_SIZE, defaultVertexSize)
                .putInt(PREF_VERTEX_ASPECT_RATIO, 100)
                .putInt(PREF_VERTEX_LABEL_SIZE, defaultVertexLabelSize)
                .putInt(PREF_VERTEX_LABEL_COLOR, GraphColors.vertexLabelColor.rgb)
                .remove(PREF_VERTEX_LABEL_BACK_COLOR)
                .putString(PREF_VERTEX_LABEL_POSITION, defaultVertexLabelPosition.toString())
                .putInt(PREF_VERTEX_LABEL_OFFSET, 10)
                .putBoolean(PREF_VERTEX_LABEL_FRAME, false)
                // edge
                .putString(PREF_EDGE_SHAPE, defaultEdgeShape)
                .putString(PREF_EDGE_STROKE, PREF_VALUE_STROKE_SOLID)
                .putInt(PREF_EDGE_COLOR, GraphColors.edgeColor.rgb)
                .putInt(PREF_EDGE_EXPAND_BASE, 60)
                .putInt(PREF_EDGE_EXPAND_INC, 40)
                .putInt(PREF_EDGE_SHIFT_INC, 10)
                .putInt(PREF_EDGE_LABEL_SIZE, defaultEdgeLabelSize)
                .putInt(PREF_EDGE_LABEL_COLOR, GraphColors.edgeLabelColor.rgb)
                .remove(PREF_EDGE_LABEL_BACK_COLOR)
                .putInt(PREF_EDGE_LABEL_POSITION, 50)
                .putInt(PREF_EDGE_LABEL_OFFSET, 0)
                .putBoolean(PREF_EDGE_LABEL_ROTATE, false)
                .putBoolean(PREF_EDGE_LABEL_FRAME, false)
                .putBoolean(PREF_EDGE_LABEL_ANCHOR, true)
                .putBoolean(PREF_EDGE_ARROW_CENTER, false)

                .putBoolean(PREF_EDGE_REVERSE_DIRECTION, false)
                .putBoolean(PREF_INITIALIZED, true)
        }
        return true
    }

    /**
     * Reset settings
     */
    fun reset() {
        val editor = sharedPrefs.edit()
        editor.clear()
        tryCommit(editor)
    }

    companion object {

        const val PREF_FILE_PREFIX = "org.grammarscope_preferences_"

        const val PREF_FILE_NIGHT_SUFFIX = "_night"

        internal const val PREF_INITIALIZED = "pref_initialized_graph"

        const val PREF_ROOT_VERTEX_SHAPE = "root_vertex_shape"
        const val PREF_ROOT_VERTEX_ICON = "root_vertex_icon"
        const val PREF_ROOT_VERTEX_COLOR = "root_vertex_color"
        const val PREF_ROOT_VERTEX_LABEL_COLOR = "root_vertex_label_color"
        const val PREF_ROOT_VERTEX_LABEL_BACK_COLOR = "root_vertex_label_back_color"

        const val PREF_EDGE_REVERSE_DIRECTION = "edge_reverse_direction"

        /**
         * Test whether in night mode.
         *
         * @param context context
         * @return -1 if in night mode, 1 in day mode
         */
        fun isNightMode(context: Context): Boolean {
            val nightModeFlags = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            return when (nightModeFlags) {
                Configuration.UI_MODE_NIGHT_YES -> true
                else -> false
            }
        }

        /**
         * Try to commit
         *
         * @param editor editor editor
         */
        @SuppressLint("CommitPrefEdits", "ApplySharedPref")
        fun tryCommit(editor: SharedPreferences.Editor) {
            try {
                editor.apply()
            } catch (_: AbstractMethodError) {
                // The app injected its own pre-Gingerbread SharedPreferences.Editor implementation without an apply method.
                editor.commit()
            }
        }
    }
}
