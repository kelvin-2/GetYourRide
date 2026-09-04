package com.example.getyourride.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.remote.dto.TripStopResponse
import com.example.getyourride.ui.components.StudentDriverBottomBar
import com.example.getyourride.ui.components.StudentDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.DriverHomeUiState
import java.math.BigDecimal

// ── Colors (matching Offer/Profile screens) ─────────────────────────────────
private val DriverBackground = Color(0xFFF4F6FB)
private val DriverPrimary = Color(0xFF1A2E5A)
private val DriverPrimaryLight = Color(0xFF2E4A82)
private val DriverTopBar = Color(0xFF1A2E5A)
private val DriverAccent = Color(0xFFFC820C)
private val DriverAccentLight = Color(0xFFFFA040)
private val DriverCardBackground = Color(0xFFFFFFFF)
private val DriverText = Color(0xFF1B1B1F)
private val DriverTextMuted = Color(0xFF5E6278)
private val DriverBorder = Color(0xFFE5E7EB)
private val DriverPendingBg = Color(0xFFFEF3C7)
private val DriverPendingText = Color(0xFF92400E)
private val DriverSuccessBg = Color(0xFFDCFCE7)
private val DriverSuccessText = Color(0xFF16A34A)
private val DriverPrimaryFixed = Color(0xFFDAE2FF)
private val DriverSoftBorder = Color(0xFFE3E2E6)
private val DriverActiveBg = Color(0xFFE3F2FD)
private val DriverActiveText = Color(0xFF1565C0)
private val DriverCancelledBg = Color(0xFFFFEBEE)
private val DriverCancelledText = Color(0xFFC62828)
private val DriverSectionPurple = Color(0xFF6366F1)
private val DriverSectionGreen = Color(0xFF16A34A)

// ── Data classes (kept for backward compat with previews) ───────────────────
@Immutable
data class StudentDriverPostedRide(
    val rideId: String,
    val pickupLocation: String,
    val destination: String,
    val date: String,
    val time: String,
    val availableSeats: Int,
    val farePerSeat: String,
    val acceptedStudents: List<RideAcceptedStudent>
)

@Immutable
data class RideAcceptedStudent(
    val name: String,
    val studentNumber: String,
    val status: String = "Confirmed"
)

