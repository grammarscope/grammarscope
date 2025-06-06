package org.depparse

data class Sentence(
    @JvmField val text: String, // not null, possibly empty
    @JvmField val start: Int, // possibly -1
    @JvmField val end: Int, // possibly -1, inclusive
    @JvmField val tokens: Array<Token>, // not null, possibly empty
    @JvmField val docid: String, // not null, possibly empty
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sentence

        if (text != other.text) return false
        if (start != other.start) return false
        if (end != other.end) return false
        if (!tokens.contentEquals(other.tokens)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + start
        result = 31 * result + end
        result = 31 * result + tokens.contentHashCode()
        return result
    }

    val segment: Segment
        get() = Segment(start, end)
}
