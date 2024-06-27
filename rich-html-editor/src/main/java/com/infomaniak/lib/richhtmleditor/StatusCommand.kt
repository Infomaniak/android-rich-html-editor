/*
 * Infomaniak Rich HTML Editor - Android
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.infomaniak.lib.richhtmleditor

enum class StatusCommand(override val argumentName: String, val statusType: StatusType) : ExecCommand {
    BOLD("bold", StatusType.STATE),
    ITALIC("italic", StatusType.STATE),
    STRIKE_THROUGH("strikeThrough", StatusType.STATE),
    UNDERLINE("underline", StatusType.STATE),
    ORDERED_LIST("insertOrderedList", StatusType.STATE),
    UNORDERED_LIST("insertUnorderedList", StatusType.STATE),
    SUBSCRIPT("subscript", StatusType.STATE),
    SUPERSCRIPT("superscript", StatusType.STATE),
    JUSTIFY_LEFT("justifyLeft", StatusType.STATE),
    JUSTIFY_CENTER("justifyCenter", StatusType.STATE),
    JUSTIFY_RIGHT("justifyRight", StatusType.STATE),
    JUSTIFY_FULL("justifyFull", StatusType.STATE),
    FONT_NAME("fontName", StatusType.VALUE),
    FONT_SIZE("fontSize", StatusType.VALUE),
    TEXT_COLOR("foreColor", StatusType.VALUE),
    BACKGROUND_COLOR("backColor", StatusType.VALUE),
    CREATE_LINK("createLink", StatusType.COMPLEX),
}

enum class OtherCommand(override val argumentName: String) : ExecCommand {
    REMOVE_FORMAT("removeFormat"),
    INDENT("indent"),
    OUTDENT("outdent"),
    UNDO("undo"),
    REDO("redo"),
}

interface ExecCommand {
    val argumentName: String
}

enum class StatusType { STATE, VALUE, COMPLEX }
