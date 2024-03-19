function notifyCommandStatus(type) {
    window.editor.notifyCommandStatus(type, document.queryCommandState(type))
}

function reportCommandStatusChange() {
    window.editor.notifyCommandStatuses(
        document.queryCommandState("bold"),
        document.queryCommandState("italic"),
        document.queryCommandState("strikeThrough"),
        document.queryCommandState("underline"),
    )
}

document.addEventListener("selectionchange", reportCommandStatusChange)
