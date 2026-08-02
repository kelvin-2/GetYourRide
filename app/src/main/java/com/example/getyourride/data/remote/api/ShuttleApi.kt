package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.ShuttleBookingSummaryResponse
import com.example.getyourride.data.remote.dto.ShuttleStopResponse
import com.example.getyourride.data.remote.dto.ShuttleTimeSlotResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ShuttleApi {
    @GET("api/shuttle-stops")
    suspend fun getAllStops(): List<ShuttleStopResponse>

    @GET("api/shuttle-stops/time-slots")
    suspend fun getAllTimeSlots(): List<ShuttleTimeSlotResponse>

    // POST /api/shuttle/book/{tripId} — tripId must be a real Trip.tripId,
    // NOT a slotId. Resolve the trip first via ShuttleRepository.findAvailableTrip().
    @POST("api/shuttle/book/{tripId}")
    suspend fun bookShuttle(@Path("tripId") tripId: Long): Response<ShuttleBookingSummaryResponse>
}