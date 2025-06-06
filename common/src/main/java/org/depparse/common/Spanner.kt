package org.depparse.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Paint.FontMetricsInt
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ClickableSpan
import android.text.style.DynamicDrawableSpan
import android.text.style.ImageSpan
import android.text.style.ReplacementSpan
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import org.depparse.common.BaseSpanner.SpanFactory

/**
 * Spanner
 *
 *
 * Drawable someDrawable = Spanner.getDrawable(this.context, R.drawable.some_drawable_id);
 * Spanner.appendImage(sb, someDrawable);
 * Spanner.toStyledCharSequence(sb, "text", 0, someFactory);
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
object Spanner {

    private const val TAG = "Spanner"

    /**
     * Collapsed marker
     */
    private const val COLLAPSED_CHAR = '@'

    /**
     * Collapsed marker
     */
    private const val COLLAPSED_STRING = COLLAPSED_CHAR.toString()

    /**
     * Expanded marker
     */
    private const val EXPANDED_STRING = "~"

    /**
     * End of expanded string marker
     */
    private const val END_OF_EXPANDED_STRING = '~'

    // A P P L Y S P A N

    /**
     * Apply spans
     *
     * @param sb    spannable string builder
     * @param from  start
     * @param to    finish
     * @param spans spans to apply
     */
    private fun setSpan(sb: SpannableStringBuilder, from: Int, to: Int, spans: Any?) {
        if (spans != null && from != to) {
            if (spans is Array<*>) {
                for (span in spans) {
                    if (span != null) {
                        sb.setSpan(span, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                }
            } else {
                sb.setSpan(spans, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
    }

    /**
     * Apply spans
     *
     * @param sb        spannable string builder
     * @param from      start
     * @param to        finish
     * @param factories span factories to call to get spans
     */
    fun setSpan(sb: SpannableStringBuilder, from: Int, to: Int, vararg factories: SpanFactory) {
        for (spanFactory in factories) {
            val spans = spanFactory.makeSpans()
            setSpan(sb, from, to, spans)
        }
    }

    // I M A G E

    /**
     * Append spans
     *
     * @param sb    spannable string builder
     * @param spans image span with possible image style span
     */
    private fun appendImageSpans(sb: SpannableStringBuilder, vararg spans: Any) {
        val from = sb.length
        sb.append(COLLAPSED_CHAR)
        val to = sb.length
        for (span in spans) {
            sb.setSpan(span, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }

    /**
     * Append image
     *
     * @param sb       spannable string builder
     * @param drawable drawable to use
     */
    fun appendImage(sb: SpannableStringBuilder, drawable: Drawable) {
        val span: Any = ImageSpan(drawable, DynamicDrawableSpan.ALIGN_BASELINE)
        appendImageSpans(sb, span)
    }

    // C L I C K A B L E

    /**
     * Append clickable image
     *
     * @param sb       spannable string builder
     * @param caption  caption
     * @param listener click listener
     * @param context  context
     */
    fun appendClickableImage(sb: SpannableStringBuilder, caption: CharSequence, collapsedResId: Int, expandedResId: Int, listener: OnClickImage, context: Context) {
        val collapsedDrawable = getDrawable(context, collapsedResId)
        val expandedDrawable = getDrawable(context, expandedResId)
        appendClickableImage(sb, collapsedDrawable, expandedDrawable, caption, listener)
    }

    /**
     * Append clickable image
     *
     * @param sb                spannable string builder
     * @param collapsedDrawable collapse drawable
     * @param expandedDrawable  expand drawable
     * @param caption           caption
     * @param listener          click listener
     */
    private fun appendClickableImage(sb: SpannableStringBuilder, collapsedDrawable: Drawable, expandedDrawable: Drawable, caption: CharSequence, listener: OnClickImage) {
        val span = ImageSpan(collapsedDrawable, DynamicDrawableSpan.ALIGN_BASELINE)
        val span2: ClickableSpan = object : ClickableSpan() {
            @Synchronized
            override fun onClick(view: View) {
                // Log.d(TAG, "Click image");
                val textView = view as TextView
                val sb1 = textView.text as SpannableStringBuilder
                val clickableStart = textView.selectionStart
                val clickableEnd = textView.selectionEnd
                val spans = sb1.getSpans(clickableStart, clickableEnd, ImageSpan::class.java)
                for (span3 in spans) {
                    // get image span
                    val from = sb1.getSpanStart(span3)
                    val to = sb1.getSpanEnd(span3)

                    // remove image span
                    sb1.removeSpan(span3)

                    // text
                    val c = sb1[from]
                    val collapsed = c == COLLAPSED_CHAR
                    sb1.replace(from, to, if (collapsed) EXPANDED_STRING else COLLAPSED_STRING)

                    // set new image span
                    val newImageSpan: Any = ImageSpan(if (collapsed) expandedDrawable else collapsedDrawable, DynamicDrawableSpan.ALIGN_BASELINE)
                    sb1.setSpan(newImageSpan, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // fire click
                    Log.d(TAG, "$from->$to")
                    listener.onClickImage(sb1, to + caption.length + 2, collapsed)
                }
                textView.text = sb1
            }
        }
        appendImageSpans(sb, span, span2)
        sb.append(' ').append(caption).append('\n')
    }

    /**
     * Insert tag
     *
     * @param sb       spannable string builder
     * @param position insert position
     * @param tag      tag
     */
    fun insertTag(sb: SpannableStringBuilder, position: Int, tag: CharSequence) {
        val insert = """
             $tag
             $END_OF_EXPANDED_STRING
             """.trimIndent()
        sb.insert(position, insert)
        val mark = position + tag.length + 1
        sb.setSpan(HiddenSpan(), mark, mark + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /**
     * Collapse
     *
     * @param sb       spannable string builder
     * @param position position to start from (to end-of-expanded string)
     */
    fun collapse(sb: SpannableStringBuilder, position: Int) {
        sb.delete(position, find(sb, position, END_OF_EXPANDED_STRING))
    }

    // H E L P E R S

    /**
     * Find delimiter
     *
     * @param sb        spannable string builder
     * @param start     search start
     * @param delimiter delimiter
     * @return delimiter position or -1 if not found
     */
    private fun find(sb: SpannableStringBuilder, start: Int, @Suppress("SameParameterValue") delimiter: Char): Int {
        var i = start
        while (i < sb.length) {
            if (sb[i] == delimiter) {
                return i + 1
            }
            i++
        }
        return -1
    }

    /**
     * Get drawable from resource id
     *
     * @param context context
     * @param resId   resource id
     * @return drawable
     */
    private fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable {
        val drawable = AppCompatResources.getDrawable(context, resId)!!
        drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        return drawable
    }

    // I N T E R F A C E S

    /**
     * Click image interface
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    interface OnClickImage {

        fun onClickImage(sb: SpannableStringBuilder?, position: Int, collapsed: Boolean)
    }

    // H I D D E N S P A N

    /**
     * Hidden span
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    class HiddenSpan
    /**
     * Constructor
     */
        : ReplacementSpan() {

        override fun draw(canvas: Canvas, arg1: CharSequence, arg2: Int, arg3: Int, arg4: Float, arg5: Int, arg6: Int, arg7: Int, arg8: Paint) {
            // do nothing
        }

        override fun getSize(paint: Paint, text: CharSequence, from: Int, to: Int, fm: FontMetricsInt?): Int {
            return 0
        }
    }

    /**
     * Hidden span factory
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    class HiddenSpanFactory : SpanFactory {

        override fun makeSpans(): Any {
            return arrayOf<Any>(HiddenSpan())
        }
    }
}
