package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView

class JsExecutableMethod(
    private val methodName: String,
    private vararg val args: Any?,
    private val callback: ((String) -> Unit)? = null,
) {
    fun executeOn(webView: WebView) {
        val formattedArgs = if (args.isNotEmpty()) {
            "'" + args.joinToString("', '", transform = ::encodeArgsForJs) + "'"
        } else {
            ""
        }

        val jsCode = "$methodName($formattedArgs)"

        webView.evaluateJavascript(jsCode, callback)
    }

    companion object {
        private fun encodeArgsForJs(value: Any?): String {
            return when (value) {
                null -> "null"
                is String -> looselyEscapeStringForJs(value)
                else -> throw NotImplementedError("Encoding ${value::class} for JS is not yet implemented")
            }
        }

        // TODO: This method might not be enough to escape user inputs and prevent access to js code execution
        private fun looselyEscapeStringForJs(string: String): String {
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
    }
}
