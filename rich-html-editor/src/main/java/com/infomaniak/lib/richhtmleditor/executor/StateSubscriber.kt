/*
 * Infomaniak Rich HTML Editor - Android
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
