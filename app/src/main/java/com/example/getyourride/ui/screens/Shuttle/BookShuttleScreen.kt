package com.example.getyourride.ui.screens.shuttle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/**
 * RENAMED: from ScheduleRideScreen to BookShuttleScreen.
 * This screen wires the ScheduleRideViewModel state to UI components for booking a shuttle.
 */
@Composable
fun BookShuttleScreen(
    onBookingConfirmed: () -> Unit,
    onPickPickup: () -> Unit,
    onPickDestination: () -> Unit,
    // FIXED: Corrected package for ScheduleRideViewModel and using standard viewModel() delegate
    viewModel: ScheduleRideViewModel = viewModel()
) {
    // FIXED: Properly collecting state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    // Error Snackbar
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
                // NEW: Hero section — scrolls with content, not a pinned top bar
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

                Text(
                    text = "Departure Time",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                DepartureTimeGrid(
                    times = uiState.availableTimes,
                    selectedTime = uiState.selectedTime,
                    onTimeSelected = viewModel::onTimeSelected
                )

                Spacer(modifier = Modifier.height(24.dp))

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
                        Text(
                            text = "Confirm Booking",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * NEW: Hero card introducing the screen — navy surface, icon badge, title + subtitle.
 * Sits inline above TripLocationCard so it scrolls with the rest of the content
 * rather than being pinned like a top app bar.
 */
@Composable
private fun BookShuttleHero(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = NavyPrimary
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsBus,
                    contentDescription = null,
                    tint = OrangeAccent,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = "Book a shuttle",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Pick a route and time that works for you",
                    fontSize = 12.5.sp,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
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