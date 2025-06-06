package org.grammarscope.service.iface

/**
 * Service interface used in client/service interaction
 *
 * @author Bernard Bou
 */
interface IServiceConstants {

    companion object {

        // action in intent
        @Suppress("unused")
        const val ACTION_PROCESS = "process"

        // arguments (key in bundle)
        @Suppress("unused")
        const val KEY_PARAMS = "params"
        @Suppress("unused")
        const val KEY_RECEIVER = "receiver"
        @Suppress("unused")
        const val KEY_RESULT = "result"

        // messaging service code
        @Suppress("unused")
        const val MSG_REGISTER_CLIENT = 1
        @Suppress("unused")
        const val MSG_UNREGISTER_CLIENT = 2
        @Suppress("unused")
        const val MSG_REQUEST = 3
        @Suppress("unused")
        const val MSG_RESULT = 4
    }
}
