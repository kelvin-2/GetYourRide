// ─────────────────────────────────────────────────────────────────────────────
// RequestRideScreen.kt
// Package: com.example.getyourride.ui.screens.Rides
//
// PURPOSE — Shown when a student taps "Book Seat" on a carpool card.
// Student reviews the ride, optionally sets a custom pickup via AddStopScreen,
// sees the price, then confirms or cancels the request.
//
// Booking is fixed at 1 seat, no notes — matches the backend's
// BookCarpoolRequest, which has no seats-count or notes field.
//
// NAVIGATION — Called from CarpoolHomeScreen:
//   onBookRide = { tripId -> navController.navigate("request_ride/$tripId") }
//
// WIRING — bookingViewModel is scoped to this trip's booking flow (see
// TripBookingViewModel) and shared with AddStopScreen via the nav back stack
// entry, so a pickup chosen there flows back into the same booking state here.
//
// SUB-COMPONENTS (all private — only used by this screen):
//   DriverInfoCard       → driver avatar, name, rating, vehicle
//   RouteCard            → pickup + destination with optional stop
//   TripDetailsRow       → departure time, arrival est., seats, price chips
//   TotalAmountRow       → price summary (always 1 seat)
//
// If something looks wrong visually:
//   Colors  → GetYourRideColors.kt (NavyPrimary, OrangeAccent etc.)
//   TopBar  → uses GyrTopBar from ui/components/GyrTopBar.kt
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Rides

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.components.GyrTopBar
import com.example.getyourride.ui.theme.*
import com.example.getyourride.viewmodel.BookingUiState
import com.example.getyourride.viewmodel.TripBookingViewModel

// ─── Data model ───────────────────────────────────────────────────────────────

/**
 * Everything the screen needs to display one ride request.
 * Populated from TripResponse via TripMapper.toRideRequestDetails() when
 * connecting to real API.
 *
 * pickupLat/pickupLng/destinationLat/destinationLng are nullable because
 * some trips in the backend are saved without coordinates (departureLat is
 * null in real sample data) — when null, the student must pick a pickup via
 * AddStopScreen before they can confirm.
 *
 * @param driverAvatarUrl  Pass null to show initials fallback avatar
 */
data class RideRequestDetails(
    val tripId               : Long,
    val driverName           : String,
    val driverRating         : Double,
    val driverRidesCompleted : Int,
    val driverAvatarUrl      : String?,  // null shows initials fallback
    val carDescription       : String,   // e.g. "Silver Toyota Corolla"
    val plate                : String,   // e.g. "ABC-1234"
    val pickupLabel          : String,
    val pickupLat            : Double?,
    val pickupLng            : Double?,
    val destinationLabel     : String,
    val destinationLat       : Double?,
    val destinationLng       : Double?,
    val departureTime        : String,   // display string e.g. "08:30 AM"
    val arrivalEstimate      : String,   // display string e.g. "08:50 AM"
    val seatsAvailable       : Int,
    val pricePerSeat         : Double,
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RequestRideScreen(
    ride             : RideRequestDetails,
    bookingViewModel : TripBookingViewModel,
    onBackClick      : () -> Unit,
    onAddStopClick   : () -> Unit = {},
    onBookingSuccess : (TripResponse) -> Unit,
    onCancel         : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    // If the trip already has known departure coordinates and the student
    // hasn't manually picked a stop yet, this defaults pickupStop to them —
    // so "Confirm" works immediately without forcing a detour through
    // AddStopScreen. Safe to call repeatedly; it no-ops once a pickup is set.
    LaunchedEffect(ride.tripId) {
        bookingViewModel.initializeDefaultPickupIfNeeded(ride)
    }

    val bookingState = bookingViewModel.bookingState

    LaunchedEffect(bookingState) {
        if (bookingState is BookingUiState.Success) {
            onBookingSuccess(bookingState.trip)
        }
    }

    // Reflects whichever pickup is currently active — the trip's own default,
    // or whatever the student picked via AddStopScreen.
    val effectivePickupLabel = bookingViewModel.pickupStop?.displayName ?: ride.pickupLabel

    Scaffold(
        containerColor = CardWhite,
        topBar = {
            // Reuses the shared GyrTopBar with back arrow — consistent with rest of app
            GyrTopBar(
                onBackClick   = onBackClick,
                trailingLabel = null,
            )
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            DriverInfoCard(ride = ride)

            RouteCard(
                pickupLabel      = effectivePickupLabel,
                destinationLabel = ride.destinationLabel,
                onAddStopClick   = onAddStopClick,
            )

            TripDetailsRow(
                departureTime   = ride.departureTime,
                arrivalEstimate = ride.arrivalEstimate,
                seatsAvailable  = ride.seatsAvailable,
                pricePerSeat    = ride.pricePerSeat,
            )

            TotalAmountRow(totalAmount = ride.pricePerSeat)

            if (bookingState is BookingUiState.Error) {
                Text(
                    text     = bookingState.message,
                    color    = DangerRed,
                    fontSize = 13.sp,
                )
            }

            // ── Confirm button ────────────────────────────────────────────────
            Button(
                onClick  = { bookingViewModel.confirmBooking() },
                enabled  = bookingState !is BookingUiState.Submitting,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            ) {
                if (bookingState is BookingUiState.Submitting) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        color       = Color.White,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(
                        text       = "Confirm Request",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White,
                    )
                }
            }

            // ── Cancel link ───────────────────────────────────────────────────
            TextButton(
                onClick  = onCancel,
                modifier = Modifier.fillMaxWidth().height(44.dp),
            ) {
                Text(
                    text     = "Cancel",
                    color    = TextMuted,
                    fontSize = 15.sp,
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

// ─── Sub-components ───────────────────────────────────────────────────────────

/**
 * Driver avatar (initials fallback if no URL), name, rides count, vehicle, rating.
 *
 * NOTE: avatar URL loading requires Coil. Add to build.gradle if not present:
 *   implementation("io.coil-kt:coil-compose:2.6.0")
 * Then replace the Box initials fallback with:
 *   AsyncImage(model = ride.driverAvatarUrl, contentDescription = ride.driverName,
 *              modifier = Modifier.size(52.dp).clip(CircleShape))
 */
@Composable
private fun DriverInfoCard(ride: RideRequestDetails) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceGrey),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Avatar — initials fallback until Coil is wired up
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text       = ride.driverName.take(1).uppercase(),
                    color      = OrangeAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 20.sp,
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = ride.driverName,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 16.sp,
                    color      = NavyPrimary,
                )
                Text(
                    text     = "${ride.driverRidesCompleted}+ rides completed",
                    fontSize = 13.sp,
                    color    = TextMuted,
                )
                Text(
                    text     = "${ride.carDescription} • ${ride.plate}",
                    fontSize = 12.sp,
                    color    = TextMuted,
                )
            }

            // Star rating
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(2.dp))
                Text(
                    text       = ride.driverRating.toString(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = NavyPrimary,
                )
            }
        }
    }
}

