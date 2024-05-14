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
    mutationObserver.observe(getEditor(), config)
}

function onBodyResize(callback) {
    let resizeObserver = new ResizeObserver(callback)
    resizeObserver.observe(getEditor())
}

// Core logic

function exportHtml() {
    // TODO: don't cloneNode if nothing else is modified
    let editorContentCopy = getEditor().cloneNode(true)
    window.editor.exportHtml(editorContentCopy.innerHTML)
}

function focusCursorOnScreen() {
    let rect = getCaretRect()
    if (rect) window.editor.focusCursorOnScreen(rect.left, rect.top, rect.right, rect.bottom)
}

function findElementNode(element) {
    if (element == null) return null
    if (element.nodeType == Node.ELEMENT_NODE) return element
    return findElementNode(element.parentNode)
}

function getCaretRect() {
    var selection = window.getSelection()
    var lastSelectedNode = selection.focusNode

    if (selection.rangeCount == 0) return

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
            newSelectionState["backColor"],
            newSelectionState["isLinkSelected"]
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

    if (REPORT_LINK_STATUS) {
        var linkStatus = computeLinkStatus() // TODO: Single line if nothing more comes here
        currentState["isLinkSelected"] = linkStatus
    }

    return currentState
}

function computeLinkStatus() {
    return getAllLinksPartiallyContainedInsideSelection().length > 0
}

function areSelectionStatesTheSame(state1, state2) {
    let isLinkTheSame = (REPORT_LINK_STATUS) ? state1["isLinkSelected"] === state2["isLinkSelected"] : true

    return stateCommands.every(property => state1[property] === state2[property])
            && valueCommands.every(property => state1[property] === state2[property])
            && isLinkTheSame
}

function updateWebViewHeightWithBodyHeight() {
    let documentElement = document.documentElement
    let paddingTop = parseInt(window.getComputedStyle(documentElement)["margin-top"])
    let paddingBottom = parseInt(window.getComputedStyle(documentElement)["margin-bottom"])

    window.editor.reportNewDocumentHeight((documentElement.offsetHeight + paddingTop + paddingBottom) * window.devicePixelRatio)
}