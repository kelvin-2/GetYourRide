package com.example.getyourride.ui.screens.profiles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.getyourride.ui.components.GyrBottomNav
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.ProfileHeader
import com.example.getyourride.ui.theme.CardWhite
import com.example.getyourride.ui.theme.IconTint
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.OrangeAccent
import com.example.getyourride.viewmodel.ProfileUiState
import com.example.getyourride.viewmodel.ProfileViewModel

@Composable
fun ProfileScreen(
    onEditProfile: () -> Unit,
    onMyRides: () -> Unit,
    onLoggedOut: () -> Unit,
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            GyrBottomNav(
                currentRoute = GyrRoutes.PROFILE,
                onNavigate = { route ->
                    if (route != GyrRoutes.PROFILE) {
                        navController.navigate(route) {
                            popUpTo(GyrRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF8F9FA))
        ) {
            when (val state = uiState) {
                is ProfileUiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangeAccent)
                    }
                }
                is ProfileUiState.Success -> {
                    val profile = state.profile
                    ProfileHeader(
                        name = profile.name,
                        initials = profile.initials,
                        studentNumber = profile.studentNumber,
                        isNsfasFunded = profile.isNsfasFunded
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text(
                            text = "Account Settings",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NavyPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ProfileMenuItem(
                            icon = Icons.Default.Person,
                            label = "Edit Profile",
                            onClick = onEditProfile
                        )
                        ProfileMenuItem(
                            icon = Icons.Default.DirectionsCar,
                            label = "My Rides",
                            onClick = onMyRides
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        ProfileMenuItem(
                            icon = Icons.Default.ExitToApp,
                            label = "Log Out",
                            textColor = Color.Red,
                            iconColor = Color.Red,
                            showChevron = false,
                            onClick = {
                                viewModel.onLogOut(onLoggedOut)
                            }
                        )
                    }
                }
                is ProfileUiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = Color.Red)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color = NavyPrimary,
    iconColor: Color = IconTint,
    showChevron: Boolean = true
) {
    Surface(
        onClick = onClick,
        color = CardWhite,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 16.sp,
                color = textColor,
                modifier = Modifier.weight(1f)
            )
            if (showChevron) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = IconTint.copy(alpha = 0.5f)
                )
            }
        }
    }
}
