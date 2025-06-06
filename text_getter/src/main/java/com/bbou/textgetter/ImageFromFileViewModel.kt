/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textgetter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import com.bbou.textrecog.ImageUtils
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.util.Objects

class ImageFromFileViewModel private constructor(context: Context) : ViewModel() {

    val input: MutableLiveData<Uri> = MutableLiveData()

    var output: LiveData<Bitmap?>? = null

    init {
        try {
            output = input.map { uri: Uri? ->
                if (uri != null) {
                    try {
                        context.contentResolver.openInputStream(uri)
                            .use { `is` ->
                                BufferedInputStream(Objects.requireNonNull<InputStream>(`is`))
                                    .use { bis -> return@map ImageUtils.makeBitmap(bis) }
                            }
                    } catch (e: IOException) {
                        Log.e(TAG, uri.toString(), e)
                    }
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
            return ImageFromFileViewModel(context) as M
        }
    }

    companion object {

        private const val TAG = "ImageFromFileViewModel"
    }
}
