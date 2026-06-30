// ─────────────────────────────────────────────────────────────────────────────
// GyrTopBar.kt
// Package: com.example.getyourride.ui.components
//
// PURPOSE — Single top app bar used on every screen in the app.
//
// Two modes:
//   1. Home screens  → no back arrow, optional bell icon on the right
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
import androidx.compose.material.icons.outlined.Notifications
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

/**
 * App-wide top bar.
 *
 * @param onBackClick    Pass a lambda to show the back arrow; null hides it.
 *                       — Home screens: null (no back arrow)
 *                       — Detail screens: { navController.popBackStack() }
 * @param trailingLabel  Optional right-side text e.g. "STEP 1 OF 3".
 *                       Ignored if showBell is true.
 * @param showBell       Show notification bell on the right. Default false.
 *                       Set to true on all main student tab screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyrTopBar(
    onBackClick   : (() -> Unit)? = null,
    trailingLabel : String?       = null,
    showBell      : Boolean       = false,
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
                    tint               = Color.White,
                    modifier           = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "GetYourRide",
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color.White,
                )
            }
        },

        // ── Right side — bell icon OR step label OR empty ─────────────────────
        actions = {
            when {
                // Main student screens show the notification bell
                showBell -> {
                    IconButton(onClick = { /* TODO: navigate to notifications */ }) {
                        Icon(
                            imageVector        = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint               = Color.White,
                        )
                    }
                }
                // Multi-step flows show e.g. "STEP 1 OF 3"
                trailingLabel != null -> {
                    Text(
                        text       = trailingLabel,
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Color.White.copy(alpha = 0.8f),
                        modifier   = Modifier.padding(end = 16.dp),
                    )
                }
                // Detail screens with only a back arrow — keep right side balanced
                else -> Spacer(Modifier.width(48.dp))
            }
        },

        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary),
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

@Preview(name = "TopBar — Home (with bell)")
@Composable
fun TopBarHomePreview() {
    MaterialTheme { GyrTopBar(showBell = true) }
}

@Preview(name = "TopBar — Detail (back arrow + step label)")
@Composable
fun TopBarDetailPreview() {
    MaterialTheme { GyrTopBar(onBackClick = {}, trailingLabel = "STEP 1 OF 3") }
}