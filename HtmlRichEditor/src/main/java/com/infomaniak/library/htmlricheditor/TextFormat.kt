package com.infomaniak.library.htmlricheditor

import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class TextFormat(private val webView: WebView) {

    private val typeToExecCommand = ExecCommand.entries.associateBy(ExecCommand::argumentName)

    private val _editorStatus: MutableStateFlow<Set<ExecCommand>> = MutableStateFlow(emptySet())
    val editorStatusFlow: Flow<Set<ExecCommand>> = _editorStatus

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
            execCommand(command) { if (isCaret) webView.evaluateJavascript("reportCommandStatusChange()", null) }
        }
    }

    @JavascriptInterface
    fun notifyCommandStatus(type: String, isActivated: Boolean) {
        val command = typeToExecCommand[type]
        // when (command) {
        //     ExecCommand.BOLD -> _boldStatus.postValue(isActivated)
        //     ExecCommand.ITALIC -> _italicStatus.postValue(isActivated)
        //     ExecCommand.STRIKE_THROUGH -> _strikeThroughStatus.postValue(isActivated)
        //     ExecCommand.UNDERLINE -> _underlineStatus.postValue(isActivated)
        //     ExecCommand.REMOVE_FORMAT -> Unit
        //     null -> Unit // Should never happen
        // }
    }

    @JavascriptInterface
    fun notifyCommandStatuses(isBold: Boolean, isItalic: Boolean, isStrikeThrough: Boolean, isUnderlined: Boolean) { // TODO : Pass array
        _editorStatus.update {
            mutableSetOf<ExecCommand>().apply {
                if (isBold) add(ExecCommand.BOLD)
                if (isItalic) add(ExecCommand.ITALIC)
                if (isStrikeThrough) add(ExecCommand.STRIKE_THROUGH)
                if (isUnderlined) add(ExecCommand.UNDERLINE)
            }
        }
    }

    enum class ExecCommand(val argumentName: String) {
        BOLD("bold"),
        ITALIC("italic"),
        STRIKE_THROUGH("strikeThrough"),
        UNDERLINE("underline"),
        REMOVE_FORMAT("removeFormat"),
    }
}
