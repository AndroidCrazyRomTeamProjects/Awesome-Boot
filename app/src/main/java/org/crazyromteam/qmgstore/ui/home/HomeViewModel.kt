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

                // ⚡ Bolt Optimization: Avoid redundant intermediate list allocations and O(N) iterations
                // by using an ArrayList with initial capacity and a single pass to map the keys to items.
                val totalSize = themeResponse.values.sumOf { it.size }
                val themeList = ArrayList<ThemeItem>(totalSize)
                for ((id, items) in themeResponse) {
                    for (item in items) {
                        item.id = id
                        themeList.add(item)
                    }
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
