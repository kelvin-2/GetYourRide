package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.getyourride.data.repository.GeocodingRepository

class CarpoolSearchViewModelFactory(
    private val geocodingRepository: GeocodingRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        if (modelClass.isAssignableFrom(CarpoolSearchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CarpoolSearchViewModel(geocodingRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}