package com.infomaniak.lib.richhtmleditor.executor

import android.webkit.WebView
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection
import com.infomaniak.lib.richhtmleditor.injectCss
import com.infomaniak.lib.richhtmleditor.injectScript

internal class ScriptCssInjector(private val webView: WebView) : JsLifecycleAwareExecutor<CodeInjection>() {

    override fun executeImmediately(value: CodeInjection): Unit = with(webView) {
        when (value.type) {
            CodeInjection.InjectionType.SCRIPT -> injectScript(value.code)
            CodeInjection.InjectionType.CSS -> injectCss(value.code)
        }
    }

    data class CodeInjection(val type: InjectionType, val code: String) {
        enum class InjectionType { SCRIPT, CSS }
    }
}
