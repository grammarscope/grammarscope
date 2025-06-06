package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.Token
import org.depparse.common.BaseSpanner
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

class PS(sentenceIdx: Int, predicate: Token, subject: Token, label: String) : PT(sentenceIdx, predicate, subject, label) {

    override fun toStringBuilder(sb: SpannableStringBuilder, index: Int, vararg factories: SpanFactory) {
        super.toStringBuilder(sb, index, *factories)
        sb
            .append(' ')
            .append('(')
            .append(' ')
            .append("subject:")
            .append(label)
            .append(':')
            .append(' ')
            .append(term.word, factories[1])
        if (FULL) {
            sb
                .append(' ')
                .append('(')
                .append(term.start.toString())
                .append('-')
                .append(term.end.toString())
                .append(')')
                .append(' ')
        }
        sb.append(' ')
            .append(')')
    }

    override fun toShortString(): String {
        return "${predicate.word}(s=${term.word})"
    }

    override fun toString(): String {
        return super.toString() + " (${term.word})"
    }
}
