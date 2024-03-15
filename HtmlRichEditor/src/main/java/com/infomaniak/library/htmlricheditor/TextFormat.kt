package com.infomaniak.library.htmlricheditor

import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.distinctUntilChanged

class TextFormat(private val webView: WebView) {

    private val _boldStatus = MutableLiveData<Boolean>()
    val boldStatus: LiveData<Boolean> = _boldStatus.distinctUntilChanged()

    private val _italicStatus = MutableLiveData<Boolean>()
    val italicStatus: LiveData<Boolean> = _italicStatus.distinctUntilChanged()

    private val _strikeThroughStatus = MutableLiveData<Boolean>()
    val strikeThroughStatus: LiveData<Boolean> = _strikeThroughStatus.distinctUntilChanged()

    private val _underlineStatus = MutableLiveData<Boolean>()
    val underlineStatus: LiveData<Boolean> = _underlineStatus.distinctUntilChanged()

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
    fun notifyBoldStatus(isBoldActive: Boolean) {
        _boldStatus.postValue(isBoldActive)
    }

    @JavascriptInterface
    fun notifyItalicStatus(isItalicActive: Boolean) {
        _italicStatus.postValue(isItalicActive)
    }

    @JavascriptInterface
    fun notifyStrikeThroughStatus(isStrikeThroughActive: Boolean) {
        _strikeThroughStatus.postValue(isStrikeThroughActive)
    }

    @JavascriptInterface
    fun notifyUnderlineStatus(isUnderlineActive: Boolean) {
        _underlineStatus.postValue(isUnderlineActive)
    }

    enum class ExecCommand(val argumentName: String) {
        BOLD("bold"),
        ITALIC("italic"),
        STRIKE_THROUGH("strikeThrough"),
        UNDERLINE("underline"),
        REMOVE_FORMAT("removeFormat"),
    }
}
