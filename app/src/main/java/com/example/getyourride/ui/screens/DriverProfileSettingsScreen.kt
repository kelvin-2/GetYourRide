package com.example.getyourride.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.StudentDriverBottomBar
import com.example.getyourride.ui.components.StudentDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme

// ─── Color Palette ───────────────────────────────────────────────────────────
private val ProfileBackground = Color(0xFFF4F6FB)
private val ProfilePrimary = Color(0xFF1A2E5A)
private val ProfilePrimaryLight = Color(0xFF2E4A82)
private val ProfileTopBar = Color(0xFF1A2E5A)
private val ProfileAccent = Color(0xFFFC820C)
private val ProfileCardBackground = Color(0xFFFFFFFF)
private val ProfileText = Color(0xFF1B1B1F)
private val ProfileTextMuted = Color(0xFF5E6278)
private val ProfileBorder = Color(0xFFE5E7EB)
private val ProfileError = Color(0xFFDC2626)
private val ProfileSuccess = Color(0xFF16A34A)
private val ProfilePendingBg = Color(0xFFFEF3C7)
private val ProfilePendingText = Color(0xFF92400E)
private val ProfileDeleteBg = Color(0xFFFEF2F2)
private val ProfileSectionPurple = Color(0xFF6366F1)
private val ProfileSectionGreen = Color(0xFF16A34A)
private val ProfileSectionBlue = Color(0xFF2563EB)

// ─── Data ────────────────────────────────────────────────────────────────────
@Immutable
data class DriverProfileDetails(
    val firstName: String,
    val surname: String,
    val studentNumber: String,
    val contactNumber: String,
    val universityEmail: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleRegistrationNumber: String,
    val vehicleColour: String,
    val seatingCapacity: Int,
    val verificationStatus: String,
    val driversLicenceStatus: String,
    val vehicleRegistrationStatus: String
)

