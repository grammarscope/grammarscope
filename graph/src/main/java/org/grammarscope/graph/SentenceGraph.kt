package org.grammarscope.graph

import androidx.annotation.CallSuper
import edu.uci.ics.jung.graph.Network
import edu.uci.ics.jung.graph.incidentNodesOf
import edu.uci.ics.jung.nonindexed.AugmentedGraphNonIndexed
import edu.uci.ics.jung.visualization.renderers.edge.functions.DefaultParallelEdgeIndexFunction
import edu.uci.ics.jung.visualization.renderers.edge.functions.nonindexed.EdgeExpandFunction
import edu.uci.ics.jung.visualization.renderers.edge.functions.nonindexed.EdgeShiftFunctions
import org.depparse.Label
import org.depparse.Sentence
import org.depparse.Token

class SentenceGraph<V : Token, E : Label>(@Suppress("unused") private val sentences: Array<Sentence>) : AugmentedGraphNonIndexed<V, E>(true) {

    @CallSuper
    override fun initFunctions() {
        super.initFunctions()

        // vertex
        vertexToIndexFunction = { vertex: V -> vertex.index }
        vertexToWeightFunction = { t: V, _: Network<V, E> -> t.word.length.toFloat() }
        vertexIndexComparator = Comparator { t1: V, t2: V ->
            val i1 = vertexToIndexFunction.invoke(t1)
            val i2 = vertexToIndexFunction.invoke(t2)
            i1.compareTo(i2)
        }

        // edge
        parallelEdgeIndexFunction = DefaultParallelEdgeIndexFunction()
        parallelEdgeIndexFunction.reset()
        edgeExpandFunction = EdgeExpandFunction(network, vertexToIndexFunction)
        edgeShiftFunctions = EdgeShiftFunctions(edgeExpandFunction, vertexToIndexFunction)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        network.nodes().sortedBy { it.index }.forEach {
            sb.append(it)
                .append(' ')
                .append(it.index)
                .append('\n')
        }
        for (e in network.edges()) {
            val endpoints = network.incidentNodesOf(e)
            val v1 = endpoints.source()
            val v2 = endpoints.target()
            sb.append(v1)
                .append('-')
                .append(e!!.label())
                .append('-')
                .append('>')
                .append(v2)
                .append('\n')
        }
        return sb.toString()
    }
}
