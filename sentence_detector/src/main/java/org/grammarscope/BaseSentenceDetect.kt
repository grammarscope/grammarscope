package org.grammarscope

import android.content.Context
import com.bbou.coroutines.BaseTaskWithCallback

abstract class BaseSentenceDetect(private val detection: SentenceDetector) : BaseTaskWithCallback<String?, Array<String>>() {

    constructor(context: Context, lang: String) : this(SentenceDetectorFactory.build(context, lang))

    override suspend fun doJob(params: String?): Array<String> {
        return detection.detectSentences(params)
    }
}
