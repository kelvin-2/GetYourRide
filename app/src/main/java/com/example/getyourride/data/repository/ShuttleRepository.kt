package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.ShuttleApi
import com.example.getyourride.ui.screens.shuttle.RecentTrip
import com.example.getyourride.ui.screens.shuttle.UpcomingShuttle
import kotlinx.coroutines.delay

/**
 * Repository for shuttle data.
 */
class ShuttleRepository(private val api: ShuttleApi) {

    suspend fun fetchShuttleHomeData(): ShuttleHomeData {
        delay(900) // simulated network latency

        // For now, home data (upcoming/recent) is still hardcoded or coming from another endpoint.
        // If there's no endpoint for this yet, we keep it mocked.
        val upcoming = listOf(
            UpcomingShuttle(
                from = "Gqeberha Bus Terminal",
                to = "NMU South Campus",
                status = "Confirmed",
                time = "07:45",
                date = "Today, 16 Jul",
                seat = "14B"
            ),
            UpcomingShuttle(
                from = "NMU North Campus",
                to = "Missionvale Campus",
                status = "Confirmed",
                time = "13:15",
                date = "Tomorrow, 17 Jul",
                seat = "09A"
            )
        )

        val recent = listOf(
            RecentTrip(from = "Missionvale Campus", to = "Gqeberha Bus Terminal", date = "14 Jul", time = "16:40"),
            RecentTrip(from = "NMU South Campus", to = "NMU North Campus", date = "12 Jul", time = "08:10"),
            RecentTrip(from = "Gqeberha Bus Terminal", to = "Missionvale Campus", date = "09 Jul", time = "15:20")
        )

        return ShuttleHomeData(upcoming, recent)
    }

    suspend fun fetchStops(): List<String> {
        return try {
            api.getAllStops().map { it.stopName }
        } catch (e: Exception) {
            // Fallback to predetermined stops if network fails
            listOf(
                "NMU South Campus",
                "NMU North Campus",
                "NMU 2nd Avenue Campus",
                "Missionvale Campus",
                "Gqeberha Bus Terminal",
                "Summerstrand North",
                "Humewood Village",
                "North Campus Main Gate",
                "South Campus Main Gate"
            )
        }
    }

    suspend fun fetchTimeSlots(): List<String> {
        return try {
            api.getAllTimeSlots().map { "${it.departs} - ${it.arrives} (${it.period})" }
        } catch (e: Exception) {
            // Fallback
            listOf(
                "08:00 AM", "08:30 AM", "09:00 AM",
                "09:30 AM", "10:00 AM", "10:30 AM"
            )
        }
    }
}

data class ShuttleHomeData(
    val upcomingShuttles: List<UpcomingShuttle>,
    val recentTrips: List<RecentTrip>
)
