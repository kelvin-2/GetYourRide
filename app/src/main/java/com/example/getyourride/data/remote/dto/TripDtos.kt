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
    // Live tracking position, written by the backend simulation engine on every tick and returned
    // by GET /api/trips/{id}. Null until a driver starts the trip. The tracking screen polls this
    // endpoint and redraws the marker from these fields (there is no WebSocket on the app).
    // Defaulted so existing constructor calls (previews, tests) keep compiling.
    val currentLat: Double? = null,
    val currentLng: Double? = null,
    val currentLegIndex: Int? = null,
    val stops: List<TripStopResponse> = emptyList()
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
    // "PENDING" until the simulated vehicle reaches this stop, then "ARRIVED". Lets the tracking
    // screen show passed stops as visited even for a student who opened the screen mid-trip.
    // Defaulted so existing constructor calls keep compiling.
    val status: String = "PENDING",
    val studentId: Long?,
    val studentName: String?
)

data class TripBookingResponse(
    val bookingId: Long,
    val trip: TripResponse,
    val bookingDate: String?,
    val bookingStatus: String?   // "CONFIRMED", "CANCELLED", "PENDING"
)