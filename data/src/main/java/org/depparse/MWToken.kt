package org.depparse

import android.os.Parcel
import android.os.Parcelable
import android.util.Log

open class MWToken(
    @JvmField val sentenceIndex: Int, // possibly -1
    @JvmField val index: Int, // possibly -1
    @JvmField val word: String, // not null
    @JvmField val start: Int, // possibly -1
    @JvmField val end: Int, // possibly -1, inclusive, relative to sentence text
    @JvmField val firstToken: Int,
    @JvmField val lastToken: Int, // inclusive
) : HasSegment, HasIndex {
    override val ith: Int
        get() = index

    override val segment: Segment
        get() = Segment(start, end)

    val tokenRange = firstToken..lastToken

    override fun toString(): String {
        return word
    }
}

class ParcelableMWToken(val mwtoken: MWToken?) : Parcelable {

    constructor(parcel: Parcel) : this(readMWToken(parcel)!!)

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        writeMWToken(parcel, mwtoken)
    }

    companion object {

        private const val TAG = "ParcelableMWToken"

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableMWToken> = object : Parcelable.Creator<ParcelableMWToken> {

            override fun createFromParcel(parcel: Parcel): ParcelableMWToken {
                return ParcelableMWToken(parcel)
            }

            override fun newArray(size: Int): Array<ParcelableMWToken?> {
                return arrayOfNulls(size)
            }
        }

        // W R I T E

        @JvmStatic
        fun writeMWToken(parcel: Parcel, mwtoken: MWToken?) {
            // null flag
            if (mwtoken == null) {
                parcel.writeInt(0)
                return
            }
            parcel.writeInt(1)

            // fields
            parcel.writeInt(mwtoken.sentenceIndex)
            parcel.writeInt(mwtoken.index)
            parcel.writeString(mwtoken.word)
            parcel.writeInt(mwtoken.start)
            parcel.writeInt(mwtoken.end)
            parcel.writeInt(mwtoken.firstToken)
            parcel.writeInt(mwtoken.lastToken)
            Log.i(TAG, "Write parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
        }

        // R E A D

        /**
         * Read mwtoken from parcel
         *
         * @param parcel parcel to read from
         * @return mwtoken
         */
        @JvmStatic
        fun readMWToken(parcel: Parcel): MWToken? {
            Log.i(TAG, "Read parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                val sentenceIndex = parcel.readInt()
                val index = parcel.readInt()
                val text = parcel.readString()!!
                val start = parcel.readInt()
                val end = parcel.readInt()
                val firstToken = parcel.readInt()
                val lastToken = parcel.readInt()
                return MWToken(sentenceIndex, index, text, start, end, firstToken, lastToken)
            }
            return null
        }
    }
}
