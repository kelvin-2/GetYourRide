// ─────────────────────────────────────────────────────────────────────────────
// StudentLayout.kt
// Package: com.example.getyourride.ui.components
//
// PURPOSE — The single reusable wrapper for every student-facing screen.
//
// Think of this like a React Layout component:
//
//   // React equivalent:
//   <StudentLayout currentRoute="home">
//     <HomeContent />
//   </StudentLayout>
//
//   // Compose equivalent:
//   StudentLayout(currentRoute = GyrRoutes.HOME, navController = navController) {
//       HomeContent()
//   }
//
// What StudentLayout provides automatically to every screen:
//   ✅ GyrTopBar  — navy top bar with GetYourRide logo + optional bell
//   ✅ GyrBottomNav — 4-tab bottom nav, correct tab highlighted
//   ✅ innerPadding — content never hides behind bars
//
// What each screen provides:
//   → Just its own content (Column, LazyColumn, etc.)
//   → Nothing else needed
//
// HOW TO USE in any new screen:
//
//   @Composable
//   fun RidesScreen(navController: NavController) {
//       StudentLayout(
//           currentRoute  = GyrRoutes.RIDES,
//           navController = navController,
//       ) {
//           // your screen content here — no Scaffold, no topBar, no bottomBar needed
//           Text("Rides content")
//       }
//   }
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.theme.SurfaceGrey

/**
 * Reusable layout wrapper for all student screens.
 *
 * Attach to any screen by wrapping your content — the top bar and
 * bottom nav are handled automatically.
 *
 * @param currentRoute   Which tab to highlight. Use [GyrRoutes] constants.
 * @param navController  Used by [GyrBottomNav] to navigate between tabs.
 * @param showBell       Show notification bell in top bar. Default true.
 * @param content        The screen's own content — rendered between the bars.
 */
@Composable
fun StudentLayout(
    currentRoute  : String,
    navController : NavController,
    showBell      : Boolean = true,
    content       : @Composable () -> Unit,
) {
    Scaffold(
        containerColor = SurfaceGrey,
        // ── Top bar — consistent across all student screens ───────────────────
        topBar = {
            GyrTopBar(
                onBackClick   = null,      // home screens never show a back arrow
                trailingLabel = null,      // no step label on main screens

            )
        },

        // ── Bottom nav — tabs stay fixed, only the highlight changes ──────────
        bottomBar = {
            GyrBottomNav(
                currentRoute = currentRoute,
                onNavigate   = { route ->
                    // Navigate without stacking duplicates on the back stack
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
            )
        },
    ) { innerPadding ->
        // innerPadding keeps content clear of both bars
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            content()
        }
    }
}