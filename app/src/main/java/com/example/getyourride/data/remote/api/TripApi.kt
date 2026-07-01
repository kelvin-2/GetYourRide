package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.TripResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface TripApi {

    // GET /api/trips — all trips
    @GET("api/trips")
    suspend fun getAllTrips(): Response<List<TripResponse>>

    // GET /api/trips/status/{status} — e.g. "SCHEDULED"
    @GET("api/trips/status/{status}")
    suspend fun getTripsByStatus(@Path("status") status: String): Response<List<TripResponse>>
}