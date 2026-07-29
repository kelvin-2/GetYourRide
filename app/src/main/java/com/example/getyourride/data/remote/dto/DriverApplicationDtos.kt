// ─────────────────────────────────────────────────────────────────────────────
// DriverApplicationDtos.kt
// Package: com.example.getyourride.data.remote.dto
//
// PURPOSE — DTOs for the Driver Application Retrofit API.
// These mirror the backend's request/response objects for:
//   POST /api/driver-applications
//   POST /api/driver-applications/{id}/documents
//   POST /api/driver-applications/{id}/finalize
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote.dto

/**
 * Request body for Phase 1: submitting the driver application.
 * Matches the backend's DriverApplicationRequest DTO.
 */
data class DriverApplicationSubmitRequest(
    // Personal info
    val firstName: String,
    val surname: String,
    val studentNumber: String,
    val contactNumber: String,
    val universityEmail: String,
    val password: String,

    // Vehicle info
    val vehicleMakeModel: String,
    val registrationNumber: String,
    val seatingCapacity: Int,
    val vehicleColor: String
)

/**
 * Response from Phase 1: the backend creates the application record
 * and returns an applicationId for document uploads.
 */
data class DriverApplicationSubmitResponse(
    val applicationId: String,
    val status: String = "PENDING"
)
