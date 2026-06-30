// ─────────────────────────────────────────────────────────────────────────────
// CarpoolBookingCard.kt
// Package: com.example.getyourride.ui.screens.Carpool.components
//
// PURPOSE — Reusable card showing a single available carpool ride.
//
// REUSE — Use this anywhere you need to display a bookable carpool:
//   - CarpoolHomeScreen  (Available Carpools section)
//   - RidesScreen        (full list of available rides)
//   - SearchResultsScreen
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Carpool.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

// ─── Data model for a carpool ride card ──────────────────────────────────────

/**
 * All the data a [CarpoolBookingCard] needs to render.
 *
 * @param from          Pickup location name e.g. "Summerstrand"
 * @param to            Destination name e.g. "Missionvale Campus"
 * @param departureTime Time string e.g. "07:45"
 * @param date          Date string e.g. "Today, 14 Oct"
 * @param driverName    Short driver name e.g. "Kelvin M."
 * @param seatsLeft     Number of seats still available (affects badge colour)
 */
data class CarpoolRide(
    val from          : String,
    val to            : String,
    val departureTime : String,
    val date          : String,
    val driverName    : String,
    val seatsLeft     : Int,
)

// ─── The card composable ──────────────────────────────────────────────────────

/**
 * Displays a single bookable carpool ride.
 *
 * Seat badge colour:
 *   2+ seats → green  (plenty available)
 *   1 seat   → amber  (almost full)
 *   0 seats  → red    (full — button disabled)
 *
 * @param ride          The ride data to display.
 * @param onBookClick   Called when "Book Seat" is tapped.
 */
@Composable
fun CarpoolBookingCard(
    ride        : CarpoolRide,
    onBookClick : () -> Unit,
    modifier    : Modifier = Modifier,
) {
    // Determine badge colour from seats remaining
    val (badgeColor, badgeText) = when {
        ride.seatsLeft == 0  -> Pair(DangerRed,     "Full")
        ride.seatsLeft == 1  -> Pair(StatusPending, "1 Seat Left")
        else                 -> Pair(GreenSuccess,  "${ride.seatsLeft} Seats Left")
    }

    Card(
        modifier  = modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {

            // ── Top row: route + time + badge ─────────────────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                // Left: FROM → TO with dotted line
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier              = Modifier.weight(1f),
                ) {
                    // Dotted vertical line with circle (from) and pin (to)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(top = 4.dp),
                    ) {
                        Icon(Icons.Outlined.Circle,   contentDescription = null, tint = NavyPrimary,  modifier = Modifier.size(12.dp))
                        // Dotted line between icons
                        repeat(3) {
                            Spacer(Modifier.height(3.dp))
                            Box(Modifier.size(width = 2.dp, height = 4.dp).background(BorderLight))
                        }
                        Spacer(Modifier.height(3.dp))
                        Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                    }

                    // FROM + TO labels and location names
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column {
                            Text("FROM", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
                            Text(ride.from, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }
                        Column {
                            Text("TO", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, letterSpacing = 0.5.sp)
                            Text(ride.to, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                        }
                    }
                }

                // Right: departure time + date + seats badge
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    // Seats badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = badgeColor.copy(alpha = 0.12f),
                    ) {
                        Text(
                            text       = badgeText,
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color      = badgeColor,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        )
                    }

                    // Departure time
                    Text(
                        text       = ride.departureTime,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color      = NavyPrimary,
                    )
                    Text(
                        text     = ride.date,
                        fontSize = 11.sp,
                        color    = TextMuted,
                    )
                }
            }

            HorizontalDivider(color = BorderLight)

            // ── Bottom row: driver name + Book Seat button ────────────────────
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                // Driver avatar + name
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(SurfaceGrey),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Person, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                    }
                    Text("by ${ride.driverName}", fontSize = 12.sp, color = TextMuted)
                }

                // Book Seat button — disabled if full
                Button(
                    onClick  = onBookClick,
                    enabled  = ride.seatsLeft > 0,
                    shape    = RoundedCornerShape(20.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = OrangeAccent,
                        disabledContainerColor = BorderLight,
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                ) {
                    Text(
                        text       = if (ride.seatsLeft > 0) "Book Seat" else "Full",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (ride.seatsLeft > 0) Color.White else TextMuted,
                    )
                }
            }
        }
    }
}

// ─── Preview data ─────────────────────────────────────────────────────────────

val sampleRide1 = CarpoolRide(
    from          = "Summerstrand",
    to            = "Missionvale Campus",
    departureTime = "07:45",
    date          = "Today, 14 Oct",
    driverName    = "Kelvin M.",
    seatsLeft     = 2,
)

val sampleRide2 = CarpoolRide(
    from          = "Walmer",
    to            = "South Campus",
    departureTime = "08:15",
    date          = "Today, 14 Oct",
    driverName    = "Thandeka N.",
    seatsLeft     = 1,
)

@Preview(showBackground = true, name = "Card — 2 seats left (green)")
@Composable
fun CarpoolCardGreenPreview() {
    MaterialTheme { CarpoolBookingCard(ride = sampleRide1, onBookClick = {}, modifier = Modifier.padding(16.dp)) }
}

@Preview(showBackground = true, name = "Card — 1 seat left (amber)")
@Composable
fun CarpoolCardAmberPreview() {
    MaterialTheme { CarpoolBookingCard(ride = sampleRide2, onBookClick = {}, modifier = Modifier.padding(16.dp)) }
}