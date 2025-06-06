package org.depparse

interface Broadcast {

    enum class RequestType {
        NEW,
        KILL
    }

    enum class EventType {
        CONNECTED,
        CONNECTED_FAILURE,
        DISCONNECTED,
        BOUND,
        BOUND_FAILURE,
        UNBOUND,
        EMBEDDED_LOADED,
        EMBEDDED_LOADED_FAILURE,
        EMBEDDED_UNLOADED,
        LOADED,
        LOADED_FAILURE,
        UNLOADED
    }

    companion object {

        const val BROADCAST_ACTION = "org.grammmarscope.provider.ACTION"
        const val BROADCAST_ACTION_REQUEST = "org.grammmarscope.provider.REQUEST"
        const val BROADCAST_LISTEN = "org.grammmarscope.provider.CHANGE"
        const val BROADCAST_LISTEN_EVENT = "org.grammmarscope.provider.EVENT"
    }
}
