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
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.coroutines.cancellation.CancellationException

/**
 * Download result
 */
typealias DownloadZipResult = Pair<Boolean, DownloadData?>?

/**
 * Download zip task
 *
 * @author [Bernard Bou](mailto:1313ou@gmail.com)
 */
class DownloadZipTask : Task<DownloadZipTask.Parameters, Pair<Long, Long>, DownloadZipResult>() {

    /**
     * Parameters
     *
     * @property fromUrl source url
     * @property toFile dest file
     * @property entry entry
     * @property renameFrom rename source
     * @property renameTo rename dest
     */
    data class Parameters(val fromUrl: String, val toFile: String, val entry: String? = null, val renameFrom: String? = null, val renameTo: String? = null)

    /**
     * Delegate
     */
    private val delegate = DownloadZipCore(this)

    override suspend fun doJob(params: Parameters): DownloadZipResult {

        // retrieve params
        val fromUrl = params.fromUrl
        val entry = params.entry
        val toFile = params.toFile
        val renameFrom = params.renameFrom
        val renameTo = params.renameTo

        // do the work
        return try {
            val job = currentCoroutineContext()[Job] ?: return null
            job.ensureActive() // checks for cancellation
            println("Job> $fromUrl $toFile ${where()}")
            val outData = delegate.work(fromUrl, toFile, renameFrom, renameTo, entry)
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
     * @param toFile file dest
     * @param entry entry
     * @param renameFrom rename source
     * @param renameTo rename dest
     * @param observer observer
     * @param resultConsumer result consumer
     */
    fun run(fromUrl: String, toFile: String, entry: String? = null, renameFrom: String? = null, renameTo: String? = null, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadZipResult>) {
        outerScope.launch {
            try {
                println("Run ${where()}")
                val result = runObserved(Dispatchers.IO, Parameters(fromUrl, toFile, entry, renameFrom, renameTo), observer)
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

        private lateinit var task: DownloadZipTask

        /**
         * Thread info
         */
        fun where(): String {
            return " @ ${Thread.currentThread()}"
        }

        /**
         * Start task
         *
         * @param fromUrl source url
         * @param toFile dest file
         * @param observer
         * @param resultConsumer
         */
        fun start(fromUrl: String, toFile: String, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadZipResult>) {
            task = DownloadZipTask()
            task.run(fromUrl, toFile, null, null, null, observer, resultConsumer)
        }

        /**
         * Start task
         *
         * @param fromUrl source url
         * @param toDir dest dir
         * @param entry entry
         * @param renameFrom rename source
         * @param renameTo rename dest
         * @param observer observer
         * @param resultConsumer result consumer
         */
        fun start(fromUrl: String, entry: String?, toDir: String, renameFrom: String?, renameTo: String?, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<DownloadZipResult>) {
            task = DownloadZipTask()
            task.run(fromUrl, toDir, entry, renameFrom, renameTo, observer, resultConsumer)
        }

        /**
         * Cancel task
         */
        fun cancel() {
            task.cancel()
        }
    }
}