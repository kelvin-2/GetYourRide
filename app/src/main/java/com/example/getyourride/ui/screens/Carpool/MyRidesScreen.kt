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
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.data.mapper.toRideCardData  // ← correct import
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.RideCard
import com.example.getyourride.ui.components.RideStatus
import com.example.getyourride.ui.components.StudentLayout
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
    navController : androidx.navigation.NavController = rememberNavController(),
) {
    var selectedTab by remember { mutableStateOf(RideTab.UPCOMING) }
    val uiState = viewModel.uiState

    // NOTE: AllRidesViewModel already calls loadAllTrips() in its init{} block.
    // If you want a fresh reload every time this screen is re-entered (e.g. after
    // navigating back from another tab), keep this LaunchedEffect. If you only
    // want the initial load, remove this and rely on init{} alone — otherwise
    // you're firing two network calls back-to-back on first composition.
    LaunchedEffect(Unit) {
        viewModel.loadAllTrips()
    }

    StudentLayout(
        currentRoute  = GyrRoutes.RIDES,
        navController = navController,
        showBell      = true,
    ) {
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
                    // Map TripResponse → RideCardData using our mapper
                    val allCards = uiState.trips.map { it.toRideCardData() }

                    // Filter by selected tab
                    val filtered = allCards.filter {
                        when (selectedTab) {
                            RideTab.UPCOMING  -> it.status == RideStatus.ACTIVE || it.status == RideStatus.SCHEDULED
                            RideTab.PAST      -> it.status == RideStatus.COMPLETED
                            RideTab.CANCELLED -> it.status == RideStatus.CANCELLED
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
                            filtered.forEach { ride ->
                                RideCard(
                                    ride         = ride,
                                    onTrackRide  = { onTrackRide(ride.id) },
                                    onCancelRide = { viewModel.cancelTrip(ride.id.toLong()) },
                                )
                            }
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                }
            }
        }
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