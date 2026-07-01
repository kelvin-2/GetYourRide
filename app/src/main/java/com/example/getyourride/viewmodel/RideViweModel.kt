package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

sealed interface TripsUiState {
    object Loading : TripsUiState
    data class Success(val trips: List<TripResponse>) : TripsUiState
    data class Error(val message: String) : TripsUiState
}

class RideViewModel(private val repository: TripRepository) : ViewModel() {

    var uiState: TripsUiState by mutableStateOf(TripsUiState.Loading)
        private set

    init {
        loadAvailableTrips()
    }

    fun loadAvailableTrips() {
        viewModelScope.launch {
            uiState = TripsUiState.Loading
            repository.getAvailableTrips()
                .onSuccess { trips -> uiState = TripsUiState.Success(trips) }
                .onFailure { e -> uiState = TripsUiState.Error(e.message ?: "Something went wrong") }
        }
    }
}