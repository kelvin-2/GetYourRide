// ─────────────────────────────────────────────────────────────────────────────
// GyrTopBar.kt
// Package: com.example.getyourride.ui.components
//
// PURPOSE — Single top app bar used on every screen in the app.
//
// Two modes:
//   1. Home screens  → no back arrow, no trailing content
//   2. Detail screens → back arrow on left, optional step label on right
//
// The progress bar (GyrStepProgressBar) is a separate composable
// used only in multi-step flows (BecomeADriver etc.)
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

// Slim navy tone for the top bar — a touch darker/tighter than NavyPrimary
private val TopBarNavy = Color(0xFF0C1E42)

/**
 * App-wide top bar.
 *
 * @param onBackClick    Pass a lambda to show the back arrow; null hides it.
 *                       — Home screens: null (no back arrow)
 *                       — Detail screens: { navController.popBackStack() }
 * @param trailingLabel  Optional right-side text e.g. "STEP 1 OF 3".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyrTopBar(
    onBackClick   : (() -> Unit)? = null,
    trailingLabel : String?       = null,
) {
    TopAppBar(
        // ── Left side — back arrow or empty space ─────────────────────────────
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector        = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                        tint               = Color.White,
                    )
                }
            } else {
                // Keep title centred by matching the width of an IconButton
                Spacer(Modifier.width(48.dp))
            }
        },

        // ── Centre — bus icon + app name ──────────────────────────────────────
        title = {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier              = Modifier.fillMaxWidth(),
            ) {
                Icon(
                    imageVector        = Icons.Outlined.DirectionsBus,
                    contentDescription = null,
                    tint               = OrangeAccent,
                    modifier           = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "GetYourRide",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color      = Color.White,
                )
            }
        },

        // ── Right side — step label OR empty ────────────────────────────────
        actions = {
            if (trailingLabel != null) {
                Text(
                    text       = trailingLabel,
                    fontSize   = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = Color.White.copy(alpha = 0.8f),
                    modifier   = Modifier.padding(end = 16.dp),
                )
            } else {
                // Keep title centred when there's nothing on the right
                Spacer(Modifier.width(48.dp))
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(containerColor = TopBarNavy),
        modifier = Modifier.height(52.dp), // slimmer than the default 64.dp bar
    )
}

/**
 * 3-segment step progress bar — used in multi-step flows only.
 * Sits directly below GyrTopBar on a navy background.
 *
 * @param totalSteps   Total number of steps in the flow.
 * @param currentStep  1-based index of the active step.
 */
@Composable
fun GyrStepProgressBar(
    totalSteps  : Int,
    currentStep : Int,
    modifier    : Modifier = Modifier,
) {
    Row(
        modifier              = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSteps) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (index < currentStep) OrangeAccent
                        else Color.White.copy(alpha = 0.30f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                    )
            )
        }
    }
}

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(name = "TopBar — Home")
@Composable
fun TopBarHomePreview() {
    MaterialTheme { GyrTopBar() }
}

@Preview(name = "TopBar — Detail (back arrow + step label)")
@Composable
fun TopBarDetailPreview() {
    MaterialTheme { GyrTopBar(onBackClick = {}, trailingLabel = "STEP 1 OF 3") }
}