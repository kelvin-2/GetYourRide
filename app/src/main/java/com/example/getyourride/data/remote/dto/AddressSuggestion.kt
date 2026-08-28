package com.example.getyourride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class AddressSuggestion(
    val displayName: String,
    @SerializedName("latitude") val lat: Double,
    @SerializedName("longitude") val lon: Double
)