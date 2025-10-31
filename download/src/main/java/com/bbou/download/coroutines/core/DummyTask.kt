package com.bbou.download.coroutines.core

import com.bbou.coroutines.ProgressEmitter
import com.bbou.coroutines.Task
import com.bbou.download.coroutines.core.DummyTask.Helpers.longBlocking
import com.bbou.download.coroutines.core.DummyTask.Helpers.where
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import java.util.function.Consumer
import kotlin.coroutines.cancellation.CancellationException

/**
 * Dummy download task : does not download anything
 */
class DummyTask : Task<DummyTask.Parameters, Pair<Long, Long>, Boolean?>() {

    /**
     * Parameters
     *
     * @property fromUrl dummy source url
     * @property toFile dummy dest file
     * @property entry dummy entry
     * @property renameFrom dummy rename source
     * @property renameTo dummy rename dest
     */
    data class Parameters(val fromUrl: String, val toFile: String, val entry: String? = null, val renameFrom: String? = null, val renameTo: String? = null)

    private suspend fun jobBody(emitter: ProgressEmitter<Pair<Long, Long>>, params: Parameters): Boolean? {

        val job = currentCoroutineContext()[Job] ?: return null
        val from = params.fromUrl
        val to = params.toFile
        println("Job> $from $to ${where()}")
        val max = 20
        for (count in 1..max) {
            job.ensureActive() // checks for cancellation
            longBlocking(1000L)
            emitter.emitProgress(count.toLong() * 1000000L to max.toLong() * 1000000L)
            yield() // checks for cancellation
        }
        println("Job< ${where()}")
        return true
    }

    override suspend fun doJob(params: Parameters): Boolean? {
        return jobBody(this, params)
    }

    private val outerScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    /**
     * Run
     *
     * @property fromUrl dummy source url
     * @property toFile dummy dest file
     * @property entry dummy entry
     * @property renameFrom dummy rename source
     * @property renameTo dummy rename dest
     * @param observer observer
     * @param resultConsumer result consumer
     */
    fun run(fromUrl: String, toFile: String, entry: String? = null, renameFrom: String? = null, renameTo: String? = null, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<Boolean?>) {
        outerScope.launch {
            try {
                println("Run ${where()}")
                val result = runObserved(Dispatchers.IO, Parameters(fromUrl, toFile, entry, renameFrom, renameTo), observer)
                println("Done '$result' ${where()}")
                resultConsumer.accept(result)
                println("End ${where()}")
            } catch (ce: CancellationException) {
                println("Caught $ce ${where()}")
                resultConsumer.accept(null)
            } finally {
                println("Exit ${where()}")
            }
        }
    }

    /**
     * Cancel
     */
    override fun cancel() {

        super.cancel()
        outerScope.cancel()
    }

    private object Helpers {

        fun where(): String {
            return " @ ${Thread.currentThread()}"
        }

        fun longBlocking(howLong: Long) {
            try {
                Thread.sleep(howLong)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }

    companion object {

        private lateinit var task: DummyTask

        /**
         * Start download
         *
         * @param fromUrl source url
         * @param toFile destination
         * @param observer observer
         * @param resultConsumer result consumer
         */
        fun start(fromUrl: String, toFile: String, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<Boolean?>) {
            task = DummyTask()
            task.run(fromUrl, toFile, null, null, null, observer, resultConsumer)
        }

        /**
         * Start download
         *
         * @param fromUrl source zip url
         * @param entry entry
         * @param toDir destination dir
         * @param renameFrom rename source
         * @param renameTo rename dest
         * @param observer observer
         * @param resultConsumer result consumer
         */
        fun start(fromUrl: String, entry: String?, toDir: String, renameFrom: String?, renameTo: String?, observer: Consumer<Pair<Long, Long>>, resultConsumer: Consumer<Boolean?>) {
            task = DummyTask()
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
