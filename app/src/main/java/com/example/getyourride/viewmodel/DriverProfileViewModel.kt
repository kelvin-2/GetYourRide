package com.example.getyourride.viewmodel

import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * UI state for the Driver Profile screen.
 */
sealed class DriverProfileUiState {
    data object Loading : DriverProfileUiState()
    data class Success(val profile: DriverProfileResponse) : DriverProfileUiState()
    data class Error(val message: String) : DriverProfileUiState()
}

/**
 * UI state for the delete action.
 */
sealed class DriverDeleteUiState {
    data object Idle : DriverDeleteUiState()
    data object Loading : DriverDeleteUiState()
    data class Success(val message: String) : DriverDeleteUiState()
    data class Error(val message: String) : DriverDeleteUiState()
}

/**
 * UI state for document upload from the profile screen.
 */
sealed class DocumentUploadUiState {
    data object Idle : DocumentUploadUiState()
    data object Uploading : DocumentUploadUiState()
    data class Success(val message: String) : DocumentUploadUiState()
    data class Error(val message: String) : DocumentUploadUiState()
}

/** Maximum allowed file size: 5 MB */
private const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L

class DriverProfileViewModel(
    private val repository: DriverApplicationRepository
) : ViewModel() {

    var profileState by mutableStateOf<DriverProfileUiState>(DriverProfileUiState.Loading)
        private set

    var deleteState by mutableStateOf<DriverDeleteUiState>(DriverDeleteUiState.Idle)
        private set

    var uploadState by mutableStateOf<DocumentUploadUiState>(DocumentUploadUiState.Idle)
        private set

    /**
     * Whether a document upload is currently in progress.
     */
    val isUploading: Boolean
        get() = uploadState is DocumentUploadUiState.Uploading

    fun loadProfile() {
        profileState = DriverProfileUiState.Loading
        viewModelScope.launch {
            profileState = when (val result = repository.getDriverProfile()) {
                is DriverProfileResult.Success -> DriverProfileUiState.Success(result.profile)
                is DriverProfileResult.Error -> DriverProfileUiState.Error(result.message)
            }
        }
    }

    fun deleteProfile() {
        deleteState = DriverDeleteUiState.Loading
        viewModelScope.launch {
            deleteState = when (val result = repository.deleteDriverProfile()) {
                is DriverProfileDeleteResult.Success -> DriverDeleteUiState.Success(result.message)
                is DriverProfileDeleteResult.Error -> DriverDeleteUiState.Error(result.message)
            }
        }
    }

    /**
     * Upload a document from the profile screen with full validation.
     *
     * Validates:
     * - URI is readable
     * - File is an image
     * - File size is under 5MB
     *
     * After upload succeeds, reloads the profile to update document status.
     * The repository sends the correct MIME type to the backend so Cloudinary
     * stores it properly and admin can view it via the URL.
     */
    fun uploadDocument(
        documentType: String,
        uri: Uri,
        contentResolver: ContentResolver
    ) {
        // Prevent double-upload
        if (isUploading) return

        // Validate the file before uploading
        val mimeType = contentResolver.getType(uri)
        if (mimeType == null || !mimeType.startsWith("image/")) {
            uploadState = DocumentUploadUiState.Error(
                "Only image files (JPG, PNG) are accepted."
            )
            return
        }

        val fileSize = getFileSizeFromUri(contentResolver, uri)
        if (fileSize > MAX_FILE_SIZE_BYTES) {
            uploadState = DocumentUploadUiState.Error(
                "File is too large. Maximum size is 5 MB."
            )
            return
        }

        val fileName = getFileNameFromUri(contentResolver, uri)

        uploadState = DocumentUploadUiState.Uploading

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repository.uploadDocumentFromProfile(
                    documentType = documentType,
                    fileName = fileName,
                    uriString = uri.toString(),
                    contentResolver = contentResolver
                )
            }

            if (result == null) {
                uploadState = DocumentUploadUiState.Success("Document uploaded successfully.")
                // Reload profile to show updated status
                loadProfile()
            } else {
                uploadState = DocumentUploadUiState.Error(result)
            }
        }
    }

    /**
     * Clear the upload state message (after user has seen it).
     */
    fun clearUploadState() {
        uploadState = DocumentUploadUiState.Idle
    }

    private fun getFileSizeFromUri(contentResolver: ContentResolver, uri: Uri): Long {
        return try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (sizeIndex >= 0 && cursor.moveToFirst()) {
                    cursor.getLong(sizeIndex)
                } else {
                    0L
                }
            } ?: 0L
        } catch (e: Exception) {
            0L
        }
    }

    private fun getFileNameFromUri(contentResolver: ContentResolver, uri: Uri): String {
        var fileName: String? = null
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
        } catch (_: Exception) {}
        return fileName ?: uri.lastPathSegment ?: "document.jpg"
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
