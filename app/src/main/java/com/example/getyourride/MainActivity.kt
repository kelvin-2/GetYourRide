package com.example.getyourride

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.getyourride.data.repository.TripRepository
import com.example.getyourride.viewmodel.RideViewModel
import com.example.getyourride.viewmodel.RideViewModelFactory
import android.os.Bundle
import com.example.getyourride.UserSession
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
import com.example.getyourride.data.mapper.toRideRequestDetails
import com.example.getyourride.data.repository.GeocodingRepository
import com.example.getyourride.data.repository.StudentAuthRepository
import com.example.getyourride.di.NetworkModule
import com.example.getyourride.network.SpringBootApiService
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.screens.AddStopScreen
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
import com.example.getyourride.ui.screens.StudentDriverHomeScreen
import com.example.getyourride.ui.screens.DriverProfileDetails
import com.example.getyourride.ui.screens.RideAcceptedStudent
import com.example.getyourride.ui.screens.Rides.MyRidesScreen
import com.example.getyourride.ui.screens.Rides.RequestRideScreen
import com.example.getyourride.ui.screens.StudentDriverPostedRide
import com.example.getyourride.viewmodel.AllRidesViewModel
import com.example.getyourride.viewmodel.AllRidesViewModelFactory
import com.example.getyourride.viewmodel.AllTripsUiState
import com.example.getyourride.viewmodel.StopSearchViewModel
import com.example.getyourride.viewmodel.StopSearchViewModelFactory
import com.example.getyourride.viewmodel.TripsUiState


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()

                // ── Existing mock API service (driver flow, offer ride) ────────
                val apiService = remember {
                    SpringBootApiService(baseUrl = "https://your-spring-boot-api.example.com")
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

                // ── Real auth — talks to StudentAuthController on :8080 ───────
                val studentAuthRepository = remember {
                    StudentAuthRepository(api = NetworkModule.studentAuthApi)
                }
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(studentAuthRepository)
                )

                // ── Shared RideViewModel — used by both RIDES and HOME tabs ───
                // Created here so the same instance survives tab navigation.
                // Both CarpoolHomeScreen and MyRidesScreen read from the same
                // ViewModel, so a booking on one screen reflects on the other.
                val rideViewModel: RideViewModel = viewModel(
                    factory = RideViewModelFactory(
                        TripRepository(NetworkModule.tripApi)
                    )
                )
                val allRidesViewModel: AllRidesViewModel = viewModel(
                    factory = AllRidesViewModelFactory(
                        TripRepository(NetworkModule.tripApi)
                    )
                )

                var isNsfasFunded by remember { mutableStateOf(false) }

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {

                    // ── LOGIN ──────────────────────────────────────────────────
                    composable("login") {
                        val uiState = authViewModel.uiState

                        LaunchedEffect(uiState) {
                            if (uiState is AuthUiState.Success) {
                                UserSession.save(uiState.response)
                                isNsfasFunded = uiState.response.isFunded ?: false
                                navController.navigate(homeRouteFor(isNsfasFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        LoginScreen(
                            onCreateAccountClick = { navController.navigate("signup") },
                            onLoginClick         = { email, password ->
                                authViewModel.login(email, password)
                            },
                            isLoading    = uiState is AuthUiState.Loading,
                            errorMessage = (uiState as? AuthUiState.Error)?.message,
                        )
                    }

                    // ── SIGN UP ────────────────────────────────────────────────
                    composable("signup") {
                        val uiState = authViewModel.uiState

                        LaunchedEffect(uiState) {
                            if (uiState is AuthUiState.Success) {
                                UserSession.save(uiState.response)
                                isNsfasFunded = uiState.response.isFunded ?: false
                                navController.navigate(homeRouteFor(isNsfasFunded)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        SignUpScreen(
                            onBackClick         = { navController.popBackStack() },
                            onLoginClick        = { navController.popBackStack() },
                            onBecomeDriverClick = { navController.navigate("driver_step_1") },
                            onSignUpClick = { fullName, studentNumber, email, password, isFunded ->
                                val nameParts = fullName.trim().split(" ", limit = 2)
                                authViewModel.register(
                                    studentNumber = studentNumber,
                                    firstName     = nameParts.getOrElse(0) { "" },
                                    lastName      = nameParts.getOrElse(1) { "" },
                                    email         = email,
                                    phone         = "",
                                    password      = password,
                                    isFunded      = isFunded,
                                )
                            },
                            isLoading    = uiState is AuthUiState.Loading,
                            errorMessage = (uiState as? AuthUiState.Error)?.message,
                        )
                    }

                    // ── BECOME A DRIVER FLOW ───────────────────────────────────
                    composable("driver_step_1") {
                        DriverStep1Screen(
                            onBackClick  = { navController.popBackStack() },
                            onNextClick  = { step1Data ->
                                if (driverApplicationViewModel.saveStep1(step1Data)) {
                                    navController.navigate("driver_step_2")
                                }
                            },
                            errorMessage = driverApplicationViewModel.step1ErrorMessage
                        )
                    }
                    composable("driver_step_2") {
                        DriverStep2Screen(
                            onBackClick  = { navController.popBackStack() },
                            onNextClick  = { step2Data ->
                                if (driverApplicationViewModel.saveStep2(step2Data)) {
                                    navController.navigate("driver_step_3")
                                }
                            },
                            errorMessage = driverApplicationViewModel.step2ErrorMessage
                        )
                    }
                    composable("driver_step_3") {
                        DriverStep3Screen(
                            onBackClick   = { navController.popBackStack() },
                            onSubmitClick = { step3Data ->
                                driverApplicationViewModel.submitApplication(step3Data)
                                navController.navigate("student_driver_home") {
                                    popUpTo("driver_step_1") { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            errorMessage  = driverApplicationViewModel.step3ErrorMessage,
                            statusMessage = when (val s = driverApplicationViewModel.submitStatus) {
                                is DriverApplicationSubmitStatus.Success -> s.message
                                is DriverApplicationSubmitStatus.Error   -> s.message
                                else -> null
                            }
                        )
                    }

                    // ── OFFER RIDE ─────────────────────────────────────────────
                    composable("offer_ride") {
                        val submitStatus = offerRideViewModel.submitStatus
                        OfferRideScreen(
                            onPostRideClick = { request -> offerRideViewModel.postRide(request) },
                            errorMessage    = offerRideViewModel.errorMessage,
                            statusMessage   = when (submitStatus) {
                                is UseCaseSubmitStatus.Loading -> "Posting ride..."
                                is UseCaseSubmitStatus.Success -> submitStatus.message
                                else -> null
                            },
                            onHomeClick      = { navController.navigate("student_driver_home") { launchSingleTop = true } },
                            onOfferRideClick = { navController.navigate("offer_ride") { launchSingleTop = true } },
                            onProfileClick   = { navController.navigate("driver_profile_settings") }
                        )
                    }

                    // ── STUDENT DRIVER HOME ────────────────────────────────────
                    composable("student_driver_home") {
                        var isDriverHomeRefreshing by remember { mutableStateOf(false) }

                        if (isDriverHomeRefreshing) {
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(1200)
                                isDriverHomeRefreshing = false
                            }
                        }

                        StudentDriverHomeScreen(
                            driverName         = "Ayabulela",
                            verificationStatus = "Pending Verification",
                            isRefreshing       = isDriverHomeRefreshing,
                            postedRides        = listOf(
                                StudentDriverPostedRide(
                                    rideId          = "1",
                                    pickupLocation  = "South Campus",
                                    destination     = "North Campus",
                                    date            = "2026-07-01",
                                    time            = "08:30",
                                    availableSeats  = 3,
                                    farePerSeat     = "R20.00",
                                    acceptedStudents = listOf(
                                        RideAcceptedStudent("Lanele Maqina",    "223456789"),
                                        RideAcceptedStudent("Tichaona Mudingwa","224567890"),
                                    )
                                )
                            ),
                            onRefreshClick   = { isDriverHomeRefreshing = true },
                            onHomeClick      = { navController.navigate("student_driver_home") { launchSingleTop = true } },
                            onOfferRideClick = { navController.navigate("offer_ride") },
                            onProfileClick   = { navController.navigate("driver_profile_settings") }
                        )
                    }

                    // ── DRIVER PROFILE SETTINGS ────────────────────────────────
                    composable("driver_profile_settings") {
                        val submitStatus = deleteDriverProfileViewModel.submitStatus
                        DriverProfileSettingsScreen(
                            profileDetails = DriverProfileDetails(
                                firstName                  = "Ayabulela",
                                surname                    = "Mtwesi",
                                studentNumber              = "223456789",
                                contactNumber              = "071 234 5678",
                                universityEmail            = "ayabulela@mandela.ac.za",
                                vehicleMake                = "Toyota",
                                vehicleModel               = "Corolla",
                                vehicleRegistrationNumber  = "ABC 123 EC",
                                vehicleColour              = "White",
                                seatingCapacity            = 4,
                                verificationStatus         = "Pending Verification",
                                driversLicenceStatus       = "Uploaded",
                                vehicleRegistrationStatus  = "Uploaded"
                            ),
                            onConfirmDeleteClick = { deleteDriverProfileViewModel.deactivateProfile() },
                            statusMessage = when (submitStatus) {
                                is UseCaseSubmitStatus.Loading -> "Deleting driver profile..."
                                is UseCaseSubmitStatus.Success -> submitStatus.message
                                else -> null
                            },
                            errorMessage = when (submitStatus) {
                                is UseCaseSubmitStatus.Error -> submitStatus.message
                                else -> null
                            },
                            onHomeClick      = { navController.navigate("student_driver_home") { launchSingleTop = true } },
                            onOfferRideClick = { navController.navigate("offer_ride") { launchSingleTop = true } },
                            onProfileClick   = { navController.navigate("driver_profile_settings") { launchSingleTop = true } }
                        )
                    }

                    // ── CARPOOL HOME (self-funded students) ────────────────────
                    composable(GyrRoutes.HOME) {
                        LaunchedEffect(Unit) {
                            rideViewModel.loadAvailableTrips()
                        }
                        CarpoolHomeScreen(
                            uiState       = rideViewModel.uiState,
                            onRetry       = { rideViewModel.loadAvailableTrips() },
                            onBookRide    ={ tripId -> navController.navigate("request_ride/$tripId")},
                            navController = navController,
                        )
                    }

                    // ── SHUTTLE HOME (NSFAS students) ──────────────────────────
                    // TODO: replace placeholder with real ShuttleHomeScreen
                    composable("shuttle_home") {
                        Column(
                            modifier            = Modifier.padding(24.dp),
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
                    ///Request ride Screen
                    composable("request_ride/{tripId}") { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L

                        val trip = (rideViewModel.uiState as? TripsUiState.Success)
                            ?.trips
                            ?.find { it.tripId == tripId }

                        if (trip == null) {
                            // Handles process death / deep link / stale state — bail out
                            // instead of crashing RequestRideScreen with a null ride
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        } else {
                            RequestRideScreen(
                                ride             = trip.toRideRequestDetails(),
                                onBackClick      = { navController.popBackStack() },
                                onAddStopClick   = { navController.navigate("add_stop/$tripId") },
                                onConfirmRequest = { seats, notes ->
                                    // TODO: call booking endpoint
                                },
                                onCancel         = { navController.popBackStack() },
                            )
                        }
                    }
                    // ── ADD A STOP ─────────────────────────────────────────────
                    composable("add_stop/{tripId}") { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L

                        val stopSearchViewModel: StopSearchViewModel = viewModel(
                            factory = StopSearchViewModelFactory(
                                GeocodingRepository(NetworkModule.geocodingApi)
                            )
                        )

                        AddStopScreen(
                            navController = navController,
                            tripId = tripId,
                            viewModel = stopSearchViewModel,
                        )
                    }

                    // ── MY RIDES (Rides tab) ───────────────────────────────────
                    // CHANGED: now uses rideViewModel instead of a static list.
                    // The same rideViewModel instance is shared with CarpoolHomeScreen
                    // above, so the data is already loaded — no extra API call on tab switch.
                    composable(GyrRoutes.RIDES) {
                        MyRidesScreen(
                            viewModel = allRidesViewModel,
                            navController = navController,
                            onTrackRide   = { rideId ->
                                // TODO: navigate to live tracking screen once built
                                // navController.navigate("track_ride/$rideId")
                            },

                        )
                    }
                }
            }
        }
    }
}

private fun homeRouteFor(isNsfasFunded: Boolean): String {
    return if (isNsfasFunded) "shuttle_home" else GyrRoutes.HOME
}