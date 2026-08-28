package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.UserSession
import com.example.getyourride.data.remote.api.ShuttleApi
import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.ShuttleDriverRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ── Time Slot Model ─────────────────────────────────────────────────────────

/**
 * Represents one shuttle time slot, loaded from the shuttle_time_slot table via API.
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

    data class NoTrip(
        val message: String,
        val timeSlots: List<TimeSlot>? = null,
        val selectedSlot: TimeSlot? = null,
        val selectedDate: LocalDate? = null,
        val driverTrips: List<TripResponse>? = null
    ) : BoardingUiState
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
    private val tripApi: TripApi,
    private val shuttleApi: ShuttleApi
) : ViewModel() {

    var uiState: BoardingUiState by mutableStateOf(BoardingUiState.Loading)
        private set

    // Tracks which booking is currently being marked (for button loading state)
    var markingBookingId: Long? by mutableStateOf(null)
        private set

    // Internal state: all trips for this driver today
    private var allDriverTrips: List<TripResponse> = emptyList()
    private var currentDate: LocalDate = LocalDate.now()

    // Time slots loaded from the database via API
    private var loadedTimeSlots: List<TimeSlot> = emptyList()

    // Auto-advance timer job — moves to next trip after drop-off (30 min after departure)
    private var autoAdvanceJob: Job? = null

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
                // Fetch time slots from the database via API
                val slotsResponse = shuttleApi.getAllTimeSlots()
                loadedTimeSlots = slotsResponse.map { slot ->
                    TimeSlot(
                        slotId = slot.slotId,
                        period = slot.period,
                        departs = LocalTime.parse(slot.departs, DateTimeFormatter.ofPattern("HH:mm:ss")),
                        arrives = LocalTime.parse(slot.arrives, DateTimeFormatter.ofPattern("HH:mm:ss"))
                    )
                }.sortedBy { it.departs }

                if (loadedTimeSlots.isEmpty()) {
                    uiState = BoardingUiState.Error("No time slots configured in the system.")
                    return@launch
                }

                // Fetch all trips from backend
                val response = tripApi.getAllTrips()
                if (!response.isSuccessful) {
                    throw Exception("Failed to load trips (${response.code()})")
                }

                val allTrips = response.body() ?: emptyList()

                // DEBUG: Log what we received and what we're filtering
                android.util.Log.d("BoardingVM", "Total trips from API: ${allTrips.size}")
                android.util.Log.d("BoardingVM", "Driver ID from session: $driverId")
                android.util.Log.d("BoardingVM", "Current date (phone): $currentDate")

                val shuttleTripsForDriver = allTrips.filter { trip ->
                    trip.driverId == driverId && trip.tripType == "SHUTTLE"
                }
                android.util.Log.d("BoardingVM", "Shuttle trips for this driver (all dates): ${shuttleTripsForDriver.size}")
                shuttleTripsForDriver.take(10).forEach { trip ->
                    android.util.Log.d("BoardingVM", "  Trip ${trip.tripId}: type=${trip.tripType}, dept='${trip.departureTime}', ${trip.departureStop}->${trip.destinationStop}")
                }

                // Filter: only SHUTTLE trips for this driver on today's date
                val todaysTrips = allTrips.filter { trip ->
                    trip.driverId == driverId &&
                            trip.tripType == "SHUTTLE" &&
                            isTripOnDate(trip.departureTime, currentDate)
                }
                android.util.Log.d("BoardingVM", "Today's trips after date filter: ${todaysTrips.size}")

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
                    val lastSlotWithTrip = loadedTimeSlots
                        .lastOrNull { slot -> allDriverTrips.any { tripMatchesSlot(it, slot) } }

                    if (lastSlotWithTrip != null) {
                        loadTripForSlot(lastSlotWithTrip, driverId)
                    } else {
                        uiState = BoardingUiState.NoTrip(
                            message = "No trips found for your schedule today.",
                            timeSlots = loadedTimeSlots,
                            selectedSlot = activeSlot,
                            selectedDate = currentDate,
                            driverTrips = allDriverTrips
                        )
                    }
                }

                // Start the auto-advance timer
                startAutoAdvanceTimer(driverId)
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
                uiState = BoardingUiState.NoTrip(
                    message = "No trip found for ${slot.fullLabel}",
                    timeSlots = loadedTimeSlots,
                    selectedSlot = slot,
                    selectedDate = currentDate,
                    driverTrips = allDriverTrips
                )
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
            uiState = BoardingUiState.NoTrip(
                message = "No trip assigned at ${slot.fullLabel}",
                timeSlots = loadedTimeSlots,
                selectedSlot = slot,
                selectedDate = currentDate,
                driverTrips = allDriverTrips
            )
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
            timeSlots = loadedTimeSlots,
            selectedSlot = slot,
            selectedDate = currentDate,
            driverTrips = allDriverTrips
        )
    }

    /**
     * Determine which time slot should be "active" based on the current real time.
     *
     * Logic: A slot remains active until 30 minutes after its departure time
     * (approximating drop-off completion). After that, move to the next slot.
     *
     * Examples (with slots 06:45, 07:45, 08:45, ...):
     * - At 06:50 → active = 06:45 (still within 30 min of departure)
     * - At 07:14 → active = 06:45 (06:45 + 30min = 07:15 not reached yet)
     * - At 07:16 → active = 07:45 (06:45 + 30min = 07:15 has passed)
     * - At 08:14 → active = 07:45 (07:45 + 30min = 08:15 not reached yet)
     */
    private fun determineActiveSlot(now: LocalTime): TimeSlot {
        // Find the first slot where now < departs + 30 minutes (drop-off window)
        for (slot in loadedTimeSlots) {
            val dropOffCutoff = slot.departs.plusMinutes(30)
            if (now.isBefore(dropOffCutoff)) {
                return slot
            }
        }
        // If past all slots' drop-off times, return the last one
        return loadedTimeSlots.last()
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
            val matches = tripTime == slot.departs
            if (!matches && slot.slotId <= 2) {
                // Log only first 2 slots to avoid spam
                android.util.Log.d("BoardingVM", "  tripMatchesSlot MISS: trip ${trip.tripId} time='$timeStr' parsed=$tripTime vs slot ${slot.slotId} departs=${slot.departs}")
            }
            matches
        } catch (e: Exception) {
            android.util.Log.e("BoardingVM", "  tripMatchesSlot ERROR: trip ${trip.tripId} departureTime='${trip.departureTime}' exception=${e.message}")
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
     * Starts a coroutine that checks every 60 seconds whether the current trip's
     * drop-off window (30 min after departure) has passed. If so, automatically
     * advances to the next time slot.
     */
    private fun startAutoAdvanceTimer(driverId: Long) {
        autoAdvanceJob?.cancel()
        autoAdvanceJob = viewModelScope.launch {
            while (true) {
                delay(60_000L) // Check every 60 seconds

                val currentState = uiState
                val currentSlot = when (currentState) {
                    is BoardingUiState.Success -> currentState.selectedSlot
                    is BoardingUiState.NoTrip -> currentState.selectedSlot
                    else -> null
                }

                val newActiveSlot = determineActiveSlot(LocalTime.now())

                // Only auto-advance if the slot has actually changed
                if (currentSlot != null && newActiveSlot.slotId != currentSlot.slotId) {
                    // Check if there's a trip for the new active slot
                    val hasTrip = allDriverTrips.any { tripMatchesSlot(it, newActiveSlot) }
                    if (hasTrip) {
                        loadTripForSlot(newActiveSlot, driverId)
                    } else {
                        uiState = BoardingUiState.NoTrip(
                            message = "No trip assigned at ${newActiveSlot.fullLabel}",
                            timeSlots = loadedTimeSlots,
                            selectedSlot = newActiveSlot,
                            selectedDate = currentDate,
                            driverTrips = allDriverTrips
                        )
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        autoAdvanceJob?.cancel()
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
    private val tripApi: TripApi,
    private val shuttleApi: ShuttleApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleDriverBoardingViewModel(repository, tripApi, shuttleApi) as T
    }
}
