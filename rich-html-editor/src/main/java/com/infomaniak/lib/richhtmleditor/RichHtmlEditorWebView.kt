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
import com.infomaniak.lib.richhtmleditor.executor.JsExecutableMethod
import com.infomaniak.lib.richhtmleditor.executor.JsExecutor
import com.infomaniak.lib.richhtmleditor.executor.KeyboardOpener
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection
import com.infomaniak.lib.richhtmleditor.executor.ScriptCssInjector.CodeInjection.InjectionType
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

/**
 * A custom WebView designed to provide simple formatting and editing capabilities to an existing HTML content.
 *
 * The [RichHtmlEditorWebView] class utilizes the `contenteditable` attribute in HTML along with a combination of `execCommand`
 * and custom logic to enable editing and basic formatting of existing HTML.
 *
 * When [setHtml] is called, it inserts the provided HTML content into the [RichHtmlEditorWebView]'s editor template HTML and
 * activates the necessary JavaScript mechanisms for the editor to update different format statuses and function properly.
 *
 * To interact with the editor, you can either listen to format status notifications or call methods to modify the current format
 * such as [toggleBold].
 */
class RichHtmlEditorWebView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : WebView(context, attrs, defStyleAttr) {

    private var keepKeyboardOpenedOnConfigurationChanged: Boolean = false

    private val documentInitializer = DocumentInitializer()
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

    /**
     * Flow that is notified everytime a subscribed [EditorStatuses] is updated.
     *
     * You can use this flow to listen to subscribed [EditorStatuses] and update your toolbar's UI accordingly to show which
     * formatting is enabled on the current selection.
     */
    val editorStatusesFlow: Flow<EditorStatuses> by jsBridge::editorStatusesFlow

    private var htmlExportCallback: ((html: String) -> Unit)? = null

    init {
        @SuppressLint("SetJavaScriptEnabled")
        settings.javaScriptEnabled = true
        isFocusableInTouchMode = true // Else the WebView can't be written in

        webViewClient = RichHtmlEditorWebViewClient(::notifyPageHasLoaded)

        addJavascriptInterface(jsBridge, "editor")
    }

    /**
     * Initializes the [RichHtmlEditorWebView] with the HTML content to be displayed.
     *
     * This method initializes the WebView to work properly and can also load the provided HTML content and subscribed states. No
     * HTML content is needed to have an empty editor; calling the method without parameters will still initialize everything
     * necessary for the editor to function properly.
     *
     * @param html The HTML content to be displayed. Defaults to an empty string. If the string is empty it means the editor will
     * start empty and simply won't load any initial data
     * @param subscribedStates A set of [StatusCommand] to subscribe to. Defaults to null, meaning all available [StatusCommand]
     * will be subscribed to.
     *
     * Example usage:
     * ```
     * val htmlContent = "<p>Hello, World!</p>"
     * val subscribedStates = setOf(StatusCommand.BOLD, StatusCommand.ITALIC)
     *
     * setHtml(htmlContent, subscribedStates)
     * ```
     */
    fun setHtml(html: String = "", subscribedStates: Set<StatusCommand>? = null) {
        documentInitializer.init(html, subscribedStates)

        val template = context.readAsset("editor_template.html")
        super.loadDataWithBaseURL("", template, "text/html", "UTF-8", null)
    }

    fun addCss(css: String) {
        scriptCssInjector.executeWhenDomIsLoaded(CodeInjection(InjectionType.CSS, css))
    }

    /**
     * Injects a custom script inside the editor template's `<head>`.
     *
     * The html loaded with [setHtml] is not guaranteed to be loaded inside the editor by the time this injected script is loaded
     * */
    fun addScript(script: String) {
        scriptCssInjector.executeWhenDomIsLoaded(CodeInjection(InjectionType.SCRIPT, script))
    }

    fun toggleBold() = jsBridge.toggleBold()
    fun toggleItalic() = jsBridge.toggleItalic()
    fun toggleStrikeThrough() = jsBridge.toggleStrikeThrough()
    fun toggleUnderline() = jsBridge.toggleUnderline()
    fun removeFormat() = jsBridge.removeFormat()
    fun setTextColor(@ColorInt color: Int) = jsBridge.setTextColor(JsColor(color))
    fun setTextBackgroundColor(@ColorInt color: Int) = jsBridge.setTextBackgroundColor(JsColor(color))
    fun setFontSize(@IntRange(from = 1, to = 7) fontSize: Int) = jsBridge.setFontSize(fontSize)
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

        jsExecutor.notifyDomLoaded()
        scriptCssInjector.notifyDomLoaded()
        keyboardOpener.notifyDomLoaded()
    }

    fun requestFocusAndOpenKeyboard() {
        keepKeyboardOpenedOnConfigurationChanged = true
        keyboardOpener.executeWhenDomIsLoaded(Unit)
    }

    fun exportHtml(resultCallback: (html: String) -> Unit) {
        htmlExportCallback = resultCallback
        jsExecutor.executeWhenDomIsLoaded(JsExecutableMethod("exportHtml"))
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
        htmlExportCallback?.invoke(html)
        htmlExportCallback = null
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
