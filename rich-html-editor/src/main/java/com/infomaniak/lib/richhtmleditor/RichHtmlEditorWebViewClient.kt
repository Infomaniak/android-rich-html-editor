package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.CallSuper

open class RichHtmlEditorWebViewClient : WebViewClient() {
    private var subscribedStates: Set<TextFormat.EditorStatusCommand>? = null

    @CallSuper
    override fun onPageFinished(view: WebView, url: String?) = view.setupDocument()

    fun subscribeToEditorStates(subscribedStates: Set<TextFormat.EditorStatusCommand>?) {
        this.subscribedStates = subscribedStates
    }

    private fun WebView.setupDocument() {
        enableEdition()
        addScript(createSubscribedStatesScript())
        addScript(context.readAsset("command_status_listener.js"))
    }

    private fun createSubscribedStatesScript(): String {
        val subscribedStates = subscribedStates ?: TextFormat.EditorStatusCommand.entries

        val stateCommands = mutableListOf<TextFormat.EditorStatusCommand>()
        val valueCommands = mutableListOf<TextFormat.EditorStatusCommand>()

        subscribedStates.forEach {
            if (it.commandType == TextFormat.CommandType.STATE) stateCommands.add(it) else valueCommands.add(it)
        }

        val firstLine = stateCommands.joinToString(prefix = "const stateCommands = [ ", postfix = " ]", separator = ", ") { "'${it.argumentName}'" }
        val secondLine = valueCommands.joinToString(prefix = "const valueCommands = [ ", postfix = " ]", separator = ", ") { "'${it.argumentName}'" }

        return "$firstLine\n$secondLine"
    }

    private fun WebView.enableEdition() {
        evaluateJavascript("document.body.contentEditable = true", null)
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
