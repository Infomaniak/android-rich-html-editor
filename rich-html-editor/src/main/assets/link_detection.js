const DOCUMENT_POSITION_SAME = 0 // TODO: Check usefulness

function getAllLinksPartiallyContainedInsideSelection() {
    let elements = [...getEditor().querySelectorAll("a[href]")]
    const range = document.getSelection().getRangeAt(0)
    const { startContainer, endContainer } = range

    // TODO: Investigate RoosterJs issues with startContainer and endContainer https://github.com/microsoft/roosterjs/blob/b1d4bab67dcae342cfdc043a8cbe2b96bb823a44/packages/roosterjs-editor-dom/lib/utils/queryElements.ts#L30

    elements = elements.filter(element =>
        doesIntersectWithNodeRange(element, startContainer, endContainer)
    )

    return elements
}

function doesIntersectWithNodeRange(node, startNode, endNode) {
    const startPosition = node.compareDocumentPosition(startNode)
    const endPosition = node.compareDocumentPosition(endNode)
    const targetPositions = [DOCUMENT_POSITION_SAME, Node.DOCUMENT_POSITION_CONTAINS, Node.DOCUMENT_POSITION_CONTAINED_BY]

    return (
        doesPositionMatchTargets(startPosition, targetPositions) || // intersectStart
        doesPositionMatchTargets(endPosition, targetPositions) || // intersectEnd
        (doesPositionMatchTargets(startPosition, [Node.DOCUMENT_POSITION_PRECEDING]) && // Contains
            doesPositionMatchTargets(endPosition, [Node.DOCUMENT_POSITION_FOLLOWING]) &&
            !doesPositionMatchTargets(endPosition, [Node.DOCUMENT_POSITION_CONTAINED_BY]))
    )
}

function doesPositionMatchTargets(position, targets) {
    return targets.some(target =>
        target === DOCUMENT_POSITION_SAME ? position === DOCUMENT_POSITION_SAME : (position & target) === target
    )
}
