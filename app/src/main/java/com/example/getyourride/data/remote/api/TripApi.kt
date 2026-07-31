package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripResponse
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

    // GET /api/trips/search?pickupLat=...&pickupLng=...&destinationLat=...&destinationLng=...
    // Backend not built yet — frontend sends the query now so the endpoint just needs
    // to be dropped in later with matching param names.
    @GET("api/trips/search")
    suspend fun searchTrips(
        @Query("pickupLat") pickupLat: Double,
        @Query("pickupLng") pickupLng: Double,
        @Query("destinationLat") destinationLat: Double,
        @Query("destinationLng") destinationLng: Double
    ): Response<List<TripResponse>>

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
     */
    @POST("api/trips/offer")
    suspend fun offerRide(
        @Body request: OfferRideRequest
    ): Response<OfferRideResponse>
}

/**
 * Request body for offering a ride.
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