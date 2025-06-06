package org.grammarscope.service.client.iface

/**
 * IResult consumer interface
 *
 * @author Bernard Bou
 */
interface IResultSink<R> {

    /**
     * Result available from consumption callback
     *
     * @param result result
     */
    fun onResult(result: R?)
}
