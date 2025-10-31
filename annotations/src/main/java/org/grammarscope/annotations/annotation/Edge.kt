/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.annotation

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.withSave
import org.grammarscope.annotations.annotate.AnnotationManager
import org.grammarscope.annotations.annotation.Utils.processLabel
import org.grammarscope.annotations.paint.AnnotationPainter.EDGE_STROKE
import org.grammarscope.annotations.paint.CurvePath
import org.grammarscope.annotations.paint.Metrics.height
import org.grammarscope.annotations.paint.Palette.arrowStartColor
import org.grammarscope.annotations.paint.Palette.arrowTipColor
import org.grammarscope.annotations.paint.Palette.edgeColor
import org.grammarscope.annotations.paint.Palette.labelColor
import org.grammarscope.annotations.paint.Utils.drawDot
import org.grammarscope.annotations.paint.Utils.drawHorizontalArrow
import org.grammarscope.annotations.paint.Utils.drawTriangle
import java.lang.Math.toDegrees
import kotlin.math.atan2

/**
 * Edge as used by renderer
 *
 * @property x1              x1
 * @property x2              x2
 * @property yBase           y
 * @property x1Anchor        anchor x1, offset from x1
 * @property x2Anchor        anchor x2, offset from x2
 * @property yAnchor         anchor y
 * @property height          height
 * @property tag             edge label/tag
 * @property isVertical      label/tag is vertical
 * @property color           color
 * @property isBackwards     direction
 * @property isLeftTerminal  is left terminal
 * @property isRightTerminal is right terminal
 * @property isVisible       is visible
 * @property bottom          pad bottom
 */
