package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.getyourride.ui.components.GyrBottomNavButtons
import com.example.getyourride.ui.components.GyrStepProgressBar
import com.example.getyourride.ui.components.GyrTopBar
import com.example.getyourride.ui.theme.NavyPrimary
import com.example.getyourride.ui.theme.SurfaceGrey

@Composable
fun BecomeADriverScreen(
    onBackToHome : () -> Unit = {},
    onSubmit     : () -> Unit = {},
) {
    val totalSteps     = 3
    var currentStep    by remember { mutableIntStateOf(1) }

    // Each step's form data lives here so it survives step navigation
    var studentState   by remember { mutableStateOf(StudentDetailsState()) }
    var vehicleState   by remember { mutableStateOf(VehicleDetailsState()) }
    var documentsState by remember { mutableStateOf(DocumentsState()) }

    Scaffold(
        topBar = {
            Column {
                GyrTopBar(
                    onBackClick   = { if (currentStep > 1) currentStep-- else onBackToHome() },
                    trailingLabel = "STEP $currentStep OF $totalSteps",
                )
                Box(Modifier.background(NavyPrimary)) {
                    GyrStepProgressBar(
                        totalSteps  = totalSteps,
                        currentStep = currentStep,
                    )
                }
            }
        },
        bottomBar = {
            GyrBottomNavButtons(
                currentStep = currentStep,
                totalSteps  = totalSteps,
                onBack      = { if (currentStep > 1) currentStep-- else onBackToHome() },
                onNext      = { currentStep++ },
                onSubmit    = onSubmit,
            )
        },
        containerColor = SurfaceGrey, // Fix: Added background color to match Login/Signup
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            when (currentStep) {
                1 -> DriverStep1Verification(
                    state              = studentState,
                    onFullNameChange   = { studentState = studentState.copy(fullName      = it) },
                    onStudentNumChange = { studentState = studentState.copy(studentNumber = it) },
                    onContactChange    = { studentState = studentState.copy(contactNumber = it) },
                    onEmailChange      = { studentState = studentState.copy(email         = it) }
                )

                2 -> DriverStep2VehicleDetails(
                    state          = vehicleState,
                    onRegChange    = { vehicleState = vehicleState.copy(regNumber    = it) },
                    onMakeChange   = { vehicleState = vehicleState.copy(makeAndModel = it) },
                    onColourChange = { vehicleState = vehicleState.copy(colour       = it) },
                    onSeatInc      = { vehicleState = vehicleState.copy(seatCapacity = (vehicleState.seatCapacity + 1).coerceAtMost(8)) },
                    onSeatDec      = { vehicleState = vehicleState.copy(seatCapacity = (vehicleState.seatCapacity - 1).coerceAtLeast(1)) },
                )

                3 -> DriverStep3Documents(
                    state            = documentsState,
                    onPickLicence    = { documentsState = documentsState.copy(licenceFileName = "licence_front.jpg") },
                    onPickVehicleReg = { documentsState = documentsState.copy(vehicleFileName = "registration_doc.pdf") },
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Become A Driver — Full Flow")
@Composable
fun BecomeADriverPreview() {
    BecomeADriverScreen()
}