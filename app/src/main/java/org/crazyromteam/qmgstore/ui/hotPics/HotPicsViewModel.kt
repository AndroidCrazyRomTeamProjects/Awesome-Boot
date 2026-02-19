package org.crazyromteam.qmgstore.ui.hotPics

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HotPicsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Hot Pics Fragment"
    }
    val text: LiveData<String> = _text
}