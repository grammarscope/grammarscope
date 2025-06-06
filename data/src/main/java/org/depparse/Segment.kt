/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse

/**
 * Segment (pair of from- and to-indices). Indices refer to nth character in text. To-index is not included in segment.
 */
typealias Segment = Pair<Int, Int>

interface HasSegment {
    val segment: Segment
}
