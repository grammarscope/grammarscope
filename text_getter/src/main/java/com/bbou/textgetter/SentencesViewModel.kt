/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.livedata.contrib.AsyncTransformations.map
import org.grammarscope.SentenceDetectorFactory.build

class SentencesViewModel private constructor(context: Context, lang: String) : ViewModel() {

    val input: MutableLiveData<String> = MutableLiveData()

    lateinit var output: LiveData<Array<String>>

    init {
        try {
            val detector = build(context, lang)
            output = map(input) { text: String -> detector.detectSentences(text) }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while making recognized text view model", e)
        }
    }

    class Factory(private val context: Context, private val lang: String) : ViewModelProvider.Factory {

        override fun <M : ViewModel> create(modelClass: Class<M>): M {
            @Suppress("UNCHECKED_CAST")
            return SentencesViewModel(context, lang) as M
        }
    }

    companion object {

        private const val TAG = "SentencesViewModel"
    }
}
