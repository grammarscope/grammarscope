package org.grammarscope.service.server.bound.udpipe

import kotlinx.coroutines.launch
import org.depparse.Sentence
import org.grammarscope.result.Parceler
import org.grammarscope.service.server.BoundService
import org.grammarscope.udpipe.UDPipeEngine

class UDPipeBoundService : BoundService<Array<Sentence>>() {

    private lateinit var udPipeEngine: UDPipeEngine

    override fun onCreate() {
        super.onCreate()

        udPipeEngine = UDPipeEngine(this.applicationContext)
        udPipeEngine.isEmbedded = true
        provider = udPipeEngine
        parceler = Parceler()

        serviceScope.launch {
            udPipeEngine.loadAsync(udPipeEngine.modelDir.absolutePath)
        }
    }

    override fun onDestroy() {
        udPipeEngine.unload()
        super.onDestroy()
    }
}
