package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.repository.GeocodingRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class LocationFieldState(
    val text: String = "",
    val suggestions: List<AddressSuggestion> = emptyList(),
    val selected: AddressSuggestion? = null,   // holds lat/lon once picked
    val isLoading: Boolean = false
)

@OptIn(FlowPreview::class)
class CarpoolSearchViewModel(
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val _pickup = MutableStateFlow(LocationFieldState())
    val pickup: StateFlow<LocationFieldState> = _pickup

    private val _destination = MutableStateFlow(LocationFieldState())
    val destination: StateFlow<LocationFieldState> = _destination

    private val pickupQuery = MutableStateFlow("")
    private val destinationQuery = MutableStateFlow("")

    // True while resolving typed-but-unselected text on search submit
    private val _isResolving = MutableStateFlow(false)
    val isResolving: StateFlow<Boolean> = _isResolving

    init {
        observeQuery(pickupQuery, _pickup)
        observeQuery(destinationQuery, _destination)
    }

    private fun observeQuery(
        queryFlow: MutableStateFlow<String>,
        stateFlow: MutableStateFlow<LocationFieldState>
    ) {
        viewModelScope.launch {
            queryFlow
                .debounce(350)
                .distinctUntilChanged()
                .filter { it.length >= 3 }
                .collectLatest { query ->
                    if (stateFlow.value.text != query) return@collectLatest
                    stateFlow.update { it.copy(isLoading = true) }

                    geocodingRepository.suggest(query)
                        .onSuccess { results ->
                            stateFlow.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = results, isLoading = false)
                            }
                        }
                        .onFailure {
                            stateFlow.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = emptyList(), isLoading = false)
                            }
                        }
                }
        }
    }

    fun onPickupTextChanged(text: String) {
        _pickup.value = _pickup.value.copy(text = text, selected = null, suggestions = emptyList())
        pickupQuery.value = text
    }

    fun onPickupSuggestionSelected(suggestion: AddressSuggestion) {
        _pickup.value = LocationFieldState(text = suggestion.displayName, selected = suggestion)
    }

    fun onDestinationTextChanged(text: String) {
        _destination.value = _destination.value.copy(text = text, selected = null, suggestions = emptyList())
        destinationQuery.value = text
    }

    fun onDestinationSuggestionSelected(suggestion: AddressSuggestion) {
        _destination.value = LocationFieldState(text = suggestion.displayName, selected = suggestion)
    }

    /**
     * Called when the user taps "Search Rides". If a field already has a tapped
     * suggestion, uses it as-is. Otherwise, resolves the raw typed text through
     * the precise /api/geocode endpoint (same one your "confirm address" flow uses),
     * so "south campus" works even if the user never opened the dropdown.
     */
    fun onSearchClicked(
        onReady: (pickup: AddressSuggestion, destination: AddressSuggestion) -> Unit,
        onError: (String) -> Unit
    ) {
        val currentPickup = _pickup.value
        val currentDestination = _destination.value

        if (currentPickup.text.isBlank() || currentDestination.text.isBlank()) {
            onError("Enter both a pickup and destination")
            return
        }

        viewModelScope.launch {
            _isResolving.value = true

            val resolvedPickup = currentPickup.selected ?: resolveTypedAddress(currentPickup.text)
            val resolvedDestination = currentDestination.selected ?: resolveTypedAddress(currentDestination.text)

            _isResolving.value = false

            if (resolvedPickup == null) {
                onError("Couldn't find that pickup location — try a suggestion instead")
                return@launch
            }
            if (resolvedDestination == null) {
                onError("Couldn't find that destination — try a suggestion instead")
                return@launch
            }

            // Lock the resolved result into state so the UI reflects it as "selected" too
            if (currentPickup.selected == null) {
                _pickup.value = currentPickup.copy(selected = resolvedPickup, text = resolvedPickup.displayName)
            }
            if (currentDestination.selected == null) {
                _destination.value = currentDestination.copy(selected = resolvedDestination, text = resolvedDestination.displayName)
            }

            onReady(resolvedPickup, resolvedDestination)
        }
    }

    private suspend fun resolveTypedAddress(text: String): AddressSuggestion? {
        return geocodingRepository.geocode(text).getOrNull()?.let { result ->
            val lat = result.lat ?: return null
            val lon = result.lon ?: return null
            val displayName = result.matchedAddress ?: text
            AddressSuggestion(displayName = displayName, lat = lat, lon = lon)
        }
    }
}