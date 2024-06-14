package com.infomaniak.lib.richhtmleditor.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EditorSampleViewModel : ViewModel() {
    var needToReloadHtml: Boolean = false
        private set
    private var _savedHtml = MutableStateFlow<String?>(null)
    val savedHtml: StateFlow<String?> = _savedHtml

    fun saveHtml(html: String) {
        viewModelScope.launch { _savedHtml.emit(html) }
    }

    fun enableHtmlReload() {
        needToReloadHtml = true
    }

    fun resetSavedHtml() {
        viewModelScope.launch {
            _savedHtml.emit(null)
        }
    }
}
