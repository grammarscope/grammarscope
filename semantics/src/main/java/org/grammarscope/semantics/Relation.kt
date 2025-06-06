package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.Label
import org.depparse.Token
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

abstract class Relation protected constructor(
    @Suppress("unused") protected val sentenceIdx: Int,
    val predicate: Token,
    val label: String
) : Label {

    abstract val term: Token

    open fun toStringBuilder(sb: SpannableStringBuilder, index: Int, vararg factories: SpanFactory) {
        if (FULL) {
            sb
                .append('P')
                .append(' ')
        }
        sb.append(predicate.word, factories[0])
        if (FULL) {
            sb
                .append(' ')
                .append('(')
                .append(predicate.start.toString())
                .append('-')
                .append(predicate.end.toString())
                .append(')')
                .append(' ')
        }
    }

    override fun label(): String {
        return label
    }

    override fun toString(): String {
        return "${predicate.word} {$label}"
    }

    companion object {

        const val FULL = false
    }
}
