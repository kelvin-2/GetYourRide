package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.ShuttleStopResponse
import com.example.getyourride.data.remote.dto.ShuttleTimeSlotResponse
import retrofit2.http.GET

interface ShuttleApi {
    @GET("api/shuttle-stops")
    suspend fun getAllStops(): List<ShuttleStopResponse>

    @GET("api/shuttle-stops/time-slots")
    suspend fun getAllTimeSlots(): List<ShuttleTimeSlotResponse>
}
