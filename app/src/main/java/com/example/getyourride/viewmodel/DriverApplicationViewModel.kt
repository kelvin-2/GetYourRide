package com.example.getyourride.viewmodel

import android.content.ContentResolver
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.DriverApplicationSubmitStatus
import com.example.getyourride.data.DriverApplicationValidationResult
import com.example.getyourride.data.DriverDocumentType
import com.example.getyourride.data.DriverPersonalInfo
import com.example.getyourride.data.DriverVehicleInfo
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitRequest
import com.example.getyourride.data.repository.DocumentUpload
import com.example.getyourride.data.repository.DriverApplicationRepository
import com.example.getyourride.data.repository.DriverApplicationResult
import com.example.getyourride.ui.screens.DriverStep1Data
import com.example.getyourride.ui.screens.DriverStep2Data
import com.example.getyourride.ui.screens.DriverStep3Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DriverApplicationViewModel(
    private val repository: DriverApplicationRepository,
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
     * Submits the full driver application:
     * 1. Validates all collected data
     * 2. Sends personal + vehicle info to backend → gets applicationId
     * 3. Uploads documents using that applicationId
     * 4. Finalizes → backend returns AuthResponse (JWT + role=DRIVER_PENDING)
     *
     * On success, submitStatus becomes Success with the AuthResponse attached,
     * so MainActivity can call UserSession.save() and navigate directly to
     * the Driver Home Screen — no second login required.
     */
    fun submitApplication(data: DriverStep3Data, contentResolver: ContentResolver) {
        // Documents are OPTIONAL — student can upload them later from their profile
        val docs = mutableListOf<DocumentUpload>()

        if (data.driversLicenceFileName.isNotBlank() && data.driversLicenceUri.isNotBlank()) {
            docs.add(DocumentUpload(
                documentType = DriverDocumentType.DriversLicence,
                fileName = data.driversLicenceFileName.trim(),
                uriString = data.driversLicenceUri.trim()
            ))
        }
        if (data.vehicleRegistrationFileName.isNotBlank() && data.vehicleRegistrationUri.isNotBlank()) {
            docs.add(DocumentUpload(
                documentType = DriverDocumentType.VehicleRegistration,
                fileName = data.vehicleRegistrationFileName.trim(),
                uriString = data.vehicleRegistrationUri.trim()
            ))
        }

        val validationResult = validateCompleteApplication()
        step3ErrorMessage = validationResult.message.takeIf { it.isNotBlank() }

        if (!validationResult.isValid) {
            submitStatus = DriverApplicationSubmitStatus.Error(validationResult.message)
            return
        }

        submitStatus = DriverApplicationSubmitStatus.Loading

        val personal = requireNotNull(personalInfo)
        val vehicle = requireNotNull(vehicleInfo)

        // Build the request DTO that matches the backend
        val request = DriverApplicationSubmitRequest(
            firstName = personal.firstName,
            surname = personal.surname,
            studentNumber = personal.studentNumber,
            contactNumber = personal.contactNumber,
            universityEmail = personal.universityEmail,
            password = personal.password,
            vehicleMakeModel = "${vehicle.vehicleMake} ${vehicle.vehicleModel}",
            registrationNumber = vehicle.vehicleRegistrationNumber,
            seatingCapacity = vehicle.seatingCapacity,
            vehicleColor = vehicle.vehicleColour
        )

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.submitFullApplication(request, docs, contentResolver)
            }

            submitStatus = when (result) {
                is DriverApplicationResult.Success -> {
                    DriverApplicationSubmitStatus.Success(
                        message = "Application submitted successfully. You are now logged in.",
                        authResponse = result.authResponse
                    )
                }
                is DriverApplicationResult.Error -> {
                    DriverApplicationSubmitStatus.Error(result.message)
                }
            }
        }
    }

    // ── Validation ──────────────────────────────────────────────────────────

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
            else -> DriverApplicationValidationResult(true)
        }
    }
}

// ── Factory — needed because DriverApplicationViewModel takes a constructor parameter ──
class DriverApplicationViewModelFactory(
    private val repository: DriverApplicationRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DriverApplicationViewModel(repository) as T
    }
}
