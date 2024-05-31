package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient


/**
 * A custom WebViewClient used to notify `RichHtmlEditorWebView` when the template has loaded. It is required by
 * `RichHtmlEditorWebView` to call [RichHtmlEditorWebView.notifyPageHasLoaded] so `RichHtmlEditorWebView` works properly.
 */
class RichHtmlEditorWebViewClient(private val onPageLoaded: () -> Unit) : WebViewClient() {
    override fun onPageFinished(webView: WebView, url: String?) = onPageLoaded()
}
