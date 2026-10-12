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

import com.example.getyourride.data.remote.ApiError
import com.example.getyourride.data.remote.ResultResource
import com.example.getyourride.data.remote.api.ShuttleDriverApi
import com.example.getyourride.data.remote.api.StudentAuthApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
import com.example.getyourride.data.remote.dto.StudentLoginRequest
import com.example.getyourride.data.remote.dto.StudentRegisterRequest
import com.example.getyourride.data.remote.safeApiCall
import com.example.getyourride.data.remote.toUserMessage
import com.example.getyourride.data.remote.withFallbackMessage

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
    private val shuttleDriverApi: ShuttleDriverApi? = null,
) {

    suspend fun login(email: String, password: String): AuthResult {
        val result = safeApiCall {
            api.login(StudentLoginRequest(email = email, password = password))
        }

        if (result is ResultResource.Success) return AuthResult.Success(result.data)

        // Student login failed (wrong credentials OR network failure) — try shuttle driver
        // login if available, exactly as before.
        if (shuttleDriverApi != null) {
            val driverResult = tryShuttleDriverLogin(email, password)
            if (driverResult != null) return driverResult
        }

        // Both failed — return the original student error
        return AuthResult.Error(
            (result as ResultResource.Error).error
                .withFallbackMessage("Invalid email or password.")
                .toUserMessage()
        )
    }

    /**
     * Try to log in as a shuttle driver.
     * Returns AuthResult.Success if it works, null if it fails.
     */
    private suspend fun tryShuttleDriverLogin(email: String, password: String): AuthResult? {
        val result = safeApiCall {
            shuttleDriverApi!!.login(ShuttleDriverLoginRequest(email = email, password = password))
        }
        return (result as? ResultResource.Success)?.let { AuthResult.Success(it.data) }
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
        android.util.Log.d("SIGNUP_DEBUG", "isFunded received in repository = $isFunded")   // debuging line of code
        val result = safeApiCall {
            api.register(
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
        }

        return when (result) {
            is ResultResource.Success -> AuthResult.Success(result.data)
            is ResultResource.Error   -> AuthResult.Error(
                result.error
                    // 409 with no backend text → tell the user what it means.
                    .withFallbackMessage("An account with these details already exists. Try logging in instead.")
                    .toUserMessage()
            )
        }
    }
}