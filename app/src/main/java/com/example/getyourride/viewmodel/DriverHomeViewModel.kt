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

    /**
     * Cancel a trip: calls backend PATCH /api/trips/{id}/cancel,
     * then moves it from active to past rides locally for instant feedback,
     * and reloads from server in background.
     */
    fun cancelRide(tripId: Long) {
        viewModelScope.launch {
            // Optimistic UI update: move trip to past rides immediately
            val currentState = uiState
            if (currentState is DriverHomeUiState.Success) {
                val cancelledTrip = currentState.activeRides.find { it.tripId == tripId }
                if (cancelledTrip != null) {
                    val updatedActive = currentState.activeRides.filter { it.tripId != tripId }
                    val updatedPast = listOf(
                        cancelledTrip.copy(status = "CANCELLED")
                    ) + currentState.pastRides
                    uiState = DriverHomeUiState.Success(
                        activeRides = updatedActive,
                        pastRides = updatedPast
                    )
                }
            }

            // Call backend
            val result = tripRepository.cancelTrip(tripId)
            result.onFailure {
                // If backend fails, reload to restore correct state
                loadMyTrips()
            }
            // On success the optimistic update is already correct
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
