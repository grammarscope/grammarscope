/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.annotation

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.withSave
import org.grammarscope.annotations.annotate.AnnotationManager
import org.grammarscope.annotations.annotation.Utils.processLabel
import org.grammarscope.annotations.paint.AnnotationPainter.OVERFLOW_HEIGHT
import org.grammarscope.annotations.paint.AnnotationPainter.OVERFLOW_WIDTH
import org.grammarscope.annotations.paint.AnnotationPainter.OVERFLOW_X_OFFSET
import org.grammarscope.annotations.paint.AnnotationPainter.OVERFLOW_Y_OFFSET
import org.grammarscope.annotations.paint.AnnotationPainter.overflowHandlePaint
import org.grammarscope.annotations.paint.AnnotationPainter.overflowPaint
import org.grammarscope.annotations.paint.AnnotationPainter.renderAsCurves
import org.grammarscope.annotations.paint.Palette

enum class AnnotationType {
    EDGE,
    BOX,
    LABEL,
}

/**
 * Sealed class for different types of between-lines annotations
 */
sealed class Annotation {

    open class BoxAnnotation(
        val box: RectF,
        val color: Int? = null,
        val isToken: Boolean = false,
    ) : Annotation() {

        open fun draw(canvas: Canvas) {
            val boxColor = color
            canvas.drawRect(box, if (isToken) tokenBoxPaint.apply { if (boxColor != null) this.color = boxColor } else boxPaint.apply { if (boxColor != null) this.color = boxColor })
        }

        companion object {
            var boxPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            var tokenBoxPaint = Paint().apply {
                style = Paint.Style.FILL
            }
        }
    }

    class LabelAnnotation(
        box: RectF,
        val label: String,
        val backColor: Int? = null,
        val foreColor: Int? = null,
    ) : BoxAnnotation(box, color = backColor, isToken = false) {

        override fun draw(canvas: Canvas) {
            val (label, isVertical) = processLabel(label, box.width(), textPaint)
            val width = textPaint.measureText(label)
            val start = box.left + (box.width() - width) / 2f
            val base = box.top - textPaint.fontMetrics.ascent
            val ascent = textPaint.fontMetrics.ascent
            val descent = textPaint.fontMetrics.descent
            val rect = RectF(start - INFLATE, base + ascent - INFLATE, start + width + INFLATE, base + descent + INFLATE)
            if (isVertical)
                canvas.withSave {
                    rotate(90F, rect.left, rect.bottom)
                    val height = descent - ascent
                    translate(-height - 2 * INFLATE, -height / 2F - 4 * INFLATE)
                    backColor?.let { canvas.drawRect(rect, boxPaint.apply { color = it }) }
                    canvas.drawText(label, start, base, textPaint.apply { color = foreColor ?: DEFAULT_BOX_FORECOLOR })
                    canvas.drawRect(rect, framePaint.apply { color = foreColor ?: DEFAULT_BOX_FORECOLOR })
                } else {
                backColor?.let { canvas.drawRect(rect, boxPaint.apply { color = it }) }
                canvas.drawText(label, start, base, textPaint.apply { color = foreColor ?: DEFAULT_BOX_FORECOLOR })
                canvas.drawRect(rect, framePaint.apply { color = foreColor ?: DEFAULT_BOX_FORECOLOR })
            }
        }

        companion object {
            const val INFLATE = 2F

            const val DEFAULT_BOX_FORECOLOR = Color.DKGRAY

            var textPaint = Paint().apply {
                style = Paint.Style.FILL_AND_STROKE
                color = DEFAULT_BOX_FORECOLOR
                typeface = Typeface.DEFAULT_BOLD
                textSize = AnnotationManager.LABEL_TEXT_SIZE
                isAntiAlias = true
            }
            var framePaint = Paint().apply {
                style = Paint.Style.STROKE
                color = DEFAULT_BOX_FORECOLOR
                strokeWidth = 2F
                isAntiAlias = false
            }
        }
    }

    data class EdgeAnnotation(
        val edge: Edge
    ) : Annotation() {

        fun draw(canvas: Canvas, padWidth: Float) {
            if (edge.isVisible) {
                edge.draw(canvas, renderAsCurves)
                edge.drawTag(canvas)
            } else {

                // can't fit in vertically / overflow
                var y: Float = edge.bottom - 1F

                // line
                canvas.drawLine(0f, y, padWidth, y, overflowPaint.apply { color = Palette.overflowColor })

                // overflow handle
                y -= OVERFLOW_Y_OFFSET
                val x = padWidth - OVERFLOW_X_OFFSET - OVERFLOW_WIDTH
                val path = Path().apply {
                    moveTo(x - OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                    lineTo(x, y)
                    lineTo(x + OVERFLOW_WIDTH, y - OVERFLOW_HEIGHT)
                    close()
                }
                canvas.drawPath(path, overflowHandlePaint.apply { color = Palette.overflowColor })
            }
        }

        override fun toString(): String {
            return "EdgeAnnotation ${edge.tag} y=${edge.yBase} h=${edge.height} x1=${edge.x1} x2=${edge.x2}"
        }
    }
}