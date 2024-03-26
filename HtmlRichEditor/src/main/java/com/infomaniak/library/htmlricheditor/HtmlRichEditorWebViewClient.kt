package com.infomaniak.library.htmlricheditor

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper

open class HtmlRichEditorWebViewClient : WebViewClient() {
    @CallSuper
    override fun onPageFinished(view: WebView, url: String?) = view.setupDocument()

    private fun WebView.setupDocument() {
        enableEdition()
        addScript(context.readAsset("command_status_listener.js"))
    }

    private fun WebView.enableEdition() {
        evaluateJavascript("document.body.contentEditable = true", null)
    }

    private fun WebView.addScript(scriptCode: String) {
        val addScriptJs = """
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.text = `${scriptCode}`;

        document.head.appendChild(script);
        """.trimIndent()

        evaluateJavascript(addScriptJs, null)
    }
}
