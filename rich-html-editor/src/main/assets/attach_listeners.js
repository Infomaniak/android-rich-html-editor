onBodyResize(() => {
    updateWebViewHeightWithBodyHeight()
    focusCursorOnScreen()
})

document.addEventListener("selectionchange", () => {
    if (!document.hasFocus()) return

    reportSelectionStateChangedIfNecessary()
    focusCursorOnScreen()
})

reportEmptyBodyStatus()
onEditorChildListChange(() => {
    reportEmptyBodyStatus()
})
