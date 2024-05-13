package com.infomaniak.lib.richhtmleditor

import android.graphics.Color
import android.graphics.Rect
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class TextFormat(private val webView: RichHtmlEditorWebView, private val notifyExportedHtml: (String) -> Unit) {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val editorStatuses = EditorStatuses()

    private val _editorStatusesFlow: MutableSharedFlow<EditorStatuses> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    val editorStatusesFlow: Flow<EditorStatuses> = _editorStatusesFlow

    fun setBold() = execCommandAndRefreshButtonStatus(EditorStatusCommand.BOLD)

    fun setItalic() = execCommandAndRefreshButtonStatus(EditorStatusCommand.ITALIC)

    fun setStrikeThrough() = execCommandAndRefreshButtonStatus(EditorStatusCommand.STRIKE_THROUGH)

    fun setUnderline() = execCommandAndRefreshButtonStatus(EditorStatusCommand.UNDERLINE)

    fun removeFormat() = execCommand(OtherCommand.REMOVE_FORMAT)

    // TODO: Do we need to refresh button status only when caret?
    fun createLink(url: String) = execCommand(EditorStatusCommand.CREATE_LINK, url)

    fun unlink() = execCommand(OtherCommand.UNLINK)

    private fun execCommand(command: ExecCommand, argument: String? = null, callback: ((executionResult: String) -> Unit)? = null) {
        val valueCallback = callback?.let { ValueCallback<String> { value -> it(value) } }

        val commandArgument = "'${command.argumentName}'"
        val jsArgument = argument?.let { "'$it'" } ?: "null"
        webView.evaluateJavascript("document.execCommand($commandArgument, false, $jsArgument)", valueCallback)
    }

    private fun execCommandAndRefreshButtonStatus(command: EditorStatusCommand, argument: String? = null) {
        withSelectionState { isCaret ->
            execCommand(command, argument) {
                if (isCaret) reportSelectionStateChangedIfNecessary()
            }
        }
    }

    private fun withSelectionState(block: (Boolean) -> Unit) {
        webView.evaluateJavascript("window.getSelection().type == 'Caret'") { isCaret -> block((isCaret == "true")) }
    }

    private fun reportSelectionStateChangedIfNecessary() {
        webView.evaluateJavascript("reportSelectionStateChangedIfNecessary()", null)
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
        isLinkSelected: Boolean,
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
                isLinkSelected,
            )
            _editorStatusesFlow.emit(editorStatuses)
        }
    }

    @JavascriptInterface
    fun reportNewDocumentHeight(newHeight: Int) {
        coroutineScope.launch(Dispatchers.Main) {
            updateWebViewHeight(newHeight)
        }
    }

    @JavascriptInterface
    fun focusCursorOnScreen(left: Int, top: Int, right: Int, bottom: Int) {
        val density: Float = webView.resources.displayMetrics.density

        webView.requestRectangleOnScreen(
            Rect(
                (left * density).roundToInt(),
                (top * density).roundToInt(),
                (right * density).roundToInt(),
                (bottom * density).roundToInt()
            )
        )
    }

    @JavascriptInterface
    fun exportHtml(html: String) = notifyExportedHtml(html)

    private fun updateWebViewHeight(newHeight: Int) {
        webView.updateLayoutParams<ViewGroup.LayoutParams> {
            height = newHeight
        }
    }

    interface ExecCommand {
        val argumentName: String
    }

    enum class CommandType { STATE, VALUE, COMPLEX }

    enum class EditorStatusCommand(override val argumentName: String, val commandType: CommandType) : ExecCommand {
        BOLD("bold", CommandType.STATE),
        ITALIC("italic", CommandType.STATE),
        STRIKE_THROUGH("strikeThrough", CommandType.STATE),
        UNDERLINE("underline", CommandType.STATE),
        FONT_NAME("fontName", CommandType.VALUE),
        FONT_SIZE("fontSize", CommandType.VALUE),
        TEXT_COLOR("foreColor", CommandType.VALUE),
        BACKGROUND_COLOR("backColor", CommandType.VALUE),
        CREATE_LINK("createLink", CommandType.COMPLEX),
    }

    enum class OtherCommand(override val argumentName: String) : ExecCommand {
        REMOVE_FORMAT("removeFormat"),
        UNLINK("unlink"),
    }
}
