/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.FontMetrics
import android.graphics.Rect
import android.graphics.RectF
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import org.depparse.Segment
import org.grammarscope.annotations.annotation.Annotation
import org.grammarscope.annotations.annotation.Annotation.BoxAnnotation
import org.grammarscope.annotations.annotation.Annotation.EdgeAnnotation
import org.grammarscope.annotations.annotation.Annotation.LabelAnnotation
import org.grammarscope.annotations.annotation.AnnotationType
import org.grammarscope.annotations.paint.AnnotationPainter
import org.grammarscope.annotations.paint.InterlacedHatchPaintBuilder
import kotlin.math.max

class AnnotatedTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    /**
     * Annotations
     */
    internal var annotations: Map<AnnotationType, Collection<Annotation>>? = null

    private val stripeColor = context.resources.getColor(R.color.stripes, context.theme)

    private val hatchPaint = InterlacedHatchPaintBuilder().build(
        angle = 45f,
        spacing = 120f,
        strokeWidth = 60f,
        color = stripeColor,
    )

    /**
     * MutableLiveData for layout change event
     */
    private val _changed = MutableLiveData<Unit>()

    /**
     * LiveData for layout change event
     */
    val changed: LiveData<Unit> get() = _changed

    /**
     * Set text size as firing layout change event
     *
     * @param size size
     */
    override fun setTextSize(size: Float) {
        super.setTextSize(size)
        Log.d(TAG, "Set text size: $size")
        _changed.postValue(Unit)
    }

    /**
     * Set line spacing as firing layout change event
     *
     * @param add add
     * @param mult multiplier
     */
    override fun setLineSpacing(add: Float, mult: Float) {
        super.setLineSpacing(add, mult)
        Log.d(TAG, "Set line spacing: $add, $mult")
        _changed.postValue(Unit)
    }

    /**
     * Set letter spacing as firing layout change event
     *
     * @param letterSpacing letter spacing
     */
    override fun setLetterSpacing(letterSpacing: Float) {
        super.setLetterSpacing(letterSpacing)
        Log.d(TAG, "Set letter spacing: $letterSpacing")
        _changed.postValue(Unit)
    }

    /**
     * On layout  as firing layout change event
     *
     * @param changed changed
     * @param left left
     * @param top top
     * @param right right
     * @param bottom bottom
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            Log.d(TAG, "Layout changed")
            _changed.postValue(Unit)
        }
    }

    override fun onDraw(canvas: Canvas) {

        // Draw text
        super.onDraw(canvas)

        if (text != "") {
            dumpLineText()
            drawAnnotationSpace(canvas)
            if (DEBUG)
                drawLineSpace(canvas)

            // Draw all annotations
            // for (annotation in annotations) {
            //     annotation.draw(canvas, this)
            // }
            if (annotations != null && annotations!!.isNotEmpty()) {
                val labelAnnotations = annotations!![AnnotationType.LABEL]!!.map { it as LabelAnnotation }
                AnnotationPainter.paintLabels(canvas, labelAnnotations)

                val boxAnnotations = annotations!![AnnotationType.BOX]!!.map { it as BoxAnnotation }
                AnnotationPainter.paintBoxes(canvas, boxAnnotations)

                val edgeAnnotations = annotations!![AnnotationType.EDGE]!!.map { it as EdgeAnnotation }
                AnnotationPainter.paintEdges(canvas, edgeAnnotations, padWidth = width.toFloat())
            }
        }
    }

    private fun drawAnnotationSpace(canvas: Canvas) {
        val paint: Paint = this.paint
        val fontMetrics = paint.fontMetrics
        val ascent = fontMetrics.ascent
        val descent = fontMetrics.descent
        val leading = fontMetrics.leading
        val height = -ascent + descent + leading

        val lineCount = layout.lineCount
        for (line in 0 until lineCount) {
            // Get the top and the bottom of the line.
            val top = layout.getLineTop(line).toFloat() + paddingTop
            val bottom = layout.getLineBottom(line).toFloat() + paddingTop
            val base = layout.getLineBaseline(line).toFloat() + paddingTop
            val lineAscent = layout.getLineAscent(line).toFloat()
            val lineDescent = layout.getLineDescent(line).toFloat()
            val y1 = base + descent
            val y2 = y1 + lineSpacingExtra //bottom

            // Print the positions.
            Log.d(TAG, "Line $line: Top = $top, Bottom = $bottom, Base = $base, Height = $height, Ascent= $ascent/$lineAscent, Descent = $descent/$lineDescent, Leading = $leading")

            // Paint rect
            val left: Float = layout.getLineLeft(line) + paddingLeft
            val right: Float = layout.getLineRight(line) + paddingLeft
            val rect = RectF(left, y1, right, y2)
            canvas.drawRect(rect, hatchPaint)
        }
    }

    private fun drawLineSpace(canvas: Canvas) {
        val paintTop = Paint().apply {
            color = Color.MAGENTA
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paintBottom = Paint().apply {
            color = Color.CYAN
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        val paint: Paint = this.paint
        val fontMetrics = paint.fontMetrics
        val ascent = fontMetrics.ascent
        val descent = fontMetrics.descent
        val leading = fontMetrics.leading
        val height = -ascent + descent + leading

        val lineCount = layout.lineCount
        for (line in 0 until lineCount) {
            // Get the top and the bottom of the line.
            val top = layout.getLineTop(line) + paddingTop.toFloat()
            val bottom = layout.getLineBottom(line) + paddingTop.toFloat()
            val base = layout.getLineBaseline(line) + paddingTop.toFloat()
            val lineAscent = layout.getLineAscent(line)
            val lineDescent = layout.getLineDescent(line)

            // Print the positions.
            Log.d(TAG, "Line $line: Top = $top, Bottom = $bottom, Base = $base, Height = $height, Ascent= $ascent/$lineAscent, Descent = $descent/$lineDescent, Leading = $leading")

            // Paint lines.
            val x1 = 0f
            val x2 = width.toFloat() / 2f
            val x3 = width.toFloat()

            //val paintBase = Paint().apply {
            //    color = Color.BLUE
            //    strokeWidth = 2f
            //    style = Paint.Style.STROKE
            //}
            //val paintAsDesCent = Paint().apply {
            //    color = Color.GREEN
            //    strokeWidth = 2f
            //    style = Paint.Style.STROKE
            //}
            //// base
            //canvas.drawLine(x1, base, x2, base, paintBase)
            //// top text
            //canvas.drawLine(x1, base + ascent, x2, base + ascent, paintAsDesCent)
            //// bottom text
            //canvas.drawLine(x1, base + descent, x2, base + descent, paintAsDesCent)

            // top
            canvas.drawLine(x1, top, x2, top, paintTop)
            // bottom
            canvas.drawLine(x2, bottom, x3, bottom, paintBottom)
        }
    }

    private fun dumpLineText() {
        val lineCount = layout.lineCount
        for (line in 0 until lineCount) {
            val lineStart: Int = layout.getLineStart(line)
            val lineEnd: Int = layout.getLineEnd(line)
            val lineText: CharSequence = text.subSequence(lineStart, lineEnd)
            Log.d(TAG, "[$line]- $lineText")
        }
    }

    companion object {

        const val TAG = "AnnotatedTextView"

        const val DEBUG = false

        /**
         * Highlight a specific word changing its background
         */
        fun TextView.backHighlightWord(wordStart: Int, wordEnd: Int, color: Int = Color.YELLOW) {
            val span = BackgroundColorSpan(color)
            applySpan(wordStart, wordEnd, span)
        }

        /**
         * Highlight a specific word changing its foreground
         */
        fun TextView.foreHighlightWord(wordStart: Int, wordEnd: Int, color: Int = Color.RED) {
            val span = ForegroundColorSpan(color)
            applySpan(wordStart, wordEnd, span)
        }

        /**
         * Apply a span to a specific word
         */
        fun TextView.applySpan(wordStart: Int, wordEnd: Int, span: Any) {
            val spannable = if (text is SpannableString) {
                text as SpannableString
            } else {
                SpannableString(text)
            }
            spannable.removeSpan(span)
            spannable.setSpan(
                span,
                wordStart,
                wordEnd,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannable
        }

        /**
         * Line bounds
         */
        fun TextView.lineBounds(): List<Rect> {
            return (0..layout.lineCount).map {
                val r = Rect()
                layout.getLineBounds(it, r)
                r
            }.toList()
        }
    }
}

