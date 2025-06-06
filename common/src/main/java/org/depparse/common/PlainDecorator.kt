package org.depparse.common

import android.util.Log
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.Token.BreakLevel
import org.depparse.Token.TokenTagProcessor
import java.io.IOException

object PlainDecorator {

    /**
     * Sentence to string builder
     *
     * @param sentence sentence
     */
    fun Appendable.append(sentence: Sentence): Appendable {
        try {
            this.append(sentence.text)
            this
                .append(sentence.docid)
                .append('\n')
                .append('\n')
            for (i in sentence.tokens.indices) {
                this
                    .append(i, sentence.tokens)
                    .append("\n")
            }
        } catch (e: IOException) {
            Log.e(PlainDecorator::class.java.name, "While appending", e)
        }
        return this
    }

    /**
     * Sentences to string builder
     *
     * @param sentences sentences
     * @return string builder
     */
    fun Appendable.append(sentences: Array<Sentence>?): Appendable {
        if (sentences != null) {
            for (sentence in sentences) {
                this.append(sentence)
            }
        }
        return this
    }

    /**
     * Tokens to string builder
     *
     * @param which  which token
     * @param tokens set of all tokens
     */
    @Throws(IOException::class)
    fun Appendable.append(which: Int, tokens: Array<Token>): Appendable {
        try {
            val token = tokens[which]
            val hasDeps = token.deps != null && token.deps!!.isNotEmpty()

            // index
            this
                .append('[')
                .append(token.index.toString())
                .append(']')
                .append(' ')

            // word
            this.append(token.word)

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
                this.append('\n')
            if (token.head != -1) {
                val headWord =  /* token.head >= tokens.length ? "undefined" : */tokens[token.head].word // 0-based head index
                this
                    .append(token.label)
                    .append(" to ")
                    .append(headWord)
                    .append(" [")
                    .append(token.head.toString())
                    .append(']')
            } else {
                this.append(token.label)
            }
            this.append('\n')

            // enhanced deps
            if (hasDeps)
                this
                    .append(token.deps.toString())
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
}
