package com.example.getyourride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeocodeRequest(
    val address: String
)

data class GeocodeResult(
    val found: Boolean,
    @SerializedName("latitude")val lat: Double? = null,
    @SerializedName("longitude")val lon: Double? = null,
    val matchedAddress: String? = null
)