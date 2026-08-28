// ─────────────────────────────────────────────────────────────────────────────
// DriverApplicationApi.kt
// Package: com.example.getyourride.data.remote.api
//
// PURPOSE — Retrofit interface for the Driver Application endpoints.
//
// The key flow:
//   1. POST /api/driver-applications              → submit personal + vehicle data
//      Backend: creates a row in `driver` (role=STUDENT_DRIVER, is_verified=false)
//               creates a row in `driverapplications` (status=Pending Review)
//   2. POST /api/driver-applications/{id}/documents → upload documents (optional)
//   3. POST /api/driver-applications/{id}/finalize  → auto-login
//      Backend: returns AuthResponse (JWT + type=DRIVER + role=DRIVER_PENDING)
//
// The student is NOT inserted into the `student` table during this flow.
// They become a driver directly.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitRequest
import com.example.getyourride.data.remote.dto.DriverApplicationSubmitResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
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
     * Phase 3: Finalize the application — the backend generates a JWT for
     * the newly created driver and returns an AuthResponse (JWT + driver
     * info + role=DRIVER_PENDING) so the app auto-logs them in.
     * No second login required.
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

    /**
     * Get the full driver profile (personal + vehicle + document status).
     * Backend reads the driver_id from the JWT token.
     */
    @GET("api/driver-profile")
    suspend fun getDriverProfile(): Response<DriverProfileResponse>

    /**
     * Upload a document directly from the profile screen.
     * Backend uses the JWT token to find the driver's application and attach the document.
     * This is for students who skipped document upload during Step 3.
     */
    @Multipart
    @POST("api/driver-profile/upload-document")
    suspend fun uploadDocumentFromProfile(
        @Part("documentType") documentType: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<Unit>

    /**
     * Permanently delete the driver profile.
     * Backend reads the driver_id from the JWT token and removes all associated data.
     */
    @DELETE("api/driver-profile")
    suspend fun deleteDriverProfile(): Response<DriverProfileDeleteResponse>
}

/**
 * Response from the status check endpoint.
 */
data class DriverApplicationStatusResponse(
    val applicationId: String,
    val status: String,  // "PENDING_REVIEW", "APPROVED", "REJECTED"
    val message: String? = null
)

/**
 * Response from GET /api/driver-profile.
 * Contains all information displayed on the Driver Profile Settings screen.
 */
data class DriverProfileResponse(
    // Personal details
    val firstName: String,
    val surname: String,
    val studentNumber: String,
    val email: String,
    val contactNumber: String,

    // Vehicle details
    val vehicleMake: String,
    val vehicleModel: String,
    val registrationNumber: String,
    val vehicleColour: String,
    val seatingCapacity: Int,

    // Application & document status
    val applicationStatus: String,       // "Pending Review", "Approved", "Rejected"
    val driversLicenceStatus: String,    // "Uploaded", "Not Uploaded"
    val vehicleRegistrationStatus: String // "Uploaded", "Not Uploaded"
)

/**
 * Response from DELETE /api/driver-profile.
 * Returned after the driver profile and all associated data are permanently deleted.
 */
data class DriverProfileDeleteResponse(
    val message: String
)
