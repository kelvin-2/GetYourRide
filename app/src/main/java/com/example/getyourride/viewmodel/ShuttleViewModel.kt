package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.repository.ShuttleRepository
import kotlinx.coroutines.launch

/**
 * Shuttle-side equivalent of RideViewModel. Deliberately separate — shuttles
 * and carpools don't share a repository, data model, or backend endpoint.
 */
class ShuttleViewModel(
    private val repository: ShuttleRepository
) : ViewModel() {

    var uiState: ShuttleUiState by mutableStateOf(ShuttleUiState.Loading)
        private set

    fun loadShuttleHomeData() {
        uiState = ShuttleUiState.Loading
        viewModelScope.launch {
            try {
                val data = repository.fetchShuttleHomeData()
                uiState = ShuttleUiState.Success(
                    upcomingShuttles = data.upcomingShuttles,
                    recentTrips = data.recentTrips
                )
            } catch (e: Exception) {
                uiState = ShuttleUiState.Error(e.message ?: "Failed to load shuttle data")
            }
        }
    }
}

class ShuttleViewModelFactory(
    private val repository: ShuttleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleViewModel(repository) as T
    }
}