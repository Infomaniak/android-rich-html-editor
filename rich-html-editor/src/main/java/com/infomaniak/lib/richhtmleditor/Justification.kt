package com.infomaniak.lib.richhtmleditor

enum class Justification(val execCommand: StatusCommand) {
    LEFT(StatusCommand.JUSTIFY_LEFT),
    CENTER(StatusCommand.JUSTIFY_CENTER),
    RIGHT(StatusCommand.JUSTIFY_RIGHT),
    FULL(StatusCommand.JUSTIFY_FULL),
}
