package org.grammarscope.corenlp

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.corenlp.CoreNlp
import org.depparse.Broadcast
import org.depparse.IAsyncLoading
import org.depparse.IEngine
import org.depparse.IProvider
import org.depparse.Sentence
import org.depparse.Storage
import java.io.File
import java.util.function.Consumer

class CoreNlpEngine(private val context: Context) : IEngine<Array<Sentence>>, IAsyncLoading, Consumer<Long?> {

    private var handle: Long? = null
    override var isEmbedded = false

    val modelDir: File
        get() = Storage.getAppStorage(context)
    val neural: Boolean
        get() = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE).getBoolean(PREF_NEURAL, true)

    override fun load(modelPath: String) {
        Log.i(TAG, "Loading from $modelPath ${if (neural) "neural" else "constituency"}")
        this.handle = CoreNlp.load(modelPath, neural)
        Log.d(TAG, "Loaded $handle")
    }

    override suspend fun loadAsync(modelPath: String) {
        Log.i(TAG, "Loading from $modelPath ${if (neural) "neural" else "constituency"}")
        Loader(neural).runAndConsumeResult(Dispatchers.IO, modelPath) { result: Long? ->
            accept(result)
        }
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
            Log.e(TAG, "Unloading (not initialized)")
            return
        }
        Log.i(TAG, "Unloading $handle")
        CoreNlp.unload(handle!!)
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
        val result = CoreNlp.parse(args)
        Log.d(TAG, "Processed $handle")
        return result
    }

    /**
     * Send broadcast from activity to all receivers listening to the action "ENGINE"
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
        val version = CoreNlp.version()
        return (version shr 24 and 0xFF).toString() + "." + (version shr 16 and 0xFF) + "." + (version shr 8 and 0xFF) + "-" + (version and 0xFF)
    }

    override fun getStatus(): Int {
        return if (handle != null) IProvider.STATUS_LOADED else 0
    }

    override fun getVersion(): String {
        return version()
    }

    companion object {

        private const val TAG = "Engine"

        private const val PREF_FILE = "corenlp"

        private const val PREF_NEURAL = "neural"

        //init {
        //    CoreNlp.init()
        //}

        /**
         * Factory (dynamically called)
         *
         * @param context context
         * @return engine
         */
        @Suppress("unused")
        fun make(context: Context): IEngine<Array<Sentence>> {
            Log.d(TAG, "Making engine")
            val engine = CoreNlpEngine(context)
            val dir = engine.modelDir
            runBlocking {
                engine.loadAsync(dir.absolutePath)
            }
            return engine
        }
    }
}
