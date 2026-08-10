package com.example.getyourride.ui.screens.Rides

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.mapper.toRideCardData
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.RideCard
import com.example.getyourride.ui.components.RideCardData
import com.example.getyourride.ui.components.RideStatus
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.components.ShuttleLayout
import com.example.getyourride.ui.theme.*
import com.example.getyourride.viewmodel.AllRidesViewModel
import com.example.getyourride.viewmodel.AllTripsUiState

private enum class RideTab(val label: String) {
    UPCOMING("Upcoming"),
    PAST("Past"),
    CANCELLED("Cancelled"),
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyRidesScreen(
    viewModel     : AllRidesViewModel,
    onTrackRide   : (String) -> Unit = {},
    navController : androidx.navigation.NavController,
    currentRoute  : String = GyrRoutes.RIDES,
) {
    var selectedTab by remember { mutableStateOf(RideTab.UPCOMING) }
    val uiState = viewModel.uiState

    // MyRidesScreen previously had no reload trigger — add one so it doesn't
    // sit on Loading forever (per the ViewModel's own comment).
    LaunchedEffect(Unit) {
        viewModel.loadAllTrips()
    }

    val content = @Composable {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceGrey)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            Text(
                text       = "My Rides",
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = NavyPrimary,
            )

            RideTabRow(selected = selectedTab, onSelect = { selectedTab = it })

            when (uiState) {

                // ── Loading ───────────────────────────────────────────────────
                is AllTripsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NavyPrimary)
                    }
                }

                // ── Error ─────────────────────────────────────────────────────
                is AllTripsUiState.Error -> {
                    Column(
                        modifier            = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(uiState.message, fontSize = 14.sp, color = DangerRed, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadAllTrips() },
                            colors  = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                            shape   = RoundedCornerShape(10.dp),
                        ) {
                            Icon(Icons.Outlined.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Try Again")
                        }
                    }
                }

                // ── Success ───────────────────────────────────────────────────
                is AllTripsUiState.Success -> {
                    // Filter by trip type based on current route.
                    // uiState.bookings is List<TripBookingResponse>.
                    val typeFiltered = uiState.bookings.filter { booking ->
                        if (currentRoute == GyrRoutes.SHUTTLE_RIDES) {
                            booking.trip.tripType.equals("SHUTTLE", ignoreCase = true)
                        } else {
                            booking.trip.tripType.equals("Carpool", ignoreCase = true)
                        }
                    }

                    // Build (card, tripId) pairs instead of just cards. This is
                    // the ONLY reason we're not calling typeFiltered.map { it.toRideCardData() }
                    // directly — we need trip.tripId kept alongside each card so
                    // cancel keeps using tripId, exactly like it did before
                    // (mapper's RideCardData.id = bookingId.toString() now, which
                    // would silently break cancel if used directly — see below).
                    val cardsWithTripId: List<Pair<RideCardData, Long>> =
                        typeFiltered.map { booking -> booking.toRideCardData() to booking.trip.tripId }

                    // Filter by selected tab (status comes from bookingStatus via
                    // the mapper, so this logic is unchanged).
                    val filtered = cardsWithTripId.filter { (card, _) ->
                        when (selectedTab) {
                            RideTab.UPCOMING  -> card.status == RideStatus.ACTIVE || card.status == RideStatus.SCHEDULED
                            RideTab.PAST      -> card.status == RideStatus.COMPLETED
                            RideTab.CANCELLED -> card.status == RideStatus.CANCELLED
                        }
                    }

                    if (filtered.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text      = "No ${selectedTab.label.lowercase()} rides.",
                                fontSize  = 14.sp,
                                color     = TextMuted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        Column(
                            modifier            = Modifier.verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            filtered.forEach { (ride, tripId) ->
                                RideCard(
                                    ride         = ride,
                                    onTrackRide  = { onTrackRide(tripId.toString()) },
                                    // Cancel still sends tripId, exactly as it did
                                    // before (matches the working PATCH
                                    // /api/trips/bookings/{tripId}/cancel call
                                    // seen in logcat) — NOT ride.id, which is now
                                    // bookingId under the TripBookingResponse mapper.
                                    onCancelRide = { viewModel.cancelTrip(tripId) },
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
    }

    if (currentRoute == GyrRoutes.SHUTTLE_RIDES) {
        ShuttleLayout(
            currentRoute = currentRoute,
            navController = navController,
            content = content
        )
    } else {
        StudentLayout(
            currentRoute = currentRoute,
            navController = navController,
            content = content
        )
    }
}

@Composable
private fun RideTabRow(selected: RideTab, onSelect: (RideTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(BorderLight)
            .padding(4.dp),
    ) {
        RideTab.entries.forEach { tab ->
            val isSelected = tab == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) CardWhite else Color.Transparent)
                    .clickable { onSelect(tab) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = tab.label,
                    fontSize   = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color      = if (isSelected) NavyPrimary else TextMuted,
                )
            }
        }
    }
}