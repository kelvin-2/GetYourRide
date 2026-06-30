package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.StudentDriverBottomBar
import com.example.getyourride.ui.components.StudentDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import androidx.compose.foundation.layout.ColumnScope

/*
 * DriverProfileSettingsScreen
 *
 * This screen is used by the student driver to view their profile information.
 *
 * It shows:
 * - verification status
 * - personal details
 * - vehicle details
 * - uploaded document status
 * - delete profile option
 *
 * The actual data will later come from the backend after login.
 * For now, MainActivity can pass sample data while we build the frontend.
 */

// Shared colours for this profile screen.
private val ProfileBackground = Color(0xFFFBF8FD)
private val ProfilePrimary = Color(0xFF011844)
private val ProfileTopBar = Color(0xFF1A2E5A)
private val ProfileAccent = Color(0xFFFC820C)
private val ProfileCardBackground = Color(0xFFFFFFFF)
private val ProfileText = Color(0xFF1B1B1F)
private val ProfileTextMuted = Color(0xFF44464F)
private val ProfilePrimaryFixed = Color(0xFFDAE2FF)
private val ProfileSoftBorder = Color(0xFFE3E2E6)
private val ProfileError = Color(0xFFC62828)
private val ProfileSuccess = Color(0xFF2E7D32)
private val ProfileDeleteBackground = Color(0xFFFFEBEE)

/*
 * This data class keeps the profile screen clean.
 *
 * Instead of passing many separate values to the screen,
 * we group the student driver profile details here.
 */
@Immutable
data class DriverProfileDetails(
    val firstName: String = "Ayabulela",
    val surname: String = "Mtwesi",
    val studentNumber: String = "223456789",
    val contactNumber: String = "071 234 5678",
    val universityEmail: String = "ayabulela@mandela.ac.za",
    val vehicleMake: String = "Toyota",
    val vehicleModel: String = "Corolla",
    val vehicleRegistrationNumber: String = "ABC 123 EC",
    val vehicleColour: String = "White",
    val seatingCapacity: Int = 4,
    val verificationStatus: String = "Pending Verification",
    val driversLicenceStatus: String = "Uploaded",
    val vehicleRegistrationStatus: String = "Uploaded"
)

/*
 * Small helper class for status colours.
 *
 * Example:
 * Approved  -> green
 * Rejected  -> red
 * Pending   -> yellow
 */
