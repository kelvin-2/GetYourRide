package com.example.getyourride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.data.DriverApplicationSubmitStatus
import com.example.getyourride.data.UseCaseSubmitStatus
import com.example.getyourride.data.repository.StudentAuthRepository
import com.example.getyourride.di.NetworkModule
import com.example.getyourride.network.SpringBootApiService
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.screens.Carpool.CarpoolHomeScreen
import com.example.getyourride.ui.screens.DriverProfileSettingsScreen
import com.example.getyourride.ui.screens.DriverStep1Screen
import com.example.getyourride.ui.screens.DriverStep2Screen
import com.example.getyourride.ui.screens.DriverStep3Screen
import com.example.getyourride.ui.screens.LoginScreen
import com.example.getyourride.ui.screens.OfferRideScreen
import com.example.getyourride.ui.screens.SignUpScreen
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.AuthUiState
import com.example.getyourride.viewmodel.AuthViewModel
import com.example.getyourride.viewmodel.AuthViewModelFactory
import com.example.getyourride.viewmodel.DeleteDriverProfileViewModel
import com.example.getyourride.viewmodel.DriverApplicationViewModel
import com.example.getyourride.viewmodel.OfferRideViewModel

// ─────────────────────────────────────────────────────────────────────────────
// ROUTING NOTES — how student type decides which dashboard they see
//
// After login/signup, the student is one of two types:
//   - NSFAS funded  → routed to "shuttle_home" (fixed shuttle routes)
//   - Self-funded   → routed to GyrRoutes.HOME = CarpoolHomeScreen (peer-to-peer)
//
// isNsfasFunded now comes from the REAL backend response (AuthResponse.isFunded)
// returned by StudentAuthApi — set inside the LaunchedEffect blocks below.
// ─────────────────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()

                // ── Existing mock API service (driver flow, offer ride, etc.) ──────
                // Unrelated to auth — left untouched.
                val apiService = remember {
                    SpringBootApiService(
                        baseUrl = "https://your-spring-boot-api.example.com"
                    )
                }
                val driverApplicationViewModel = remember {
                    DriverApplicationViewModel(apiService = apiService)
                }
                val offerRideViewModel = remember {
                    OfferRideViewModel(apiService = apiService)
                }
                val deleteDriverProfileViewModel = remember {
                    DeleteDriverProfileViewModel(apiService = apiService)
                }

                // ── Real auth wiring — talks to StudentAuthController on :8080 ─────
                val studentAuthRepository = remember {
                    StudentAuthRepository(api = NetworkModule.studentAuthApi)
                }
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(studentAuthRepository)
                )

                // ── Student funding status — drives which dashboard they land on ──
                // Set from the real AuthResponse.isFunded field once login/signup succeeds.
                var isNsfasFunded by remember { mutableStateOf(false) }

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {

                    // ── LOGIN ──────────────────────────────────────────────────────
                    composable("login") {
                        val uiState = authViewModel.uiState

                        // Fires once when login succeeds — navigates, then resets state
                        // so re-entering this screen later doesn't auto-navigate again.
                        LaunchedEffect(uiState) {
                            if (uiState is AuthUiState.Success) {
                                isNsfasFunded = uiState.response.isFunded ?: false
                                navController.navigate(homeRouteFor(isNsfasFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        LoginScreen(
                            onCreateAccountClick = { navController.navigate("signup") },
                            onLoginClick = { email, password ->
                                authViewModel.login(email, password)
                            },
                            // ⚠️ Add these two params to LoginScreen's signature
                            // if they don't already exist — see note below file.
                            isLoading    = uiState is AuthUiState.Loading,
                            errorMessage = (uiState as? AuthUiState.Error)?.message,
                        )
                    }

                    // ── SIGN UP ────────────────────────────────────────────────────
                    composable("signup") {
                        val uiState = authViewModel.uiState

                        LaunchedEffect(uiState) {
                            if (uiState is AuthUiState.Success) {
                                isNsfasFunded = uiState.response.isFunded ?: false
                                navController.navigate(homeRouteFor(isNsfasFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        SignUpScreen(
                            onBackClick          = { navController.popBackStack() },
                            onLoginClick         = { navController.popBackStack() },
                            onBecomeDriverClick  = { navController.navigate("driver_step_1") },
                            onSignUpClick = { fullName, studentNumber, email, password, isFunded ->
                                // SignUpScreen captures one "fullName" field, but the backend
                                // wants firstName + lastName separately — split on first space.
                                val nameParts = fullName.trim().split(" ", limit = 2)
                                val firstName = nameParts.getOrElse(0) { "" }
                                val lastName  = nameParts.getOrElse(1) { "" }

                                authViewModel.register(
                                    studentNumber = studentNumber,
                                    firstName     = firstName,
                                    lastName      = lastName,
                                    email         = email,
                                    phone         = "", // ⚠️ SignUpScreen has no phone field yet
                                    password      = password,
                                    isFunded      = isFunded,
                                )
                            },

                            isLoading    = uiState is AuthUiState.Loading,
                            errorMessage = (uiState as? AuthUiState.Error)?.message,
                        )
                    }

                    // ── BECOME A DRIVER FLOW (unchanged) ──────────────────────────
                    composable("driver_step_1") {
                        DriverStep1Screen(
                            onBackClick = { navController.popBackStack() },
                            onNextClick = { step1Data ->
                                if (driverApplicationViewModel.saveStep1(step1Data)) {
                                    navController.navigate("driver_step_2")
                                }
                            },
                            errorMessage = driverApplicationViewModel.step1ErrorMessage
                        )
                    }
                    composable("driver_step_2") {
                        DriverStep2Screen(
                            onBackClick = { navController.popBackStack() },
                            onNextClick = { step2Data ->
                                if (driverApplicationViewModel.saveStep2(step2Data)) {
                                    navController.navigate("driver_step_3")
                                }
                            },
                            errorMessage = driverApplicationViewModel.step2ErrorMessage
                        )
                    }
                    composable("driver_step_3") {
                        val submitStatus = driverApplicationViewModel.submitStatus
                        DriverStep3Screen(
                            onBackClick = { navController.popBackStack() },
                            onSubmitClick = { step3Data ->
                                driverApplicationViewModel.submitApplication(step3Data)
                            },
                            errorMessage = driverApplicationViewModel.step3ErrorMessage,
                            statusMessage = when (submitStatus) {
                                is DriverApplicationSubmitStatus.Loading -> "Submitting driver application..."
                                is DriverApplicationSubmitStatus.Success -> submitStatus.message
                                else -> null
                            }
                        )
                    }

                    composable("offer_ride") {
                        val submitStatus = offerRideViewModel.submitStatus
                        OfferRideScreen(
                            onPostRideClick = { request -> offerRideViewModel.postRide(request) },
                            errorMessage    = offerRideViewModel.errorMessage,
                            statusMessage = when (submitStatus) {
                                is UseCaseSubmitStatus.Loading -> "Posting ride..."
                                is UseCaseSubmitStatus.Success -> submitStatus.message
                                else -> null
                            }
                        )
                    }

                    composable("driver_profile_settings") {
                        val submitStatus = deleteDriverProfileViewModel.submitStatus
                        DriverProfileSettingsScreen(
                            onBackClick = { navController.popBackStack() },
                            onConfirmDeleteClick = { deleteDriverProfileViewModel.deactivateProfile() },
                            statusMessage = when (submitStatus) {
                                is UseCaseSubmitStatus.Loading -> "Deleting driver profile..."
                                is UseCaseSubmitStatus.Success -> submitStatus.message
                                else -> null
                            },
                            errorMessage = when (submitStatus) {
                                is UseCaseSubmitStatus.Error -> submitStatus.message
                                else -> null
                            }
                        )
                    }

                    // ── CARPOOL DASHBOARD (self-funded students) ──────────────────
                    composable(GyrRoutes.HOME) {
                        CarpoolHomeScreen(
                            navController = navController,
                            onPostRide    = { navController.navigate("offer_ride") },
                        )
                    }

                    // ── SHUTTLE DASHBOARD (NSFAS-funded students) ─────────────────
                    // TODO: replace this placeholder with a real ShuttleHomeScreen
                    // once it's built — same wiring pattern as CarpoolHomeScreen above.
                    composable("shuttle_home") {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Shuttle Home Screen — coming soon")
                            Button(onClick = { navController.navigate("offer_ride") }) {
                                Text("Offer Ride")
                            }
                            Button(onClick = { navController.navigate("driver_profile_settings") }) {
                                Text("Delete Driver Profile")
                            }
                        }
                    }

                    // Add GyrRoutes.RIDES / TRACK / PROFILE composables here as you build them
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// homeRouteFor — single source of truth for "which dashboard does this student see"
//
// Called from both login and signup so the routing rule only lives in one place.
// If you add a 3rd student type later, this is the only function to update.
// ─────────────────────────────────────────────────────────────────────────────
private fun homeRouteFor(isNsfasFunded: Boolean): String {
    return if (isNsfasFunded) "shuttle_home" else GyrRoutes.HOME
}