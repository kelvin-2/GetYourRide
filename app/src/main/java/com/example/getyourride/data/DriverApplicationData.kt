package com.example.getyourride.data

import com.example.getyourride.data.remote.dto.AuthResponse

data class DriverPersonalInfo(
    val surname: String,
    val firstName: String,
    val studentNumber: String,
    val contactNumber: String,
    val universityEmail: String,
    val password: String
)

data class DriverVehicleInfo(
    val vehicleRegistrationNumber: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleColour: String,
    val seatingCapacity: Int
)

data class DriverDocumentInfo(
    val documentType: DriverDocumentType,
    val originalFileName: String,
    val localUri: String = "",
    val cloudUrl: String = ""
)

data class DriverApplicationRequest(
    val personalInfo: DriverPersonalInfo,
    val vehicleInfo: DriverVehicleInfo,
    val documents: List<DriverDocumentInfo>,
    val status: DriverApplicationStatus = DriverApplicationStatus.PendingReview
)

data class DriverApplicationResponse(
    val applicationId: String,
    val status: DriverApplicationStatus
)

data class DriverApplicationValidationResult(
    val isValid: Boolean,
    val message: String = ""
)

/**
 * UI-facing state for the driver application submission flow.
 *
 * Success now carries the AuthResponse from the backend's auto-login.
 * After a successful submission the backend returns JWT + student info +
 * role=DRIVER_PENDING — the app saves this in UserSession and navigates
 * directly to the Driver Home Screen without requiring a second login.
 */
sealed class DriverApplicationSubmitStatus {
    data object Idle : DriverApplicationSubmitStatus()
    data object Loading : DriverApplicationSubmitStatus()
    data class Success(
        val message: String,
        val authResponse: AuthResponse? = null
    ) : DriverApplicationSubmitStatus()
    data class Error(val message: String) : DriverApplicationSubmitStatus()
}

enum class DriverDocumentType {
    DriversLicence,
    VehicleRegistration
}

enum class DriverApplicationStatus {
    PendingReview,
    Approved,
    Rejected
}
