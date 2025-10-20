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

internal fun WebView.injectScript(scriptCode: String, id: String? = null) {
    val escapedStringLiteralId = id?.let { looselyEscapeAsStringLiteralForJs(it) }

    val removePreviousId = escapedStringLiteralId?.let {
        """
        var previousScript = document.getElementById($it)
        if (previousScript) previousScript.remove()
        """.trimIndent()
    } ?: ""
    val setId = escapedStringLiteralId?.let { "script.id = ${it};" } ?: ""

    val escapedStringLiteralScriptCode = looselyEscapeAsStringLiteralForJs(scriptCode)
    val addScriptJs = """
        var script = document.createElement('script');
        script.type = 'text/javascript';
        script.text = $escapedStringLiteralScriptCode;
        $setId

        document.head.appendChild(script);
        """.trimIndent()

    val code = removePreviousId + "\n" + addScriptJs

    evaluateJavascript(code, null)
}

internal fun WebView.injectCss(css: String) {
    val escapedStringLiteralCss = looselyEscapeAsStringLiteralForJs(css)
    val addCssJs = """
        var style = document.createElement('style');
        style.textContent = $escapedStringLiteralCss;

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