// M O D E L   T O   V I E W   C O N V E R T I O N S
// Equivalent to Swing's modelToView()
// All offsets are expressed in code points (not UTF-16 chars).

/**
 * Get float rectangle for a code point offset in text
 *
 * @param offset target offset in text, in code points
 * @return float rectangle in view coordinates (before any scroll)
 */
fun TextView.modelToViewF(offset: Int): RectF {
    val text = text
    val codePointCount = Character.codePointCount(text, 0, text.length)
    require(offset in 0..codePointCount) { "Invalid position: $offset (code points: $codePointCount)" }
    val layout = checkNotNull(layout) { "Layout not available yet" }

    // code point offset -> UTF-16 index
    val start = Character.offsetByCodePoints(text, 0, offset)
    val end = if (start < text.length) Character.offsetByCodePoints(text, start, 1) else start

    val line = layout.getLineForOffset(start)
    val baseline = layout.getLineBaseline(line)
    val metrics: FontMetrics = paint.fontMetrics
    val top = baseline + metrics.ascent + paddingTop //layout.getLineTop(line)
    val bottom = baseline + metrics.descent + paddingTop // layout.getLineBottom(line)
    val x = layout.getPrimaryHorizontal(offset)
    val width = if (offset < text.length) {
        layout.getPrimaryHorizontal(offset + 1) - x
    } else {
        // Handle the end of the text.
        if (text.isNotEmpty()) {
            // Get the previous character position.
            val previousPos = offset - 1
            val previousX = layout.getPrimaryHorizontal(previousPos)
            x - previousX
        } else {
            // Handle empty text.
            0F
        }
    }
    val left = x + paddingLeft
    val right = left + width
    val metrics = paint.fontMetrics
    val top = baseline + metrics.ascent + totalPaddingTop
    val bottom = baseline + metrics.descent + totalPaddingTop

    val x0 = layout.getPrimaryHorizontal(start)
    val x1 = when {
        end == start -> x0 // end of text: zero-width caret position
        layout.getLineForOffset(end) == line -> layout.getPrimaryHorizontal(end)
        // glyph is last on its line: stay on this line
        layout.getParagraphDirection(line) == Layout.DIR_LEFT_TO_RIGHT -> layout.getLineRight(line)
        else -> layout.getLineLeft(line)
    }

    val left = minOf(x0, x1) + totalPaddingLeft
    val right = maxOf(x0, x1) + totalPaddingLeft
    return RectF(left, top, right, bottom)
}

