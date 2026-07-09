package com.example.getyourride.viewmodel

import android.util.Log.e
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

sealed interface TripsUiState {
    object Loading : TripsUiState
    data class Success(val trips: List<TripResponse>) : TripsUiState
    data class Error(val message: String) : TripsUiState
}

class RideViewModel(private val repository: TripRepository) : ViewModel() {

    var uiState: TripsUiState by mutableStateOf(TripsUiState.Loading)
        private set

    // Tracks whether we're currently showing a filtered search result vs. the
    // default "all available trips" list — lets onRetry() know which to redo.
    private var lastSearch: SearchParams? = null

    private data class SearchParams(
        val pickupLat: Double,
        val pickupLng: Double,
        val destinationLat: Double,
        val destinationLng: Double
    )

    // NOTE: no init { loadAvailableTrips() } here on purpose.
    //
    // This ViewModel is constructed at the top of MainActivity's setContent,
    // i.e. at app launch — before the student has logged in and before any
    // auth token exists in UserSession. If this fired eagerly in init, it
    // hit the trips endpoints with no Authorization header and the backend
    // correctly returned 403, leaving uiState stuck in Error/Loading.
    //
    // Instead, CarpoolHomeScreen's own LaunchedEffect(Unit) in MainActivity
    // calls loadAvailableTrips() every time the student actually enters the
    // Home screen (post-login), which is the only time this should fire.

    // getting available trips using the status code
    fun loadAvailableTrips() {
        lastSearch = null
        viewModelScope.launch {
            uiState = TripsUiState.Loading
            repository.getAvailableTrips()
                .onSuccess { trips -> uiState = TripsUiState.Success(trips) }
                .onFailure { e -> uiState = TripsUiState.Error(e.message ?: "Something went wrong") }
        }
    }

    // gets all trips no filter

    /**
     * Called when the student presses "Search Rides" with a resolved
     * pickup + destination. Filters AvailableRidesSection down to trips
     * matching that route instead of showing every scheduled trip.
     */
    fun searchTrips(
        pickupLat: Double,
        pickupLng: Double,
        destinationLat: Double,
        destinationLng: Double
    ) {
        lastSearch = SearchParams(pickupLat, pickupLng, destinationLat, destinationLng)
        viewModelScope.launch {
            uiState = TripsUiState.Loading
            repository.searchTrips(pickupLat, pickupLng, destinationLat, destinationLng)
                .onSuccess { trips -> uiState = TripsUiState.Success(trips) }
                .onFailure { e -> uiState = TripsUiState.Error(e.message ?: "Couldn't find rides for that route") }
        }
    }

    /** Retry whichever was last active — a plain reload or the last search. */
    fun retry() {
        val search = lastSearch
        if (search != null) {
            searchTrips(search.pickupLat, search.pickupLng, search.destinationLat, search.destinationLng)
        } else {
            loadAvailableTrips()
        }
    }
}