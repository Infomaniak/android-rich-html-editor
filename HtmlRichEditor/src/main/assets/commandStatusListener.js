function notifyCommandStatus(type) {
    window.editor.notifyCommandStatus(type, document.queryCommandState(type))
}

function reportCommandStatusChange() {
    notifyCommandStatus("bold")
    notifyCommandStatus("italic")
    notifyCommandStatus("strikeThrough")
    notifyCommandStatus("underline")
}

document.addEventListener("selectionchange", reportCommandStatusChange)
