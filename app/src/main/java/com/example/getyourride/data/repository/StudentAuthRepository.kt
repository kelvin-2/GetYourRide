// ─────────────────────────────────────────────────────────────────────────────
// StudentAuthRepository.kt
// Package: com.example.getyourride.data.repository
//
// PURPOSE — Wraps StudentAuthApi calls in a result type the ViewModel can
// handle safely (success / failure), instead of letting raw exceptions or
// Retrofit Response objects leak into the UI layer.
//
// This is the ONLY place that touches StudentAuthApi directly.
// ViewModels call this repository — never the Api interface directly.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.StudentAuthApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.StudentLoginRequest
import com.example.getyourride.data.remote.dto.StudentRegisterRequest

/**
 * Simple sealed result so the ViewModel can pattern-match success vs failure
 * without needing to know anything about Retrofit or HTTP status codes.
 */
sealed class AuthResult {
    data class Success(val data: AuthResponse) : AuthResult()
    data class Error(val message: String)      : AuthResult()
}

class StudentAuthRepository(
    private val api: StudentAuthApi,
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(StudentLoginRequest(email = email, password = password))
            handleResponse(response)
        } catch (e: Exception) {
            // Network failure (no internet, server down, timeout, etc.)
            AuthResult.Error(e.message ?: "Network error — could not reach server")
        }
    }

    suspend fun register(
        studentNumber : String,
        firstName     : String,
        lastName      : String,
        email         : String,
        phone         : String,
        password      : String,
        isFunded      : Boolean,
    ): AuthResult {
        android.util.Log.d("SIGNUP_DEBUG", "isFunded received in repository = $isFunded")   // ← ADD THIS LINE
        return try {
            val response = api.register(
                StudentRegisterRequest(
                    studentNumber = studentNumber,
                    firstName     = firstName,
                    lastName      = lastName,
                    email         = email,
                    phone         = phone,
                    password      = password,
                    isFunded      = isFunded,
                )
            )
            handleResponse(response)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Network error — could not reach server")
        }
    }

    // Shared logic for turning a Retrofit Response<AuthResponse> into an AuthResult
    private fun handleResponse(response: retrofit2.Response<AuthResponse>): AuthResult {
        return if (response.isSuccessful && response.body() != null) {
            AuthResult.Success(response.body()!!)
        } else {
            // Spring's @Valid validation errors come back as 400 with a message body —
            // for now we show a generic message; can be improved to parse the error JSON.
            AuthResult.Error("Login/Signup failed (${response.code()}). Check your details and try again.")
        }
    }
}