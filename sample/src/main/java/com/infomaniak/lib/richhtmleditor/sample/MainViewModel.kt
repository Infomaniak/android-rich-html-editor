package com.infomaniak.lib.richhtmleditor.sample

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    var linkUrl = MutableLiveData<String?>()
    var linkText = MutableLiveData<String?>()
}
