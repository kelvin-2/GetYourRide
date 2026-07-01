package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.TripResponse

class TripRepository(private val api: TripApi) {

    suspend fun getAvailableTrips(): Result<List<TripResponse>> {
        return try {
            val response = api.getTripsByStatus("Scheduled") // was "SCHEDULED" — backend uses "Scheduled"
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load trips: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}