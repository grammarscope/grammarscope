package org.grammarscope

import android.content.Context
import android.util.Log
import opennlp.tools.sentdetect.SentenceDetectorME
import opennlp.tools.util.Span

class SentenceDetector(private val detector: SentenceDetectorME) {

    fun detectSentences(text: String?): Array<String> {

        // Detecting the sentence
        val sentences = detector.sentDetect(text)

        // Logging the sentences
        for ((i, sent) in sentences.withIndex()) {
            Log.d(TAG, "[$i] $sent")
        }
        return sentences
    }

    fun detectSpans(text: String): Array<Span> {

        // Detecting the position of the sentences in the raw text
        val spans = detector.sentPosDetect(text)

        // Logging the spans of the sentences in the paragraph
        for ((i, span) in spans.withIndex()) {
            Log.d(TAG, "Span[$i] $span")
        }
        return spans
    }

    fun detectProbs(): DoubleArray {

        // Getting the probabilities of the last decoded sequence
        val probs = detector.sentenceProbabilities

        // Logging the probability of the sentences in the paragraph
        for ((i, prob) in probs.withIndex()) {
            Log.d(TAG, "Prob[$i] $prob")
        }
        return probs
    }

    companion object {

        private const val TAG = "SentenceDetectionME"

        @Throws(Exception::class)
        fun detect(context: Context, lang: String, text: String?): Array<String> {
            val detector = SentenceDetectorFactory.build(context, lang)
            val sentences = detector.detectSentences(text)
            detector.detectProbs()
            return sentences
        }
    }
}
