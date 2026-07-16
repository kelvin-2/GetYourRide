package com.example.getyourride.ui.screens.shuttle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.example.getyourride.ui.theme.GreenSuccess
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

// ---------- Data models for this screen ----------

data class UpcomingShuttle(
    val from: String,
    val to: String,
    val status: String,
    val time: String,
    val date: String,
    val seat: String
)

data class RecentTrip(
    val from: String,
    val to: String,
    val date: String,
    val time: String
)

// ---------- Screen ----------

@Composable
fun ShuttleHomeScreen(
    userName: String,
    upcomingShuttles: List<UpcomingShuttle>,
    recentTrips: List<RecentTrip>,
    onBookShuttle: () -> Unit,
    onViewAllShuttles: () -> Unit,
    onShowTicket: (UpcomingShuttle) -> Unit,
    onTripClick: (RecentTrip) -> Unit,
    onFabClick: () -> Unit,
    onNavHome: () -> Unit,
    onNavRides: () -> Unit,
    onNavTrack: () -> Unit,
    onNavProfile: () -> Unit
) {
    Scaffold(
        topBar = { HomeTopBar() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onFabClick,
                containerColor = OrangeAccent,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add", tint = Color.White)
            } 
        },
        bottomBar = {
            HomeBottomBar(
                onNavHome = onNavHome,
                onNavRides = onNavRides,
                onNavTrack = onNavTrack,
                onNavProfile = onNavProfile
            )
        },
        containerColor = Color(0xFFF5F6FA)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Good morning,",
                    color = OrangeAccent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = userName,
                    color = NavyPrimary,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(20.dp))
                BookShuttleCard(onClick = onBookShuttle)
                Spacer(Modifier.height(28.dp))
            }

            item {
                SectionHeader(title = "Upcoming Shuttles", actionLabel = "View All", onAction = onViewAllShuttles)
                Spacer(Modifier.height(12.dp))
            }

            items(upcomingShuttles) { shuttle ->
                UpcomingShuttleCard(shuttle = shuttle, onShowTicket = { onShowTicket(shuttle) })
                Spacer(Modifier.height(14.dp))
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Recent Trips",
                    color = NavyPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
            }

            items(recentTrips) { trip ->
                RecentTripItem(trip = trip, onClick = { onTripClick(trip) })
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

// ---------- Top bar ----------

@Composable
private fun HomeTopBar() {
    Surface(color = NavyPrimary) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "GetYourRide",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Box {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Color.White
                )
            }
        }
    }
}

// ---------- Book a Shuttle card with bus photo background ----------

@Composable
private fun BookShuttleCard(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        // Placeholder background — add actual image at res/drawable/bus_background.jpg
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.LightGray)
        )

        // Navy gradient overlay so text stays readable over the photo
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            NavyPrimary.copy(alpha = 0.92f),
                            NavyPrimary.copy(alpha = 0.55f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "Book a Shuttle",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Reserve your seat on the next campus ride.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "New Booking",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ---------- Section header ----------

@Composable
private fun SectionHeader(title: String, actionLabel: String, onAction: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, color = NavyPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            text = actionLabel,
            color = OrangeAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable { onAction() }
        )
    }
}

// ---------- Upcoming shuttle card ----------

@Composable
private fun UpcomingShuttleCard(shuttle: UpcomingShuttle, onShowTicket: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    RouteRow(label = "FROM", place = shuttle.from, dotColor = NavyPrimary)
                    Spacer(Modifier.height(6.dp))
                    RouteRow(label = "TO", place = shuttle.to, dotColor = OrangeAccent)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Surface(
                        color = GreenSuccess.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = shuttle.status,
                            color = GreenSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(text = shuttle.time, color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(text = shuttle.date, color = Color.Gray, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFEFEFEF))
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.EventSeat,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "Seat ${shuttle.seat}", color = Color.Gray, fontSize = 13.sp)
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onShowTicket() }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ConfirmationNumber,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(text = "SHOW TICKET", color = OrangeAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun RouteRow(label: String, place: String, dotColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(text = label, color = Color.Gray, fontSize = 10.sp, fontWeight = FontWeight.Medium)
            Text(text = place, color = NavyPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------- Recent trip item ----------

@Composable
private fun RecentTripItem(trip: RecentTrip, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F1F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.History,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${trip.from} → ${trip.to}",
                    color = NavyPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${trip.date} • ${trip.time}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}

// ---------- Bottom nav ----------

@Composable
private fun HomeBottomBar(
    onNavHome: () -> Unit,
    onNavRides: () -> Unit,
    onNavTrack: () -> Unit,
    onNavProfile: () -> Unit
) {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(
            selected = true,
            onClick = onNavHome,
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = OrangeAccent,
                selectedTextColor = OrangeAccent,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavRides,
            icon = { Icon(Icons.Filled.DirectionsCar, contentDescription = "Rides") },
            label = { Text("Rides") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavTrack,
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Track") },
            label = { Text("Track") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onNavProfile,
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") }
        )
    }
}

// ---------- Preview ----------

@Preview(showBackground = true)
@Composable
private fun ShuttleHomeScreenPreview() {
    ShuttleHomeScreen(
        userName = "Alex",
        upcomingShuttles = listOf(
            UpcomingShuttle(
                from = "Gqeberha",
                to = "Missionvale",
                status = "Active",
                time = "08:30",
                date = "Today, 14 Oct",
                seat = "12A"
            )
        ),
        recentTrips = listOf(
            RecentTrip("Missionvale", "Gqeberha", "Yesterday", "16:45"),
            RecentTrip("North Campus", "Gqeberha", "12 Oct", "13:00")
        ),
        onBookShuttle = {},
        onViewAllShuttles = {},
        onShowTicket = {},
        onTripClick = {},
        onFabClick = {},
        onNavHome = {},
        onNavRides = {},
        onNavTrack = {},
        onNavProfile = {}
    )
}