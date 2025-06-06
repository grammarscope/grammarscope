package org.grammarscope.graph

import android.content.Context
import androidx.core.content.edit
import edu.uci.ics.jung.visualization.renderers.IRenderer.VertexLabel

class DependencySettings @JvmOverloads constructor(context: Context, nightMode: Boolean = isNightMode(context)) : CommonSettings(context, getPreferenceFile(nightMode)) {

    init {
        super.defaultLayout = PREF_VALUE_LAYOUT_SBLINEAR
        super.defaultVertexShape = PREF_VALUE_VERTEX_SHAPE_CIRCLE
        super.defaultVertexSize = 50
        super.defaultVertexIconSet = "base"
        super.defaultVertexLabelSize = 16
        super.defaultVertexLabelPosition = VertexLabel.Position.SW
        super.defaultEdgeShape = PREF_VALUE_EDGE_SHAPE_E_ROUNDED_BRACKET
        super.defaultEdgeLabelSize = 16
        super.reverseDirection = makeBool(PREF_EDGE_REVERSE_DIRECTION, false)
    }

    /**
     * Initialize
     *
     * @return true if successful, false if fail or already done
     */
    override fun initializePrefs(): Boolean {
        val initialized = sharedPrefs.getBoolean(PREF_INITIALIZED, false)
        if (initialized) {
            return false
        }
        super.initializePrefs()
        // initialize
        sharedPrefs.edit(commit = true) {

        }
        return true
    }

    companion object {

        private const val NAME = PREF_FILE_PREFIX + "deps"

        fun getPreferenceFile(nightMode: Boolean): String {
            var name = NAME
            if (nightMode) {
                name += PREF_FILE_NIGHT_SUFFIX
            }
            return name
        }
    }
}
