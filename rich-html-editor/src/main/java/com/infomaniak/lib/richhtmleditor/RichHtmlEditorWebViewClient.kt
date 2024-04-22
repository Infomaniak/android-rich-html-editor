package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper

open class RichHtmlEditorWebViewClient : WebViewClient() {

    private var html: String? = null
    private var subscribedStates: Set<TextFormat.EditorStatusCommand>? = null

    @CallSuper
    override fun onPageFinished(view: WebView, url: String?) = view.setupDocument()

    fun init(html: String, subscribedStates: Set<TextFormat.EditorStatusCommand>?) {
        this.html = html
        this.subscribedStates = subscribedStates
    }

    private fun WebView.setupDocument() {
        insertUserHtml()

        // TODO: Would it make sens to insert this inside the editor template?
        addScript(createSubscribedStatesScript())
        addScript(context.readAsset("command_status_listener.js"))
    }

    private fun WebView.insertUserHtml() { // TODO: Reuse `getEditor()`?
        evaluateJavascript("""document.getElementById("editor").innerHTML = `${html}`""", null)
    }

    private fun createSubscribedStatesScript(): String {
        val subscribedStates = subscribedStates ?: TextFormat.EditorStatusCommand.entries

        val stateCommands = mutableListOf<TextFormat.EditorStatusCommand>()
        val valueCommands = mutableListOf<TextFormat.EditorStatusCommand>()

        subscribedStates.forEach {
            if (it.commandType == TextFormat.CommandType.STATE) stateCommands.add(it) else valueCommands.add(it)
        }

        val firstLine = generateConstTable("stateCommands", stateCommands)
        val secondLine = generateConstTable("valueCommands", valueCommands)

        return "$firstLine\n$secondLine"
    }

    private fun generateConstTable(name: String, commands: Collection<TextFormat.EditorStatusCommand>): String {
        return commands.joinToString(prefix = "const $name = [ ", postfix = " ]", separator = ", ") { "'${it.argumentName}'" }
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
