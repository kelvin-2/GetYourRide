package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.ShuttleDriverApi
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.MarkAsBoardedRequest
import com.example.getyourride.data.remote.dto.MarkAsBoardedResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse
import com.example.getyourride.data.remote.getOrThrow
import com.example.getyourride.data.remote.safeApiCall
import com.example.getyourride.data.remote.withFallbackMessage

/**
 * Repository for shuttle driver operations.
 *
 * Handles:
 * - Login (admin-created credentials)
 * - Profile fetching (driver info + vehicle + trip summary)
 * - Boarding (active trip, booked students, mark as boarded)
 *
 * Failures are thrown as ApiException; its message is already user-friendly
 * (backend {"error": "..."} text is used when present).
 */
class ShuttleDriverRepository(private val api: ShuttleDriverApi) {

    /**
     * Authenticate shuttle driver with email and password.
     */
    suspend fun login(email: String, password: String): AuthResponse =
        safeApiCall { api.login(ShuttleDriverLoginRequest(email, password)) }
            .withFallbackMessage("Invalid email or password")
            .getOrThrow()

    /**
     * Fetch the full shuttle driver profile including vehicle and trip stats.
     */
    suspend fun getProfile(driverId: Long): ShuttleDriverProfileResponse =
        safeApiCall { api.getProfile(driverId) }
            .withFallbackMessage("Driver profile not found")
            .getOrThrow()

    // ── Boarding Methods ────────────────────────────────────────────────────

    /**
     * Fetch the current/next active trip for this shuttle driver.
     */
    suspend fun getActiveTrip(driverId: Long): ShuttleDriverActiveTripResponse =
        safeApiCall { api.getActiveTrip(driverId) }
            .withFallbackMessage("No active trip assigned. Check your schedule.")
            .getOrThrow()

    /**
     * Fetch all booked students for a specific trip.
     */
    suspend fun getBookedStudents(tripId: Long): List<BoardedStudentResponse> =
        safeApiCall<List<BoardedStudentResponse>>(onEmptyBody = { emptyList() }) {
            api.getBookedStudents(tripId)
        }.getOrThrow()

    /**
     * Mark a student as boarded — updates boarding_log.boarded_at.
     */
    suspend fun markAsBoarded(bookingId: Long): MarkAsBoardedResponse =
        safeApiCall<MarkAsBoardedResponse>(
            onEmptyBody = { MarkAsBoardedResponse(true, "Marked as boarded", null) }
        ) {
            api.markAsBoarded(MarkAsBoardedRequest(bookingId))
        }.getOrThrow()
}