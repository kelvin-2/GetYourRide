package com.example.getyourride.data.remote.dto

data class GeocodeRequest(
    val address: String
)

data class GeocodeResult(
    val found: Boolean,
    val lat: Double? = null,
    val lon: Double? = null,
    val matchedAddress: String? = null
)