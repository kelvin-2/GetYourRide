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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.data.DriverApplicationSubmitStatus
import com.example.getyourride.data.UseCaseSubmitStatus
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
import com.example.getyourride.viewmodel.DeleteDriverProfileViewModel
import com.example.getyourride.viewmodel.DriverApplicationViewModel
import com.example.getyourride.viewmodel.OfferRideViewModel
import com.example.getyourride.ui.screens.StudentDriverHomeScreen
import com.example.getyourride.ui.screens.DriverProfileDetails
import androidx.compose.runtime.LaunchedEffect
import com.example.getyourride.ui.screens.RideAcceptedStudent
import com.example.getyourride.ui.screens.StudentDriverPostedRide


// ─────────────────────────────────────────────────────────────────────────────
// ROUTING NOTES — how student type decides which dashboard they see
//
// After login/signup, the student is one of two types:
//   - NSFAS funded  → routed to "shuttle_home" (fixed shuttle routes)
//   - Self-funded   → routed to GyrRoutes.HOME = CarpoolHomeScreen (peer-to-peer)
//
// "isNsfasFunded" is currently a local Compose state variable set from the
// SignUpScreen's NSFAS radio answer. Once your Spring Boot auth endpoint is
// wired up, replace this with the value from the login API response instead
// (e.g. response.user.isNsfasFunded) — everything else stays the same because
// routing always goes through homeRouteFor() below.
// ─────────────────────────────────────────────────────────────────────────────

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
                    DriverApplicationViewModel(apiService = apiService)
                }
                val offerRideViewModel = remember {
                    OfferRideViewModel(apiService = apiService)
                }
                val deleteDriverProfileViewModel = remember {
                    DeleteDriverProfileViewModel(apiService = apiService)
                }

                // ── Student funding status — drives which dashboard they land on ──
                // TODO: replace this default with the real value from your
                // login API response once the backend is connected.
                var isNsfasFunded by remember { mutableStateOf(false) }

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {

                    // ── LOGIN ──────────────────────────────────────────────────────
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick = { navController.navigate("signup") },
                            onLoginClick = { _, _ ->
                                // TODO: replace with real API call.
                                // isNsfasFunded keeps whatever value it last had
                                // (set during signup, or default false on fresh installs).
                                navController.navigate(homeRouteFor(isNsfasFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
                        )
                    }

                    // ── SIGN UP ────────────────────────────────────────────────────
                    composable("signup") {
                        SignUpScreen(
                            onBackClick          = { navController.popBackStack() },
                            onLoginClick         = { navController.popBackStack() },
                            onBecomeDriverClick  = { navController.navigate("driver_step_1") },
                            onSignUpClick = {firstName, lastName, studentNumber, email, password, isFunded->
                                // The 5th param from SignUpScreen IS the NSFAS
                                // radio answer the student picked — use it directly.
                                navController.navigate(homeRouteFor(isFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                            }
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
                            onBackClick = {
                                navController.popBackStack()
                            },
                            onSubmitClick = { step3Data ->
                                driverApplicationViewModel.submitApplication(step3Data)

                                navController.navigate("student_driver_home") {
                                    popUpTo("driver_step_1") {
                                        inclusive = true
                                    }
                                    launchSingleTop = true
                                }
                            },
                            errorMessage = driverApplicationViewModel.step3ErrorMessage,
                            statusMessage = when (val status = driverApplicationViewModel.submitStatus) {
                                is DriverApplicationSubmitStatus.Success -> status.message
                                is DriverApplicationSubmitStatus.Error -> status.message
                                else -> null
                            }
                        )
                    }

                    //Offer a Ride page
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
                            },
                            onHomeClick = {
                                navController.navigate("student_driver_home") {
                                    launchSingleTop = true
                                }
                            },
                            onOfferRideClick = {
                                navController.navigate("offer_ride") {
                                    launchSingleTop = true
                                }
                            },
                            onProfileClick = {
                                navController.navigate("driver_profile_settings")
                            }
                        )
                    }
                    //Home page for Student driver
                    composable("student_driver_home") {
                        var isDriverHomeRefreshing by remember {
                            mutableStateOf(false)
                        }

                        /*
                         * Temporary frontend refresh simulation.
                         *
                         * Later, this is where we will call the backend API to get:
                         * - latest verification status
                         * - latest posted rides
                         * - latest accepted students
                         */
                        if (isDriverHomeRefreshing) {
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(1200)
                                isDriverHomeRefreshing = false
                            }
                        }

                        StudentDriverHomeScreen(
                            driverName = "Ayabulela",
                            verificationStatus = "Pending Verification",
                            isRefreshing = isDriverHomeRefreshing,
                            postedRides = listOf(
                                StudentDriverPostedRide(
                                    rideId = "1",
                                    pickupLocation = "South Campus",
                                    destination = "North Campus",
                                    date = "2026-07-01",
                                    time = "08:30",
                                    availableSeats = 3,
                                    farePerSeat = "R20.00",
                                    acceptedStudents = listOf(
                                        RideAcceptedStudent(
                                            name = "Lanele Maqina",
                                            studentNumber = "223456789"
                                        ),
                                        RideAcceptedStudent(
                                            name = "Tichaona Mudingwa",
                                            studentNumber = "224567890"
                                        )
                                    )
                                )
                            ),
                            onRefreshClick = {
                                isDriverHomeRefreshing = true
                            },
                            onHomeClick = {
                                navController.navigate("student_driver_home") {
                                    launchSingleTop = true
                                }
                            },
                            onOfferRideClick = {
                                navController.navigate("offer_ride")
                            },
                            onProfileClick = {
                                navController.navigate("driver_profile_settings")
                            }
                        )
                    }

                    composable("driver_profile_settings") {
                        val submitStatus = deleteDriverProfileViewModel.submitStatus

                        DriverProfileSettingsScreen(
                            profileDetails = DriverProfileDetails(
                                firstName = "Ayabulela",
                                surname = "Mtwesi",
                                studentNumber = "223456789",
                                contactNumber = "071 234 5678",
                                universityEmail = "ayabulela@mandela.ac.za",
                                vehicleMake = "Toyota",
                                vehicleModel = "Corolla",
                                vehicleRegistrationNumber = "ABC 123 EC",
                                vehicleColour = "White",
                                seatingCapacity = 4,
                                verificationStatus = "Pending Verification",
                                driversLicenceStatus = "Uploaded",
                                vehicleRegistrationStatus = "Uploaded"
                            ),
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
                            },
                            onHomeClick = {
                                navController.navigate("student_driver_home") {
                                    launchSingleTop = true
                                }
                            },
                            onOfferRideClick = {
                                navController.navigate("offer_ride") {
                                    launchSingleTop = true
                                }
                            },
                            onProfileClick = {
                                navController.navigate("driver_profile_settings") {
                                    launchSingleTop = true
                                }
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