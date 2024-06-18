package com.infomaniak.lib.richhtmleditor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class EditorReloader(private val coroutineScope: CoroutineScope) {

    private var needToReloadHtml: Boolean = false
    private var savedHtml = MutableStateFlow<String?>(null)

    suspend fun load(editor: RichHtmlEditorWebView, defaultHtml: String) {
        if (needToReloadHtml) {
            savedHtml.collectLatest {
                if (it == null) return@collectLatest

                resetSavedHtml()
                editor.setHtml(it)
            }
        } else {
            editor.setHtml(defaultHtml)
        }

        enableHtmlReload()
    }

    fun save(editor: RichHtmlEditorWebView) {
        editor.exportHtml {
            coroutineScope.launch { savedHtml.emit(it) }
        }
    }

    private suspend fun resetSavedHtml() {
        savedHtml.emit(null)
    }

    private fun enableHtmlReload() {
        needToReloadHtml = true
    }
}
