package com.example.getyourride.viewmodel

import android.content.ContentResolver
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.api.UpdateDriverProfileRequest
import com.example.getyourride.data.repository.DriverApplicationRepository
import com.example.getyourride.data.repository.DriverProfileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loading state for the initial profile fetch that pre-fills the edit form.
 */
sealed class EditProfileLoadState {
    data object Loading : EditProfileLoadState()
    data object Loaded : EditProfileLoadState()
    data class Error(val message: String) : EditProfileLoadState()
}

/**
 * Outcome of a save attempt.
 */
sealed class EditProfileSaveState {
    data object Idle : EditProfileSaveState()
    data object Saving : EditProfileSaveState()
    data object Success : EditProfileSaveState()
    data class Error(val message: String) : EditProfileSaveState()
}

/** Max upload size: 5 MB (matches the backend limit). */
private const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L

/**
 * Backs the Edit Driver Profile screen.
 *
 * Loads the current profile, exposes editable fields (contact number + vehicle details),
 * and on save updates the profile and optionally re-uploads the vehicle registration document.
 * Saving resets verification server-side (the driver goes back to "Pending Review").
 */
class EditDriverProfileViewModel(
    private val repository: DriverApplicationRepository
) : ViewModel() {

    var loadState by mutableStateOf<EditProfileLoadState>(EditProfileLoadState.Loading)
        private set

    var saveState by mutableStateOf<EditProfileSaveState>(EditProfileSaveState.Idle)
        private set

    // Read-only context (shown but not editable)
    var firstName by mutableStateOf("")
        private set
    var surname by mutableStateOf("")
        private set
    var email by mutableStateOf("")
        private set

    // Editable fields
    var contactNumber by mutableStateOf("")
    var vehicleMakeModel by mutableStateOf("")
    var registrationNumber by mutableStateOf("")
    var vehicleColour by mutableStateOf("")
    var seatingCapacity by mutableStateOf("")
    var vehicleYear by mutableStateOf("")

    /** URI of a newly picked vehicle registration document, or null to keep the current one. */
    var newRegistrationDocUri by mutableStateOf<Uri?>(null)
        private set
    var newRegistrationDocFileName by mutableStateOf<String?>(null)
        private set

    fun onRegistrationDocPicked(uri: Uri, fileName: String?) {
        newRegistrationDocUri = uri
        newRegistrationDocFileName = fileName
    }

    fun loadProfile() {
        loadState = EditProfileLoadState.Loading
        viewModelScope.launch {
            when (val result = repository.getDriverProfile()) {
                is DriverProfileResult.Success -> {
                    val p = result.profile
                    firstName = p.firstName
                    surname = p.surname
                    email = p.email
                    contactNumber = p.contactNumber
                    // vehicleMake + vehicleModel are split on the backend; recombine for editing.
                    vehicleMakeModel = listOf(p.vehicleMake, p.vehicleModel)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")
                    registrationNumber = p.registrationNumber
                    vehicleColour = p.vehicleColour
                    seatingCapacity = if (p.seatingCapacity > 0) p.seatingCapacity.toString() else ""
                    vehicleYear = p.vehicleYear?.toString() ?: ""
                    loadState = EditProfileLoadState.Loaded
                }
                is DriverProfileResult.Error -> {
                    loadState = EditProfileLoadState.Error(result.message)
                }
            }
        }
    }

    /**
     * Validate + save. On success the save state becomes Success and the screen navigates back.
     */
    fun save(contentResolver: ContentResolver) {
        if (saveState is EditProfileSaveState.Saving) return

        // Basic validation
        if (contactNumber.isBlank()) {
            saveState = EditProfileSaveState.Error("Contact number is required.")
            return
        }
        if (vehicleMakeModel.isBlank()) {
            saveState = EditProfileSaveState.Error("Vehicle make & model is required.")
            return
        }
        if (registrationNumber.isBlank()) {
            saveState = EditProfileSaveState.Error("Registration number is required.")
            return
        }
        val capacity = seatingCapacity.toIntOrNull()
        if (capacity == null || capacity <= 0) {
            saveState = EditProfileSaveState.Error("Seating capacity must be a positive number.")
            return
        }
        val year = if (vehicleYear.isBlank()) null else vehicleYear.toIntOrNull()
        if (vehicleYear.isNotBlank() && year == null) {
            saveState = EditProfileSaveState.Error("Vehicle year must be a number.")
            return
        }

        saveState = EditProfileSaveState.Saving
        viewModelScope.launch {
            // 1. Update the structured fields.
            val request = UpdateDriverProfileRequest(
                contactNumber = contactNumber.trim(),
                vehicleMakeModel = vehicleMakeModel.trim(),
                registrationNumber = registrationNumber.trim(),
                seatingCapacity = capacity,
                vehicleColor = vehicleColour.trim(),
                vehicleYear = year
            )

            when (val result = repository.updateDriverProfile(request)) {
                is DriverProfileResult.Error -> {
                    saveState = EditProfileSaveState.Error(result.message)
                    return@launch
                }
                is DriverProfileResult.Success -> { /* continue to optional doc upload */ }
            }

            // 2. Optionally re-upload the vehicle registration document.
            val uri = newRegistrationDocUri
            if (uri != null) {
                val fileName = newRegistrationDocFileName ?: "VehicleRegistration.jpg"
                val mimeType = contentResolver.getType(uri)
                if (mimeType == null || !mimeType.startsWith("image/")) {
                    saveState = EditProfileSaveState.Error(
                        "Profile saved, but the registration document must be an image (JPG, PNG)."
                    )
                    return@launch
                }
                val uploadError = withContext(Dispatchers.IO) {
                    repository.uploadDocumentFromProfile(
                        documentType = "VehicleRegistration",
                        fileName = fileName,
                        uriString = uri.toString(),
                        contentResolver = contentResolver
                    )
                }
                if (uploadError != null) {
                    saveState = EditProfileSaveState.Error(
                        "Profile saved, but the document upload failed: $uploadError"
                    )
                    return@launch
                }
            }

            saveState = EditProfileSaveState.Success
        }
    }

    fun consumeError() {
        if (saveState is EditProfileSaveState.Error) {
            saveState = EditProfileSaveState.Idle
        }
    }
}

class EditDriverProfileViewModelFactory(
    private val repository: DriverApplicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return EditDriverProfileViewModel(repository) as T
    }
}
