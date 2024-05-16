package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper

/**
 * A custom WebViewClient designed required by `RichHtmlEditorWebView` to work properly.
 *
 * The `RichHtmlEditorWebViewClient` handles the required mechanisms for the RichHtmlEditorWebView to work properly such as
 * subscribed states notifications to the native code.
 */
open class RichHtmlEditorWebViewClient : WebViewClient() {

    private var html: String? = null
    private var subscribedStates: Set<TextFormat.StatusCommand>? = null
    private var customCss: List<String> = emptyList()
    private var customScripts: List<String> = emptyList()

    @CallSuper
    override fun onPageFinished(view: WebView, url: String?) = view.setupDocument()

    fun init(html: String, editorConfig: EditorConfig? = null) {
        this.html = html
        subscribedStates = editorConfig?.subscribedStates
        editorConfig?.let { customCss = it.customCss }
        editorConfig?.let { customScripts = it.customScripts }
    }

    private fun WebView.setupDocument() {
        insertUserHtml()

        addScript(createSubscribedStatesScript())
        addScript(context.readAsset("attach_listeners.js")) // Needs to only be called once the page has finished loading

        customCss.forEach { css -> addCss(css) }
        customScripts.forEach { script -> addScript(script) }
    }

    private fun WebView.insertUserHtml() {
        evaluateJavascript("""document.getElementById("editor").innerHTML = `${html}`""", null)
    }

    private fun createSubscribedStatesScript(): String {
        val subscribedStates = subscribedStates ?: TextFormat.StatusCommand.entries

        val stateCommands = mutableListOf<TextFormat.StatusCommand>()
        val valueCommands = mutableListOf<TextFormat.StatusCommand>()

        subscribedStates.forEach {
            when (it.statusType) {
                TextFormat.StatusType.STATE -> stateCommands.add(it)
                TextFormat.StatusType.VALUE -> valueCommands.add(it)
                TextFormat.StatusType.COMPLEX -> Unit
            }
        }

        val firstLine = generateConstTable("stateCommands", stateCommands)
        val secondLine = generateConstTable("valueCommands", valueCommands)

        val areLinksSubscribedTo = subscribedStates.contains(TextFormat.StatusCommand.CREATE_LINK)
        val reportLinkStatusLine = "const REPORT_LINK_STATUS = $areLinksSubscribedTo"

        return "$firstLine\n$secondLine\n$reportLinkStatusLine"
    }

    private fun generateConstTable(name: String, commands: Collection<TextFormat.StatusCommand>): String {
        return commands.joinToString(prefix = "const $name = [ ", postfix = " ]", separator = ", ") { "'${it.argumentName}'" }
    }

    private fun WebView.addCss(css: String) {
        val addCssJs = """
        var style = document.createElement('style');
        style.textContent = `${css}`;

        document.head.appendChild(style);
        """.trimIndent()

        evaluateJavascript(addCssJs, null)
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