data class Edge(
    /**
     * Edge x left coordinate
     */
    val x1: Float,
    /**
     * Edge x right coordinate
     */
    val x2: Float,
    /**
     * Edge vertical base position
     */
    val yBase: Float,
    /**
     * Edge x left anchor
     */
    val x1Anchor: Float,
    /**
     * Edge x right anchor
     */
    val x2Anchor: Float,
    /**
     * Edge vertical base position
     */
    val yAnchor: Float,
    /**
     * Edge height
     */
    val height: Float,
    /**
     * Bottom past which this edge is not visible
     */
    val bottom: Float,
    /**
     * Edge tag (relation)
     */
    val tag: String,
    /**
     * Tag start position
     */
    val tagPosition: PointF,
    /**
     * Tag width
     */
    val tagWidth: Float,
    /**
     * Tag space
     */
    val tagRectangle: RectF,
    /**
     * Whether this label is vertical
     */
    val isVertical: Boolean,
    /**
     * Whether this edge is continued on next line
     */
    val isBackwards: Boolean,
    /**
     * Whether this edge is continued on previous line
     */
    val isLeftTerminal: Boolean,
    /**
     * Whether this edge is continued on next line
     */
    val isRightTerminal: Boolean,
    /**
     * Whether this edge is visible
     */
    val isVisible: Boolean,
    /**
     * Annotation color
     */
    val annotationColor: Int?,
) {
    // P O S I T I O N   A N D   L A Y O U T

    /**
     * Rectangle for edge space including tag
     */
    val rectangle: RectF
        get() = RectF(x1, yBase - height, x2 - x1, height)

    // D R A W

    /**
     * Draw edge
     *
     * @param canvas      graphics context
     * @param asCurve whether to draw edge as curve (or straight arrow)
     */
    fun draw(canvas: Canvas, asCurve: Boolean) {
        if (asCurve) {
            drawCurvePath(canvas)
        } else {
            drawStraightArrow(canvas)
        }
    }

    /**
     * Draw edge as curve
     *
     * @param canvas canvas
     */
    private fun drawCurvePath(canvas: Canvas) {
        // edge
        val xFrom = if (!isLeftTerminal) x1 else x1 + x1Anchor
        val xTo = if (!isRightTerminal) x2 else x2 + x2Anchor - 1F
        val xTextFrom = tagRectangle.left
        val xTextTo = tagRectangle.left + tagRectangle.width()
        val yText: Float = if (isVertical) tagPosition.y else tagRectangle.top + tagRectangle.height() / 2
        val yFrom = if (isLeftTerminal) yAnchor else yText
        val yTo = if (isRightTerminal) yAnchor else yText
        val flatLeft = !isLeftTerminal
        val flatRight = !isRightTerminal

        // curve
        val shape = CurvePath(xFrom, xTo, xTextFrom, xTextTo, yFrom, yTo, yText, flatLeft, flatRight)
        canvas.drawPath(shape, edgePaint.apply { color = edgeColor; isAntiAlias = true })

        // control
        // val cox1 = shape.xCornerRight
        // val cox2 = shape.xCornerLeft
        val ctx1 = shape.xControlRight
        val ctx2 = shape.xControlLeft
        val cty = shape.yBase

        // arrow tip at corner
        if (isRightTerminal && !isBackwards) {
            //val xc = shape.xCornerRight
            //val yc = yText
            //val theta = toDegrees(atan2(yTo.toDouble() - yc, xTo.toDouble() - xc)) // corner
            //canvas.drawTriangle(xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = false, rotation = theta.toFloat(), paint)
            val theta = toDegrees(atan2(yTo.toDouble() - cty, xTo.toDouble() - ctx1))
            canvas.drawTriangle(xTo, yTo, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = false, rotation = theta.toFloat(), arrowPaint.apply { color = arrowTipColor })
        }
        if (isLeftTerminal && isBackwards) {
            // val xc = shape.xCornerLeft
            // val yc = yText
            // val theta = toDegrees(atan2(yc - yTo.toDouble(), xc - xTo.toDouble())) // corner
            // canvas.drawTriangle(xc, yc, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = true, rotation = theta.toFloat(), paint)
            val theta = toDegrees(atan2(cty.toDouble() - yFrom, ctx2.toDouble() - xFrom))
            canvas.drawTriangle(xFrom, yFrom, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, reverse = true, rotation = theta.toFloat(), arrowPaint.apply { color = arrowTipColor })
        }

        // g.drawDot(Color.RED, cox1, cty, 1)
        // g.drawDot(Color.RED, cox2, cty, 1)
        // g.drawDiamond(Color.BLUE, ctx1, cty, 1)
        // g.drawDiamond(Color.BLUE, ctx2, cty, 1)
    }

    /**
     * Draw edge as straight arrow
     *
     * @param canvas canvas
     */
    private fun drawStraightArrow(canvas: Canvas) {
        // edge
        canvas.drawLine(x1, yBase, x2 - 1F, yBase, edgePaint.apply { color = edgeColor; isAntiAlias = false })

        // arrow tip
        val drawArrowEnd = if (isBackwards) isLeftTerminal else isRightTerminal
        if (drawArrowEnd) {
            val xTip = if (isBackwards) x1 else x2
            canvas.drawHorizontalArrow(xTip, yBase, ARROW_TIP_WIDTH, ARROW_TIP_HEIGHT, arrowPaint.apply { color = arrowTipColor }, leftward = isBackwards)
        }

        // arrow start
        val drawArrowStart = if (isBackwards) isRightTerminal else isLeftTerminal
        if (drawArrowStart) {
            val xTip = if (isBackwards) x2 - ARROW_START_DIAMETER else x1 + ARROW_START_DIAMETER
            canvas.drawDot(xTip, yBase, ARROW_START_DIAMETER, arrowPaint.apply { color = arrowStartColor })
        }
    }

    /**
     * Draw tag
     *
     * @param canvas canvas
     */
    fun drawTag(canvas: Canvas) {
        // tag background
        annotationColor?.let { canvas.drawRect(tagRectangle.left, tagRectangle.top, tagRectangle.right, tagRectangle.bottom, tagBackPaint.apply { color = annotationColor }) }

        // tag text
        tagTextPaint.apply {
            color = labelColor
        }
        if (isVertical) {
            canvas.withSave {
                rotate(90f, tagPosition.x, tagPosition.y)
                drawText(tag, tagPosition.x, tagPosition.y, tagTextPaint)
            }
        } else canvas.drawText(tag, tagPosition.x, tagPosition.y, tagTextPaint)
    }

    override fun toString(): String {
        return "'$tag' _${yBase.toInt()} ↕${height.toInt()} ${x1.toInt()}${if (isLeftTerminal) "|" else ""}${if (isBackwards) "🡄" else "🡆"}${if (isRightTerminal) "|" else ""}${x2.toInt()}" //← → ⇽ ⇾ ⏐ ▴ ▾ ▶ ◀ ➔ ➽ ⟵ ⟶ ⥼ ⥽ ⬅ ⮕ 🡄🡅 🡆🡇🠈🠊🠉🠋 🢀🢂🡪🡲🡺🢀↕
    }

    companion object {

        const val ARROW_TIP_WIDTH = 15F
        const val ARROW_TIP_HEIGHT = 15F
        const val ARROW_START_DIAMETER = 10F

        const val EDGE_STROKE_WIDTH = 3F

        val DEFAULT_LABEL_TYPEFACE: Typeface = Typeface.SANS_SERIF
        const val LABEL_BOTTOM_INSET = 10F
        const val LABEL_INFLATE = 1F

        var tagTypeFace = DEFAULT_LABEL_TYPEFACE

        val tagBackPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        val tagTextPaint = Paint().apply {
            typeface = tagTypeFace
            color = labelColor
            textSize = AnnotationManager.EDGE_TAG_TEXT_SIZE
            isAntiAlias = true
        }

        val edgePaint = Paint().apply {
            color = edgeColor
            style = Paint.Style.STROKE
            strokeWidth = EDGE_STROKE_WIDTH
            pathEffect = EDGE_STROKE
            isAntiAlias = true
        }

        val arrowPaint = Paint().apply {
            color = edgeColor
            isAntiAlias = true
        }

        /**
         * Compute tag
         *
         * @param tag edge tag
         * @param isVertical
         * @param x1 from
         * @param x1Anchor from anchor, offset from x1
         * @param x2 to
         * @param x2Anchor to anchor, offset from x2
         * @param yBase edge base
         * @param tagPaint paint
         * @return tag position, width, rectangle
         */
        fun computeTag(tag: String, isVertical: Boolean, x1: Float, x1Anchor: Float, x2: Float, x2Anchor: Float, yBase: Float, tagPaint: Paint): Triple<PointF, Float, RectF> {
            if (isVertical) {

                // tag width
                val tagFontHeight = tagPaint.fontMetrics.height()
                val tagFontDescent = tagPaint.fontMetrics.descent

                // tag position
                val xa1 = x1 + x1Anchor
                val xa2 = x2 + x2Anchor
                val labelLeft = xa1 + (xa2 - xa1 - tagFontHeight) / 2 + tagFontDescent
                val labelBase = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE
                val tagPosition = PointF(labelLeft, labelBase)

                // tag rectangle
                val x = tagPosition.x - tagFontDescent - LABEL_INFLATE
                val y = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - LABEL_INFLATE
                val h = tagPaint.measureText(tag) + 2 * LABEL_INFLATE
                val w = tagFontHeight + 2 * LABEL_INFLATE
                val tagRectangle = RectF(x, y, x + w, y + h)

                return Triple(tagPosition, tagFontHeight, tagRectangle)
            } else {

                // tag width
                val tagWidth = tagPaint.measureText(tag)

                // tag position
                val xa1 = x1 + x1Anchor
                val xa2 = x2 + x2Anchor
                val labelLeft = xa1 + (xa2 - xa1 - tagWidth) / 2
                val fontDescent: Float = tagPaint.fontMetrics.descent
                val labelBase: Float = yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - fontDescent
                val tagPosition = PointF(labelLeft, labelBase)

                // tag rectangle
                val tagFontHeight: Float = tagPaint.fontMetrics.height()
                val x = tagPosition.x - LABEL_INFLATE
                val y = (yBase - LABEL_BOTTOM_INSET - LABEL_INFLATE - tagFontHeight - LABEL_INFLATE)
                val w = tagWidth + 2 * LABEL_INFLATE
                val h = tagFontHeight + 2 * LABEL_INFLATE
                val tagRectangle = RectF(x, y, x + w, y + h)

                return Triple(tagPosition, tagWidth, tagRectangle)
            }
        }

        /**
         * Make edge
         *
         * @param fromX           x from
         * @param toX             x to
         * @param baseY           base y
         * @param fromAnchorX     x anchor from
         * @param toAnchorX       x anchor to
         * @param yAnchor         y anchor
         * @param height          height
         * @param bottom          pad bottom
         * @param label           label
         * @param isBackwards     whether edge is backwards
         * @param isLeftTerminal  whether this edge left-terminates
         * @param isRightTerminal whether this edge right-terminates
         * @param isVisible       whether this edge is visible
         * @param color           color
         */
        fun makeEdge(
            fromX: Float,
            toX: Float,
            baseY: Float,
            fromAnchorX: Float,
            toAnchorX: Float,
            yAnchor: Float,
            height: Float,
            bottom: Float,
            label: String?,
            isBackwards: Boolean,
            isLeftTerminal: Boolean,
            isRightTerminal: Boolean,
            isVisible: Boolean,
            color: Int,
            tagPaint: Paint,

            ): Edge {

            // tag
            val maxWidth = toX + toAnchorX - (fromX + fromAnchorX)
            val (tag, isVertical) = processLabel(label, maxWidth, tagPaint)
            val (tagPosition, tagWidth, tagRectangle) = computeTag(tag, isVertical, fromX, fromAnchorX, toX, toAnchorX, baseY, tagPaint)

            // edge
            val edge = Edge(
                fromX,
                toX,
                baseY,
                fromAnchorX,
                toAnchorX,
                yAnchor,
                height,
                bottom,
                tag,
                tagPosition,
                tagWidth,
                tagRectangle,
                isVertical = isVertical,
                isBackwards = isBackwards,
                isLeftTerminal = isLeftTerminal,
                isRightTerminal = isRightTerminal,
                isVisible = isVisible,
                color,
            )
            return edge
        }
    }
}