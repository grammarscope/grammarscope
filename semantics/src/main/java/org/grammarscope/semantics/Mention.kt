package org.grammarscope.semantics

import org.depparse.Token

internal class Mention {
    internal class Chain {
        @Suppress("unused")
        var representative: Mention? = null
    }

    @Suppress("unused")
    var isRepresentative = false
    @Suppress("unused")
    var sentenceIdx = 0
    var head: Token? = null
    @Suppress("unused")
    var chain: Chain? = null
}
