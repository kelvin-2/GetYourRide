package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.getyourride.data.repository.GeocodingRepository

class StopSearchViewModelFactory(
    private val geocodingRepository: GeocodingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StopSearchViewModel(geocodingRepository) as T
    }
    
}