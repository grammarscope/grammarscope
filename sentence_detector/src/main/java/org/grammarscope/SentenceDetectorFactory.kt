/*
 * Copyright (c) 2019. Bernard Bou <1313ou@gmail.com>.
 */
package org.grammarscope

import android.content.Context
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.sentdetect.SentenceModel
import org.depparse.sentdetector.R
import java.util.Locale

object SentenceDetectorFactory {

    @Throws(Exception::class)
    fun build(context: Context, lang: String): SentenceDetector {
        val modelPath: String = try {
            getTag(lang) + "-sent.bin"
        } catch (e: UnsupportedOperationException) {
            throw UnsupportedOperationException(context.getString(R.string.status_unsupported) + ' ' + lang, e)
        }

        // Loading sentence detector model
        var model: SentenceModel?
        context.assets.open(modelPath).use { inputStream -> model = SentenceModel(inputStream) }

        // Instantiating the SentenceDetectorME class
        return SentenceDetector(SentenceDetectorME(model))
    }

    private fun getTag(lang: String): String {
        return when (val tag = lang.take(2).lowercase(Locale.getDefault())) {
            "en", "fr" -> tag
            else -> throw UnsupportedOperationException(tag)
        }
    }
}
