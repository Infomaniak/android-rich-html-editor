package com.infomaniak.library.htmleditor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infomaniak.library.htmleditor.databinding.FragmentFirstBinding

class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!! // This property is only valid between onCreateView and onDestroyView

    private val testHtmlHard = """
        <h1 style="color:red; background-color:#8F0">Hello World</h1><blblue>Yo</blblue>
    """.trimIndent()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return FragmentFirstBinding.inflate(inflater, container, false).also { _binding = it }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?): Unit = with(binding) {
        super.onViewCreated(view, savedInstanceState)
        editor.loadHtml(testHtmlHard)

        buttonBold.setOnClickListener { editor.textFormat.setBold() }
        buttonItalic.setOnClickListener { editor.textFormat.setItalic() }
        buttonStrikeThrough.setOnClickListener { editor.textFormat.setStrikeThrough() }
        buttonUnderline.setOnClickListener { editor.textFormat.setUnderline() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
