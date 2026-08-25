package com.example.getyourride.ui.screens.shuttleDriver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info

import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Route

import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.remote.dto.ShuttleDriverProfileResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverVehicleResponse
import com.example.getyourride.ui.components.ShuttleDriverBottomBar
import com.example.getyourride.ui.components.ShuttleDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.ShuttleDriverProfileUiState

// ── Design tokens ───────────────────────────────────────────────────────────
private val ProfileBackground = Color(0xFFF6F8FC)
private val ProfilePrimary = Color(0xFF0D1B4A)
private val ProfileAccent = Color(0xFFFC820C)
private val ProfileTopBarStart = Color(0xFF0D1B4A)
private val ProfileTopBarEnd = Color(0xFF1A3A7A)
private val ProfileCardBg = Color(0xFFFFFFFF)
private val ProfileText = Color(0xFF1B1B1F)
private val ProfileTextMuted = Color(0xFF5E6278)
private val ProfileIconBg = Color(0xFFEDF1FA)
private val ProfileDivider = Color(0xFFE8EBF0)
private val ProfileInfoBg = Color(0xFFE8F4FD)
private val ProfileInfoText = Color(0xFF1565C0)
private val ProfileSuccessBg = Color(0xFFE8F5E9)
private val ProfileSuccessText = Color(0xFF2E7D32)
private val ProfileWarningBg = Color(0xFFFFF8E1)
private val ProfileWarningText = Color(0xFFE65100)
private val ProfileErrorBg = Color(0xFFFFEBEE)
private val ProfileErrorText = Color(0xFFC62828)
private val ProfileLogoutBg = Color(0xFFFFF0F0)
private val ProfileLogoutText = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleDriverProfileScreen(
    uiState: ShuttleDriverProfileUiState,
    onLoadProfile: () -> Unit = {},
    onLogoutClick: () -> Unit = {},
    onScanQrCodeClick: () -> Unit = {},
    onBoardingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    // Load profile data when screen opens
    LaunchedEffect(Unit) {
        onLoadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsBus,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
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
                    containerColor = ProfileTopBarStart
                )
            )
        },
        bottomBar = {
            ShuttleDriverBottomBar(
                selectedItem = ShuttleDriverBottomBarItem.Profile,
                onScanQrCodeClick = onScanQrCodeClick,
                onBoardingClick = onBoardingClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = ProfileBackground
    ) { innerPadding ->

        when (uiState) {
            is ShuttleDriverProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = ProfileAccent,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading profile...",
                            color = ProfileTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is ShuttleDriverProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ProfileErrorBg,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = ProfileErrorText,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = uiState.message,
                            color = ProfileErrorText,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onLoadProfile,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ProfilePrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            is ShuttleDriverProfileUiState.Success -> {
                ProfileContent(
                    profile = uiState.profile,
                    onLogoutClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

@Composable
private fun ProfileContent(
    profile: ShuttleDriverProfileResponse,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Profile Header Card ─────────────────────────────────────────
        ProfileHeaderCard(profile)

        // ── Admin Managed Notice ────────────────────────────────────────
        AdminManagedNotice()

        // ── Driver Information ──────────────────────────────────────────
        SectionCard(
            title = "Driver Information",
            icon = Icons.Outlined.Person
        ) {
            InfoRow(icon = Icons.Outlined.Email, label = "Email", value = profile.email)
            ProfileDividerLine()
            InfoRow(icon = Icons.Outlined.Phone, label = "Phone", value = profile.phone ?: "Not provided")
            ProfileDividerLine()
            InfoRow(icon = Icons.Outlined.VerifiedUser, label = "Role", value = formatRole(profile.role))
            ProfileDividerLine()
            InfoRow(icon = Icons.Outlined.CalendarMonth, label = "Member Since", value = profile.joinDate ?: "Unknown")
            ProfileDividerLine()
            InfoRow(icon = Icons.Outlined.Route, label = "Total Trips Completed", value = profile.totalTrips.toString())
        }

        // ── Vehicle Details ─────────────────────────────────────────────
        profile.vehicle?.let { vehicle ->
            SectionCard(
                title = "Assigned Vehicle",
                icon = Icons.Outlined.DirectionsCar
            ) {
                InfoRow(label = "Registration", value = vehicle.registrationNumber)
                ProfileDividerLine()
                InfoRow(label = "Model", value = vehicle.model ?: "Not set")
                ProfileDividerLine()
                InfoRow(label = "Year", value = vehicle.vehicleYear?.toString() ?: "Not set")
                ProfileDividerLine()
                InfoRow(label = "Colour", value = vehicle.colour ?: "Not set")
                ProfileDividerLine()
                InfoRow(label = "Capacity", value = "${vehicle.capacity} seats")
            }
        }

        // ── Logout Button ───────────────────────────────────────────────
        Button(
            onClick = onLogoutClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = ProfileLogoutBg,
                contentColor = ProfileLogoutText
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ExitToApp,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Log Out",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ── Profile Header ──────────────────────────────────────────────────────────

@Composable
private fun ProfileHeaderCard(profile: ShuttleDriverProfileResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(ProfileTopBarStart, ProfileTopBarEnd)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Avatar circle with initials
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "${profile.firstName.firstOrNull() ?: ""}${profile.lastName.firstOrNull() ?: ""}",
                            color = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "${profile.firstName} ${profile.lastName}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatRole(profile.role),
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp
                    )

                    // Verification badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (profile.isVerified) {
                            ProfileSuccessBg.copy(alpha = 0.9f)
                        } else {
                            ProfileWarningBg.copy(alpha = 0.9f)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.VerifiedUser,
                                contentDescription = null,
                                tint = if (profile.isVerified) ProfileSuccessText else ProfileWarningText,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (profile.isVerified) "Verified" else "Pending Verification",
                                color = if (profile.isVerified) ProfileSuccessText else ProfileWarningText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Admin Notice ────────────────────────────────────────────────────────────

@Composable
private fun AdminManagedNotice() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ProfileInfoBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = ProfileInfoText,
                modifier = Modifier.size(22.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Admin Managed Account",
                    color = ProfileInfoText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Your account is managed by the administrator. Contact admin to update your details.",
                    color = ProfileInfoText.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

// ── Section Card ────────────────────────────────────────────────────────────

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ProfileCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ProfileIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ProfilePrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    color = ProfilePrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(ProfileDivider)
            )

            content()
        }
    }
}

// ── Info Row ────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ProfileTextMuted,
                modifier = Modifier.size(18.dp)
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
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                color = ProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}



// ── Divider ─────────────────────────────────────────────────────────────────

@Composable
private fun ProfileDividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .height(1.dp)
            .background(ProfileDivider)
    )
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun formatRole(role: String): String {
    return when (role.uppercase()) {
        "SHUTTLE_DRIVER" -> "Shuttle Driver"
        "STUDENT_DRIVER" -> "Student Driver"
        else -> role.replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

// ── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShuttleDriverProfileScreenPreview() {
    val sampleProfile = ShuttleDriverProfileResponse(
        driverId = 1,
        firstName = "Thabo",
        lastName = "Nkosi",
        email = "thabo.nkosi@shuttle.nmu.ac.za",
        phone = "082 123 4501",
        role = "SHUTTLE_DRIVER",
        joinDate = "2024-02-01",
        totalTrips = 142,
        isVerified = true,
        vehicle = ShuttleDriverVehicleResponse(
            vehicleId = 1,
            registrationNumber = "NMU001EC",
            model = "Toyota Quantum",
            vehicleYear = 2021,
            colour = "White",
            capacity = 15
        ),
        tripSummary = null
    )

    GetYourRideTheme(dynamicColor = false) {
        ShuttleDriverProfileScreen(
            uiState = ShuttleDriverProfileUiState.Success(sampleProfile)
        )
    }
}
