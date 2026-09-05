package com.example.getyourride
import com.example.getyourride.viewmodel.TripBookingViewModel
import com.example.getyourride.viewmodel.TripBookingViewModelFactory
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.getyourride.data.repository.TripRepository
import com.example.getyourride.viewmodel.RideViewModel
import com.example.getyourride.viewmodel.RideViewModelFactory
import android.os.Bundle
import com.example.getyourride.UserSession
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import com.example.getyourride.data.repository.DriverApplicationRepository
import com.example.getyourride.data.repository.StudentAuthRepository
import com.example.getyourride.di.NetworkModule
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
import com.example.getyourride.viewmodel.DriverApplicationViewModel
import com.example.getyourride.viewmodel.DriverApplicationViewModelFactory
import com.example.getyourride.viewmodel.DriverProfileViewModel
import com.example.getyourride.viewmodel.DriverProfileViewModelFactory
import com.example.getyourride.viewmodel.DriverProfileUiState
import com.example.getyourride.viewmodel.DriverDeleteUiState
import com.example.getyourride.viewmodel.DocumentUploadUiState
import com.example.getyourride.viewmodel.DriverHomeViewModel
import com.example.getyourride.viewmodel.DriverHomeViewModelFactory
import com.example.getyourride.viewmodel.OfferRideViewModel
import com.example.getyourride.viewmodel.StompRideLocationSocket
import com.example.getyourride.viewmodel.TrackingViewModel
import com.example.getyourride.viewmodel.TrackingViewModelFactory
import com.example.getyourride.ui.screens.Tracking.TrackingScreen
import com.example.getyourride.ui.screens.StudentDriverHomeScreen
import com.example.getyourride.ui.screens.DriverProfileDetails
import com.example.getyourride.ui.screens.Rides.MyRidesScreen
import com.example.getyourride.ui.screens.Rides.RequestRideScreen
import com.example.getyourride.ui.screens.Rides.RideRequestDetails
import com.example.getyourride.ui.screens.Rides.BookingConfirmationDetails
import com.example.getyourride.ui.screens.Rides.BookingConfirmedScreen
import com.example.getyourride.ui.screens.Rides.toBookingConfirmationDetails
import com.example.getyourride.viewmodel.AllRidesViewModel
import com.example.getyourride.viewmodel.AllRidesViewModelFactory
import com.example.getyourride.viewmodel.AllTripsUiState
import com.example.getyourride.viewmodel.StopSearchViewModel
import com.example.getyourride.viewmodel.StopSearchViewModelFactory
import com.example.getyourride.viewmodel.TripsUiState
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import com.example.getyourride.data.repository.ShuttleRepository
import com.example.getyourride.data.repository.ShuttleDriverRepository
import com.example.getyourride.ui.screens.shuttle.ShuttleHomeScreen
import com.example.getyourride.ui.screens.shuttle.UpcomingShuttle
import com.example.getyourride.ui.screens.shuttle.RecentTrip
import com.example.getyourride.ui.screens.shuttle.BookShuttleScreen
import com.example.getyourride.ui.screens.shuttle.ShuttleStopSelectionScreen
import com.example.getyourride.ui.screens.shuttle.BookingConfirmation
import com.example.getyourride.ui.screens.shuttle.BookingConfirmationScreen
import com.example.getyourride.ui.screens.profiles.ProfileScreen
import com.example.getyourride.viewmodel.ProfileViewModel
import com.example.getyourride.viewmodel.ScheduleRideViewModel
import com.example.getyourride.viewmodel.ScheduleRideViewModelFactory
import com.example.getyourride.viewmodel.ShuttleStopSearchViewModel
import com.example.getyourride.viewmodel.ShuttleStopSearchViewModelFactory
import com.example.getyourride.viewmodel.ShuttleUiState
import com.example.getyourride.viewmodel.ShuttleViewModel
import com.example.getyourride.viewmodel.ShuttleViewModelFactory
import com.example.getyourride.viewmodel.ShuttleDriverProfileViewModel
import com.example.getyourride.viewmodel.ShuttleDriverProfileViewModelFactory
import com.example.getyourride.viewmodel.ShuttleDriverBoardingViewModel
import com.example.getyourride.viewmodel.ShuttleDriverBoardingViewModelFactory
import com.example.getyourride.ui.screens.shuttleDriver.ShuttleDriverBoardingScreen
import com.example.getyourride.ui.screens.shuttleDriver.ShuttleDriverProfileScreen
import com.example.getyourride.ui.screens.shuttleDriver.ShuttleDriverScanQrScreen


class MainActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()

                // ── Existing mock API service (delete profile) ─────────────
                val offerRideViewModel = remember {
                    OfferRideViewModel(
                        tripApi = NetworkModule.tripApi,
                        geocodingRepository = GeocodingRepository(NetworkModule.geocodingApi)
                    )
                }

                // ── Real driver application — uses Retrofit + auto-login ──────
                val driverApplicationRepository = remember {
                    DriverApplicationRepository(api = NetworkModule.driverApplicationApi)
                }
                val driverApplicationViewModel: DriverApplicationViewModel = viewModel(
                    factory = DriverApplicationViewModelFactory(driverApplicationRepository)
                )

                // ── Real auth — talks to StudentAuthController on :8080 ───────
                val studentAuthRepository = remember {
                    StudentAuthRepository(
                        api = NetworkModule.studentAuthApi,
                        shuttleDriverApi = NetworkModule.shuttleDriverApi
                    )
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

                // NEW — holds the details for BookingConfirmedScreen. Set right
                // before navigating to "booking_confirmed" and cleared once the
                // student leaves that screen via "View My Rides".
                var confirmedBooking by remember { mutableStateOf<BookingConfirmationDetails?>(null) }
                var confirmedShuttle by remember { mutableStateOf<BookingConfirmation?>(null) }

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
                                navController.navigate(homeRouteFor(uiState.response)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        LoginScreen(
                            onCreateAccountClick = { navController.navigate("signup") },
                            onBecomeDriverClick  = { navController.navigate("driver_step_1") },
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
                                navController.navigate(homeRouteFor(uiState.response)) {
                                    popUpTo("login") { inclusive = true }
                                }
                                authViewModel.resetState()
                            }
                        }

                        SignUpScreen(
                            onBackClick         = { navController.popBackStack() },
                            onLoginClick        = { navController.popBackStack() },
                            onBecomeDriverClick = { navController.navigate("driver_step_1") },
                            onSignUpClick = { firstName, lastName, studentNumber, email, password, isFunded ->
                                authViewModel.register(
                                    studentNumber = studentNumber,
                                    firstName     = firstName,
                                    lastName      = lastName,
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
                    // Inside NavHost -> composable("driver_step_3")
                    composable("driver_step_3") {
                        val context = LocalContext.current

                        // Auto-navigate when submission succeeds with auto-login
                        LaunchedEffect(driverApplicationViewModel.submitStatus) {
                            val status = driverApplicationViewModel.submitStatus
                            if (status is DriverApplicationSubmitStatus.Success) {
                                // Save the JWT + user info from auto-login response
                                val authResponse = status.authResponse
                                if (authResponse != null) {
                                    UserSession.save(authResponse)
                                }

                                // Navigate to Driver Home — no second login needed
                                navController.navigate("student_driver_home") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                        DriverStep3Screen(
                            onBackClick   = { navController.popBackStack() },
                            onSubmitClick = { step3Data ->
                                // Pass the contentResolver so the ViewModel can read the image bytes
                                driverApplicationViewModel.submitApplication(step3Data, context.contentResolver)
                            },
                            errorMessage  = driverApplicationViewModel.step3ErrorMessage,
                            statusMessage = (driverApplicationViewModel.submitStatus as? DriverApplicationSubmitStatus.Success)?.message,
                            isLoading     = driverApplicationViewModel.isSubmitting
                        )
                    }

                    // ── OFFER RIDE (Student Driver)─────────────────────────────────────────────
                    composable("offer_ride") {
                        val submitStatus = offerRideViewModel.submitStatus
                        val pickupState by offerRideViewModel.pickup.collectAsState()
                        val destinationState by offerRideViewModel.destination.collectAsState()

                        // Navigate to home after successful ride posting
                        LaunchedEffect(submitStatus) {
                            if (submitStatus is UseCaseSubmitStatus.Success) {
                                kotlinx.coroutines.delay(800) // Brief delay so user sees success message
                                navController.navigate("student_driver_home") {
                                    popUpTo("offer_ride") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }

                        OfferRideScreen(
                            isDriverVerified = UserSession.canPerformDriverActions,
                            pickupState = pickupState,
                            destinationState = destinationState,
                            onPickupTextChanged = { text -> offerRideViewModel.onPickupTextChanged(text) },
                            onPickupSuggestionSelected = { suggestion -> offerRideViewModel.onPickupSuggestionSelected(suggestion) },
                            onDestinationTextChanged = { text -> offerRideViewModel.onDestinationTextChanged(text) },
                            onDestinationSuggestionSelected = { suggestion -> offerRideViewModel.onDestinationSuggestionSelected(suggestion) },
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
                        val driverHomeViewModel: DriverHomeViewModel = viewModel(
                            factory = DriverHomeViewModelFactory(
                                TripRepository(NetworkModule.tripApi)
                            )
                        )

                        LaunchedEffect(Unit) {
                            driverHomeViewModel.loadMyTrips()
                        }

                        val driverName = UserSession.firstName ?: "Driver"
                        val verificationStatus = when {
                            UserSession.isDriverPending  -> "Pending Review"
                            UserSession.isDriverApproved -> "Approved"
                            else                         -> "Pending Review"
                        }

                        StudentDriverHomeScreen(
                            driverName         = driverName,
                            verificationStatus = verificationStatus,
                            homeUiState        = driverHomeViewModel.uiState,
                            onRefreshClick     = { driverHomeViewModel.loadMyTrips() },
                            onHomeClick        = { navController.navigate("student_driver_home") { launchSingleTop = true } },
                            onOfferRideClick   = { navController.navigate("offer_ride") },
                            onProfileClick     = { navController.navigate("driver_profile_settings") },
                            onCancelRide       = { tripId ->
                                driverHomeViewModel.cancelRide(tripId)
                            },
                            onStartRide          = { tripId -> driverHomeViewModel.startRide(tripId) },
                            startingTripId       = driverHomeViewModel.startingTripId,
                            actionMessage        = driverHomeViewModel.actionMessage,
                            onActionMessageShown = { driverHomeViewModel.consumeActionMessage() }
                        )
                    }

                    // ── DRIVER PROFILE SETTINGS(Student Driver) ────────────────────────────────
                    composable("driver_profile_settings") {
                        val context = LocalContext.current
                        val driverProfileViewModel: DriverProfileViewModel = viewModel(
                            factory = DriverProfileViewModelFactory(driverApplicationRepository)
                        )

                        LaunchedEffect(Unit) {
                            driverProfileViewModel.loadProfile()
                        }

                        // File pickers for document upload from profile
                        val licencePicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri: android.net.Uri? ->
                            if (uri != null) {
                                // Persist read permission so URI survives process restarts
                                runCatching {
                                    context.contentResolver.takePersistableUriPermission(
                                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                                driverProfileViewModel.uploadDocument(
                                    documentType = "DriversLicence",
                                    uri = uri,
                                    contentResolver = context.contentResolver
                                )
                            }
                        }

                        val registrationPicker = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.OpenDocument()
                        ) { uri: android.net.Uri? ->
                            if (uri != null) {
                                // Persist read permission so URI survives process restarts
                                runCatching {
                                    context.contentResolver.takePersistableUriPermission(
                                        uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                }
                                driverProfileViewModel.uploadDocument(
                                    documentType = "VehicleRegistration",
                                    uri = uri,
                                    contentResolver = context.contentResolver
                                )
                            }
                        }

                        when (val profileState = driverProfileViewModel.profileState) {
                            is DriverProfileUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is DriverProfileUiState.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Could not load profile: ${profileState.message}")
                                        Button(onClick = { driverProfileViewModel.loadProfile() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                            is DriverProfileUiState.Success -> {
                                val profile = profileState.profile
                                val deleteState = driverProfileViewModel.deleteState

                                // Navigate to login after successful deletion
                                LaunchedEffect(deleteState) {
                                    if (deleteState is DriverDeleteUiState.Success) {
                                        UserSession.clear()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                }

                                DriverProfileSettingsScreen(
                                    profileDetails = DriverProfileDetails(
                                        firstName                 = profile.firstName,
                                        surname                   = profile.surname,
                                        studentNumber             = profile.studentNumber,
                                        contactNumber             = profile.contactNumber,
                                        universityEmail           = profile.email,
                                        vehicleMake               = profile.vehicleMake,
                                        vehicleModel              = profile.vehicleModel,
                                        vehicleRegistrationNumber = profile.registrationNumber,
                                        vehicleColour             = profile.vehicleColour,
                                        seatingCapacity           = profile.seatingCapacity,
                                        verificationStatus        = profile.applicationStatus,
                                        driversLicenceStatus      = profile.driversLicenceStatus,
                                        vehicleRegistrationStatus = profile.vehicleRegistrationStatus
                                    ),
                                    onConfirmDeleteClick = { driverProfileViewModel.deleteProfile() },
                                    onUploadLicence = { licencePicker.launch(arrayOf("image/*")) },
                                    onUploadRegistration = { registrationPicker.launch(arrayOf("image/*")) },
                                    statusMessage = when {
                                        driverProfileViewModel.uploadState is DocumentUploadUiState.Success ->
                                            (driverProfileViewModel.uploadState as DocumentUploadUiState.Success).message
                                        driverProfileViewModel.uploadState is DocumentUploadUiState.Uploading ->
                                            "Uploading document..."
                                        deleteState is DriverDeleteUiState.Loading -> "Deleting driver profile..."
                                        deleteState is DriverDeleteUiState.Success -> (deleteState as DriverDeleteUiState.Success).message
                                        else -> null
                                    },
                                    errorMessage = when {
                                        driverProfileViewModel.uploadState is DocumentUploadUiState.Error ->
                                            (driverProfileViewModel.uploadState as DocumentUploadUiState.Error).message
                                        deleteState is DriverDeleteUiState.Error -> (deleteState as DriverDeleteUiState.Error).message
                                        else -> null
                                    },
                                    onHomeClick      = { navController.navigate("student_driver_home") { launchSingleTop = true } },
                                    onOfferRideClick = { navController.navigate("offer_ride") { launchSingleTop = true } },
                                    onProfileClick   = { navController.navigate("driver_profile_settings") { launchSingleTop = true } },
                                    onLogoutClick    = {
                                        UserSession.clear()
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // ── CARPOOL HOME (self-funded students) ────────────────────
                    composable(GyrRoutes.HOME) {
                        LaunchedEffect(Unit) {
                            if (rideViewModel.uiState is TripsUiState.Loading) {
                                rideViewModel.loadAvailableTrips()
                            }
                        }
                        CarpoolHomeScreen(
                            uiState       = rideViewModel.uiState,
                            onRetry       = { rideViewModel.loadAvailableTrips() },
                            onBookRide    ={ tripId -> navController.navigate("request_ride/$tripId")},
                            onSearchRides = { pickup, destination ->
                                rideViewModel.searchTrips(
                                    pickupLat       = pickup.latitude,
                                    pickupLng       = pickup.longitude,
                                    destinationLat  = destination.latitude,
                                    destinationLng  = destination.longitude,
                                )
                            },
                            onNotifications = { /* TODO: notifications screen */ },
                            navController = navController,
                        )
                    }

                    // ── PROFILE ────────────────────────────────────────────────
                    composable(GyrRoutes.PROFILE) {
                        ProfileScreen(
                            onEditProfile = { /* TODO: edit profile screen */ },
                            onLoggedOut = {
                                UserSession.clear()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            navController = navController
                        )
                    }

                    // ── SHUTTLE HOME (NSFAS students) ──────────────────────────
                    composable("shuttle_home") {
                        val context = LocalContext.current
                        val shuttleViewModel: ShuttleViewModel = viewModel(
                            factory = ShuttleViewModelFactory(
                                ShuttleRepository(
                                    api = NetworkModule.shuttleApi,
                                    tripApi = NetworkModule.tripApi
                                )
                            )
                        )

                        LaunchedEffect(Unit) {
                            shuttleViewModel.loadShuttleHomeData()
                        }

                        when (val state = shuttleViewModel.uiState) {
                            is ShuttleUiState.Loading -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                            is ShuttleUiState.Error -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("Couldn't load shuttles: ${state.message}")
                                        Button(onClick = { shuttleViewModel.loadShuttleHomeData() }) {
                                            Text("Retry")
                                        }
                                    }
                                }
                            }
                            is ShuttleUiState.Success -> {
                                ShuttleHomeScreen(
                                    userName = "Student", // TODO: pull real name once UserSession exposes it
                                    upcomingShuttles = state.upcomingShuttles,
                                    recentTrips = state.recentTrips,
                                    navController = navController,
                                    onBookShuttle = {
                                        navController.navigate("book_shuttle")
                                    },
                                    onFabClick = {
                                        navController.navigate("book_shuttle")
                                    },
                                    onViewAllShuttles = {
                                        Toast.makeText(context, "View All Shuttles — screen coming soon", Toast.LENGTH_SHORT).show()
                                    },

                                    onShowTicket = { shuttle ->
                                        confirmedShuttle = BookingConfirmation(
                                            shuttleId = shuttle.tripId,
                                            // Stable per booking: derived from tripId, not random,
                                            // so the QR code doesn't change every time this screen opens.
                                            ticketId = "GYR-" + shuttle.tripId,
                                            pickupLocation = shuttle.from,
                                            dropoffLocation = shuttle.to,
                                            date = shuttle.date,
                                            departureTime = shuttle.time,
                                            driverName = shuttle.driverName ?: "S. Mokoena",
                                            plateNumber = shuttle.plateNumber ?: "BS 42 GP",
                                            vehicleModel = shuttle.vehicleModel ?: "Mercedes Sprinter",
                                            status = shuttle.status
                                        )
                                        navController.navigate("shuttle_booking_confirmed")
                                    },
                                    onTripClick = { trip ->
                                        Toast.makeText(context, "Trip details: ${trip.from} → ${trip.to} — screen coming soon", Toast.LENGTH_SHORT).show()
                                    },
                                )
                            }
                        }
                    }

                    // ── SHUTTLE RIDES ──────────────────────────────────────────
                    composable(GyrRoutes.SHUTTLE_RIDES) {
                        LaunchedEffect(Unit) {
                            if (allRidesViewModel.uiState is AllTripsUiState.Loading) {
                                allRidesViewModel.loadAllTrips()
                            }
                        }
                        MyRidesScreen(
                            viewModel = allRidesViewModel,
                            navController = navController,
                            currentRoute = GyrRoutes.SHUTTLE_RIDES
                        )
                    }

                    ///Request ride Screen
                    composable("request_ride/{tripId}") { backStackEntry ->
                        val tripId = backStackEntry.arguments?.getString("tripId")?.toLongOrNull() ?: 0L

                        val trip = (rideViewModel.uiState as? TripsUiState.Success)
                            ?.trips
                            ?.find { it.tripId == tripId }

                        // STASH the ride details so the screen doesn't disappear if the list
                        // refreshes (e.g. on booking success).
                        var stashedRide by remember(tripId) { mutableStateOf<RideRequestDetails?>(null) }
                        if (trip != null) {
                            stashedRide = trip.toRideRequestDetails()
                        }

                        if (stashedRide == null) {
                            // Truly nothing to show. Pop back only if not currently loading.
                            if (rideViewModel.uiState !is TripsUiState.Loading) {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        } else {
                            val rideDetails = stashedRide!!
                            val tripBookingViewModel: TripBookingViewModel = viewModel(
                                factory = TripBookingViewModelFactory(
                                    tripId,
                                    TripRepository(NetworkModule.tripApi)
                                )
                            )

                            RequestRideScreen(
                                ride             = rideDetails,
                                bookingViewModel = tripBookingViewModel,
                                navController    = navController,
                                onBackClick      = { navController.popBackStack() },
                                onAddStopClick   = { navController.navigate("add_stop/$tripId") },
                                onBookingSuccess = { confirmedTrip ->
                                    // Refresh both lists so MyRidesScreen and CarpoolHomeScreen
                                    // reflect the seat that just got taken.
                                    rideViewModel.loadAvailableTrips()

                                    // Stash the confirmed booking's display details
                                    confirmedBooking = confirmedTrip.toRideRequestDetails()
                                        .toBookingConfirmationDetails()

                                    android.util.Log.d("NAV", "Booking success! Navigating to confirmed. Data: $confirmedBooking")

                                    // Reset ViewModel state so it doesn't trigger again on re-entry
                                    tripBookingViewModel.resetState()

                                    navController.navigate("booking_confirmed") {
                                        popUpTo(GyrRoutes.HOME)
                                    }
                                },
                                onCancel = { navController.popBackStack() },
                            )
                        }
                    }

                    // ── BOOK SHUTTLE ──────────────────────────────────────────
                    composable("book_shuttle") {
                        val context = LocalContext.current
                        val shuttleViewModel: ScheduleRideViewModel = viewModel(
                            factory = ScheduleRideViewModelFactory(
                                ShuttleRepository(
                                    api = NetworkModule.shuttleApi,
                                    tripApi = NetworkModule.tripApi
                                )
                            )
                        )

                        BookShuttleScreen(
                            navController = navController,
                            viewModel = shuttleViewModel,
                            onPickPickup = {
                                navController.navigate("add_stop_shuttle/pickup")
                            },
                            onPickDestination = {
                                navController.navigate("add_stop_shuttle/destination")
                            },
                            onBookingConfirmed = {
                                val uiState = shuttleViewModel.uiState.value
                                val trip = uiState.lastBookedTrip
                                confirmedShuttle = BookingConfirmation(
                                    shuttleId = trip?.tripId?.toString() ?: "SH-102",
                                    // Stable per booking: derived from tripId, not random,
                                    // so the QR code doesn't change every time this screen opens.
                                    ticketId = "GYR-" + (trip?.tripId?.toString() ?: (1000..9999).random().toString()),
                                    pickupLocation = uiState.pickupLabel,
                                    dropoffLocation = uiState.destinationLabel,
                                    date = "Today",
                                    departureTime = uiState.selectedTime ?: "08:30",
                                    driverName = trip?.driverName ?: "S. Mokoena",
                                    plateNumber = trip?.registrationNumber ?: "BS 42 GP",
                                    vehicleModel = trip?.vehicleModel ?: "Mercedes Sprinter"
                                )
                                navController.navigate("shuttle_booking_confirmed") {
                                    popUpTo("shuttle_home")
                                }
                            }
                        )
                    }

                    composable("shuttle_booking_confirmed") {
                        val booking = confirmedShuttle
                        val context = LocalContext.current
                        if (booking == null) {
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        } else {
                            BookingConfirmationScreen(
                                navController = navController,
                                booking = booking,
                                onViewMyRides = {
                                    confirmedShuttle = null
                                    navController.navigate(GyrRoutes.SHUTTLE_RIDES) {
                                        popUpTo("shuttle_home")
                                    }
                                },
                                onDownloadTicket = {
                                    Toast.makeText(context, "Downloading Ticket...", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }

                    // ── ADD A STOP (SHUTTLE) ───────────────────────────────────
                    composable("add_stop_shuttle/{type}") { backStackEntry ->
                        val type = backStackEntry.arguments?.getString("type") ?: "pickup"

                        val shuttleStopSearchViewModel: ShuttleStopSearchViewModel = viewModel(
                            factory = ShuttleStopSearchViewModelFactory(
                                ShuttleRepository(
                                    api = NetworkModule.shuttleApi,
                                    tripApi = NetworkModule.tripApi
                                )
                            )
                        )

                        val bookShuttleEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("book_shuttle")
                        }
                        val shuttleViewModel: ScheduleRideViewModel = viewModel(
                            viewModelStoreOwner = bookShuttleEntry,
                            factory = ScheduleRideViewModelFactory(
                                ShuttleRepository(
                                    api = NetworkModule.shuttleApi,
                                    tripApi = NetworkModule.tripApi
                                )
                            )
                        )

                        ShuttleStopSelectionScreen(
                            navController = navController,
                            viewModel = shuttleStopSearchViewModel,
                            onStopSelected = { stop ->
                                if (type == "pickup") {
                                    shuttleViewModel.updatePickup(stop)
                                } else {
                                    shuttleViewModel.updateDestination(stop)
                                }
                            }
                        )
                    }

                    // ── BOOKING CONFIRMED ───────────────────────────────────────
                    composable("booking_confirmed") {
                        val booking = confirmedBooking
                        android.util.Log.d("NAV", "booking_confirmed composed, booking=$booking")
                        if (booking == null) {
                            // Defensive guard for process death / deep link — sameb
                            // pattern as the null-trip check in request_ride/{tripId}.
                            LaunchedEffect(Unit) { navController.popBackStack() }
                        } else {
                            BookingConfirmedScreen(
                                details = booking,
                                onDownloadReceipt = {
                                    // PDF/summary generation exists
                                },
                                onViewMyRides = {
                                    confirmedBooking = null
                                    navController.navigate(GyrRoutes.RIDES) {
                                        popUpTo(GyrRoutes.HOME)
                                    }
                                },
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

                        // Grabs the SAME TripBookingViewModel instance the request_ride screen
                        // is using, scoped to that entry's ViewModelStoreOwner rather than this
                        // one — otherwise a plain viewModel() call here would create a second,
                        // disconnected TripBookingViewModel and the picked stop would vanish
                        // when we pop back.
                        val requestRideEntry = remember(backStackEntry) {
                            navController.getBackStackEntry("request_ride/$tripId")
                        }
                        val tripBookingViewModel: TripBookingViewModel = viewModel(
                            viewModelStoreOwner = requestRideEntry,
                            factory = TripBookingViewModelFactory(
                                tripId,
                                TripRepository(NetworkModule.tripApi)
                            )
                        )

                        AddStopScreen(
                            navController = navController,
                            tripId = tripId,
                            viewModel = stopSearchViewModel,
                            onStopChosen = { stop ->
                                tripBookingViewModel.choosePickupStop(stop)   // was setPickupStop
                                navController.popBackStack()
                            }
                        )
                    }

                    // ── MY RIDES (Rides tab) ───────────────────────────────────
                    // CHANGED: now uses rideViewModel instead of a static list.
                    // The same rideViewModel instance is shared with CarpoolHomeScreen
                    // above, so the data is already loaded — no extra API call on tab switch.

                    composable(GyrRoutes.RIDES) {
                        LaunchedEffect(Unit) {
                            if (allRidesViewModel.uiState is AllTripsUiState.Loading) {
                                allRidesViewModel.loadAllTrips()
                            }
                        }
                        MyRidesScreen(
                            viewModel = allRidesViewModel,
                            navController = navController,
                            onTrackRide   = { rideId ->
                                navController.navigate("track/$rideId")
                            },
                        )
                    }

                    // ── TRACK RIDE ─────────────────────────────────────────────
                    // Route for the bottom nav tab (no ID)
                    composable(GyrRoutes.TRACK) {
                        // No trip id from this entry point, so pass null and let the ViewModel
                        // resolve the student's active booking from the backend. It shows the
                        // "no rides to track" empty state when there isn't one — previously this
                        // route hardcoded rideId "0" + MockRideLocationSocket, which is why the
                        // tab always displayed fake driver/vehicle data.
                        val socket = remember { StompRideLocationSocket() }
                        val trackingViewModel: TrackingViewModel = viewModel(
                            factory = TrackingViewModelFactory(
                                rideId = null,
                                socket = socket,
                                tripRepository = TripRepository(NetworkModule.tripApi)
                            )
                        )
                        TrackingScreen(
                            viewModel = trackingViewModel,
                            navController = navController,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // Route for direct tracking from My Rides (with ID)
                    composable("track/{rideId}") { backStackEntry ->
                        val rideId = backStackEntry.arguments?.getString("rideId")
                        // This route always has a real trip id, so it uses the real STOMP socket.
                        // Was previously hardcoded to the mock ("useRealSocket = false") here too,
                        // which meant live tracking never actually connected to the backend.
                        val socket = remember { StompRideLocationSocket() }
                        val trackingViewModel: TrackingViewModel = viewModel(
                            factory = TrackingViewModelFactory(
                                rideId = rideId,
                                socket = socket,
                                tripRepository = TripRepository(NetworkModule.tripApi)
                            )
                        )
                        TrackingScreen(
                            viewModel = trackingViewModel,
                            navController = navController,
                            onBackClick = { navController.popBackStack() }
                        )
                    }

                    // ── SHUTTLE DRIVER: BOARDING (Home) ────────────────────────
                    composable("shuttle_driver_boarding") {
                        val boardingViewModel: ShuttleDriverBoardingViewModel = viewModel(
                            factory = ShuttleDriverBoardingViewModelFactory(
                                ShuttleDriverRepository(NetworkModule.shuttleDriverApi),
                                NetworkModule.tripApi,
                                NetworkModule.shuttleApi
                            )
                        )

                        ShuttleDriverBoardingScreen(
                            uiState = boardingViewModel.uiState,
                            markingBookingId = boardingViewModel.markingBookingId,
                            onLoadData = { boardingViewModel.loadBoardingData() },
                            onMarkAsBoarded = { bookingId ->
                                boardingViewModel.markStudentAsBoarded(bookingId)
                            },
                            onSelectTimeSlot = { slot ->
                                boardingViewModel.selectTimeSlot(slot)
                            },
                            onScanQrCodeClick = {
                                navController.navigate("shuttle_driver_scan_qr") { launchSingleTop = true }
                            },
                            onBoardingClick = { /* already here */ },
                            onProfileClick = {
                                navController.navigate("shuttle_driver_profile") { launchSingleTop = true }
                            }
                        )
                    }

                    // ── SHUTTLE DRIVER: SCAN QR ───────────────────────────────
                    composable("shuttle_driver_scan_qr") {
                        ShuttleDriverScanQrScreen(
                            onScanQrCodeClick = { /* already here */ },
                            onBoardingClick = {
                                navController.navigate("shuttle_driver_boarding") { launchSingleTop = true }
                            },
                            onProfileClick = {
                                navController.navigate("shuttle_driver_profile") { launchSingleTop = true }
                            }
                        )
                    }

                    // ── SHUTTLE DRIVER: PROFILE ───────────────────────────────
                    composable("shuttle_driver_profile") {
                        val shuttleDriverProfileViewModel: ShuttleDriverProfileViewModel = viewModel(
                            factory = ShuttleDriverProfileViewModelFactory(
                                ShuttleDriverRepository(NetworkModule.shuttleDriverApi)
                            )
                        )

                        ShuttleDriverProfileScreen(
                            uiState = shuttleDriverProfileViewModel.uiState,
                            onLoadProfile = { shuttleDriverProfileViewModel.loadProfile() },
                            onLogoutClick = {
                                UserSession.clear()
                                navController.navigate("login") {
                                    popUpTo(0) { inclusive = true }
                                }
                            },
                            onScanQrCodeClick = {
                                navController.navigate("shuttle_driver_scan_qr") { launchSingleTop = true }
                            },
                            onBoardingClick = {
                                navController.navigate("shuttle_driver_boarding") { launchSingleTop = true }
                            },
                            onProfileClick = { /* already here */ }
                        )
                    }
                }
            }
        }
    }
}

private fun homeRouteFor(response: com.example.getyourride.data.remote.dto.AuthResponse): String {
    // Shuttle drivers go to boarding screen (their home page)
    if (response.type == "SHUTTLE_DRIVER") return "shuttle_driver_boarding"
    if (response.role == "SHUTTLE_DRIVER") return "shuttle_driver_boarding"
    // Student drivers go to driver home
    if (response.type == "DRIVER") return "student_driver_home"
    // NSFAS-funded students go to shuttle home
    if (response.isFunded == true) return "shuttle_home"
    // Self-funded students go to carpool home
    return GyrRoutes.HOME
}