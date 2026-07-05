package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.getyourride.domain.model.RideStatus
import com.example.getyourride.domain.model.TripTrackingInfo
import com.google.mlkit.vision.barcode.common.Barcode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val tripInfo: TripTrackingInfo? = null,
    val isConnected: Boolean = false,
    val error: String? = null
)

/**
 * Implement this against whichever STOMP client you already wired up
 * with FusedLocationProviderClient on the driver side.
 */
interface RideLocationSocket {
    fun connect(rideId: String, onUpdate: (DriverLocationUpdate) -> Unit, onError: (String) -> Unit)
    fun disconnect()
}

class TrackingViewModel(
    private val rideId: String,
    private val socket: RideLocationSocket
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    fun startTracking() {
        // TODO: replace with a real call to TripApi to load initial trip/driver details
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
            onError = { message ->
                _uiState.update { it.copy(isConnected = false, error = message) }
            }
        )
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
    private val socket: RideLocationSocket
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TrackingViewModel(rideId, socket) as T
    }
}