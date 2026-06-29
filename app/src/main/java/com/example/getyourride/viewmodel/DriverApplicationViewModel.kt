package com.example.getyourride.viewmodel

import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.getyourride.data.DriverApplicationRequest
import com.example.getyourride.data.DriverApplicationSubmitStatus
import com.example.getyourride.data.DriverApplicationValidationResult
import com.example.getyourride.data.DriverDocumentInfo
import com.example.getyourride.data.DriverDocumentType
import com.example.getyourride.data.DriverPersonalInfo
import com.example.getyourride.data.DriverVehicleInfo
import com.example.getyourride.network.ApiResult
import com.example.getyourride.network.ApiService
import com.example.getyourride.ui.screens.DriverStep1Data
import com.example.getyourride.ui.screens.DriverStep2Data
import com.example.getyourride.ui.screens.DriverStep3Data

class DriverApplicationViewModel(
    private val apiService: ApiService
) : ViewModel() {

    private val mainHandler = Handler(Looper.getMainLooper())

    var step1ErrorMessage by mutableStateOf<String?>(null)
        private set

    var step2ErrorMessage by mutableStateOf<String?>(null)
        private set

    var step3ErrorMessage by mutableStateOf<String?>(null)
        private set

    var submitStatus by mutableStateOf<DriverApplicationSubmitStatus>(
        DriverApplicationSubmitStatus.Idle
    )
        private set

    private var personalInfo: DriverPersonalInfo? = null
    private var vehicleInfo: DriverVehicleInfo? = null
    private var documents: List<DriverDocumentInfo> = emptyList()

    fun saveStep1(data: DriverStep1Data): Boolean {
        val info = DriverPersonalInfo(
            surname = data.surname.trim(),
            firstName = data.firstName.trim(),
            studentNumber = data.studentNumber.trim(),
            contactNumber = data.contactNumber.trim(),
            universityEmail = data.universityEmail.trim(),
            password = data.password
        )

        val validationResult = validatePersonalInfo(info)
        step1ErrorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (validationResult.isValid) {
            personalInfo = info
        }

        return validationResult.isValid
    }

    fun saveStep2(data: DriverStep2Data): Boolean {
        val info = DriverVehicleInfo(
            vehicleRegistrationNumber = data.vehicleRegistrationNumber.trim(),
            vehicleMake = data.vehicleMake.trim(),
            vehicleModel = data.vehicleModel.trim(),
            vehicleColour = data.vehicleColour.trim(),
            seatingCapacity = data.seatingCapacity
        )

        val validationResult = validateVehicleInfo(info)
        step2ErrorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (validationResult.isValid) {
            vehicleInfo = info
        }

        return validationResult.isValid
    }

    fun submitApplication(data: DriverStep3Data) {
        documents = listOf(
            DriverDocumentInfo(
                documentType = DriverDocumentType.DriversLicence,
                originalFileName = data.driversLicenceFileName.trim(),
                localUri = data.driversLicenceUri.trim()
            ),
            DriverDocumentInfo(
                documentType = DriverDocumentType.VehicleRegistration,
                originalFileName = data.vehicleRegistrationFileName.trim(),
                localUri = data.vehicleRegistrationUri.trim()
            )
        )

        val validationResult = validateCompleteApplication()
        step3ErrorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (!validationResult.isValid) {
            submitStatus = DriverApplicationSubmitStatus.Error(validationResult.message)
            return
        }

        val request = DriverApplicationRequest(
            personalInfo = requireNotNull(personalInfo),
            vehicleInfo = requireNotNull(vehicleInfo),
            documents = documents
        )

        submitStatus = DriverApplicationSubmitStatus.Loading

        Thread {
            val result = apiService.submitDriverApplication(request)

            mainHandler.post {
                submitStatus = when (result) {
                    is ApiResult.Success -> DriverApplicationSubmitStatus.Success(
                        "Driver profile submitted. Status: Pending Verification."
                    )

                    is ApiResult.Error -> DriverApplicationSubmitStatus.Error(result.message)
                }
            }
        }.start()
    }

    private fun validatePersonalInfo(
        info: DriverPersonalInfo
    ): DriverApplicationValidationResult {
        return when {
            info.surname.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter your surname.")
            }

            info.firstName.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter your first name.")
            }

            info.studentNumber.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter your student number.")
            }

            info.contactNumber.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter your contact number.")
            }

            info.universityEmail.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter your university email.")
            }

            !info.universityEmail.endsWith("@mandela.ac.za", ignoreCase = true) -> {
                DriverApplicationValidationResult(
                    false,
                    "Use your NMU email ending with @mandela.ac.za."
                )
            }

            info.password.length < 8 -> {
                DriverApplicationValidationResult(
                    false,
                    "Password must be at least 8 characters."
                )
            }

            else -> DriverApplicationValidationResult(true)
        }
    }

    private fun validateVehicleInfo(
        info: DriverVehicleInfo
    ): DriverApplicationValidationResult {
        return when {
            info.vehicleRegistrationNumber.isBlank() -> {
                DriverApplicationValidationResult(
                    false,
                    "Enter the vehicle registration number."
                )
            }

            info.vehicleMake.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter the vehicle make.")
            }

            info.vehicleModel.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter the vehicle model.")
            }

            info.vehicleColour.isBlank() -> {
                DriverApplicationValidationResult(false, "Enter the vehicle colour.")
            }

            info.seatingCapacity !in 1..8 -> {
                DriverApplicationValidationResult(
                    false,
                    "Seating capacity must be between 1 and 8."
                )
            }

            else -> DriverApplicationValidationResult(true)
        }
    }

    private fun validateCompleteApplication(): DriverApplicationValidationResult {
        return when {
            personalInfo == null -> {
                DriverApplicationValidationResult(
                    false,
                    "Complete Step 1 before submitting."
                )
            }

            vehicleInfo == null -> {
                DriverApplicationValidationResult(
                    false,
                    "Complete Step 2 before submitting."
                )
            }

            documents.any { it.originalFileName.isBlank() } -> {
                DriverApplicationValidationResult(
                    false,
                    "Upload both required document images before submitting."
                )
            }

            documents.any { it.localUri.isBlank() } -> {
                DriverApplicationValidationResult(
                    false,
                    "Choose valid images for both required documents."
                )
            }

            else -> DriverApplicationValidationResult(true)
        }
    }
}