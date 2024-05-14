onBodyResize(() => {
    updateWebViewHeightWithBodyHeight()
    focusCursorOnScreen()
})

// On some occasions execCommand modifies the attributes of tags which doesn't trigger the "selectionchange" so listening to
// attributes has been needed in order to catch some of these modifications we were lacking
onAttributesChange(reportSelectionStateChangedIfNecessary)
document.addEventListener("selectionchange", () => {
    reportSelectionStateChangedIfNecessary()
    focusCursorOnScreen()
})
