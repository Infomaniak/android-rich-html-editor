package com.infomaniak.library.htmlricheditor

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView
import android.webkit.WebViewClient

class HtmlRichEditorWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    val textFormat = TextFormat(this)

    init {
        settings.javaScriptEnabled = true
        isFocusableInTouchMode = true
        webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) = setupDocument()
        }

        addJavascriptInterface(textFormat, "editor")
    }

    fun loadHtml(html: String = "") {
        super.loadDataWithBaseURL("", html, "text/html", "UTF-8", null) // TODO
    }

    private fun setupDocument() {
        enableEdition()
        addScript(context.readAsset("commandStatusListener.js"))
    }

    private fun enableEdition() {
        evaluateJavascript("document.body.contentEditable = true", null)
    }

    private fun addScript(scriptCode: String) {
        val addScriptJs = """
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.text = `${scriptCode}`;

        document.head.appendChild(script);
        """.trimIndent()

        evaluateJavascript(addScriptJs, null)
    }

    // TODO : Use correct message in deprecated annotation
    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.library.htmlricheditor"))
    override fun loadUrl(url: String) = super.loadUrl(url)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.library.htmlricheditor"))
    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) =
        super.loadUrl(url, additionalHttpHeaders)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.library.htmlricheditor"))
    override fun loadData(data: String, mimeType: String?, encoding: String?) = super.loadData(data, mimeType, encoding)

    @Deprecated("Use loadHtml", ReplaceWith("this.loadHtml()", "com.infomaniak.library.htmlricheditor"))
    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) =
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
}
