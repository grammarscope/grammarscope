/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.document

import android.graphics.Canvas
import android.graphics.Paint
import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineBreakConfigSpan
import android.text.style.ReplacementSpan
import org.depparse.Token
import kotlin.math.roundToInt

class TokenSpan : ReplacementSpan() {

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
        canvas.drawText(text, start, end, x, y.toFloat(), paint)
    }

    companion object {

        fun enforceNoBreaks(
            text: String,
            tokens: Array<Token>
        ): CharSequence {
            val sb = SpannableStringBuilder(text)
            tokens.forEach { token ->
                if (token.end > token.start)
                    applyNoBreakSpan(sb, token.start, token.end + 1)
            }
            return sb
        }

        fun applyNoBreakSpan(
            text: Spannable,
            start: Int,
            end: Int // exclusive
        ) {
            val span: Any =
                if (Build.VERSION.SDK_INT >= 35) {
                    LineBreakConfigSpan.createNoBreakSpan()
                } else {
                    TokenSpan()
                }

            text.setSpan(
                span,
                start,
                end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}