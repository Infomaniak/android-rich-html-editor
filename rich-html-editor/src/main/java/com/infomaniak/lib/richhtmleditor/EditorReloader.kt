package com.infomaniak.lib.richhtmleditor

import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A utility class to help easily reload the html content of a [RichHtmlEditorWebView] on configuration changes.
 *
 * This class is meant to be instantiated inside a ViewModel to be able to retain the HTML content through configuration changes.
 *
 * @param coroutineScope A coroutine scope where the exportation of the HTML will be processed. Preferably the viewModelScope of
 * the ViewModel where this class has been instantiated.
 *
 * @see load
 * @see save
 */
class EditorReloader(private val coroutineScope: CoroutineScope) {

    private var needToReloadHtml: Boolean = false
    private var savedHtml = MutableStateFlow<String?>(null)

    /**
     * An alternative for loading the HTML content of the [RichHtmlEditorWebView].
     *
     * This method can be called within the `onViewCreated()` method of the Fragment containing your [RichHtmlEditorWebView].
     * On the initial call, it loads the default HTML content. For all subsequent calls, it reloads the previously loaded HTML
     * content.
     *
     * @param editor The editor to load content into.
     * @param defaultHtml The HTML to load on the initial call. On subsequent calls, this value won't be taken into account.
     *
     * Usage:
     * ```
     * override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
     *     super.onViewCreated(view, savedInstanceState)
     *     lifecycleScope.launch {
     *         viewModel.editorReloader.load(editor, "<p>Hello World</p>")
     *     }
     * }
     * ```
     *
     * @throws IllegalStateException If the method is not called on the main thread.
     */
    suspend fun load(editor: RichHtmlEditorWebView, defaultHtml: String) {
        if (Looper.myLooper() != Looper.getMainLooper()) error("The load method needs to be called on the main thread")

        if (needToReloadHtml) {
            savedHtml.collectLatest {
                if (it == null) return@collectLatest

                resetSavedHtml()
                editor.setHtml(it)
            }
        } else {
            editor.setHtml(defaultHtml)
        }

        enableHtmlReload()
    }

    /**
     * Exports and saves the editor's HTML content for later reloading.
     *
     * This method should be called within the `onSaveInstanceState()` method of the Fragment.
     *
     * @param editor The editor whose content needs to be saved.
     *
     * Usage:
     * ```
     * override fun onSaveInstanceState(outState: Bundle) {
     *     super.onSaveInstanceState(outState)
     *     viewModel.editorReloader.save(editor)
     * }
     * ```
     */
    fun save(editor: RichHtmlEditorWebView) {
        editor.exportHtml {
            coroutineScope.launch { savedHtml.emit(it) }
        }
    }

    private suspend fun resetSavedHtml() {
        savedHtml.emit(null)
    }

    private fun enableHtmlReload() {
        needToReloadHtml = true
    }
}
