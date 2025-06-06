package org.grammarscope.semantics

import org.depparse.Token

data class Instance(val sentenceIndex: Int, val token: Token) {

    override fun equals(other: Any?): Boolean {
        return if (other !is Instance) {
            false
        } else sentenceIndex == other.sentenceIndex && token == other.token
    }

    override fun hashCode(): Int {
        val hash1 = sentenceIndex.hashCode()
        val hash2 = token.hashCode()
        return hash1 * 31 + hash2 * 19
    }

    override fun toString(): String {
        return "${token.head}-$sentenceIndex-$token"
    }
}
