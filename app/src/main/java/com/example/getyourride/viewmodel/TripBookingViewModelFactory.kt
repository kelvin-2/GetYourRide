// TripBookingViewModelFactory.kt
package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.getyourride.data.repository.TripRepository

class TripBookingViewModelFactory(
    private val tripId: Long,
    private val repository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TripBookingViewModel(tripId, repository) as T
    }
}