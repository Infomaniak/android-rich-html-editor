onBodyResize(() => {
    updateWebViewHeightWithBodyHeight()
    focusCursorOnScreen()
})

document.addEventListener("selectionchange", () => {
    if (document.hasFocus()) {
        focusCursorOnScreen()
    }

    reportSelectionStateChangedIfNecessary()
})

reportEmptyBodyStatus()
onEditorChildListChange(() => {
    reportEmptyBodyStatus()
})
