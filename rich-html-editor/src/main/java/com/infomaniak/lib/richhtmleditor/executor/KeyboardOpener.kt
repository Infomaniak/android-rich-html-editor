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
