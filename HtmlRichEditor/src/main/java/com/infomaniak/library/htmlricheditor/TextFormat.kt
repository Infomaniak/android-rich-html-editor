package com.infomaniak.library.htmlricheditor

import android.webkit.WebView

class TextFormat(private val webView: WebView) {
    fun setBold() {
        execCommand(ExecCommand.BOLD)
    }

    fun setItalic() {
        execCommand(ExecCommand.ITALIC)
    }

    fun setStrikeThrough() {
        execCommand(ExecCommand.STRIKE_THROUGH)
    }

    fun setUnderline() {
        execCommand(ExecCommand.UNDERLINE)
    }

    private fun execCommand(command: ExecCommand) {
        webView.evaluateJavascript("document.execCommand('${command.argumentName}')", null)
    }

    enum class ExecCommand(val argumentName: String) {
        BOLD("bold"),
        ITALIC("italic"),
        STRIKE_THROUGH("strikeThrough"),
        UNDERLINE("underline"),
    }
}
