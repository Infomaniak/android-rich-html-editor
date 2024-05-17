package com.infomaniak.lib.richhtmleditor 

import android.webkit.WebView

class JsExecutor(private val webView: WebView) {

    private var hasDomLoaded = false
    private val methodWaitingForDom = mutableListOf<JsExecutableMethod>()

    fun executeWhenDomIsLoaded(code: String, callback: ((String) -> Unit)? = null) {
        val method = JsExecutableMethod(code, callback)
        if (hasDomLoaded) method.executeOn(webView) else methodWaitingForDom.add(method)
    }

    fun notifyDomLoaded() {
        hasDomLoaded = true
        methodWaitingForDom.forEach { it.executeOn(webView) }
        methodWaitingForDom.clear()
    }
}
