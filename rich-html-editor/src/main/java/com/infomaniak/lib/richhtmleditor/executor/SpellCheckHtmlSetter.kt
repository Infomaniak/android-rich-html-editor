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
import com.infomaniak.lib.richhtmleditor.RichHtmlEditorWebView.Companion.EDITOR_ID
import com.infomaniak.lib.richhtmleditor.encodeArgsForJs

internal class SpellCheckHtmlSetter(private val webView: WebView) : JsLifecycleAwareExecutor<Boolean>() {

    override fun executeImmediately(value: Boolean) = webView.insertSpellCheckValue(value)

    private fun WebView.insertSpellCheckValue(enable: Boolean) {
        evaluateJavascript(
            """document.getElementById("$EDITOR_ID").setAttribute("spellcheck", ${encodeArgsForJs(enable)})""",
            null
        )
    }
}
