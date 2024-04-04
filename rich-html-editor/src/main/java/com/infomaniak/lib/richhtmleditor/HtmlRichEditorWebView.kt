package com.infomaniak.lib.richhtmleditor

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class HtmlRichEditorWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    val textFormat = TextFormat(this)

    private val htmlRichEditorWebViewClient = HtmlRichEditorWebViewClient()

    init {
        settings.javaScriptEnabled = true
        isFocusableInTouchMode = true
        webViewClient = htmlRichEditorWebViewClient

        addJavascriptInterface(textFormat, "editor")
    }

    /**
     * subscribedStates: set of the EditorStatusCommand that the TextFormatter needs to detect. null means everything is detected
     * */
    fun loadHtml(html: String = "", subscribedStates: Set<TextFormat.EditorStatusCommand>? = null) {
        htmlRichEditorWebViewClient.subscribeToEditorStates(subscribedStates)
        super.loadDataWithBaseURL("", html, "text/html", "UTF-8", null) // TODO
    }

    // TODO : Use correct message in deprecated annotation
    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.htmlricheditor"))
    override fun loadUrl(url: String) = super.loadUrl(url)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.htmlricheditor"))
    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) =
        super.loadUrl(url, additionalHttpHeaders)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.htmlricheditor"))
    override fun loadData(data: String, mimeType: String?, encoding: String?) = super.loadData(data, mimeType, encoding)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.htmlricheditor"))
    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) =
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
}
