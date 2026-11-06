package org.depparse

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.depparse.ParcelableMWToken.Companion.readMWToken
import org.depparse.ParcelableMWToken.Companion.writeMWToken
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
    @JvmField var mwt: MWToken?, // nullable
) : Label, HasSegment, HasIndex {

    override val ith: Int
        get() = index

    override val segment: Segment
        get() = mwt?.segment ?: tokenSegment

    val tokenSegment: Segment
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

class ParcelableToken(val token: Token?) : Parcelable {

    constructor(parcel: Parcel) : this(readToken(parcel)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeToken(parcel, token)
    }

    companion object {

        private const val TAG = "ParcelableToken"

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableToken> = object : Parcelable.Creator<ParcelableToken> {

            override fun createFromParcel(parcel: Parcel): ParcelableToken {
                return ParcelableToken(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableToken?> {
                return arrayOfNulls(size)
            }
        }

        // W R I T E

        /**
         * Write token to parcel
         *
         * @param parcel parcel to write to
         * @param token token
         */
        @JvmStatic
        fun writeToken(parcel: Parcel, token: Token?) {
            // null flag
            if (token == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)

            // fields
            parcel.writeInt(token.sentenceIndex)
            parcel.writeInt(token.index)
            parcel.writeString(token.word)
            parcel.writeInt(token.start)
            parcel.writeInt(token.end)
            parcel.writeString(token.category)
            parcel.writeString(token.tag)
            parcel.writeInt(token.head)
            parcel.writeString(token.label)
            parcel.writeInt(token.breakLevel)
            writeMWToken(parcel, token.mwt)
            Log.i(TAG, "Write parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
        }

        // R E A D

        /**
         * Read token from parcel
         *
         * @param parcel parcel to read from
         * @return token
         */
        @JvmStatic
        fun readToken(parcel: Parcel): Token? {
            Log.i(TAG, "Read parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                val sentenceIndex = parcel.readInt()
                val index = parcel.readInt()
                val word = parcel.readString()!!
                val start = parcel.readInt()
                val end = parcel.readInt()
                val category = parcel.readString()!!
                val tag = parcel.readString()!!
                val head = parcel.readInt()
                val label = parcel.readString()!!
                val breaklevel = parcel.readInt()
                val deps = parcel.readString()
                val mwtoken = readMWToken(parcel)
                return Token(sentenceIndex, index, word, start, end, category, tag, head, label, breaklevel, deps, mwtoken)
            }
            return null
        }
    }
}
