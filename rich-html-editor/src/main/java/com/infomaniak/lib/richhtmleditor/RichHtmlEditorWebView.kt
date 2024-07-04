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

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.AbsSavedState
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.ColorInt
import androidx.annotation.IntRange
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import com.infomaniak.lib.richhtmleditor.JsBridge.Companion.FONT_MAX_SIZE
import com.infomaniak.lib.richhtmleditor.JsBridge.Companion.FONT_MIN_SIZE
import com.infomaniak.lib.richhtmleditor.executor.HtmlSetter
import com.infomaniak.lib.richhtmleditor.executor.JsExecutableMethod
import com.infomaniak.lib.richhtmleditor.executor.JsExecutor
import com.infomaniak.lib.richhtmleditor.executor.KeyboardOpener
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection.InjectionType
import com.infomaniak.lib.richhtmleditor.executor.StateSubscriber
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.roundToInt

/**
 * A custom WebView designed to provide simple formatting and editing capabilities to an existing HTML content.
 *
 * The [RichHtmlEditorWebView] class utilizes the `contenteditable` attribute in HTML along with a combination of `execCommand`
 * and custom logic to enable editing and basic formatting of existing HTML.
 *
 * When the [RichHtmlEditorWebView] is instantiated it loads its HTML template and activates the necessary JavaScript mechanisms
 * for the editor to update different format statuses and function properly.
 *
 * To interact with the editor, you can either listen to format status notifications or call methods to modify the current format
 * such as [toggleBold].
 *
 * @see setHtml
 * @see subscribeToStates
 * @see addCss
 * @see addScript
 */
class RichHtmlEditorWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    private var keepKeyboardOpenedOnConfigurationChanged: Boolean = false

    private val documentInitializer = DocumentInitializer()
    private val stateSubscriber = StateSubscriber(this)
    private val htmlSetter = HtmlSetter(this)
    private val jsExecutor = JsExecutor(this)
    private val scriptCssInjector = ScriptCssInjector(this)
    private val keyboardOpener = KeyboardOpener(this)

    private val jsBridgeJob = Job()
    private val jsBridge = JsBridge(
        coroutineScope = CoroutineScope(CoroutineName("JsBridgeCoroutine") + jsBridgeJob),
        jsExecutor = jsExecutor,
        notifyExportedHtml = ::notifyExportedHtml,
        requestRectangleOnScreen = ::requestRectangleOnScreen,
        updateWebViewHeight = ::updateWebViewHeight,
    )

    @OptIn(DelicateCoroutinesApi::class)
    private val htmlExportCoroutineScope = CoroutineScope(newSingleThreadContext("HtmlExportCoroutine"))

    /**
     * Flow that is notified everytime a subscribed [EditorStatuses] is updated.
     *
     * You can use this flow to listen to subscribed [EditorStatuses] and update your toolbar's UI accordingly to show which
     * formatting is enabled on the current selection.
     *
     * @see subscribeToStates
     */
    val editorStatusesFlow: Flow<EditorStatuses> by jsBridge::editorStatusesFlow

    private var htmlExportCallback: MutableList<((html: String) -> Unit)> = mutableListOf()

    private val htmlExportMutex = Mutex()

    init {
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        isFocusableInTouchMode = true // Else the WebView can't be written in

        webViewClient = RichHtmlEditorWebViewClient(::notifyPageHasLoaded)

        addJavascriptInterface(jsBridge, "editor")

        stateSubscriber.executeWhenDomIsLoaded(null)
        val template = context.readAsset("editor_template.html")
        super.loadDataWithBaseURL("", template, "text/html", "UTF-8", null)

    }

    /**
     * Sets the HTML content that will be displayed inside of the editor.
     *
     * @param html A string containing the HTML content to be set.
     */
    fun setHtml(html: String) = htmlSetter.executeWhenDomIsLoaded(html)

    /**
     * Subscribes to a subset of states, ensuring that [editorStatusesFlow] triggers only when at least one of the specified
     * states changes.
     *
     * @param subscribedStates A set of [StatusCommand] to subscribe to. If `null`, all available [StatusCommand] will be
     * subscribed to.
     */
    fun subscribeToStates(subscribedStates: Set<StatusCommand>?) = stateSubscriber.executeWhenDomIsLoaded(subscribedStates)

    fun addCss(css: String) = scriptCssInjector.executeWhenDomIsLoaded(CodeInjection(InjectionType.CSS, css))

    /**
     * Injects a custom script inside the editor template's `<head>`.
     *
     * The html loaded with [initEditor] is not guaranteed to be loaded inside the editor by the time this injected script is
     * loaded
     * */
    fun addScript(script: String) = scriptCssInjector.executeWhenDomIsLoaded(CodeInjection(InjectionType.SCRIPT, script))

    fun toggleBold() = jsBridge.toggleBold()
    fun toggleItalic() = jsBridge.toggleItalic()
    fun toggleStrikeThrough() = jsBridge.toggleStrikeThrough()
    fun toggleUnderline() = jsBridge.toggleUnderline()
    fun toggleOrderedList() = jsBridge.toggleOrderedList()
    fun toggleUnorderedList() = jsBridge.toggleUnorderedList()
    fun toggleSubscript() = jsBridge.toggleSubscript()
    fun toggleSuperscript() = jsBridge.toggleSuperscript()
    fun removeFormat() = jsBridge.removeFormat()
    fun justify(justification: Justification) = jsBridge.justify(justification)
    fun indent() = jsBridge.indent()
    fun outdent() = jsBridge.outdent()
    fun setTextColor(@ColorInt color: Int) = jsBridge.setTextColor(JsColor(color))
    fun setTextBackgroundColor(@ColorInt color: Int) = jsBridge.setTextBackgroundColor(JsColor(color))

    /**
     * Updates the font size of the text.
     *
     * @param fontSize The new size of the text. This value's range constraint comes from the JavaScript `execCommand` method
     * called with the argument `fontSize`.
     */
    fun setFontSize(@IntRange(from = FONT_MIN_SIZE, to = FONT_MAX_SIZE) fontSize: Int) = jsBridge.setFontSize(fontSize)
    fun undo() = jsBridge.undo()
    fun redo() = jsBridge.redo()
    fun createLink(displayText: String?, url: String) = jsBridge.createLink(displayText?.takeIf { it.isNotBlank() }, url)
    fun unlink() = jsBridge.unlink()

    /**
     * Notifies the [RichHtmlEditorWebView] to setup the editor.
     *
     * This method is only required if you want to use your own custom WebViewClient. To use your own custom WebViewClient call
     * this method inside [WebViewClient.onPageFinished] of your [WebViewClient] so the editor can setup itself correctly.
     */
    fun notifyPageHasLoaded() {
        documentInitializer.setupDocument(this)

        stateSubscriber.notifyDomLoaded()
        htmlSetter.notifyDomLoaded()
        jsExecutor.notifyDomLoaded()
        scriptCssInjector.notifyDomLoaded()
        keyboardOpener.notifyDomLoaded()
    }

    fun requestFocusAndOpenKeyboard() {
        keepKeyboardOpenedOnConfigurationChanged = true
        keyboardOpener.executeWhenDomIsLoaded(Unit)
    }

    fun exportHtml(resultCallback: (html: String) -> Unit) {
        htmlExportCoroutineScope.launch(Dispatchers.Main) {
            htmlExportMutex.withLock {
                val notYetRunning = htmlExportCallback.isEmpty()
                htmlExportCallback.add(resultCallback)
                if (notYetRunning) jsExecutor.executeWhenDomIsLoaded(JsExecutableMethod("exportHtml"))
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return bundleOf(
            KEYBOARD_SHOULD_REOPEN_KEY to keepKeyboardOpenedOnConfigurationChanged,
            SUPER_STATE_KEY to superState,
        )
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        (state as Bundle?)?.getBoolean(KEYBOARD_SHOULD_REOPEN_KEY)?.let { keepKeyboardOpenedOnConfigurationChanged = it }
        super.onRestoreInstanceState(state?.getParcelableCompat(SUPER_STATE_KEY, AbsSavedState::class.java))
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect)
        if (focused) {
            jsExecutor.executeWhenDomIsLoaded(JsExecutableMethod("requestFocus"))
            if (keepKeyboardOpenedOnConfigurationChanged) keyboardOpener.executeWhenDomIsLoaded(Unit)
        } else {
            keepKeyboardOpenedOnConfigurationChanged = false
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        keyboardOpener.removePendingListener()
        jsBridgeJob.cancel()
    }

    private fun notifyExportedHtml(html: String) {
        htmlExportCoroutineScope.launch(Dispatchers.Main) {
            htmlExportMutex.withLock {
                htmlExportCallback.forEach { it.invoke(html) }
                htmlExportCallback.clear()
            }
        }
    }

    private fun requestRectangleOnScreen(left: Int, top: Int, right: Int, bottom: Int) {
        val density: Float = resources.displayMetrics.density

        requestRectangleOnScreen(
            Rect(
                (left * density).roundToInt(),
                (top * density).roundToInt(),
                (right * density).roundToInt(),
                (bottom * density).roundToInt(),
            ),
        )
    }

    private fun updateWebViewHeight(newHeight: Int) {
        updateLayoutParams<ViewGroup.LayoutParams> {
            height = newHeight
        }
    }

    @Deprecated(
        "Use setHtml() instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadUrl(url: String) = unsupported()

    @Deprecated(
        "Use setHtml() instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadUrl(url: String, additionalHttpHeaders: MutableMap<String, String>) = unsupported()

    @Deprecated(
        "Use setHtml() instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadData(data: String, mimeType: String?, encoding: String?) = unsupported()

    @Deprecated(
        "Use setHtml() instead to initialize the editor with the desired HTML content.",
        ReplaceWith("setHtml()", "com.infomaniak.lib.richhtmleditor")
    )
    override fun loadDataWithBaseURL(baseUrl: String?, data: String, mimeType: String?, encoding: String?, historyUrl: String?) {
        unsupported()
    }

    private fun unsupported() {
        throw UnsupportedOperationException("Use setHtml() instead")
    }

    companion object {
        private const val KEYBOARD_SHOULD_REOPEN_KEY = "keyboardShouldReopen"
        private const val SUPER_STATE_KEY = "superState"
    }
}