/**
 * Get int rectangle for a code point offset in text
 *
 * @param offset target offset in text, in code points
 * @return int rectangle (rounded outwards so it fully covers the glyph)
 */
fun TextView.offsetToViewRect(offset: Int): Rect =
    Rect().also { modelToViewF(offset).roundOut(it) }

/**
 * Get float rectangle for a segment in text
 *
 * @param segment target segment, in code points; `second` is the offset of
 *                the last code point of the segment (inclusive, as in the original)
 * @return bounding float rectangle of the segment
 */
fun TextView.segmentToViewRectF(segment: Segment): RectF {
    val from = modelToViewF(segment.first)
    val to = modelToViewF(segment.second)
    // Manual bounding box: RectF.union() silently ignores empty (zero-width) rectangles,
    // which is exactly what modelToViewF returns at the end of the text.
    return RectF(
        minOf(from.left, to.left),
        minOf(from.top, to.top),
        maxOf(from.right, to.right),
        maxOf(from.bottom, to.bottom)
    )
}

/**
 * Get int rectangle for a segment in text
 *
 * @param segment target segment, in code points (see [segmentToViewRectF])
 * @return bounding int rectangle of the segment (rounded outwards)
 */
fun TextView.segmentToViewRect(segment: Segment): Rect =
    Rect().also { segmentToViewRectF(segment).roundOut(it) }
