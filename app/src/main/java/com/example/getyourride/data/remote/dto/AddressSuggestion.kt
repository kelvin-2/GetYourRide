package com.example.getyourride.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Response shape for GET /api/geocode/suggestions and GET /api/geocode/reverse.
 *
 * The backend serialises these as "latitude"/"longitude". These used to be
 * declared as `lat`/`lon` with no @SerializedName, so Gson found no matching
 * keys and left both at 0.0 (it allocates via Unsafe, so Kotlin constructor
 * defaults never run). That silently produced pickup stops at 0,0 in the
 * Gulf of Guinea while the displayName looked perfectly correct.
 *
 * `alternate` keeps the old short keys working too, so either wire format
 * deserialises correctly.
 */
data class AddressSuggestion(
    @SerializedName("displayName", alternate = ["display_name", "name"])
    val displayName: String,

    @SerializedName("latitude", alternate = ["lat"])
    val latitude: Double,

    @SerializedName("longitude", alternate = ["lon", "lng"])
    val longitude: Double
)
