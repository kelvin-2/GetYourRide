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
    val vehicleModel: String?,
    val vehicleColour: String?,
    val vehicleCapacity: Int?
)