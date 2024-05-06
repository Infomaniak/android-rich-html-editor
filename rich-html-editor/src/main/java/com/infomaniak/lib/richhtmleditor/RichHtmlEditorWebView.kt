package com.infomaniak.lib.richhtmleditor

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

class RichHtmlEditorWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    val textFormat = TextFormat(this, ::notifyExportedHtml)
    private val richHtmlEditorWebViewClient = RichHtmlEditorWebViewClient()

    private var htmlExportCallback: ((html: String) -> Unit)? = null

    init {
        settings.javaScriptEnabled = true
        isFocusableInTouchMode = true
        webViewClient = richHtmlEditorWebViewClient

        addJavascriptInterface(textFormat, "editor")
    }

    // TODO: Doc
    /**
     * subscribedStates: set of the EditorStatusCommand that the TextFormatter needs to detect. null means everything is detected
     * */
    fun setHtml(
        html: String = "",
        subscribedStates: Set<TextFormat.EditorStatusCommand>? = null,
        customCss: List<String> = emptyList(),
    ) {
        richHtmlEditorWebViewClient.init(html, subscribedStates, customCss)

        val template = context.readAsset("editor_template.html")
        super.loadDataWithBaseURL("", template, "text/html", "UTF-8", null)
    }

    // TODO: Find the best way to notify of new html
    fun exportHtml(callback: (html: String) -> Unit) {
        htmlExportCallback = callback
        evaluateJavascript("exportHtml()", null)
    }

    private fun notifyExportedHtml(html: String) {
        htmlExportCallback?.invoke(html)
        htmlExportCallback = null
    }

    // TODO : Use correct message in deprecated annotation
    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.richhtmleditor"))
    override fun loadUrl(url: String) = super.loadUrl(url)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.richhtmleditor"))
    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) =
        super.loadUrl(url, additionalHttpHeaders)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.richhtmleditor"))
    override fun loadData(data: String, mimeType: String?, encoding: String?) = super.loadData(data, mimeType, encoding)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.lib.richhtmleditor"))
    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) =
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
}
