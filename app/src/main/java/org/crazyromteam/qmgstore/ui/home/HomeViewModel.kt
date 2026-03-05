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
import org.crazyromteam.qmgstore.api.RetrofitClient
import org.crazyromteam.qmgstore.api.ThemeItem


class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome to QMG Store"
    }
    val text: LiveData<String> = _text

    private val _themes = MutableLiveData<List<ThemeItem>>()
    val themes: LiveData<List<ThemeItem>> = _themes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    init {
        fetchThemes()
    }

    private fun fetchThemes() {
        viewModelScope.launch {
            try {
                val themeResponse = RetrofitClient.apiService.getThemes()
                val themeList = themeResponse.values.flatten()
                _themes.postValue(themeList)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching themes", e)
                _error.postValue(e.message)

                // For demonstration, let's load some mock data if server is unreachable
                _themes.postValue(getMockThemes())
            }
        }
    }

    private fun getMockThemes(): List<ThemeItem> {
        return listOf(
            ThemeItem("Neon Cyber", "Alice", "https://picsum.photos/seed/1/200/300"),
            ThemeItem("Dark Elegance", "Bob", "https://picsum.photos/seed/2/200/300"),
            ThemeItem("Ocean Breeze", "Charlie", "https://picsum.photos/seed/3/200/300"),
            ThemeItem("Minimal White", "Diana", "https://picsum.photos/seed/4/200/300"),
            ThemeItem("Retro 80s", "Eve", "https://picsum.photos/seed/5/200/300"),
            ThemeItem("Nature Green", "Frank", "https://picsum.photos/seed/6/200/300"),
            ThemeItem("Galaxy Deep", "Grace", "https://picsum.photos/seed/7/200/300"),
            ThemeItem("Material UI", "Hank", "https://picsum.photos/seed/8/200/300"),
            ThemeItem("Sunset Orange", "Ivy", "https://picsum.photos/seed/9/200/300")
        )
    }
}
