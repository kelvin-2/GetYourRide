// ─────────────────────────────────────────────────────────────────────────────
// CarpoolHomeScreen.kt
// Package: com.example.getyourride.ui.screens.Carpool
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Carpool

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.UserSession
import com.example.getyourride.data.mapper.toCarpoolRide
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.screens.Carpool.components.*
import com.example.getyourride.ui.theme.*
import com.example.getyourride.viewmodel.TripsUiState

@Composable
fun CarpoolHomeScreen(
    uiState          : TripsUiState             = TripsUiState.Loading,
    onRetry          : () -> Unit               = {},
    onBookRide       : (rideId: String) -> Unit = {},
    onViewAllRides   : () -> Unit               = {},
    onViewAllTrips   : () -> Unit               = {},
    onPickupClick    : () -> Unit               = {},
    onDestinationClick: () -> Unit              = {},
    onSearchRides    : () -> Unit               = {},
    onNotifications  : () -> Unit               = {},
    navController    : androidx.navigation.NavController = rememberNavController(),
)

{
    StudentLayout(
        currentRoute  = GyrRoutes.HOME,
        navController = navController,
        showBell      = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceGrey)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(4.dp))



            FindCarpoolHeader(
                pickupLocation      = "Campus North Entrance",
                destinationLocation = "Science Park / Downtown",
                onPickupClick       = onPickupClick,
                onDestinationClick  = onDestinationClick,
                onSearchRides       = onSearchRides,
            )

            AvailableRidesSection(
                uiState    = uiState,
                onRetry    = onRetry,
                onBookRide = onBookRide,
                onViewAll  = onViewAllRides,
            )

            RecentTripsSection(onViewAll = onViewAllTrips)

            Spacer(Modifier.height(20.dp))
        }
    }
}



// ── 1. Find a Carpool header ────────────────────────────────────────────────

@Composable
private fun FindCarpoolHeader(
    pickupLocation      : String,
    destinationLocation : String,
    onPickupClick       : () -> Unit,
    onDestinationClick  : () -> Unit,
    onSearchRides       : () -> Unit,
) {
    Column {
        Text(
            text       = "Find a Carpool",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = NavyPrimary,
        )
        Text(
            text     = "Connect with students traveling your way",
            fontSize = 13.sp,
            color    = TextMuted,
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                LocationInputField(
                    label     = "Pickup Location",
                    value     = pickupLocation,
                    icon      = Icons.Outlined.LocationOn,
                    iconTint  = NavyPrimary,
                    onClick   = onPickupClick,
                )

                Spacer(Modifier.height(14.dp))

                LocationInputField(
                    label     = "Destination",
                    value     = destinationLocation,
                    icon      = Icons.Outlined.Navigation,
                    iconTint  = OrangeAccent,
                    onClick   = onDestinationClick,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick        = onSearchRides,
            shape          = RoundedCornerShape(24.dp),
            colors         = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier       = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Search Rides",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
        }
    }
}

@Composable
private fun LocationInputField(
    label    : String,
    value    : String,
    icon     : androidx.compose.ui.graphics.vector.ImageVector,
    iconTint : Color,
    onClick  : () -> Unit,
) {
    Column {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = NavyPrimary,
        )
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceGrey)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(text = value, fontSize = 13.sp, color = TextMuted)
        }
    }
}

// ── 2. Available rides — now driven by TripsUiState from the backend ─────────

@Composable
private fun AvailableRidesSection(
    uiState    : TripsUiState,
    onRetry    : () -> Unit,
    onBookRide : (String) -> Unit,
    onViewAll  : () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Available Rides", onViewAll = onViewAll)

        when (uiState) {
            is TripsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is TripsUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = CardWhite),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text     = "Couldn't load rides — ${uiState.message}",
                            fontSize = 13.sp,
                            color    = TextMuted,
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onRetry,
                            colors  = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }

            is TripsUiState.Success -> {
                if (uiState.trips.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = CardWhite),
                    ) {
                        Text(
                            text     = "No carpool rides available right now — check back soon.",
                            fontSize = 13.sp,
                            color    = TextMuted,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    uiState.trips.forEach { trip: TripResponse ->
                        CarpoolBookingCard(
                            ride        = trip.toCarpoolRide(),
                            onBookClick = { onBookRide(trip.tripId.toString()) },
                        )
                    }
                }
            }
        }
    }
}

// ── 3. Recent trips ─────────────────────────────────────────────────────────

@Composable
private fun RecentTripsSection(onViewAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Recent Trips", onViewAll = onViewAll)

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp),
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Summerstrand → South Campus", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    Text("Yesterday, 13 Oct", fontSize = 12.sp, color = TextMuted)
                    Row {
                        repeat(4) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                        }
                        Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = BorderLight, modifier = Modifier.size(14.dp))
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusCompleted.copy(alpha = 0.12f),
                ) {
                    Text(
                        text       = "Completed",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = StatusCompleted,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

// ─── Preview ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Carpool Home")
@Composable
fun CarpoolHomePreview() {
    MaterialTheme { CarpoolHomeScreen() } // defaults to Loading state
}