package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient


/**
 * A custom WebViewClient designed required by `RichHtmlEditorWebView` to work properly.
 *
 * The `RichHtmlEditorWebViewClient` handles the required mechanisms for the RichHtmlEditorWebView to work properly such as
 * subscribed states notifications to the native code.
 */
class RichHtmlEditorWebViewClient(private val onPageLoaded: () -> Unit) : WebViewClient() {
    override fun onPageFinished(webView: WebView, url: String?) = onPageLoaded()
}
