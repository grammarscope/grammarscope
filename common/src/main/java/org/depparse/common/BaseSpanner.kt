package org.depparse.common

import android.text.SpannableStringBuilder
import android.text.Spanned

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
object BaseSpanner {

    /**
     * Append text
     *
     * @param text      text
     * @param factories span factories
     */
    fun SpannableStringBuilder.append(text: CharSequence?, vararg factories: SpanFactory) : SpannableStringBuilder {
        if (text.isNullOrEmpty()) {
            return this
        }
        val from = this.length
        this.append(text)
        val to = this.length
        for (spanFactory in factories) {
            val spans = spanFactory.makeSpans()
            if (spans is Array<*>) {
                for (span in spans) {
                    this.setSpan(span, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            } else {
                this.setSpan(spans, from, to, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
        }
        return this
    }

    // I N T E R F A C E S

    /**
     * Span factory
     *
     * @author [Bernard Bou](mailto:1313ou@gmail.com)
     */
    fun interface SpanFactory {

        fun makeSpans(): Any?
    }
}
