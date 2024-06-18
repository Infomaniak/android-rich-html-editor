package com.infomaniak.lib.richhtmleditor

import android.graphics.Color
import android.webkit.JavascriptInterface
import androidx.annotation.ColorInt
import com.infomaniak.lib.richhtmleditor.executor.JsExecutableMethod
import com.infomaniak.lib.richhtmleditor.executor.JsExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

internal class JsBridge(
    private val coroutineScope: CoroutineScope,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val jsExecutor: JsExecutor,
    private val notifyExportedHtml: (String) -> Unit,
    private val requestRectangleOnScreen: (left: Int, top: Int, right: Int, bottom: Int) -> Unit,
    private val updateWebViewHeight: (Int) -> Unit,
) {

    private val editorStatuses = EditorStatuses()

    private val _editorStatusesFlow = MutableSharedFlow<EditorStatuses>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val editorStatusesFlow: Flow<EditorStatuses> = _editorStatusesFlow

    fun toggleBold() = execCommand(StatusCommand.BOLD)

    fun toggleItalic() = execCommand(StatusCommand.ITALIC)

    fun toggleStrikeThrough() = execCommand(StatusCommand.STRIKE_THROUGH)

    fun toggleUnderline() = execCommand(StatusCommand.UNDERLINE)

    fun removeFormat() = execCommand(OtherCommand.REMOVE_FORMAT)

    fun createLink(displayText: String?, url: String) {
        jsExecutor.executeImmediatelyAndRefreshToolbar(JsExecutableMethod("createLink", displayText, url))
    }

    fun unlink() = jsExecutor.executeImmediatelyAndRefreshToolbar(JsExecutableMethod("unlink"))

    private fun execCommand(command: ExecCommand, argument: String? = null) {
        jsExecutor.executeImmediatelyAndRefreshToolbar(
            JsExecutableMethod(
                "document.execCommand",
                command.argumentName,
                false,
                argument,
            )
        )
    }

    // Parses the css formatted color string obtained from the js method queryCommandValue() into an easy to use ColorInt
    @ColorInt
    fun String.toColorIntOrNull(): Int? {
        val startIndex = when {
            startsWith("rgb(") -> 4
            startsWith("rgba(") -> 5
            else -> return null
        }

        val (r, g, b) = substring(startIndex, lastIndex).replace(" ", "").split(",")

        return Color.argb(255, r.toInt(), g.toInt(), b.toInt())
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
        coroutineScope.launch(defaultDispatcher) {
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
        coroutineScope.launch(mainDispatcher) {
            updateWebViewHeight(newHeight)
        }
    }

    @JavascriptInterface
    fun focusCursorOnScreen(left: Int, top: Int, right: Int, bottom: Int) {
        requestRectangleOnScreen(left, top, right, bottom)
    }

    @JavascriptInterface
    fun exportHtml(html: String) = notifyExportedHtml(html)
}
