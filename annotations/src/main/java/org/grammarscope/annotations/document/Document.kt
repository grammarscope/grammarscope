package org.grammarscope.annotations.document

import org.depparse.HasIndex
import org.depparse.HasIndices
import org.depparse.HasSegment
import org.depparse.Segment

/**
 * Graph edge
 *
 * @param N node type
 */
interface GraphEdge<N> : HasIndex, HasIndices where N : HasIndex, N : HasSegment {
    val source: N
    val target: N
    val label: String?
}

/**
 * Graph interface
 *
 * @param N node type
 */
interface Graph<N> where N : HasIndex, N : HasSegment {
    val nodes: Collection<N>
    val edges: Collection<GraphEdge<N>>
}

/**
 * Document interface
 *
 * @author Bernard Bou
 */
interface Document<N> where N : HasIndex, N : HasSegment {

    val sentenceCount: Int

    val wordSegments: List<Segment>

    fun getTextSegment(sentenceIdx: Int, segment: Segment): Segment

    fun getGraph(sentenceIdx: Int): Graph<N>
}
