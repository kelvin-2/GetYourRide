package com.example.getyourride.ui.screens.shuttle

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.theme.CardWhite
import com.example.getyourride.ui.theme.DangerRed
import com.example.getyourride.ui.theme.GreenSuccess
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

// ---------- Data model ----------
data class BookingConfirmation(
    // Real trip_booking.booking_id — there is no separate "ticket" concept in the database,
    // so this IS the ticket. Nullable is not allowed here on purpose: a screen with no real
    // booking id should never reach this composable (see MainActivity's onShowTicket / onBookingConfirmed).
    val bookingId: Long,
    val shuttleId: String, // this is trip_id, kept as String for existing display code
    val studentFirstName: String,
    val studentLastName: String,
    val studentNumber: String,
    val pickupLocation: String,
    val dropoffLocation: String,
    val date: String,
    val departureTime: String,
    val driverName: String,
    val plateNumber: String,
    val vehicleModel: String,
    val status: String = "Confirmed"
)

/**
 * QR payload — must match what ShuttleDriverScanQrScreen.parseScannedStudentQr() expects:
 * booking_id=..;trip_id=..;first_name=..;last_name=..;student_number=..
 */
fun buildQrPayload(booking: BookingConfirmation): String {
    return "booking_id=${booking.bookingId};trip_id=${booking.shuttleId};" +
        "first_name=${booking.studentFirstName};last_name=${booking.studentLastName};" +
        "student_number=${booking.studentNumber}"
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

@Composable
fun BookingConfirmationScreen(
    navController: NavController,
    booking: BookingConfirmation,
    onViewMyRides: () -> Unit,
    onDownloadTicket: () -> Unit
) {
    // Generate QR bitmap once per booking
    val qrBitmap = remember(booking.bookingId) {
        generateQrCodeBitmap(buildQrPayload(booking))
    }

    StudentLayout(
        currentRoute = "shuttle_home",
        navController = navController,
        showBottomBar = false,
        showTopBar = false, // full-bleed navy design — no GyrTopBar, we draw our own back arrow
        onBackClick = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyPrimary)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Self-drawn back button, since GyrTopBar is hidden for this screen.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Success icon
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
            }

            Spacer(Modifier.height(12.dp))
            Text("Booking Confirmed", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                "Your shuttle seat is successfully reserved.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            // ---------- QR + IDs Card ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Ticket QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Filled.QrCode2,
                                contentDescription = "QR unavailable",
                                modifier = Modifier.size(80.dp),
                                tint = NavyPrimary
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("SHUTTLE ID", fontSize = 11.sp, color = Color.Gray)
                            Text(booking.shuttleId, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("BOOKING ID", fontSize = 11.sp, color = Color.Gray)
                            Text("#${booking.bookingId}", fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Pickup / Drop-off / Date Card ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Pickup", fontSize = 12.sp, color = Color.Gray)
                        }
                        StatusBadge(status = booking.status)
                    }
                    Text(booking.pickupLocation, fontWeight = FontWeight.Bold, color = NavyPrimary)

                    Spacer(Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.NearMe, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Drop-off", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(booking.dropoffLocation, fontWeight = FontWeight.Bold, color = NavyPrimary)

                    Spacer(Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Date", fontSize = 12.sp, color = Color.Gray)
                            Text(booking.date, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Departure", fontSize = 12.sp, color = Color.Gray)
                            Text(booking.departureTime, fontWeight = FontWeight.Bold, color = NavyPrimary)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ---------- Driver & Vehicle Card ----------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // FIX: mockup shows this label in uppercase small-caps style
                    Text(
                        "DRIVER & VEHICLE",
                        fontWeight = FontWeight.Bold,
                        color = OrangeAccent,
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    InfoRow("Driver", booking.driverName)
                    InfoRow("Shuttle ID", booking.shuttleId)
                    InfoRow("Plate Number", booking.plateNumber)
                    InfoRow("Vehicle", booking.vehicleModel)
                }
            }

            Spacer(Modifier.height(20.dp))

            // ---------- Buttons ----------
            Button(
                onClick = onViewMyRides,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
            ) {
                Text("View My Rides", color = Color.White)
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                onClick = onDownloadTicket,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("Download Ticket")
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = Color.Gray)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = NavyPrimary)
    }
}

@Composable
private fun StatusBadge(status: String) {
    // Case/whitespace-insensitive match so backend variants like "confirmed"
    // or "CONFIRMED" don't silently fall into the else branch.
    val normalized = status.trim().lowercase()

    // FIX: mockup shows "Confirmed" as an orange badge (not green) — matching
    // the app's accent color language rather than a literal traffic-light scheme.
    val bg = when (normalized) {
        "confirmed" -> OrangeAccent.copy(alpha = 0.15f)
        "completed" -> GreenSuccess.copy(alpha = 0.15f)
        "cancelled" -> DangerRed.copy(alpha = 0.15f)
        else -> OrangeAccent.copy(alpha = 0.15f)
    }
    val textColor = when (normalized) {
        "confirmed" -> OrangeAccent
        "completed" -> GreenSuccess
        "cancelled" -> DangerRed
        else -> OrangeAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(status, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ---------- Preview ----------
@Preview(showBackground = true, backgroundColor = 0xFF0B1F3A)
@Composable
private fun BookingConfirmationScreenPreview() {
    val sampleBooking = BookingConfirmation(
        bookingId = 9928L,
        shuttleId = "SH-1024",
        studentFirstName = "Alex",
        studentLastName = "Thompson",
        studentNumber = "ST88291",
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
        onViewMyRides = {},
        onDownloadTicket = {}
    )
}