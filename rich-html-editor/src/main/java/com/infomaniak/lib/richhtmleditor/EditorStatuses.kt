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
    var isSubscript: Boolean = false,
    var isSuperscript: Boolean = false,
    var justification: Justification? = null,
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
        isSubscript: Boolean,
        isSuperscript: Boolean,
        justification: Justification?,
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
            this.isSubscript = isSubscript
            this.isSuperscript = isSuperscript
            this.justification = justification
        }
    }
}
