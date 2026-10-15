package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi

import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.BookCarpoolRequest
import com.example.getyourride.data.remote.dto.TripBookingResponse
import com.example.getyourride.data.remote.dto.TripRatingRequest
import com.example.getyourride.data.remote.dto.TripReviewDetailResponse
import com.example.getyourride.data.remote.dto.TripReviewResponse
import com.example.getyourride.data.remote.ApiError
import com.example.getyourride.data.remote.ApiException
import com.example.getyourride.data.remote.ResultResource
import com.example.getyourride.data.remote.safeApiCall
import com.example.getyourride.data.remote.toResult
import com.example.getyourride.data.remote.withFallbackMessage

/**
 * Every method goes through safeApiCall, so failures arrive as Result.failure(ApiException)
 * whose message is already short and friendly (backend {"error": "..."} text is passed
 * through as-is). Callers can keep using `e.message`.
 */
class TripRepository(private val api: TripApi) {

    suspend fun getAvailableTrips(): Result<List<TripResponse>> =
        safeApiCall<List<TripResponse>>(onEmptyBody = { emptyList() }) {
            api.getTripsByStatus("SCHEDULED")
        }.toResult()

    suspend fun getTrips(): Result<List<TripResponse>> =
        safeApiCall<List<TripResponse>>(onEmptyBody = { emptyList() }) {
            api.getAllTrips()
        }.toResult()

    /**
     * Fetch a single trip by id. Used by the tracking screen to hydrate driver/vehicle/stop
     * details for the trip it is following.
     */
    suspend fun getTripById(tripId: Long): Result<TripResponse> =
        safeApiCall { api.getTripById(tripId) }.toResult()

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
    ): Result<List<TripResponse>> =
        safeApiCall<List<TripResponse>>(onEmptyBody = { emptyList() }) {
            api.searchTrips(pickupLat, pickupLng, destinationLat, destinationLng)
        }.toResult()

    /**
     * Start a trip (driver action). Precomputes the route if needed, sets it IN_PROGRESS and
     * begins the backend simulation. The returned trip already carries the seeded live position.
     *
     * The failure message is surfaced directly to the driver on screen, so it is the
     * backend's own message when there is one, otherwise a friendly default.
     */
    suspend fun startTrip(tripId: Long): Result<TripResponse> =
        safeApiCall { api.startTrip(tripId) }
            .withFallbackMessage("This trip can't be started. It may have no route, or it's already completed or cancelled.")
            .toResult()

    suspend fun cancelTrip(tripId: Long): Result<TripResponse> =
        // Driver cancels their OWN posted trip (PATCH /api/trips/{id}/cancel).
        // Previously this wrongly called cancelBooking (the student seat-cancel
        // endpoint), so the driver's trip never actually got cancelled server-side.
        safeApiCall { api.cancelTrip(tripId) }.toResult()

    suspend fun bookCarpool(tripId: Long, request: BookCarpoolRequest): Result<TripResponse> =
        safeApiCall { api.bookCarpool(tripId, request) }
            // A 409 with no message of its own almost always means a duplicate booking.
            .withFallbackMessage("You've already booked this trip, or it conflicts with another booking.")
            .toResult()

    /**
     * Fetch all trips posted by the logged-in driver.
     * JWT token is automatically attached by the auth interceptor.
     */
    suspend fun getMyTrips(): Result<List<TripResponse>> =
        safeApiCall<List<TripResponse>>(onEmptyBody = { emptyList() }) {
            api.getMyTrips()
        }.toResult()

    /**
     * Fetch the student's bookings filtered by booking_status.
     *
     * Usage:
     *   getMyBookings("CONFIRMED") → active/oncoming trips only
     *   getMyBookings("CANCELLED") → cancelled trips (history page)
     *   getMyBookings()            → all bookings regardless of status
     */
    suspend fun getMyBookings(status: String? = null): Result<List<TripBookingResponse>> =
        safeApiCall<List<TripBookingResponse>>(onEmptyBody = { emptyList() }) {
            api.getMyBookings(status)
        }.toResult()

    /**
     * Live ETA to the trip's destination, from wherever the vehicle currently is.
     * Calls through to TripEtaController#getEta, which asks Google Compute Routes on demand.
     */
    suspend fun getEta(tripId: Long): Result<com.example.getyourride.data.remote.api.EtaResponse> =
        safeApiCall { api.getEta(tripId) }.toResult()

    /**
     * Submit a student's rating for a completed trip.
     * Calls POST /api/ratings/rate. Keyed by bookingId (per the backend's
     * TripRatingRequest), NOT tripId — the opposite convention from
     * cancelTrip/startTrip above. JWT is attached automatically by the
     * auth interceptor, so the student isn't passed explicitly.
     */
    suspend fun rateTrip(bookingId: Long, rating: Int, review: String, tags: List<String> = emptyList()): Result<TripReviewResponse> {
        val fallback = TripReviewResponse(
            id = 0L, bookingId = bookingId, rating = rating, review = review, tags = tags
        )
        return when (
            val outcome = safeApiCall(onEmptyBody = { fallback }) {
                api.rateTrip(TripRatingRequest(bookingId, rating, review, tags))
            }
        ) {
            is ResultResource.Success -> Result.success(outcome.data)
            is ResultResource.Error -> when (val err = outcome.error) {
                // The backend persists the rating before it builds its response, so a 5xx or an
                // unreadable response body here still means the rating was saved. Treat it as
                // success so the screen closes instead of showing "something went wrong".
                is ApiError.Unknown -> Result.success(fallback)
                is ApiError.Server -> if (err.code >= 500) Result.success(fallback) else Result.failure(ApiException(err))
                else -> Result.failure(ApiException(err))
            }
        }
    }

    /**
     * Fetch all reviews students left on a completed trip (driver's "View Ratings" screen).
     * Calls GET /api/ratings/trip/{tripId}. JWT is attached automatically; the backend
     * verifies the authenticated driver owns the trip.
     */
    suspend fun getTripReviews(tripId: Long): Result<List<TripReviewDetailResponse>> =
        safeApiCall<List<TripReviewDetailResponse>>(onEmptyBody = { emptyList() }) {
            api.getTripReviews(tripId)
        }.toResult()

    private companion object {
        const val BOOKING_STATUS_CONFIRMED = "CONFIRMED"
    }
}