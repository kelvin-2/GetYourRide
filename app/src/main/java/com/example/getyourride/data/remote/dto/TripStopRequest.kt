
package com.example.getyourride.data.remote.dto

data class TripStopRequest(
    val stopName: String,
    val latitude: Double,
    val longitude: Double,
    val stopOrder: Int? = null
)