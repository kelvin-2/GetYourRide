package com.example.getyourride.ui.screens.ratings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

/**
 * "Rate Your Trip" screen shown after a completed GetYourRide trip.
 * Pure Jetpack Compose / Material3 — no emoji glyphs anywhere; all
 * iconography comes from androidx.compose.material.icons (filled/outlined).
 *
 * Colors all come from ui/theme/GetYourRideColors.kt (NavyPrimary,
 * OrangeAccentAccent, SurfaceGrey, CardWhite, TextMuted, BorderLight, IconTint) —
 * nothing new is defined here.
 */

// ---------- Data models ----------
data class TripRatingData(
    val driverName: String,
    val isVerified: Boolean,
    val driverRating: Double,
    val ridesCompleted: Int,
    val vehicleDescription: String, // e.g. "Silver Toyota Corolla • ABC..."
    val pickupLabel: String,
    val dropoffLabel: String,
    val dateTimeLabel: String,
    val priceLabel: String,
    val seatsLabel: String
)

data class WhatWentWellOption(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

val defaultWentWellOptions = listOf(
    WhatWentWellOption("On time", Icons.Filled.Schedule),
    WhatWentWellOption("Friendly driver", Icons.Filled.SentimentSatisfiedAlt),
    WhatWentWellOption("Clean car", Icons.Filled.AutoAwesome),
    WhatWentWellOption("Safe driving", Icons.Filled.Shield),
    WhatWentWellOption("Smooth route", Icons.Filled.AltRoute),
    WhatWentWellOption("Great music", Icons.Filled.MusicNote)
)

private val ratingLabels = listOf("Poor", "Fair", "Good", "Very Good", "Excellent!")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RateTripScreen(
    trip: TripRatingData,
    onClose: () -> Unit,
    onSkip: () -> Unit,
    onSubmit: (rating: Int, tags: Set<String>, note: String) -> Unit,
    wentWellOptions: List<WhatWentWellOption> = defaultWentWellOptions,
    maxNoteLength: Int = 250
) {
    var rating by remember { mutableStateOf(5) }
    var selectedTags by remember { mutableStateOf(setOf("On time", "Friendly driver")) }
    var note by remember { mutableStateOf("") }

    Scaffold(containerColor = SurfaceGrey) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HeaderSection(trip = trip, onClose = onClose)

            val scrollState = androidx.compose.foundation.rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(Modifier.height(24.dp))
                DriverCard(trip)

                Spacer(Modifier.height(24.dp))
                TripInfoCard(trip)

                Spacer(Modifier.height(20.dp))
                StarRatingSection(
                    rating = rating,
                    onRatingChange = { rating = it }
                )

                Spacer(Modifier.height(24.dp))
                WhatWentWellSection(
                    options = wentWellOptions,
                    selected = selectedTags,
                    onToggle = { label ->
                        selectedTags = if (selectedTags.contains(label)) {
                            selectedTags - label
                        } else {
                            selectedTags + label
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))
                NoteSection(
                    note = note,
                    maxLength = maxNoteLength,
                    onNoteChange = { if (it.length <= maxNoteLength) note = it }
                )

                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = { onSubmit(rating, selectedTags, note) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Text("Submit Rating", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Filled.ArrowForward, contentDescription = null)
                }

                Spacer(Modifier.height(12.dp))
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now", color = TextMuted, fontSize = 14.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ---------- Header ----------
@Composable
private fun HeaderSection(trip: TripRatingData, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(top = 16.dp, bottom = 28.dp, start = 16.dp, end = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
            }
            Text(
                "Rate Your Trip",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            // Balances the close button's width so the title stays centered
            // now that the account icon is gone.
            Spacer(Modifier.width(48.dp))
        }

        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = CardWhite.copy(alpha = 0.75f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                "TRIP COMPLETED",
                color = CardWhite.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Trip Complete!",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "How was your ${timeOfDayLabel()} ride with ${trip.driverName.substringBefore(" ")}?",
            color = CardWhite.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Returns "morning" / "afternoon" / "evening" / "night" based on the
 * device's current local time — no hardcoded time-of-day text.
 */
private fun timeOfDayLabel(): String {
    val hour = java.time.LocalTime.now().hour
    return when (hour) {
        in 5..11 -> "morning"
        in 12..16 -> "afternoon"
        in 17..20 -> "evening"
        else -> "night"
    }
}

// ---------- Driver card ----------
@Composable
private fun DriverCard(trip: TripRatingData) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(trip.driverName, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    if (trip.isVerified) {
                        Spacer(Modifier.width(6.dp))
                        VerifiedBadge()
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(trip.vehicleDescription, color = TextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = OrangeAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        trip.driverRating.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text("${trip.ridesCompleted}+ rides", color = TextMuted, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun VerifiedBadge() {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceGrey)
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Verified,
            contentDescription = null,
            tint = NavyPrimary,
            modifier = Modifier.size(11.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text("Verified", fontSize = 10.sp, color = TextMuted)
    }
}

// ---------- Trip info card (route + date/time + price) ----------
@Composable
private fun TripInfoCard(trip: TripRatingData) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Pickup/dropoff each on their own row now — these labels are full
            // street addresses, and cramming icon+text+arrow+icon+text into a
            // single unconstrained Row let each Text measure against nearly the
            // full row width independently, ballooning the row's height far
            // beyond the visible card (the "huge gap" before the stars/chips).
            // weight(1f) bounds each label so it wraps normally instead.
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.TripOrigin,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    trip.pickupLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(14.dp).padding(top = 2.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    trip.dropoffLabel,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccessTime,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(trip.dateTimeLabel, color = TextMuted, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceGrey)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        "${trip.priceLabel} • ${trip.seatsLabel}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ---------- Star rating ----------
@Composable
private fun StarRatingSection(rating: Int, onRatingChange: (Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            ratingLabels.getOrElse(rating - 1) { "" },
            color = OrangeAccent,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Row {
            for (i in 1..5) {
                Icon(
                    imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Rate $i star${if (i > 1) "s" else ""}",
                    tint = if (i <= rating) OrangeAccent else TextMuted,
                    modifier = Modifier
                        .size(36.dp)
                        .clickable { onRatingChange(i) }
                        .padding(4.dp)
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        Text("Tap a star to rate Kelvin", color = TextMuted, fontSize = 12.sp)
    }
}

// ---------- What went well ----------
@Composable
private fun WhatWentWellSection(
    options: List<WhatWentWellOption>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column {
        Text(
            "WHAT WENT WELL?",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextMuted,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(10.dp))

        // Wrap chips across rows (simple 2-row flow using two Rows via chunking)
        options.chunked(3).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowOptions.forEach { option ->
                    WentWellChip(
                        option = option,
                        isSelected = selected.contains(option.label),
                        onClick = { onToggle(option.label) }
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun WentWellChip(
    option: WhatWentWellOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) OrangeAccent.copy(alpha = 0.15f) else SurfaceGrey)
            .border(
                width = if (isSelected) 1.dp else 0.dp,
                color = if (isSelected) OrangeAccent else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            option.icon,
            contentDescription = null,
            tint = if (isSelected) OrangeAccent else TextMuted,
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            option.label,
            fontSize = 12.sp,
            color = if (isSelected) TextPrimary else TextMuted,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

// ---------- Note field ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteSection(note: String, maxLength: Int, onNoteChange: (String) -> Unit) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "LEAVE A NOTE",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                letterSpacing = 0.5.sp
            )
            Text("${note.length} / $maxLength", fontSize = 11.sp, color = TextMuted)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            placeholder = {
                Text(
                    "Kelvin was super punctual, had great campus vibes...",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            shape = RoundedCornerShape(14.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardWhite,
                focusedContainerColor = CardWhite,
                unfocusedBorderColor = BorderLight,
                focusedBorderColor = OrangeAccent
            )
        )
    }
}

// ---------- Preview / usage example ----------
@Preview(showBackground = true)
@Composable
private fun RateTripScreenPreview() {
    MaterialTheme {
        RateTripScreen(
            trip = TripRatingData(
                driverName = "Kelvin M.",
                isVerified = true,
                driverRating = 4.9,
                ridesCompleted = 120,
                vehicleDescription = "Silver Toyota Corolla • ABC...",
                pickupLabel = "NMU North Campus",
                dropoffLabel = "Engineering Hub",
                dateTimeLabel = "Today, 08:30 AM",
                priceLabel = "R25.00",
                seatsLabel = "1 Seat"
            ),
            onClose = {},
            onSkip = {},
            onSubmit = { _, _, _ -> }
        )
    }
}