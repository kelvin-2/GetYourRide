package com.getyourride.app.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

// ---- Design system colors (matches your existing GetYourRide theme) ----
val NavyPrimary = Color(0xFF16224D)
val OrangeAccent = Color(0xFFF2994A)
val GreenSuccess = Color(0xFF27AE60)
val LogoutRed = Color(0xFFEB5757)
val CardBackground = Color(0xFFFFFFFF)
val ScreenBackground = Color(0xFFF5F6FA)
val TextSecondary = Color(0xFF8A8FA3)

data class StudentProfile(
    val name: String,
    val initials: String,
    val studentNumber: String,
    val email: String,
    val phone: String
)

data class SettingsItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(
    profile: StudentProfile,
    onEditProfile: () -> Unit = {},
    onMyRides: () -> Unit = {},
    onPaymentMethods: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onHelpSupport: () -> Unit = {},
    onLogOut: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // ---- Header ----
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyPrimary)
                .padding(top = 48.dp, bottom = 56.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(OrangeAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = profile.name,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = profile.studentNumber,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 13.sp
                )
            }
        }

        // ---- Info card (overlaps header) ----
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-32).dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    InfoRow(
                        icon = Icons.Outlined.Email,
                        label = "EMAIL",
                        value = profile.email
                    )
                    HorizontalDivider(color = ScreenBackground, thickness = 1.dp)
                    InfoRow(
                        icon = Icons.Outlined.Phone,
                        label = "PHONE",
                        value = profile.phone
                    )
                    HorizontalDivider(color = ScreenBackground, thickness = 1.dp)
                    InfoRow(
                        icon = Icons.Outlined.Badge,
                        label = "STUDENT NUMBER",
                        value = profile.studentNumber
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Account Settings ----
            Text(
                text = "Account Settings",
                color = NavyPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardBackground),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    val items = listOf(
                        SettingsItem(Icons.Outlined.Edit, "Edit Profile", onEditProfile),
                        SettingsItem(Icons.Outlined.DirectionsCar, "My Rides", onMyRides),
                        SettingsItem(Icons.Outlined.CreditCard, "Payment Methods", onPaymentMethods),
                        SettingsItem(Icons.Outlined.Notifications, "Notifications", onNotifications),
                        SettingsItem(Icons.Outlined.HelpOutline, "Help & Support", onHelpSupport)
                    )
                    items.forEachIndexed { index, item ->
                        SettingsRow(item = item)
                        if (index != items.lastIndex) {
                            HorizontalDivider(color = ScreenBackground, thickness = 1.dp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---- Log Out button ----
            OutlinedButton(
                onClick = onLogOut,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LogoutRed),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = LogoutRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Log Out", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(OrangeAccent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = OrangeAccent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                color = NavyPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun SettingsRow(item: SettingsItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = item.label,
            color = OrangeAccent,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(18.dp)
        )
    }
}