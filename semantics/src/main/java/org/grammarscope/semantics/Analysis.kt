package org.grammarscope.semantics

import android.text.SpannableStringBuilder
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append

class Analysis(private val text: String) : ArrayList<Relation>() {

    /**
     * To string builder
     *
     * @param sb string builder
     * @param factories span factories: [0] text, [1] predicate, [2] subject, [3] object, [4] term, [5] predicate2
     */
    fun toStringBuilder(sb: SpannableStringBuilder, vararg factories: SpanFactory) {
        sb
            .append(text, factories[0])
            .append('\n')
            .append('\n')
        for ((index, relation) in this.withIndex()) {
            sb
                .append('[')
                .append(index.toString())
                .append(']')
                .append(' ')
            relation.toStringBuilder(sb, index, factories[1], factories[2], factories[3], factories[4], factories[5])
            sb.append("\n")
        }
    }

    companion object {

        /**
         * To string builder
         *
         * @param analyses analyses
         * @param factories span factories: [0] text, [1] predicate, [2] subject, [3] object, [4] term, [5] predicate2
         * @return spannable string builder
         */
        fun toStringBuilder(analyses: Array<Analysis>, vararg factories: SpanFactory): SpannableStringBuilder {
            val sb = SpannableStringBuilder()
            for (analysis in analyses) {
                analysis.toStringBuilder(sb, *factories)
                sb.append('\n')
            }
            return sb
        }
    }
}
