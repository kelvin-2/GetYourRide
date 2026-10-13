package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.ResultResource
import com.example.getyourride.data.remote.api.ShuttleApi
import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.ShuttleBookingSummaryResponse
import com.example.getyourride.data.remote.dto.ShuttleStopResponse
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.dto.TripBookingResponse
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.getOrThrow
import com.example.getyourride.data.remote.safeApiCall
import com.example.getyourride.data.remote.safeCall
import com.example.getyourride.data.remote.withFallbackMessage
import com.example.getyourride.ui.screens.shuttle.RecentTrip
import com.example.getyourride.ui.screens.shuttle.UpcomingShuttle
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository for shuttle data.
 *
 * java.time (LocalDateTime, DateTimeFormatter) works on minSdk 24 here because core library
 * desugaring is enabled in app/build.gradle.kts (isCoreLibraryDesugaringEnabled = true +
 * desugar_jdk_libs). No @RequiresApi guards needed.
 *
 * API failures are thrown as ApiException (see data/remote/ApiError.kt); its message is
 * already user-friendly, so callers can show `e.message` or `e.userFacingMessage()`.
 */
class ShuttleRepository(
    private val api: ShuttleApi,
    private val tripApi: TripApi
) {

    suspend fun fetchShuttleHomeData(): ShuttleHomeData {
        // Upcoming trips come from the student's BOOKINGS (not raw trips) so each one
        // carries its bookingId — that's what the shuttle QR needs for boarding.
        val allBookings = safeApiCall<List<TripBookingResponse>>(onEmptyBody = { emptyList() }) {
            tripApi.getMyBookings(null)
        }.getOrThrow()

        val shuttleBookings = allBookings.filter {
            it.trip.tripType.equals("SHUTTLE", ignoreCase = true)
        }

        // 1. Upcoming = SCHEDULED, CONFIRMED, or ACTIVE trip status.
        //    CONFIRMED must be included — a trip becomes CONFIRMED immediately after booking,
        //    so leaving it out means a just-booked trip never shows up here.
        val upcoming = shuttleBookings
            // A cancelled booking keeps a CONFIRMED trip status, so check the booking too.
            .filter {
                !it.bookingStatus.equals("CANCELLED", ignoreCase = true) &&
                        !it.bookingStatus.equals("NO_SHOW", ignoreCase = true)
            }
            .filter {
                val s = it.trip.status
                s.equals("SCHEDULED", ignoreCase = true) ||
                        s.equals("CONFIRMED", ignoreCase = true) ||
                        s.equals("ACTIVE", ignoreCase = true)
            }
            .map { it.toUpcomingShuttle() }

        // 2. Recent = COMPLETED trips (take last 5).
        val recent = shuttleBookings
            .filter { it.trip.status.equals("COMPLETED", ignoreCase = true) }
            .sortedByDescending { it.trip.departureTime }
            .take(5)
            .map { it.trip.toRecentTrip() }

        return ShuttleHomeData(upcoming, recent)
    }

    private fun com.example.getyourride.data.remote.dto.TripBookingResponse.toUpcomingShuttle(): UpcomingShuttle {
        val t = this.trip
        val dateTime = try {
            LocalDateTime.parse(t.departureTime)
        } catch (e: Exception) {
            null
        }

        return UpcomingShuttle(
            tripId = t.tripId.toString(),
            from = t.departureStop,
            to = t.destinationStop,
            status = t.status.lowercase().replaceFirstChar { it.uppercase() },
            time = dateTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: t.departureTime.takeLast(8),
            date = dateTime?.format(DateTimeFormatter.ofPattern("EEE, dd MMM")) ?: "Upcoming",
            seat = "Any", // Backend doesn't return specific seat numbers yet
            driverName = t.driverName,
            plateNumber = t.registrationNumber,
            vehicleModel = t.vehicleModel,
            bookingId = this.bookingId
        )
    }

    private fun TripResponse.toRecentTrip(): RecentTrip {
        val dateTime = try {
            LocalDateTime.parse(departureTime)
        } catch (e: Exception) {
            null
        }

        return RecentTrip(
            from = departureStop,
            to = destinationStop,
            date = dateTime?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "Past",
            time = dateTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: ""
        )
    }

    suspend fun fetchStops(): List<ShuttleStopResponse> {
        return try {
            api.getAllStops()
        } catch (e: Exception) {
            listOf(
                ShuttleStopResponse(1, "South Campus Main Gate", "South Campus", "University Way", -34.0016, 25.6724),
                ShuttleStopResponse(2, "North Campus Main Gate", "North Campus", "Gardham Avenue", -33.9918, 25.6669),
                ShuttleStopResponse(3, "2nd Avenue Gate", "2nd Avenue Campus", "2nd Avenue", -33.9856, 25.6575),
                ShuttleStopResponse(4, "Missionvale Main Entrance", "Missionvale Campus", "Johnson Road", -33.8821, 25.5562),
                ShuttleStopResponse(13, "Forest Hill", "Forest Hill", "Garage, Morestond Flats and Stadium", null, null)
            )
        }
    }

    suspend fun fetchTimeSlots(): List<ShuttleTimeSlot> {
        return try {
            api.getAllTimeSlots().map {
                ShuttleTimeSlot(slotId = it.slotId, departs = it.departs, arrives = it.arrives, period = it.period)
            }
        } catch (e: Exception) {
            // 24-hour "HH:mm" strings. These must line up with the departure times of the
            // seeded SHUTTLE trips, since findAvailableTrip matches a slot to a trip on the
            // HH:mm of its departureTime. Mixed AM/PM strings here would silently break booking.
            listOf(
                ShuttleTimeSlot(0, "06:45", "07:15", "Morning"),
                ShuttleTimeSlot(0, "07:45", "08:15", "Morning"),
                ShuttleTimeSlot(0, "08:45", "09:15", "Morning"),
                ShuttleTimeSlot(0, "09:45", "10:15", "Morning"),
                ShuttleTimeSlot(0, "12:30", "13:00", "Afternoon"),
                ShuttleTimeSlot(0, "14:30", "15:00", "Afternoon"),
                ShuttleTimeSlot(0, "16:00", "16:30", "Afternoon"),
                ShuttleTimeSlot(0, "17:30", "18:00", "Afternoon")
            )
        }
    }

    /**
     * Same as [fetchTimeSlots] but also reports whether the built-in fallback list was used
     * because the server could not be reached, so the UI can warn the user.
     */
    suspend fun fetchTimeSlotsWithStatus(): Pair<List<ShuttleTimeSlot>, Boolean> {
        val remote = safeCall { api.getAllTimeSlots() }
        return if (remote is ResultResource.Success) {
            remote.data.map {
                ShuttleTimeSlot(slotId = it.slotId, departs = it.departs, arrives = it.arrives, period = it.period)
            } to false
        } else {
            fetchTimeSlots() to true
        }
        val trips =  response.body() ?: emptyList()

        // The selected slot's departure time, normalised to "HH:mm" (e.g. "14:30").
        // slot.departs can arrive as "14:30", "14:30:00" or "14:30 PM" depending on
        // whether the time-slots API responded or the hardcoded fallback kicked in,
        // so we strip to the first 5 chars.
        val slotHhmm = slot.departs.take(5)

        return trips.firstOrNull { trip ->
            if (!trip.tripType.equals("SHUTTLE", ignoreCase = true)) return@firstOrNull false

            val isBookable = trip.status.equals("SCHEDULED", ignoreCase = true) ||
                    trip.status.equals("CONFIRMED", ignoreCase = true)
            if (!isBookable) return@firstOrNull false

            // Shuttles come back with availableSeats == null (unlimited / seat count not
            // tracked). Only enforce a seat check when the backend actually reports a count.
            if (trip.availableSeats != null && trip.availableSeats <= 0) return@firstOrNull false

            if (!trip.departureStop.equals(pickupStop, ignoreCase = true)) return@firstOrNull false

            // Match the slot to the trip's time. Prefer the backend slotTime
            // ("HH:mm:ss - HH:mm:ss") when present; otherwise fall back to the HH:mm of
            // the ISO departureTime ("2026-10-12T14:30:00" -> "14:30").
            val tripHhmm = when {
                !trip.slotTime.isNullOrBlank() -> trip.slotTime.take(5)
                trip.departureTime.length >= 16 -> trip.departureTime.substring(11, 16)
                else -> ""
            }
            tripHhmm == slotHhmm
        }
    }

    suspend fun bookShuttle(tripId: Long): ShuttleBookingSummaryResponse {
        // Backend {"error": "..."} text (e.g. "Student already has a booking for this trip")
        // is thrown as an ApiException carrying exactly that message.
        return safeApiCall { api.bookShuttle(tripId) }
            // A 409 with no message of its own almost always means a duplicate booking.
            .withFallbackMessage("You've already booked this trip, or it conflicts with another booking.")
            .getOrThrow()
    }
}

data class ShuttleHomeData(
    val upcomingShuttles: List<UpcomingShuttle>,
    val recentTrips: List<RecentTrip>
)