package org.grammarscope.semantics

import org.depparse.Token

data class Dependency(
    val governor: Token?, // swims in Jack <--nsubj-- swims
    val dependent: Token,
    val idx: Int,
    val sentenceIdx: Int, // jack in Jack <--nsubj-- swims
) {

    val label: String
        get() = dependent.label

    override fun toString(): String {
        return "${governor?.word}--$label-->${dependent.word}"
    }
}
