/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.allocator

import org.depparse.HasIndex
import org.depparse.HasSegment
import org.grammarscope.annotations.document.GraphEdge

/**
 * Graph edge anchor allocator. Anchors are (float) shift value shifts from edge end center.
 *
 * @author Bernard Bou
 */
class AnchorAllocator<N>(val isRtl: Boolean) where N : HasIndex, N : HasSegment {

    /**
     * Cache of allocated anchors
     */
    private val anchors: MutableMap<N, Triple<Int, Int, Map<GraphEdge<N>, Int>>> = HashMap<N, Triple<Int, Int, Map<GraphEdge<N>, Int>>>()

    /**
     * Allocate node slots
     *
     * @param nodes           collection of nodes
     * @param leftComparator  anchor allocation order
     * @param rightComparator anchor allocation order
     */
    fun allocate(nodes: Collection<N>, edges: Collection<GraphEdge<N>>, leftComparator: Comparator<GraphEdge<N>>, rightComparator: Comparator<GraphEdge<N>>) {
        for (node in nodes) {

            val nodeIndices: MutableMap<GraphEdge<N>, Int> = HashMap<GraphEdge<N>, Int>()

            // sort into right / left incident
            val leftEdges: MutableList<GraphEdge<N>> = ArrayList<GraphEdge<N>>()
            val rightEdges: MutableList<GraphEdge<N>> = ArrayList<GraphEdge<N>>()

            edges
                .filter { node == it.source || node == it.target }
                .forEach {
                    if (isLeftIncident(it, node)) {
                        leftEdges.add(it)
                    } else if (isRightIncident(it, node)) {
                        rightEdges.add(it)
                    } else throw RuntimeException("Edge $it is neither right nor left incident to $node")
                }

            // sort
            leftEdges.sortWith(leftComparator)
            rightEdges.sortWith(rightComparator)

            // iterate on node's edges and allocate edge's anchors
            var left = 0
            for (edge in leftEdges) {
                nodeIndices.put(edge, --left)
            }
            var right = 0
            for (edge in rightEdges) {
                nodeIndices.put(edge, ++right)
            }
            this.anchors.put(node, Triple(left, right, nodeIndices))
        }
    }

    /**
     * Get anchor
     *
     * @param node node
     * @param edge oncoming or outgoing edge
     * @return anchor
     */
    fun getAnchor(node: N, edge: GraphEdge<N>): Float {
        val entry = this.anchors[node]!!
        val left: Int = entry.first
        val right: Int = entry.second
        val map: Map<GraphEdge<N>, Int> = entry.third
        val i: Int = map[edge]!!
        return center(i, left, right)
    }

    /**
     * Get edge's left anchor
     *
     * @param edge edge
     * @return left anchor
     */
    fun getLeftAnchor(edge: GraphEdge<N>): Float {
        val node = getLeftNode(edge)
        return getAnchor(node, edge)
    }

    /**
     * Get edge's right anchor
     *
     * @param edge edge
     * @return right anchor
     */
    fun getRightAnchor(edge: GraphEdge<N>): Float {
        val node = getRightNode(edge)
        return getAnchor(node, edge)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append(this.anchors.keys)
        sb.append('\n')
        for (node in this.anchors.keys) {
            val entry = this.anchors[node]!!
            val left: Int = entry.first
            val right: Int = entry.second
            val n = -left + right
            sb.append('-')
            sb.append(node)
            sb.append(' ')
            sb.append('#')
            sb.append(n)
            sb.append('(')
            sb.append(left)
            sb.append(',')
            sb.append(right)
            sb.append(')')
            sb.append('\n')
            val map: Map<GraphEdge<N>, Int> = entry.third
            for (edge in map.keys) {
                sb.append('\t')
                sb.append(edge)
                sb.append(' ')
                val i: Int = map[edge]!!
                sb.append(i)
                sb.append("->")
                sb.append(center(i, left, right))
                sb.append('\n')
            }
        }
        return sb.toString()
    }

    // H E L P E R S

    /**
     * Is backwards
     *
     * @param edge edge
     * @return whether this edge is backwards
     */
    private fun isBackwards(edge: GraphEdge<N>): Boolean {
        return if (isRtl) edge.target.ith > edge.source.ith else edge.target.ith < edge.source.ith
    }

    /**
     * Is right incident
     *
     * @param edge edge
     * @param node node
     * @return whether this edge is right incident relative to this node
     */
    private fun isRightIncident(edge: GraphEdge<N>, node: N): Boolean {
        if (node == edge.target) return isBackwards(edge)
        if (node == edge.source) return !isBackwards(edge)
        throw IllegalArgumentException(node.toString())
    }

    /**
     * Is left incident
     *
     * @param edge edge
     * @param node node
     * @return whether this edge is left incident relative to this node
     */
    private fun isLeftIncident(edge: GraphEdge<N>, node: N): Boolean {
        if (node == edge.target) return !isBackwards(edge)
        if (node == edge.source) return isBackwards(edge)
        throw IllegalArgumentException(node.toString())
    }

    /**
     * Get left node
     *
     * @param edge edge
     * @return left node
     */
    private fun getLeftNode(edge: GraphEdge<N>): N {
        return if (isBackwards(edge)) edge.target else edge.source
    }

    /**
     * Get right node
     *
     * @param edge edge
     * @return right node
     */
    private fun getRightNode(edge: GraphEdge<N>): N {
        return if (isBackwards(edge)) edge.source else edge.target
    }

    companion object {

        /**
         * Center
         *
         * @param i     index
         * @param left  leftmost index
         * @param right rightmost index
         * @return centering offset for node
         */
        fun center(i: Int, left: Int, right: Int): Float {
            val n = -left + right

            // shift so that zero slot is occupied
            var i2 = i
            var left2 = left
            if (-left > right)  // left outnumbers right
            {
                left2 = left + 1 // shift lower
                if (i < 0) {
                    i2++ // shift i rightward if it is left
                }
            } else  // right outnumbers left
            {
                if (i > 0) {
                    i2-- // shit i leftward if it is right
                }
            }
            val nLanes = n - 1
            val offsetInLanes = -left2 - nLanes / 2f
            return i2 + offsetInLanes
        }
    }
}
