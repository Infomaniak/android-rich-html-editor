package com.infomaniak.lib.richhtmleditor.executor

import android.app.Activity
import android.view.View
import android.view.inputmethod.InputMethodManager

internal class KeyboardOpener(private val view: View) : JsLifecycleAwareExecutor<Unit>() {
    override fun executeImmediately(value: Unit) {
        if (view.requestFocus()) {
            val inputMethodManager = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
