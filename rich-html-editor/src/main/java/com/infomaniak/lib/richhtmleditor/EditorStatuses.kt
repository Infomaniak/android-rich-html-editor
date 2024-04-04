package com.infomaniak.lib.richhtmleditor

import androidx.annotation.ColorInt
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class EditorStatuses(
    var isBold: Boolean = false,
    var isItalic: Boolean = false,
    var isStrikeThrough: Boolean = false,
    var isUnderlined: Boolean = false,
    var fontName: String? = null,
    var fontSize: Float? = null,
    var textColor: Int? = null,
    var backgroundColor: Int? = null,
) {
    private val mutex = Mutex()

    suspend fun updateStatusesAtomically(
        isBold: Boolean,
        isItalic: Boolean,
        isStrikeThrough: Boolean,
        isUnderlined: Boolean,
        fontName: String,
        fontSize: Float?,
        @ColorInt textColor: Int?,
        @ColorInt backgroundColor: Int?,
    ) {
        mutex.withLock {
            this.isBold = isBold
            this.isItalic = isItalic
            this.isStrikeThrough = isStrikeThrough
            this.isUnderlined = isUnderlined
            this.fontName = fontName
            this.fontSize = fontSize
            this.textColor = textColor
            this.backgroundColor = backgroundColor
        }
    }

    suspend fun updateStatusAtomically(command: TextFormat.EditorStatusCommand, value: Any) {
        mutex.withLock {
            when (command) {
                TextFormat.EditorStatusCommand.BOLD -> this.isBold = value as Boolean
                TextFormat.EditorStatusCommand.ITALIC -> this.isItalic = value as Boolean
                TextFormat.EditorStatusCommand.STRIKE_THROUGH -> this.isStrikeThrough = value as Boolean
                TextFormat.EditorStatusCommand.UNDERLINE -> this.isUnderlined = value as Boolean
                TextFormat.EditorStatusCommand.FONT_NAME -> this.fontName = value as String
                TextFormat.EditorStatusCommand.FONT_SIZE -> this.fontSize = value as Float
                TextFormat.EditorStatusCommand.TEXT_COLOR -> this.textColor = value as Int
                TextFormat.EditorStatusCommand.BACKGROUND_COLOR -> this.backgroundColor = value as Int
            }
        }
    }
}
