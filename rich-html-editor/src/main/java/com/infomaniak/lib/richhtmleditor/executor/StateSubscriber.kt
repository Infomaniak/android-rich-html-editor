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
package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView
import com.infomaniak.lib.richhtmleditor.StatusCommand
import com.infomaniak.lib.richhtmleditor.StatusType
import com.infomaniak.lib.richhtmleditor.injectScript

internal class StateSubscriber(private val webView: WebView) : JsLifecycleAwareExecutor<Set<StatusCommand>?>() {

    override fun executeImmediately(value: Set<StatusCommand>?) {
        webView.injectScript(createSubscribedStatesScript(value), "subscribedStates")
    }

    private fun createSubscribedStatesScript(inputSubscribedStates: Set<StatusCommand>?): String {
        val subscribedStates = inputSubscribedStates ?: StatusCommand.entries

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
        val reportLinkStatusLine = "var REPORT_LINK_STATUS = $areLinksSubscribedTo"

        return "$firstLine\n$secondLine\n$reportLinkStatusLine"
    }

    private fun generateConstTable(name: String, commands: Collection<StatusCommand>): String {
        return commands.joinToString(prefix = "var $name = [ ", postfix = " ]") { "'${it.argumentName}'" }
    }
}
