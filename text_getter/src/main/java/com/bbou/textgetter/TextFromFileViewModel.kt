/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.Objects

class TextFromFileViewModel private constructor(context: Context) : ViewModel() {

    val input: MutableLiveData<Uri> = MutableLiveData()

    lateinit var output: LiveData<String?>

    init {
        try {
            output = input.map { uri: Uri ->

                try {
                    context.contentResolver.openInputStream(uri).use { `is` ->
                        BufferedReader(InputStreamReader(Objects.requireNonNull<InputStream>(`is`))).use { reader ->
                            val sb = StringBuilder()
                            var line: String?
                            while (null != reader.readLine().also { line = it }) {
                                sb.append(line)
                                sb.append('\n')
                            }
                            return@map sb.toString()
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, uri.toString(), e)
                }
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception while making file text view model", e)
        }
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {

        override fun <M : ViewModel> create(modelClass: Class<M>): M {
            @Suppress("UNCHECKED_CAST")
            return TextFromFileViewModel(context) as M
        }
    }

    companion object {

        private const val TAG = "TextFromFileViewModel"
    }
}
