package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.Token
import org.depparse.common.BaseSpanner
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

class TP(sentenceIdx: Int, override val term: Token, predicate: Token, label: String) : Relation(sentenceIdx, predicate, label) {

    override fun toStringBuilder(sb: SpannableStringBuilder, index: Int, vararg factories: SpanFactory) {
        super.toStringBuilder(sb, index, *factories)
        sb
            .append(' ')
            .append('(')
            .append(' ')
            .append("term:")
            .append(label)
            .append(':')
            .append(' ')
            .append(term.word, factories[3])
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

    fun toShortString(): String {
        return "${predicate.word}(${term.word})"
    }

    override fun toString(): String {
        return super.toString() + " (${term.word})"
    }
}
