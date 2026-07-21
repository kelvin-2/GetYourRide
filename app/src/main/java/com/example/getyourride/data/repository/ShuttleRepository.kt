package com.example.getyourride.data.repository

import com.example.getyourride.ui.screens.shuttle.RecentTrip
import com.example.getyourride.ui.screens.shuttle.UpcomingShuttle
import kotlinx.coroutines.delay

/**
 * MOCK repository. There's no real shuttle endpoint on the Spring Boot backend
 * yet (only /trips is wired up via NetworkModule.tripApi + TripRepository).
 * This fakes a network round trip with a delay so the loading state in
 * ShuttleViewModel actually shows on screen, then returns hardcoded data.
 *
 * TODO: when a real ShuttleController + endpoint exists, replace the body of
 * fetchShuttleHomeData() with a real Retrofit call (same shape as
 * TripRepository). ShuttleViewModel's public API doesn't need to change.
 */
class ShuttleRepository {

    suspend fun fetchShuttleHomeData(): ShuttleHomeData {
        delay(900) // simulated network latency

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
        delay(300)
        return listOf(
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

data class ShuttleHomeData(
    val upcomingShuttles: List<UpcomingShuttle>,
    val recentTrips: List<RecentTrip>
)