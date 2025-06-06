package org.grammarscope.service.client.iface

/**
 * Interface to client
 *
 * @author Bernard Bou
 */
interface IClient {

    /**
     * Bind
     */
    fun bind()

    /**
     * Unbind
     */
    fun unbind()

    /**
     * Start
     */
    fun startService()

    /**
     * Stop
     */
    fun stopService()

    /**
     * Request
     *
     * @param args args
     */
    fun request(args: Array<String>)
}
