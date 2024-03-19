function reportCommandStatusChange() {
    window.editor.notifyCommandStatus(
        document.queryCommandState("bold"),
        document.queryCommandState("italic"),
        document.queryCommandState("strikeThrough"),
        document.queryCommandState("underline"),
    )
}

document.addEventListener("selectionchange", reportCommandStatusChange)
