package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.repository.ShuttleRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ShuttleStopSearchViewModel(
    private val shuttleRepository: ShuttleRepository
) : ViewModel() {

    private val _allStops = MutableStateFlow<List<String>>(emptyList())
    
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    private val _filteredStops = MutableStateFlow<List<String>>(emptyList())
    val filteredStops: StateFlow<List<String>> = _filteredStops

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadStops()
        observeQuery()
    }

    private fun loadStops() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val stops = shuttleRepository.fetchStops()
                _allStops.value = stops
                _filteredStops.value = stops
            } catch (e: Exception) {
                // handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeQuery() {
        _query
            .onEach { q ->
                if (q.isBlank()) {
                    _filteredStops.value = _allStops.value
                } else {
                    _filteredStops.value = _allStops.value.filter {
                        it.contains(q, ignoreCase = true)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun onQueryChanged(newQuery: String) {
        _query.value = newQuery
    }
}

class ShuttleStopSearchViewModelFactory(
    private val repository: ShuttleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleStopSearchViewModel(repository) as T
    }
}
