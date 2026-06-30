// ─────────────────────────────────────────────────────────────────────────────
// AuthDtos.kt
// Package: com.example.getyourride.data.remote.dto
//
// PURPOSE — Kotlin mirrors of your Spring Boot DTOs. Field names must match
// the Java side EXACTLY (Gson/Moshi serializes by field name).
//
// Maps to:
//   StudentLoginRequest.java     → StudentLoginRequest
//   StudentRegisterRequest.java  → StudentRegisterRequest
//   AuthResponse.java            → AuthResponse
//
// ⚠️ BACKEND TODO — StudentRegisterRequest.java is currently missing
// "isFunded". Add this to the Java class:
//     private Boolean isFunded;
// Without it, every student's NSFAS status will be saved as null.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote.dto

// ── Requests ────────────────────────────────────────────────────────────────

data class StudentLoginRequest(
    val email    : String,
    val password : String,
)

data class StudentRegisterRequest(
    val studentNumber : String,
    val firstName     : String,
    val lastName      : String,
    val email         : String,
    val phone         : String,
    val password      : String,
    val isFunded      : Boolean,  // ⚠️ requires backend field to be added — see note above
)

// ── Response ────────────────────────────────────────────────────────────────

/**
 * Mirrors AuthResponse.java exactly.
 *
 * Note: isFunded / role / isVerified are nullable because the SAME response
 * class is shared between student and driver login on the backend —
 * a student response will have role=null, a driver response will have
 * isFunded=null.
 */
data class AuthResponse(
    val token      : String,
    val type       : String,   // "STUDENT" or "DRIVER"
    val id         : Long,
    val firstName  : String,
    val lastName   : String,
    val email      : String,

    // student-specific — null for drivers
    val isFunded   : Boolean? = null,

    // driver-specific — null for students
    val role       : String?  = null,
    val isVerified : Boolean? = null,
)