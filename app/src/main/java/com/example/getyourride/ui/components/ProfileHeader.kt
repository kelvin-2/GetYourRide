package com.example.getyourride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GreenSuccess
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent

/**
 * Navy header block used at the top of the Profile screen.
 * Shows an initials avatar, the user's name, student number,
 * and an NSFAS funding badge when applicable.
 */
@Composable
fun ProfileHeader(
    name: String,
    initials: String,
    studentNumber: String,
    isNsfasFunded: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NavyPrimary)
            .padding(top = 48.dp, bottom = 56.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(OrangeAccent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = studentNumber,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            FundingBadge(isNsfasFunded = isNsfasFunded)
        }
    }
}

@Composable
private fun FundingBadge(isNsfasFunded: Boolean) {
    val (label, color) = if (isNsfasFunded) {
        "NSFAS Funded" to GreenSuccess
    } else {
        "Self-Funded" to OrangeAccent
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}