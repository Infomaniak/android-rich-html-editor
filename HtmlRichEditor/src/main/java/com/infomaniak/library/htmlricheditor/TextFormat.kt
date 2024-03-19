package com.infomaniak.library.htmlricheditor

import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class TextFormat(private val webView: WebView) {

    private val _editorStatus: MutableStateFlow<Set<ExecCommand>> = MutableStateFlow(mutableSetOf())
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
            execCommand(command) {
                if (isCaret) updateEditorStatus(command)
            }
        }
    }

    private fun updateEditorStatus(command: ExecCommand) {
        webView.evaluateJavascript("document.queryCommandState('${command.argumentName}')") { result ->
            val isActivated = result == "true"

            // toMutableSet() is need to clone the set to have a new reference on the set so the .value assignation will consider
            // it an updated value
            _editorStatus.value = _editorStatus.value.toMutableSet().apply {
                if (isActivated) add(command) else remove(command)
            }
        }
    }

    @JavascriptInterface
    fun notifyCommandStatus(isBold: Boolean, isItalic: Boolean, isStrikeThrough: Boolean, isUnderlined: Boolean) { // TODO : Pass array
        _editorStatus.value = mutableSetOf<ExecCommand>().apply {
            if (isBold) add(ExecCommand.BOLD)
            if (isItalic) add(ExecCommand.ITALIC)
            if (isStrikeThrough) add(ExecCommand.STRIKE_THROUGH)
            if (isUnderlined) add(ExecCommand.UNDERLINE)
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
