package com.infomaniak.library.htmlricheditor

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TextFormat(private val webView: WebView) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val editorStatuses = EditorStatuses()

    private val _editorStatusesFlow: MutableSharedFlow<EditorStatuses> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val editorStatusesFlow: Flow<EditorStatuses> = _editorStatusesFlow

    fun setBold() {
        execCommandAndRefreshButtonStatus(ExecCommand.BOLD)
    }

    fun setItalic() {
        execCommandAndRefreshButtonStatus(ExecCommand.ITALIC)
    }

    fun setStrikeThrough() {
        execCommandAndRefreshButtonStatus(ExecCommand.STRIKE_THROUGH)
    }

    fun setUnderline() {
        execCommandAndRefreshButtonStatus(ExecCommand.UNDERLINE)
    }

    fun removeFormat() {
        execCommandAndRefreshButtonStatus(ExecCommand.REMOVE_FORMAT)
    }

    private fun execCommand(command: ExecCommand, callback: ((executionResult: String) -> Unit)? = null) {
        val valueCallback = callback?.let { callback -> ValueCallback<String> { callback(it) } }
        webView.evaluateJavascript("document.execCommand('${command.argumentName}')", valueCallback)
    }

    private fun withSelectionState(block: (Boolean) -> Unit) {
        webView.evaluateJavascript("window.getSelection().type == 'Caret'") { isCaret -> block((isCaret == "true")) }
    }

    private fun execCommandAndRefreshButtonStatus(command: ExecCommand) {
        withSelectionState { isCaret ->
            execCommand(command) {
                if (isCaret) updateEditorStatus(command)
            }
        }
    }

    private fun updateEditorStatus(command: ExecCommand) {
        webView.evaluateJavascript("document.queryCommandState('${command.argumentName}')") { result ->
            val isActivated = result == "true"

            coroutineScope.launch {
                editorStatuses.updateStatusAtomically(command, isActivated)
                _editorStatusesFlow.emit(editorStatuses)
            }
        }
    }

    @JavascriptInterface
    fun reportCommandDataChange(
        isBold: Boolean,
        isItalic: Boolean,
        isStrikeThrough: Boolean,
        isUnderlined: Boolean,
        fontName: String,
        fontSize: Int,
        textColor: String,
        backgroundColor: String,
    ) {
        coroutineScope.launch {
            editorStatuses.updateStatusesAtomically(
                isBold,
                isItalic,
                isStrikeThrough,
                isUnderlined,
                fontName,
                fontSize,
                textColor,
                backgroundColor,
            )
            _editorStatusesFlow.emit(editorStatuses)
        }
    }

    enum class ExecCommand(val argumentName: String) {
        BOLD("bold"),
        ITALIC("italic"),
        STRIKE_THROUGH("strikeThrough"),
        UNDERLINE("underline"),

        FONT_NAME("fontName"),
        FONT_SIZE("fontSize"),
        TEXT_COLOR("foreColor"),
        BACKGROUND_COLOR("hiliteColor"),

        REMOVE_FORMAT("removeFormat"),
    }
}


data class EditorStatuses(
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var isStrikeThrough: Boolean = false,
    var isUnderlined: Boolean = false,
    var fontName: String? = null,
    var fontSize: Int? = null,
    var textColor: String? = null,
    var backgroundColor: String? = null,
) {
    private val mutex = Mutex()

    suspend fun updateStatusesAtomically(
        isBold: Boolean,
        isItalic: Boolean,
        isStrikeThrough: Boolean,
        isUnderlined: Boolean,
        fontName: String,
        fontSize: Int,
        textColor: String,
        backgroundColor: String,
    ) {
        mutex.withLock {
            this.isBold = isBold
            this.isItalic = isItalic
            this.isStrikeThrough = isStrikeThrough
            this.isUnderlined = isUnderlined
            this.fontName = fontName
            this.fontSize = fontSize
            this.textColor = textColor
            this.backgroundColor = backgroundColor
        }
    }

    suspend fun updateStatusAtomically(command: TextFormat.ExecCommand, value: Any) {
        mutex.withLock {
            when (command) {
                TextFormat.ExecCommand.BOLD -> this.isBold = value as Boolean
                TextFormat.ExecCommand.ITALIC -> this.isItalic = value as Boolean
                TextFormat.ExecCommand.STRIKE_THROUGH -> this.isStrikeThrough = value as Boolean
                TextFormat.ExecCommand.UNDERLINE -> this.isUnderlined = value as Boolean
                TextFormat.ExecCommand.FONT_NAME -> this.fontName = value as String
                TextFormat.ExecCommand.FONT_SIZE -> this.fontSize = value as Int
                TextFormat.ExecCommand.TEXT_COLOR -> this.textColor = value as String
                TextFormat.ExecCommand.BACKGROUND_COLOR -> this.backgroundColor = value as String
                TextFormat.ExecCommand.REMOVE_FORMAT -> Unit
            }
        }
    }
}
