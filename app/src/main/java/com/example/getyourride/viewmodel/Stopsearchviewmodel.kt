package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.repository.GeocodingRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Mirrors CarpoolSearchViewModel's LocationFieldState pattern but for a single
 * generic field (an extra stop), rather than a fixed pickup/destination pair.
 * Kept separate from CarpoolSearchViewModel on purpose - that one is scoped
 * specifically to a 2-field ride search and reusing it here would mean
 * bolting on a 3rd field it wasn't designed for.
 */
sealed interface CurrentLocationState {
    object Idle : CurrentLocationState
    object Locating : CurrentLocationState
    data class Resolved(val address: AddressSuggestion) : CurrentLocationState
    data class Failed(val message: String) : CurrentLocationState
}

@OptIn(FlowPreview::class)
class StopSearchViewModel(
    private val geocodingRepository: GeocodingRepository
) : ViewModel() {

    private val _field = MutableStateFlow(LocationFieldState())
    val field: StateFlow<LocationFieldState> = _field

    private val fieldQuery = MutableStateFlow("")

    private val _currentLocation = MutableStateFlow<CurrentLocationState>(CurrentLocationState.Idle)
    val currentLocation: StateFlow<CurrentLocationState> = _currentLocation

    init {
        observeQuery()
    }

    private fun observeQuery() {
        viewModelScope.launch {
            fieldQuery
                .debounce(700)
                .distinctUntilChanged()
                .filter { it.length >= 3 }
                .collectLatest { query ->
                    if (_field.value.text != query) return@collectLatest
                    _field.update { it.copy(isLoading = true) }

                    geocodingRepository.suggest(query)
                        .onSuccess { results ->
                            _field.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = results, isLoading = false)
                            }
                        }
                        .onFailure {
                            _field.update { current ->
                                if (current.selected != null || current.text != query) current
                                else current.copy(suggestions = emptyList(), isLoading = false)
                            }
                        }
                }
        }
    }

    fun onTextChanged(text: String) {
        _field.value = _field.value.copy(text = text, selected = null, suggestions = emptyList())
        fieldQuery.value = text
    }

    fun onSuggestionSelected(suggestion: AddressSuggestion) {
        _field.value = LocationFieldState(text = suggestion.displayName, selected = suggestion)
    }

    /**
     * Resolves a recent/history item (which is just a saved label, no coordinates)
     * back into a real AddressSuggestion via the precise geocode endpoint - the
     * same one CarpoolSearchViewModel.resolveTypedAddress uses for typed text.
     */
    fun resolveRecentLocation(label: String, onResolved: (AddressSuggestion?) -> Unit) {
        viewModelScope.launch {
            val resolved = geocodingRepository.geocode(label).getOrNull()?.let { result ->
                val lat = result.lat ?: return@let null
                val lon = result.lon ?: return@let null
                AddressSuggestion(displayName = result.matchedAddress ?: label, lat = lat, lon = lon)
            }
            onResolved(resolved)
        }
    }

    /** Called once a GPS fix comes back from FusedLocationProviderClient. */
    fun resolveCurrentLocation(lat: Double, lon: Double) {
        viewModelScope.launch {
            _currentLocation.value = CurrentLocationState.Locating
            geocodingRepository.reverseGeocode(lat, lon)
                .onSuccess { address -> _currentLocation.value = CurrentLocationState.Resolved(address) }
                .onFailure { e -> _currentLocation.value = CurrentLocationState.Failed(e.message ?: "Couldn't resolve address") }
        }
    }

    fun markCurrentLocationFailed(message: String) {
        _currentLocation.value = CurrentLocationState.Failed(message)
    }
    /**
     * Fallback for when the student typed a full address but never tapped a
     * suggestion row (e.g. because suggest() returned no results). Resolves the
     * raw typed text via the precise /api/geocode endpoint instead.
     */
    fun resolveTypedAddress(text: String, onResolved: (AddressSuggestion?) -> Unit) {
        viewModelScope.launch {
            val resolved = geocodingRepository.geocode(text).getOrNull()?.let { result ->
                val lat = result.lat ?: return@let null
                val lon = result.lon ?: return@let null
                AddressSuggestion(displayName = result.matchedAddress ?: text, lat = lat, lon = lon)
            }
            onResolved(resolved)
        }
    }
}

