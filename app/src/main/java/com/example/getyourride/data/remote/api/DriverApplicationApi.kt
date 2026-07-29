// ─────────────────────────────────────────────────────────────────────────────
// DriverApplicationApi.kt
// Package: com.example.getyourride.data.remote.api
//
// PURPOSE — Retrofit interface for the Driver Application endpoints.
//
// The key flow:
//   1. POST /api/driver-applications          → submit application data
//   2. POST /api/driver-applications/{id}/documents → upload documents
//
// After documents are uploaded, the backend auto-logs the student in and
// returns the same AuthResponse as the student login endpoint — with
// role = "DRIVER_PENDING".
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitRequest
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

interface DriverApplicationApi {

    /**
     * Phase 1: Submit the driver application (personal info + vehicle info).
     * Returns an applicationId that we use to upload documents in Phase 2.
     */
    @POST("api/driver-applications")
    suspend fun submitApplication(
        @Body request: DriverApplicationSubmitRequest
    ): Response<DriverApplicationSubmitResponse>

    /**
     * Phase 2: Upload a document (driver's licence or vehicle registration).
     * Uses multipart form data.
     */
    @Multipart
    @POST("api/driver-applications/{applicationId}/documents")
    suspend fun uploadDocument(
        @Path("applicationId") applicationId: String,
        @Part("documentType") documentType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    /**
     * Phase 3: Finalize the application — tells the backend all documents are
     * uploaded and it can process the application. The backend validates the
     * NMU email, creates/finds the student, saves the driverapplications
     * record with status=PENDING, and returns an AuthResponse (JWT + student
     * info + role=DRIVER_PENDING) so the app auto-logs the student in.
     */
    @POST("api/driver-applications/{applicationId}/finalize")
    suspend fun finalizeApplication(
        @Path("applicationId") applicationId: String
    ): Response<AuthResponse>

    /**
     * Check application status — used on Driver Home to show current status.
     */
    @GET("api/driver-applications/status")
    suspend fun getApplicationStatus(): Response<DriverApplicationStatusResponse>
}

/**
 * Response from the status check endpoint.
 */
data class DriverApplicationStatusResponse(
    val applicationId: String,
    val status: String,  // "PENDING", "APPROVED", "REJECTED"
    val message: String? = null
)
