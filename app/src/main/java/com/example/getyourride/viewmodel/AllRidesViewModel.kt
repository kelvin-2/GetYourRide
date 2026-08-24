package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripBookingResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

sealed interface AllTripsUiState {
    object Loading : AllTripsUiState
    data class Success(val bookings: List<TripBookingResponse>) : AllTripsUiState
    data class Error(val message: String) : AllTripsUiState
}

class AllRidesViewModel(
    private val repository: TripRepository
) : ViewModel() {

    var uiState: AllTripsUiState by mutableStateOf(AllTripsUiState.Loading)
        private set

    // NOTE: no init { loadAllTrips() } here on purpose — same reasoning as
    // RideViewModel. This ViewModel is constructed at the top of
    // MainActivity's setContent (app launch, pre-login), so an eager load
    // here hits the backend with no auth token and gets a 403.
    //
    // MyRidesScreen's composable in MainActivity needs a LaunchedEffect
    // calling loadAllTrips(), or this screen will sit on Loading forever.

    fun loadAllTrips() {
        viewModelScope.launch {
            uiState = AllTripsUiState.Loading

            // status = null → fetch ALL bookings regardless of bookingStatus.
            // Tab filtering (Upcoming/Past/Cancelled) happens client-side in
            // MyRidesScreen based on bookingStatus, so we want everything here.
            repository.getMyBookings(status = null)
                .onSuccess { bookings ->
                    uiState = AllTripsUiState.Success(bookings)
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
                .onSuccess {
                    // cancelTrip() returns a TripResponse, but our state holds
                    // TripBookingResponse — the shapes don't match, so instead
                    // of trying to patch the list in place, just refetch.
                    // This also guarantees bookingStatus is authoritative
                    // (comes straight from the backend, not assumed client-side).
                    loadAllTrips()
                }
                .onFailure { e ->
                    // Cancel failed — list stays as-is so the user doesn't lose
                    // their other trips from view. Surface this to the user
                    // (snackbar/toast) once you decide on an error-display pattern.
                }
        }
    }
}