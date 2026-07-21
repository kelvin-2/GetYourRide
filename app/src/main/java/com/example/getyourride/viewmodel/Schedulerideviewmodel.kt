package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val availableTimes: List<String> = emptyList(),
    val selectedTime: String? = null,
    val isConfirming: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
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

    fun onConfirmBooking(onSuccess: () -> Unit) {
        _uiState.update { it.copy(isConfirming = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                // TODO: Wire to a real booking endpoint
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(isConfirming = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update { it.copy(isConfirming = false, errorMessage = "Booking failed") }
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
