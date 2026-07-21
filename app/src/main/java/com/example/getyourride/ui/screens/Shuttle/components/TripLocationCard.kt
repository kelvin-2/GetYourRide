package com.example.getyourride.ui.screens.shuttle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

/**
 * Card showing pickup + destination with a swap action on the right.
 * Pure display component — swap logic (swapping the two location strings)
 * is handled by the caller via [onSwapClick].
 */
@Composable
fun TripLocationCard(
    pickupLabel: String,
    destinationLabel: String,
    onSwapClick: () -> Unit,
    onPickupClick: () -> Unit,
    onDestinationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left column: pickup + destination rows stacked, connected by a vertical line
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                LocationRow(
                    label = "Pickup",
                    value = pickupLabel,
                    dotColor = OrangeAccent,
                    onClick = onPickupClick
                )
                LocationRow(
                    label = "Destination",
                    value = destinationLabel,
                    dotColor = NavyPrimary,
                    onClick = onDestinationClick
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Swap button — flips pickup/destination
            IconButton(
                onClick = onSwapClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF2F2F7))
            ) {
                Icon(
                    imageVector = Icons.Filled.SwapVert,
                    contentDescription = "Swap pickup and destination",
                    tint = NavyPrimary
                )
            }
        }
    }
}

/** Single labeled row (e.g. "Pickup" / "North Campus Main Gate") with a leading dot marker. */
@Composable
private fun LocationRow(
    label: String,
    value: String,
    dotColor: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = OrangeAccent
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = NavyPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TripLocationCardPreview() {
    TripLocationCard(
        pickupLabel = "North Campus Main Gate",
        destinationLabel = "South Campus",
        onSwapClick = {},
        onPickupClick = {},
        onDestinationClick = {}
    )
}
