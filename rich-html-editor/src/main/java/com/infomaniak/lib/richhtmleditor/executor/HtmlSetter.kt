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

internal class HtmlSetter(private val webView: WebView) : JsLifecycleAwareExecutor<String>() {

    override fun executeImmediately(value: String) = webView.insertUserHtml(value)

    private fun WebView.insertUserHtml(html: String) {
        evaluateJavascript("""document.getElementById("$EDITOR_ID").innerHTML = `${html}`""", null)
    }

    companion object {
        // The id of this HTML tag is shared across multiple files and needs to remain the same
        private const val EDITOR_ID = "editor"
    }
}
