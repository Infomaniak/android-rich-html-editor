package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper

open class RichHtmlEditorWebViewClient : WebViewClient() {

    private var html: String? = null
    private var subscribedStates: Set<TextFormat.EditorStatusCommand>? = null
    private var customCss: List<String> = emptyList()

    @CallSuper
    override fun onPageFinished(view: WebView, url: String?) = view.setupDocument()

    fun init(html: String, subscribedStates: Set<TextFormat.EditorStatusCommand>?, customCss: List<String>) {
        this.html = html
        this.subscribedStates = subscribedStates
        this.customCss = customCss
    }

    private fun WebView.setupDocument() {
        insertUserHtml()

        customCss.forEach { css -> addCss(css) }

        addScript(context.readAsset("link_detection.js"))

        addScript(createSubscribedStatesScript())
        addScript(context.readAsset("command_status_listener.js"))
    }

    private fun WebView.insertUserHtml() {
        evaluateJavascript("""document.getElementById("editor").innerHTML = `${html}`""", null)
    }

    private fun createSubscribedStatesScript(): String {
        val subscribedStates = subscribedStates ?: TextFormat.EditorStatusCommand.entries

        val stateCommands = mutableListOf<TextFormat.EditorStatusCommand>()
        val valueCommands = mutableListOf<TextFormat.EditorStatusCommand>()

        subscribedStates.forEach {
            when (it.commandType) {
                TextFormat.CommandType.STATE -> stateCommands.add(it)
                TextFormat.CommandType.VALUE -> valueCommands.add(it)
                TextFormat.CommandType.COMPLEX -> Unit
            }
        }

        val firstLine = generateConstTable("stateCommands", stateCommands)
        val secondLine = generateConstTable("valueCommands", valueCommands)
        val reportLinkStatusLine = "const REPORT_LINK_STATUS = ${subscribedStates.contains(TextFormat.EditorStatusCommand.CREATE_LINK)}"

        return "$firstLine\n$secondLine\n$reportLinkStatusLine"
    }

    private fun generateConstTable(name: String, commands: Collection<TextFormat.EditorStatusCommand>): String {
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
