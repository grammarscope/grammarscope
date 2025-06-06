package org.grammarscope.service.server.bound.syntaxnet

import kotlinx.coroutines.launch
import org.depparse.Sentence
import org.grammarscope.result.Parceler
import org.grammarscope.service.server.BoundService
import org.grammarscope.syntaxnet.SyntaxnetEngine

class SyntaxnetBoundService : BoundService<Array<Sentence>>() {

    private lateinit var syntaxnetEngine: SyntaxnetEngine

    override fun onCreate() {
        super.onCreate()

        syntaxnetEngine = SyntaxnetEngine(this.applicationContext)
        syntaxnetEngine.isEmbedded = true
        provider = syntaxnetEngine
        parceler = Parceler()

        serviceScope.launch {
            syntaxnetEngine.loadAsync(syntaxnetEngine.modelDir.absolutePath)
        }
    }

    override fun onDestroy() {
        syntaxnetEngine.unload()
        super.onDestroy()
    }
}