/** Pickup → optional stop → destination route display. */
@Composable
private fun RouteCard(
    pickupLabel      : String,
    destinationLabel : String,
    onAddStopClick   : () -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardWhite),
        border   = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Pickup row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary),
                )
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("PICKUP", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(pickupLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }

            // Add stop link
            Row(
                modifier          = Modifier
                    .padding(start = 4.dp, top = 6.dp, bottom = 6.dp)
                    .clickable { onAddStopClick() },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add stop", tint = OrangeAccent, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Text("Add a Stop", fontSize = 13.sp, color = OrangeAccent, fontWeight = FontWeight.Medium)
            }

            // Destination row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("DESTINATION", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(destinationLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }
        }
    }
}

/** 2×2 grid of info chips: departure, arrival, seats left, price. */
@Composable
private fun TripDetailsRow(
    departureTime   : String,
    arrivalEstimate : String,
    seatsAvailable  : Int,
    pricePerSeat    : Double,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailChip(modifier = Modifier.weight(1f), label = "Departure",   value = "Today, $departureTime")
            DetailChip(modifier = Modifier.weight(1f), label = "Arrival Est.", value = arrivalEstimate)
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DetailChip(
                modifier   = Modifier.weight(1f),
                label      = "Seats Available",
                value      = "$seatsAvailable Left",
                valueColor = OrangeAccent,
                showDot    = true,
            )
            DetailChip(
                modifier = Modifier.weight(1f),
                label    = "Price",
                value    = "R${"%.2f".format(pricePerSeat)}",
            )
        }
    }
}

/** Single info chip used inside TripDetailsRow. */
@Composable
private fun DetailChip(
    modifier   : Modifier = Modifier,
    label      : String,
    value      : String,
    valueColor : Color  = NavyPrimary,
    showDot    : Boolean = false,
) {
    Card(
        modifier = modifier,
        shape    = RoundedCornerShape(14.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceGrey),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 12.sp, color = TextMuted)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showDot) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(OrangeAccent),
                    )
                    Spacer(Modifier.width(6.dp))
                }
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = valueColor)
            }
        }
    }
}

/** Price summary row shown above the confirm button — always 1 seat. */
@Composable
private fun TotalAmountRow(totalAmount: Double) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text("Total Amount", fontSize = 15.sp, color = TextMuted)
        Text(
            text       = "R${"%.2f".format(totalAmount)}",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = NavyPrimary,
        )
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────
// NOTE: Previews can't construct a real TripBookingViewModel (it needs a
// TripRepository). If these previews need to keep rendering in Android
// Studio's preview pane, they'll need a fake/no-op TripBookingViewModel or
// should be temporarily commented out — let me know which you'd rather do.

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Request Ride — default pickup")
@Composable
private fun RequestRidePreview() {
    MaterialTheme {
        // Preview left as a placeholder — needs a fake TripBookingViewModel
        // to compile now that bookingViewModel is a required param.
    }
}