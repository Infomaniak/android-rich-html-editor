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

import androidx.annotation.ColorInt

// TODO: This method might not be enough to escape user inputs and prevent access to JS code execution
fun looselyEscapeAsStringLiteralForJs(string: String): String {
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

    return stringBuilder.append("`").toString()
}

fun encodeArgsForJs(value: Any?): String {
    return when (value) {
        null -> "null"
        is String -> looselyEscapeAsStringLiteralForJs(value)
        is Boolean, is Number -> value.toString()
        is JsColor -> "'${colorToRgbHex(value.color)}'"
        else -> throw NotImplementedError("Encoding ${value::class} for JS is not yet implemented")
    }
}

@OptIn(ExperimentalStdlibApi::class)
private fun colorToRgbHex(@ColorInt color: Int) = color.toHexString(HexFormat.UpperCase).takeLast(6)
