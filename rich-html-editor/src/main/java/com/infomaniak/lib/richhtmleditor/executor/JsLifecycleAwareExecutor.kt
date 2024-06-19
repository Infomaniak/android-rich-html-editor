package com.infomaniak.lib.richhtmleditor.executor

internal abstract class JsLifecycleAwareExecutor<T> {

    private var hasDomLoaded = false
    private val objectsWaitingForDom = mutableListOf<T>()

    fun executeWhenDomIsLoaded(value: T) = synchronized(lock = this) {
        if (hasDomLoaded) executeImmediately(value) else objectsWaitingForDom.add(value)
    }

    fun notifyDomLoaded() = synchronized(lock = this) {
        hasDomLoaded = true
        objectsWaitingForDom.forEach(::executeImmediately)
        objectsWaitingForDom.clear()
    }

    abstract fun executeImmediately(value: T)
}
