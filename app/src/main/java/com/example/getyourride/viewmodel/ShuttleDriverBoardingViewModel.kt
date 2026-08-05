package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.UserSession
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.repository.ShuttleDriverRepository
import kotlinx.coroutines.launch

/**
 * UI state for the boarding screen.
 */
sealed interface BoardingUiState {
    object Loading : BoardingUiState
    data class Success(
        val trip: ShuttleDriverActiveTripResponse,
        val students: List<BoardedStudentResponse>
    ) : BoardingUiState
    data class NoTrip(val message: String) : BoardingUiState
    data class Error(val message: String) : BoardingUiState
}

/**
 * ViewModel for ShuttleDriverBoardingScreen.
 *
 * Loads the active trip and booked students from the backend.
 * Handles marking students as boarded.
 */
class ShuttleDriverBoardingViewModel(
    private val repository: ShuttleDriverRepository
) : ViewModel() {

    var uiState: BoardingUiState by mutableStateOf(BoardingUiState.Loading)
        private set

    // Tracks which booking is currently being marked (for button loading state)
    var markingBookingId: Long? by mutableStateOf(null)
        private set

    /**
     * Load the active trip and its booked students.
     */
    fun loadBoardingData() {
        val driverId = UserSession.id
        if (driverId == null) {
            uiState = BoardingUiState.Error("Not logged in. Please log in again.")
            return
        }

        uiState = BoardingUiState.Loading
        viewModelScope.launch {
            try {
                val trip = repository.getActiveTrip(driverId)
                val students = repository.getBookedStudents(trip.tripId)
                uiState = BoardingUiState.Success(trip = trip, students = students)
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to load boarding data"
                if (msg.contains("No active trip", ignoreCase = true)) {
                    uiState = BoardingUiState.NoTrip(msg)
                } else {
                    uiState = BoardingUiState.Error(msg)
                }
            }
        }
    }

    /**
     * Mark a student as boarded.
     * Updates the local student list on success so UI reflects immediately.
     */
    fun markStudentAsBoarded(bookingId: Long) {
        markingBookingId = bookingId
        viewModelScope.launch {
            try {
                val result = repository.markAsBoarded(bookingId)

                // Update local state on success
                val currentState = uiState
                if (currentState is BoardingUiState.Success) {
                    val updatedStudents = currentState.students.map { student ->
                        if (student.bookingId == bookingId) {
                            student.copy(boardedAt = result.boardedAt ?: "Now")
                        } else {
                            student
                        }
                    }
                    val newBoarded = updatedStudents.count { it.boardedAt != null }
                    uiState = currentState.copy(
                        students = updatedStudents,
                        trip = currentState.trip.copy(totalBoarded = newBoarded)
                    )
                }
            } catch (e: Exception) {
                // Don't change screen state — just log error silently
                // The button will stop loading and student stays as "Pending"
            } finally {
                markingBookingId = null
            }
        }
    }
}

class ShuttleDriverBoardingViewModelFactory(
    private val repository: ShuttleDriverRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleDriverBoardingViewModel(repository) as T
    }
}
