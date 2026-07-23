package com.example.getyourride.ui.screens.shuttle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getyourride.ui.screens.shuttle.components.DepartureTimeGrid
import com.example.getyourride.ui.screens.shuttle.components.TripLocationCard
import com.example.getyourride.viewmodel.ScheduleRideViewModel
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * RENAMED: from ScheduleRideScreen to BookShuttleScreen.
 * This screen wires the ScheduleRideViewModel state to UI components for booking a shuttle.
 */
@Composable
fun BookShuttleScreen(
    onBookingConfirmed: () -> Unit,
    onPickPickup: () -> Unit,
    onPickDestination: () -> Unit,
    viewModel: ScheduleRideViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = NavyPrimary.copy(alpha = 0.05f)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                BookShuttleHero()

                Spacer(modifier = Modifier.height(20.dp))

                TripLocationCard(
                    pickupLabel = uiState.pickupLabel,
                    destinationLabel = uiState.destinationLabel,
                    onSwapClick = viewModel::onSwapLocations,
                    onPickupClick = onPickPickup,
                    onDestinationClick = onPickDestination
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Departure Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                DepartureTimeGrid(
                    times = uiState.availableTimes,
                    selectedTime = uiState.selectedTime,
                    onTimeSelected = viewModel::onTimeSelected
                )

                Spacer(modifier = Modifier.height(16.dp))

                ShuttleInfoBanner()

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { viewModel.onConfirmBooking(onSuccess = onBookingConfirmed) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                    enabled = uiState.selectedTime != null && !uiState.isConfirming
                ) {
                    if (uiState.isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Confirm Booking",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hero card introducing the screen — navy surface, icon badge, title + subtitle,
 * with a subtle decorative dashed-radar pattern in the background.
 */
@Composable
private fun BookShuttleHero(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NavyPrimary
    ) {
        Box {
            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(16.dp))
            ) {
                val cx = size.width * 0.82f
                val cy = size.height * 0.35f
                val dash = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                listOf(38.dp.toPx(), 62.dp.toPx()).forEach { r ->
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = dash, cap = StrokeCap.Round)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.18f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 14.dp, end = 30.dp)
                    .size(28.dp)
            )

            Column(
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp)
            ) {
                Text(
                    text = "Book Your Shuttle",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Secure your seat for the next campus commute.",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/** Info banner shown below the time grid: shuttle frequency + arrival reminder. */
@Composable
private fun ShuttleInfoBanner(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFEDEDF3)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(NavyPrimary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = NavyPrimary,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Shuttles run every 30 minutes. Please arrive 5 minutes before departure.",
                fontSize = 12.5.sp,
                color = NavyPrimary,
                lineHeight = 16.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BookShuttleScreenPreview() {
    BookShuttleScreen(
        onBookingConfirmed = {},
        onPickPickup = {},
        onPickDestination = {}
    )
}