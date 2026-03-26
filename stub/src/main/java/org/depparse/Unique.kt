package org.depparse

open class Unique<T> {

    @Volatile
    private var instance: T? = null

    fun interface Factory<T> {

        fun make(): T
    }

    fun get(): T? {
        return instance
    }

    @Synchronized
    fun consume(): T? {
        val instance: T? = instance
        this.instance = null
        return instance
    }

    fun make(factory: Factory<out T>): Boolean {
        if (instance == null) {
            synchronized(this) {
                if (instance == null) {
                    instance = factory.make()
                    return true
                }
            }
        }
        return false
    }
}
