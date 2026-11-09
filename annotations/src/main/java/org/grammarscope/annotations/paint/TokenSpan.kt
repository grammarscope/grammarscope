/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.graphics.Canvas
import android.graphics.Paint
import android.text.style.ReplacementSpan
import kotlin.math.roundToInt

class TokenSpan(val isRoot: Boolean = false) : ReplacementSpan() {

    /**
     * Returns the size
     *
     * @param paint – Paint instance.
     * @param text – Current text.
     * @param start – Start character index for span.
     * @param end – End character index for span.
     * @param fm - Font metrics, can be null.
     * @return Width of the span.
     */
    override fun getSize(
        paint: Paint,
        text: CharSequence,
        start: Int,
        end: Int,
        fm: Paint.FontMetricsInt?
    ): Int {
        return paint.measureText(text, start, end).roundToInt()
    }

    /**
     * Draws the span into the canvas.

     * @param canvas – Canvas into which the span should be rendered.
     * @param text – Current text.
     * @param start – Start character index for span.
     * @param end – End character index for span.
     * @param x – Edge of the replacement closest to the leading margin.
     * @param top – Top of the line.
     * @param y – Baseline.
     * @param bottom – Bottom of the line.
     * @param paint – Paint instance.
     */
    override fun draw(
        canvas: Canvas,
        text: CharSequence,
        start: Int,
        end: Int,
        x: Float,
        top: Int,
        y: Int,
        bottom: Int,
        paint: Paint
    ) {
        if (isRoot) {
            val paintRoot = Paint(paint).apply { color = Palette.rootColor }
            canvas.drawText(text, start, end, x, y.toFloat(), paintRoot)
        } else
            canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }
}