private data class StatusStyle(
    val backgroundColor: Color,
    val textColor: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileSettingsScreen(
    profileDetails: DriverProfileDetails,
    onBackClick: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onUploadLicence: () -> Unit = {},
    onUploadRegistration: () -> Unit = {},
    statusMessage: String? = null,
    errorMessage: String? = null,
    onHomeClick: () -> Unit = {},
    onOfferRideClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "GetYourRide",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ProfileTopBar)
            )
        },
        bottomBar = {
            StudentDriverBottomBar(
                selectedItem = StudentDriverBottomBarItem.Profile,
                onHomeClick = onHomeClick,
                onOfferRideClick = onOfferRideClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = ProfileBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {

            // ─── Profile Hero Header ─────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(ProfileTopBar, ProfilePrimaryLight),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 400f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Avatar circle with initials
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${profileDetails.firstName.firstOrNull() ?: ""}${profileDetails.surname.firstOrNull() ?: ""}".uppercase(),
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "${profileDetails.firstName} ${profileDetails.surname}",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = profileDetails.studentNumber,
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        // Inline verification badge
                        val verStyle = statusStyleFor(profileDetails.verificationStatus)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = verStyle.backgroundColor.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = verStyle.icon,
                                    contentDescription = null,
                                    tint = verStyle.textColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = profileDetails.verificationStatus,
                                    color = verStyle.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Personal Details Card ───────────────────────────────────────
            ProfileSectionCard(
                title = "Personal Details",
                icon = Icons.Outlined.Person,
                iconTint = ProfileSectionPurple
            ) {
                ProfileDetailRow(
                    label = "First Name",
                    value = profileDetails.firstName
                )
                ProfileDetailRow(
                    label = "Surname",
                    value = profileDetails.surname
                )
                ProfileDetailRow(
                    label = "Student Number",
                    value = profileDetails.studentNumber,
                    icon = Icons.Outlined.Badge
                )
                ProfileDetailRow(
                    label = "University Email",
                    value = profileDetails.universityEmail,
                    icon = Icons.Outlined.Email
                )
                ProfileDetailRow(
                    label = "Contact Number",
                    value = profileDetails.contactNumber,
                    icon = Icons.Outlined.Phone
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Vehicle Details Card ────────────────────────────────────────
            ProfileSectionCard(
                title = "Vehicle Details",
                icon = Icons.Outlined.DirectionsCar,
                iconTint = ProfileAccent
            ) {
                ProfileDetailRow(
                    label = "Vehicle",
                    value = "${profileDetails.vehicleMake} ${profileDetails.vehicleModel}"
                )
                ProfileDetailRow(
                    label = "Registration Number",
                    value = profileDetails.vehicleRegistrationNumber
                )
                ProfileDetailRow(
                    label = "Colour",
                    value = profileDetails.vehicleColour
                )
                ProfileDetailRow(
                    label = "Seating Capacity",
                    value = "${profileDetails.seatingCapacity} passengers"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Document Status Card ────────────────────────────────────────
            ProfileSectionCard(
                title = "Documents",
                icon = Icons.Outlined.Description,
                iconTint = ProfileSectionBlue
            ) {
                DocumentStatusRow(
                    documentName = "Driver's Licence",
                    status = profileDetails.driversLicenceStatus,
                    onUploadClick = if (profileDetails.driversLicenceStatus.equals(
                            "Not Uploaded", ignoreCase = true
                        )
                    ) onUploadLicence else null
                )

                Spacer(modifier = Modifier.height(12.dp))

                DocumentStatusRow(
                    documentName = "Vehicle Registration",
                    status = profileDetails.vehicleRegistrationStatus,
                    onUploadClick = if (profileDetails.vehicleRegistrationStatus.equals(
                            "Not Uploaded", ignoreCase = true
                        )
                    ) onUploadRegistration else null
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Status / Error Messages ─────────────────────────────────────
            AnimatedVisibility(
                visible = !statusMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!statusMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = ProfileSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage,
                                color = ProfileSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = ProfileError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage,
                                color = ProfileError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Delete Profile Section ──────────────────────────────────────
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = ProfileDeleteBg,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 0.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(ProfileError.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null,
                                tint = ProfileError,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Danger Zone",
                                color = ProfileError,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Permanently delete your driver profile",
                                color = ProfileError.copy(alpha = 0.7f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // ── Logout Button ────────────────────────────────────────
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileError
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, ProfileError.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                            contentDescription = null,
                            tint = ProfileError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Out",
                            color = ProfileError,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Delete Button ────────────────────────────────────────
                    OutlinedButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = ProfileError
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp, ProfileError.copy(alpha = 0.4f)
                        ),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = ProfileError,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Delete Driver Profile",
                            color = ProfileError,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ─── Delete Confirmation Dialog ──────────────────────────────────────────
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(ProfileError.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Warning,
                        contentDescription = null,
                        tint = ProfileError,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Delete Profile?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "This will permanently delete your student driver profile and all associated data. You will no longer be able to offer rides.",
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        onConfirmDeleteClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ProfileError),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─── Section Card ────────────────────────────────────────────────────────────
@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = ProfileCardBackground,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section header with icon badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = ProfilePrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ProfileBorder)
            )

            content()
        }
    }
}

// ─── Detail Row ──────────────────────────────────────────────────────────────
@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF9FAFB),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(ProfilePrimary.copy(alpha = 0.06f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ProfilePrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label.uppercase(),
                color = ProfileTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                color = ProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Document Status Row ─────────────────────────────────────────────────────
@Composable
private fun DocumentStatusRow(
    documentName: String,
    status: String,
    onUploadClick: (() -> Unit)? = null
) {
    val style = statusStyleFor(status)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFFF9FAFB),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(style.backgroundColor.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Description,
                    contentDescription = null,
                    tint = style.textColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = documentName,
                    color = ProfileText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Required document",
                    color = ProfileTextMuted,
                    fontSize = 11.sp
                )
            }

            if (status.equals("Not Uploaded", ignoreCase = true) && onUploadClick != null) {
                Button(
                    onClick = onUploadClick,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ProfileAccent),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Upload",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = style.backgroundColor
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            tint = style.textColor,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = status,
                            color = style.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ─── Status Style Helper ─────────────────────────────────────────────────────
private fun statusStyleFor(status: String): StatusStyle {
    val cleaned = status.trim().lowercase()
    return when {
        cleaned.contains("approved") || cleaned.contains("uploaded") -> StatusStyle(
            backgroundColor = Color(0xFFDCFCE7),
            textColor = Color(0xFF16A34A),
            icon = Icons.Outlined.CheckCircle
        )
        cleaned.contains("rejected") -> StatusStyle(
            backgroundColor = Color(0xFFFEE2E2),
            textColor = Color(0xFFDC2626),
            icon = Icons.Outlined.ErrorOutline
        )
        cleaned.contains("pending") -> StatusStyle(
            backgroundColor = Color(0xFFFEF3C7),
            textColor = Color(0xFF92400E),
            icon = Icons.Outlined.Schedule
        )
        cleaned.contains("not uploaded") -> StatusStyle(
            backgroundColor = Color(0xFFFEF3C7),
            textColor = Color(0xFF92400E),
            icon = Icons.Outlined.UploadFile
        )
        else -> StatusStyle(
            backgroundColor = Color(0xFFE0E7FF),
            textColor = Color(0xFF4338CA),
            icon = Icons.Outlined.Shield
        )
    }
}

// ─── Preview ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverProfileSettingsScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverProfileSettingsScreen(
            profileDetails = DriverProfileDetails(
                firstName = "Thabo",
                surname = "Mokena",
                studentNumber = "S219045678",
                contactNumber = "071 234 5678",
                universityEmail = "s219045678@mandela.ac.za",
                vehicleMake = "Toyota",
                vehicleModel = "Corolla",
                vehicleRegistrationNumber = "ABC 123 EC",
                vehicleColour = "White",
                seatingCapacity = 4,
                verificationStatus = "Pending Review",
                driversLicenceStatus = "Uploaded",
                vehicleRegistrationStatus = "Not Uploaded"
            )
        )
    }
}
