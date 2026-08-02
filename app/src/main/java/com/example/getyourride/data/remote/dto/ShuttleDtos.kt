package com.example.getyourride.data.remote.dto

data class ShuttleStopResponse(
    val stopId: Int,
    val stopName: String,
    val area: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?
)

data class ShuttleTimeSlotResponse(
    val slotId: Int,
    val period: String,
    val departs: String,
    val arrives: String
)

/**
 * UI representation of a time slot.
 * `slotId` and `arrives` are needed to resolve the real Trip that matches
 * this slot before booking — slotId is NOT the same as Trip.tripId.
 */
data class ShuttleTimeSlot(
    val slotId: Int,
    val departs: String,
    val arrives: String,
    val period: String
)
data class BookingConfirmationResponse(
    val bookingId: Long,
    val tripId: Long,
    val status: String,
    val message: String
)

data class ShuttleBookingSummaryResponse(
    val bookingConfirmation: BookingConfirmationResponse,
    val myShuttleTrips: List<TripResponse>,
    val allShuttleTrips: List<TripResponse>
)