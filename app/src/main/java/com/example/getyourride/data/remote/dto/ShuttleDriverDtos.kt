package com.example.getyourride.data.remote.dto

/**
 * DTOs for the shuttle driver feature.
 *
 * These map to the Spring Boot request/response classes
 * for the shuttle driver login and profile endpoints.
 */

// ── Login Request ───────────────────────────────────────────────────────────

data class ShuttleDriverLoginRequest(
    val email: String,
    val password: String
)

// ── Profile Response ────────────────────────────────────────────────────────

/**
 * Full profile response combining driver, vehicle, and trip summary.
 *
 * Maps to:
 *   driver.driver_id, first_name, last_name, email, phone, role, join_date, total_trips
 *   vehicle.registration_number, model, vehicle_year, colour, capacity
 *   Computed trip statistics from trip + trip_booking + boarding_log
 */
data class ShuttleDriverProfileResponse(
    val driverId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String?,
    val role: String,
    val joinDate: String?,
    val totalTrips: Int,
    val isVerified: Boolean,

    // Vehicle assigned to this driver
    val vehicle: ShuttleDriverVehicleResponse?,

    // Trip statistics
    val tripSummary: ShuttleDriverTripSummaryResponse?
)

data class ShuttleDriverVehicleResponse(
    val vehicleId: Long,
    val registrationNumber: String,
    val model: String?,
    val vehicleYear: Int?,
    val colour: String?,
    val capacity: Int
)

data class ShuttleDriverTripSummaryResponse(
    val currentTripRoute: String?,
    val currentTripStatus: String?,
    val scheduledTrips: Int,
    val inProgressTrips: Int,
    val completedTrips: Int,
    val cancelledTrips: Int,
    val studentsBookedToday: Int,
    val studentsBoardedToday: Int
)

// ── Boarding Screen DTOs ────────────────────────────────────────────────────

/**
 * The active trip for the shuttle driver's boarding screen.
 * Backend returns the current/next trip assigned to this driver.
 *
 * Maps to: trip + vehicle tables
 */
data class ShuttleDriverActiveTripResponse(
    val tripId: Long,
    val departureStop: String,
    val destinationStop: String,
    val departureTime: String,
    val arrivalTime: String?,
    val status: String,
    val capacity: Int,
    val registrationNumber: String,
    val totalBooked: Int,
    val totalBoarded: Int
)

/**
 * One booked student shown on the boarding list.
 *
 * Maps to: trip_booking + student + boarding_log
 */
data class BoardedStudentResponse(
    val bookingId: Long,
    val studentId: Long,
    val firstName: String,
    val lastName: String,
    val studentNumber: String,
    val bookingStatus: String,
    val boardedAt: String?
)

/**
 * Request body when the driver marks a student as boarded.
 */
data class MarkAsBoardedRequest(
    val bookingId: Long
)

/**
 * Simple success response after marking a student as boarded.
 */
data class MarkAsBoardedResponse(
    val success: Boolean,
    val message: String,
    val boardedAt: String?
)
