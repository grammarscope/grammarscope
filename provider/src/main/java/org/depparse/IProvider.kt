package org.depparse

interface IProvider<R> {

    @Throws(IllegalStateException::class)
    fun process(args: Array<String>): R

    fun kill()

    fun getStatus(): Int

    fun getVersion(): String

    companion object {

        const val STATUS_BOUND = 0x00000001
        const val STATUS_LOADED = 0x00000002
        const val STATUS_EMBEDDED = 0x10000000
    }
}
