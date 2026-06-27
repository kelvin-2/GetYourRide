package com.example.getyourride.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

private val SettingsBackground = Color(0xFFFBF8FD)
private val SettingsPrimary = Color(0xFF011844)
private val SettingsTopBar = Color(0xFF1A2E5A)
private val SettingsTextMuted = Color(0xFF44464F)
private val SettingsDanger = Color(0xFFC62828)
private val SettingsDangerContainer = Color(0xFFFFDAD6)
private val SettingsSuccess = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverProfileSettingsScreen(
    onBackClick: () -> Unit = {},
    onConfirmDeleteClick: () -> Unit = {},
    statusMessage: String? = null,
    errorMessage: String? = null
) {
    var confirmationVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Driver Profile",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SettingsTopBar
                )
            )
        },
        containerColor = SettingsBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(SettingsBackground)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Profile Settings",
                color = SettingsPrimary,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Deactivate your student driver profile if you no longer want to offer rides.",
                color = SettingsTextMuted,
                fontSize = 16.sp,
                lineHeight = 24.sp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                border = BorderStroke(1.dp, SettingsDanger.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Warning,
                            contentDescription = null,
                            tint = SettingsDanger
                        )

                        Text(
                            text = "Delete Student Driver Profile",
                            color = SettingsDanger,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "This deactivates your driver profile and vehicle. You will not be able to offer rides until a new profile is created and approved.",
                        color = SettingsTextMuted,
                        fontSize = 14.sp,
                        lineHeight = 21.sp
                    )

                    if (confirmationVisible) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { confirmationVisible = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = onConfirmDeleteClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SettingsDanger
                                )
                            ) {
                                Text(
                                    text = "Confirm",
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        Button(
                            onClick = { confirmationVisible = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SettingsDangerContainer,
                                contentColor = SettingsDanger
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
                                contentDescription = null
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                text = "Delete Profile",
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        SettingsMessageText(statusMessage, SettingsSuccess)
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        SettingsMessageText(errorMessage, SettingsDanger)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsMessageText(
    text: String,
    color: Color
) {
    Text(
        text = text,
        color = color,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverProfileSettingsScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverProfileSettingsScreen()
    }
}
