/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.allocator

import org.depparse.HasIndex
import org.depparse.HasSegment
import org.grammarscope.annotations.document.GraphEdge
import kotlin.math.abs

/**
 * Slot and anchor allocator
 *
 * @param nodes nodes
 * @param edges edges
 *
 * @author Bernard Bou
 * @see SlotAllocator
 *
 * @see AnchorAllocator
 */
class Allocator<N>(nodes: Collection<N>, edges: Collection<GraphEdge<N>>) where N : HasIndex, N : HasSegment {

    /**
     * Slot allocator
     */
    private val slotAllocator = SlotAllocator<GraphEdge<N>>()

    /**
     * Anchor allocator
     */
    private val anchorAllocator = AnchorAllocator<N>()

    /**
     * Edge comparator based on edge's low index
     */
    private val leftComparator = Comparator { edge1: GraphEdge<N>, edge2: GraphEdge<N> ->
        val compare = edge1.lowIndex.compareTo(edge2.lowIndex)
        if (compare != 0)
            return@Comparator compare
        -1 * slotAllocator.getSlot(edge1).compareTo(slotAllocator.getSlot(edge2))
    }

    /**
     * Edge comparator based on edge's high index
     */
    private val rightComparator = Comparator { edge1: GraphEdge<N>, edge2: GraphEdge<N> ->
        val compare = -1 * edge1.highIndex.compareTo(edge2.highIndex)
        if (compare != 0) return@Comparator compare
        -1 * this@Allocator.slotAllocator.getSlot(edge1).compareTo(this@Allocator.slotAllocator.getSlot(edge2))
    }

    init {
        // Edge comparator based on edge's span
        val slotComparator: Comparator<GraphEdge<N>> = object : Comparator<GraphEdge<N>> {
            override fun compare(edge1: GraphEdge<N>, edge2: GraphEdge<N>): Int {
                val span1 = getSpan(edge1)
                val span2 = getSpan(edge2)
                if (span1 > span2) return 1
                else if (span1 < span2) return -1
                return 0
            }

            /**
             * Compute span (as bound by node indices)
             *
             * @return span
             */
            private fun getSpan(edge: GraphEdge<N>): Int {
                return abs((edge.target.ith - edge.source.ith).toDouble()).toInt()
            }
        }

        // slot allocator
        slotAllocator.allocate(edges, slotComparator)

        // anchor allocator
        anchorAllocator.allocate(nodes, edges, leftComparator, rightComparator)
    }

    /**
     * Get edge's slot
     *
     * @param edge edge
     * @return slot
     */
    fun getSlot(edge: GraphEdge<N>): Int {
        return slotAllocator.getSlot(edge)
    }

    /**
     * Get edge's left anchor
     *
     * @param edge edge
     * @return left anchor
     */
    fun getLeftAnchor(edge: GraphEdge<N>): Float {
        return anchorAllocator.getLeftAnchor(edge)
    }

    /**
     * Get edge's right anchor
     *
     * @param edge edge
     * @return right anchor
     */
    fun getRightAnchor(edge: GraphEdge<N>): Float {
        return anchorAllocator.getRightAnchor(edge)
    }

    override fun toString(): String {
        return "SLOTS\n" + slotAllocator + "ANCHORS\n" + anchorAllocator
    }
}
