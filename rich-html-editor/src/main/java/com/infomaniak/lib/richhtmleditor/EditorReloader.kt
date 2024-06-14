package com.infomaniak.lib.richhtmleditor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class EditorReloader(private val viewModelScope: CoroutineScope) {

    private var needToReloadHtml: Boolean = false
    private var savedHtml = MutableStateFlow<String?>(null)

    suspend fun load(editor: RichHtmlEditorWebView, defaultHtml: String) {
        if (needToReloadHtml) {
            savedHtml.collect {
                if (it == null) return@collect

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
            viewModelScope.launch { savedHtml.emit(it) }
        }
    }

    private suspend fun resetSavedHtml() {
        savedHtml.emit(null)
    }

    private fun enableHtmlReload() {
        needToReloadHtml = true
    }
}
