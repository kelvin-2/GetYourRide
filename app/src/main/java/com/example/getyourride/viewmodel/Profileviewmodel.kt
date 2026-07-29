package com.example.getyourride.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.model.StudentProfile
import com.example.getyourride.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    data object Loading : ProfileUiState()
    data class Success(val profile: StudentProfile) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val user = UserSession.current
                if (user != null) {
                    val profile = StudentProfile(
                        name = "${user.firstName} ${user.lastName}",
                        initials = "${user.firstName.take(1)}${user.lastName.take(1)}".uppercase(),
                        studentNumber = "S-${user.studentNumber}", // Fallback since studentNumber isn't in AuthResponse
                        email = user.email,
                        phone = user.phone,
                        isNsfasFunded = user.isFunded ?: false
                    )
                    _uiState.value = ProfileUiState.Success(profile)
                } else {
                    _uiState.value = ProfileUiState.Error("No user logged in")
                }
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun onLogOut(onComplete: () -> Unit) {
        viewModelScope.launch {
            UserSession.clear()
            onComplete()
        }
    }
}
