package org.depparse.common

import android.text.SpannableStringBuilder
import androidx.core.text.BidiFormatter
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.Token.BreakLevel
import org.depparse.Token.TokenEnhancedDepsProcessor
import org.depparse.Token.TokenTagProcessor
import org.depparse.common.BaseSpanner.SpanFactory
import org.depparse.common.BaseSpanner.append
import java.io.IOException

object Decorator {

    /**
     * Sentences to string builder
     *
     * @param sentences sentences
     * @param factories span factories: [0] text, [1] dependent, [2] head, [3] label, [4] root_label
     * @return spannable string builder
     */
    fun toStyledCharSequence(sentences: Array<Sentence>, vararg factories: SpanFactory): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        for (sentence in sentences) {
            sb.append(sentence, *factories)
        }
        return sb
    }

    /**
     * Sentence to string builder
     *
     * @param sentence  sentence
     * @param factories span factories: [0] text, [1] dependent, [2] head, [3] label, [4] root_label
     */
    fun SpannableStringBuilder.append(sentence: Sentence, vararg factories: SpanFactory): SpannableStringBuilder {
        this
            .append(sentence.text, factories[0])
            .append(sentence.docid)
            .append('\n')
            .append('\n')
        for (i in sentence.tokens.indices) {
            this
                .append(i, sentence.tokens, factories[1], factories[2], factories[3], factories[4], factories[5]) // [0] dependent, [1] head, [2] label, [3] root_label, [4] enhanced_label
                .append("\n")
        }
        return this
    }

    /**
     * Tokens to string builder
     *
     * @param which     which token
     * @param tokens    set of all tokens
     * @param factories span factories: [0] dependent, [1] head, [2] label, [3] root_label, [4] enhanced_label
     */
    fun SpannableStringBuilder.append(which: Int, tokens: Array<Token>, vararg factories: SpanFactory): SpannableStringBuilder {
        try {
            val token = tokens[which]
            val hasDeps = token.deps != null && token.deps!!.isNotEmpty()
            this
                .append('[')
                .append(token.index.toString())
                .append(']')
                .append(' ')
                .append(directional(token.word), factories[0]) // dependent

            // segment
            this
                .append(" (")
                .append(token.start.toString())
                .append('-')
                .append(token.end.toString())
                .append(')')
                .append(' ')

            // head (governor) and label (relation)
            if (hasDeps)
                this.append("\n- ")
            if (token.head != -1) {
                val headWord =  /* token.head >= tokens.length ? "undefined" : */tokens[token.head].word // 0-based head index
                this
                    .append(token.label, factories[2]) // label
                    .append(" to ")
                    .append(directional(headWord), factories[1]) // head
                    .append(" [")
                    .append(token.head.toString())
                    .append(']')
            } else {
                this.append(token.label, factories[3]) // root label
            }
            this.append('\n')

            // enhanced deps
            if (hasDeps)
                this
                    .append('-')
                    .append(' ')
                    .append(enhanced(token.deps!!, tokens, factories[4], factories[1]))
                    .append('\n')

            // tag
            this.append(TokenTagProcessor.toString(token.tag))

            // category
            if (token.category.isNotEmpty()) {
                this
                    .append("category")
                    .append(" = ")
                    .append(token.category)
                    .append('\n')
            }

            // break level
            this
                .append("break level")
                .append(" = ")
                .append(BreakLevel.toString(token.breakLevel))
                .append('\n')

        } catch (_: IOException) {
            this
                .append("<error")
                .append('\n')
        }
        return this
    }

    val bidiFormatter: BidiFormatter = BidiFormatter.getInstance(false)

    fun directional(token: String): String {
        return bidiFormatter.unicodeWrap(token)
    }

    fun enhanced(deps: String, tokens: Array<Token>, vararg factories: SpanFactory): CharSequence {
        val enhancedDeps = TokenEnhancedDepsProcessor.parse(deps)
        val sb = SpannableStringBuilder()
        for ((label, head) in enhancedDeps) {
            val headWord = if (head == -1 || head >= tokens.size) "?" else tokens[head].word // 0-based head index
            sb
                .append(label, factories[0])
                .append(" to ")
                .append(headWord, factories[1])
                .append(' ')
                .append('[')
                .append(head.toString())
                .append(']')
        }
        return sb
    }
}