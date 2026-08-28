package com.example.getyourride.viewmodel

import kotlinx.coroutines.*
import kotlin.random.Random

/**
 * A mock implementation of RideLocationSocket for UI testing and manual demos.
 *
 * NOT WIRED INTO THE APP. The tracking routes in MainActivity both use
 * [StompRideLocationSocket]; this class is intentionally left unreferenced so no runtime
 * path can show simulated driver movement. It used to back the Track bottom-nav tab, which
 * is why that screen always animated a fake driver even with no active rides.
 *
 * If you wire it up again, do so from an explicitly debug-only entry point.
 */
class MockRideLocationSocket : RideLocationSocket {
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    override fun connect(
        rideId: String,
        onUpdate: (DriverLocationUpdate) -> Unit,
        onStopUpdate: (stopId: Long) -> Unit,
        onError: (String) -> Unit
    ) {
        job?.cancel()
        job = scope.launch {
            // Starting point (roughly around Gqeberha/Port Elizabeth area for context)
            var lat = -33.99
            var lng = 25.66
            var count = 0

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

                // Simulate passing a stop every 10 updates. This is preview/demo data, not a real
                // trip_stop.id - TrackingViewModel treats it as a plain index since stopIds is
                // never populated for the mock's synthetic trip.
                count++
                if (count % 10 == 0) {
                    onStopUpdate((count / 10).toLong())
                }

                delay(2000) // Update every 2 seconds
            }
        }
    }

    override fun disconnect() {
        job?.cancel()
        job = null
    }
}