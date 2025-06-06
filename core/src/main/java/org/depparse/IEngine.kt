package org.depparse

interface IEngine<R> : IProvider<R> {

    fun load(modelPath: String)

    fun unload()

    fun version(): String

    @Suppress("SameReturnValue")
    val isEmbedded: Boolean
}
