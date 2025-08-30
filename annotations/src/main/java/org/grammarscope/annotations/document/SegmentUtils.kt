/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.grammarscope.annotations.document

import org.depparse.Segment

/**
 * Segment utilities
 *
 * @author Bernard Bou
 */
object SegmentUtils {

    /**
     * Merge segments to segment
     *
     * @param segments ordered segments
     * @return segment
     */
    fun merge(vararg segments: Segment): Segment {
        if (segments.isEmpty()) throw IllegalArgumentException("segment vararg is empty")
        return segments[0].first to segments[segments.size - 1].second
    }

    /**
     * Make intermediate segment list
     *
     * @param leftSegment  start segment
     * @param rightSegment finish segment
     * @param wordSegments word segments
     * @return list of segments
     */
    fun split(leftSegment: Segment, rightSegment: Segment, wordSegments: List<Segment>): MutableList<Segment> {
        val segment = merge(leftSegment, rightSegment)
        return split(segment, wordSegments)
    }

    /**
     * Split segment into word segments
     *
     * @param segment segment
     * @param wordSegments word segments
     * @return list of word segments
     */
    fun split(segment: Segment, wordSegments: List<Segment>): MutableList<Segment> {
        val list: MutableList<Segment> = ArrayList()
        for (wordSegment in wordSegments) {
            val l = wordSegment.first.compareTo(segment.first)
            val r = wordSegment.second.compareTo(segment.second)
            if (l >= 0 && r <= 0) {
                list.add(wordSegment)
            }
        }
        return list
    }
}