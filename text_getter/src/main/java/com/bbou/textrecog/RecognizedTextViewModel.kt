/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textrecog

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.livedata.contrib.AsyncTransformations

class RecognizedTextViewModel : ViewModel() {

    val input: MutableLiveData<Bitmap> = MutableLiveData()

    lateinit var output: LiveData<String?>

    init {
        try {
            val recognizer = LiveDataTextRecognizer()
            output = AsyncTransformations.map(input, recognizer)
        } catch (e: Exception) {
            Log.e(TAG, "Exception while making recognized text view model", e)
        }
    }

    companion object {

        private const val TAG = "RecognizedTextViewModel"
    }
}
