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
