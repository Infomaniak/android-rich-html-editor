package com.infomaniak.lib.richhtmleditor

import android.graphics.Color
import android.graphics.Rect
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import androidx.annotation.ColorInt
import androidx.core.view.updateLayoutParams
import com.infomaniak.lib.richhtmleditor.executor.JsExecutableMethod
import com.infomaniak.lib.richhtmleditor.executor.JsExecutor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Utility class for handling text formatting operations within `RichHtmlEditorWebView`.
 *
 * The `TextFormat` class provides methods for setting and removing text formatting such as bold, italic, underline,
 * strike-through, creating and unlinking hyperlinks, and exposing an observable flow of editor statuses.
 *
 * To interact with text formatting in `RichHtmlEditorWebView`, access its instance of `TextFormat` and utilize this class'
 * methods. Additionally, you can observe the editor statuses using the `editorStatusesFlow` property.
 *
 * Example usage:
 * ```
 * // Access the textFormat instance from RichHtmlEditorWebView
 * val textFormat = richHtmlEditorWebView.textFormat
 *
 * buttonBold.setOnClickListener { textFormat.toggleBold() }
 * buttonItalic.setOnClickListener { textFormat.toggleItalic() }
 *
 * ...
 *
 * viewLifecycleOwner.lifecycleScope.launch {
 *   textFormat.editorStatusesFlow.collect {
 *     buttonBold.isActivated = it.isBold
 *     buttonItalic.isActivated = it.isItalic
 *   }
 * }
 * ```
 *
 * @property editorStatusesFlow A flow representing the current status of the editor's text formatting.
 */
internal class TextFormat(
    private val webView: RichHtmlEditorWebView,
    private val coroutineScope: CoroutineScope,
    private val jsExecutor: JsExecutor,
    private val notifyExportedHtml: (String) -> Unit,
) {

    private val editorStatuses = EditorStatuses()

    private val _editorStatusesFlow: MutableSharedFlow<EditorStatuses> = MutableSharedFlow(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Flow that is notified everytime a subscribed `EditorStatuses` is updated.
     *
     * You can use this flow to listen to subscribed `EditorStatuses` and update your toolbar's UI accordingly to show which
     * formatting is enabled on the current selection.
     */
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
}
