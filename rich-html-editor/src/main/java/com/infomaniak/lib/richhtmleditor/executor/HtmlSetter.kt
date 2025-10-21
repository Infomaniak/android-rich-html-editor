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
import com.infomaniak.lib.richhtmleditor.RichHtmlEditorWebView.Companion.EDITOR_ID
import com.infomaniak.lib.richhtmleditor.looselyEscapeAsStringLiteralForJs

internal class HtmlSetter(private val webView: WebView) : JsLifecycleAwareExecutor<String>() {

    override fun executeImmediately(value: String) = webView.insertUserHtml(value)

    private fun WebView.insertUserHtml(html: String) {
        val escapedHtmlStringLiteral = looselyEscapeAsStringLiteralForJs(html)
        evaluateJavascript(
            """document.getElementById("$EDITOR_ID").innerHTML = $escapedHtmlStringLiteral""",
            null
        )
    }
}
