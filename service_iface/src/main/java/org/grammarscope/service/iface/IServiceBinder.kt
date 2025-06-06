package org.grammarscope.service.iface

interface IServiceBinder<R> {

    /**
     * Get status
     *
     * @return status
     */
    fun getStatus(): Int

    /**
     * Get version
     *
     * @return version
     */
    fun getVersion(): String

    /**
     * Process args and return result to consumer
     *
     * @param args args
     */
    @Throws(IllegalStateException::class)
    fun process(args: Array<String>): R
}
