package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.api.DriverProfileResponse
import com.example.getyourride.data.repository.DriverApplicationRepository
import com.example.getyourride.data.repository.DriverProfileDeleteResult
import com.example.getyourride.data.repository.DriverProfileResult
import kotlinx.coroutines.launch

/**
 * UI state for the Driver Profile screen.
 */
sealed class DriverProfileUiState {
    data object Loading : DriverProfileUiState()
    data class Success(val profile: DriverProfileResponse) : DriverProfileUiState()
    data class Error(val message: String) : DriverProfileUiState()
}

/**
 * UI state for the delete/deactivate action.
 */
sealed class DriverDeleteUiState {
    data object Idle : DriverDeleteUiState()
    data object Loading : DriverDeleteUiState()
    data class Success(val message: String) : DriverDeleteUiState()
    data class Error(val message: String) : DriverDeleteUiState()
}

class DriverProfileViewModel(
    private val repository: DriverApplicationRepository
) : ViewModel() {

    var profileState by mutableStateOf<DriverProfileUiState>(DriverProfileUiState.Loading)
        private set

    var deleteState by mutableStateOf<DriverDeleteUiState>(DriverDeleteUiState.Idle)
        private set

    fun loadProfile() {
        profileState = DriverProfileUiState.Loading
        viewModelScope.launch {
            profileState = when (val result = repository.getDriverProfile()) {
                is DriverProfileResult.Success -> DriverProfileUiState.Success(result.profile)
                is DriverProfileResult.Error -> DriverProfileUiState.Error(result.message)
            }
        }
    }

    fun deactivateProfile() {
        deleteState = DriverDeleteUiState.Loading
        viewModelScope.launch {
            deleteState = when (val result = repository.deleteDriverProfile()) {
                is DriverProfileDeleteResult.Success -> DriverDeleteUiState.Success(result.message)
                is DriverProfileDeleteResult.Error -> DriverDeleteUiState.Error(result.message)
            }
        }
    }

    /**
     * Upload a document from the profile screen.
     * After upload succeeds, reloads the profile to update document status.
     */
    fun uploadDocument(
        documentType: String,
        fileName: String,
        uriString: String,
        contentResolver: android.content.ContentResolver
    ) {
        viewModelScope.launch {
            val result = repository.uploadDocumentFromProfile(documentType, fileName, uriString, contentResolver)
            if (result == null) {
                // Success — reload profile to show updated status
                loadProfile()
            }
            // If error, we could show it but for now just reload
        }
    }
}

class DriverProfileViewModelFactory(
    private val repository: DriverApplicationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return DriverProfileViewModel(repository) as T
    }
}
