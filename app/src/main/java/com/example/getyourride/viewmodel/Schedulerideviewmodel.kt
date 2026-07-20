package com.example.getyourride.ui.screens.Shuttle

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * NOTE: deliberately has NO eager init { } block that touches auth/session state —
 * this screen only needs local UI state (pickup/destination + time slot), so there's
 * nothing here that can race against auth and cause the white-screen issue you hit
 * on BookingConfirmedScreen.
 */
data class ScheduleRideUiState(
    val pickupLabel: String = "North Campus Main Gate",
    val destinationLabel: String = "South Campus",
    val availableTimes: List<String> = listOf(
        "08:00 AM", "08:30 AM", "09:00 AM",
        "09:30 AM", "10:00 AM", "10:30 AM"
    ),
    val selectedTime: String? = "08:30 AM",
    val isConfirming: Boolean = false
)

class ScheduleRideViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleRideUiState())
    val uiState: StateFlow<ScheduleRideUiState> = _uiState

    fun onTimeSelected(time: String) {
        _uiState.update { it.copy(selectedTime = time) }
    }

    fun onSwapLocations() {
        _uiState.update {
            it.copy(
                pickupLabel = it.destinationLabel,
                destinationLabel = it.pickupLabel
            )
        }
    }

    /**
     * Confirm booking. Wire this up to TripController / DriverAuthApi once the
     * booking submission endpoint is ready — for now it just flips a loading flag
     * so the UI can be wired up end-to-end before the network call exists.
     */
    fun onConfirmBooking(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isConfirming = true) }
        // TODO: replace with real Retrofit call once booking endpoint is wired
        _uiState.update { it.copy(isConfirming = false) }
        onSuccess()
    }
}