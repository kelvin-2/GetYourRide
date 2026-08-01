package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.api.TripApi
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

data class TrackingUiState(
    val driverLocation: GeoPoint? = null,
    val destinationLocation: GeoPoint? = null,
    val stops: List<GeoPoint> = emptyList(),
    val currentStopIndex: Int = 0,
    val tripInfo: TripTrackingInfo? = null,
    val isConnected: Boolean = false,
    val error: String? = null
)

/**
 * Implement this against whichever STOMP client you already wired up
 * with FusedLocationProviderClient on the driver side.
 */
interface RideLocationSocket {
    fun connect(
        rideId: String,
        onUpdate: (DriverLocationUpdate) -> Unit,
        onStopUpdate: (Int) -> Unit,
        onError: (String) -> Unit
    )
    fun disconnect()
}

class TrackingViewModel(
    private val rideId: String,
    private val socket: RideLocationSocket,
    private val tripApi: TripApi
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()
    
    private var pollingJob: Job? = null

    fun startTracking() {
        if (rideId != "0") {
            loadTripDetails()
        } else {
            // Mock data for demo/preview if rideId is "0"
            _uiState.update {
                it.copy(
                    tripInfo = TripTrackingInfo(
                        driverName = "Marcus Thompson",
                        driverRating = 4.9,
                        status = RideStatus.ON_THE_WAY,
                        etaMinutes = 4,
                        carModel = "Toyota Corolla",
                        carColor = "White",
                        carYear = 2022,
                        plateNumber = "UNI-7842",
                        isPlateVerified = true,
                        destinationLabel = "Library North"
                    )
                )
            }
        }

        socket.connect(
            rideId = rideId,
            onUpdate = { update ->
                _uiState.update {
                    it.copy(
                        driverLocation = GeoPoint(update.latitude, update.longitude),
                        isConnected = true,
                        error = null
                    )
                }
            },
            onStopUpdate = { stopIndex ->
                _uiState.update { it.copy(currentStopIndex = stopIndex) }
            },
            onError = { message ->
                _uiState.update { it.copy(isConnected = false, error = message) }
                startPollingIfDisconnected()
            }
        )
    }

    private fun startPollingIfDisconnected() {
        if (pollingJob?.isActive == true || rideId == "0") return
        
        pollingJob = viewModelScope.launch {
            while (!_uiState.value.isConnected) {
                loadTripDetails()
                delay(10000) // Poll every 10s if socket is down
            }
        }
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            try {
                val response = tripApi.getTripById(rideId.toLong())
                if (response.isSuccessful) {
                    val trip = response.body()
                    trip?.let { t ->
                        _uiState.update { state ->
                            state.copy(
                                destinationLocation = if (t.destinationLat != null && t.destinationLng != null) 
                                    GeoPoint(t.destinationLat, t.destinationLng) else state.destinationLocation,
                                stops = t.stops.map { GeoPoint(it.latitude, it.longitude) },
                                tripInfo = TripTrackingInfo(
                                    driverName = t.driverName ?: "Unknown Driver",
                                    driverRating = 4.8,
                                    status = when (t.status) {
                                        "SCHEDULED" -> RideStatus.ON_THE_WAY
                                        "IN_PROGRESS" -> RideStatus.IN_TRANSIT
                                        "ARRIVED" -> RideStatus.ARRIVED
                                        "CANCELLED" -> RideStatus.CANCELLED
                                        else -> RideStatus.ON_THE_WAY
                                    },
                                    etaMinutes = 5,
                                    carModel = t.vehicleModel ?: "Unknown Car",
                                    carColor = t.vehicleColour ?: "Unknown Color",
                                    carYear = 2020,
                                    plateNumber = t.registrationNumber ?: "Unknown",
                                    isPlateVerified = true,
                                    destinationLabel = t.destinationStop
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to load trip details: ${e.message}") }
            }
        }
    }

    fun setDestination(lat: Double, lng: Double) {
        _uiState.update { it.copy(destinationLocation = GeoPoint(lat, lng)) }
    }

    fun cancelRide(onCancelled: () -> Unit) {
        // TODO: call your cancel-ride endpoint via TripApi, then:
        onCancelled()
    }

    override fun onCleared() {
        super.onCleared()
        socket.disconnect()
    }
}

class TrackingViewModelFactory(
    private val rideId: String,
    private val socket: RideLocationSocket,
    private val tripApi: TripApi
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TrackingViewModel(rideId, socket, tripApi) as T
    }
}