package org.depparse

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

class ParcelableSentence : Parcelable {

    /**
     * Wrapped sentence
     */
    val sentence: Sentence

    constructor(sentence0: Sentence) {
        sentence = sentence0
    }

    constructor(parcel: Parcel) {
        sentence = readSentence(parcel)!!
    }

    override fun describeContents(): Int {
        return 0
    }

    // W R I T E

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeToParcel(parcel, sentence)
    }

    companion object {

        private const val TAG = "ParcelableSentence"

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableSentence> = object : Parcelable.Creator<ParcelableSentence> {

            override fun createFromParcel(`in`: Parcel): ParcelableSentence {
                return ParcelableSentence(`in`)
            }

            override fun newArray(size: Int): Array<ParcelableSentence?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun writeToParcel(parcel: Parcel, sentence: Sentence?) {
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

            // null tokens flag
            parcel.writeInt(1)
            parcel.writeInt(sentence.tokens.size)
            for (token in sentence.tokens) {
                writeToParcel(token, parcel)
            }
            Log.i(TAG, "Write parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
        }

        private fun writeToParcel(token: Token?, parcel: Parcel) {
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
                val tokens = if (isNotNullTokens != 0) {
                    val n = parcel.readInt()
                    Array(n) {
                        readToken(parcel)!!
                    }
                } else {
                    emptyArray<Token>()
                }
                return Sentence(text, start, end, tokens, docid)
            }
            return null
        }

        private fun readToken(parcel: Parcel): Token? {
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
                return Token(sentenceIndex, index, word, start, end, category, tag, head, label, breaklevel, deps)
            }
            return null
        }
    }
}
