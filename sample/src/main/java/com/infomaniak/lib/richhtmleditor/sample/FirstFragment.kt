package com.infomaniak.lib.richhtmleditor.sample

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infomaniak.lib.richhtmleditor.sample.databinding.CreateLinkTextInputBinding
import com.infomaniak.lib.richhtmleditor.sample.databinding.FragmentFirstBinding
import kotlinx.coroutines.launch
import java.io.BufferedReader

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView
    private val mainViewModel: MainViewModel by activityViewModels()

    private val createLinkDialog by lazy { CreateLinkDialog() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentFirstBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        val customCss = readAsset("editor_custom_css.css")
        val html = readAsset("example1.html")

        editor.apply {
            setHtml(html, customCss = listOf(customCss))
            isVisible = true
        }

        setEditorButtonClickListeners()
        observeEditorStatusUpdates()
    }

    private fun setEditorButtonClickListeners() = with(binding) {
        buttonBold.setOnClickListener { editor.textFormat.setBold() }
        buttonItalic.setOnClickListener { editor.textFormat.setItalic() }
        buttonStrikeThrough.setOnClickListener { editor.textFormat.setStrikeThrough() }
        buttonUnderline.setOnClickListener { editor.textFormat.setUnderline() }
        buttonRemoveFormat.setOnClickListener { editor.textFormat.removeFormat() }
        buttonLink.setOnClickListener {
            if (buttonLink.isActivated) {
            } else {
                createLinkDialog.show("", "") { url, _ ->
                    editor.textFormat.createLink(url)
                }
            }
        }

        buttonExportHtml.setOnClickListener { editor.exportHtml { html -> Log.e("gibran", "onViewCreated - html: ${html}") } }
    }

    private fun observeEditorStatusUpdates() = with(binding) {
        viewLifecycleOwner.lifecycleScope.launch {
            editor.textFormat.editorStatusesFlow.collect {
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
