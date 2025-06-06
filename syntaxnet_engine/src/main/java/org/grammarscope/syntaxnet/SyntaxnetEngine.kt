package org.grammarscope.syntaxnet

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.depparse.Broadcast
import org.depparse.IAsyncLoading
import org.depparse.IEngine
import org.depparse.IProvider
import org.depparse.Sentence
import org.depparse.Storage
import org.syntaxnet2.JNI2
import java.io.File
import java.util.function.Consumer

class SyntaxnetEngine(private val context: Context) : IEngine<Array<Sentence>>, IAsyncLoading, Consumer<Long?> {

    private var handle: Long? = null
    override var isEmbedded = false
    val modelDir: File
        get() = Storage.getAppStorage(context)

    override fun load(modelPath: String) {
        Log.i(TAG, "loading from $modelPath")
        handle = JNI2.load(modelPath)
        Log.d(TAG, "loaded $handle")
    }

    override suspend fun loadAsync(modelPath: String) {
        Log.i(TAG, "Loading from $modelPath")
        Loader().runAndConsumeResult(Dispatchers.IO, modelPath) { result: Long? -> accept(result) }
    }

    override fun accept(handle: Long?) {
        Log.i(TAG, "Loaded $handle")
        this.handle = handle
        val event: Broadcast.EventType = if (handle != null) {
            if (isEmbedded) Broadcast.EventType.EMBEDDED_LOADED else Broadcast.EventType.LOADED
        } else {
            if (isEmbedded) Broadcast.EventType.EMBEDDED_LOADED_FAILURE else Broadcast.EventType.LOADED_FAILURE
        }
        broadcastEvent(event.name)
    }

    override fun unload() {
        if (handle == null) {
            Log.e(TAG, "Unloading exception (not initialized)")
            return
        }
        Log.i(TAG, "Unloading $handle")
        JNI2.unload(handle!!)
        handle = null
        broadcastEvent(if (isEmbedded) Broadcast.EventType.EMBEDDED_UNLOADED.name else Broadcast.EventType.UNLOADED.name)
        Log.i(TAG, "Unloaded")
    }

    @Throws(IllegalStateException::class)
    override fun process(args: Array<String>): Array<Sentence> {
        if (handle == null) {
            Log.e(TAG, "Trying to process while not initialized.")
            throw IllegalStateException("Trying to process while not initialized.")
        }
        Log.d(TAG, "Processing $handle")
        val result = JNI2.parse(handle!!, args)
        Log.d(TAG, "Processed $handle")
        return result
    }

    /**
     * Send broadcast from activity to all receivers listening to the action
     */
    private fun broadcastEvent(event: String) {
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.action = Broadcast.BROADCAST_LISTEN
        intent.putExtra(Broadcast.BROADCAST_LISTEN_EVENT, event)
        context.sendBroadcast(intent)
    }

    override fun kill() {
        unload()
    }

    override fun version(): String {
        val version = JNI2.version()
        return version.toString()
    }

    override fun getStatus(): Int {
        return if (handle != null) IProvider.STATUS_LOADED else 0
    }

    override fun getVersion(): String {
        return version()
    }

    companion object {

        private const val TAG = "Engine"

        init {
            JNI2.init()
        }

        /**
         * Factory (dynamically called)
         *
         * @param context context
         * @return engine
         */
        @Suppress("unused")
        fun make(context: Context): IEngine<Array<Sentence>> {
            Log.d(TAG, "Making engine")
            val engine = SyntaxnetEngine(context)
            val dir = engine.modelDir
            runBlocking {
                engine.loadAsync(dir.absolutePath)
            }
            return engine
        }
    }
}