// ── Main Screen Composable ──────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDriverHomeScreen(
    driverName: String = "Driver",
    verificationStatus: String = "Pending Review",
    homeUiState: DriverHomeUiState = DriverHomeUiState.Loading,
    onRefreshClick: () -> Unit = {},
    onOfferRideClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onCancelRide: (Long) -> Unit = {},
    // Tracking-simulation additions. Defaulted so existing callers and previews are unaffected.
    onStartRide: (Long) -> Unit = {},
    startingTripId: Long? = null,
    actionMessage: String? = null,
    onActionMessageShown: () -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }

    // Surface the outcome of starting a trip as a snackbar, then clear it so it shows only once.
    LaunchedEffect(actionMessage) {
        val message = actionMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            onActionMessageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Outlined.DirectionsCar, null, tint = Color.White)
                        Text("GetYourRide", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshClick) {
                        Icon(Icons.Outlined.Refresh, "Refresh", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DriverTopBar)
            )
        },
        bottomBar = {
            StudentDriverBottomBar(
                selectedItem = StudentDriverBottomBarItem.Home,
                onHomeClick = onHomeClick,
                onOfferRideClick = onOfferRideClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = DriverBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Hero Header with Gradient (matching Offer/Profile) ──────────
            HeroHeader(
                driverName = driverName,
                verificationStatus = verificationStatus
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Quick Actions Row ───────────────────────────────────────────
            QuickActionsRow(
                onOfferRideClick = onOfferRideClick,
                onRefreshClick = onRefreshClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Content based on state
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                when (homeUiState) {
                    is DriverHomeUiState.Loading -> {
                        Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = DriverAccent)
                        }
                    }
                    is DriverHomeUiState.Error -> {
                        ErrorCard(message = homeUiState.message, onRetry = onRefreshClick)
                    }
                    is DriverHomeUiState.Success -> {
                        // Active rides section (Uber-style)
                        ActiveRidesSection(
                            activeRides = homeUiState.activeRides,
                            onOfferRideClick = onOfferRideClick,
                            onCancelRide = onCancelRide,
                            onStartRide = onStartRide,
                            startingTripId = startingTripId
                        )
                        // Past rides section
                        PastRidesSection(pastRides = homeUiState.pastRides)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ── Welcome Header ──────────────────────────────────────────────────────────
@Composable
private fun HeroHeader(driverName: String, verificationStatus: String) {
    val isApproved = verificationStatus.contains("Approved", true)
    val (statusBg, statusColor, statusIcon) = when {
        isApproved -> Triple(DriverSuccessBg, DriverSuccessText, Icons.Outlined.CheckCircle)
        verificationStatus.contains("Pending", true) -> Triple(DriverPendingBg, DriverPendingText, Icons.Outlined.Schedule)
        else -> Triple(DriverPendingBg, DriverPendingText, Icons.Outlined.Schedule)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(DriverTopBar, DriverPrimaryLight),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, 400f)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(120.dp)
                .offset(x = (-30).dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.TopEnd)
                .offset(x = 20.dp, y = 40.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = driverName.take(2).uppercase(),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Welcome back,",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = driverName,
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 32.sp
                    )
                }
            }

            // Verification status badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = statusBg.copy(alpha = 0.95f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = statusIcon,
                        contentDescription = null,
                        tint = statusColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Driver Status",
                            color = statusColor.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = verificationStatus,
                            color = statusColor,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.VerifiedUser,
                        contentDescription = null,
                        tint = statusColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

// ── Quick Actions Row ───────────────────────────────────────────────────────
@Composable
private fun QuickActionsRow(
    onOfferRideClick: () -> Unit,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionCard(
            icon = Icons.Outlined.Add,
            label = "Post Ride",
            color = DriverAccent,
            onClick = onOfferRideClick,
            modifier = Modifier.weight(1f)
        )
        QuickActionCard(
            icon = Icons.Outlined.History,
            label = "My Rides",
            color = DriverSectionPurple,
            onClick = onRefreshClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = DriverCardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = label,
                color = DriverText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

// ── Old WelcomeHeader removed, replaced by HeroHeader ───────────────────────

// ── Error Card ──────────────────────────────────────────────────────────────
@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DriverCardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(DriverCancelledBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.ErrorOutline,
                    null,
                    tint = DriverCancelledText,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(message, color = DriverTextMuted, fontSize = 14.sp)
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriverAccent)
            ) {
                Icon(Icons.Outlined.Refresh, null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Retry", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

// ── Active Rides Section (Uber-style) ───────────────────────────────────────
@Composable
private fun ActiveRidesSection(
    activeRides: List<TripResponse>,
    onOfferRideClick: () -> Unit,
    onCancelRide: (Long) -> Unit,
    onStartRide: (Long) -> Unit,
    startingTripId: Long?
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DriverSectionGreen.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.LocalTaxi,
                        null,
                        tint = DriverSectionGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    "Active Rides",
                    color = DriverPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            if (activeRides.isNotEmpty()) {
                Surface(
                    color = DriverActiveBg,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        text = "${activeRides.size} live",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        color = DriverActiveText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (activeRides.isEmpty()) {
            NoActiveRidesCard(onOfferRideClick = onOfferRideClick)
        } else {
            activeRides.forEach { trip ->
                ActiveRideCard(
                    trip = trip,
                    onCancelRide = onCancelRide,
                    onStartRide = onStartRide,
                    isStarting = startingTripId == trip.tripId
                )
            }
        }
    }
}

@Composable
private fun NoActiveRidesCard(onOfferRideClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DriverCardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                DriverAccent.copy(alpha = 0.1f),
                                DriverSectionPurple.copy(alpha = 0.1f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.DirectionsCar,
                    null,
                    tint = DriverAccent,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "No active rides",
                    color = DriverText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Ready to earn? Post your first ride and start connecting with students heading your way.",
                    color = DriverTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
            
            Button(
                onClick = onOfferRideClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DriverAccent)
            ) {
                Icon(Icons.Outlined.RocketLaunch, null, tint = Color.White)
                Spacer(Modifier.width(10.dp))
                Text(
                    "Offer Your First Ride",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

// ── Active Ride Card (Uber-style: shows waiting status, passengers, route) ──
@Composable
private fun ActiveRideCard(
    trip: TripResponse,
    onCancelRide: (Long) -> Unit,
    onStartRide: (Long) -> Unit = {},
    isStarting: Boolean = false
) {
    val bookedPassengers = trip.stops.filter { it.studentId != null }
    val seatsRemaining = trip.availableSeats - bookedPassengers.size
    val isScheduled = trip.status.equals("SCHEDULED", ignoreCase = true) ||
            trip.status.equals("CONFIRMED", ignoreCase = true)
    val isLive = trip.status.equals("IN_PROGRESS", ignoreCase = true)

    Surface(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = DriverCardBackground,
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing dot effect
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape)
                            .background(if (bookedPassengers.isEmpty()) DriverAccent else DriverSuccessText)
                    )
                    Text(
                        text = if (bookedPassengers.isEmpty()) "Waiting for passengers..." else "${bookedPassengers.size} passenger(s) booked",
                        color = if (bookedPassengers.isEmpty()) DriverAccent else DriverSuccessText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(color = DriverActiveBg, shape = RoundedCornerShape(999.dp)) {
                    Text(
                        text = trip.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = DriverActiveText, fontSize = 11.sp, fontWeight = FontWeight.Bold
                    )
                }
            }

            // Route visualization
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(Modifier.size(12.dp).clip(CircleShape).background(DriverSuccessText))
                        repeat(3) { Box(Modifier.size(3.dp, 6.dp).background(DriverBorder)) }
                        Box(Modifier.size(12.dp).clip(CircleShape).background(DriverAccent))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.weight(1f)) {
                        Column {
                            Text("FROM", color = DriverTextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                            Text(trip.departureStop, color = DriverText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Column {
                            Text("TO", color = DriverTextMuted, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
                            Text(trip.destinationStop, color = DriverText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Trip details chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TripDetailChip(icon = Icons.Outlined.CalendarToday, text = trip.departureTime.take(10), modifier = Modifier.weight(1f))
                TripDetailChip(icon = Icons.Outlined.Schedule, text = trip.departureTime.takeLast(5), modifier = Modifier.weight(1f))
                TripDetailChip(icon = Icons.Outlined.AirlineSeatReclineNormal, text = "$seatsRemaining left", modifier = Modifier.weight(1f))
                TripDetailChip(icon = Icons.Outlined.Payments, text = "R${trip.price}", modifier = Modifier.weight(1f))
            }

            // Passengers section
            if (bookedPassengers.isNotEmpty()) {
                HorizontalDivider(color = DriverBorder)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Outlined.People, null, tint = DriverPrimary, modifier = Modifier.size(18.dp))
                    Text("Passengers", color = DriverPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                bookedPassengers.forEach { stop ->
                    PassengerRow(name = stop.studentName ?: "Student", pickup = stop.stopName)
                }
            }

            // Start Trip button — only for a trip that hasn't started yet. Tapping it puts the
            // trip IN_PROGRESS on the backend and begins the live simulation passengers can track.
            if (isScheduled) {
                Button(
                    onClick = { onStartRide(trip.tripId) },
                    enabled = !isStarting,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DriverSuccessText)
                ) {
                    if (isStarting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Starting…", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    } else {
                        Icon(Icons.Outlined.PlayArrow, null, tint = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Start Trip", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            // Live indicator — visible confirmation to the driver that the trip is now moving and
            // trackable, without needing to open the passenger view.
            if (isLive) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = DriverActiveBg,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(Modifier.size(10.dp).clip(CircleShape).background(DriverSuccessText))
                        Text(
                            "Live — passengers can track this trip now.",
                            color = DriverActiveText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Cancel button
            OutlinedButton(
                onClick = { onCancelRide(trip.tripId) },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 1.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DriverCancelledText)
            ) {
                Icon(Icons.Outlined.Close, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Cancel Ride", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun TripDetailChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF3F4F6),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = DriverTextMuted)
            Spacer(Modifier.width(4.dp))
            Text(text, color = DriverText, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun PassengerRow(name: String, pickup: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(DriverSectionPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(2).uppercase(),
                    color = DriverSectionPurple,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(name, color = DriverText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text("Pickup: $pickup", color = DriverTextMuted, fontSize = 12.sp)
            }
            Surface(color = DriverSuccessBg, shape = RoundedCornerShape(999.dp)) {
                Text(
                    "Booked",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = DriverSuccessText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Past Rides Section ──────────────────────────────────────────────────────
@Composable
private fun PastRidesSection(pastRides: List<TripResponse>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DriverSectionPurple.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.History,
                    null,
                    tint = DriverSectionPurple,
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                "Past Rides",
                color = DriverPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (pastRides.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Info,
                        null,
                        tint = DriverTextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        "Your completed and cancelled rides will appear here.",
                        color = DriverTextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            pastRides.take(10).forEach { trip ->
                PastRideCard(trip = trip)
            }
        }
    }
}

@Composable
private fun PastRideCard(trip: TripResponse) {
    val (statusBg, statusColor, statusIcon) = when {
        trip.status.equals("COMPLETED", true) -> Triple(DriverSuccessBg, DriverSuccessText, Icons.Outlined.CheckCircle)
        trip.status.equals("CANCELLED", true) -> Triple(DriverCancelledBg, DriverCancelledText, Icons.Outlined.Cancel)
        else -> Triple(DriverPendingBg, DriverPendingText, Icons.Outlined.Schedule)
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DriverCardBackground,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(statusBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${trip.departureStop} → ${trip.destinationStop}",
                    color = DriverText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(12.dp), tint = DriverTextMuted)
                        Text(trip.departureTime.take(10), color = DriverTextMuted, fontSize = 12.sp)
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Payments, null, modifier = Modifier.size(12.dp), tint = DriverTextMuted)
                        Text("R${trip.price}", color = DriverTextMuted, fontSize = 12.sp)
                    }
                }
            }
            Surface(color = statusBg, shape = RoundedCornerShape(999.dp)) {
                Text(
                    trip.status,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StudentDriverHomeScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        StudentDriverHomeScreen(
            driverName = "Ayabulela",
            verificationStatus = "Approved",
            homeUiState = DriverHomeUiState.Success(
                activeRides = listOf(
                    TripResponse(
                        tripId = 1L,
                        driverId = 10L,
                        driverName = "Ayabulela",
                        registrationNumber = "ABC 123 EC",
                        tripType = "Carpool",
                        departureStop = "South Campus",
                        departureLat = null, departureLng = null,
                        destinationStop = "North Campus",
                        destinationLat = null, destinationLng = null,
                        departureTime = "2026-07-30 08:30",
                        arrivalTime = null,
                        availableSeats = 3,
                        price = BigDecimal("20.00"),
                        status = "SCHEDULED",
                        vehicleModel = "Toyota Corolla",
                        vehicleColour = "White",
                        vehicleCapacity = 4,
                        stops = listOf(
                            TripStopResponse(1, "Library Stop", -33.99, 25.67, 1, studentId = 5L, studentName = "Lanele Maqina")
                        )
                    )
                ),
                pastRides = listOf(
                    TripResponse(
                        tripId = 2L, driverId = 10L, driverName = "Ayabulela",
                        registrationNumber = "ABC 123 EC", tripType = "Carpool",
                        departureStop = "Walmer", departureLat = null, departureLng = null,
                        destinationStop = "Summerstrand", destinationLat = null, destinationLng = null,
                        departureTime = "2026-07-28 17:00", arrivalTime = "2026-07-28 17:30",
                        availableSeats = 3, price = BigDecimal("25.00"), status = "COMPLETED",
                        vehicleModel = "Toyota Corolla", vehicleColour = "White", vehicleCapacity = 4
                    )
                )
            )
        )
    }
}
