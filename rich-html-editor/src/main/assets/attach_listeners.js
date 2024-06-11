onBodyResize(() => {
    updateWebViewHeightWithBodyHeight()
    focusCursorOnScreen()
})

document.addEventListener("selectionchange", () => {
    reportSelectionStateChangedIfNecessary()
    focusCursorOnScreen()
})
