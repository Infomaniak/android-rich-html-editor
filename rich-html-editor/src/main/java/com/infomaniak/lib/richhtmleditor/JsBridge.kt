/*
 * Infomaniak Rich HTML Editor - Android
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.richhtmleditor

import android.graphics.Color
import android.webkit.JavascriptInterface
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import com.infomaniak.lib.richhtmleditor.executor.JsExecutableMethod
import com.infomaniak.lib.richhtmleditor.executor.JsExecutor
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val editorStatusesFlow: SharedFlow<EditorStatuses> = _editorStatusesFlow.asSharedFlow()

    private var _isEmptyFlow: MutableStateFlow<Boolean?> = MutableStateFlow(null)
    var isEmptyFlow: StateFlow<Boolean?> = _isEmptyFlow.asStateFlow()

    fun toggleBold() = execCommand(StatusCommand.BOLD)

    fun toggleItalic() = execCommand(StatusCommand.ITALIC)

    fun toggleStrikeThrough() = execCommand(StatusCommand.STRIKE_THROUGH)

    fun toggleUnderline() = execCommand(StatusCommand.UNDERLINE)

    fun toggleOrderedList() = execCommand(StatusCommand.ORDERED_LIST)

    fun toggleUnorderedList() = execCommand(StatusCommand.UNORDERED_LIST)

    fun toggleSubscript() = execCommand(StatusCommand.SUBSCRIPT)

    fun toggleSuperscript() = execCommand(StatusCommand.SUPERSCRIPT)

    fun removeFormat() = execCommand(OtherCommand.REMOVE_FORMAT)

    fun justify(justification: Justification) = execCommand(justification.execCommand)

    fun indent() = execCommand(OtherCommand.INDENT)

    fun outdent() = execCommand(OtherCommand.OUTDENT)

    fun setTextColor(color: JsColor) = execCommand(StatusCommand.TEXT_COLOR, color)

    fun setTextBackgroundColor(color: JsColor) = execCommand(StatusCommand.BACKGROUND_COLOR, color)

    fun setFontSize(@IntRange(from = FONT_MIN_SIZE, to = FONT_MAX_SIZE) fontSize: Int) {
        execCommand(StatusCommand.FONT_SIZE, fontSize)
    }

    fun undo() = execCommand(OtherCommand.UNDO)

    fun redo() = execCommand(OtherCommand.REDO)

    fun createLink(displayText: String?, url: String) {
        jsExecutor.executeImmediatelyAndRefreshToolbar(JsExecutableMethod("createLink", displayText, url))
    }

    fun unlink() = jsExecutor.executeImmediatelyAndRefreshToolbar(JsExecutableMethod("unlink"))

    private fun execCommand(command: ExecCommand, argument: Any? = null) {
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
        if (!startsWith("rgb")) return null

        val (r, g, b) = filterNot { it in CHARACTERS_TO_REMOVE }.split(",").takeIf { it.size == 3 || it.size == 4 } ?: return null

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
        isOrderedListSelected: Boolean,
        isUnorderedListSelected: Boolean,
        isSubscript: Boolean,
        isSuperscript: Boolean,
        isJustifyLeft: Boolean,
        isJustifyCenter: Boolean,
        isJustifyRight: Boolean,
        isJustifyFull: Boolean,
    ) {
        fun computeJustification(): Justification? = when {
            isJustifyLeft -> Justification.LEFT
            isJustifyCenter -> Justification.CENTER
            isJustifyRight -> Justification.RIGHT
            isJustifyFull -> Justification.FULL
            else -> null
        }

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
                isOrderedListSelected,
                isUnorderedListSelected,
                isSubscript,
                isSuperscript,
                computeJustification(),
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

    @JavascriptInterface
    fun onEmptyBodyChanges(isBodyEmpty: Boolean) {
        coroutineScope.launch(defaultDispatcher) {
            _isEmptyFlow.emit(isBodyEmpty)
        }
    }

    companion object {
        private val CHARACTERS_TO_REMOVE = setOf('r', 'g', 'b', 'a', '(', ')', ' ')
        const val FONT_MIN_SIZE: Long = 1
        const val FONT_MAX_SIZE: Long = 7
    }
}
