package org.grammarscope.service.server.bound.corenlp

import kotlinx.coroutines.launch
import org.depparse.Sentence
import org.grammarscope.result.Parceler
import org.grammarscope.service.server.BoundService
import org.grammarscope.corenlp.CoreNlpEngine

class CoreNlpBoundService : BoundService<Array<Sentence>>() {

    private lateinit var coreNlpEngine: CoreNlpEngine

    override fun onCreate() {
        super.onCreate()

        coreNlpEngine = CoreNlpEngine(this.applicationContext)
        coreNlpEngine.isEmbedded = true
        provider = coreNlpEngine
        parceler = Parceler()

        serviceScope.launch {
            coreNlpEngine.loadAsync(coreNlpEngine.modelDir.absolutePath)
        }
    }

    override fun onDestroy() {
        coreNlpEngine.unload()
        super.onDestroy()
    }
}
