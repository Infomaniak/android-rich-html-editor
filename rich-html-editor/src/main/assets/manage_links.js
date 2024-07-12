// Create link

function createLink(displayText, url) {
    let range = getSelectionRangeOrNull()
    if (range === null) return

    if (range.collapsed) {
        // There's no selection, only a cursor. We can add the link manually
        let anchor = getAnchorNodeAtCursor()

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

        setCaretAtEndOfAnchor(anchor)
    } else {
        // There's already a selection so use execCommand to create a new link
        document.execCommand("createLink", false, url)

        // Update the newly created link's display text if we have a custom text
        if (displayText) {
            let anchor = getAnchorNodeAtCursor()
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

function setCaretAtEndOfAnchor(anchor) {
    const range = new Range();
    range.setStart(anchor, 1);
    range.setEnd(anchor, 1);
    range.collapsed = true;

    const selection = document.getSelection();
    selection.removeAllRanges();
    selection.addRange(range);
}

// Unlink

function unlink() {
    getAllLinksPartiallyContainedInsideSelection().forEach(anchor => unlinkAnchorNode(anchor))
}

function unlinkAnchorNode(anchor) {
    let selection = document.getSelection()
    if (selection.rangeCount === 0) return

    let range = selection.getRangeAt(0)
    let rangeBackup = range.cloneRange()
    range.selectNodeContents(anchor)
    document.execCommand("unlink")

    selection.removeAllRanges()
    selection.addRange(rangeBackup)
}
