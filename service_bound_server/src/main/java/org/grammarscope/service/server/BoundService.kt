package org.grammarscope.service.server

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.depparse.IProvider
import org.grammarscope.service.IParceler
import org.grammarscope.service.iface.IServiceBinder

/**
 * Bound service
 *
 * @author Bernard Bou
 */
abstract class BoundService<R> : Service() {

    /**
     * Binder given to clients
     */
    class ServiceBinder<R> internal constructor(private val provider: IProvider<R>) : Binder(), IServiceBinder<R> {

        @Throws(IllegalStateException::class)
        override fun process(args: Array<String>): R {
            return provider.process(args)
        }

        override fun getStatus(): Int {
            return provider.getStatus() or IProvider.STATUS_EMBEDDED
        }

        override fun getVersion(): String {
            return provider.getVersion()
        }
    }

    /**
     * Abstract: provider
     */
    protected lateinit var provider: IProvider<R>

    /**
     * Abstract: parceler
     */
    protected lateinit var parceler: IParceler<R>

    /**
     * Coroutine scope
     */
    val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    /**
     * When binding to the service, we return an interface to the service
     */
    override fun onBind(intent: Intent): IBinder? {
        Log.d(TAG, "Binding service")
        return ServiceBinder(provider)
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {

        private const val TAG = "SBound"
    }
}
