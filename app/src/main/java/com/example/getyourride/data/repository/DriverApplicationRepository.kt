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
// This is the ONLY place that touches DriverApplicationApi directly.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.repository

import android.content.ContentResolver
import android.net.Uri
import com.example.getyourride.data.DriverDocumentType
import com.example.getyourride.data.remote.api.DriverApplicationApi
import com.example.getyourride.data.remote.api.DriverApplicationStatusResponse
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitRequest
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

class DriverApplicationRepository(
    private val api: DriverApplicationApi
) {

    /**
     * Full submission flow:
     * 1. Submit application data → get applicationId
     * 2. Upload each document
     * 3. Finalize → get AuthResponse (JWT + role=DRIVER_PENDING)
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
     */
    private suspend fun uploadDocument(
        applicationId: String,
        doc: DocumentUpload,
        contentResolver: ContentResolver
    ): String? {
        val uri = Uri.parse(doc.uriString)
        val bytes = try {
            contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: Exception) {
            return "Failed to read file: ${doc.fileName}"
        } ?: return "Failed to read file: ${doc.fileName}"

        val requestFile = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData(
            "file", doc.fileName, requestFile
        )
        val typePart = doc.documentType.name
            .toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadDocument(applicationId, typePart, filePart)
        return if (response.isSuccessful) {
            null // success
        } else {
            "Failed to upload ${doc.fileName} (${response.code()})."
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
