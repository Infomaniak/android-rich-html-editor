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

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.AbsSavedState
import android.webkit.WebView
import java.io.BufferedReader

internal fun Context.readAsset(fileName: String): String {
    return assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)
}

internal fun WebView.injectScript(scriptCode: String) {
    val addScriptJs = """
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.text = `${scriptCode}`;

        document.head.appendChild(script);
        """.trimIndent()

    evaluateJavascript(addScriptJs, null)
}

internal fun WebView.injectCss(css: String) {
    val addCssJs = """
        var style = document.createElement('style');
        style.textContent = `${css}`;

        document.head.appendChild(style);
        """.trimIndent()

    evaluateJavascript(addCssJs, null)
}

fun Bundle.getParcelableCompat(key: String, clazz: Class<AbsSavedState>): AbsSavedState? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelable(key, clazz)
    } else {
        getParcelable(key)
    }
}
