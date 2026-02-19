package org.crazyromteam.qmgstore.ui.home


import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.crazyromteam.qmgstore.qmg.DecodeQmg
import java.nio.ByteBuffer
import kotlinx.coroutines.*
import androidx.core.graphics.createBitmap


class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is Home Fragment"
    }
    val text: LiveData<String> = _text
}
