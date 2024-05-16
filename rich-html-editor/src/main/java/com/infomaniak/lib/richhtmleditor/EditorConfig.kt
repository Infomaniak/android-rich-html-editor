package com.infomaniak.lib.richhtmleditor

/**
 * A wrapper to encapsulate the different config that need to be passed to the `RichHtmlEditorWebViewClient` use the editor how
 * you want it.
 *
 * @param subscribedStates A set of status commands to be subscribed to. Defaults to null, which means subscribing to all
 * available status commands.
 * @param customCss A list of custom CSS strings to be applied. Defaults to an empty list. These CSS styles will be loaded
 * when the page has finished loading. Supports new lines inside the CSS.
 * @param customScripts A list of custom scripts to be included. Defaults to an empty list. These scripts will be loaded when
 * the page has finished loading. Supports new lines inside the script.
 *
 * @see RichHtmlEditorWebViewClient
 * */
data class EditorConfig(
    val subscribedStates: Set<TextFormat.StatusCommand>? = null,
    val customCss: List<String> = emptyList(),
    val customScripts: List<String> = emptyList(),
)
