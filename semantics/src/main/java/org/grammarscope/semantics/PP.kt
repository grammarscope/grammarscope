package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.Token
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

class PP(sentenceIdx: Int, predicate: Token, override val term: Token, label: String) : Relation(sentenceIdx, predicate, label) {

    override fun toStringBuilder(sb: SpannableStringBuilder, index: Int, vararg factories: SpanFactory) {
        super.toStringBuilder(sb, index, *factories)
        sb
            .append(' ')
            .append('(')
            .append(' ')
            .append("predicate:")
            .append(label)
            .append(':')
            .append(' ')
            .append(term.word, factories[4])
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
        return "(${predicate.word}~${predicate.word})"
    }

    override fun toString(): String {
        return super.toString() + " (${term.word})"
    }
}
