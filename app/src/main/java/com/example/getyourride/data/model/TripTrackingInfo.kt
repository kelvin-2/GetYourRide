package com.example.getyourride.domain.model

data class TripTrackingInfo(
    val driverName: String,
    val driverRating: Double,
    val driverPhotoUrl: String? = null,
    val status: RideStatus = RideStatus.ON_THE_WAY,
    val etaMinutes: Int? = null,
    val carModel: String,
    val carColor: String,
    val carYear: Int,
    val plateNumber: String,
    val isPlateVerified: Boolean = true,
    val destinationLabel: String = "Library North"
)

enum class RideStatus(val label: String) {
    ON_THE_WAY("ON THE WAY"),
    ARRIVED("ARRIVED"),
    IN_TRANSIT("IN TRANSIT"),
    CANCELLED("CANCELLED")
}