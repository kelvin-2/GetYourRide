package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi

import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripBookingResponse

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

    /**
     * Fetch a single trip by id. Used by the tracking screen to hydrate driver/vehicle/stop
     * details for the trip it is following.
     */
    suspend fun getTripById(tripId: Long): Result<TripResponse> {
        return try {
            val response = api.getTripById(tripId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Failed to load trip $tripId: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolve the trip the logged-in student should currently be tracking, or `null` when
     * there is nothing to track.
     *
     * "Trackable" means a CONFIRMED booking whose trip is still SCHEDULED or IN_PROGRESS —
     * a COMPLETED or CANCELLED trip has no live position to follow. When several qualify,
     * the soonest departure wins.
     *
     * `Result.success(null)` is a legitimate outcome (no active rides) and must NOT be
     * treated as an error or substituted with sample data by callers.
     */
    suspend fun getActiveTrackableTrip(): Result<TripResponse?> {
        return getMyBookings(BOOKING_STATUS_CONFIRMED).map { bookings ->
            bookings
                .map { it.trip }
                .filter { it.status.uppercase() in TRACKABLE_TRIP_STATUSES }
                .minByOrNull { it.departureTime }
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
            val response = api.cancelBooking(tripId)
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

    /**
     * Fetch all trips posted by the logged-in driver.
     * JWT token is automatically attached by the auth interceptor.
     */
    suspend fun getMyTrips(): Result<List<TripResponse>> {
        return try {
            val response = api.getMyTrips()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load your trips: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetch the student's bookings filtered by booking_status.
     *
     * Usage:
     *   getMyBookings("CONFIRMED") → active/oncoming trips only
     *   getMyBookings("CANCELLED") → cancelled trips (history page)
     *   getMyBookings()            → all bookings regardless of status
     */
    suspend fun getMyBookings(status: String? = null): Result<List<TripBookingResponse>> {
        return try {
            val response = api.getMyBookings(status)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load bookings: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val BOOKING_STATUS_CONFIRMED = "CONFIRMED"

        /** Trip statuses that still have a live position worth following on the map. */
        val TRACKABLE_TRIP_STATUSES = setOf("SCHEDULED", "IN_PROGRESS", "ARRIVED")
    }
}
