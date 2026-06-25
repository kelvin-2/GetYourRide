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
import com.example.getyourride.ui.screens.DriverStep1Screen
import com.example.getyourride.ui.screens.DriverStep2Screen
import com.example.getyourride.ui.screens.DriverStep3Screen

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
                            onBecomeDriverClick = { navController.navigate("driver_step_1") },
                            onSignUpClick = { _, _, _, _, _ -> navController.navigate("home") }
                        )
                    }
                    composable("driver_step_1") {
                        DriverStep1Screen(
                            onBackClick = { navController.popBackStack() },
                            onNextClick = { navController.navigate("driver_step_2") }
                        )
                    }
                    composable("driver_step_2") {
                        DriverStep2Screen(
                            onBackClick = { navController.popBackStack() },
                            onNextClick = { navController.navigate("driver_step_3") }
                        )
                    }
                    composable("driver_step_3") {
                        DriverStep3Screen(
                            onBackClick = { navController.popBackStack() },
                            onSubmitClick = { navController.navigate("home") }
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