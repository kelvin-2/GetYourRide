package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.ShuttleDriverApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.MarkAsBoardedRequest
import com.example.getyourride.data.remote.dto.MarkAsBoardedResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse

/**
 * Repository for shuttle driver operations.
 *
 * Handles:
 * - Login (admin-created credentials)
 * - Profile fetching (driver info + vehicle + trip summary)
 * - Boarding (active trip, booked students, mark as boarded)
 */
class ShuttleDriverRepository(private val api: ShuttleDriverApi) {

    /**
     * Authenticate shuttle driver with email and password.
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

    // ── Boarding Methods ────────────────────────────────────────────────────

    /**
     * Fetch the current/next active trip for this shuttle driver.
     */
    suspend fun getActiveTrip(driverId: Long): ShuttleDriverActiveTripResponse {
        val response = api.getActiveTrip(driverId)

        if (response.isSuccessful) {
            return response.body()
                ?: throw Exception("No active trip found")
        }

        val errorBody = response.errorBody()?.string()
        throw Exception(
            when (response.code()) {
                404 -> "No active trip assigned. Check your schedule."
                401 -> "Session expired. Please log in again."
                else -> errorBody ?: "Failed to load trip (${response.code()})"
            }
        )
    }

    /**
     * Fetch all booked students for a specific trip.
     */
    suspend fun getBookedStudents(tripId: Long): List<BoardedStudentResponse> {
        val response = api.getBookedStudents(tripId)

        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        }

        val errorBody = response.errorBody()?.string()
        throw Exception(
            errorBody ?: "Failed to load students (${response.code()})"
        )
    }

    /**
     * Mark a student as boarded — updates boarding_log.boarded_at.
     */
    suspend fun markAsBoarded(bookingId: Long): MarkAsBoardedResponse {
        val response = api.markAsBoarded(MarkAsBoardedRequest(bookingId))

        if (response.isSuccessful) {
            return response.body()
                ?: MarkAsBoardedResponse(true, "Marked as boarded", null)
        }

        val errorBody = response.errorBody()?.string()
        throw Exception(
            errorBody ?: "Failed to mark as boarded (${response.code()})"
        )
    }
}
