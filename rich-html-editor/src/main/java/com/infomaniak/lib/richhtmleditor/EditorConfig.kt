package com.infomaniak.lib.richhtmleditor

data class EditorConfig(
    val subscribedStates: Set<TextFormat.StatusCommand>? = null,
    val customCss: List<String> = emptyList(),
    val customScripts: List<String> = emptyList(),
)
