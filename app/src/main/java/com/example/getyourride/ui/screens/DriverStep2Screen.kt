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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

private val DriverBackground = Color(0xFFFBF8FD)
private val DriverPrimary = Color(0xFF011844)
private val DriverTopBar = Color(0xFF1A2E5A)
private val DriverAccent = Color(0xFFFC820C)
private val DriverFieldBackground = Color(0xFFE3E2E6)
private val DriverText = Color(0xFF1B1B1F)
private val DriverTextMuted = Color(0xFF44464F)
private val DriverOutline = Color(0xFF757780)
private val DriverInactiveProgress = Color(0xFFE3E2E6)

data class DriverStep2Data(
    val vehicleRegistrationNumber: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleColour: String,
    val seatingCapacity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverStep2Screen(
    onBackClick: () -> Unit = {},
    onNextClick: (DriverStep2Data) -> Unit = {}
) {
    var vehicleRegistrationNumber by rememberSaveable {
        mutableStateOf("")
    }

    var vehicleMake by rememberSaveable {
        mutableStateOf("")
    }

    var vehicleModel by rememberSaveable {
        mutableStateOf("")
    }

    var vehicleColour by rememberSaveable {
        mutableStateOf("")
    }

    var seatingCapacity by rememberSaveable {
        mutableStateOf(4)
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
                        text = "STEP 2 OF 3",
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
                                onNextClick(
                                    DriverStep2Data(
                                        vehicleRegistrationNumber = vehicleRegistrationNumber,
                                        vehicleMake = vehicleMake,
                                        vehicleModel = vehicleModel,
                                        vehicleColour = vehicleColour,
                                        seatingCapacity = seatingCapacity
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
                                text = "Next",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )

                            Spacer(
                                modifier = Modifier.width(8.dp)
                            )

                            Icon(
                                imageVector = Icons.Outlined.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
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
                DriverStep2ProgressIndicator(
                    currentStep = 2,
                    totalSteps = 3
                )

                Spacer(
                    modifier = Modifier.height(32.dp)
                )

                Text(
                    text = "Vehicle Details",
                    color = DriverPrimary,
                    fontSize = 32.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(
                    modifier = Modifier.height(4.dp)
                )

                Text(
                    text = "Tell us about the vehicle you'll be using for university rides.",
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
                        DriverStep2FormField(
                            label = "Vehicle Registration Number",
                            value = vehicleRegistrationNumber,
                            placeholder = "Example: ABC 1234",
                            onValueChange = { vehicleRegistrationNumber = it },
                            icon = Icons.Outlined.Badge,
                            capitalization = KeyboardCapitalization.Characters
                        )

                        DriverStep2FormField(
                            label = "Make",
                            value = vehicleMake,
                            placeholder = "Example: Toyota",
                            onValueChange = { vehicleMake = it },
                            icon = Icons.Outlined.DirectionsCar,
                            capitalization = KeyboardCapitalization.Words
                        )

                        DriverStep2FormField(
                            label = "Model",
                            value = vehicleModel,
                            placeholder = "Example: Corolla",
                            onValueChange = { vehicleModel = it },
                            icon = Icons.Outlined.DirectionsCar,
                            capitalization = KeyboardCapitalization.Words
                        )

                        DriverStep2FormField(
                            label = "Vehicle Colour",
                            value = vehicleColour,
                            placeholder = "Example: Silver Metallic",
                            onValueChange = { vehicleColour = it },
                            icon = Icons.Outlined.Palette,
                            capitalization = KeyboardCapitalization.Words
                        )

                        DriverStep2CapacityField(
                            seatingCapacity = seatingCapacity,
                            onDecreaseClick = {
                                if (seatingCapacity > 1) {
                                    seatingCapacity--
                                }
                            },
                            onIncreaseClick = {
                                if (seatingCapacity < 8) {
                                    seatingCapacity++
                                }
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
private fun DriverStep2ProgressIndicator(
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
private fun DriverStep2FormField(
    label: String,
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            Text(
                text = label.uppercase(),
                color = DriverTextMuted,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.24.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = DriverOutline
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = DriverOutline,
                        modifier = Modifier.size(22.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = keyboardType,
                    capitalization = capitalization
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = DriverText,
                    unfocusedTextColor = DriverText,
                    focusedPlaceholderColor = DriverOutline,
                    unfocusedPlaceholderColor = DriverOutline,
                    cursorColor = DriverPrimary,
                    focusedBorderColor = DriverPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = DriverFieldBackground,
                    unfocusedContainerColor = DriverFieldBackground
                )
            )
        }
    )
}

@Composable
private fun DriverStep2CapacityField(
    seatingCapacity: Int,
    onDecreaseClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = {
            Text(
                text = "SEATING CAPACITY",
                color = DriverTextMuted,
                fontSize = 12.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.24.sp,
                modifier = Modifier.padding(start = 4.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DriverFieldBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                content = {
                    Column(
                        content = {
                            Text(
                                text = "Seats available",
                                color = DriverText,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                fontWeight = FontWeight.Normal
                            )

                            Text(
                                text = "Including driver's seat",
                                color = DriverTextMuted,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                        }
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        content = {
                            IconButton(
                                onClick = onDecreaseClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Remove,
                                    contentDescription = "Decrease seating capacity",
                                    tint = DriverPrimary
                                )
                            }

                            Text(
                                text = seatingCapacity.toString(),
                                color = DriverPrimary,
                                fontSize = 20.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            IconButton(
                                onClick = onIncreaseClick,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Add,
                                    contentDescription = "Increase seating capacity",
                                    tint = DriverPrimary
                                )
                            }
                        }
                    )
                }
            )
        }
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverStep2ScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverStep2Screen()
    }
}