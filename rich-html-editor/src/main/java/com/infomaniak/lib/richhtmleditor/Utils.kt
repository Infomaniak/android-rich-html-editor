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
internal fun looselyEscapeStringForJs(string: String, stringDelimiterChar: String): String {
    return string
        .replace("""\""", """\\""")
        .replace(stringDelimiterChar, """\${stringDelimiterChar}""")
        .replace("\n", "")
        .replace("\r", "")
}
