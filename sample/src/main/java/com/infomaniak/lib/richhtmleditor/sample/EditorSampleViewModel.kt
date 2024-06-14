package com.infomaniak.lib.richhtmleditor.sample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infomaniak.lib.richhtmleditor.EditorReloader

class EditorSampleViewModel : ViewModel() {
    val editorReloader = EditorReloader(viewModelScope)
}
