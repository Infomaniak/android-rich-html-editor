package com.infomaniak.library.htmlricheditor

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

    fun setBold() = execCommand(ExecCommand.BOLD)

    fun setItalic() = execCommand(ExecCommand.ITALIC)

    fun setStrikeThrough() = execCommand(ExecCommand.STRIKE_THROUGH)

    fun setUnderline() = execCommand(ExecCommand.UNDERLINE)

    fun removeFormat() = execCommand(ExecCommand.REMOVE_FORMAT)

    private fun execCommand(command: ExecCommand, callback: ((executionResult: String) -> Unit)? = null) {
        val valueCallback = callback?.let { callback -> ValueCallback<String> { callback(it) } }
        webView.evaluateJavascript("document.execCommand('${command.argumentName}')", valueCallback)
    }

    // Parses the css formatted color string obtained from the js method queryCommandValue() into an easy to use ColorInt
    @ColorInt
    fun String.toColorInt(): Int {
        val startIndex = when {
            startsWith("rgb(") -> 4
            startsWith("rgba(") -> 5
            else -> throw IllegalArgumentException("Color string should start with rgb( ou with rgba(")
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
                fontSize.toFloat(),
                textColor.toColorInt(),
                backgroundColor.toColorInt(),
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
