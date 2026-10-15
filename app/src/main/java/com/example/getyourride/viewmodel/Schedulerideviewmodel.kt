package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.ApiError
import com.example.getyourride.data.remote.ApiException
import com.example.getyourride.data.remote.UserMessageException
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.dto.hasDepartedToday
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.userFacingMessage
import com.example.getyourride.data.repository.ShuttleRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State for shuttle booking.
 */
data class ScheduleRideUiState(
    val pickupLabel: String = "Select Pickup",
    val destinationLabel: String = "Select Destination",
    val availableTimes: List<ShuttleTimeSlot> = emptyList(),
    val selectedTime: String? = null,
    val selectedSlot: ShuttleTimeSlot? = null,
    val isConfirming: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    // Backend "error" text for errorCode == CAMPUS_REQUIRED; shown as an inline alert.
    val campusRequiredMessage: String? = null,
    val lastBookedTrip: TripResponse? = null, // real trip data for the confirmation screen
    val lastBookingId: Long? = null           // real booking id from the backend, encoded into the QR
)

private const val CAMPUS_REQUIRED_CODE = "CAMPUS_REQUIRED"

private const val CAMPUS_HINT_MESSAGE =
    "Shuttles must start or end at an NMU campus: North Campus, South Campus, " +
        "Missionvale Campus or 2nd Avenue Campus. Please change your pickup or drop-off."

private val NMU_CAMPUSES = listOf("north campus", "south campus", "missionvale campus", "2nd avenue campus")

private fun isCampus(label: String): Boolean {
    val l = label.trim().lowercase()
    return NMU_CAMPUSES.any { l.contains(it) }
}

private const val PAST_SLOT_MESSAGE =
    "That shuttle has already left. Shuttles can only be booked for today, so please pick a later time."

class ScheduleRideViewModel(
    private val repository: ShuttleRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScheduleRideUiState())
    val uiState: StateFlow<ScheduleRideUiState> = _uiState

    init {
        loadTimeSlots()
    }

    private fun loadTimeSlots() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (slots, usedFallback) = repository.fetchTimeSlotsWithStatus()
                _uiState.update {
                    it.copy(
                        availableTimes = slots,
                        isLoading = false,
                        errorMessage = if (usedFallback) "Can't reach the server. Showing default departure times." else it.errorMessage
                    )
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = e.userFacingMessage(), isLoading = false) }
            }
        }
    }

    fun onTimeSelected(slot: ShuttleTimeSlot) {
        if (slot.hasDepartedToday()) {
            _uiState.update {
                it.copy(
                    // Drop any earlier selection so a stale slot can't be confirmed.
                    selectedTime = if (it.selectedSlot == slot) null else it.selectedTime,
                    selectedSlot = if (it.selectedSlot == slot) null else it.selectedSlot,
                    errorMessage = PAST_SLOT_MESSAGE
                )
            }
            return
        }
        _uiState.update {
            it.copy(selectedTime = slot.departs, selectedSlot = slot, errorMessage = null)
        }
    }

    fun onSwapLocations() {
        _uiState.update {
            it.copy(
                pickupLabel = it.destinationLabel,
                destinationLabel = it.pickupLabel
            )
        }
    }

    fun onConfirmBooking(onSuccess: () -> Unit) {
        val state = _uiState.value
        // Ignore taps while a booking request is already in flight (prevents double-submits).
        if (state.isConfirming) return
        val slot = state.selectedSlot

        if (slot == null) {
            _uiState.update { it.copy(errorMessage = "Please select a departure time") }
            return
        }
        // The slot may have passed while the screen was open, so check again at confirm time.
        if (slot.hasDepartedToday()) {
            _uiState.update { it.copy(errorMessage = PAST_SLOT_MESSAGE) }
            return
        }
        if (state.pickupLabel == "Select Pickup") {
            _uiState.update { it.copy(errorMessage = "Please select a pickup stop") }
            return
        }
        if (state.destinationLabel == "Select Destination") {
            _uiState.update { it.copy(errorMessage = "Please select a destination stop") }
            return
        }
        if (state.pickupLabel == state.destinationLabel) {
            _uiState.update { it.copy(errorMessage = "Pickup and destination must be different") }
            return
        }

        // Shuttle-only rule: at least one end of the trip must be an NMU campus.
        if (!isCampus(state.pickupLabel) && !isCampus(state.destinationLabel)) {
            _uiState.update { it.copy(campusRequiredMessage = CAMPUS_HINT_MESSAGE) }
            return
        }

        _uiState.update { it.copy(isConfirming = true, errorMessage = null, campusRequiredMessage = null) }
        viewModelScope.launch {
            try {
                val trip = repository.findAvailableTrip(state.pickupLabel, slot)
                    ?: throw UserMessageException("No shuttle available for this stop and time. Try another slot.")

                val summary = repository.bookShuttle(trip.tripId)

                // Store the real trip (driver, plate, vehicle, times) for the confirmation screen,
                // instead of leaving the caller to invent placeholder data. Also keep the real
                // bookingId returned by the backend — it goes into the QR the driver scans.
                _uiState.update {
                    it.copy(
                        isConfirming = false,
                        lastBookedTrip = trip,
                        lastBookingId = summary.bookingConfirmation.bookingId
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val apiError = (e as? ApiException)?.apiError as? ApiError.Server
                // Always reset the in-flight flag so the button never stays stuck.
                if (apiError?.errorCode == CAMPUS_REQUIRED_CODE) {
                    // Backend text is shown as-is; no retry, no field highlighting.
                    _uiState.update {
                        it.copy(isConfirming = false, campusRequiredMessage = apiError.message)
                    }
                } else {
                    _uiState.update {
                        it.copy(isConfirming = false, errorMessage = e.userFacingMessage())
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun dismissCampusRequired() {
        _uiState.update { it.copy(campusRequiredMessage = null) }
    }

    fun updatePickup(location: String) {
        _uiState.update { it.copy(pickupLabel = location, campusRequiredMessage = null) }
    }

    fun updateDestination(location: String) {
        _uiState.update { it.copy(destinationLabel = location, campusRequiredMessage = null) }
    }
}

class ScheduleRideViewModelFactory(
    private val repository: ShuttleRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ScheduleRideViewModel(repository) as T
    }
}