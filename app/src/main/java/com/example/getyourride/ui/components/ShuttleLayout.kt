package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

/**
 * Specialized layout for Shuttle screens.
 * - No TopBar (as requested)
 * - BottomNav excludes the "Tracking" tab
 *
 * @param currentRoute  Active tab route — use [GyrRoutes] constants.
 * @param navController Navigation controller — used for tab switching.
 * @param content       The screen content displayed.
 */
@Composable
fun ShuttleLayout(
    currentRoute : String,
    navController: NavController,
    content      : @Composable () -> Unit,
) {
    Scaffold(
        bottomBar = {
            GyrBottomNav(
                currentRoute = currentRoute,
                onNavigate   = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                isShuttle = true
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            content()
        }
    }
}
