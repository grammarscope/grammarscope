/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.coroutines.observe

import android.util.Log
import com.bbou.coroutines.observe.Formatter.formatAsString
import java.util.function.Consumer

/**
 * Task print observer
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
@Suppress("unused")
class TaskPrintObserver<Progress : Pair<Number, Number>> : Consumer<Progress> {

    /**
     * Consumer accept
     *
     * @param progress observed progress
     */
    override fun accept(progress: Progress) {
        val unit: String? = null
        val longProgress = progress.first.toLong()
        val longLength = progress.second.toLong()
        Log.d(TAG, formatAsString(longProgress, longLength, unit))
    }


    companion object {
        private const val TAG = "TaskObserver"
    }
}
