package com.example.getyourride.ui.screens.Shuttle

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.getyourride.ui.screens.Shuttle.componets.DepartureTimeGrid
import com.example.getyourride.ui.screens.Shuttle.componets.TripLocationCard
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

/**
 * RENAMED: from ScheduleRideScreen to BookShuttleScreen.
 * This screen wires the ScheduleRideViewModel state to UI components for booking a shuttle.
 */
@Composable
fun BookShuttleScreen(
    onBookingConfirmed: () -> Unit,
    // FIXED: Corrected package for ScheduleRideViewModel and using standard viewModel() delegate
    viewModel: ScheduleRideViewModel = viewModel()
) {
    // FIXED: Properly collecting state from the ViewModel
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5FA))
            .padding(16.dp)
    ) {
        // FIXED: Wiring TripLocationCard with uiState properties and ViewModel methods
        TripLocationCard(
            pickupLabel = uiState.pickupLabel,
            destinationLabel = uiState.destinationLabel,
            onSwapClick = viewModel::onSwapLocations
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

@Preview(showBackground = true)
@Composable
private fun BookShuttleScreenPreview() {
    BookShuttleScreen(onBookingConfirmed = {})
}
