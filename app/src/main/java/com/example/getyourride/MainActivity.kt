package com.example.getyourride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.data.DriverApplicationSubmitStatus
import com.example.getyourride.data.UseCaseSubmitStatus
import com.example.getyourride.network.SpringBootApiService
import com.example.getyourride.ui.screens.DriverProfileSettingsScreen
import com.example.getyourride.ui.screens.DriverStep1Screen
import com.example.getyourride.ui.screens.DriverStep2Screen
import com.example.getyourride.ui.screens.DriverStep3Screen
import com.example.getyourride.ui.screens.LoginScreen
import com.example.getyourride.ui.screens.OfferRideScreen
import com.example.getyourride.ui.screens.SignUpScreen
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.DeleteDriverProfileViewModel
import com.example.getyourride.viewmodel.DriverApplicationViewModel
import com.example.getyourride.viewmodel.OfferRideViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()
                val apiService = remember {
                    SpringBootApiService(
                        baseUrl = "https://your-spring-boot-api.example.com"
                    )
                }
                val driverApplicationViewModel = remember {
                    DriverApplicationViewModel(
                        apiService = apiService
                    )
                }
                val offerRideViewModel = remember {
                    OfferRideViewModel(apiService = apiService)
                }
                val deleteDriverProfileViewModel = remember {
                    DeleteDriverProfileViewModel(apiService = apiService)
                }

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick  = { navController.navigate("signup") },
                            onLoginClick          = { _, _ -> navController.navigate("home") }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onBackClick   = { navController.popBackStack() },
                            onLoginClick  = { navController.popBackStack() },
                            onBecomeDriverClick = { navController.navigate("driver_step_1") },
                            onSignUpClick = { _, _, _, _, _ -> navController.navigate("home") }
                        )
                    }
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
                            onPostRideClick = { request ->
                                offerRideViewModel.postRide(request)
                            },
                            errorMessage = offerRideViewModel.errorMessage,
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
                            onConfirmDeleteClick = {
                                deleteDriverProfileViewModel.deactivateProfile()
                            },
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

                    composable("home") {
                        androidx.compose.foundation.layout.Column(
                            modifier = androidx.compose.ui.Modifier.padding(24.dp),
                            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                        ) {
                            androidx.compose.material3.Text("Home Screen - coming soon")
                            androidx.compose.material3.Button(
                                onClick = { navController.navigate("offer_ride") }
                            ) {
                                androidx.compose.material3.Text("Offer Ride")
                            }
                            androidx.compose.material3.Button(
                                onClick = { navController.navigate("driver_profile_settings") }
                            ) {
                                androidx.compose.material3.Text("Delete Driver Profile")
                            }
                        }
                    }
                }
            }
        }
    }
}
