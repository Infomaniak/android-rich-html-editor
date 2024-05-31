package com.infomaniak.lib.richhtmleditor

import android.webkit.WebView

internal class DocumentInitializer {

    private var html: String? = null
    private var subscribedStates: Set<StatusCommand>? = null

    fun init(html: String, subscribedStates: Set<StatusCommand>?) {
        this.html = html
        this.subscribedStates = subscribedStates
    }

    fun setupDocument(webView: WebView) = with(webView) {
        insertUserHtml()

        injectScript(createSubscribedStatesScript())
        injectScript(context.readAsset("attach_listeners.js")) // Needs to only be called once the page has finished loading
    }

    private fun WebView.insertUserHtml() {
        evaluateJavascript("""document.getElementById("editor").innerHTML = `${html}`""", null)
    }

    private fun createSubscribedStatesScript(): String {
        val subscribedStates = subscribedStates ?: StatusCommand.entries

        val stateCommands = mutableListOf<StatusCommand>()
        val valueCommands = mutableListOf<StatusCommand>()

        subscribedStates.forEach {
            when (it.statusType) {
                StatusType.STATE -> stateCommands.add(it)
                StatusType.VALUE -> valueCommands.add(it)
                StatusType.COMPLEX -> Unit
            }
        }

        val firstLine = generateConstTable("stateCommands", stateCommands)
        val secondLine = generateConstTable("valueCommands", valueCommands)

        val areLinksSubscribedTo = subscribedStates.contains(StatusCommand.CREATE_LINK)
        val reportLinkStatusLine = "const REPORT_LINK_STATUS = $areLinksSubscribedTo"

        return "$firstLine\n$secondLine\n$reportLinkStatusLine"
    }

    private fun generateConstTable(name: String, commands: Collection<StatusCommand>): String {
        return commands.joinToString(prefix = "const $name = [ ", postfix = " ]", separator = ", ") { "'${it.argumentName}'" }
    }
}
