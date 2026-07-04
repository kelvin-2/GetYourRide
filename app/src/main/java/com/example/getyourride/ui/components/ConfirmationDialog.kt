package com.example.getyourride.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.getyourride.ui.theme.*

data class ConfirmationDialogData(
    val icon               : ImageVector = Icons.Filled.Warning,
    val iconTint           : Color = Color(0xFFD32F2F),
    val iconBackgroundTint : Color = Color(0xFFFCE4E4),
    val title              : String,
    val message            : AnnotatedString,
    val confirmLabel       : String = "Yes, Confirm",
    val dismissLabel       : String = "No, Cancel",
    val confirmColor       : Color = Color(0xFFD32F2F),
)

@Composable
fun ConfirmationDialog(
    data       : ConfirmationDialogData,
    onConfirm  : () -> Unit,
    onDismiss  : () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardWhite,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Drag-handle bar
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(SurfaceGrey, RoundedCornerShape(2.dp))
                )

                Spacer(Modifier.height(20.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(data.iconBackgroundTint, shape = androidx.compose.foundation.shape.CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(data.icon, contentDescription = null, tint = data.iconTint, modifier = Modifier.size(28.dp))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = data.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = data.message,
                    fontSize = 13.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = data.confirmColor),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(data.confirmLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                ) {
                    Text(data.dismissLabel, fontSize = 14.sp, color = NavyPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationDialogPreview() {
    val message = buildAnnotatedString {
        append("Are you sure you want to cancel your ride to ")
        withStyle(style = androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold, color = NavyPrimary)) {
            append("East Residence")
        }
        append("? This action cannot be undone.")
    }

    GetYourRideTheme {
        ConfirmationDialog(
            data = ConfirmationDialogData(
                title = "Cancel Booking?",
                message = message,
            ),
            onConfirm = {},
            onDismiss = {},
        )
    }
}