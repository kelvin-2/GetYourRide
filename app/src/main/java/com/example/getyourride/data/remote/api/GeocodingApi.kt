package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.remote.dto.GeocodeRequest
import com.example.getyourride.data.remote.dto.GeocodeResult
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface GeocodingApi {
    @GET("api/geocode/suggestions")
    suspend fun suggest(@Query("query") query: String): List<AddressSuggestion>

    @POST("api/geocode")
    suspend fun geocode(@Body request: GeocodeRequest): GeocodeResult

    // Turns a raw GPS fix into a readable address - same AddressSuggestion shape
    // as suggest(), so "Current Location" saves as a stop exactly like a searched one.
    @GET("api/geocode/reverse")
    suspend fun reverseGeocode(@Query("lat") lat: Double, @Query("lon") lon: Double): AddressSuggestion
}