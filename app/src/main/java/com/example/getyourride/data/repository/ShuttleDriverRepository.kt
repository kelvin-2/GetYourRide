package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.ShuttleDriverApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse

/**
 * Repository for shuttle driver operations.
 *
 * Handles:
 * - Login (admin-created credentials)
 * - Profile fetching (driver info + vehicle + trip summary)
 */
class ShuttleDriverRepository(private val api: ShuttleDriverApi) {

    /**
     * Authenticate shuttle driver with email and password.
     * Returns AuthResponse on success, throws on failure.
     */
    suspend fun login(email: String, password: String): AuthResponse {
        val response = api.login(ShuttleDriverLoginRequest(email, password))

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Login succeeded but response body was empty")
        }

        val errorBody = response.errorBody()?.string()
        throw Exception(
            when (response.code()) {
                401 -> "Invalid email or password"
                403 -> "Account not verified. Contact admin."
                404 -> "No shuttle driver account found with this email"
                else -> errorBody ?: "Login failed (${response.code()})"
            }
        )
    }

    /**
     * Fetch the full shuttle driver profile including vehicle and trip stats.
     */
    suspend fun getProfile(driverId: Long): ShuttleDriverProfileResponse {
        val response = api.getProfile(driverId)

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("Profile loaded but response body was empty")
        }

        val errorBody = response.errorBody()?.string()
        throw Exception(
            when (response.code()) {
                404 -> "Driver profile not found"
                401 -> "Session expired. Please log in again."
                else -> errorBody ?: "Failed to load profile (${response.code()})"
            }
        )
    }
}
