package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

sealed interface AllTripsUiState {
    object Loading : AllTripsUiState
    data class Success(val trips: List<TripResponse>) : AllTripsUiState
    data class Error(val message: String) : AllTripsUiState
}

class AllRidesViewModel(
    private val repository: TripRepository
) : ViewModel() {

    var uiState: AllTripsUiState by mutableStateOf(AllTripsUiState.Loading)
        private set

    init {
        loadAllTrips()
    }

    fun loadAllTrips() {
        viewModelScope.launch {
            uiState = AllTripsUiState.Loading

            repository.getTrips()
                .onSuccess { trips ->
                    uiState = AllTripsUiState.Success(trips)
                }
                .onFailure { e ->
                    uiState = AllTripsUiState.Error(
                        e.message ?: "Something went wrong"
                    )
                }
        }
    }

    fun cancelTrip(tripId: Long) {
        viewModelScope.launch {
            repository.cancelTrip(tripId)
                .onSuccess { updatedTrip ->
                    val currentState = uiState
                    if (currentState is AllTripsUiState.Success) {
                        uiState = AllTripsUiState.Success(
                            currentState.trips.map { trip ->
                                if (trip.tripId == tripId) updatedTrip else trip
                            }
                        )
                    }
                }
                .onFailure { e ->
                    // Cancel failed — list stays as-is so the user doesn't lose
                    // their other trips from view. Surface this to the user
                    // (snackbar/toast) once you decide on an error-display pattern.
                }
        }
    }
}