package com.example.getyourride.data.remote.dto

/**
 * Request body for POST /api/ratings/rate.
 * Backend: TripReviewController#rateTrip — @Valid @RequestBody TripRatingRequest.
 * studentEmail is NOT sent here — the controller reads it itself from
 * authentication.getName() via the JWT (same as every other endpoint).
 *
 * tags is new — mirrors the `List<String> tags` field being added to the
 * backend's TripRatingRequest.java alongside this. Until that lands, the
 * backend will just ignore this field (Jackson drops unknown JSON props by
 * default), so this is safe to ship ahead of the backend change.
 */
data class TripRatingRequest(
    val bookingId: Long,
    val rating: Int,      // 1–5
    val review: String,
    val tags: List<String> = emptyList()
)

/** Response from POST /api/ratings/rate. */
data class TripReviewResponse(
    val id: Long,
    val bookingId: Long,
    val rating: Int,
    val review: String,
    val tags: List<String> = emptyList()
)

/**
 * Response from GET /api/ratings/trip/{tripId}.
 * One entry per student who rated the trip. Field names match the backend
 * TripReviewDetailResponse. reviewDate is an ISO-8601 string (e.g. "2026-09-23T10:37:49").
 */
data class TripReviewDetailResponse(
    val reviewId: Long,
    val bookingId: Long?,
    val studentName: String?,
    val rating: Int,
    val review: String? = null,
    val tags: List<String> = emptyList(),
    val reviewDate: String? = null
)