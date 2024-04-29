let currentSelectionState = {}

// Helper functions

function getBody() {
    return document.body
}

function getEditor() {
    return document.getElementById("editor")
}

function onAttributesChange(callback) {
    const mutationObserver = new MutationObserver(callback)
    const config = { subtree: true, attributes: true }
    mutationObserver.observe(getBody(), config)
}

function onBodyResize(callback) {
    let resizeObserver = new ResizeObserver(callback)
    resizeObserver.observe(getBody())
}

// Core logic

function exportHtml() {
    // TODO: don't cloneNode if nothing else is modified
    let editorContentCopy = getEditor().cloneNode(true)
    window.editor.exportHtml(editorContentCopy.innerHTML)
}

function focusCursorOnScreen() {
    let rect = getCaretRect()
    window.editor.focusCursorOnScreen(rect.left, rect.top, rect.right, rect.bottom)
}

function findElementNode(element) {
    if (element == null) return null
    if (element.nodeType == Node.ELEMENT_NODE) return element
    return findElementNode(element.parentNode)
}

function getCaretRect() {
    var selection = window.getSelection()
    var lastSelectedNode = selection.focusNode
    var range = selection.getRangeAt(0).cloneRange()

    // Create a range around the last selected node so the webview can scroll and follow the cursor even if the whole range is
    // bigger than the screen
    range.selectNodeContents(lastSelectedNode)

    var rangeRects = range.getClientRects()

    let rect
    switch (rangeRects.length) {
      case 0:
        rect = findElementNode(lastSelectedNode).getBoundingClientRect()
        break;
      case 1:
        rect = rangeRects[0]
        break;
      default:
        rect = range.getBoundingClientRect()
        break;
    }

    return rect
}

function reportSelectionStateChangedIfNecessary() {
    const newSelectionState = getCurrentSelectionState()
    if (!areSelectionStatesTheSame(currentSelectionState, newSelectionState)) {
        currentSelectionState = newSelectionState
        console.log("New selection changed:", currentSelectionState)
        window.editor.reportCommandDataChange(
            newSelectionState["bold"],
            newSelectionState["italic"],
            newSelectionState["strikeThrough"],
            newSelectionState["underline"],
            newSelectionState["fontName"],
            newSelectionState["fontSize"],
            newSelectionState["foreColor"],
            newSelectionState["backColor"]
        )
    }
}

function getCurrentSelectionState() {
    let currentState = {}

    for (const property of stateCommands) {
        currentState[property] = document.queryCommandState(property)
    }
    for (const property of valueCommands) {
        currentState[property] = document.queryCommandValue(property)
    }

    return currentState
}

function areSelectionStatesTheSame(state1, state2) {
    return stateCommands.every(property => state1[property] === state2[property]) && valueCommands.every(property => state1[property] === state2[property])
}

function updateWebViewHeightWithBodyHeight() {
    let body = getBody()
    let paddingTop = parseInt(window.getComputedStyle(body)["margin-top"])
    let paddingBottom = parseInt(window.getComputedStyle(body)["margin-bottom"])

    window.editor.reportNewDocumentHeight((body.offsetHeight + paddingTop + paddingBottom) * window.devicePixelRatio)
}

// Actually using the listeners

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
