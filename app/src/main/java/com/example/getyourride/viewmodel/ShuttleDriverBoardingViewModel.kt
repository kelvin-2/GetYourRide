package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.UserSession
import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.ShuttleDriverRepository
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ── Time Slot Model ─────────────────────────────────────────────────────────

/**
 * Represents one of the 8 standard shuttle time slots from the database.
 */
data class TimeSlot(
    val slotId: Int,
    val period: String,        // "Morning" or "Afternoon"
    val departs: LocalTime,
    val arrives: LocalTime
) {
    /** Display label e.g. "06:45" */
    val label: String get() = departs.format(DateTimeFormatter.ofPattern("HH:mm"))

    /** Full display label e.g. "06:45 - 07:30" */
    val fullLabel: String
        get() = "${departs.format(DateTimeFormatter.ofPattern("HH:mm"))} - ${arrives.format(DateTimeFormatter.ofPattern("HH:mm"))}"
}

/**
 * The 8 standard time slots as defined in the shuttle_time_slot database table.
 */
val STANDARD_TIME_SLOTS = listOf(
    TimeSlot(1, "Morning", LocalTime.of(6, 45), LocalTime.of(7, 30)),
    TimeSlot(2, "Morning", LocalTime.of(7, 45), LocalTime.of(8, 30)),
    TimeSlot(3, "Morning", LocalTime.of(8, 45), LocalTime.of(9, 30)),
    TimeSlot(4, "Morning", LocalTime.of(9, 45), LocalTime.of(10, 30)),
    TimeSlot(5, "Afternoon", LocalTime.of(12, 30), LocalTime.of(13, 15)),
    TimeSlot(6, "Afternoon", LocalTime.of(14, 30), LocalTime.of(15, 15)),
    TimeSlot(7, "Afternoon", LocalTime.of(16, 0), LocalTime.of(16, 45)),
    TimeSlot(8, "Afternoon", LocalTime.of(17, 30), LocalTime.of(18, 15)),
)

/**
 * UI state for the boarding screen.
 */
sealed interface BoardingUiState {
    object Loading : BoardingUiState

    data class Success(
        val trip: ShuttleDriverActiveTripResponse,
        val students: List<BoardedStudentResponse>,
        val timeSlots: List<TimeSlot>,
        val selectedSlot: TimeSlot,
        val selectedDate: LocalDate,
        val driverTrips: List<TripResponse>   // All trips for this driver on selected date
    ) : BoardingUiState

    data class NoTrip(val message: String) : BoardingUiState
    data class Error(val message: String) : BoardingUiState
}

/**
 * ViewModel for ShuttleDriverBoardingScreen.
 *
 * Fetches all trips for the logged-in shuttle driver, groups them by time slot,
 * auto-selects the active slot based on real time, and loads booked students.
 */
