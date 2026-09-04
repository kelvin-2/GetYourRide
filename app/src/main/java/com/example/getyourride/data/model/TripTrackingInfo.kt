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
    /**
     * The driver has not started the trip yet, so there is no live position to follow.
     * Distinct from [ON_THE_WAY]: a SCHEDULED trip was previously shown as "ON THE WAY", which
     * told the student the car was coming when in fact nothing was moving.
     */
    WAITING("NOT STARTED"),
    ON_THE_WAY("ON THE WAY"),
    ARRIVED("ARRIVED"),
    IN_TRANSIT("IN TRANSIT"),
    CANCELLED("CANCELLED")
}