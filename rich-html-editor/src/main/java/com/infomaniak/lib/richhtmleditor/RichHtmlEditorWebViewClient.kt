package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient


/**
 * A custom [WebViewClient] used to notify the [RichHtmlEditorWebView] editor of when the template has finished loading.
 * [RichHtmlEditorWebView.notifyPageHasLoaded] needs to be called inside [WebViewClient.onPageFinished] so the editor can work properly.
 */
class RichHtmlEditorWebViewClient(private val onPageLoaded: () -> Unit) : WebViewClient() {
    override fun onPageFinished(webView: WebView, url: String?) = onPageLoaded()
}
