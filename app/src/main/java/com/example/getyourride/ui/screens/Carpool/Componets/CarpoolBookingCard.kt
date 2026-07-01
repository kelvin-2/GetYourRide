// ─────────────────────────────────────────────────────────────────────────────
// CarpoolBookingCard.kt
// Package: com.example.getyourride.ui.screens.Carpool.components
//
// Redesigned ride card — avatar, driver name + rating, seats-left badge,
// departure time, route, price per seat, "Request Ride" pill button.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Carpool.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

data class CarpoolRide(
    val driverName: String,
    val driverInitials: String,
    val rating: Double,
    val ratingCount: Int,
    val seatsLeft: Int,
    val departureTime: String,
    val fromLocation: String,
    val toLocation: String,
    val pricePerSeat: String,
)

// Sample data used by CarpoolHomeScreen preview / section
val sampleRide1 = CarpoolRide(
    driverName = "Alex Rivera",
    driverInitials = "AR",
    rating = 4.9,
    ratingCount = 42,
    seatsLeft = 3,
    departureTime = "08:30 AM",
    fromLocation = "Engineering Bldg",
    toLocation = "City Tech Hub",
    pricePerSeat = "R4.50",
)

val sampleRide2 = CarpoolRide(
    driverName = "Thando Mokoena",
    driverInitials = "TM",
    rating = 4.7,
    ratingCount = 28,
    seatsLeft = 1,
    departureTime = "09:15 AM",
    fromLocation = "South Campus",
    toLocation = "Summerstrand",
    pricePerSeat = "R5.00",
)

@Composable
fun CarpoolBookingCard(
    ride: CarpoolRide,
    onBookClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // ── Row 1: avatar + name/rating on the left, seats badge on the right ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Initials avatar
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(NavyPrimary.copy(alpha = 0.10f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text       = ride.driverInitials,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyPrimary,
                        )
                    }

                    Spacer(Modifier.width(10.dp))

                    Column {
                        Text(
                            text       = ride.driverName,
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyPrimary,
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector        = Icons.Outlined.Star,
                                contentDescription = null,
                                tint               = OrangeAccent,
                                modifier           = Modifier.size(13.dp),
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text     = "${ride.rating} (${ride.ratingCount})",
                                fontSize = 12.sp,
                                color    = TextMuted,
                            )
                        }
                    }
                }

                // Seats left badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GreenSuccess.copy(alpha = 0.12f),
                ) {
                    Text(
                        text       = "${ride.seatsLeft} seat${if (ride.seatsLeft == 1) "" else "s"} left",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = GreenSuccess,
                        modifier   = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── Departure time ──────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint               = TextMuted,
                    modifier           = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text     = "Departure: ",
                    fontSize = 13.sp,
                    color    = TextMuted,
                )
                Text(
                    text       = ride.departureTime,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = NavyPrimary,
                )
            }

            Spacer(Modifier.height(6.dp))

            // ── Route ────────────────────────────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint               = TextMuted,
                    modifier           = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text     = "${ride.fromLocation} → ${ride.toLocation}",
                    fontSize = 13.sp,
                    color    = TextMuted,
                )
            }

            Spacer(Modifier.height(14.dp))

            // ── Price + Request Ride button ─────────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text       = ride.pricePerSeat,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NavyPrimary,
                    )
                    Text(
                        text     = "per seat",
                        fontSize = 11.sp,
                        color    = TextMuted,
                    )
                }

                Button(
                    onClick        = onBookClick,
                    shape          = RoundedCornerShape(20.dp),
                    colors         = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 10.dp),
                    modifier       = Modifier.defaultMinSize(minHeight = 1.dp),
                ) {
                    Text(
                        text       = "Request Ride",
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = androidx.compose.ui.graphics.Color.White,
                    )
                }
            }
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun CarpoolBookingCardPreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CarpoolBookingCard(ride = sampleRide1, onBookClick = {})
            CarpoolBookingCard(ride = sampleRide2, onBookClick = {})
        }
    }
}