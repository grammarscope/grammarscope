package org.grammarscope.graph

import android.util.Log
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.common.BaseParse
import java.util.function.Consumer

class DependencyGraphParse(consumer: Consumer<SentenceGraph<Token, Token>?>, private val reverse: Boolean = false) : BaseParse<SentenceGraph<Token, Token>>(consumer) {

    override fun toR(sentences: Array<Sentence>?): SentenceGraph<Token, Token>? {
        return sentences?.let {
            val graph = SentenceGraph<Token, Token>(sentences)
            for (sentence in sentences) {
                for (token in sentence.tokens) {
                    if (!graph.mutableNetwork.nodes().contains(token)) {
                        graph.mutableNetwork.addNode(token)
                    }
                    if (token.head != -1 && token.head < sentence.tokens.size) {
                        val token2 = sentence.tokens[token.head] // 0-based head index
                        if (!graph.mutableNetwork.nodes().contains(token2)) {
                            graph.mutableNetwork.addNode(token2)
                        }
                        // if nodeU and nodeV are not already present in this graph, this method will silently add nodeU and nodeV to the graph.
                        // however insertion order of vertices will be disrupted;
                        if (reverse)
                            graph.mutableNetwork.addEdge(token2, token, token)
                        else
                            graph.mutableNetwork.addEdge(token, token2, token)
                    }
                }
            }
            graph.apply { initFunctions() }
            Log.d(TAG, graph.toString())
            graph
        }
    }

    companion object {

        private const val TAG = "DependencyGraphParse"
    }
}
