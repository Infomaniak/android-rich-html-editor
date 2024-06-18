package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView

internal class JsExecutor(private val webView: WebView) : JsLifecycleAwareExecutor<JsExecutableMethod>() {

    override fun executeImmediately(value: JsExecutableMethod) {
        value.executeOn(webView)
    }

    fun executeImmediatelyAndRefreshToolbar(method: JsExecutableMethod) {
        method.addCallback { JsExecutableMethod("reportSelectionStateChangedIfNecessary").executeOn(webView) }
        executeImmediately(method)
    }
}