package com.example.getyourride.ui.screens.profiles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// NEW: import the decorative navy-map background component
import com.example.getyourride.ui.components.GyrMapBackground
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.InfoCard
import com.example.getyourride.ui.components.InfoRowData
import com.example.getyourride.ui.components.ProfileHeader
import com.example.getyourride.ui.components.SettingsItem
import com.example.getyourride.ui.components.SettingsListCard
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.viewmodel.ProfileUiState
import com.example.getyourride.viewmodel.ProfileViewModel

private val ScreenBackground = Color(0xFFF5F6FA)
private val LogoutRed = Color(0xFFEB5757)

@Composable
fun ProfileScreen(
    navController: NavController,
    onEditProfile: () -> Unit = {},
    onMyRides: () -> Unit = {},
    onPaymentMethods: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onHelpSupport: () -> Unit = {},
    onLoggedOut: () -> Unit = {},
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    StudentLayout(
        currentRoute = GyrRoutes.PROFILE,
        navController = navController
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScreenBackground)
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = NavyPrimary
                    )
                }

                is ProfileUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = state.message, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        TextButton(onClick = { viewModel.loadProfile() }) {
                            Text("Retry")
                        }
                    }
                }

                is ProfileUiState.Success -> {
                    val profile = state.profile

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // CHANGED: ProfileHeader is now wrapped in GyrMapBackground
                        // so the navy section gets the decorative radar/pins/route
                        // pattern instead of a flat navy fill.
                        //
                        // - fillMaxWidth() (not fillMaxSize()) so it only takes the
                        //   width, and wraps to ProfileHeader's natural height —
                        //   since GyrMapBackground no longer forces fillMaxSize()
                        //   internally (see GyrMapBackground.kt fix), this sizes
                        //   correctly to just the header, not the whole screen.
                        // - backgroundColor = NavyPrimary keeps it consistent with
                        //   the rest of your navy/orange design system.
                        GyrMapBackground(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = NavyPrimary
                        ) {
                            ProfileHeader(
                                name = profile.name,
                                initials = profile.initials,
                                studentNumber = profile.studentNumber,
                                isNsfasFunded = profile.isNsfasFunded
                            )
                        }

                        // Unchanged: white card section below the header
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .offset(y = (-32).dp)
                        ) {
                            InfoCard(
                                rows = listOf(
                                    InfoRowData(
                                        icon = Icons.Outlined.Email,
                                        label = "EMAIL",
                                        value = profile.email
                                    ),
                                    InfoRowData(
                                        icon = Icons.Outlined.Phone,
                                        label = "PHONE",
                                        value = profile.phone
                                    ),
                                    InfoRowData(
                                        icon = Icons.Outlined.Badge,
                                        label = "STUDENT NUMBER",
                                        value = profile.studentNumber
                                    )
                                )
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Account Settings",
                                color = NavyPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                            )

                            val settingsItems = buildList {
                                add(SettingsItem(Icons.Outlined.Edit, "Edit Profile", onEditProfile))
                                add(SettingsItem(Icons.Outlined.DirectionsCar, "My Rides", onMyRides))
                                // Payment Methods only makes sense for students paying
                                // out of pocket (carpool). NSFAS shuttle rides are
                                // pre-funded, so this is hidden for those students.
                                if (!profile.isNsfasFunded) {
                                    add(SettingsItem(Icons.Outlined.CreditCard, "Payment Methods", onPaymentMethods))
                                }
                                add(SettingsItem(Icons.Outlined.Notifications, "Notifications", onNotifications))
                                add(SettingsItem(Icons.Outlined.HelpOutline, "Help & Support", onHelpSupport))
                            }

                            SettingsListCard(items = settingsItems)

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedButton(
                                onClick = { viewModel.onLogOut(onLoggedOut) },
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
            }
        }
    }
}