package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.GeocodingApi
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.remote.dto.GeocodeRequest
import com.example.getyourride.data.remote.dto.GeocodeResult

class GeocodingRepository(private val api: GeocodingApi) {
    suspend fun suggest(query: String): Result<List<AddressSuggestion>> {
        return try {
            Result.success(api.suggest(query))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun geocode(address: String): Result<GeocodeResult> {
        return try {
            val result = api.geocode(GeocodeRequest(address))
            if (result.found) {
                Result.success(result)
            } else {
                Result.failure(NoSuchElementException("Address not found: $address"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Turns a GPS fix into a readable address. Unlike geocode(), there's no
    // "found" flag from the backend to check - GeocodingService always returns
    // an AddressSuggestion (falling back to a generic "Current Location" label
    // if Nominatim has nothing tagged at that exact point), so any thrown
    // exception here means a real network/server failure, not a "not found".
    suspend fun reverseGeocode(lat: Double, lon: Double): Result<AddressSuggestion> {
        return try {
            Result.success(api.reverseGeocode(lat, lon))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}