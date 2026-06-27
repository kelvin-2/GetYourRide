package com.example.getyourride.data

data class OfferRideRequest(
    val pickupLocation: String,
    val destination: String,
    val rideDate: String,
    val rideTime: String,
    val availableSeats: Int,
    val farePerSeat: Double
)

data class OfferRideResponse(
    val tripId: String,
    val message: String
)

data class DeleteDriverProfileRequest(
    val reason: String = ""
)

data class DeleteDriverProfileResponse(
    val driverProfileId: String,
    val status: DriverApplicationStatus,
    val message: String
)

data class StudentProfileRequest(
    val firstName: String,
    val surname: String,
    val studentNumber: String,
    val contactNumber: String,
    val universityEmail: String,
    val password: String,
    val nsfasFunded: Boolean
)

data class StudentProfileResponse(
    val userId: String,
    val universityEmail: String,
    val message: String
)

data class ValidationResult(
    val isValid: Boolean,
    val message: String = ""
)

sealed class UseCaseSubmitStatus {
    data object Idle : UseCaseSubmitStatus()
    data object Loading : UseCaseSubmitStatus()
    data class Success(val message: String) : UseCaseSubmitStatus()
    data class Error(val message: String) : UseCaseSubmitStatus()
}
