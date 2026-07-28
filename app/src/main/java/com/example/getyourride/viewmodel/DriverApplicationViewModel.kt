package com.example.getyourride.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverApplicationViewModel(
    private val apiService: ApiService,
) : ViewModel() {

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

    /**
     * Submits the application in two phases:
     * 1. Send personal and vehicle data to get an Application ID.
     * 2. Upload document images using that ID.
     */
    fun submitApplication(data: DriverStep3Data, contentResolver: ContentResolver) {
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

        submitStatus = DriverApplicationSubmitStatus.Loading

        viewModelScope.launch {
            val request = DriverApplicationRequest(
                personalInfo = requireNotNull(personalInfo),
                vehicleInfo = requireNotNull(vehicleInfo),
                documents = emptyList() // We upload them in Phase 2
            )

            val result = withContext(Dispatchers.IO) {
                apiService.submitDriverApplication(request)
            }

            when (result) {
                is ApiResult.Success -> {
                    val appId = result.data.applicationId
                    uploadAllDocuments(appId, data, contentResolver)
                }
                is ApiResult.Error -> {
                    submitStatus = DriverApplicationSubmitStatus.Error(result.message)
                }
            }
        }
    }

    private suspend fun uploadAllDocuments(
        appId: String,
        data: DriverStep3Data,
        contentResolver: ContentResolver
    ) {
        val docsToUpload = listOf(
            Triple(DriverDocumentType.DriversLicence, data.driversLicenceFileName, data.driversLicenceUri),
            Triple(DriverDocumentType.VehicleRegistration, data.vehicleRegistrationFileName, data.vehicleRegistrationUri)
        )

        for ((type, fileName, uriString) in docsToUpload) {
            val uri = Uri.parse(uriString)
            val bytes = withContext(Dispatchers.IO) {
                try {
                    contentResolver.openInputStream(uri)?.use { it.readBytes() }
                } catch (e: Exception) {
                    null
                }
            }

            if (bytes == null) {
                submitStatus = DriverApplicationSubmitStatus.Error("Failed to read document: $fileName")
                return
            }

            val uploadResult = withContext(Dispatchers.IO) {
                apiService.uploadDriverDocument(
                    applicationId = appId,
                    documentType = type,
                    fileName = fileName,
                    contentType = "image/*",
                    fileBytes = bytes
                )
            }

            if (uploadResult is ApiResult.Error) {
                submitStatus = DriverApplicationSubmitStatus.Error("Failed to upload $fileName: ${uploadResult.message}")
                return
            }
        }

        submitStatus = DriverApplicationSubmitStatus.Success(
            "Driver profile submitted successfully. Status: Pending Verification."
        )
    }

    private fun validatePersonalInfo(info: DriverPersonalInfo): DriverApplicationValidationResult {
        return when {
            info.surname.isBlank() -> DriverApplicationValidationResult(false, "Enter your surname.")
            info.firstName.isBlank() -> DriverApplicationValidationResult(false, "Enter your first name.")
            info.studentNumber.isBlank() -> DriverApplicationValidationResult(false, "Enter your student number.")
            info.contactNumber.isBlank() -> DriverApplicationValidationResult(false, "Enter your contact number.")
            info.universityEmail.isBlank() -> DriverApplicationValidationResult(false, "Enter your university email.")
            !info.universityEmail.endsWith("@mandela.ac.za", true) ->
                DriverApplicationValidationResult(false, "Use your NMU email ending with @mandela.ac.za.")
            info.password.length < 8 -> DriverApplicationValidationResult(false, "Password must be at least 8 characters.")
            else -> DriverApplicationValidationResult(true)
        }
    }

    private fun validateVehicleInfo(info: DriverVehicleInfo): DriverApplicationValidationResult {
        return when {
            info.vehicleRegistrationNumber.isBlank() -> DriverApplicationValidationResult(false, "Enter registration number.")
            info.vehicleMake.isBlank() -> DriverApplicationValidationResult(false, "Enter vehicle make.")
            info.vehicleModel.isBlank() -> DriverApplicationValidationResult(false, "Enter vehicle model.")
            info.vehicleColour.isBlank() -> DriverApplicationValidationResult(false, "Enter vehicle colour.")
            (info.seatingCapacity !in 1..8) -> DriverApplicationValidationResult(false, "Capacity must be 1-8.")
            else -> DriverApplicationValidationResult(true)
        }
    }

    private fun validateCompleteApplication(): DriverApplicationValidationResult {
        return when {
            personalInfo == null -> DriverApplicationValidationResult(false, "Complete Step 1 first.")
            vehicleInfo == null -> DriverApplicationValidationResult(false, "Complete Step 2 first.")
            documents.any { it.originalFileName.isBlank() || it.localUri.isBlank() } ->
                DriverApplicationValidationResult(false, "Upload both required documents.")
            else -> DriverApplicationValidationResult(true)
        }
    }
}