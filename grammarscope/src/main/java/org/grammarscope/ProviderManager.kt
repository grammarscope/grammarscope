package org.grammarscope

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import org.depparse.Broadcast
import org.depparse.IEngine
import org.depparse.IProvider
import org.depparse.Sentence
import org.depparse.Unique
import org.depparse.common.UniqueProvider
import org.grammarscope.common.R
import org.grammarscope.service.client.DepParseBoundClient
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

object ProviderManager : BroadcastReceiver() {

    enum class ProviderType {
        /** Engine wrapped as service */
        BOUND_SERVICE,

        /** Direct engine */
        ENGINE;

        companion object {

            fun getPref(context: Context): ProviderType {
                val providerTypeStr = PreferenceManager.getDefaultSharedPreferences(context).getString("pref_provider", BOUND_SERVICE.name)
                return valueOf(providerTypeStr!!)
            }
        }
    }

    private const val TAG = "Manager"

    init {
        Log.d(TAG, "Provider manager constructed")
    }

    // R E C E I V E R

    // r e c e i v e r   l i s t e n e r

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (Broadcast.BROADCAST_ACTION == action) {
            val arg = intent.getStringExtra(Broadcast.BROADCAST_ACTION_REQUEST)
            Log.d(TAG, "Received broadcast request $arg")
            val request = Broadcast.RequestType.valueOf(arg!!)
            when (request) {
                Broadcast.RequestType.NEW -> try {
                    newProvider(context)
                } catch (_: RuntimeException) {
                }

                Broadcast.RequestType.KILL -> killProvider()
            }
        }
    }

    // a c t i o n s

    /**
     * New provider
     *
     * @param context context context
     */
    @Throws(RuntimeException::class)
    private fun newProvider(context: Context) {
        val providerIsService = ProviderType.getPref(context) == ProviderType.BOUND_SERVICE

        // provider factory
        val factory: Unique.Factory<IProvider<Array<Sentence>>> = if (providerIsService) makeClientFactory(context) else makeEngineFactory(context)

        // provider
        val made = UniqueProvider.SINGLETON.make(factory)
        Log.i(TAG, "Provider " + if (made) "created" else "already created")
    }

    /**
     * Kill provider
     */
    private fun killProvider() {
        UniqueProvider.SINGLETON.kill()
        Log.i(TAG, "Provider killed")
    }

    // f i r e

    fun requestNew(context: Context) {
        broadcastRequest(context, Broadcast.RequestType.NEW)
    }

    fun requestKill(context: Context) {
        broadcastRequest(context, Broadcast.RequestType.KILL)
    }

    private fun broadcastRequest(context: Context, request: Broadcast.RequestType) {
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.action = Broadcast.BROADCAST_ACTION
        intent.putExtra(Broadcast.BROADCAST_ACTION_REQUEST, request.name)
        context.sendBroadcast(intent)
    }

    private fun broadcastEvent(context: Context, event: Broadcast.EventType) {
        val intent = Intent()
        intent.setPackage(context.packageName)
        intent.action = Broadcast.BROADCAST_LISTEN
        intent.putExtra(Broadcast.BROADCAST_LISTEN_EVENT, event.name)
        context.sendBroadcast(intent)
    }

    // F A C T O R I E S

    private fun makeClientFactory(context: Context): Unique.Factory<IProvider<Array<Sentence>>> {
        // Service package
        val packageName = context.packageName

        // Service name
        val serviceName = packageName + '/' + context.resources.getString(R.string.bound_service)
        return Unique.Factory {
            val client = DepParseBoundClient(context, serviceName)
            client.startService()
            client.bind()
            client
        }
    }

    @Throws(RuntimeException::class)
    private fun makeEngineFactory(context: Context): Unique.Factory<IProvider<Array<Sentence>>> {
        return Unique.Factory { instantiateEngineFactory(context) }
    }

    @Throws(RuntimeException::class)
    private fun instantiateEngineFactory(context: Context): IEngine<Array<Sentence>> {
        val name = context.getString(R.string.engine_class)
        Log.i(TAG, "Engine factory from class $name")
        return try {
            invokeCompanionFunction(context, name, "make")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create engine factory from class $name", e)
            throw RuntimeException("Failed to create engine factory from class $name")
        }
    }

    private fun invokeCompanionFunction(context: Context, className: String, @Suppress("SameParameterValue") functionName: String): IEngine<Array<Sentence>> {
        val kClass = Class.forName(className).kotlin
        val companion = kClass.companionObject
        val companionInstance = kClass.companionObjectInstance
        val function = companion!!.functions.firstOrNull { it.name == functionName }
        @Suppress("UNCHECKED_CAST")
        return function?.call(companionInstance, context) as IEngine<Array<Sentence>>
    }

    // I N F O R M A T I O N

    fun providerIsEngineOrNull(engine: IProvider<Array<Sentence?>?>?): Boolean {
        return engine == null || !engine.javaClass.simpleName.endsWith("Client")
    }

    fun providerToString(context: Context, provider: IProvider<Array<Sentence>>?): String {
        if (provider == null) {
            return context.getString(R.string.provider_none)
        }
        val status = provider.getStatus()
        val attrs: MutableList<String> = ArrayList()
        val isEmbedded = status and IProvider.STATUS_EMBEDDED != 0
        attrs.add(context.getString(if (isEmbedded) R.string.provider_service else R.string.provider_engine))
        if (isEmbedded) {
            val isBound = status and IProvider.STATUS_BOUND != 0
            attrs.add(context.getString(if (isBound) R.string.provider_bound else R.string.provider_unbound))
        }
        val isLoaded = status and IProvider.STATUS_LOADED != 0
        attrs.add(context.getString(if (isLoaded) R.string.provider_loaded else R.string.provider_unloaded))
        val sb = StringBuilder()
        var first = true
        for (attr in attrs) {
            if (first) {
                first = false
            } else {
                sb.append(',').append(' ')
            }
            sb.append(attr)
        }
        sb.append('.')
        return context.getString(R.string.provider_is, sb)
    }

    fun engineVersion(): String {
        val provider = UniqueProvider.SINGLETON.get()
        return provider?.getVersion() ?: "unknown"
    }
}
