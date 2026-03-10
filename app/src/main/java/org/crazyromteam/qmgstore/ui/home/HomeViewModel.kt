package org.crazyromteam.qmgstore.ui.home


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import org.crazyromteam.qmgstore.api.RetrofitClient
import org.crazyromteam.qmgstore.api.ThemeItem


class HomeViewModel : ViewModel() {


    private val _themes = MutableLiveData<List<ThemeItem>>()
    val themes: LiveData<List<ThemeItem>> = _themes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        fetchThemes()
    }

    fun fetchThemes() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            _error.postValue("") // Reset error on fetch (using empty string instead of null as requested by previous compiler error)
            try {
                val themeResponse = RetrofitClient.apiService.getThemes()
                val themeList = themeResponse.flatMap { (id, items) ->
                    items.onEach { it.id = id }
                }
                _themes.postValue(themeList)
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching themes", e)
                _error.postValue(e.message ?: "Unknown error")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
