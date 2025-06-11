package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.Token
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

class PO(sentenceIdx: Int, predicate: Token, `object`: Token, label: String) : PT(sentenceIdx, predicate, `object`, label) {

    override fun toStringBuilder(sb: SpannableStringBuilder, index: Int, vararg factories: SpanFactory) {
        super.toStringBuilder(sb, index, *factories)
        sb
            .append(' ')
            .append('(')
            .append(' ')
            .append("object:")
            .append(label)
            .append(':')
            .append(' ')
            .append(term.word, factories[2])
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
        return "${predicate.word}(o=${term.word})"
    }

    override fun toString(): String {
        return super.toString() + " (${term.word})"
    }
}
