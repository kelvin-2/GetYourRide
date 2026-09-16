package com.example.getyourride.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GeocodeRequest(
    val address: String
)

/**
 * Response shape for POST /api/geocode (the precise, single-address lookup).
 *
 * These are nullable, so a field-name mismatch here fails differently from
 * AddressSuggestion: instead of 0.0 coordinates the values come back null and
 * the caller treats it as "address not found", meaning the button appears to
 * do nothing. The alternates cover both the short (lat/lon) and long
 * (latitude/longitude) spellings so neither case can happen.
 */
data class GeocodeResult(
    val found: Boolean,
    @SerializedName("latitude", alternate = ["lat"])
    val latitude: Double? = null,
    @SerializedName("longitude", alternate = ["lon", "lng"])
    val longitude: Double? = null,
    val matchedAddress: String? = null
)
