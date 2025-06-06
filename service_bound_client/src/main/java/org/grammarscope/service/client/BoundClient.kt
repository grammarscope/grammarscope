package org.grammarscope.service.client

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import org.depparse.Broadcast
import org.depparse.IProvider
import org.grammarscope.service.client.iface.IClient2
import org.grammarscope.service.client.iface.IResultSink
import org.grammarscope.service.iface.IServiceBinder

/**
 * Bound client
 *
 * @author Bernard Bou
 */
abstract class BoundClient<R>(private val context: Context, service0: String) : IClient2<R>, ServiceConnection {

    /**
     * Service component
     */
    private val component: ComponentName

    /**
     * Connection state
     */
    @Volatile
    private var isConnected = false

    /**
     * Binder
     */
    private var binder: IServiceBinder<R>? = null

    /**
     * Result listener
     */
    private var resultSink: IResultSink<R>?

    init {
        val serviceNameComponents = service0.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        component = ComponentName(serviceNameComponents[0], serviceNameComponents[1])
        resultSink = null
    }

    fun setResultSink(resultSink0: IResultSink<R>?) {
        resultSink = resultSink0
    }

    override fun bind() {
        Log.d(TAG, "Service binding")

        // Ensure each client connects only once.
        // This double-locking scheme works with JDK >= 5 and provided isConnected is volatile
        if (!isConnected) {
            synchronized(this) {
                if (!isConnected) {
                    bindService()
                    isConnected = true
                }
            }
        }
        if (isConnected) {
            broadcastEvent(Broadcast.EventType.CONNECTED)
        }
    }

    /**
     * Bind client to service
     */
    private fun bindService() {
        val intent = Intent()
        intent.component = component
        if (!context.bindService(intent, this, Context.BIND_AUTO_CREATE)) {
            Log.e(TAG, "Service failed to bind $component")
            broadcastEvent(Broadcast.EventType.BOUND_FAILURE)
        }
    }

    @Synchronized
    override fun unbind() {
        if (binder != null) {
            Log.d(TAG, "Service unbinding")

            // invalidate
            binder = null

            // detach our existing connection.
            context.unbindService(this)
            broadcastEvent(Broadcast.EventType.UNBOUND)
        }
    }

    // C O N N E C T I O N

    override fun onServiceConnected(name: ComponentName, binder0: IBinder) {
        Log.d(TAG, "Service bound $name")
        @Suppress("UNCHECKED_CAST")
        binder = binder0 as IServiceBinder<R>
        broadcastEvent(Broadcast.EventType.BOUND)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        Log.d(TAG, "Service disconnected $name")
        binder = null
        isConnected = false
        broadcastEvent(Broadcast.EventType.UNBOUND)
    }

    override fun onBindingDied(name: ComponentName) {
        Log.d(TAG, "Service died $name")
        binder = null
        isConnected = false
        broadcastEvent(Broadcast.EventType.BOUND_FAILURE)
    }

    // P R O C E S S

    @Throws(IllegalStateException::class)
    override fun process(args: Array<String>): R {
        return binder!!.process(args)
    }

    override fun request(args: Array<String>) {
        if (resultSink != null) {
            resultSink!!.onResult(binder!!.process(args))
        }
    }

    override fun getStatus(): Int {
        return if (binder == null) {
            0
        } else binder!!.getStatus() or IProvider.STATUS_BOUND
    }

    override fun getVersion(): String {
        return if (binder == null) {
            "unknown"
        } else binder!!.getVersion()
    }

    // L I F E C Y C L E

    override fun kill() {
        unbind()
        stopService()
    }

    // S T A R T / S T O P

    override fun startService() {
        // start
        val intent = Intent()
        intent.component = component
        val success = context.startService(intent) != null
        if (success) {
            Log.i(TAG, "Service started $component")
        } else {
            Log.e(TAG, "Service failed to start $component")
        }
    }

    override fun stopService() {
        val intent = Intent()
        intent.component = component
        val success = context.stopService(intent)
        if (success) {
            Log.i(TAG, "Service stopped $component")
        } else {
            Log.e(TAG, "Service failed to stop $component")
        }
    }

    // E V E N T S

    /**
     * Send broadcastEvent from activity to all receivers listening to the action
     */
    private fun broadcastEvent(event: Broadcast.EventType) {
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.action = Broadcast.BROADCAST_LISTEN
        intent.putExtra(Broadcast.BROADCAST_LISTEN_EVENT, event.name)
        context.sendBroadcast(intent)
    }

    companion object {

        private const val TAG = "CBound"
    }
}
