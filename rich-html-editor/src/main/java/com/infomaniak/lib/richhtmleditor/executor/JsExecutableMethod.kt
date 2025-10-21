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
import com.infomaniak.lib.richhtmleditor.encodeArgsForJs

class JsExecutableMethod(
    private val methodName: String,
    private vararg val args: Any?,
    resultCallback: ((String) -> Unit)? = null,
) {
    private val callbacks: MutableList<(String) -> Unit> = resultCallback?.let { mutableListOf(it) } ?: mutableListOf()

    fun executeOn(webView: WebView) {
        val formattedArgs = args.joinToString(transform = ::encodeArgsForJs)
        val jsCode = "$methodName($formattedArgs)"

        val evaluationCallback: ((String) -> Unit)? = if (callbacks.isEmpty()) {
            null
        } else {
            { jsExecutionOutput ->
                callbacks.forEach { callback -> callback(jsExecutionOutput) }
            }
        }

        webView.evaluateJavascript(jsCode, evaluationCallback)
    }

    fun addCallback(callback: (String) -> Unit) {
        callbacks.add(callback)
    }
}
