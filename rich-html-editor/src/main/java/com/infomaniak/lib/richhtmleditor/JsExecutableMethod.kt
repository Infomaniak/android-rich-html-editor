package com.infomaniak.lib.richhtmleditor 

import android.webkit.WebView

class JsExecutableMethod(
    private val jsCode: String = "",
    private val callback: ((String) -> Unit)? = null,
) {
    fun executeOn(webView: WebView) = webView.evaluateJavascript(jsCode, callback)
}
