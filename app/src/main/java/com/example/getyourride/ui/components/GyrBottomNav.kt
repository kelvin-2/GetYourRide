// ─────────────────────────────────────────────────────────────────────────────
// GyrBottomNav.kt
// Role: Reusable bottom navigation bar for all student-facing screens.
//
// Usage — wrap any screen with GyrScaffold and pass the current route:
//
//   GyrScaffold(currentRoute = "home", onNavigate = { navController.navigate(it) }) {
//       HomeScreen()
//   }
//
// Routes (use the constants below — never raw strings):
//   GyrRoutes.HOME    → Home tab
//   GyrRoutes.RIDES   → Rides tab
//   GyrRoutes.TRACK   → Track tab
//   GyrRoutes.PROFILE → Profile tab
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.*

// ─── Route constants — use these everywhere, never raw strings ────────────────

object GyrRoutes {
    const val HOME    = "home"
    const val RIDES   = "rides"
    const val TRACK   = "track"
    const val PROFILE = "profile"
}

// ─── Data model for each tab ──────────────────────────────────────────────────

private data class NavTab(
    val route       : String,
    val label       : String,
    val activeIcon  : ImageVector,   // filled — shown when this tab is selected
    val inactiveIcon: ImageVector,   // outlined — shown when another tab is selected
)

private val tabs = listOf(
    NavTab(GyrRoutes.HOME,    "Home",    Icons.Filled.Home,         Icons.Outlined.Home),
    NavTab(GyrRoutes.RIDES,   "Rides",   Icons.Filled.DirectionsCar,Icons.Outlined.DirectionsCar),
    NavTab(GyrRoutes.TRACK,   "Track",   Icons.Filled.LocationOn,   Icons.Outlined.LocationOn),
    NavTab(GyrRoutes.PROFILE, "Profile", Icons.Filled.Person,       Icons.Outlined.Person),
)

// ─── The nav bar composable ───────────────────────────────────────────────────

/**
 * Bottom navigation bar matching the GetYourRide design system.
 *
 * @param currentRoute  The active route string — use [GyrRoutes] constants.
 * @param onNavigate    Called with the route string when a tab is tapped.
 */
@Composable
fun GyrBottomNav(
    currentRoute : String,
    onNavigate   : (route: String) -> Unit,
) {
    NavigationBar(
        containerColor = CardWhite,
        tonalElevation = 8.dp,
    ) {
        tabs.forEach { tab ->
            val isActive = currentRoute == tab.route

            NavigationBarItem(
                selected  = isActive,
                onClick   = {
                    // Only navigate if not already on this tab
                    if (!isActive) onNavigate(tab.route)
                },
                icon = {
                    Icon(
                        imageVector        = if (isActive) tab.activeIcon else tab.inactiveIcon,
                        contentDescription = tab.label,
                    )
                },
                label = {
                    Text(
                        text     = tab.label,
                        fontSize = 11.sp,
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = OrangeAccent,    // active icon colour
                    selectedTextColor   = OrangeAccent,    // active label colour
                    unselectedIconColor = IconTint,        // inactive icon colour
                    unselectedTextColor = IconTint,        // inactive label colour
                    indicatorColor      = OrangeAccent.copy(alpha = 0.10f), // subtle pill behind active icon
                ),
            )
        }
    }
}

// ─── Convenience scaffold wrapper ─────────────────────────────────────────────

/**
 * Drop-in Scaffold wrapper that attaches [GyrBottomNav] to any student screen.
 *
 * Example:
 *   GyrScaffold(currentRoute = GyrRoutes.HOME, onNavigate = { navController.navigate(it) }) {
 *       HomeContent()
 *   }
 *
 * @param currentRoute  Active tab route — use [GyrRoutes] constants.
 * @param onNavigate    Navigation callback — usually navController.navigate(route).
 * @param topBar        Optional top bar slot — defaults to [GyrTopBar].
 * @param content       The screen content displayed above the nav bar.
 */
@Composable
fun GyrScaffold(
    currentRoute : String,
    onNavigate   : (String) -> Unit,
    topBar       : @Composable () -> Unit = {},
    content      : @Composable () -> Unit,
) {
    Scaffold(
        topBar    = topBar,
        bottomBar = {
            GyrBottomNav(
                currentRoute = currentRoute,
                onNavigate   = onNavigate,
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

// ─── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, name = "Nav — Home active")
@Composable
fun NavHomePreview() {
    MaterialTheme { GyrBottomNav(currentRoute = GyrRoutes.HOME, onNavigate = {}) }
}

@Preview(showBackground = true, name = "Nav — Rides active")
@Composable
fun NavRidesPreview() {
    MaterialTheme { GyrBottomNav(currentRoute = GyrRoutes.RIDES, onNavigate = {}) }
}

@Preview(showBackground = true, name = "Nav — Track active")
@Composable
fun NavTrackPreview() {
    MaterialTheme { GyrBottomNav(currentRoute = GyrRoutes.TRACK, onNavigate = {}) }
}

@Preview(showBackground = true, name = "Nav — Profile active")
@Composable
fun NavProfilePreview() {
    MaterialTheme { GyrBottomNav(currentRoute = GyrRoutes.PROFILE, onNavigate = {}) }
}