package com.example.getyourride.viewmodel

import com.example.getyourride.data.remote.dto.AddressSuggestion

/**
 * Shared state for an autocomplete location field.
 * Used by CarpoolSearchViewModel, StopSearchViewModel, and now OfferRideViewModel.
 */
data class LocationFieldState(
    val text: String = "",
    val suggestions: List<AddressSuggestion> = emptyList(),
    val selected: AddressSuggestion? = null,
    val isLoading: Boolean = false
)