class ShuttleDriverBoardingViewModel(
    private val repository: ShuttleDriverRepository,
    private val tripApi: TripApi
) : ViewModel() {

    var uiState: BoardingUiState by mutableStateOf(BoardingUiState.Loading)
        private set

    // Tracks which booking is currently being marked (for button loading state)
    var markingBookingId: Long? by mutableStateOf(null)
        private set

    // Internal state: all trips for this driver today
    private var allDriverTrips: List<TripResponse> = emptyList()
    private var currentDate: LocalDate = LocalDate.now()

    /**
     * Load boarding data: fetches all trips, filters for this driver and today,
     * auto-selects the next upcoming time slot.
     */
    fun loadBoardingData() {
        val driverId = UserSession.id
        if (driverId == null) {
            uiState = BoardingUiState.Error("Not logged in. Please log in again.")
            return
        }

        uiState = BoardingUiState.Loading
        currentDate = LocalDate.now()

        viewModelScope.launch {
            try {
                // Fetch all trips from backend
                val response = tripApi.getAllTrips()
                if (!response.isSuccessful) {
                    throw Exception("Failed to load trips (${response.code()})")
                }

                val allTrips = response.body() ?: emptyList()

                // Filter: only SHUTTLE trips for this driver on today's date
                val todaysTrips = allTrips.filter { trip ->
                    trip.driverId == driverId &&
                            trip.tripType == "SHUTTLE" &&
                            isTripOnDate(trip.departureTime, currentDate)
                }

                if (todaysTrips.isNotEmpty()) {
                    allDriverTrips = todaysTrips
                } else {
                    // No trips today — fall back to the most recent date that has trips
                    val allDriverShuttleTrips = allTrips.filter { trip ->
                        trip.driverId == driverId && trip.tripType == "SHUTTLE"
                    }

                    if (allDriverShuttleTrips.isEmpty()) {
                        uiState = BoardingUiState.NoTrip("No trips scheduled for you.")
                        return@launch
                    }

                    // Find the most recent date among this driver's trips
                    val mostRecentDate = allDriverShuttleTrips
                        .mapNotNull { extractDate(it.departureTime) }
                        .maxOrNull()

                    if (mostRecentDate == null) {
                        uiState = BoardingUiState.NoTrip("No trips scheduled for you.")
                        return@launch
                    }

                    allDriverTrips = allDriverShuttleTrips.filter { trip ->
                        isTripOnDate(trip.departureTime, mostRecentDate)
                    }
                    currentDate = mostRecentDate
                }

                // Determine the active slot based on current time
                val activeSlot = determineActiveSlot(LocalTime.now())

                // Try loading the active slot; if no trip there, fall back to the last slot with a trip
                val hasTrip = allDriverTrips.any { tripMatchesSlot(it, activeSlot) }
                if (hasTrip) {
                    loadTripForSlot(activeSlot, driverId)
                } else {
                    // Find the most recent past slot that has a trip (fall back to last trip)
                    val lastSlotWithTrip = STANDARD_TIME_SLOTS
                        .lastOrNull { slot -> allDriverTrips.any { tripMatchesSlot(it, slot) } }

                    if (lastSlotWithTrip != null) {
                        loadTripForSlot(lastSlotWithTrip, driverId)
                    } else {
                        uiState = BoardingUiState.NoTrip("No trips found for your schedule today.")
                    }
                }
            } catch (e: Exception) {
                val msg = e.message ?: "Failed to load boarding data"
                uiState = BoardingUiState.Error(msg)
            }
        }
    }

    /**
     * Called when the driver manually selects a different time slot.
     */
    fun selectTimeSlot(slot: TimeSlot) {
        val driverId = UserSession.id ?: return

        viewModelScope.launch {
            try {
                loadTripForSlot(slot, driverId)
            } catch (e: Exception) {
                uiState = BoardingUiState.NoTrip("No trip found for ${slot.fullLabel}")
            }
        }
    }

    /**
     * Load the trip and students for a specific time slot.
     */
    private suspend fun loadTripForSlot(slot: TimeSlot, driverId: Long) {
        // Find trip(s) matching this slot for the driver
        val tripsForSlot = allDriverTrips.filter { trip ->
            tripMatchesSlot(trip, slot)
        }

        if (tripsForSlot.isEmpty()) {
            uiState = BoardingUiState.NoTrip("No trip assigned at ${slot.fullLabel}")
            return
        }

        // Pick the first matching trip (there should typically be one per slot per driver)
        val selectedTrip = tripsForSlot.first()

        // Fetch booked students for this trip
        val students = try {
            repository.getBookedStudents(selectedTrip.tripId)
        } catch (e: Exception) {
            emptyList()
        }

        // Convert TripResponse to ShuttleDriverActiveTripResponse for UI compatibility
        val activeTripResponse = ShuttleDriverActiveTripResponse(
            tripId = selectedTrip.tripId,
            departureStop = selectedTrip.departureStop,
            destinationStop = selectedTrip.destinationStop,
            departureTime = slot.fullLabel,
            arrivalTime = selectedTrip.arrivalTime,
            status = selectedTrip.status,
            capacity = selectedTrip.vehicleCapacity ?: 22,
            registrationNumber = selectedTrip.registrationNumber ?: "N/A",
            totalBooked = students.size,
            totalBoarded = students.count { it.boardedAt != null }
        )

        uiState = BoardingUiState.Success(
            trip = activeTripResponse,
            students = students,
            timeSlots = STANDARD_TIME_SLOTS,
            selectedSlot = slot,
            selectedDate = currentDate,
            driverTrips = allDriverTrips
        )
    }

    /**
     * Determine which time slot should be "active" based on the current real time.
     *
     * Logic: The active slot is the next upcoming slot whose departure time
     * hasn't passed by more than 5 minutes. After departure + 5 min, move to next slot.
     *
     * Examples:
     * - At 14:54, active slot = 16:00 (slot 7) because 14:30+5min = 14:35 has passed
     * - At 16:04, active slot = 16:00 (slot 7) still (within 5 min grace)
     * - At 16:06, active slot = 17:30 (slot 8)
     * - At 06:30, active slot = 06:45 (slot 1)
     */
    private fun determineActiveSlot(now: LocalTime): TimeSlot {
        // Find the first slot where now < departs + 5 minutes
        for (slot in STANDARD_TIME_SLOTS) {
            val cutoff = slot.departs.plusMinutes(5)
            if (now.isBefore(cutoff)) {
                return slot
            }
        }
        // If past all slots, return the last one
        return STANDARD_TIME_SLOTS.last()
    }

    /**
     * Check if a trip's departure time matches a given time slot.
     * The trip's departureTime is an ISO datetime string like "2026-08-04 06:45:00"
     * or "2026-08-04T06:45:00". We extract the time and compare with slot's depart time.
     */
    private fun tripMatchesSlot(trip: TripResponse, slot: TimeSlot): Boolean {
        return try {
            val timeStr = trip.departureTime
                .replace("T", " ")
                .substringAfter(" ")
                .take(5) // "06:45"
            val tripTime = LocalTime.parse(timeStr, DateTimeFormatter.ofPattern("HH:mm"))
            tripTime == slot.departs
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Check if a trip's departure time falls on the given date.
     */
    private fun isTripOnDate(departureTime: String, date: LocalDate): Boolean {
        return try {
            val dateStr = departureTime.replace("T", " ").take(10) // "2026-08-04"
            val tripDate = LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            tripDate == date
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extract the date portion from a departure time string.
     */
    private fun extractDate(departureTime: String): LocalDate? {
        return try {
            val dateStr = departureTime.replace("T", " ").take(10)
            LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        } catch (e: Exception) {
            null
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
    private val repository: ShuttleDriverRepository,
    private val tripApi: TripApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleDriverBoardingViewModel(repository, tripApi) as T
    }
}
