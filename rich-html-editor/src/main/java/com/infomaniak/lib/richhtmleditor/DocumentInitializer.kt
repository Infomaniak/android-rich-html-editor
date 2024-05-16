package com.infomaniak.lib.richhtmleditor 

import android.webkit.WebView

internal class DocumentInitializer {

    private var html: String? = null
    private var subscribedStates: Set<TextFormat.StatusCommand>? = null
    private var customCss: List<String> = emptyList()
    private var customScripts: List<String> = emptyList()

    fun init(
        html: String,
        subscribedStates: Set<TextFormat.StatusCommand>?,
        customCss: List<String>?,
        customScripts: List<String>?,
    ) {
        this.html = html
        this.subscribedStates = subscribedStates
        customCss?.let { this.customCss = it }
        customScripts?.let { this.customScripts = it }
    }

    fun setupDocument(webView: WebView) = with(webView) {
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
