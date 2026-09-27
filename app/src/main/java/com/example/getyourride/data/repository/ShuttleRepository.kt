package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.ShuttleApi
import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.ShuttleBookingSummaryResponse
import com.example.getyourride.data.remote.dto.ShuttleStopResponse
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.dto.TripResponse
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
 */
class ShuttleRepository(
    private val api: ShuttleApi,
    private val tripApi: TripApi
) {

    suspend fun fetchShuttleHomeData(): ShuttleHomeData {
        // Upcoming trips come from the student's BOOKINGS (not raw trips) so each one
        // carries its bookingId — that's what the shuttle QR needs for boarding.
        val bookingsResponse = tripApi.getMyBookings(null)
        if (!bookingsResponse.isSuccessful) {
            throw Exception("Failed to fetch bookings: ${bookingsResponse.message()}")
        }
        val allBookings = bookingsResponse.body() ?: emptyList()

        val shuttleBookings = allBookings.filter {
            it.trip.tripType.equals("SHUTTLE", ignoreCase = true)
        }

        // 1. Upcoming = SCHEDULED, CONFIRMED, or ACTIVE trip status.
        //    CONFIRMED must be included — a trip becomes CONFIRMED immediately after booking,
        //    so leaving it out means a just-booked trip never shows up here.
        val upcoming = shuttleBookings
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
            listOf(
                ShuttleTimeSlot(0, "08:00 AM", "08:30 AM", "Morning"),
                ShuttleTimeSlot(0, "08:30 AM", "09:00 AM", "Morning"),
                ShuttleTimeSlot(0, "09:00 AM", "09:30 AM", "Morning"),
                ShuttleTimeSlot(0, "09:30 AM", "10:00 AM", "Morning"),
                ShuttleTimeSlot(0, "10:00 AM", "10:30 AM", "Morning"),
                ShuttleTimeSlot(0, "10:30 AM", "11:00 AM", "Morning"),
                ShuttleTimeSlot(0, "12:30 PM", "13:15 PM", "Afternoon"),
                ShuttleTimeSlot(0, "14:30 PM", "15:15 PM", "Afternoon")
            )
        }
    }

    suspend fun findAvailableTrip(pickupStop: String, slot: ShuttleTimeSlot): TripResponse? {
        val response = tripApi.getAllTrips()
        if (!response.isSuccessful) {
            throw Exception("Failed to search trips: ${response.message()}")
        }
        val trips = response.body() ?: emptyList()
        val expectedSlotTime = "${slot.departs.take(5)} - ${slot.arrives.take(5)}"

        return trips.firstOrNull { trip ->
            trip.tripType.equals("SHUTTLE", ignoreCase = true) &&
                    (trip.status.equals("SCHEDULED", ignoreCase = true) ||
                            trip.status.equals("CONFIRMED", ignoreCase = true)) &&
                    trip.availableSeats > 0 &&
                    trip.departureStop.equals(pickupStop, ignoreCase = true) &&
                    trip.slotTime == expectedSlotTime
        }
    }

    suspend fun bookShuttle(tripId: Long): ShuttleBookingSummaryResponse {
        val response = api.bookShuttle(tripId)
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Booking failed: ${response.message()}")
        }
        return response.body() ?: throw Exception("Empty booking response from server")
    }
}

data class ShuttleHomeData(
    val upcomingShuttles: List<UpcomingShuttle>,
    val recentTrips: List<RecentTrip>
)