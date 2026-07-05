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
}
