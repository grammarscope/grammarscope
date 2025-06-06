/*
 * Copyright (c) 2021. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope.graph

import android.content.Context
import glue.Color
import org.depparse.common.BuildConfig

object GraphColors {

    var backColor: Color = Color.WHITE
    var vertexColor: Color = Color.ORANGE
    var rootVertexColor: Color = Color.RED
    var vertexLabelColor: Color = Color.BLACK
    var edgeColor: Color = Color.DARK_GRAY
    var edgeLabelColor: Color = Color.BLACK
    var predicateColor: Color = Color.RED
    var subjectColor: Color = Color.BLUE
    var objectColor: Color = Color.MAGENTA
    var termModifyingPredicateColor: Color = Color.PINK
    var predicateModifyingPredicateColor: Color = Color.ORANGE

    fun setColorsFromResources(context: Context) {
        // Log.d(TAG, "setColorsFromResources night=" + BaseSettings.isNightMode(context))
        val palette = context.resources.getIntArray(R.array.palette_graph)
        var i = 0
        backColor = Color(palette[i++])
        vertexColor = Color(palette[i++])
        rootVertexColor = Color(palette[i++])
        vertexLabelColor = Color(palette[i++])
        edgeColor = Color(palette[i++])
        edgeLabelColor = Color(palette[i++])
        predicateColor = Color(palette[i++])
        subjectColor = Color(palette[i++])
        objectColor = Color(palette[i++])
        termModifyingPredicateColor = Color(palette[i++])
        predicateModifyingPredicateColor = Color(palette[i++])
        if (BuildConfig.DEBUG && i != palette.size) {
            throw AssertionError("Assertion failed")
        }
    }
}
