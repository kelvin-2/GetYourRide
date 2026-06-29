// ─────────────────────────────────────────────────────────────────────────────
// CarpoolHomeScreen.kt
// Package: com.example.getyourride.ui.screens.Carpool
//
// PURPOSE — Home dashboard for carpool students.
//
// This file only assembles components — it contains no raw UI primitives.
// If something looks wrong, find the relevant component file to fix it:
//
//   Layout / nav bar issue  → StudentLayout.kt
//   Carpool card issue      → CarpoolBookingCard.kt
//   Section heading issue   → SectionHeader.kt
//   Quick action issue      → QuickActionCard.kt
//   Top bar issue           → GyrTopBar.kt
//   Colors                  → GetYourRideColors.kt
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.screens.Carpool.components.*
import com.example.getyourride.ui.theme.*

@Composable
fun CarpoolHomeScreen(
    // Callbacks — all defaulted so @Preview works without a NavController
    onBookRide       : (rideId: String) -> Unit = {},
    onViewAllRides   : () -> Unit               = {},
    onViewAllTrips   : () -> Unit               = {},
    onWhereTo        : () -> Unit               = {},
    onSchedule       : () -> Unit               = {},
    onPostRide       : () -> Unit               = {},
    onNotifications  : () -> Unit               = {},
    navController    : androidx.navigation.NavController = rememberNavController(),
) {
    // ── StudentLayout gives us the top bar + bottom nav for free ─────────────
    // Just set currentRoute = HOME and everything else is handled
    StudentLayout(
        currentRoute  = GyrRoutes.HOME,
        navController = navController,
        showBell      = true,
    ) {
        Box(Modifier.fillMaxSize()) {

            // ── Scrollable page content ───────────────────────────────────────
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceGrey)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                GreetingRow(userName = "Alex", isNsfas = true)

                HeroBookingCard(onBookNow = onWhereTo)

                QuickActionsRow(onWhereTo = onWhereTo, onSchedule = onSchedule)

                AvailableCarpoolsSection(
                    onBookRide  = onBookRide,
                    onViewAll   = onViewAllRides,
                )

                RecentTripsSection(onViewAll = onViewAllTrips)

                // Extra space so FAB doesn't cover last card
                Spacer(Modifier.height(72.dp))
            }

            // ── Floating Action Button — Post a Ride ──────────────────────────
            // Positioned bottom-right, above the bottom nav bar
            FloatingActionButton(
                onClick          = onPostRide,
                containerColor   = OrangeAccent,
                contentColor     = Color.White,
                modifier         = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = "Post a Ride")
            }
        }
    }
}

// ─── Section composables ——————————————————————————————————————————————────────
// Each section is its own function so it can be previewed and debugged alone.

// ── 1. Greeting row ───────────────────────────────────────────────────────────

@Composable
private fun GreetingRow(userName: String, isNsfas: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Column {
            Text("Good morning,", fontSize = 13.sp, color = TextMuted)
            Text(
                text       = userName,
                fontSize   = 24.sp,
                fontWeight = FontWeight.Bold,
                color      = NavyPrimary,
            )
        }

        // NSFAS badge — only shown for NSFAS-funded students
        if (isNsfas) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GreenSuccess.copy(alpha = 0.10f),
            ) {
                Text(
                    text       = "NSFAS STUDENT",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color      = GreenSuccess,
                    modifier   = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }
        }
    }
}

// ── 2. Hero card ──────────────────────────────────────────────────────────────

@Composable
private fun HeroBookingCard(onBookNow: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            // Navy gradient simulates the dark overlay on the background photo
            // TODO: replace with an actual image using:
            // Box(modifier = Modifier.fillMaxSize()) {
            //     Image(painter = painterResource(R.drawable.hero_carpool), ...)
            //     Box(modifier = Modifier.fillMaxSize().background(darkOverlay))
            // }
            .background(
                Brush.linearGradient(
                    colors = listOf(NavyPrimary, Color(0xFF2A4A8A))
                )
            ),
    ) {
        Column(
            modifier            = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Orange icon box
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.PeopleAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            }

            Text("Find a Carpool", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Share a ride with fellow NMU students.", fontSize = 12.sp, color = Color.White.copy(alpha = 0.75f))

            // Book Now pill button
            Button(
                onClick        = onBookNow,
                shape          = RoundedCornerShape(20.dp),
                colors         = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text("Book Now ›", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

// ── 3. Quick actions row ──────────────────────────────────────────────────────

@Composable
private fun QuickActionsRow(onWhereTo: () -> Unit, onSchedule: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        QuickActionCard(
            icon     = Icons.Outlined.LocationOn,
            label    = "Where To?",
            onClick  = onWhereTo,
            iconTint = OrangeAccent,
            modifier = Modifier.weight(1f),
        )
        QuickActionCard(
            icon     = Icons.Outlined.CalendarMonth,
            label    = "Schedule",
            onClick  = onSchedule,
            iconTint = NavyPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

// ── 4. Available carpools ─────────────────────────────────────────────────────

@Composable
private fun AvailableCarpoolsSection(
    onBookRide : (String) -> Unit,
    onViewAll  : () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = "Available Carpools", onViewAll = onViewAll)

        // Card 1 — 2 seats left (green badge)
        CarpoolBookingCard(
            ride        = sampleRide1,
            onBookClick = { onBookRide("ride_001") },
        )

        // Card 2 — 1 seat left (amber badge)
        CarpoolBookingCard(
            ride        = sampleRide2,
            onBookClick = { onBookRide("ride_002") },
        )
    }
}

// ── 5. Recent trips ───────────────────────────────────────────────────────────

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
                    // Star rating row
                    Row {
                        repeat(4) {
                            Icon(Icons.Outlined.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                        }
                        Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = BorderLight, modifier = Modifier.size(14.dp))
                    }
                }

                // Completed badge
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

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Carpool Home")
@Composable
fun CarpoolHomePreview() {
    MaterialTheme { CarpoolHomeScreen() }
}