/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.graphics.Paint

object Metrics {

    @JvmStatic
    fun Paint.FontMetrics.height(): Float {
        return descent - ascent + leading // precise line height information, and be sure you're including the inter-line spacing
        // return fontSpacing // a quick approximation of the line height.
    }
}