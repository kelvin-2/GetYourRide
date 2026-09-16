// ─────────────────────────────────────────────────────────────────────────────
// DriverApplicationRepository.kt
// Package: com.example.getyourride.data.repository
//
// PURPOSE — Wraps DriverApplicationApi calls in a clean result type the
// ViewModel can safely consume. Handles the full 3-phase submission:
//   Phase 1: Submit personal + vehicle data → get applicationId
//   Phase 2: Upload documents (licence, registration)
//   Phase 3: Finalize → backend returns AuthResponse (auto-login)
//
// Documents are uploaded with the CORRECT content type (detected from the URI)
// so the backend can store the file in a format that admin can access directly
// via a URL link. The backend stores/serves the files so the admin panel can
// display them when reviewing applications.
//
// This is the ONLY place that touches DriverApplicationApi directly.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.getyourride.data.DriverDocumentType
import com.example.getyourride.data.remote.api.DriverApplicationApi
import com.example.getyourride.data.remote.api.DriverApplicationStatusResponse
import com.example.getyourride.data.remote.api.DriverProfileResponse
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitRequest
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/** Maximum allowed file size: 5 MB */
private const val MAX_FILE_SIZE_BYTES = 5L * 1024L * 1024L

/**
 * Result type for the full driver application flow.
 * Success carries the AuthResponse from the finalize step (auto-login).
 */
sealed class DriverApplicationResult {
    data class Success(val authResponse: AuthResponse) : DriverApplicationResult()
    data class Error(val message: String) : DriverApplicationResult()
}

/**
 * Result type for checking application status.
 */
sealed class ApplicationStatusResult {
    data class Success(val status: DriverApplicationStatusResponse) : ApplicationStatusResult()
    data class Error(val message: String) : ApplicationStatusResult()
}

/**
 * Result type for fetching driver profile.
 */
sealed class DriverProfileResult {
    data class Success(val profile: DriverProfileResponse) : DriverProfileResult()
    data class Error(val message: String) : DriverProfileResult()
}

/**
 * Result type for deleting/deactivating driver profile.
 */
sealed class DriverProfileDeleteResult {
    data class Success(val message: String) : DriverProfileDeleteResult()
    data class Error(val message: String) : DriverProfileDeleteResult()
}

