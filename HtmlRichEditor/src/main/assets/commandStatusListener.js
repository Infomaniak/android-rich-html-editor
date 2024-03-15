function reportCommandStatusChange() {
    window.editor.notifyBoldStatus(document.queryCommandState("bold"))
    window.editor.notifyItalicStatus(document.queryCommandState("italic"))
    window.editor.notifyStrikeThroughStatus(document.queryCommandState("strikeThrough"))
    window.editor.notifyUnderlineStatus(document.queryCommandState("underline"))
}

document.addEventListener("selectionchange", reportCommandStatusChange)
