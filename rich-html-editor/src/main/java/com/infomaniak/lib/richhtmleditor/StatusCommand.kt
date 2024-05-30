package com.infomaniak.lib.richhtmleditor


enum class StatusCommand(override val argumentName: String, val statusType: StatusType) :
    ExecCommand {
    BOLD("bold", StatusType.STATE),
    ITALIC("italic", StatusType.STATE),
    STRIKE_THROUGH("strikeThrough", StatusType.STATE),
    UNDERLINE("underline", StatusType.STATE),
    FONT_NAME("fontName", StatusType.VALUE),
    FONT_SIZE("fontSize", StatusType.VALUE),
    TEXT_COLOR("foreColor", StatusType.VALUE),
    BACKGROUND_COLOR("backColor", StatusType.VALUE),
    CREATE_LINK("createLink", StatusType.COMPLEX),
}

enum class OtherCommand(override val argumentName: String) : ExecCommand {
    REMOVE_FORMAT("removeFormat"),
}

interface ExecCommand {
    val argumentName: String
}

enum class StatusType { STATE, VALUE, COMPLEX }