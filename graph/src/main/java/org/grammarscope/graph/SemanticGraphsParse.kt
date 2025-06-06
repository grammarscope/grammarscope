package org.grammarscope.graph

import android.util.Log
import org.depparse.Sentence
import org.depparse.Token
import org.depparse.common.BaseParse
import org.grammarscope.semantics.Relation
import org.grammarscope.semantics.SemanticAnalyzer
import java.util.function.Consumer

class SemanticGraphsParse(consumer: Consumer<List<SentenceGraph<Token, Relation>>?>, private val reverse: Boolean = false) : BaseParse<List<SentenceGraph<Token, Relation>>?>(consumer) {

    override fun toR(sentences: Array<Sentence>?): MutableList<SentenceGraph<Token, Relation>>? {
        return sentences?.let {
            val analyzer = SemanticAnalyzer()
            val graphs: MutableList<SentenceGraph<Token, Relation>> = ArrayList()
            for (sentence in sentences) {
                val analyses = analyzer.analyze(sentence)
                val graph = SentenceGraph<Token, Relation>(sentences)
                for (analysis in analyses) {
                    for (relation in analysis) {
                        val predicate = relation.predicate
                        val term = relation.term
                        if (!graph.mutableNetwork.nodes().contains(predicate)) {
                            graph.mutableNetwork.addNode(predicate)
                        }
                        if (!graph.mutableNetwork.nodes().contains(term)) {
                            graph.mutableNetwork.addNode(term)
                        }
                        // if nodeU and nodeV are not already present in this graph, this method will silently add nodeU and nodeV to the graph.
                        // however insertion order of vertices will be disrupted.

                        if (reverse)
                            graph.mutableNetwork.addEdge(predicate, term, relation)
                        else
                            graph.mutableNetwork.addEdge(term, predicate, relation)
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

        private const val TAG = "SemanticGraphsParse"
    }
}
