package org.grammarscope.semantics

import org.depparse.Token

open class PT(sentenceIdx: Int, predicate: Token, override val term: Token, label: String) : Relation(sentenceIdx, predicate, label) {

    open fun toShortString(): String {
        return "${predicate.word}(${term.word})"
    }
}
