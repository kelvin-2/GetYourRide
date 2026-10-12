// ─────────────────────────────────────────────────────────────────────────────
// GyrErrorComponents.kt
// Package: com.example.getyourride.ui.components
//
// PURPOSE — The ONE set of error UI used across the app:
//   GyrSnackbarHost   → themed snackbar for action failures (booking, cancel...)
//   GyrMessageEffect  → shows a one-shot message from a ViewModel, then consumes it
//   GyrErrorState     → inline "couldn't load" state with a Retry button
//   GyrErrorDialog    → optional blocking dialog for serious errors
//
// Styling reuses the app's existing tokens: DangerRed / NavyPrimary / OrangeAccent,
// 12dp buttons, 24dp dialog surface (same as ConfirmationDialog).
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.getyourride.ui.theme.CardWhite
import com.example.getyourride.ui.theme.DangerRed
import com.example.getyourride.ui.theme.DangerRedSoft
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.example.getyourride.ui.theme.TextMuted

/**
 * Themed snackbar host. Pass it as `snackbarHost` of a Scaffold (or the `snackbarHost`
 * parameter of StudentLayout / ShuttleLayout) so it sits above the bottom navigation.
 *
 * @param isError red for failures (default); navy for neutral/success messages.
 */
@Composable
fun GyrSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    isError: Boolean = true,
) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        val actionLabel = data.visuals.actionLabel
        Snackbar(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            containerColor = if (isError) DangerRed else NavyPrimary,
            contentColor = Color.White,
            actionContentColor = Color.White,
            action = if (actionLabel != null) {
                {
                    TextButton(onClick = { data.performAction() }) {
                        Text(
                            text = actionLabel,
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            } else null,
        ) {
            Text(text = data.visuals.message, fontSize = 13.sp, color = Color.White)
        }
    }
}

/**
 * Shows [message] in the snackbar once, then calls [onShown] so the ViewModel clears it
 * (no re-show on recomposition).
 */
@Composable
fun GyrMessageEffect(
    message: String?,
    hostState: SnackbarHostState,
    onShown: () -> Unit,
) {
    LaunchedEffect(message) {
        if (message != null) {
            hostState.showSnackbar(message)
            onShown()
        }
    }
}

/** Inline error for screens that fail to load their data. [onRetry] null hides the button. */
@Composable
fun GyrErrorState(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Something went wrong",
    retryLabel: String = "Try again",
    onRetry: (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        ErrorBadge()
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = NavyPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            fontSize = 13.sp,
            color = TextMuted,
            textAlign = TextAlign.Center,
        )
        if (onRetry != null) {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                modifier = Modifier.height(48.dp),
            ) {
                Text(retryLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            }
        }
    }
}

/** Optional blocking dialog for errors the user must acknowledge. */
@Composable
fun GyrErrorDialog(
    message: String,
    onDismiss: () -> Unit,
    title: String = "Something went wrong",
    dismissLabel: String = "Got it",
    onRetry: (() -> Unit)? = null,
    retryLabel: String = "Try again",
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = CardWhite) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                ErrorBadge()
                Spacer(Modifier.height(16.dp))
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(24.dp))
                if (onRetry != null) {
                    Button(
                        onClick = onRetry,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text(retryLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                    Spacer(Modifier.height(10.dp))
                    androidx.compose.material3.OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text(dismissLabel, fontSize = 14.sp, color = NavyPrimary)
                    }
                } else {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                    ) {
                        Text(dismissLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorBadge() {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(DangerRedSoft, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.ErrorOutline,
            contentDescription = null,
            tint = DangerRed,
            modifier = Modifier.size(28.dp),
        )
    }
}