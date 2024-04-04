package com.infomaniak.lib.richhtmleditor

import android.graphics.Color
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.annotation.ColorInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class TextFormat(private val webView: WebView) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val editorStatuses = EditorStatuses()

    private val _editorStatusesFlow: MutableSharedFlow<EditorStatuses> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    // TODO: Make a flow for each property so properties a user will never be interested in won't trigger a collect for no reason
    val editorStatusesFlow: Flow<EditorStatuses> = _editorStatusesFlow

    fun setBold() {
        execCommandAndRefreshButtonStatus(EditorStatusCommand.BOLD)
    }

    fun setItalic() {
        execCommandAndRefreshButtonStatus(EditorStatusCommand.ITALIC)
    }

    fun setStrikeThrough() {
        execCommandAndRefreshButtonStatus(EditorStatusCommand.STRIKE_THROUGH)
    }

    fun setUnderline() {
        execCommandAndRefreshButtonStatus(EditorStatusCommand.UNDERLINE)
    }

    fun removeFormat() {
        execCommand(OtherCommand.REMOVE_FORMAT)
    }

    private fun execCommand(command: ExecCommand, callback: ((executionResult: String) -> Unit)? = null) {
        val valueCallback = callback?.let { ValueCallback<String> { value -> it(value) } }
        webView.evaluateJavascript("document.execCommand('${command.argumentName}')", valueCallback)
    }

    private fun withSelectionState(block: (Boolean) -> Unit) {
        webView.evaluateJavascript("window.getSelection().type == 'Caret'") { isCaret -> block((isCaret == "true")) }
    }

    private fun execCommandAndRefreshButtonStatus(command: EditorStatusCommand) {
        withSelectionState { isCaret ->
            execCommand(command) {
                if (isCaret) updateEditorStatus(command)
            }
        }
    }

    private fun updateEditorStatus(command: EditorStatusCommand) {
        // TODO: Handle queryCommandValue cases
        webView.evaluateJavascript("document.queryCommandState('${command.argumentName}')") { result ->
            val isActivated = result == "true"

            coroutineScope.launch {
                editorStatuses.updateStatusAtomically(command, isActivated)
                _editorStatusesFlow.emit(editorStatuses)
            }
        }
    }

    // Parses the css formatted color string obtained from the js method queryCommandValue() into an easy to use ColorInt
    @ColorInt
    fun String.toColorIntOrNull(): Int? {
        val startIndex = when {
            startsWith("rgb(") -> 4
            startsWith("rgba(") -> 5
            else -> return null
        }

        val (r, g, b) = substring(startIndex, length - 1).replace(" ", "").split(",").map { it.toInt() }

        return Color.argb(255, r, g, b)
    }

    @JavascriptInterface
    fun reportCommandDataChange(
        isBold: Boolean,
        isItalic: Boolean,
        isStrikeThrough: Boolean,
        isUnderlined: Boolean,
        fontName: String,
        fontSize: String,
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
                fontSize.toFloatOrNull(),
                textColor.toColorIntOrNull(),
                backgroundColor.toColorIntOrNull(),
            )
            _editorStatusesFlow.emit(editorStatuses)
        }
    }

    interface ExecCommand {
        val argumentName: String
    }

    enum class CommandType { STATE, VALUE }

    enum class EditorStatusCommand(override val argumentName: String, val commandType: CommandType) : ExecCommand {
        BOLD("bold", CommandType.STATE),
        ITALIC("italic", CommandType.STATE),
        STRIKE_THROUGH("strikeThrough", CommandType.STATE),
        UNDERLINE("underline", CommandType.STATE),
        FONT_NAME("fontName", CommandType.VALUE),
        FONT_SIZE("fontSize", CommandType.VALUE),
        TEXT_COLOR("foreColor", CommandType.VALUE),
        BACKGROUND_COLOR("backColor", CommandType.VALUE),
    }

    enum class OtherCommand(override val argumentName: String) : ExecCommand {
        REMOVE_FORMAT("removeFormat")
    }
}
