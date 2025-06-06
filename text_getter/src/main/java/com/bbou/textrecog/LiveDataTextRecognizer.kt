/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.textrecog

import android.graphics.Bitmap
import android.util.Log
import androidx.livedata.contrib.AsyncTransformations.LiveDataAsyncFunction
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.function.Consumer

class LiveDataTextRecognizer : LiveDataAsyncFunction<Bitmap, String?>() {

    override fun transform(input: Bitmap, consumer: Consumer<String?>) {

        val image = InputImage.fromBitmap(input, 0)
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            .process(image)
            .addOnSuccessListener { visionText: Text ->
                val text = TextExtractor.extract(visionText)
                consumer.accept(text?.toString())
            }
            .addOnFailureListener { e: Exception? -> Log.e(TAG, "Exception while executing livedata text recognizer", e) }
    }

    companion object {

        private const val TAG = "LiveDataTextRecognizer"
    }
}
