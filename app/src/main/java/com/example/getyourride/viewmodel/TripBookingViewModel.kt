package com.example.getyourride.viewmodel
import com.example.getyourride.ui.screens.Rides.RideRequestDetails
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.TripStopRequest
import com.example.getyourride.data.repository.TripRepository
import com.example.getyourride.ui.screens.StopResult
import kotlinx.coroutines.launch

sealed interface BookingUiState {
    object Idle : BookingUiState
    object Submitting : BookingUiState
    data class Success(val trip: TripResponse) : BookingUiState
    data class Error(val message: String) : BookingUiState
}

/**
 * Scoped to a single trip's booking flow (one instance shared between
 * RequestRideScreen and AddStopScreen via the nav back stack entry - see
 * NavGraph wiring below). Holds whichever pickup/drop-off stop the student
 * picked, then fires POST /api/trips/{tripId}/book on confirm.
 *
 * NOTE: backend BookCarpoolRequest has no seats-count or notes field -
 * every booking is 1 seat, no notes, matching the current model exactly.
 */
class TripBookingViewModel(
    private val tripId: Long,
    private val repository: TripRepository
) : ViewModel() {

    var pickupStop: StopResult? by mutableStateOf(null)
        private set

    var dropOffStop: StopResult? by mutableStateOf(null)
        private set

    var bookingState: BookingUiState by mutableStateOf(BookingUiState.Idle)
        private set

    /** Called from AddStopScreen when the student is choosing where to be picked up. */
    fun setPickupStop(stop: StopResult) {
        pickupStop = stop
    }

    /** Called from AddStopScreen when the student is choosing an optional drop-off point. */
    fun setDropOffStop(stop: StopResult) {
        dropOffStop = stop
    }

    fun clearDropOffStop() {
        dropOffStop = null
    }

    /**
     * pickupStop must be set before this succeeds - AddStopScreen picking a
     * pickup is required, since the backend has no default/self-service pickup.
     */
    fun confirmBooking() {
        val pickup = pickupStop
        if (pickup == null) {
            bookingState = BookingUiState.Error("Please choose a pickup stop first")
            return
        }

        val request = BookCarpoolRequest(
            pickupStop = TripStopRequest(
                stopName = pickup.displayName,
                latitude = pickup.latitude,
                longitude = pickup.longitude
            ),
            dropOffStop = dropOffStop?.let { drop ->
                TripStopRequest(
                    stopName = drop.displayName,
                    latitude = drop.latitude,
                    longitude = drop.longitude
                )
            }
        )

        viewModelScope.launch {
            bookingState = BookingUiState.Submitting
            repository.bookCarpool(tripId, request)
                .onSuccess { trip -> bookingState = BookingUiState.Success(trip) }
                .onFailure { e -> bookingState = BookingUiState.Error(e.message ?: "Couldn't book this ride") }
        }
    }

    fun resetState() {
        bookingState = BookingUiState.Idle
    }

    /**
     * Called once when RequestRideScreen loads. If the trip itself has known
     * departure coordinates and the student hasn't manually picked a stop yet,
     * default the pickup to the trip's own departure point - so "Confirm" works
     * without forcing a trip through AddStopScreen. If the trip has no coords
     * (departureLat/Lng null, per your sample data), pickupStop stays null and
     * AddStopScreen becomes required, same as before.
     */
    fun initializeDefaultPickupIfNeeded(ride: RideRequestDetails) {
        if (pickupStop != null) return
        val lat = ride.pickupLat ?: return
        val lng = ride.pickupLng ?: return
        pickupStop = StopResult(
            displayName = ride.pickupLabel,
            latitude = lat,
            longitude = lng
        )
    }
}