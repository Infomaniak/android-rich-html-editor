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
    var isLinkSelected: Boolean = false,
    var isOrderedListSelected: Boolean = false,
    var isUnorderedListSelected: Boolean = false,
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
        isLinkSelected: Boolean,
        isOrderedListSelected: Boolean,
        isUnorderedListSelected: Boolean,
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
            this.isLinkSelected = isLinkSelected
            this.isOrderedListSelected = isOrderedListSelected
            this.isUnorderedListSelected = isUnorderedListSelected
        }
    }
}
