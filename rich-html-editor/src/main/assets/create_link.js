function createLink(displayText, url) {
    let range = document.getSelection().getRangeAt(0)
    if (range.collapsed) {
        // There's no selection, only a cursor. We can add the link manually
        var anchor = getAnchorNodeAtCursor()

        // If there is already a link, just change its href
        if (anchor) {
            anchor.href = url;
            // Change text content if it is specified
            updateAnchorDisplayText(anchor, displayText);
        } else {
            anchor = document.createElement('A')
            anchor.textContent = displayText || url;
            anchor.href = url;
            range.insertNode(anchor)
        }
    } else {
        // There's already a selction so use execCommand to create a new link
        document.execCommand("createLink", false, url)

        // Update the newly created link's display text if we have a custom text
        if (displayText) {
            let anchor = getAnchorNodeAtCursor(editor)
            updateAnchorDisplayText(anchor, displayText)
        }
    }
}

function getAnchorNodeAtCursor() {
    let anchors = getAllLinksPartiallyContainedInsideSelection()
    return anchors.length > 0 ? anchors[0] : null
}

function updateAnchorDisplayText(anchor, displayText) {
    if (displayText && anchor.textContent != displayText) {
        anchor.textContent = displayText;
    }
}
