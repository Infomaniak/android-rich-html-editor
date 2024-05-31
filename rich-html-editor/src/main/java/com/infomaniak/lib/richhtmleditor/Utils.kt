package com.infomaniak.lib.richhtmleditor

import android.content.Context
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
