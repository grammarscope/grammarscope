/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.annotate

import android.graphics.Paint
import android.graphics.Rect
import android.util.Log
import android.widget.TextView
import org.grammarscope.annotations.AnnotatedTextView.Companion.lineBounds
import org.grammarscope.annotations.paint.Metrics.height

class AnnotationManager(val textView: TextView) {

    enum class Type { DEP, POS }

    /**
     * Height of the TextView, excluding padding
     */
    val height: Int
        get() = textView.height - textView.paddingTop - textView.paddingBottom

    /**
     * Metrics for text: ascent + descent + leading
     */
    val lineHeight: Float
        get() = textView.paint.fontMetrics.height()

    /**
     * Annotation space between lines
     */
    val annotationHeight: Float
        get() = textView.lineSpacingExtra

    init {
        Log.d(TAG, "Annotation height $annotationHeight")
    }

    fun allocate(type: Type): Float {
        return when (type) {
            Type.POS -> TOP_OFFSET
            Type.DEP -> TOP_OFFSET + Paint().apply { textSize = LABEL_TEXT_SIZE }.fontMetrics.height() + TOP_OFFSET
        }
    }

    fun dumpLineBounds() {
        val bounds: List<Rect> = textView.lineBounds()
        for (bound in bounds) {
            Log.d(TAG, "Line bounds: $bound")
        }
    }

    companion object {
        const val TAG = "Space manager"

        const val INITIAL_LINESPACING = 500F
        const val LINE_SPACING_MULTIPLIER = 1F
        const val INITIAL_WORDSPACING = 0F

        const val TOP_OFFSET = 20F

        const val INITIAL_TEXTSIZE = 24F
        const val EDGE_TAG_TEXT_SIZE = 40F
        const val LABEL_TEXT_SIZE = 40F
    }
}