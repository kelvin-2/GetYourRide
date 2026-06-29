package com.example.getyourride.data

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
    val status: DriverApplicationStatus = DriverApplicationStatus.PendingVerification
)

data class DriverApplicationResponse(
    val applicationId: String,
    val status: DriverApplicationStatus
)

data class DriverApplicationValidationResult(
    val isValid: Boolean,
    val message: String = ""
)

sealed class DriverApplicationSubmitStatus {
    data object Idle : DriverApplicationSubmitStatus()
    data object Loading : DriverApplicationSubmitStatus()
    data class Success(val message: String) : DriverApplicationSubmitStatus()
    data class Error(val message: String) : DriverApplicationSubmitStatus()
}

enum class DriverDocumentType {
    DriversLicence,
    VehicleRegistration
}

enum class DriverApplicationStatus {
    PendingVerification,
    Approved,
    Rejected
}
