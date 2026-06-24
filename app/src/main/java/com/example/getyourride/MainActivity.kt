import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GetYourRideTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"   // ← app starts here
                ) {
                    composable("login") {
                        LoginScreen(
                            onCreateAccountClick = { navController.navigate("signup") },
                            onLoginClick = { email, password ->
                                // TODO: hook up ViewModel later
                                // for now just navigate to test
                                navController.navigate("home")
                            }
                        )
                    }

                    composable("signup") {
                        SignUpScreen(
                            onBackClick = { navController.popBackStack() },
                            onLoginClick = { navController.popBackStack() },
                            onSignUpClick = { _, _, _, _, _ ->
                                navController.navigate("home")
                            }
                        )
                    }

                    composable("home") {
                        // placeholder until you build HomeScreen
                        androidx.compose.material3.Text("Home Screen — coming soon")
                    }
                }
            }
        }
    }
}