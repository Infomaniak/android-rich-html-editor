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
