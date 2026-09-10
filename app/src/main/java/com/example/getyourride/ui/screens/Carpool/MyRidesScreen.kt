package com.example.getyourride.ui.screens.Rides

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsNone
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
import androidx.compose.ui.window.PopupProperties
import com.example.getyourride.data.mapper.toRideCardData
import com.example.getyourride.data.remote.dto.NotificationResponse
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
    notifications       : List<NotificationResponse> = emptyList(),
    unreadCount         : Long = 0,
    onNotificationsOpen : () -> Unit = {},
    onNotificationClick : (Long) -> Unit = {},
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text       = "My Rides",
                    fontSize   = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color      = NavyPrimary,
                )
                RidesNotificationBell(
                    notifications       = notifications,
                    unreadCount         = unreadCount,
                    onOpen              = onNotificationsOpen,
                    onNotificationClick = onNotificationClick,
                )
            }

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
                                    // Track by tripId, NOT ride.id. Under the TripBookingResponse
                                    // mapper ride.id is the bookingId, so passing it made the
                                    // tracking screen call GET /api/trips/{bookingId} and get a 404
                                    // ("Trip not found with id: 10"). tripId is the real trip and
                                    // is what the tracking endpoint expects — same value cancel uses.
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

// ── Notification bell + dropdown popup ──────────────────────────────────────
@Composable
private fun RidesNotificationBell(
    notifications       : List<NotificationResponse>,
    unreadCount         : Long,
    onOpen              : () -> Unit,
    onNotificationClick : (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Box {
            IconButton(
                onClick = {
                    expanded = true
                    onOpen()
                }
            ) {
                Icon(
                    imageVector = Icons.Outlined.NotificationsNone,
                    contentDescription = "Notifications",
                    tint = NavyPrimary,
                )
            }
            if (unreadCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            properties = PopupProperties(focusable = true),
            modifier = Modifier
                .background(CardWhite)
                .widthIn(min = 280.dp, max = 340.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Notifications",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                )
                if (unreadCount > 0) {
                    Text(
                        text = "$unreadCount new",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrangeAccent,
                    )
                }
            }
            HorizontalDivider(color = SurfaceGrey)

            if (notifications.isEmpty()) {
                Text(
                    text = "You're all caught up. No notifications yet.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                )
            } else {
                notifications.take(15).forEach { notification ->
                    RidesNotificationRow(
                        notification = notification,
                        onClick = { onNotificationClick(notification.notificationId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RidesNotificationRow(
    notification : NotificationResponse,
    onClick      : () -> Unit,
) {
    // A clear, short headline based on the notification type, with the detail beneath.
    val title = when (notification.type) {
        "RIDE_CANCELLED" -> "Ride cancelled"
        else -> "Notification"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (notification.read) Color.Transparent else OrangeAccent.copy(alpha = 0.08f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        // Unread dot (invisible placeholder when read, to keep text aligned).
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(if (notification.read) Color.Transparent else OrangeAccent),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = notification.message,
                fontSize = 13.sp,
                color = TextMuted,
                lineHeight = 18.sp,
            )
        }
    }
}