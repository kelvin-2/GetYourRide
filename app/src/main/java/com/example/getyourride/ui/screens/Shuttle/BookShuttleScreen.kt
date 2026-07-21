package com.example.getyourride.ui.screens.shuttle

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        containerColor = Color(0xFFF5F5FA)
    ) { padding ->
        // Wrap in a Box to center the entire content block vertically and horizontally
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
                    // Use wrapContentHeight so it doesn't take full height, allowing Box to center it
                    .wrapContentHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                // FIXED: Wiring TripLocationCard with uiState properties and ViewModel methods
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
                    // FIXED: Using NavyPrimary directly from theme
                    color = NavyPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // FIXED: Wiring DepartureTimeGrid with uiState and selection handler
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
                    // FIXED: Using OrangeAccent directly from theme
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

@Preview(showBackground = true)
@Composable
private fun BookShuttleScreenPreview() {
    BookShuttleScreen(
        onBookingConfirmed = {},
        onPickPickup = {},
        onPickDestination = {}
    )
}
