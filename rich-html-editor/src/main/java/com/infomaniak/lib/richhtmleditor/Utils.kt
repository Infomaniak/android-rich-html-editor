package com.infomaniak.lib.richhtmleditor

import android.content.Context
import java.io.BufferedReader

internal fun Context.readAsset(fileName: String): String {
    return assets
        .open(fileName)
        .bufferedReader()
        .use(BufferedReader::readText)
}

// TODO: This method might not be enough to escape user inputs and prevent access to js code execution
internal fun looselyEscapeStringForJs(string: String): String {
    val stringBuilder = StringBuilder()

    string.forEach {
        val char = when (it) {
            '"' -> "\\\""
            '\'' -> "\\'"
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\u000c' -> "\\u000c"
            else -> it
        }
        stringBuilder.append(char)
    }

    return stringBuilder.toString()
}
