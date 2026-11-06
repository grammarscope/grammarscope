package org.depparse

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.depparse.ParcelableMWToken.Companion.readMWToken
import org.depparse.ParcelableMWToken.Companion.writeMWToken
import org.depparse.ParcelableToken.Companion.readToken
import org.depparse.ParcelableToken.Companion.writeToken

data class Sentence(
    @JvmField val text: String, // not null, possibly empty
    @JvmField val start: Int, // possibly -1
    @JvmField val end: Int, // possibly -1, inclusive
    @JvmField val tokens: Array<Token>, // not null
    @JvmField val mwtokens: Array<MWToken>?, // nullable, possibly empty
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
        if (!mwtokens.contentEquals(other.mwtokens)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + start
        result = 31 * result + end
        result = 31 * result + tokens.contentHashCode()
        result = 31 * result + mwtokens.contentHashCode()
        return result
    }

    val segment: Segment
        get() = Segment(start, end)
}

class ParcelableSentence(val sentence: Sentence) : Parcelable {

    constructor(parcel: Parcel) : this(readSentence(parcel)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeSentence(parcel, sentence)
    }

    companion object {

        private const val TAG = "ParcelableSentence"

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableSentence> = object : Parcelable.Creator<ParcelableSentence> {

            override fun createFromParcel(`in`: Parcel): ParcelableSentence {
                return ParcelableSentence(`in`)
            }

            override fun newArray(size: Int): Array<ParcelableSentence?> {
                return arrayOfNulls(size)
            }
        }

        // W R I T E

        @JvmStatic
        fun writeSentence(parcel: Parcel, sentence: Sentence?) {
            // null flag
            if (sentence == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)

            // fields
            parcel.writeString(sentence.docid)
            parcel.writeString(sentence.text)
            parcel.writeInt(sentence.start)
            parcel.writeInt(sentence.end)

            // tokens
            parcel.writeInt(1) // null tokens flag
            parcel.writeInt(sentence.tokens.size)
            for (token in sentence.tokens) {
                writeToken(parcel, token)
            }
            // mwtokens
            val isNotNullMWTokens = sentence.mwtokens != null
            parcel.writeInt(if (isNotNullMWTokens) 1 else 0) // null mwtokens flag
            if (isNotNullMWTokens) {
                parcel.writeInt(sentence.mwtokens.size)
                for (mwtoken in sentence.mwtokens) {
                    writeMWToken(parcel, mwtoken)
                }
            }
            Log.i(TAG, "Write parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
        }


        // R E A D

        /**
         * Read sentence from parcel
         *
         * @param parcel parcel to read from
         * @return sentence
         */
        @JvmStatic
        fun readSentence(parcel: Parcel): Sentence? {
            Log.i(TAG, "Read parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                val docid = parcel.readString()!!
                val text = parcel.readString()!!
                val start = parcel.readInt()
                val end = parcel.readInt()
                val isNotNullTokens = parcel.readInt()
                // tokens
                val tokens = if (isNotNullTokens != 0) {
                    val n = parcel.readInt()
                    Array(n) {
                        readToken(parcel)!!
                    }
                } else {
                    emptyArray<Token>()
                }
                // mwtokens
                val isNotNullMWTokens = parcel.readInt()
                val mwtokens = if (isNotNullMWTokens != 0) {
                    val n = parcel.readInt()
                    val array = Array(n) {
                        readMWToken(parcel)!!
                    }
                    array
                } else {
                    null
                }
                return Sentence(text, start, end, tokens, mwtokens, docid)
            }
            return null
        }
    }
}
