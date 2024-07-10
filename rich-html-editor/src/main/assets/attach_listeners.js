onBodyResize(() => {
    updateWebViewHeightWithBodyHeight()
    focusCursorOnScreen()
})

document.addEventListener("selectionchange", () => {
    reportSelectionStateChangedIfNecessary()
    focusCursorOnScreen()
})

onEmptyBodyChanges(isEditorEmpty => {
    window.editor.onEmptyBodyChanges(isEditorEmpty)
})
