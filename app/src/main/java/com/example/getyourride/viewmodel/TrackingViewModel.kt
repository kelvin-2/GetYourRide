package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.TripRepository
import com.example.getyourride.domain.model.RideStatus
import com.example.getyourride.domain.model.TripTrackingInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint

data class DriverLocationUpdate(
    val latitude: Double,
    val longitude: Double,
    val heading: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Live-tracking payload for a trip that is genuinely being tracked.
 *
 * This only ever describes a real trip. There is deliberately no default/placeholder
 * instance available to the runtime path — "nothing to track" is expressed by
 * [TrackingUiState.NoRidesAvailable], not by an empty or faked payload.
 */
data class TrackingData(
    val tripId: Long,
    val tripInfo: TripTrackingInfo,
    val driverLocation: GeoPoint? = null,
    val destinationLocation: GeoPoint? = null,
    val stops: List<GeoPoint> = emptyList(),
    // trip_stop.id for each entry in `stops`, same order, same length. Needed because
    // STOP_EVENT broadcasts carry a stopId (see RideLocationSocket doc below), not a
    // position in this list, so we need a way to translate one into the other.
    val stopIds: List<Long> = emptyList(),
    val currentStopIndex: Int = 0,
    val isConnected: Boolean = false,
    val connectionError: String? = null
)

/**
 * Exhaustive state model for the tracking screen.
 *
 * Making this a sealed hierarchy is what prevents the old failure mode: previously the
 * state was a single data class with a nullable `tripInfo`, so "still loading",
 * "nothing to track" and "backend call failed" were all indistinguishable, and the
 * screen filled the gap with hardcoded demo data.
 */
sealed interface TrackingUiState {

    /** Resolving which trip (if any) to track. */
    data object Loading : TrackingUiState

    /** Confirmed: the student has no trip with a live position to follow. */
    data object NoRidesAvailable : TrackingUiState

    /** Could not determine what to track (network/backend failure) — distinct from empty. */
    data class Error(val message: String) : TrackingUiState

    /** A real trip is being tracked. */
    data class Active(val data: TrackingData) : TrackingUiState
}

/**
 * Implement this against whichever STOMP client you already wired up
 * with FusedLocationProviderClient on the driver side.
 */
interface RideLocationSocket {
    fun connect(
        rideId: String,
        onUpdate: (DriverLocationUpdate) -> Unit,
        // Carries trip_stop.id, per the backend's StopEventDTO contract - NOT a position
        // in the stops list. TrackingViewModel resolves it against TrackingData.stopIds.
        onStopUpdate: (stopId: Long) -> Unit,
        onError: (String) -> Unit
    )
    fun disconnect()
}

/**
 * @param rideId the trip to track when the caller already knows it (deep link from
 *   "My Rides"). Pass `null` from entry points that have no specific trip in mind —
 *   e.g. the Track bottom-nav tab — and the ViewModel resolves the student's active
 *   trip from the backend, or reports [TrackingUiState.NoRidesAvailable].
 */
class TrackingViewModel(
    private val rideId: String?,
    private val socket: RideLocationSocket,
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackingUiState>(TrackingUiState.Loading)
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    private var pollingJob: Job? = null
    private var trackedTripId: Long? = null
    private var started = false

    fun startTracking() {
        if (started) return
        started = true

        viewModelScope.launch {
            _uiState.value = TrackingUiState.Loading

            val explicitTripId = rideId?.toLongOrNull()?.takeIf { it > 0 }
            val resolved: TripResponse? = if (explicitTripId != null) {
                tripRepository.getTripById(explicitTripId).fold(
                    onSuccess = { it },
                    onFailure = { e ->
                        _uiState.value = TrackingUiState.Error(
                            e.message ?: "Could not load this ride."
                        )
                        return@launch
                    }
                )
            } else {
                tripRepository.getActiveTrackableTrip().fold(
                    onSuccess = { it },
                    onFailure = { e ->
                        _uiState.value = TrackingUiState.Error(
                            e.message ?: "Could not check for active rides."
                        )
                        return@launch
                    }
                )
            }

            // An empty backend result is a real answer, not a reason to show sample data.
            if (resolved == null) {
                _uiState.value = TrackingUiState.NoRidesAvailable
                return@launch
            }

            trackedTripId = resolved.tripId
            _uiState.value = TrackingUiState.Active(resolved.toTrackingData())
            connectSocket(resolved.tripId.toString())
        }
    }

    private fun connectSocket(tripId: String) {
        socket.connect(
            rideId = tripId,
            onUpdate = { update ->
                updateActive {
                    it.copy(
                        driverLocation = GeoPoint(update.latitude, update.longitude),
                        isConnected = true,
                        connectionError = null
                    )
                }
            },
            onStopUpdate = { stopId ->
                updateActive { data ->
                    // Backend STOP_EVENTs carry trip_stop.id, so translate it into a
                    // position in `stops`. Unknown ids leave the index untouched rather
                    // than corrupting the route rendering.
                    val index = data.stopIds.indexOf(stopId)
                    if (index >= 0) data.copy(currentStopIndex = index) else data
                }
            },
            onError = { message ->
                updateActive { it.copy(isConnected = false, connectionError = message) }
                startPollingIfDisconnected()
            }
        )
    }

    /**
     * Socket is down — fall back to periodically re-reading the trip over REST so the
     * card stays accurate. This refreshes real trip data only; it never invents a position.
     */
    private fun startPollingIfDisconnected() {
        if (pollingJob?.isActive == true) return
        val tripId = trackedTripId ?: return

        pollingJob = viewModelScope.launch {
            while (!isSocketConnected()) {
                refreshTripDetails(tripId)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    private fun isSocketConnected(): Boolean =
        (_uiState.value as? TrackingUiState.Active)?.data?.isConnected == true

    private suspend fun refreshTripDetails(tripId: Long) {
        tripRepository.getTripById(tripId).fold(
            onSuccess = { trip ->
                updateActive { current ->
                    trip.toTrackingData().copy(
                        // Keep whatever live values the socket already delivered.
                        driverLocation = current.driverLocation,
                        currentStopIndex = current.currentStopIndex,
                        isConnected = current.isConnected,
                        connectionError = current.connectionError
                    )
                }
            },
            onFailure = { e ->
                updateActive {
                    it.copy(connectionError = "Failed to refresh trip details: ${e.message}")
                }
            }
        )
    }

    /** Applies [transform] only while a real trip is being tracked. */
    private fun updateActive(transform: (TrackingData) -> TrackingData) {
        _uiState.update { state ->
            if (state is TrackingUiState.Active) state.copy(data = transform(state.data)) else state
        }
    }

    fun setDestination(lat: Double, lng: Double) {
        updateActive { it.copy(destinationLocation = GeoPoint(lat, lng)) }
    }

    fun cancelRide(onCancelled: () -> Unit) {
        val tripId = trackedTripId
        if (tripId == null) {
            onCancelled()
            return
        }
        viewModelScope.launch {
            tripRepository.cancelTrip(tripId)
            socket.disconnect()
            pollingJob?.cancel()
            _uiState.value = TrackingUiState.NoRidesAvailable
            onCancelled()
        }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
        socket.disconnect()
    }

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
    }
}

/** Maps the trip DTO onto the tracking payload. Single place where trip data enters the screen. */
private fun TripResponse.toTrackingData(): TrackingData = TrackingData(
    tripId = tripId,
    destinationLocation = if (destinationLat != null && destinationLng != null) {
        GeoPoint(destinationLat, destinationLng)
    } else null,
    stops = stops.map { GeoPoint(it.latitude, it.longitude) },
    stopIds = stops.map { it.id },
    tripInfo = TripTrackingInfo(
        driverName = driverName ?: "Unknown Driver",
        driverRating = 4.8,
        status = when (status.uppercase()) {
            "SCHEDULED" -> RideStatus.ON_THE_WAY
            "IN_PROGRESS" -> RideStatus.IN_TRANSIT
            "ARRIVED" -> RideStatus.ARRIVED
            "CANCELLED" -> RideStatus.CANCELLED
            else -> RideStatus.ON_THE_WAY
        },
        etaMinutes = null,
        carModel = vehicleModel ?: "Unknown Car",
        carColor = vehicleColour ?: "Unknown Color",
        carYear = 0,
        plateNumber = registrationNumber ?: "Unknown",
        isPlateVerified = registrationNumber != null,
        destinationLabel = destinationStop
    )
)

class TrackingViewModelFactory(
    private val rideId: String?,
    private val socket: RideLocationSocket,
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TrackingViewModel(rideId, socket, tripRepository) as T
    }
}
