package org.grammarscope.graph

import android.util.Log
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.common.BaseParse
import java.util.function.Consumer

class DependencyGraphsParse(consumer: Consumer<List<SentenceGraph<Token, Token>>?>, private val reverse: Boolean = false) : BaseParse<List<SentenceGraph<Token, Token>>?>(consumer) {

    override fun toR(sentences: Array<Sentence>?): List<SentenceGraph<Token, Token>>? {
        return sentences?.let {
            val graphs: MutableList<SentenceGraph<Token, Token>> = ArrayList()
            for (sentence in sentences) {
                val graph = SentenceGraph<Token, Token>(sentences)
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
                        // however insertion order of vertices will be disrupted.
                        if (reverse)
                            graph.mutableNetwork.addEdge(token2, token, token)
                        else
                            graph.mutableNetwork.addEdge(token, token2, token)
                    }
                }
                graph.apply { initFunctions() }
                graphs.add(graph)
                Log.d(TAG, graph.toString())
            }
            graphs
        }
    }

    companion object {

        private const val TAG = "DependencyGraphsParse"
    }
}
