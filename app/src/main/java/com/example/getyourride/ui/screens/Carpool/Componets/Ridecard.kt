// ─────────────────────────────────────────────────────────────────────────────
// RideCard.kt
// Package: com.example.getyourride.ui.components
// Reusable card used by MyRidesScreen (and available for CarpoolHomeScreen
// if you want to consolidate CarpoolBookingCard onto the same component later).
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

enum class RideStatus { ACTIVE, SCHEDULED, COMPLETED, CANCELLED }

data class RideCardData(
    val id             : String,
    val driverName     : String,
    val carDescription : String,
    val plate          : String,
    val status         : RideStatus,
    val pickup         : String,
    val dropoff        : String,
    val dateLabel      : String,
    val timeLabel      : String,
)

@Composable
fun RideCard(
    ride         : RideCardData,
    onTrackRide  : () -> Unit = {},
    onCancelRide : () -> Unit = {}, // parent updates ride.status = CANCELLED (locally and/or via API) here
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(ride.driverName, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    Text("${ride.carDescription} • ${ride.plate}", fontSize = 12.sp, color = TextMuted)
                }
                StatusPill(status = ride.status)
            }

            Spacer(Modifier.height(14.dp))

            LocationLine(label = "PICKUP", value = ride.pickup, icon = Icons.Outlined.LocationOn, tint = NavyPrimary)
            Spacer(Modifier.height(8.dp))
            LocationLine(label = "DROP-OFF", value = ride.dropoff, icon = Icons.Outlined.Navigation, tint = OrangeAccent)

            Spacer(Modifier.height(14.dp))

            Row(
                modifier          = Modifier.fillMaxWidth().background(SurfaceGrey, RoundedCornerShape(10.dp)).padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Outlined.CalendarToday, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(ride.dateLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                    Text(ride.timeLabel, fontSize = 12.sp, color = TextMuted)
                }
            }

            if (ride.status == RideStatus.ACTIVE || ride.status == RideStatus.SCHEDULED) {
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick  = onTrackRide,
                        shape    = RoundedCornerShape(12.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Track Ride", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    OutlinedButton(
                        onClick  = { showCancelDialog = true },
                        shape    = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                    ) {
                        Text("Cancel Ride", fontSize = 13.sp, color = NavyPrimary)
                    }
                }
            }
        }
    }

    if (showCancelDialog) {
        val message = buildAnnotatedString {
            append("Are you sure you want to cancel your ride to ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = NavyPrimary)) {
                append(ride.dropoff)
            }
            append("? This action cannot be undone.")
        }
        ConfirmationDialog(
            data = ConfirmationDialogData(
                title = "Cancel Booking?",
                message = message,
                confirmLabel = "Yes, Cancel Booking",
                dismissLabel = "No, Keep It",
            ),
            onConfirm = {
                showCancelDialog = false
                onCancelRide()
            },
            onDismiss = { showCancelDialog = false },
        )
    }
}

@Composable
private fun LocationLine(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tint: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
            Text(value, fontSize = 13.sp, color = NavyPrimary)
        }
    }
}

// Reuses the same "pill" pattern as RecentTripsSection's Completed badge:
// color at 12% alpha for the background, full color for the text.
@Composable
private fun StatusPill(status: RideStatus) {
    val (color, label) = when (status) {
        RideStatus.ACTIVE    -> StatusCompleted to "ACTIVE"
        RideStatus.SCHEDULED -> OrangeAccent to "SCHEDULED"
        RideStatus.COMPLETED -> StatusCompleted to "COMPLETED"
        RideStatus.CANCELLED -> Color(0xFFD32F2F) to "CANCELLED"
    }
    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.12f)) {
        Text(
            text       = label,
            fontSize   = 10.sp,
            fontWeight = FontWeight.Bold,
            color      = color,
            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun RideCardPreview() {
    val sampleRide = RideCardData(
        id = "1",
        driverName = "Alex Rivera",
        carDescription = "Toyota Corolla",
        plate = "ABC 123 EC",
        status = RideStatus.ACTIVE,
        pickup = "Engineering Bldg",
        dropoff = "City Tech Hub",
        dateLabel = "Today, 24 Oct",
        timeLabel = "08:30 AM"
    )

    GetYourRideTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RideCard(ride = sampleRide)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RideCardCancelledPreview() {
    val cancelledRide = RideCardData(
        id = "2",
        driverName = "Alex Rivera",
        carDescription = "Toyota Corolla",
        plate = "ABC 123 EC",
        status = RideStatus.CANCELLED,
        pickup = "Engineering Bldg",
        dropoff = "East Residence",
        dateLabel = "Today, 24 Oct",
        timeLabel = "08:30 AM"
    )

    GetYourRideTheme {
        Box(modifier = Modifier.padding(16.dp)) {
            RideCard(ride = cancelledRide)
        }
    }
}