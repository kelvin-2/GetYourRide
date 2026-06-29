// ─────────────────────────────────────────────────────────────────────────────
// QuickActionCard.kt
// Package: com.example.getyourride.ui.screens.Carpool.components
//
// PURPOSE — Small tappable card used in the Quick Actions row.
//
// REUSE — Used in CarpoolHomeScreen's quick actions row.
//   Row {
//       QuickActionCard(icon = Icons.Outlined.LocationOn, label = "Where To?", onClick = {})
//       QuickActionCard(icon = Icons.Outlined.CalendarMonth, label = "Schedule", onClick = {})
//   }
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Carpool.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

/**
 * Small icon + label card for quick navigation actions.
 *
 * @param icon      Icon to display (use Icons.Outlined.*)
 * @param label     Short label below the icon.
 * @param iconTint  Icon colour — defaults to OrangeAccent.
 * @param onClick   Called when the card is tapped.
 */
@Composable
fun QuickActionCard(
    icon     : ImageVector,
    label    : String,
    onClick  : () -> Unit,
    iconTint : androidx.compose.ui.graphics.Color = OrangeAccent,
    modifier : Modifier = Modifier,
) {
    Card(
        modifier  = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier             = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement  = Arrangement.Center,
            horizontalAlignment  = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = iconTint,
                modifier           = Modifier.size(24.dp),
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text       = label,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                color      = NavyPrimary,
            )
        }
    }
}