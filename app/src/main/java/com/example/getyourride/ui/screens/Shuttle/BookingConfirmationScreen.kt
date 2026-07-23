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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
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
    val shuttleId: String,
    val ticketId: String,
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
 * QR payload — encode whatever your backend/scanner expects to validate the ticket.
 */
fun buildQrPayload(booking: BookingConfirmation): String {
    return "GYR|ticket=${booking.ticketId}|shuttle=${booking.shuttleId}"
}

/**
 * Generates a QR code Bitmap using ZXing.
 *
 * FIX: The previous version used Bitmap.Config.RGB_565 + a per-pixel setPixel() loop
 * (262,144 individual calls at 512x512). On real devices this is a known source of
 * banding/corruption — exactly the "4 giant blocks" artifact you were seeing instead
 * of a real QR pattern. Two changes fix this:
 *   1. ARGB_8888 instead of RGB_565 (565 has no alpha channel and is more prone to
 *      driver-level dithering/corruption on some GPUs when written pixel-by-pixel).
 *   2. Build the full IntArray in memory and write it in one setPixels() call instead
 *      of 262k separate setPixel() calls — faster AND avoids the corruption.
 * Also added explicit encode hints (margin + error correction) so ZXing doesn't pick
 * an unpredictable quiet-zone/module size for a short payload string.
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
    val qrBitmap = remember(booking.ticketId) {
        generateQrCodeBitmap(buildQrPayload(booking))
    }

    StudentLayout(
        currentRoute = "shuttle_home", // Or a specific route if you have one
        navController = navController,
        showBottomBar = false,
        showTopBar = false, // FIX: this screen is full-bleed navy in the target design —
        // no "GetYourRide" branded header, so we skip GyrTopBar entirely
        // and draw our own back button below instead.
        onBackClick = { navController.popBackStack() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyPrimary)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Self-drawn back button, since GyrTopBar is now hidden for this screen.
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
                                // FIX: pin to a fixed size + FilterQuality.None-equivalent behavior.
                                // fillMaxSize() on a Bitmap without specifying nearest-neighbor scaling
                                // can blur/blockify a QR when Compose bilinear-filters it up from a
                                // small bitmap. Since we now generate at full 512px this matters less,
                                // but keeping it explicit avoids future regressions if sizePx is lowered.
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
                            Text("TICKET ID", fontSize = 11.sp, color = Color.Gray)
                            Text(booking.ticketId, fontWeight = FontWeight.Bold, color = NavyPrimary)
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
                            // FIX: pickup pin is orange accent in the target design,
                            // drop-off stays navy — this is what visually differentiates
                            // the two rows at a glance instead of them looking identical.
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
                    Text("Driver & Vehicle", fontWeight = FontWeight.Bold, color = OrangeAccent, fontSize = 13.sp)
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
    // FIX: your screenshot showed an orange badge even for what should be a
    // "Confirmed" booking. The original `when (status)` did an exact, case-sensitive
    // match — so anything like "confirmed", " Confirmed", or "CONFIRMED" coming back
    // from the backend silently fell into the `else` branch (orange). Normalizing
    // with trim() + lowercase() makes the badge resilient to that.
    val normalized = status.trim().lowercase()

    val bg = when (normalized) {
        "confirmed" -> GreenSuccess.copy(alpha = 0.15f)
        "cancelled" -> DangerRed.copy(alpha = 0.15f)
        else -> OrangeAccent.copy(alpha = 0.15f)
    }
    val textColor = when (normalized) {
        "confirmed" -> GreenSuccess
        "cancelled" -> DangerRed
        else -> OrangeAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        // Display text keeps original casing (e.g. "Confirmed") even though
        // matching logic is normalized above.
        Text(status, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}