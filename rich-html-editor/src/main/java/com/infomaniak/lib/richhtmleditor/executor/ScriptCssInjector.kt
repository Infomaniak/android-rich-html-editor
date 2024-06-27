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
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection.InjectionType
import com.infomaniak.lib.richhtmleditor.injectCss
import com.infomaniak.lib.richhtmleditor.injectScript

internal class ScriptCssInjector(private val webView: WebView) : JsLifecycleAwareExecutor<CodeInjection>() {

    override fun executeImmediately(value: CodeInjection): Unit = with(webView) {
        when (value.type) {
            InjectionType.SCRIPT -> injectScript(value.code)
            InjectionType.CSS -> injectCss(value.code)
        }
    }

    data class CodeInjection(val type: InjectionType, val code: String) {
        enum class InjectionType { SCRIPT, CSS }
    }
}
