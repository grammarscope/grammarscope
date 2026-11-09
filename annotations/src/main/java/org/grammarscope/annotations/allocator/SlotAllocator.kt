package org.grammarscope.annotations.allocator

import org.depparse.HasIndices

/**
 * Edge vertical slot allocator
 *
 * @author Bernard Bou
 */
class SlotAllocator<E : HasIndices> : SlotAllocatorForIndexRanges<E>() {

    /**
     * Allocate
     *
     * @param elements0  elements
     * @param comparator element allocation order
     */
    fun allocate(elements0: Collection<E>, comparator: Comparator<E>) {
        // sort elements
        val elements: MutableList<E> = ArrayList(elements0)
        elements.sortWith(comparator)

        // allocate
        super.allocate(elements)
    }

    override fun toString(): String {
        val sb = StringBuilder()
        for ((edge, value) in this.slots) {
            sb.append("$edge index=(${edge.lowIndex}-${edge.highIndex}) = $value\n")
        }
        return sb.toString()
    }
}
