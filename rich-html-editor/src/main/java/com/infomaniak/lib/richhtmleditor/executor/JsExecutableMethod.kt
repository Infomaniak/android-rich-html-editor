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
import androidx.annotation.ColorInt
import com.infomaniak.lib.richhtmleditor.JsColor

class JsExecutableMethod(
    private val methodName: String,
    private vararg val args: Any?,
    resultCallback: ((String) -> Unit)? = null,
) {
    private val callbacks: MutableList<(String) -> Unit> = resultCallback?.let { mutableListOf(it) } ?: mutableListOf()

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
                is String -> looselyEscapeAsStringLiteralForJs(value)
                is Boolean, is Number -> value.toString()
                is JsColor -> "'${colorToRgbHex(value.color)}'"
                else -> throw NotImplementedError("Encoding ${value::class} for JS is not yet implemented")
            }
        }

        // TODO: This method might not be enough to escape user inputs and prevent access to JS code execution
        private fun looselyEscapeAsStringLiteralForJs(string: String): String {
            val stringBuilder = StringBuilder("`")

            string.forEach {
                val char = when (it) {
                    '`' -> "\\`"
                    '\\' -> "\\\\"
                    '$' -> "\\$"
                    else -> it
                }
                stringBuilder.append(char)
            }

            stringBuilder.append("`")

            return stringBuilder.toString()
        }

        @OptIn(ExperimentalStdlibApi::class)
        private fun colorToRgbHex(@ColorInt color: Int) = color.toHexString(HexFormat.UpperCase).takeLast(6)
    }
}
