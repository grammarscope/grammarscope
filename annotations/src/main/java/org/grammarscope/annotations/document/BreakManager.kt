/*
 * Copyright (c) 2026. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.document

import android.os.Build
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.LineBreakConfigSpan
import org.depparse.Token
import org.grammarscope.annotations.paint.TokenSpan

object BreakManager {

    fun enforceNoBreaks(
        text: String,
        tokens: Array<Token>
    ): CharSequence {
        val sb = SpannableStringBuilder(text)
        tokens.forEach { token ->
            if (token.end > token.start)
                applyNoBreakSpan(sb, token.start, token.end + 1, token)
        }
        return sb
    }

    private fun applyNoBreakSpan(
        text: Spannable,
        start: Int,
        end: Int, // exclusive
        token: Token
    ) {
        val span: Any =
            if (Build.VERSION.SDK_INT >= 35) {
                LineBreakConfigSpan.createNoBreakSpan()
            } else {
                TokenSpan("root" == token.label)
            }

        text.setSpan(
            span,
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
}
