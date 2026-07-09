package com.example.getyourride.viewmodel

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * A mock implementation of RideLocationSocket for UI testing.
 * Simulates a driver moving towards a destination.
 */
class MockRideLocationSocket : RideLocationSocket {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun connect(
        rideId: String,
        onUpdate: (DriverLocationUpdate) -> Unit,
        onError: (String) -> Unit
    ) {
        job?.cancel()
        job = scope.launch {
            // Starting point (roughly around Gqeberha/Port Elizabeth area for context)
            var lat = -33.99
            var lng = 25.66

            while (isActive) {
                // Simulate small movements towards a "destination"
                lat += (Random.nextDouble() - 0.45) * 0.001
                lng += (Random.nextDouble() - 0.45) * 0.001
                
                onUpdate(
                    DriverLocationUpdate(
                        latitude = lat,
                        longitude = lng,
                        heading = Random.nextFloat() * 360f
                    )
                )
                delay(2000) // Update every 2 seconds
            }
        }
    }

    override fun disconnect() {
        job?.cancel()
        job = null
    }
}