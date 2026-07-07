// ─────────────────────────────────────────────────────────────────────────────
// RequestRideScreen.kt
// Package: com.example.getyourride.ui.screens.Rides
//
// PURPOSE — Shown when a student taps "Book Seat" on a carpool card.
// Student selects how many seats, adds optional notes for the driver,
// sees the total cost, then confirms or cancels the request.
//
// NAVIGATION — Called from CarpoolHomeScreen:
//   onBookRide = { tripId -> navController.navigate("request_ride/$tripId") }
//
// WIRING TODO (when backend booking endpoint is ready):
//   onConfirmRequest = { seats, notes ->
//       bookingViewModel.bookSeat(tripId, seats, notes)
//   }
//
// SUB-COMPONENTS (all private — only used by this screen):
//   DriverInfoCard       → driver avatar, name, rating, vehicle
//   RouteCard            → pickup + destination with optional stop
//   TripDetailsRow       → departure time, arrival est., seats, price chips
//   SeatStepperCard      → increment/decrement seat count
//   NotesField           → optional message to driver
//   TotalAmountRow       → seats × price summary
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.GyrTopBar
import com.example.getyourride.ui.theme.*

// ─── Data model ───────────────────────────────────────────────────────────────

/**
 * Everything the screen needs to display one ride request.
 * Populated from TripResponse via TripMapper when connecting to real API.
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
    val destinationLabel     : String,
    val departureTime        : String,   // display string e.g. "08:30 AM"
    val arrivalEstimate      : String,   // display string e.g. "08:50 AM"
    val seatsAvailable       : Int,
    val pricePerSeat         : Double,
)

// ─── Screen ───────────────────────────────────────────────────────────────────

@Composable
fun RequestRideScreen(
    ride             : RideRequestDetails,
    onBackClick      : () -> Unit,
    onAddStopClick   : () -> Unit = {},
    onConfirmRequest : (seats: Int, notes: String) -> Unit,
    onCancel         : () -> Unit,
    modifier         : Modifier = Modifier,
) {
    var seatCount by remember { mutableIntStateOf(1) }
    var notes     by remember { mutableStateOf("") }

    val totalAmount = ride.pricePerSeat * seatCount

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
                pickupLabel      = ride.pickupLabel,
                destinationLabel = ride.destinationLabel,
                onAddStopClick   = onAddStopClick,
            )

            TripDetailsRow(
                departureTime   = ride.departureTime,
                arrivalEstimate = ride.arrivalEstimate,
                seatsAvailable  = ride.seatsAvailable,
                pricePerSeat    = ride.pricePerSeat,
            )

            SeatStepperCard(
                seatCount    = seatCount,
                maxSeats     = ride.seatsAvailable,
                pricePerSeat = ride.pricePerSeat,
                onIncrement  = { if (seatCount < ride.seatsAvailable) seatCount++ },
                onDecrement  = { if (seatCount > 1) seatCount-- },
            )

            NotesField(
                notes         = notes,
                onNotesChange = { notes = it },
            )

            TotalAmountRow(totalAmount = totalAmount)

            // ── Confirm button ────────────────────────────────────────────────
            Button(
                onClick  = { onConfirmRequest(seatCount, notes) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape    = RoundedCornerShape(14.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            ) {
                Text(
                    text       = "Confirm Request",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White,
                )
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

/** +/− stepper for selecting seat count with live price-per-seat label. */
@Composable
private fun SeatStepperCard(
    seatCount    : Int,
    maxSeats     : Int,
    pricePerSeat : Double,
    onIncrement  : () -> Unit,
    onDecrement  : () -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = SurfaceGrey),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Number of Seats", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                Text("R${"%.2f".format(pricePerSeat)} per person", fontSize = 12.sp, color = TextMuted)
            }

            StepperButton(icon = Icons.Filled.Remove, enabled = seatCount > 1,        onClick = onDecrement)
            Text(
                text       = seatCount.toString(),
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = OrangeAccent,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.width(36.dp),
            )
            StepperButton(icon = Icons.Filled.Add,    enabled = seatCount < maxSeats, onClick = onIncrement)
        }
    }
}

/** Circular +/- button used in SeatStepperCard. */
@Composable
private fun StepperButton(
    icon    : ImageVector,
    enabled : Boolean,
    onClick : () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (enabled) CardWhite else BorderLight)
            .border(1.dp, BorderLight, CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = if (enabled) NavyPrimary else TextMuted,
            modifier           = Modifier.size(18.dp),
        )
    }
}

/** Optional free-text notes field for the driver. */
@Composable
private fun NotesField(
    notes         : String,
    onNotesChange : (String) -> Unit,
) {
    Column {
        Text("Notes for Driver", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value         = notes,
            onValueChange = onNotesChange,
            placeholder   = { Text("e.g., Wait at the library stairs", fontSize = 13.sp, color = TextHint) },
            modifier      = Modifier.fillMaxWidth().height(88.dp),
            shape         = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            colors        = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = NavyPrimary,
                unfocusedBorderColor = BorderLight,
            ),
        )
    }
}

/** Final seats × price summary row shown above the confirm button. */
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Request Ride — 2 seats")
@Composable
private fun RequestRidePreview() {
    MaterialTheme {
        RequestRideScreen(
            ride = RideRequestDetails(
                tripId               = 1L,
                driverName           = "Kelvin M.",
                driverRating         = 4.9,
                driverRidesCompleted = 120,
                driverAvatarUrl      = null,
                carDescription       = "Silver Toyota Corolla",
                plate                = "ABC-1234",
                pickupLabel          = "Main Gate (NMU North Campus)",
                destinationLabel     = "Engineering Hub",
                departureTime        = "08:30 AM",
                arrivalEstimate      = "08:50 AM",
                seatsAvailable       = 2,
                pricePerSeat         = 25.00,
            ),
            onBackClick      = {},
            onConfirmRequest = { _, _ -> },
            onCancel         = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Request Ride — 1 seat left")
@Composable
private fun RequestRideOneSeatPreview() {
    MaterialTheme {
        RequestRideScreen(
            ride = RideRequestDetails(
                tripId               = 2L,
                driverName           = "Thandiwe N.",
                driverRating         = 4.7,
                driverRidesCompleted = 42,
                driverAvatarUrl      = null,
                carDescription       = "White VW Polo",
                plate                = "NMU-2288",
                pickupLabel          = "Missionvale Campus",
                destinationLabel     = "South Campus Library",
                departureTime        = "07:15 AM",
                arrivalEstimate      = "07:40 AM",
                seatsAvailable       = 1,
                pricePerSeat         = 18.50,
            ),
            onBackClick      = {},
            onConfirmRequest = { _, _ -> },
            onCancel         = {},
        )
    }
}