package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.UserSession
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse
import com.example.getyourride.data.repository.ShuttleDriverRepository
import kotlinx.coroutines.launch

/**
 * UI state for the shuttle driver profile screen.
 */
sealed interface ShuttleDriverProfileUiState {
    object Loading : ShuttleDriverProfileUiState
    data class Success(val profile: ShuttleDriverProfileResponse) : ShuttleDriverProfileUiState
    data class Error(val message: String) : ShuttleDriverProfileUiState
}

/**
 * ViewModel for the ShuttleDriverProfileScreen.
 *
 * Fetches the driver's profile from the backend when loadProfile() is called.
 * Uses the driverId from UserSession (set at login).
 */
class ShuttleDriverProfileViewModel(
    private val repository: ShuttleDriverRepository
) : ViewModel() {

    var uiState: ShuttleDriverProfileUiState by mutableStateOf(ShuttleDriverProfileUiState.Loading)
        private set

    /**
     * Load the shuttle driver profile from the backend.
     * Uses the driver ID stored in UserSession after login.
     */
    fun loadProfile() {
        val driverId = UserSession.id
        if (driverId == null) {
            uiState = ShuttleDriverProfileUiState.Error("Not logged in. Please log in again.")
            return
        }

        uiState = ShuttleDriverProfileUiState.Loading
        viewModelScope.launch {
            try {
                val profile = repository.getProfile(driverId)
                uiState = ShuttleDriverProfileUiState.Success(profile)
            } catch (e: Exception) {
                uiState = ShuttleDriverProfileUiState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }
}

class ShuttleDriverProfileViewModelFactory(
    private val repository: ShuttleDriverRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ShuttleDriverProfileViewModel(repository) as T
    }
}
