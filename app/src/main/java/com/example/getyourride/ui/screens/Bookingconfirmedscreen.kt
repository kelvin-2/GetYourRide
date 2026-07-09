// ─────────────────────────────────────────────────────────────────────────────
// BookingConfirmedScreen.kt
// Package: com.example.getyourride.ui.screens.Rides
//
// PURPOSE — Shown right after RequestRideScreen's booking call succeeds.
// Reachable only via the "booking_confirmed" route, which reads the
// BookingConfirmationDetails that MainActivity stashed from the same
// RideRequestDetails already loaded on RequestRideScreen (see wiring notes
// in MainActivity.kt).
//
// NAVIGATION — Called from RequestRideScreen's onBookingSuccess:
//   onBookingSuccess = { trip ->
//       confirmedBooking = ride.toBookingConfirmationDetails()
//       navController.navigate("booking_confirmed") {
//           popUpTo(GyrRoutes.HOME)
//       }
//   }
//
// There is deliberately no "Add to Calendar" action here — Download Receipt
// spans the full width instead.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Rides

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

/**
 * Everything BookingConfirmedScreen needs to render. Built directly from the
 * RideRequestDetails RequestRideScreen already has in scope — see
 * RideRequestDetails.toBookingConfirmationDetails() below — so there's no
 * second network call or TripResponse re-mapping involved.
 */
data class BookingConfirmationDetails(
    val driverName: String,
    val driverRating: Double,
    val carDescription: String,
    val plate: String,
    val pickupLabel: String,
    val destinationLabel: String,
)

/** Maps the details RequestRideScreen already has into confirmation-screen details. */
fun RideRequestDetails.toBookingConfirmationDetails(): BookingConfirmationDetails =
    BookingConfirmationDetails(
        driverName = driverName,
        driverRating = driverRating,
        carDescription = carDescription,
        plate = plate,
        pickupLabel = pickupLabel,
        destinationLabel = destinationLabel,
    )

@Composable
fun BookingConfirmedScreen(
    details: BookingConfirmationDetails,
    onDownloadReceipt: () -> Unit,
    onViewMyRides: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(containerColor = CardWhite) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Spacer(Modifier.height(28.dp))

            // Success icon
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Booking confirmed",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp),
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Booking Confirmed!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = NavyPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Your carpool with ${details.driverName} is scheduled and ready for pickup.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )

            Spacer(Modifier.height(20.dp))

            RouteSummaryCard(
                pickupLabel = details.pickupLabel,
                destinationLabel = details.destinationLabel,
            )

            Spacer(Modifier.height(12.dp))

            DriverVehicleCard(
                driverName = details.driverName,
                driverRating = details.driverRating,
                carDescription = details.carDescription,
                plate = details.plate,
            )

            Spacer(Modifier.height(24.dp))

            // Download receipt — full width, no calendar action next to it
            OutlinedButton(
                onClick = onDownloadReceipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangeAccent),
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(text = "Download Receipt", fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onViewMyRides,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
            ) {
                Text(text = "View My Rides", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun RouteSummaryCard(pickupLabel: String, destinationLabel: String) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            Spacer(Modifier.height(10.dp))
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

@Composable
private fun DriverVehicleCard(
    driverName: String,
    driverRating: Double,
    carDescription: String,
    plate: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardWhite)
            .border(1.dp, BorderLight, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(OrangeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(22.dp),
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
             Text(
                text = "DRIVER & VEHICLE INFO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted,
            )
            Spacer(Modifier.height(2.dp))
            Text(driverName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            Text(carDescription, fontSize = 13.sp, color = TextMuted)
        }

        Column(horizontalAlignment = Alignment.End) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(OrangeAccent.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(2.dp))
                Text(driverRating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
            }
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(NavyPrimary.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text(plate, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
            }
        }
    }
}