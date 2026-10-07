package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.TripReviewDetailResponse
import com.example.getyourride.data.repository.TripRepository
import kotlinx.coroutines.launch

/**
 * UI state for the Trip Reviews screen (driver "View Ratings").
 */
sealed class TripReviewsUiState {
    data object Loading : TripReviewsUiState()
    data class Success(val reviews: List<TripReviewDetailResponse>) : TripReviewsUiState()
    data class Error(val message: String) : TripReviewsUiState()
}

/**
 * Loads the reviews students left for a specific completed trip.
 */
class TripReviewsViewModel(
    private val tripRepository: TripRepository
) : ViewModel() {

    var uiState by mutableStateOf<TripReviewsUiState>(TripReviewsUiState.Loading)
        private set

    fun loadReviews(tripId: Long) {
        uiState = TripReviewsUiState.Loading
        viewModelScope.launch {
            tripRepository.getTripReviews(tripId)
                .onSuccess { reviews ->
                    uiState = TripReviewsUiState.Success(reviews)
                }
                .onFailure { error ->
                    uiState = TripReviewsUiState.Error(
                        error.message ?: "Failed to load ratings."
                    )
                }
        }
    }
}

class TripReviewsViewModelFactory(
    private val tripRepository: TripRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TripReviewsViewModel(tripRepository) as T
    }
}
