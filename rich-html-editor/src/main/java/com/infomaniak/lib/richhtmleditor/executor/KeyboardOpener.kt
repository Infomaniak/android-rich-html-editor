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
package com.infomaniak.lib.richhtmleditor.executor

import android.app.Activity
import android.view.View
import android.view.ViewTreeObserver
import android.view.inputmethod.InputMethodManager

internal class KeyboardOpener(private val view: View) : JsLifecycleAwareExecutor<Unit>() {

    private var listener: ViewTreeObserver.OnWindowFocusChangeListener? = null

    override fun executeImmediately(value: Unit) {
        if (view.requestFocus()) {
            if (view.hasWindowFocus()) {
                openKeyboard()
            } else {
                // The window won't have the focus most of the time when the configuration changes and we want to reopen the
                // keyboard right away. When this happen, we need to wait for the window to get the focus before opening the
                // keyboard.
                listener = object : ViewTreeObserver.OnWindowFocusChangeListener {
                    override fun onWindowFocusChanged(hasFocus: Boolean) {
                        if (hasFocus) {
                            openKeyboard()
                            view.viewTreeObserver.removeOnWindowFocusChangeListener(this)
                        }
                    }
                }

                view.viewTreeObserver.addOnWindowFocusChangeListener(listener)
            }
        }
    }

    fun removePendingListener() {
        view.viewTreeObserver.removeOnWindowFocusChangeListener(listener)
        listener = null
    }

    private fun openKeyboard() {
        val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }
}
