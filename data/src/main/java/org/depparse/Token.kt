package org.depparse

import java.io.IOException
import java.util.Locale
import java.util.regex.Pattern

open class Token(
    @JvmField val sentenceIndex: Int, // possibly -1
    @JvmField val index: Int, // possibly -1
    @JvmField val word: String, // not null
    @JvmField val start: Int, // possibly -1
    @JvmField val end: Int, // possibly -1, inclusive, relative to sentence text
    @JvmField val category: String, // not null, possibly empty
    @JvmField val tag: String, // not null, possibly empty
    @JvmField val head: Int, // possibly -1
    @JvmField val label: String, // not null, possibly empty
    @JvmField val breakLevel: Int, // possibly -1
    @JvmField val deps: String?, // not null, possibly empty
) : Label, HasSegment, HasIndex {

    override val ith: Int
        get() = index

    override val segment: Segment
        get() = Segment(start, end)

    object TokenTagProcessor {

        private const val REGEXPR = "name: [\"']([^\"']+)[\"'] value: [\"']([^\"']+)[\"']"
        private val pattern = Pattern.compile(REGEXPR)

        @Throws(IOException::class)
        @JvmStatic
        fun toString(tag: String): String {
            val matcher = pattern.matcher(tag)
            val sb = StringBuilder()
            while (matcher.find()) {
                val name = matcher.group(1)
                val value = matcher.group(2)
                sb.append(name)
                    .append(" = ")
                    .append(value)
                    .append('\n')
            }
            return sb.toString()
        }

        @JvmStatic
        fun splitTag(tag: String): Map<String, String> {
            val result = HashMap<String, String>()
            val matcher = pattern.matcher(tag)
            while (matcher.find()) {
                val name = matcher.group(1)!!
                val value = matcher.group(2)!!
                result[name] = value
            }
            return result
        }
    }

    object TokenEnhancedDepsProcessor {
        /**
         * Parse DEPS string
         *
         * @param input input
         * @return list of (label, head) pairs
         */
        fun parse(input: String): List<Pair<String, Int>> {
            return input
                .trim()
                .split("|")
                .map {
                    val (head, label) = it.split(":")
                    label to head.toInt()
                }
                .toList()
        }
    }

    enum class BreakLevel {
        /* No separation between tokens */ NO_BREAK,
        /* Tokens separated by space */ SPACE_BREAK,
        /* Tokens separated by line break */ LINE_BREAK,
        /* Tokens separated by sentence break. New sentence. */ SENTENCE_BREAK;

        companion object {

            @Throws(IOException::class)
            @JvmStatic
            fun toString(breakLevelIndex: Int): String {
                if (breakLevelIndex == -1) return ""
                val breakLevel = entries[breakLevelIndex]
                return breakLevel.toString().lowercase(Locale.getDefault())
            }
        }
    }

    override fun label(): String {
        return label
    }

    override fun toString(): String {
        return word
    }
}
