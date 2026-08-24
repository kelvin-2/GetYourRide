package com.example.getyourride.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.ErrorOutline
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.theme.GetYourRideTheme

// ─── Color Palette ───────────────────────────────────────────────────────────
private val StepBackground = Color(0xFFF4F6FB)
private val StepPrimary = Color(0xFF1A2E5A)
private val StepPrimaryLight = Color(0xFF2E4A82)
private val StepTopBar = Color(0xFF1A2E5A)
private val StepAccent = Color(0xFFFC820C)
private val StepCardBackground = Color(0xFFFFFFFF)
private val StepText = Color(0xFF1B1B1F)
private val StepTextMuted = Color(0xFF5E6278)
private val StepBorder = Color(0xFFE5E7EB)
private val StepOutline = Color(0xFF757780)
private val StepFieldBackground = Color(0xFFF9FAFB)
private val StepInactiveProgress = Color(0xFFE5E7EB)
private val StepError = Color(0xFFDC2626)
private val StepSectionOrange = Color(0xFFFC820C)

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
    onNextClick: (DriverStep2Data) -> Unit = {},
    errorMessage: String? = null
) {
    var vehicleRegistrationNumber by rememberSaveable { mutableStateOf("") }
    var vehicleMake by rememberSaveable { mutableStateOf("") }
    var vehicleModel by rememberSaveable { mutableStateOf("") }
    var vehicleColour by rememberSaveable { mutableStateOf("") }
    var seatingCapacity by rememberSaveable { mutableStateOf(4) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White
                        )
                        Text(
                            text = "GetYourRide",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = StepTopBar)
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onBackClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, StepBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = StepPrimary
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StepAccent),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        Text(
                            text = "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Outlined.ArrowForward,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        },
        containerColor = StepBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Gradient Hero Header ────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(StepTopBar, StepPrimaryLight),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 400f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Step icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Vehicle Details",
                            color = Color.White,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 28.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tell us about your vehicle for rides.",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Step badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = StepAccent
                        ) {
                            Text(
                                text = "Step 2 of 3",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Progress Indicator ──────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                if (index < 2) StepAccent else StepInactiveProgress
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Vehicle Details Card ────────────────────────────────────────
            Step2SectionCard(
                title = "Vehicle Information",
                icon = Icons.Outlined.DirectionsCar,
                iconTint = StepSectionOrange
            ) {
                Step2FormField(
                    label = "Vehicle Registration Number",
                    value = vehicleRegistrationNumber,
                    placeholder = "Example: ABC 1234",
                    onValueChange = { vehicleRegistrationNumber = it },
                    icon = Icons.Outlined.Badge,
                    capitalization = KeyboardCapitalization.Characters
                )

                Step2FormField(
                    label = "Make",
                    value = vehicleMake,
                    placeholder = "Example: Toyota",
                    onValueChange = { vehicleMake = it },
                    icon = Icons.Outlined.DirectionsCar,
                    capitalization = KeyboardCapitalization.Words
                )

                Step2FormField(
                    label = "Model",
                    value = vehicleModel,
                    placeholder = "Example: Corolla",
                    onValueChange = { vehicleModel = it },
                    icon = Icons.Outlined.DirectionsCar,
                    capitalization = KeyboardCapitalization.Words
                )

                Step2FormField(
                    label = "Vehicle Colour",
                    value = vehicleColour,
                    placeholder = "Example: Silver Metallic",
                    onValueChange = { vehicleColour = it },
                    icon = Icons.Outlined.Palette,
                    capitalization = KeyboardCapitalization.Words
                )

                // ─── Seating Capacity ────────────────────────────────────
                Step2CapacitySelector(
                    seatingCapacity = seatingCapacity,
                    onDecreaseClick = {
                        if (seatingCapacity > 1) seatingCapacity--
                    },
                    onIncreaseClick = {
                        if (seatingCapacity < 8) seatingCapacity++
                    }
                )
            }

            // ─── Error Message ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = null,
                                tint = StepError,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = errorMessage,
                                color = StepError,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Section Card ────────────────────────────────────────────────────────────
@Composable
private fun Step2SectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = StepCardBackground,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section header with icon badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    color = StepPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Thin divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(StepBorder)
            )

            content()
        }
    }
}

// ─── Form Field ──────────────────────────────────────────────────────────────
@Composable
private fun Step2FormField(
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = StepTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(text = placeholder, color = StepOutline)
            },
            singleLine = true,
            leadingIcon = {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(StepPrimary.copy(alpha = 0.06f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = StepPrimary.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                capitalization = capitalization
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = StepText,
                unfocusedTextColor = StepText,
                focusedPlaceholderColor = StepOutline,
                unfocusedPlaceholderColor = StepOutline,
                cursorColor = StepPrimary,
                focusedBorderColor = StepPrimary,
                unfocusedBorderColor = StepBorder,
                focusedContainerColor = StepFieldBackground,
                unfocusedContainerColor = StepFieldBackground
            )
        )
    }
}

// ─── Capacity Selector ───────────────────────────────────────────────────────
@Composable
private fun Step2CapacitySelector(
    seatingCapacity: Int,
    onDecreaseClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "SEATING CAPACITY",
            color = StepTextMuted,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(start = 4.dp)
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = StepFieldBackground,
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, StepBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(StepPrimary.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsCar,
                            contentDescription = null,
                            tint = StepPrimary.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Passengers",
                            color = StepText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Including driver's seat",
                            color = StepTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        onClick = onDecreaseClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Remove,
                                contentDescription = "Decrease",
                                tint = StepPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Text(
                        text = seatingCapacity.toString(),
                        color = StepPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 2.dp,
                        onClick = onIncreaseClick
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Outlined.Add,
                                contentDescription = "Increase",
                                tint = StepPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DriverStep2ScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        DriverStep2Screen()
    }
}
