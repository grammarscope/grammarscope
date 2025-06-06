package org.depparse.common

import com.bbou.coroutines.BaseTaskWithCallback
import org.depparse.Sentence
import java.text.Normalizer
import java.util.function.Consumer

abstract class BaseParse<R>(private val consumer: Consumer<R?>) : BaseTaskWithCallback<String, R?>() {

    /**
     * Input strings
     */
    lateinit var inputs: Array<Pair<Int, String>>

    /**
     * Engine processing
     *
     * @param params a single parameter as input string (a single parameter here)
     * @return an array of (parsed) non-null sentences, or null if failure
     */
    @Throws(IllegalStateException::class)
    override suspend fun doJob(params: String): R? {
        val provider = UniqueProvider.SINGLETON.get()
        if (provider != null) {

            // input: split, trimmed of empties
            inputs = params
                .splitIntoSentencesWithPositions()
                .dropLastWhile { it.second.isEmpty() }
                .toTypedArray()

            // provider processing operation (parse) returns an array of (parsed) non-null sentences, or null if failure
            val sentences = provider.process(
                inputs
                .map { it.second }
                .map { Normalizer.normalize(it, Normalizer.Form.NFD) }
                .toTypedArray())

//            val inputs = params
//                .split("\n+".toRegex())
//                .dropLastWhile { it.isEmpty() }
//                .map { Normalizer.normalize(it, Normalizer.Form.NFD) }
//                .toTypedArray()
//            val sentences = provider.process(inputs)

            // transforms this parse result to a result of type R, possibly null
            return toR(sentences)
        }
        throw IllegalStateException("Null provider.")
    }

    /**
     * Splits a string into sentences while tracking their starting positions.
     *
     * @return An array of SentenceWithPosition objects.
     */
    private fun String.splitIntoSentencesWithPositions(): Array<Pair<Int, String>> {
        //val pattern = "(?<=[.!?])\\s+".toRegex() // Regular expression for sentence ending
        val pattern = "\n+".toRegex() // Regular expression for sentence ending
        val sentences = mutableListOf<Pair<Int, String>>()
        var start = 0
        pattern.findAll(this).forEach { match ->
            val sentence = this.substring(start, match.range.first + 1).trim()
            sentences.add(start to sentence)
            start = match.range.last + 1
        }
        // Adds the last sentence (no end punctuation).
        if (start < this.length) {
            val lastSentence = this.substring(start).trim()
            sentences.add(start to lastSentence)
        }
        return sentences.toTypedArray()
    }

    /**
     * Transform parse result into type T
     *
     * @param sentences parsed sentences, possibly null
     * @return T
     */
    protected abstract fun toR(sentences: Array<Sentence>?): R?

    /**
     * Pass result to consumer
     *
     * @param result
     */
    override fun onDone(result: R?) {
        consumer.accept(result)
    }
}