private data class StatusStyle(
    val backgroundColor: Color,
    val textColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileSettingsScreen(
    profileDetails: DriverProfileDetails = DriverProfileDetails(),
    onBackClick: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
    statusMessage: String? = null,
    errorMessage: String? = null,

    /*
     * Bottom menu callbacks.
     *
     * MainActivity controls the actual navigation.
     */
    onHomeClick: () -> Unit = {},
    onOfferRideClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    var showDeleteDialog by rememberSaveable {
        mutableStateOf(false)
    }

    Scaffold(
        topBar = {
            /*
             * Same top bar style as Home and Offer Ride.
             */
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfileTopBar
                )
            )
        },
        bottomBar = {
            /*
             * Profile is selected because the user is currently on the Profile page.
             */
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
                .background(ProfileBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Driver Profile",
                    color = ProfilePrimary,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View your driver details and verification status.",
                    color = ProfileTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            VerificationStatusCard(
                status = profileDetails.verificationStatus
            )

            ProfileSectionCard(
                title = "Personal Details",
                icon = Icons.Outlined.Person
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
                    value = profileDetails.studentNumber
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

            ProfileSectionCard(
                title = "Vehicle Details",
                icon = Icons.Outlined.DirectionsCar
            ) {
                ProfileDetailRow(
                    label = "Vehicle Make",
                    value = profileDetails.vehicleMake
                )

                ProfileDetailRow(
                    label = "Vehicle Model",
                    value = profileDetails.vehicleModel
                )

                ProfileDetailRow(
                    label = "Registration Number",
                    value = profileDetails.vehicleRegistrationNumber
                )

                ProfileDetailRow(
                    label = "Vehicle Colour",
                    value = profileDetails.vehicleColour
                )

                ProfileDetailRow(
                    label = "Seating Capacity",
                    value = profileDetails.seatingCapacity.toString()
                )
            }

            ProfileSectionCard(
                title = "Document Status",
                icon = Icons.Outlined.Description
            ) {
                DocumentStatusRow(
                    documentName = "Driver's Licence",
                    status = profileDetails.driversLicenceStatus
                )

                DocumentStatusRow(
                    documentName = "Vehicle Registration",
                    status = profileDetails.vehicleRegistrationStatus
                )
            }

            if (!statusMessage.isNullOrBlank()) {
                ProfileMessageText(
                    text = statusMessage,
                    color = ProfileSuccess
                )
            }

            if (!errorMessage.isNullOrBlank()) {
                ProfileMessageText(
                    text = errorMessage,
                    color = ProfileError
                )
            }

            DeleteProfileCard(
                onDeleteClick = {
                    showDeleteDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    if (showDeleteDialog) {
        DeleteProfileConfirmationDialog(
            onDismissClick = {
                showDeleteDialog = false
            },
            onConfirmClick = {
                showDeleteDialog = false
                onConfirmDeleteClick()
            }
        )
    }
}

@Composable
private fun VerificationStatusCard(
    status: String
) {
    val style = statusStyleFor(status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(style.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = style.textColor,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Verification Status",
                    color = ProfileTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = status,
                    color = style.textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
 * Reusable card used for Personal Details, Vehicle Details, and Documents.
 */
@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ProfileCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfilePrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ProfilePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = title,
                    color = ProfilePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ProfileSoftBorder)
            )

            content()
        }
    }
}

/*
 * One row for a profile field.
 */
@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfileTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = ProfileTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
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

/*
 * Shows whether a required document was uploaded.
 */
@Composable
private fun DocumentStatusRow(
    documentName: String,
    status: String
) {
    val style = statusStyleFor(status)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = documentName,
                color = ProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = "Required document",
                color = ProfileTextMuted,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(style.backgroundColor)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = status,
                color = style.textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/*
 * Delete profile warning card.
 *
 * This does not delete immediately.
 * It opens a confirmation dialog first.
 */
@Composable
private fun DeleteProfileCard(
    onDeleteClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileDeleteBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null,
                    tint = ProfileError,
                    modifier = Modifier.size(28.dp)
                )

                Text(
                    text = "Delete Driver Profile",
                    color = ProfileError,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = "This will deactivate your student driver profile. You will not be able to offer rides after deleting it.",
                color = ProfileError,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )

            OutlinedButton(
                onClick = onDeleteClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Text(
                    text = "Delete Profile",
                    color = ProfileError,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/*
 * Confirmation dialog before deleting/deactivating the profile.
 */
@Composable
private fun DeleteProfileConfirmationDialog(
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissClick,
        title = {
            Text(
                text = "Delete profile?",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete your student driver profile? This action will deactivate your driver account."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmClick
            ) {
                Text(
                    text = "Delete",
                    color = ProfileError,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismissClick
            ) {
                Text(text = "Cancel")
            }
        }
    )
}

/*
 * Chooses colours based on status text.
 */
private fun statusStyleFor(status: String): StatusStyle {
    val cleanedStatus = status.trim().lowercase()

    return when {
        cleanedStatus.contains("approved") -> StatusStyle(
            backgroundColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF2E7D32)
        )

        cleanedStatus.contains("rejected") -> StatusStyle(
            backgroundColor = Color(0xFFFFEBEE),
            textColor = Color(0xFFC62828)
        )

        cleanedStatus.contains("uploaded") -> StatusStyle(
            backgroundColor = Color(0xFFE8F5E9),
            textColor = Color(0xFF2E7D32)
        )

        cleanedStatus.contains("pending") -> StatusStyle(
            backgroundColor = Color(0xFFFFF3CD),
            textColor = Color(0xFF8A5A00)
        )

        else -> StatusStyle(
            backgroundColor = Color(0xFFE3F2FD),
            textColor = Color(0xFF1565C0)
        )
    }
}

@Composable
private fun ProfileMessageText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

/*
 * Android Studio preview.
 * This helps us check the UI before connecting it to real backend data.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverProfileSettingsScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverProfileSettingsScreen()
    }
}