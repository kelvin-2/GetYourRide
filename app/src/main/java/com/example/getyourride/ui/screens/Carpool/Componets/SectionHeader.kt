// ─────────────────────────────────────────────────────────────────────────────
// SectionHeader.kt
// Package: com.example.getyourride.ui.screens.Carpool.components
//
// PURPOSE — Reusable "Title + View All" row used before every list section.
//
// REUSE — Use wherever you have a section with a "View All" link:
//   SectionHeader(title = "Available Carpools", onViewAll = { navController.navigate("rides") })
//   SectionHeader(title = "Recent Trips")   // onViewAll = null hides the link
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.screens.Carpool.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

/**
 * Section title row with an optional "View All" link on the right.
 *
 * @param title      Section heading text.
 * @param onViewAll  Called when "View All" is tapped. Pass null to hide it.
 */
@Composable
fun SectionHeader(
    title     : String,
    onViewAll : (() -> Unit)? = null,
    modifier  : Modifier = Modifier,
) {
    Row(
        modifier              = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text       = title,
            fontSize   = 16.sp,
            fontWeight = FontWeight.Bold,
            color      = NavyPrimary,
        )

        if (onViewAll != null) {
            Text(
                text     = "View All",
                fontSize = 13.sp,
                color    = OrangeAccent,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.clickable { onViewAll() },
            )
        }
    }
}