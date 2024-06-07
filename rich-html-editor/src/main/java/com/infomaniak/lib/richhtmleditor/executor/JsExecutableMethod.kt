package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView

class JsExecutableMethod(
    private val methodName: String,
    private vararg val args: Any?,
    callback: ((String) -> Unit)? = null,
) {
    private val callbacks: MutableList<(String) -> Unit> = callback?.let { mutableListOf(it) } ?: mutableListOf()

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

    companion object {
        private fun encodeArgsForJs(value: Any?): String {
            return when (value) {
                null -> "null"
                is String -> "'${looselyEscapeStringForJs(value)}'"
                is Boolean -> value.toString()
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
                    else -> it
                }
                stringBuilder.append(char)
            }

            return stringBuilder.toString()
        }
    }
}
