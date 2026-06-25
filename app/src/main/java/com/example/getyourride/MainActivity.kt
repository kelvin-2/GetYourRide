package com.example.getyourride

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.ui.screens.LoginScreen
import com.example.getyourride.ui.screens.SignUpScreen
import com.example.getyourride.ui.theme.GetYourRideTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()

                NavHost(
                    navController    = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick  = { navController.navigate("signup") },
                            onLoginClick          = { _, _ -> navController.navigate("home") }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onBackClick   = { navController.popBackStack() },
                            onLoginClick  = { navController.popBackStack() },
                            onSignUpClick = { _, _, _, _, _ -> navController.navigate("home") }
                        )
                    }

                    composable("home") {
                        androidx.compose.material3.Text("Home Screen — coming soon")
                    }
                }
            }
        }
    }
}