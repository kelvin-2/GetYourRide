package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

data class DocumentsState(
    val licenceFileName  : String? = null,
    val vehicleFileName  : String? = null,
)

@Composable
fun DriverStep3Documents(
    state            : DocumentsState,
    onPickLicence    : () -> Unit,
    onPickVehicleReg : () -> Unit,
) {
    Column(
        modifier            = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        DriverStepHeading(
            title    = "Upload Documents",
            subtitle = "We need to verify your driver credentials to ensure safety on the platform.",
        )

        DocumentUploadCard(
            title        = "Driver's Licence",
            description  = "Front and back of your licence",
            fileName     = state.licenceFileName,
            icon         = Icons.Outlined.Badge,
            onChooseFile = onPickLicence,
        )

        DocumentUploadCard(
            title        = "Vehicle Registration",
            description  = "Official registration document",
            fileName     = state.vehicleFileName,
            icon         = Icons.Outlined.Description,
            onChooseFile = onPickVehicleReg,
        )

        VerificationTimeNotice()
    }
}

// ─── Local components ─────────────────────────────────────────────────────────

@Composable
private fun DocumentUploadCard(
    title        : String,
    description  : String,
    fileName     : String?,
    icon         : ImageVector,
    onChooseFile : () -> Unit,
) {
    val uploaded = fileName != null

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.Top,
            ) {
                // Icon + title + file name
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    modifier              = Modifier.weight(1f),
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(SurfaceGrey, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint     = NavyPrimary,
                            modifier = Modifier.size(24.dp),
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text       = title,
                            fontSize   = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyPrimary,
                        )
                        Text(
                            text     = description,
                            fontSize = 12.sp,
                            color    = TextMuted,
                        )
                        if (fileName != null) {
                            Text(
                                text     = fileName,
                                fontSize = 11.sp,
                                color    = OrangeAccent,
                            )
                        }
                    }
                }

                // Status badge
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (uploaded)
                        GreenSuccess.copy(alpha = 0.12f)
                    else
                        StatusPending.copy(alpha = 0.15f),
                ) {
                    Text(
                        text       = if (uploaded) "UPLOADED" else "PENDING",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = if (uploaded) GreenSuccess else StatusPending,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }

            // File picker button
            OutlinedButton(
                onClick  = onChooseFile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape  = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderLight),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = NavyPrimary),
            ) {
                Icon(
                    Icons.Outlined.AttachFile,
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = if (uploaded) "Change File" else "Choose File",
                    fontSize   = 14.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun VerificationTimeNotice() {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = NavyPrimary),
    ) {
        Row(
            modifier              = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.Top,
        ) {
            Icon(
                Icons.Outlined.Schedule,
                contentDescription = null,
                tint     = Color.White,
                modifier = Modifier
                    .size(20.dp)
                    .padding(top = 2.dp),
            )
            Text(
                text       = "Verification typically takes 24–48 business hours. You'll receive a notification once your profile is approved.",
                fontSize   = 13.sp,
                color      = Color.White.copy(alpha = 0.9f),
                lineHeight = 20.sp,
            )
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Step 3 — Upload Documents")
@Composable
fun DriverStep3Preview() {
    MaterialTheme {
        DriverStep3Documents(
            state            = DocumentsState(licenceFileName = "licence_front.jpg"),
            onPickLicence    = {},
            onPickVehicleReg = {},
        )
    }
}