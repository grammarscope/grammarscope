package org.grammarscope.result

import android.os.Build
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import org.depparse.Sentence
import org.grammarscope.service.IParceler

class Parceler : IParceler<Array<Sentence>> {

    override fun toParcelable(result: Array<Sentence>): Parcelable {
        return ParcelableResult(result)
    }

    override fun toResult(result: Parcelable): Array<Sentence>? {
        val parcelableResult = result as ParcelableResult
        return parcelableResult.result
    }

    override fun toResult(bundle: Bundle, key: String): Array<Sentence>? {
        return getResult(bundle, key)
    }

    companion object {

        private const val TAG = "Parceler"

        // F R O M   B U N D L E

        fun getResult(resultData: Bundle, key: String?): Array<Sentence>? {
            resultData.classLoader = ParcelableResult::class.java.classLoader
            var result: Array<Sentence>? = null
            var parcelable = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) resultData.getParcelable(key, Parcelable::class.java) else @Suppress("DEPRECATION") resultData.getParcelable(key)
            if (parcelable != null) {

                // across class loaders
                if (ParcelableResult::class.java != parcelable.javaClass) {
                    Log.d(TAG, "Parcel/Unparcel from classloader " + parcelable.javaClass.classLoader + " to target classloader " + ParcelableResult::class.java.classLoader)

                    // obtain parcel
                    val parcel = Parcel.obtain()

                    // write parcel
                    parcel.setDataPosition(0)
                    parcelable.writeToParcel(parcel, 0)

                    // read parcel
                    parcel.setDataPosition(0)
                    parcelable = ParcelableResult(parcel)

                    // recycle
                    parcel.recycle()
                }
                val parcelResult = parcelable as ParcelableResult
                result = parcelResult.result
            }
            return result
        }
    }
}
