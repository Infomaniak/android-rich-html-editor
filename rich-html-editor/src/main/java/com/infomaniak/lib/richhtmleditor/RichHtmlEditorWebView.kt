package com.infomaniak.lib.richhtmleditor

import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

/**
 * A custom WebView designed to provide simple formatting and editing capabilities to an existing HTML content.
 *
 * The `RichHtmlEditorWebView` class utilizes the `contenteditable` attribute in HTML along with a combination of `execCommand`
 * and custom logic to enable editing and basic formatting of existing HTML.
 *
 * When [setHtml] is called, it inserts the provided HTML content into the RichHtmlEditorWebView's editor template HTML and
 * activates the necessary JavaScript mechanisms for the editor to update different format statuses and function properly.
 *
 * To interact with the editor, you can either listen to format status notifications or call methods to modify the current format
 * using [textFormat].
 */
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

    /**
     * Sets the HTML content to be displayed in the rich HTML editor.
     *
     * This method initializes the web view client with the provided HTML content, subscribed states, custom CSS, and custom
     * scripts. It then loads the HTML editor template into the web view. No HTML content is needed to have an empty editor;
     * calling the method without parameters will still initialize everything necessary for the editor to function.
     *
     * @param html The HTML content to be displayed. Defaults to an empty string.
     * @param subscribedStates A set of status commands to be subscribed to. Defaults to null, which means subscribing to all
     * available status commands.
     * @param customCss A list of custom CSS strings to be applied. Defaults to an empty list. These CSS styles will be loaded
     * when the page has finished loading. Supports new lines inside the CSS.
     * @param customScripts A list of custom scripts to be included. Defaults to an empty list. These scripts will be loaded when
     * the page has finished loading. Supports new lines inside the script.
     *
     * Example usage:
     * ```
     * val htmlContent = "<p>Hello, World!</p>"
     * val states = setOf(TextFormat.StatusCommand.BOLD, TextFormat.StatusCommand.ITALIC)
     * val css = listOf("body { background-color: #f0f0f0; }")
     * val scripts = listOf("console.log('Custom script loaded');")
     *
     * setHtml(htmlContent, states, css, scripts)
     * ```
     */

    fun setHtml(
        html: String = "",
        subscribedStates: Set<TextFormat.StatusCommand>? = null,
        customCss: List<String> = emptyList(),
        customScripts: List<String> = emptyList(),
    ) {
        // TODO: Provide a way to override the webview client without any issue
        richHtmlEditorWebViewClient.init(html, subscribedStates, customCss, customScripts)

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

    @Deprecated(
        "Use setHtml instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadUrl(url: String) = super.loadUrl(url)

    @Deprecated(
        "Use setHtml instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) =
        super.loadUrl(url, additionalHttpHeaders)

    @Deprecated(
        "Use setHtml instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadData(data: String, mimeType: String?, encoding: String?) = super.loadData(data, mimeType, encoding)

    @Deprecated(
        "Use setHtml instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) =
        super.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, historyUrl)
}
