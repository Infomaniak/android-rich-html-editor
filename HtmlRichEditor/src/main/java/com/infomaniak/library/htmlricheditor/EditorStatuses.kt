package com.infomaniak.library.htmlricheditor

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
        fontSize: Float,
        @ColorInt textColor: Int,
        @ColorInt backgroundColor: Int,
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
}
