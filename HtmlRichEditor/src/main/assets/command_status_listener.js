const format = [
    "bold",
    "italic",
    "underline",
    "strikethrough"
];
const textInfo = [
    "fontName",
    "fontSize",
    "foreColor",
    "backColor"
];

let currentSelectionState = {};

function reportSelectionStateChangedIfNecessary() {
    const newSelectionState = getCurrentSelectionState();
    if (!areSelectionStatesTheSame(currentSelectionState, newSelectionState)) {
        currentSelectionState = newSelectionState;
        window.editor.reportCommandDataChange(
            newSelectionState["bold"],
            newSelectionState["italic"],
            newSelectionState["strikethrough"],
            newSelectionState["underline"],
            newSelectionState["fontName"],
            newSelectionState["fontSize"],
            newSelectionState["foreColor"],
            newSelectionState["backColor"]
        );
    }
}

function getCurrentSelectionState() {
    let currentState = {};

    for (const property of format) {
        currentState[property] = document.queryCommandState(property);
    }
    for (const property of textInfo) {
        currentState[property] = document.queryCommandValue(property);
    }

    return currentState;
}

function areSelectionStatesTheSame(state1, state2) {
    return format.every(property => state1[property] === state2[property]) && textInfo.every(property => state1[property] === state2[property]);
}

function onAttributesChange(callback) {
    const mutationObserver = new MutationObserver(callback);
    const config = { subtree: true, attributes: true };
    mutationObserver.observe(document.body, config);
}

document.addEventListener("selectionchange", reportSelectionStateChangedIfNecessary)

// On some occasions execCommand modifies the attributes of tags which doesn't trigger the "selectionchange" so this has been
// needed in order to catch some of these modifications we were lacking
onAttributesChange(reportSelectionStateChangedIfNecessary)
