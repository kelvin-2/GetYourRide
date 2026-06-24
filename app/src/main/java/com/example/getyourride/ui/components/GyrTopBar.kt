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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.example.getyourride.ui.theme.TextMuted

/**
 * Consistent top app bar used on every screen.
 *
 * @param onBackClick  Pass a lambda to show the back arrow; null hides it.
 * @param trailingLabel Optional right-side label e.g. "STEP 1 OF 3"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GyrTopBar(
    onBackClick    : (() -> Unit)? = null,
    trailingLabel  : String?       = null,
) {
    TopAppBar(
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
                Spacer(Modifier.width(48.dp)) // keep title centred
            }
        },
        title = {
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier             = Modifier.fillMaxWidth(),
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
                Spacer(Modifier.width(48.dp)) // balance nav icon
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyPrimary),
    )
}

/**
 * 3-segment step progress bar.
 * Filled segments use OrangeAccent; unfilled use white at 30% opacity.
 */
@Composable
fun GyrStepProgressBar(
    totalSteps   : Int,
    currentStep  : Int,  // 1-based
    modifier     : Modifier = Modifier,
) {
    Row(
        modifier            = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(totalSteps) { index ->
            val filled = index < currentStep
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .background(
                        color = if (filled) OrangeAccent
                        else Color.White.copy(alpha = 0.30f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
                    )
            )
        }
    }
}