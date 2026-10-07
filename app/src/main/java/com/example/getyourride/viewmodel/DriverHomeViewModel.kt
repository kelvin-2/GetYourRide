package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.DriverApplicationRepository
import com.example.getyourride.data.repository.DriverProfileResult
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

/**
 * UI state for the Driver Home screen.
 */
sealed class DriverHomeUiState {
    data object Loading : DriverHomeUiState()
    data class Success(
        val activeRides: List<TripResponse>,     // SCHEDULED or IN_PROGRESS
        val pastRides: List<TripResponse>,       // COMPLETED only
        val cancelledRides: List<TripResponse> = emptyList()  // CANCELLED only
    ) : DriverHomeUiState()
    data class Error(val message: String) : DriverHomeUiState()
}

class DriverHomeViewModel(
    private val tripRepository: TripRepository,
    // Optional so existing call sites that only pass a TripRepository keep compiling. When present,
    // the home screen can refresh the driver's live verification status (e.g. after an edit sends
    // the application back to "Pending Review").
    private val driverProfileRepository: DriverApplicationRepository? = null
) : ViewModel() {

    var uiState by mutableStateOf<DriverHomeUiState>(DriverHomeUiState.Loading)
        private set

    /**
     * Live verification status pulled from the backend profile (e.g. "Approved", "Pending Review",
     * "Rejected"). Null until first loaded; the screen falls back to the session value when null.
     * Kept fresh by [loadMyTrips] so pressing refresh updates the status badge too, not just trips.
     */
    var verificationStatus by mutableStateOf<String?>(null)
        private set

    /**
     * One-shot, user-facing message for the outcome of an action (e.g. starting a trip), shown as
     * a snackbar. Null when there is nothing to show. The screen calls [consumeActionMessage] once
     * it has displayed it so the same message does not reappear on recomposition.
     */
    var actionMessage by mutableStateOf<String?>(null)
        private set

    /** The trip currently being started, so its card can show a spinner and disable the button. */
    var startingTripId by mutableStateOf<Long?>(null)
        private set

    fun consumeActionMessage() {
        actionMessage = null
    }

    /**
     * Fetch the driver's current verification/application status from the backend profile.
     * No-op when no profile repository was supplied. Uses applicationStatus, which is the field
     * that reads "Approved" / "Pending Review" / "Rejected".
     */
    private fun refreshVerificationStatus() {
        val repo = driverProfileRepository ?: return
        viewModelScope.launch {
            when (val result = repo.getDriverProfile()) {
                is DriverProfileResult.Success -> {
                    verificationStatus = result.profile.applicationStatus
                }
                is DriverProfileResult.Error -> {
                    // Leave the previous value; the screen falls back to the session status.
                }
            }
        }
    }

    /**
     * Start a trip the driver has posted: sets it IN_PROGRESS on the backend and kicks off the
     * live simulation. On success the trip list is reloaded so the card reflects the new status;
     * on failure the driver sees why and the trip is left untouched.
     *
     * Additive: this does not alter [loadMyTrips] or [cancelRide].
     */
    fun startRide(tripId: Long) {
        if (startingTripId != null) return // ignore double-taps while a start is in flight
        startingTripId = tripId
        viewModelScope.launch {
            tripRepository.startTrip(tripId)
                .onSuccess {
                    startingTripId = null
                    actionMessage = "Trip started — it's now live for passengers to track."
                    loadMyTrips()
                }
                .onFailure { error ->
                    startingTripId = null
                    actionMessage = error.message ?: "Couldn't start the trip. Please try again."
                }
        }
    }

    fun loadMyTrips() {
        uiState = DriverHomeUiState.Loading
        // Refresh the live verification status alongside the trips so the "Driver Status" badge
        // reflects the latest state (e.g. after a profile edit reset it to "Pending Review").
        refreshVerificationStatus()
        viewModelScope.launch {
            val result = tripRepository.getMyTrips()
            result.onSuccess { trips ->
                val active = trips.filter { trip ->
                    trip.status.equals("SCHEDULED", ignoreCase = true) ||
                    trip.status.equals("IN_PROGRESS", ignoreCase = true) ||
                    trip.status.equals("CONFIRMED", ignoreCase = true)
                }.sortedByDescending { it.departureTime }

                val past = trips.filter { trip ->
                    trip.status.equals("COMPLETED", ignoreCase = true)
                }.sortedByDescending { it.departureTime }

                val cancelled = trips.filter { trip ->
                    trip.status.equals("CANCELLED", ignoreCase = true)
                }.sortedByDescending { it.departureTime }

                uiState = DriverHomeUiState.Success(
                    activeRides = active,
                    pastRides = past,
                    cancelledRides = cancelled
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
            // Optimistic UI update: move trip into the Cancelled list immediately
            val currentState = uiState
            if (currentState is DriverHomeUiState.Success) {
                val cancelledTrip = currentState.activeRides.find { it.tripId == tripId }
                if (cancelledTrip != null) {
                    val updatedActive = currentState.activeRides.filter { it.tripId != tripId }
                    val updatedCancelled = listOf(
                        cancelledTrip.copy(status = "CANCELLED")
                    ) + currentState.cancelledRides
                    uiState = DriverHomeUiState.Success(
                        activeRides = updatedActive,
                        pastRides = currentState.pastRides,
                        cancelledRides = updatedCancelled
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
    private val tripRepository: TripRepository,
    private val driverProfileRepository: DriverApplicationRepository? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DriverHomeViewModel(tripRepository, driverProfileRepository) as T
    }
}
