package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.ShuttleRepository
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
    val lastBookedTrip: TripResponse? = null // real trip data for the confirmation screen
)

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
                val slots = repository.fetchTimeSlots()
                _uiState.update { it.copy(availableTimes = slots, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Failed to load time slots", isLoading = false) }
            }
        }
    }

    fun onTimeSelected(slot: ShuttleTimeSlot) {
        _uiState.update { it.copy(selectedTime = slot.departs, selectedSlot = slot) }
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
        val slot = state.selectedSlot

        if (slot == null) {
            _uiState.update { it.copy(errorMessage = "Please select a departure time") }
            return
        }
        if (state.pickupLabel == "Select Pickup") {
            _uiState.update { it.copy(errorMessage = "Please select a pickup stop") }
            return
        }

        _uiState.update { it.copy(isConfirming = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val trip = repository.findAvailableTrip(state.pickupLabel, slot)
                    ?: throw Exception("No shuttle available for this stop and time — try another slot")

                repository.bookShuttle(trip.tripId)

                // Store the real trip (driver, plate, vehicle, times) for the confirmation screen,
                // instead of leaving the caller to invent placeholder data.
                _uiState.update { it.copy(isConfirming = false, lastBookedTrip = trip) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isConfirming = false, errorMessage = e.message ?: "Booking failed")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updatePickup(location: String) {
        _uiState.update { it.copy(pickupLabel = location) }
    }

    fun updateDestination(location: String) {
        _uiState.update { it.copy(destinationLabel = location) }
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