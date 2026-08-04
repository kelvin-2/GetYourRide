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

import com.example.getyourride.data.remote.api.ShuttleDriverApi
import com.example.getyourride.data.remote.api.StudentAuthApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
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
    private val shuttleDriverApi: ShuttleDriverApi? = null,
) {

    suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = api.login(StudentLoginRequest(email = email, password = password))

            if (response.isSuccessful && response.body() != null) {
                return AuthResult.Success(response.body()!!)
            }

            // Student login failed — try shuttle driver login if available
            if (shuttleDriverApi != null) {
                val driverResult = tryShuttleDriverLogin(email, password)
                if (driverResult != null) return driverResult
            }

            // Both failed — return original student error
            handleResponse(response)
        } catch (e: Exception) {
            // Network failure on student login — still try shuttle driver
            if (shuttleDriverApi != null) {
                val driverResult = tryShuttleDriverLogin(email, password)
                if (driverResult != null) return driverResult
            }
            AuthResult.Error(e.message ?: "Network error — could not reach server")
        }
    }

    /**
     * Try to log in as a shuttle driver.
     * Returns AuthResult.Success if it works, null if it fails.
     */
    private suspend fun tryShuttleDriverLogin(email: String, password: String): AuthResult? {
        return try {
            val response = shuttleDriverApi!!.login(
                ShuttleDriverLoginRequest(email = email, password = password)
            )
            if (response.isSuccessful && response.body() != null) {
                AuthResult.Success(response.body()!!)
            } else {
                null
            }
        } catch (e: Exception) {
            null
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
        android.util.Log.d("SIGNUP_DEBUG", "isFunded received in repository = $isFunded")   // debuging line of code
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
            val errorBody = response.errorBody()?.string()
            val message = extractMessage(errorBody) ?: when (response.code()) {
                401  -> "Invalid email or password."
                404  -> "Account does not exist."
                409  -> "User already exists."
                400  -> "Invalid request. Please check your details."
                else -> "Login/Signup failed (${response.code()}). Try again."
            }
            AuthResult.Error(message)
        }
    }

    private fun extractMessage(json: String?): String? {
        if (json.isNullOrBlank()) return null
        return try {
            // Simple regex for "message":"..." if we don't want to add a full JSON parser dependency here
            val regex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
            regex.find(json)?.groupValues?.get(1)
        } catch (e: Exception) {
            null
        }
    }
}
