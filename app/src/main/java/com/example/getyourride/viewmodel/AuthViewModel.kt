// ─────────────────────────────────────────────────────────────────────────────
// AuthViewModel.kt
// Package: com.example.getyourride.viewmodel
//
// PURPOSE — Holds login/signup state and talks to StudentAuthRepository.
// LoginScreen and SignUpScreen call this — they never call the repository
// or API directly.
//
// Usage in MainActivity:
//   val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(...))
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.AuthResponse
import com.example.getyourride.data.repository.AuthResult
import com.example.getyourride.data.repository.StudentAuthRepository
import kotlinx.coroutines.launch

/**
 * UI-facing state for any screen using this ViewModel.
 * Idle    → nothing happening yet
 * Loading → request in flight, show a spinner / disable the button
 * Success → got a token back, safe to navigate
 * Error   → show errorMessage to the user
 */
sealed class AuthUiState {
    object Idle    : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val response: AuthResponse) : AuthUiState()
    data class Error(val message: String)           : AuthUiState()
}

class AuthViewModel(
    private val repository: StudentAuthRepository,
) : ViewModel() {

    var uiState by mutableStateOf<AuthUiState>(AuthUiState.Idle)
        private set

    fun login(email: String, password: String) {
        // Basic guard so we don't hit the API with empty fields
        if (email.isBlank() || password.isBlank()) {
            uiState = AuthUiState.Error("Please enter your email and password.")
            return
        }

        uiState = AuthUiState.Loading
        viewModelScope.launch {
            uiState = when (val result = repository.login(email, password)) {
                is AuthResult.Success -> AuthUiState.Success(result.data)
                is AuthResult.Error   -> AuthUiState.Error(result.message)
            }
        }
    }

    fun register(
        studentNumber : String,
        firstName     : String,
        lastName      : String,
        email         : String,
        phone         : String,
        password      : String,
        isFunded      : Boolean,
    ) {
        if (listOf(studentNumber, firstName, lastName, email, password).any { it.isBlank() }) {
            uiState = AuthUiState.Error("Please fill in all required fields.")
            return
        }

        uiState = AuthUiState.Loading
        viewModelScope.launch {
            uiState = when (
                val result = repository.register(
                    studentNumber = studentNumber,
                    firstName     = firstName,
                    lastName      = lastName,
                    email         = email,
                    phone         = phone,
                    password      = password,
                    isFunded      = isFunded,
                )
            ) {
                is AuthResult.Success -> AuthUiState.Success(result.data)
                is AuthResult.Error   -> AuthUiState.Error(result.message)
            }
        }
    }

    /** Call after navigating away from a Success/Error state so it doesn't re-trigger. */
    fun resetState() {
        uiState = AuthUiState.Idle
    }
}

// ── Factory — needed because AuthViewModel takes a constructor parameter ──────
class AuthViewModelFactory(
    private val repository: StudentAuthRepository,
) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return AuthViewModel(repository) as T
    }
}