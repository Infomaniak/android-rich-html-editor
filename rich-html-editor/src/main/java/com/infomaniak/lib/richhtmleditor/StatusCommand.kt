/*
 * Infomaniak Rich HTML Editor - Android
 * Copyright (C) 2024 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
    CREATE_LINK("", StatusType.COMPLEX), // This value is not meant to be called by JsBride's execCommand()
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
