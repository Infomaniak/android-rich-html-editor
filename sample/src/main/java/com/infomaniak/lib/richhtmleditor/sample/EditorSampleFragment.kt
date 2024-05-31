package com.infomaniak.lib.richhtmleditor.sample

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.view.forEach
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infomaniak.lib.richhtmleditor.sample.databinding.CreateLinkTextInputBinding
import com.infomaniak.lib.richhtmleditor.sample.databinding.FragmentEditorSampleBinding
import kotlinx.coroutines.launch
import java.io.BufferedReader

class EditorSampleFragment : Fragment() {

    private var _binding: FragmentEditorSampleBinding? = null
    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView

    private val createLinkDialog by lazy { CreateLinkDialog() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentEditorSampleBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val html = readAsset("example1.html")

        setToolbarEnabledStatus(false)
        editor.apply {
            // addCss(readAsset("editor_custom_css.css"))
            // addScript("document.body.style['background'] = '#00FFFF'")

            setHtml(html)

            isVisible = true

            setOnFocusChangeListener { _, hasFocus -> setToolbarEnabledStatus(hasFocus) }
        }

        setEditorButtonClickListeners()
        observeEditorStatusUpdates()
    }

    private fun setEditorButtonClickListeners() = with(binding) {
        buttonBold.setOnClickListener { editor.toggleBold() }
        buttonItalic.setOnClickListener { editor.toggleItalic() }
        buttonStrikeThrough.setOnClickListener { editor.toggleStrikeThrough() }
        buttonUnderline.setOnClickListener { editor.toggleUnderline() }
        buttonRemoveFormat.setOnClickListener { editor.removeFormat() }
        buttonLink.setOnClickListener {
            if (buttonLink.isActivated) {
                editor.unlink()
            } else {
                createLinkDialog.show("", "") { url, displayText ->
                    editor.createLink(displayText, url)
                }
            }
        }

        buttonExportHtml.setOnClickListener { editor.exportHtml { html -> Log.d("editor", "Output html: $html") } }

        removeEditorFocusButton.setOnClickListener {
            editor.clearFocus()
            val inputMethodManager = requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editor.windowToken, 0)
        }
        focusEditorButton.setOnClickListener { editor.requestFocusAndOpenKeyboard() }
    }

    private fun observeEditorStatusUpdates() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            editor.editorStatusesFlow.collect {
                buttonBold.isActivated = it.isBold
                buttonItalic.isActivated = it.isItalic
                buttonStrikeThrough.isActivated = it.isStrikeThrough
                buttonUnderline.isActivated = it.isUnderlined

                buttonLink.isActivated = it.isLinkSelected
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun readAsset(fileName: String): String {
        return requireContext().assets
            .open(fileName)
            .bufferedReader()
            .use(BufferedReader::readText)
    }

    private fun setToolbarEnabledStatus(isEnabled: Boolean) {
        binding.toolbarLayout.forEach { view -> view.isEnabled = isEnabled }
    }

    inner class CreateLinkDialog {
        private var callback: ((String, String) -> Unit)? = null
        private var binding: CreateLinkTextInputBinding

        private val dialog = with(CreateLinkTextInputBinding.inflate(layoutInflater)) {
            binding = this
            MaterialAlertDialogBuilder(requireContext())
                .setView(root)
                .setTitle(R.string.link_dialog_title)
                .setPositiveButton(R.string.link_dialog_title_button_positive) { _, _ ->
                    callback?.invoke(
                        textInputEditTextUrl.text.toString(),
                        textInputEditTextPlaceholder.text.toString()
                    )
                }
                .setNegativeButton(R.string.link_dialog_title_button_negative, null)
                .create()
        }

        fun show(url: String? = null, text: String?, callback: (String, String) -> Unit) {
            this.callback = callback
            with(binding) {
                textInputEditTextUrl.setText(url ?: "")
                textInputEditTextPlaceholder.setText(text ?: "")
            }
            dialog.show()
        }
    }
}
