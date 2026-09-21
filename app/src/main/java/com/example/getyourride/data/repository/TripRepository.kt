package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi

import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripBookingResponse
import com.example.getyourride.data.remote.dto.TripRatingRequest
import com.example.getyourride.data.remote.dto.TripReviewResponse

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
     * "Trackable" means a CONFIRMED booking whose trip is IN_PROGRESS — SCHEDULED trips are
     * deliberately excluded: nothing is moving yet, so showing them here produced a screen
     * with a blank map and a permanent "Waiting for driver…" state. That's not useful and
     * reads as broken; the student should see "No rides currently available to track" until
     * the trip actually starts, then land straight on the live vehicle.
     * COMPLETED/CANCELLED trips have no live position either and are excluded for the same
     * reason. When several trips are IN_PROGRESS, the soonest departure wins.
     *
     * `Result.success(null)` is a legitimate outcome (no active rides) and must NOT be
     * treated as an error or substituted with sample data by callers.
     */
    suspend fun getActiveTrackableTrip(): Result<TripResponse?> {
        return getMyBookings(BOOKING_STATUS_CONFIRMED).map { bookings ->
            bookings
                .map { it.trip }
                .filter { it.status.equals("IN_PROGRESS", ignoreCase = true) }
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

    /**
     * Start a trip (driver action). Precomputes the route if needed, sets it IN_PROGRESS and
     * begins the backend simulation. The returned trip already carries the seeded live position.
     *
     * Error messages are written for a human reader rather than exposing an HTTP code, because
     * this result is surfaced directly to the driver on screen.
     */
    suspend fun startTrip(tripId: Long): Result<TripResponse> {
        return try {
            val response = api.startTrip(tripId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception(startTripErrorMessage(response.code())))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Couldn't reach the server to start the trip. Check your connection and try again."))
        }
    }

    private fun startTripErrorMessage(code: Int): String = when (code) {
        400 -> "This trip can't be started. It may have no route, or it's already completed or cancelled."
        401, 403 -> "You're not allowed to start this trip. Please sign in again."
        404 -> "This trip no longer exists."
        else -> "Couldn't start the trip (error $code). Please try again."
    }

    suspend fun cancelTrip(tripId: Long): Result<TripResponse> {
        return try {
            // Driver cancels their OWN posted trip (PATCH /api/trips/{id}/cancel).
            // Previously this wrongly called cancelBooking (the student seat-cancel
            // endpoint), so the driver's trip never actually got cancelled server-side.
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

    /**
     * Live ETA to the trip's destination, from wherever the vehicle currently is.
     * Calls through to TripEtaController#getEta, which asks Google Compute Routes on demand.
     */
    suspend fun getEta(tripId: Long): Result<com.example.getyourride.data.remote.api.EtaResponse> {
        return try {
            val response = api.getEta(tripId)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                Result.success(body)
            } else {
                Result.failure(Exception("Failed to load ETA for trip $tripId: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Submit a student's rating for a completed trip.
     * Calls POST /api/ratings/rate. Keyed by bookingId (per the backend's
     * TripRatingRequest), NOT tripId — the opposite convention from
     * cancelTrip/startTrip above. JWT is attached automatically by the
     * auth interceptor, so the student isn't passed explicitly.
     */
    suspend fun rateTrip(bookingId: Long, rating: Int, review: String, tags: List<String> = emptyList()): Result<TripReviewResponse> {
        return try {
            val response = api.rateTrip(TripRatingRequest(bookingId, rating, review, tags))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to submit rating: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private companion object {
        const val BOOKING_STATUS_CONFIRMED = "CONFIRMED"
    }
}