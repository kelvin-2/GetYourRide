package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.TripApi
import com.example.getyourride.data.remote.dto.ShuttleStopResponse
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.api.ShuttleApi
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.screens.shuttle.RecentTrip
import com.example.getyourride.ui.screens.shuttle.UpcomingShuttle
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Repository for shuttle data.
 */
class ShuttleRepository(
    private val api: ShuttleApi,
    private val tripApi: TripApi
) {

    suspend fun fetchShuttleHomeData(): ShuttleHomeData {
        val response = tripApi.getMyTrips()
        
        if (!response.isSuccessful) {
            throw Exception("Failed to fetch trips: ${response.message()}")
        }

        val allTrips = response.body() ?: emptyList()
        
        // Filter for SHUTTLE trips only
        val shuttleTrips = allTrips.filter { it.tripType.equals("SHUTTLE", ignoreCase = true) }

        // 1. Upcoming = SCHEDULED or ACTIVE
        val upcoming = shuttleTrips
            .filter { it.status.equals("SCHEDULED", ignoreCase = true) || it.status.equals("ACTIVE", ignoreCase = true) }
            .map { it.toUpcomingShuttle() }

        // 2. Recent = COMPLETED (take last 5)
        val recent = shuttleTrips
            .filter { it.status.equals("COMPLETED", ignoreCase = true) }
            .sortedByDescending { it.departureTime }
            .take(5)
            .map { it.toRecentTrip() }

        return ShuttleHomeData(upcoming, recent)
    }

    private fun TripResponse.toUpcomingShuttle(): UpcomingShuttle {
        val dateTime = try {
            LocalDateTime.parse(departureTime)
        } catch (e: Exception) {
            null
        }

        return UpcomingShuttle(
            from = departureStop,
            to = destinationStop,
            status = status.lowercase().replaceFirstChar { it.uppercase() },
            time = dateTime?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: departureTime.takeLast(8),
            date = dateTime?.format(DateTimeFormatter.ofPattern("EEE, dd MMM")) ?: "Upcoming",
            seat = "Any" // Backend doesn't return specific seat numbers yet
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
            // Fallback to predetermined stops if network fails
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
                ShuttleTimeSlot(departs = it.departs, period = it.period)
            }
        } catch (e: Exception) {
            // Fallback
            listOf(
                ShuttleTimeSlot("08:00 AM", "Morning"),
                ShuttleTimeSlot("08:30 AM", "Morning"),
                ShuttleTimeSlot("09:00 AM", "Morning"),
                ShuttleTimeSlot("09:30 AM", "Morning"),
                ShuttleTimeSlot("10:00 AM", "Morning"),
                ShuttleTimeSlot("10:30 AM", "Morning"),
                ShuttleTimeSlot("12:30 PM", "Afternoon"),
                ShuttleTimeSlot("14:30 PM", "Afternoon")
            )
        }
    }
}

data class ShuttleHomeData(
    val upcomingShuttles: List<UpcomingShuttle>,
    val recentTrips: List<RecentTrip>
)
