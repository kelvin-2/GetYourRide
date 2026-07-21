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
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

/**
 * 3-column grid of selectable departure time slots.
 * [selectedTime] drives which slot is highlighted; [onTimeSelected] reports taps up
 * to the caller (ViewModel), keeping this component stateless.
 */
@Composable
fun DepartureTimeGrid(
    times: List<String>,
    selectedTime: String?,
    onTimeSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    columns: Int = 3
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        times.chunked(columns).forEach { rowTimes ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                rowTimes.forEach { time ->
                    TimeSlotChip(
                        time = time,
                        isSelected = time == selectedTime,
                        onClick = { onTimeSelected(time) },
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) OrangeAccent else Color(0xFFE0E0E5)
    val textColor = if (isSelected) OrangeAccent else NavyPrimary
    val backgroundColor = if (isSelected) OrangeAccent.copy(alpha = 0.08f) else Color.White

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.5.dp, borderColor),
        colors = ButtonDefaults.outlinedButtonColors(containerColor = backgroundColor),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = time,
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
        times = listOf("08:00 AM", "08:30 AM", "09:00 AM", "09:30 AM", "10:00 AM", "10:30 AM"),
        selectedTime = "08:30 AM",
        onTimeSelected = {}
    )
}
