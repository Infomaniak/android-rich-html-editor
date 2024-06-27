/*
 * Infomaniak Rich HTML Editor - Android
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
        evaluateJavascript("""document.getElementById("$EDITOR_ID").innerHTML = `${html}`""", null)
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
        return commands.joinToString(prefix = "const $name = [ ", postfix = " ]") { "'${it.argumentName}'" }
    }

    companion object {
        // The id of this HTML tag is shared across multiple files and needs to remain the same
        private const val EDITOR_ID = "editor"
    }
}
