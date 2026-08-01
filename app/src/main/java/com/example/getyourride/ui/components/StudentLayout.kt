package com.example.getyourride.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.getyourride.ui.theme.SurfaceGrey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLayout(
    currentRoute: String,
    navController: NavController,
    showBottomBar: Boolean = true,
    showTopBar: Boolean = true,
    onBackClick: (() -> Unit)? = null,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {

    Scaffold(
        containerColor = SurfaceGrey,

        topBar = {
            if (showTopBar && onBackClick != null) {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = SurfaceGrey
                    )
                )
            }
        },

        bottomBar = {
            if (showBottomBar) {
                GyrBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        try {
                            navController.navigate(route) {
                                // SAFELY access the graph if it exists. 
                                // Sometimes Live Edit or fast tab switching can 
                                // trigger a nav call before the graph is attached.
                                try {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                } catch (e: Exception) {
                                    // Fallback: graph not ready or startDestination missing
                                }

                                restoreState = true
                                launchSingleTop = true
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("StudentLayout", "Navigation failed: ${e.message}")
                        }
                    }
                )
            }
        },

        floatingActionButton = floatingActionButton

    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            content()

        }
    }

}