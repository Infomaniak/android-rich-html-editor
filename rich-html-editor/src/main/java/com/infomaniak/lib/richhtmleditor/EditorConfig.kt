package com.infomaniak.lib.richhtmleditor

/**
 * A wrapper to encapsulate the various configurations needed to customize the `RichHtmlEditorWebViewClient`.
 *
 * @param subscribedStates A set of status commands to subscribe to. Defaults to null, meaning all available status commands
 * will be subscribed to.
 * @param customCss A list of custom CSS strings to be applied. Defaults to an empty list. These styles will be applied when
 * the page has finished loading. Supports new lines within the CSS.
 * @param customScripts A list of custom scripts to be included. Defaults to an empty list. These scripts will be executed
 * when the page has finished loading. Supports new lines within the script.
 *
 * @see RichHtmlEditorWebViewClient
 */
data class EditorConfig(
    val subscribedStates: Set<StatusCommand>? = null,
    val customCss: List<String> = emptyList(),
    val customScripts: List<String> = emptyList(),
)
