/*
 * Copyright (c) 2023. Bernard Bou <1313ou@gmail.com>.
 */
package com.bbou.download.coroutines.core

import com.bbou.coroutines.Task
import com.bbou.download.DownloadData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.coroutineContext

/**
 * Download result
 */
typealias DownloadResult = Pair<Boolean, DownloadData?>?

/**
 * Download task
 */
class DownloadTask : Task<DownloadTask.Parameters, Pair<Long, Long>, DownloadResult>() {

    /**
     * Parameters
     *
     * @property fromUrl source url
     * @property toFile destination file
     * @property renameFrom rename source
     * @property renameTo rename dest
     */
    data class Parameters(val fromUrl: String, val toFile: String, val renameFrom: String? = null, val renameTo: String? = null)

    /**
     * Delegate
     */
    private val delegate = DownloadCore(this)

    override suspend fun doJob(params: Parameters): DownloadResult {

        // retrieve params
        val fromUrl = params.fromUrl
        val toFile = params.toFile
        val renameFrom = params.renameFrom
        val renameTo = params.renameTo

        // do the work
        return try {
            val job = coroutineContext[Job] ?: return null
            job.ensureActive() // checks for cancellation
            println("Job> $fromUrl $toFile ${where()}")
            val outData = delegate.work(fromUrl, toFile, renameFrom, renameTo, null)
            println("Job< ${where()}")
            true to outData
        } catch (e: Exception) {
            false to null
        }
    }

    private val outerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Run
     *
     * @param fromUrl source url
     * @param toFile dest file
     * @param renameFrom rename source
     * @param renameTo rename dest
     * @param observer observer
     * @param resultConsumer result consumer
     */
    fun run(fromUrl: String, toFile: String, renameFrom: String? = null, renameTo: String? = null, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadResult>) {
        outerScope.launch {
            try {
                println("Run ${where()}")
                val result = runObserved(Dispatchers.IO, Parameters(fromUrl, toFile, renameFrom, renameTo), observer)
                println("Done '$result' ${where()}")
                resultConsumer.accept(result)
                println("End ${where()}")
            } catch (ce: CancellationException) {
                println("Caught $ce ${where()}")
                println("Cancelled ${where()}")
                resultConsumer.accept(null)
            } finally {
                println("Exit ${where()}")
            }
        }
    }

    override fun cancel() {
        super.cancel()
        outerScope.cancel()
    }

    companion object {

        private lateinit var task: DownloadTask

        /**
         * Thread info
         */
        fun where(): String {
            return " @ ${Thread.currentThread()}"
        }

        /**
         * Start download
         *
         * @param fromUrl source url
         * @param toFile destination
         * @param observer observer
         * @param resultConsumer result consumer
         */
        fun start(fromUrl: String, toFile: String, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadResult>) {
            task = DownloadTask()
            task.run(fromUrl, toFile, null, null, observer, resultConsumer)
        }

        /**
         * Start download
         *
         * @param fromUrl source zip url
         * @param toDir destination dir
         * @param renameFrom rename source
         * @param renameTo rename dest
         * @param observer observer
         * @param resultConsumer result consumer
         */
        fun start(fromUrl: String, toDir: String, renameFrom: String?, renameTo: String?, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadResult>) {
            task = DownloadTask()
            task.run(fromUrl, toDir, renameFrom, renameTo, observer, resultConsumer)
        }

        /**
         * Cancel task
         */
        fun cancel() {
            task.cancel()
        }
    }
}