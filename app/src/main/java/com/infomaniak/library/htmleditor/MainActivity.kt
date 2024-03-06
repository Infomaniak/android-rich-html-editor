package com.infomaniak.library.htmleditor

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.infomaniak.library.htmleditor.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
    }
}
