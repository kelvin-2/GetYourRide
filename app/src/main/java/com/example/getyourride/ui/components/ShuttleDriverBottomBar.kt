package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FactCheck
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/*
 * Bottom menu items used by the shuttle driver screens.
 *
 * The shuttle driver will mainly use:
 * - Scan QR Code: for scanning student QR codes later
 * - Boarding: for marking booked students as boarded
 * - Profile: for shuttle driver profile details later
 */
enum class ShuttleDriverBottomBarItem {
    ScanQrCode,
    Boarding,
    Profile
}

/*
 * Reusable bottom navigation bar for shuttle driver screens.
 *
 * We create it once and reuse it on:
 * - Shuttle Driver Boarding
 * - Shuttle Driver Scan QR Code
 * - Shuttle Driver Profile
 */
@Composable
fun ShuttleDriverBottomBar(
    selectedItem: ShuttleDriverBottomBarItem,
    onScanQrCodeClick: () -> Unit,
    onBoardingClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = selectedItem == ShuttleDriverBottomBarItem.ScanQrCode,
                onClick = onScanQrCodeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.QrCodeScanner,
                        contentDescription = "Scan QR Code"
                    )
                },
                label = {
                    Text(text = "Scan QR")
                },
                colors = shuttleDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == ShuttleDriverBottomBarItem.Boarding,
                onClick = onBoardingClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.FactCheck,
                        contentDescription = "Boarding"
                    )
                },
                label = {
                    Text(text = "Boarding")
                },
                colors = shuttleDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == ShuttleDriverBottomBarItem.Profile,
                onClick = onProfileClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = {
                    Text(text = "Profile")
                },
                colors = shuttleDriverNavigationColors()
            )
        }
    }
}

/*
 * Shared bottom menu colours.
 */
@Composable
private fun shuttleDriverNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF011844),
        selectedTextColor = Color(0xFF011844),
        indicatorColor = Color(0xFFDAE2FF),
        unselectedIconColor = Color(0xFF44464F),
        unselectedTextColor = Color(0xFF44464F)
    )