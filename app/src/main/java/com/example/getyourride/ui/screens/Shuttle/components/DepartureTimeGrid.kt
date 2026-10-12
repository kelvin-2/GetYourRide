package com.example.getyourride.ui.screens.shuttle.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.remote.dto.ShuttleTimeSlot
import com.example.getyourride.data.remote.dto.hasDepartedToday
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

/**
 * Sectioned grid of selectable departure time slots.
 */
@Composable
fun DepartureTimeGrid(
    times: List<ShuttleTimeSlot>,
    selectedTime: String?,
    onTimeSelected: (ShuttleTimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    val morningTimes = times.filter { it.period.equals("Morning", ignoreCase = true) }
    val afternoonTimes = times.filter { it.period.equals("Afternoon", ignoreCase = true) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (morningTimes.isNotEmpty()) {
            TimeSection(
                title = "Morning",
                times = morningTimes,
                selectedTime = selectedTime,
                onTimeSelected = onTimeSelected
            )
        }

        if (afternoonTimes.isNotEmpty()) {
            TimeSection(
                title = "Afternoon",
                times = afternoonTimes,
                selectedTime = selectedTime,
                onTimeSelected = onTimeSelected
            )
        }
    }
}

@Composable
private fun TimeSection(
    title: String,
    times: List<ShuttleTimeSlot>,
    selectedTime: String?,
    onTimeSelected: (ShuttleTimeSlot) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )

        val columns = 3
        times.chunked(columns).forEach { rowTimes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowTimes.forEach { slot ->
                    TimeSlotChip(
                        time = slot.departs,
                        isSelected = slot.departs == selectedTime,
                        isPast = slot.hasDepartedToday(),
                        onClick = { onTimeSelected(slot) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Pad the last row so slots keep equal width if the row is incomplete
                repeat(columns - rowTimes.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun TimeSlotChip(
    time: String,
    isSelected: Boolean,
    isPast: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Past slots stay tappable so the student gets an explanation, but look greyed out.
    val borderColor = when {
        isPast -> Color(0xFFEAEAEA)
        isSelected -> OrangeAccent
        else -> Color(0xFFE0E0E5)
    }
    val textColor = when {
        isPast -> Color(0xFFB0B0B8)
        isSelected -> OrangeAccent
        else -> NavyPrimary
    }
    val backgroundColor = when {
        isPast -> Color(0xFFF4F4F6)
        isSelected -> OrangeAccent.copy(alpha = 0.08f)
        else -> Color.White
    }

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        // Formats time like "06:45:00" -> "06:45"
        val displayTime = if (time.count { it == ':' } == 2) time.substringBeforeLast(':') else time

        Text(
            text = displayTime,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DepartureTimeGridPreview() {
    DepartureTimeGrid(
        times = listOf(
            ShuttleTimeSlot(1, "06:45:00", "07:30:00", "Morning"),
            ShuttleTimeSlot(2, "07:45:00", "08:30:00", "Morning"),
            ShuttleTimeSlot(5, "12:30:00", "13:15:00", "Afternoon"),
            ShuttleTimeSlot(6, "14:30:00", "15:15:00", "Afternoon")
        ),
        selectedTime = "07:45:00",
        onTimeSelected = {}
    )
}