package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

/**
 * UI state for the Driver Home screen.
 */
sealed class DriverHomeUiState {
    data object Loading : DriverHomeUiState()
    data class Success(
        val activeRides: List<TripResponse>,   // SCHEDULED or IN_PROGRESS
        val pastRides: List<TripResponse>      // COMPLETED or CANCELLED
    ) : DriverHomeUiState()
    data class Error(val message: String) : DriverHomeUiState()
}

class DriverHomeViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    var uiState by mutableStateOf<DriverHomeUiState>(DriverHomeUiState.Loading)
        private set

    fun loadMyTrips() {
        uiState = DriverHomeUiState.Loading
        viewModelScope.launch {
            val result = tripRepository.getMyTrips()
            result.onSuccess { trips ->
                val active = trips.filter { trip ->
                    trip.status.equals("SCHEDULED", ignoreCase = true) ||
                    trip.status.equals("IN_PROGRESS", ignoreCase = true) ||
                    trip.status.equals("CONFIRMED", ignoreCase = true)
                }.sortedByDescending { it.departureTime }

                val past = trips.filter { trip ->
                    trip.status.equals("COMPLETED", ignoreCase = true) ||
                    trip.status.equals("CANCELLED", ignoreCase = true)
                }.sortedByDescending { it.departureTime }

                uiState = DriverHomeUiState.Success(
                    activeRides = active,
                    pastRides = past
                )
            }.onFailure { error ->
                uiState = DriverHomeUiState.Error(
                    error.message ?: "Failed to load your trips."
                )
            }
        }
    }
}

class DriverHomeViewModelFactory(
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DriverHomeViewModel(tripRepository) as T
    }
}
