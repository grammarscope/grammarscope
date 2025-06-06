package org.grammarscope.result

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.depparse.ParcelableSentence.Companion.readSentence
import org.depparse.ParcelableSentence.Companion.writeToParcel
import org.depparse.Sentence

class ParcelableResult : Parcelable {

    /**
     * Wrapped result
     */
    val result: Array<Sentence>?

    constructor(result0: Array<Sentence>?) {
        result = result0
    }

    constructor(parcel: Parcel) {
        result = readResult(parcel)
    }

    override fun describeContents(): Int {
        return 0
    }

    // W R I T E

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        if (result == null) {
            parcel.writeInt(0)
            return
        }
        parcel.writeInt(1)
        val n = result.size
        parcel.writeInt(n)
        for (sentence in result) {
            writeToParcel(parcel, sentence)
        }
        Log.i(TAG, "Write parcel write size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
    }

    companion object {

        private const val TAG = "ParcelableResult"

        @Suppress("unused")
        @JvmField
        val CREATOR: Parcelable.Creator<ParcelableResult> = object : Parcelable.Creator<ParcelableResult> {

            override fun createFromParcel(`in`: Parcel): ParcelableResult {
                return ParcelableResult(`in`)
            }

            override fun newArray(size: Int): Array<ParcelableResult?> {
                return arrayOfNulls(size)
            }
        }

        // R E A D

        /**
         * Read result from parcel
         *
         * @param parcel parcel to read from
         * @return result
         */
        private fun readResult(parcel: Parcel): Array<Sentence>? {
            Log.i(TAG, "Read parcel read size=" + parcel.dataSize() + " pos=" + parcel.dataPosition())
            val isNotNull = parcel.readInt()
            if (isNotNull != 0) {
                val n = parcel.readInt()
                val sentences = Array(n) {
                    readSentence(parcel)!!
                }
                return sentences
            }
            return null
        }
    }
}
