/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.annotation

import android.graphics.Paint
import kotlin.math.min

object Utils {

    // T R U N C A T E

    /**
     * Truncate margin
     */
    const val NULL_LABEL = ""

    /**
     * Truncate margin
     */
    const val TRUNCATE_MARGIN = 30

    /**
     * Character used when there is not enough space. If null, label will be truncated, and rotated 90°
     */
    val LABEL_STAND_IN_CHAR: String? = null // "▾"; // "↓▾▿☟"

    /**
     * Ellipsis
     */
    const val ELLIPSIS = '…' // '⋮' '⋯' '…' '‥' '․'

    /**
     * Truncate length when label is vertical
     */
    const val LABEL_VERTICAL_TRUNCATE: Int = 1

    /**
     * Process label
     *
     * @param label label to be processed
     * @param maxWidth max admissible width
     * @param paint text paint
     * @return new label and whether it should be vertical
     */
    fun processLabel(label: String?, maxWidth: Float, paint: Paint): Pair<String, Boolean> {
        if (label == null) {
            return NULL_LABEL to false
        }
        // truncate if needed to fit in
        var isVertical = false
        var result: String? = truncate(label, maxWidth - TRUNCATE_MARGIN, paint)
        if (result == null) {
            // has failed
            if (LABEL_STAND_IN_CHAR != null)
                result = LABEL_STAND_IN_CHAR
            else {
                result = label
                if (result.length > LABEL_VERTICAL_TRUNCATE)
                    result = label.take(min(LABEL_VERTICAL_TRUNCATE, label.length)) + ELLIPSIS
                isVertical = true
            }
        }
        return result to isVertical
    }

    /**
     * Truncate label to width
     *
     * @param label   label
     * @param width   width
     * @param paint   paint for text measurement
     * @return truncated label, null if can't
     */
    fun truncate(label: String, width: Float, paint: Paint): String? {
        val tagCharWidth: Float = paint.measureText("M")
        val n = (width / tagCharWidth).toInt()

        // if available space width less than one character, return null
        if (n < 1) return null // can't

        // do not truncate if all fits in
        val xOffset = (width - paint.measureText(label)) / 2
        if (xOffset > 0) return label

        // truncate
        return if (n >= 3) {
            label.take(min(n - 2, label.length)) + ELLIPSIS
        } else {
            label.take(min(n, label.length))
        }
    }
}