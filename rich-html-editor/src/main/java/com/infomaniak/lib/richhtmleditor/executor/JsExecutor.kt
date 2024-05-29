package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView
import com.infomaniak.lib.richhtmleditor.JsExecutableMethod

internal class JsExecutor(private val webView: WebView) : JsLifecycleAwareExecutor<JsExecutableMethod>() {

    override fun executeImmediately(value: JsExecutableMethod) {
        value.executeOn(webView)
    }
}