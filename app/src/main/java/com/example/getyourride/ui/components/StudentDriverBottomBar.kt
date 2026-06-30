package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Person
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
 * This enum tells the bottom menu which item is currently selected.
 *
 * Example:
 * - Home screen uses StudentDriverBottomBarItem.Home
 * - Offer Ride screen uses StudentDriverBottomBarItem.OfferRide
 * - Profile screen uses StudentDriverBottomBarItem.Profile
 */
enum class StudentDriverBottomBarItem {
    Home,
    OfferRide,
    Profile
}

/*
 * Reusable bottom navigation bar for all student driver screens.
 *
 * This keeps the Home, Offer Ride, and Profile screens consistent.
 * If we change the bottom menu design later, we only edit this one file.
 */
@Composable
fun StudentDriverBottomBar(
    selectedItem: StudentDriverBottomBarItem,
    onHomeClick: () -> Unit,
    onOfferRideClick: () -> Unit,
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
                selected = selectedItem == StudentDriverBottomBarItem.Home,
                onClick = onHomeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text(text = "Home")
                },
                colors = studentDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == StudentDriverBottomBarItem.OfferRide,
                onClick = onOfferRideClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocalTaxi,
                        contentDescription = "Offer Ride"
                    )
                },
                label = {
                    Text(text = "Offer Ride")
                },
                colors = studentDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == StudentDriverBottomBarItem.Profile,
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
                colors = studentDriverNavigationColors()
            )
        }
    }
}

/*
 * Shared colours for the selected and unselected bottom menu items.
 */
@Composable
private fun studentDriverNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = Color(0xFF011844),
        selectedTextColor = Color(0xFF011844),
        indicatorColor = Color(0xFFDAE2FF),
        unselectedIconColor = Color(0xFF44464F),
        unselectedTextColor = Color(0xFF44464F)
    )