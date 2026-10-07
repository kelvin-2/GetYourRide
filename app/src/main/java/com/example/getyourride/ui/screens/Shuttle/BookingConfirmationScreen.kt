package com.example.getyourride.ui.screens.shuttle

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.theme.BorderLight
import com.example.getyourride.ui.theme.CardWhite
import com.example.getyourride.ui.theme.DangerRed
import com.example.getyourride.ui.theme.GreenSuccess
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.example.getyourride.ui.theme.SurfaceGrey
import com.example.getyourride.ui.theme.TextMuted
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

// ---------- Data model ----------
data class BookingConfirmation(
    val shuttleId: String,
    val ticketId: String,
    val pickupLocation: String,
    val dropoffLocation: String,
    val date: String,
    val departureTime: String,
    val driverName: String,
    val plateNumber: String,
    val vehicleModel: String,
    val status: String = "Confirmed",
    // Real backend booking id. Encoded into the QR so the shuttle driver's scanner
    // can mark this exact booking as boarded. Null only for previews/placeholders.
    val bookingId: Long? = null
)

/**
 * QR payload — the shuttle driver's scanner reads this to board the student.
 * booking=<id> is the key part: it identifies the exact booking to mark boarded.
 * ticket/shuttle are included for display/debugging.
 */
fun buildQrPayload(booking: BookingConfirmation): String {
    return "GYR|booking=${booking.bookingId ?: ""}|ticket=${booking.ticketId}|shuttle=${booking.shuttleId}"
}

/**
 * Generates a QR code Bitmap using ZXing.
 *
 * Uses ARGB_8888 + a single setPixels() call (instead of RGB_565 + per-pixel
 * setPixel(), which was the source of the earlier "4 giant blocks" corruption
 * on real devices). Explicit encode hints keep the quiet zone and error
 * correction predictable for short payload strings.
 */
fun generateQrCodeBitmap(content: String, sizePx: Int = 512): Bitmap? {
    return try {
        val hints = mapOf(
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,
            EncodeHintType.MARGIN to 1 // thin quiet zone; card padding already gives breathing room
        )
        val writer = QRCodeWriter()
        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, sizePx, sizePx, hints)

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
        }

        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bmp.setPixels(pixels, 0, width, 0, 0, width, height)
        bmp
    } catch (e: Exception) {
        null
    }
}

/**
 * Shuttle booking confirmation — styled to match the carpool
 * BookingConfirmedScreen: white background, orange success icon, grey summary
 * cards, and a navy "View My Rides" button. The shuttle-specific QR ticket is
 * shown in the first card. There is no Download Ticket button.
 */
@Composable
fun BookingConfirmationScreen(
    // Kept so existing callers still compile; this screen no longer needs it.
    navController: NavController,
    booking: BookingConfirmation,
    onViewMyRides: () -> Unit,
    // No longer used: the Download Ticket button was removed (tickets are not
    // downloadable). Kept with a default so existing callers still compile.
    onDownloadTicket: () -> Unit = {}
) {
    // Generate QR bitmap once per booking
    val qrBitmap = remember(booking.ticketId) {
        generateQrCodeBitmap(buildQrPayload(booking))
    }

    Scaffold(containerColor = CardWhite) { padding ->
        Column(
            modifier = Modifier
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
                text = "Your shuttle seat is successfully reserved.",
                fontSize = 14.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            )

            Spacer(Modifier.height(20.dp))

            // ---------- QR + IDs Card ----------
            QrTicketCard(
                qrBitmap = qrBitmap,
                shuttleId = booking.shuttleId,
                ticketId = booking.ticketId,
            )

            Spacer(Modifier.height(12.dp))

            // ---------- Route + date/time Card ----------
            RouteSummaryCard(
                pickupLabel = booking.pickupLocation,
                destinationLabel = booking.dropoffLocation,
                date = booking.date,
                departureTime = booking.departureTime,
                status = booking.status,
            )

            Spacer(Modifier.height(12.dp))

            // ---------- Driver & Vehicle Card ----------
            DriverVehicleCard(
                driverName = booking.driverName,
                vehicleModel = booking.vehicleModel,
                plate = booking.plateNumber,
            )

            Spacer(Modifier.height(24.dp))

            // ---------- Button ----------
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
private fun QrTicketCard(
    qrBitmap: Bitmap?,
    shuttleId: String,
    ticketId: String,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Ticket QR Code",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                    )
                } else {
                    Icon(
                        Icons.Filled.QrCode2,
                        contentDescription = "QR unavailable",
                        modifier = Modifier.size(80.dp),
                        tint = NavyPrimary,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("SHUTTLE ID", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(shuttleId, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("TICKET ID", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(ticketId, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }
        }
    }
}

@Composable
private fun RouteSummaryCard(
    pickupLabel: String,
    destinationLabel: String,
    date: String,
    departureTime: String,
    status: String,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGrey),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(NavyPrimary),
                )
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("PICKUP", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(pickupLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
                StatusBadge(status = status)
            }
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = DangerRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(6.dp))
                Column {
                    Text("DROP-OFF", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(destinationLabel, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text("DATE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(date, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("DEPARTURE", fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Medium)
                    Text(departureTime, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = NavyPrimary)
                }
            }
        }
    }
}

@Composable
private fun DriverVehicleCard(
    driverName: String,
    vehicleModel: String,
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
            Text(vehicleModel, fontSize = 13.sp, color = TextMuted)
        }

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

@Composable
private fun StatusBadge(status: String) {
    // Case/whitespace-insensitive match so backend variants like "confirmed"
    // or "CONFIRMED" don't silently fall into the else branch.
    val normalized = status.trim().lowercase()

    // "Confirmed" is an orange badge (not green) — matching the app's accent
    // color language rather than a literal traffic-light scheme.
    val color = when (normalized) {
        "completed" -> GreenSuccess
        "cancelled" -> DangerRed
        else -> OrangeAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Preview ----------
@Preview(showBackground = true)
@Composable
private fun BookingConfirmationScreenPreview() {
    val sampleBooking = BookingConfirmation(
        shuttleId = "SH-1024",
        ticketId = "NMU-9928-AX",
        pickupLocation = "North Campus Main Gate",
        dropoffLocation = "South Campus",
        date = "Today",
        departureTime = "08:30 AM",
        driverName = "Markus Taylor",
        plateNumber = "NMU-042-EC",
        vehicleModel = "Toyota Quantum",
        status = "Confirmed"
    )
    BookingConfirmationScreen(
        navController = rememberNavController(),
        booking = sampleBooking,
        onViewMyRides = {}
    )
}