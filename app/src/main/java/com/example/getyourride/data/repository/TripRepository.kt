package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.UpdateTripStatusRequest
import com.example.getyourride.data.remote.dto.BookCarpoolRequest
class TripRepository(private val api: TripApi) {

    suspend fun getAvailableTrips(): Result<List<TripResponse>> {
        return try {
            val response = api.getTripsByStatus("SCHEDULED")
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load trips: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrips(): Result<List<TripResponse>> {
        return try {
            val response = api.getAllTrips()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to get trips: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchTrips(
        pickupLat: Double,
        pickupLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ): Result<List<TripResponse>> {
        return try {
            val response = api.searchTrips(pickupLat, pickupLng, destinationLat, destinationLng)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to search trips: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun cancelTrip(tripId: Long): Result<TripResponse> {
        return try {
            val response = api.cancelTrip(tripId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to cancel trip: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun bookCarpool(tripId: Long, request: BookCarpoolRequest): Result<TripResponse> {
        return try {
            val response = api.bookCarpool(tripId, request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to book trip: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


}