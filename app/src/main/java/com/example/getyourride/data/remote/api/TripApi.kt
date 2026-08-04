package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.TripStopRequest
import com.example.getyourride.data.remote.dto.UpdateTripStatusRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface TripApi {

    // GET /api/trips — all trips
    @GET("api/trips")
    suspend fun getAllTrips(): Response<List<TripResponse>>

    // GET /api/trips/status/{status} — e.g. "SCHEDULED"
    @GET("api/trips/status/{status}")
    suspend fun getTripsByStatus(@Path("status") status: String): Response<List<TripResponse>>

    // GET /api/trips/search?depLat=...&depLng=...&destLat=...&destLng=...
    @GET("api/trips/search")
    suspend fun searchTrips(
        @Query("depLat") departureLat: Double,
        @Query("depLng") departureLng: Double,
        @Query("destLat") destinationLat: Double,
        @Query("destLng") destinationLng: Double
    ): Response<List<TripResponse>>

    @GET("api/trips/{id}")
    suspend fun getTripById(@Path("id") tripId: Long): Response<TripResponse>

    @PATCH("api/trips/{id}/cancel")
    suspend fun cancelTrip(@Path("id") tripId: Long): Response<TripResponse>

    /**
     * GET /api/trips/my-trips — Fetch all trips posted by the logged-in driver.
     * Backend reads driver_id from the JWT token.
     * Returns trips in all statuses (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED).
     */
    @GET("api/trips/my-trips")
    suspend fun getMyTrips(): Response<List<TripResponse>>

    @POST("api/trips/{tripId}/book")
    suspend fun bookCarpool(
        @Path("tripId") tripId: Long,
        @Body request: BookCarpoolRequest
    ): Response<TripResponse>

    /**
     * POST /api/trips/offer — Student driver posts a new carpool ride.
     * Backend reads driver_id from the JWT token.
     * Simpler flow: string date/time, farePerSeat, no intermediate stops.
     */
    @POST("api/trips/offer")
    suspend fun offerRide(
        @Body request: OfferRideRequest
    ): Response<OfferRideResponse>

    /**
     * POST /api/trips — Create a full trip (shuttle or student-driver).
     * Backend endpoint: TripController#createTrip.
     * Supports intermediate stops and a structured departureTime/price,
     * unlike the simpler /api/trips/offer flow above.
     */
    @POST("api/trips")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<TripResponse>
}

/**
 * Request body for offering a ride (POST /api/trips/offer).
 */
data class OfferRideRequest(
    val pickupLocation: String,
    val destination: String,
    val rideDate: String,        // "yyyy-MM-dd"
    val rideTime: String,        // "HH:mm"
    val availableSeats: Int,
    val farePerSeat: Double,
    val pickupLat: Double? = null,
    val pickupLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null
)

/**
 * Response from offering a ride.
 */
data class OfferRideResponse(
    val tripId: Long,
    val message: String
)

/**
 * Request body for creating a full trip (POST /api/trips).
 * Mirrors backend CreateTripRequest exactly — field names and nullability matter
 * for JSON deserialization on the Spring side (tripType/departureStop/destinationStop
 * and departureTime/availableSeats/price are @NotBlank / @NotNull server-side).
 *
 * departureTime must be an ISO-8601 string (e.g. "2026-08-15T14:30:00") to match
 * java.time.LocalDateTime on the backend.
 * price is sent as a string to preserve precision when mapped to BigDecimal server-side.
 */
data class CreateTripRequest(
    val tripType: String,              // "SHUTTLE" or "STUDENT_DRIVER"
    val departureStop: String,
    val destinationStop: String,
    val departureLat: Double? = null,
    val departureLng: Double? = null,
    val destinationLat: Double? = null,
    val destinationLng: Double? = null,
    val departureTime: String,         // ISO-8601, e.g. "2026-08-15T14:30:00"
    val availableSeats: Int,
    val price: String,                 // decimal as string, e.g. "25.00"
    val stops: List<TripStopRequest>? = null
)