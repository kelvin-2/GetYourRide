package com.example.getyourride.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

private val DriverBackground = Color(0xFFFBF8FD)
private val DriverPrimary = Color(0xFF011844)
private val DriverTopBar = Color(0xFF1A2E5A)
private val DriverAccent = Color(0xFFFC820C)
private val DriverCardBackground = Color(0xFFFFFFFF)
private val DriverCardBorder = Color(0xFFC5C6D0)
private val DriverFieldBackground = Color(0xFFE3E2E6)
private val DriverText = Color(0xFF1B1B1F)
private val DriverTextMuted = Color(0xFF44464F)
private val DriverOutline = Color(0xFF757780)
private val DriverInactiveProgress = Color(0xFFE3E2E6)
private val DriverPendingBackground = Color(0xFFFFF3CD)
private val DriverPendingText = Color(0xFF8A5A00)
private val DriverPrimaryFixed = Color(0xFFDAE2FF)

data class DriverStep3Data(
    val driversLicenceFileName: String,
    val vehicleRegistrationFileName: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStep3Screen(
    onBackClick: () -> Unit = {},
    onSubmitClick: (DriverStep3Data) -> Unit = {}
) {
    var driversLicenceFileName by rememberSaveable {
        mutableStateOf("")
    }

    var vehicleRegistrationFileName by rememberSaveable {
        mutableStateOf("")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "GetYourRide",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Text(
                        text = "STEP 3 OF 3",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DriverTopBar
                )
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    content = {
                        OutlinedButton(
                            onClick = onBackClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, DriverOutline),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = DriverPrimary
                            )
                        ) {
                            Text(
                                text = "Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Button(
                            onClick = {
                                onSubmitClick(
                                    DriverStep3Data(
                                        driversLicenceFileName = driversLicenceFileName,
                                        vehicleRegistrationFileName = vehicleRegistrationFileName
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(2f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DriverAccent
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text(
                                text = "Submit Profile",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                )
            }
        },
        containerColor = DriverBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DriverBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            content = {
                DriverStep3ProgressIndicator(
                    currentStep = 3,
                    totalSteps = 3
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "Upload Documents",
                    color = DriverPrimary,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "We need to verify your driver credentials to ensure safety on the platform.",
                    color = DriverTextMuted,
                    fontSize = 16.sp,
                    lineHeight = 24.sp
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    content = {
                        DriverDocumentCard(
                            title = "Driver's Licence",
                            fileName = driversLicenceFileName,
                            emptyText = "No driver's licence selected",
                            icon = Icons.Outlined.Badge,
                            onChooseFileClick = {
                                driversLicenceFileName = "licence_front_final.jpg"
                            }
                        )

                        DriverDocumentCard(
                            title = "Vehicle Registration",
                            fileName = vehicleRegistrationFileName,
                            emptyText = "No vehicle registration selected",
                            icon = Icons.Outlined.Description,
                            onChooseFileClick = {
                                vehicleRegistrationFileName = "registration_doc_2023.pdf"
                            }
                        )
                    }
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        )
    }
}

@Composable
private fun DriverStep3ProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth(),
        content = {
            repeat(totalSteps) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(
                            if (index + 1 == currentStep) {
                                DriverAccent
                            } else {
                                DriverInactiveProgress
                            }
                        )
                )
            }
        }
    )
}

@Composable
private fun DriverDocumentCard(
    title: String,
    fileName: String,
    emptyText: String,
    icon: ImageVector,
    onChooseFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverCardBackground
        ),
        border = BorderStroke(1.dp, DriverCardBorder),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    content = {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(DriverPrimaryFixed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = DriverPrimary,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        DriverPendingBadge()
                    }
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    content = {
                        Text(
                            text = title,
                            color = DriverText,
                            fontSize = 20.sp,
                            lineHeight = 28.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Text(
                            text = if (fileName.isBlank()) emptyText else fileName,
                            color = DriverOutline,
                            fontSize = 14.sp,
                            lineHeight = 21.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                )

                Button(
                    onClick = onChooseFileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DriverFieldBackground,
                        contentColor = DriverTextMuted
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.UploadFile,
                        contentDescription = null,
                        tint = DriverTextMuted
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Choose File",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DriverTextMuted
                    )
                }
            }
        )
    }
}

@Composable
private fun DriverPendingBadge() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(DriverPendingBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Pending",
            color = DriverPendingText,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.8.sp
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverStep3ScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverStep3Screen()
    }
}