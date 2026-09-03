package com.example.getyourride.data.remote.dto

import java.math.BigDecimal

// Matches TripResponse.java field-for-field.
// LocalDateTime on the backend serializes as an ISO string array or string
// depending on Jackson config — using String here is the safe default;
// we parse/format it on the UI side so a backend date-format quirk doesn't
// crash Gson parsing.
data class TripResponse(
    val tripId: Long,
    val driverId: Long?,
    val driverName: String?,
    val registrationNumber: String?,
    val tripType: String,
    val departureStop: String,
    val departureLat: Double?,
    val departureLng: Double?,
    val destinationStop: String,
    val destinationLat: Double?,
    val destinationLng: Double?,
    val departureTime: String,
    val arrivalTime: String?,
    val availableSeats: Int,
    val price: BigDecimal,
    val status: String,
    val slotTime: String? = null,   // NEW — "06:45:00 - 07:30:00", matches backend TripResponse.slotTime
    val vehicleModel: String?,
    val vehicleColour: String?,
    val vehicleCapacity: Int?,
    val stops: List<TripStopResponse> = emptyList(),
    // NEW — backend TripResponse.bookingId/bookingStatus (Phase 4 — booking wiring) were already
    // being sent on /my-trips but this DTO never declared them, so Gson silently dropped them.
    // Needed so the shuttle QR ticket can reference a real trip_booking.booking_id.
    val bookingId: Long? = null,
    val bookingStatus: String? = null
)

/**
 * One stop on a trip, as returned inside TripResponse.stops.
 * Field names match your sample JSON exactly:
 *   { "id": 4, "stopName": "...", "latitude": ..., "longitude": ...,
 *     "stopOrder": 1, "studentId": 1, "studentName": "..." }
 *
 * studentId/studentName are null when the stop hasn't been claimed by a
 * student yet (e.g. a driver-defined waypoint vs. a student's pickup stop).
 */
data class TripStopResponse(
    val id: Long,
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
    val stopOrder: Int,
    val studentId: Long?,
    val studentName: String?
)

data class TripBookingResponse(
    val bookingId: Long,
    val trip: TripResponse,
    val bookingDate: String?,
    val bookingStatus: String?   // "CONFIRMED", "CANCELLED", "PENDING"
)