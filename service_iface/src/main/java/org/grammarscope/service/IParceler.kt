package org.grammarscope.service

import android.os.Bundle
import android.os.Parcelable

interface IParceler<R> {

    fun toParcelable(result: R): Parcelable

    fun toResult(result: Parcelable): R?

    fun toResult(bundle: Bundle, key: String): R?
}
