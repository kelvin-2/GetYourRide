
// ─────────────────────────────────────────────────────────────────────────────
// MyRidesScreen.kt
// Package: com.example.getyourride.ui.screens.Rides
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Rides

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.RideCard
import com.example.getyourride.ui.components.RideCardData
import com.example.getyourride.ui.components.RideStatus
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.theme.*

private enum class RideTab(val label: String) { UPCOMING("Upcoming"), PAST("Past"), CANCELLED("Cancelled") }

@Composable
fun MyRidesScreen(
    rides         : List<RideCardData>       = emptyList(),
    onTrackRide   : (String) -> Unit         = {},
    onCancelRide  : (String) -> Unit         = {},
    navController : androidx.navigation.NavController = rememberNavController(),
) {
    var selectedTab by remember { mutableStateOf(RideTab.UPCOMING) }

    val filtered = rides.filter {
        when (selectedTab) {
            RideTab.UPCOMING  -> it.status == RideStatus.ACTIVE || it.status == RideStatus.SCHEDULED
            RideTab.PAST      -> it.status == RideStatus.COMPLETED
            RideTab.CANCELLED -> it.status == RideStatus.CANCELLED
        }
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            Text("My Rides", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)

            RideTabRow(selected = selectedTab, onSelect = { selectedTab = it })

            if (filtered.isEmpty()) {
                Text(
                    text     = "No ${selectedTab.label.lowercase()} rides yet.",
                    fontSize = 13.sp,
                    color    = TextMuted,
                    modifier = Modifier.padding(vertical = 24.dp),
                )
            } else {
                filtered.forEach { ride ->
                    RideCard(
                        ride         = ride,
                        onTrackRide  = { onTrackRide(ride.id) },
                        onCancelRide = { onCancelRide(ride.id) },
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

// ─── Previews ────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun MyRidesScreenPreview() {
    val sampleRides = listOf(
        RideCardData(
            id = "1",
            driverName = "Alex Rivera",
            carDescription = "Toyota Corolla",
            plate = "ABC 123 EC",
            status = RideStatus.ACTIVE,
            pickup = "Engineering Bldg",
            dropoff = "City Tech Hub",
            dateLabel = "Today, 24 Oct",
            timeLabel = "08:30 AM"
        ),
        RideCardData(
            id = "2",
            driverName = "Thando Mokoena",
            carDescription = "VW Polo",
            plate = "XYZ 789 EC",
            status = RideStatus.SCHEDULED,
            pickup = "South Campus",
            dropoff = "Summerstrand",
            dateLabel = "Tomorrow, 25 Oct",
            timeLabel = "09:15 AM"
        ),
        RideCardData(
            id = "3",
            driverName = "John Doe",
            carDescription = "Ford Ranger",
            plate = "LMN 456 EC",
            status = RideStatus.COMPLETED,
            pickup = "North Campus",
            dropoff = "Central",
            dateLabel = "Yesterday, 23 Oct",
            timeLabel = "05:00 PM"
        )
    )

    GetYourRideTheme {
        MyRidesScreen(rides = sampleRides)
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
        RideTab.values().forEach { tab ->
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