function reportCommandStatusChange() {
    window.editor.reportCommandDataChange(
        /* isBold */ document.queryCommandState("bold"),
        /* isItalic */ document.queryCommandState("italic"),
        /* isStrikeThrough */ document.queryCommandState("strikeThrough"),
        /* isUnderlined */ document.queryCommandState("underline"),
        /* fontName */ "Calibri",
        /* fontSize */ 12345,
        /* textColor */ "#123456",
        /* backgroundColor */ "#123456",
    );
}

document.addEventListener("selectionchange", reportCommandStatusChange)