class DriverApplicationRepository(
    private val api: DriverApplicationApi
) {

    /**
     * Full submission flow:
     * 1. Submit application data → get applicationId
     * 2. Upload each document (with correct MIME type for admin retrieval)
     * 3. Finalize → get AuthResponse (JWT + role=DRIVER_PENDING)
     *
     * Documents are uploaded with the actual content type detected from the URI
     * so the backend can store them correctly and serve them back to admin via
     * a direct URL link.
     */
    suspend fun submitFullApplication(
        request: DriverApplicationSubmitRequest,
        documents: List<DocumentUpload>,
        contentResolver: ContentResolver
    ): DriverApplicationResult {
        return try {
            // ── Phase 1: Submit personal + vehicle info ──────────────────
            val submitResponse = api.submitApplication(request)
            if (!submitResponse.isSuccessful || submitResponse.body() == null) {
                val errorMsg = extractMessage(submitResponse.errorBody()?.string())
                    ?: "Failed to submit application (${submitResponse.code()})."
                return DriverApplicationResult.Error(errorMsg)
            }

            val applicationId = submitResponse.body()!!.applicationId

            // ── Phase 2: Upload documents ───────────────────────────────
            for (doc in documents) {
                val uploadResult = uploadDocument(applicationId, doc, contentResolver)
                if (uploadResult != null) {
                    return DriverApplicationResult.Error(uploadResult)
                }
            }

            // ── Phase 3: Finalize — backend returns AuthResponse ────────
            val finalizeResponse = api.finalizeApplication(applicationId)
            if (!finalizeResponse.isSuccessful || finalizeResponse.body() == null) {
                val errorMsg = extractMessage(finalizeResponse.errorBody()?.string())
                    ?: "Application submitted but auto-login failed (${finalizeResponse.code()})."
                return DriverApplicationResult.Error(errorMsg)
            }

            DriverApplicationResult.Success(finalizeResponse.body()!!)

        } catch (e: Exception) {
            DriverApplicationResult.Error(
                e.message ?: "Network error — could not reach server."
            )
        }
    }

    /**
     * Upload a single document. Returns null on success, or an error message.
     *
     * Key improvements:
     * - Detects the REAL content type from the URI (e.g. image/jpeg, image/png)
     *   so the backend stores the file with the correct extension/format.
     * - Validates file size before uploading to prevent OOM and server rejections.
     * - Includes the original filename so admin can see a meaningful name.
     * - The backend should store the file and generate a permanent URL that
     *   admin can use to view the document when reviewing applications.
     */
    private suspend fun uploadDocument(
        applicationId: String,
        doc: DocumentUpload,
        contentResolver: ContentResolver
    ): String? {
        val uri = Uri.parse(doc.uriString)

        // Detect the actual MIME type from the content resolver
        val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

        // Validate that it's an image type
        if (!mimeType.startsWith("image/")) {
            return "Only image files are accepted for ${doc.fileName}. Got: $mimeType"
        }

        // Read file bytes with size validation
        val bytes = try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val data = inputStream.readBytes()
                if (data.size > MAX_FILE_SIZE_BYTES) {
                    return "File ${doc.fileName} is too large (${data.size / 1024 / 1024}MB). Maximum is 5MB."
                }
                data
            }
        } catch (e: SecurityException) {
            return "Permission denied: cannot read ${doc.fileName}. Please select the file again."
        } catch (e: OutOfMemoryError) {
            return "File ${doc.fileName} is too large to process. Please use a smaller image."
        } catch (e: Exception) {
            return "Failed to read file: ${doc.fileName}"
        } ?: return "Failed to read file: ${doc.fileName}"

        // Build the multipart request with the CORRECT content type
        // This ensures the backend knows the exact file format and can store
        // it in a way that produces a working URL for admin to view.
        val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "file", doc.fileName, requestFile
        )
        val typePart = doc.documentType.name
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadDocument(applicationId, typePart, filePart)
        return if (response.isSuccessful) {
            null // success
        } else {
            val errorBody = response.errorBody()?.string()
            val errorMsg = extractMessage(errorBody)
            errorMsg ?: "Failed to upload ${doc.fileName} (${response.code()})."
        }
    }

    /**
     * Check current application status for the logged-in student.
     */
    suspend fun getApplicationStatus(): ApplicationStatusResult {
        return try {
            val response = api.getApplicationStatus()
            if (response.isSuccessful && response.body() != null) {
                ApplicationStatusResult.Success(response.body()!!)
            } else {
                val errorMsg = extractMessage(response.errorBody()?.string())
                    ?: "Could not fetch application status (${response.code()})."
                ApplicationStatusResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            ApplicationStatusResult.Error(
                e.message ?: "Network error — could not reach server."
            )
        }
    }

    /**
     * Fetch the full driver profile (personal + vehicle + document status).
     * JWT token is attached automatically by the auth interceptor in NetworkModule.
     */
    suspend fun getDriverProfile(): DriverProfileResult {
        return try {
            val response = api.getDriverProfile()
            if (response.isSuccessful && response.body() != null) {
                DriverProfileResult.Success(response.body()!!)
            } else {
                val errorMsg = extractMessage(response.errorBody()?.string())
                    ?: "Could not fetch driver profile (${response.code()})."
                DriverProfileResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            DriverProfileResult.Error(
                e.message ?: "Network error — could not reach server."
            )
        }
    }

    /**
     * Permanently delete the driver profile. Backend reads driver_id from JWT.
     */
    suspend fun deleteDriverProfile(): DriverProfileDeleteResult {
        return try {
            val response = api.deleteDriverProfile()
            if (response.isSuccessful && response.body() != null) {
                DriverProfileDeleteResult.Success(response.body()!!.message)
            } else {
                val errorMsg = extractMessage(response.errorBody()?.string())
                    ?: "Could not delete profile (${response.code()})."
                DriverProfileDeleteResult.Error(errorMsg)
            }
        } catch (e: Exception) {
            DriverProfileDeleteResult.Error(
                e.message ?: "Network error — could not reach server."
            )
        }
    }

    /**
     * Upload a document from the profile screen (after initial application).
     * Uses the dedicated profile upload endpoint that gets the applicationId
     * from the JWT token on the backend side — no need to look it up ourselves.
     * Sends the correct MIME type so admin can view the document via Cloudinary URL.
     * Returns null on success, or an error message.
     */
    suspend fun uploadDocumentFromProfile(
        documentType: String,
        fileName: String,
        uriString: String,
        contentResolver: ContentResolver
    ): String? {
        return try {
            val uri = Uri.parse(uriString)

            // Detect real MIME type
            val mimeType = contentResolver.getType(uri) ?: "image/jpeg"

            if (!mimeType.startsWith("image/")) {
                return "Only image files are accepted. Got: $mimeType"
            }

            // Read and validate size
            val bytes = contentResolver.openInputStream(uri)?.use { inputStream ->
                val data = inputStream.readBytes()
                if (data.size > MAX_FILE_SIZE_BYTES) {
                    return "File is too large (${data.size / 1024 / 1024}MB). Maximum is 5MB."
                }
                data
            } ?: return "Failed to read file."

            // Upload with correct content type for admin accessibility
            val requestFile = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", fileName, requestFile)
            val typePart = documentType.toRequestBody("text/plain".toMediaTypeOrNull())

            // Use the profile-specific upload endpoint (backend finds applicationId from JWT)
            val response = api.uploadDocumentFromProfile(typePart, filePart)
            if (response.isSuccessful) null else "Upload failed (${response.code()})."
        } catch (e: SecurityException) {
            "Permission denied. Please select the file again."
        } catch (e: OutOfMemoryError) {
            "File is too large to process. Please use a smaller image."
        } catch (e: Exception) {
            e.message ?: "Network error."
        }
    }

    private fun extractMessage(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            regex.find(json)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Simple holder for a document to be uploaded.
 */
data class DocumentUpload(
    val documentType: DriverDocumentType,
    val fileName: String,
    val uriString: String
)
