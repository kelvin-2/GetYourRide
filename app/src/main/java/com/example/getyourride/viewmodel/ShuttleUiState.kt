package com.example.getyourride.viewmodel

import com.example.getyourride.ui.screens.Shuttle.RecentTrip
import com.example.getyourride.ui.screens.Shuttle.UpcomingShuttle

/**
 * Shuttle-side equivalent of TripsUiState (used by RideViewModel/CarpoolHomeScreen).
 * Kept as its own sealed interface because shuttles (fixed routes, NSFAS students)
 * are a totally different domain from carpools (on-demand, driver-matched) —
 * even though the Loading/Success/Error shape is identical.
 */
sealed interface ShuttleUiState {
    object Loading : ShuttleUiState
    data class Success(
        val upcomingShuttles: List<UpcomingShuttle>,
        val recentTrips: List<RecentTrip>
    ) : ShuttleUiState
    data class Error(val message: String) : ShuttleUiState
}