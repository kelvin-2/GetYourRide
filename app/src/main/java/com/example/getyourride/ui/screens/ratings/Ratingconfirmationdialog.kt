package com.example.getyourride.ui.screens.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.getyourride.ui.theme.*

/**
 * "Rating Submitted!" pop-up. Shown after RateTripScreen's onSubmit
 * succeeds — pass it the trip and the values the person actually picked
 * (real data, no placeholders): driver name, the star rating they gave,
 * and whichever "what went well" tags they selected.
 *
 * No "View Trip Receipt" button — that feature doesn't exist in this app,
 * so it isn't offered here.
 */
@Composable
fun RatingConfirmationDialog(
    driverName: String,
    rating: Int,
    compliments: List<String>,
    onBackToHome: () -> Unit,
) {
    Dialog(
        onDismissRequest = onBackToHome,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(CardWhite)
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(GreenSuccess),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Rating Submitted!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(Modifier.height(6.dp))
                Text(
                    "Thank you for helping keep the GetYourRide campus community safe and reliable.",
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(20.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceGrey)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryRow(label = "Driver") {
                        Text(driverName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    }
                    SummaryRow(label = "Your Rating") {
                        Row {
                            repeat(rating.coerceIn(0, 5)) {
                                Icon(
                                    Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                    if (compliments.isNotEmpty()) {
                        SummaryRow(label = "Compliments", alignTop = true) {
                            ComplimentChips(compliments)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onBackToHome,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary)
                ) {
                    Text("Back to Home", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    alignTop: Boolean = false,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = if (alignTop) Alignment.Top else Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextMuted)
        value()
    }
}

@Composable
private fun ComplimentChips(compliments: List<String>) {
    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        compliments.chunked(2).forEach { rowTags ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                rowTags.forEach { tag ->
                    val icon = defaultWentWellOptions.firstOrNull { it.label == tag }?.icon
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(OrangeAccent.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (icon != null) {
                            Icon(icon, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(tag, fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RatingConfirmationDialogPreview() {
    MaterialTheme {
        RatingConfirmationDialog(
            driverName = "Kelvin M.",
            rating = 5,
            compliments = listOf("On time", "Friendly driver"),
            onBackToHome = {}
        )
    }
}