package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.MarkAsBoardedRequest
import com.example.getyourride.data.remote.dto.MarkAsBoardedResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverLoginRequest
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit interface for shuttle driver endpoints.
 *
 * Shuttle drivers do NOT sign up in-app. They log in with
 * credentials created by the admin. The backend validates
 * against the `driver` table where role = 'SHUTTLE_DRIVER'.
 */
interface ShuttleDriverApi {

    /**
     * POST /api/auth/driver/login
     *
     * Validates email + password against the driver table.
     * Returns AuthResponse with type = "SHUTTLE_DRIVER".
     */
    @POST("api/auth/driver/login")
    suspend fun login(@Body request: ShuttleDriverLoginRequest): Response<AuthResponse>

    /**
     * GET /api/shuttle-driver/profile/{driverId}
     *
     * Returns the full profile: driver info, assigned vehicle(s),
     * and trip summary statistics.
     */
    @GET("api/shuttle-driver/profile/{driverId}")
    suspend fun getProfile(@Path("driverId") driverId: Long): Response<ShuttleDriverProfileResponse>

    // ── Boarding Endpoints ──────────────────────────────────────────────────

    /**
     * GET /api/shuttle-driver/{driverId}/active-trip
     *
     * Returns the current/next active trip for this driver.
     * Backend finds the trip with status SCHEDULED or IN_PROGRESS.
     */
    @GET("api/shuttle-driver/{driverId}/active-trip")
    suspend fun getActiveTrip(@Path("driverId") driverId: Long): Response<ShuttleDriverActiveTripResponse>

    /**
     * GET /api/shuttle-driver/trip/{tripId}/students
     *
     * Returns all booked students for a specific trip with their boarding status.
     */
    @GET("api/shuttle-driver/trip/{tripId}/students")
    suspend fun getBookedStudents(@Path("tripId") tripId: Long): Response<List<BoardedStudentResponse>>

    /**
     * POST /api/shuttle-driver/boarding/mark
     *
     * Marks a student as boarded — creates/updates boarding_log.boarded_at.
     */
    @POST("api/shuttle-driver/boarding/mark")
    suspend fun markAsBoarded(@Body request: MarkAsBoardedRequest): Response<MarkAsBoardedResponse>
}
