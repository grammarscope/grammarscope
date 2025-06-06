package org.grammarscope.annotations.document

import android.util.Log
import org.depparse.Segment
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.Token.TokenEnhancedDepsProcessor
import org.depparse.common.BaseParse
import java.util.function.Consumer

class AnnotationParse(consumer: Consumer<Document<Token>?>) : BaseParse<Document<Token>>(consumer) {

    override fun toR(sentences: Array<Sentence>?): Document<Token>? {
        return sentences?.let {
            val doc = ParsedDocument(sentences, inputs.map { it.first }.toTypedArray())
            doc.dump()
            return doc
        }
    }
}

data class TokenEdge(
    override val source: Token,
    override val target: Token,
    override val label: String,
    override val ith: Int,
    val enhanced: Boolean = false
) : GraphEdge<Token> {
    override val lowIndex: Int = minOf(source.index, target.index)
    override val highIndex: Int = maxOf(source.index, target.index)
}

class ParsedGraph(val sentence: Sentence) : Graph<Token> {

    override val nodes: Collection<Token> = sentence.tokens.toList()

    override val edges: MutableCollection<GraphEdge<Token>> = ArrayList()

    val reverse: Boolean = false

    fun make() {
        for (token in sentence.tokens) {

            // basic dependencies
            if (token.head != -1 && token.head < sentence.tokens.size) {
                val headToken = sentence.tokens[token.head]
                val edge = if (reverse)
                    TokenEdge(headToken, token, token.label, token.index)
                else
                    TokenEdge(token, headToken, token.label, token.index)
                edges.add(edge)
            }

            // enhanced dependencies
            if (token.deps != null && token.deps!!.isNotEmpty()) {
                val enhancedDeps = TokenEnhancedDepsProcessor.parse(token.deps!!) // list of (label, head) pairs
                for ((label, head) in enhancedDeps) {
                    val headToken = sentence.tokens[head]
                    val edge = if (reverse)
                        TokenEdge(headToken, token, label, token.index, true)
                    else
                        TokenEdge(token, headToken, label, token.index, true)
                    edges.add(edge)
                }
            }
        }
    }
}

class ParsedDocument(val sentences: Array<Sentence>, val sentenceStarts: Array<Int>) : Document<Token> {

    val graphs: Array<Graph<Token>> = Array(sentences.size) {
        ParsedGraph(sentences[it]).apply { make() }
    }

    override fun getGraph(sentenceIdx: Int): Graph<Token> {
        return graphs[sentenceIdx]
    }

    override val sentenceCount: Int
        get() = sentences.size

    override fun getTextSegment(sentenceIdx: Int, segment: Segment): Segment {
        val start = sentenceStarts[sentenceIdx]
        return start + segment.first to start + segment.second
    }

    override val wordSegments: List<Segment> by lazy {
        (0..<sentenceCount).flatMap { sentenceIdx -> sentences[sentenceIdx].tokens.map { getTextSegment(sentenceIdx, it.segment) } }.toList()
    }

    fun dump() {
        val text: String = sentences.joinToString("\n") { it.text }
        for (sentenceIdx in 0..<sentenceCount) {
            val sentence = sentences[sentenceIdx]
            Log.d("Sentence", "Sentence[$sentenceIdx] '${sentence.text}' ${sentence.start}-${sentence.end} ${sentence.segment.first}-${sentence.segment.second} '${text.substring(sentence.segment.first, sentence.segment.second + 1)}'")
            for (token in sentence.tokens) {
                val textSegment = getTextSegment(sentenceIdx, token.segment)
                val tokenSegment = token.segment
                Log.d("Token", "Token [$sentenceIdx] ${textSegment.first}-${textSegment.second} '${text.substring(textSegment.first, textSegment.second + 1)}' / '${sentence.text.substring(tokenSegment.first, tokenSegment.second + 1)}'")
            }
        }
    }
}