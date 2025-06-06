/*
 * Copyright (c) 2025. Bernard Bou <1313ou@gmail.com>.
 */

package org.depparse

import java.nio.charset.StandardCharsets

/**
 * Gets character indices corresponding to byte offsets in a UTF-8 string efficiently.
 */
fun getCharIndices(text: String): IntArray {

    // Create byte-to-char index mapping
    val bytes = text.toByteArray(StandardCharsets.UTF_8)
    val byteToCharIndex = IntArray(bytes.size + 1)

    var bytePos = 0
    for (i in text.indices) {
        val char = text[i]
        val charByteCount = char.toString().toByteArray(StandardCharsets.UTF_8).size

        // Fill in the byte-to-char mapping for each byte in this character
        for (j in 0 until charByteCount) {
            if (bytePos + j < byteToCharIndex.size) {
                byteToCharIndex[bytePos + j] = i
            }
        }
        bytePos += charByteCount
    }

    // Set the final position
    if (bytePos < byteToCharIndex.size) {
        byteToCharIndex[bytePos] = text.length
    }
    return byteToCharIndex
}

/**
 * Look up the character indices for byte offsets
 * @param byteToCharIndex Byte-to-character index mapping
 */
fun Segment.charSegment(byteToCharIndex: IntArray): Segment {
    val startCharIndex = if (first >= 0 && first < byteToCharIndex.size)
        byteToCharIndex[first] else 0
    val endCharIndex = if (second >= 0 && second < byteToCharIndex.size)
        byteToCharIndex[second] else byteToCharIndex[byteToCharIndex.lastIndex]
    return startCharIndex to endCharIndex
}

/**
 * Byte segment to character segment
 *
 * @param text text
 * @return character segment
 */
fun Segment.charSegment(text: String): Segment {
    val byteToCharIndex: IntArray = getCharIndices(text)
    return charSegment(byteToCharIndex)
}

/**
 * Unicode segments
 *
 * @param text text
 * @param segments segments to look up
 * @return map of byte segments to character segments
 */
fun unicodeSegments(text: String, vararg segments: Segment): Map<Segment, Segment> {
    val byteToCharIndex: IntArray = getCharIndices(text)
    return segments.associate {
        it to it.charSegment(byteToCharIndex)
    }
}