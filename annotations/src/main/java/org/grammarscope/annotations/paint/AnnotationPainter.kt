/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.paint

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathEffect
import android.graphics.RectF
import org.grammarscope.annotations.annotation.Annotation.BoxAnnotation
import org.grammarscope.annotations.annotation.Annotation.EdgeAnnotation
import org.grammarscope.annotations.annotation.Annotation.LabelAnnotation
import org.grammarscope.annotations.annotation.CurlyBracePath

object AnnotationPainter {

    fun paintLabels(canvas: Canvas, boxAnnotations: Collection<LabelAnnotation>) {
        for (boxAnnotation in boxAnnotations) {
            boxAnnotation.draw(canvas)
        }
    }

    fun paintBoxes(canvas: Canvas, boxAnnotations: Collection<BoxAnnotation>) {
        for (boxAnnotation in boxAnnotations) {
            boxAnnotation.draw(canvas)
        }
    }

    fun paintEdges(canvas: Canvas, edgeAnnotations: Collection<EdgeAnnotation>, padWidth: Float) {

        // draw edge
        for (edgeAnnotation in edgeAnnotations) {
            edgeAnnotation.draw(canvas, padWidth)
        }

        // draw labels
        //for (edgeAnnotation in edgeAnnotations) {
        //    if (edgeAnnotation.edge.isVisible) {
        //        edgeAnnotation.edge.drawTag(canvas)
        //    }
        //}
    }

    /**
     * Draw box
     *
     * @param canvas   graphics context
     * @param box box for typed dependency node
     */
    private fun drawCurlyBraceSpan(canvas: Canvas, box: RectF) {
        val boxL = box.left
        val boxT = box.top
        val boxW = box.width()
        val boxR = boxL + boxW
        val boxM = boxL + boxW / 2F

        val shape: Path = CurlyBracePath(boxL, boxR, boxM, boxT, false)
        canvas.drawPath(shape, spanPaint.apply { color = Palette.spanColor })
    }

    var renderAsCurves = true

    const val OVERFLOW_X_OFFSET = 50F
    const val OVERFLOW_Y_OFFSET = 10F
    const val OVERFLOW_WIDTH = 30F
    const val OVERFLOW_HEIGHT = 30F

    // STROKES
    val SOLID: PathEffect? = null
    val DOTTED: PathEffect = DashPathEffect(floatArrayOf(1.0f, 1.0f), 0f)
    val DASHED: PathEffect = DashPathEffect(floatArrayOf(5.0f, 5.0f), 0f)
    val EDGE_STROKE: PathEffect? = SOLID
    const val OVERFLOW_STROKE_WIDTH = 8F
    val OVERFLOW_STROKE: PathEffect = DashPathEffect(floatArrayOf(20f, 5f, 5f, 5f), 0f)
    val OVERFLOW_HANDLE_STROKE: PathEffect? = SOLID
    val SPAN_STROKE: PathEffect? = SOLID

    val overflowPaint = Paint().apply {
        color = Palette.overflowColor
        style = Paint.Style.STROKE
        strokeWidth = OVERFLOW_STROKE_WIDTH
        pathEffect = OVERFLOW_STROKE
    }

    val overflowHandlePaint = Paint().apply {
        color = Palette.overflowColor
        style = Paint.Style.STROKE
        strokeWidth = OVERFLOW_STROKE_WIDTH
        pathEffect = OVERFLOW_HANDLE_STROKE
    }

    val spanPaint = Paint().apply {
        color = Palette.spanColor
        pathEffect = SPAN_STROKE
    }
}