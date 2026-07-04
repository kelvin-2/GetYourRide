package com.example.getyourride.data.remote.api
import com.example.getyourride.data.remote.dto.UpdateTripStatusRequest

import com.example.getyourride.data.remote.dto.TripResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface TripApi {

    // GET /api/trips — all trips
    @GET("api/trips")
    suspend fun getAllTrips(): Response<List<TripResponse>>

    // GET /api/trips/status/{status} — e.g. "SCHEDULED"
    @GET("api/trips/status/{status}")
    suspend fun getTripsByStatus(@Path("status") status: String): Response<List<TripResponse>>

    @PATCH("api/trips/{id}/cancel")
    suspend fun cancelTrip(@Path("id") tripId: Long): Response<TripResponse>
}