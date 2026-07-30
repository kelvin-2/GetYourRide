package com.example.getyourride.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.outlined.AirlineSeatReclineNormal
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.OfferRideRequest
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.ui.components.StudentDriverBottomBar
import com.example.getyourride.ui.components.StudentDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.LocationFieldState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

// ─── Color Palette ───────────────────────────────────────────────────────────
private val OfferBackground = Color(0xFFF4F6FB)
private val OfferPrimary = Color(0xFF1A2E5A)
private val OfferPrimaryLight = Color(0xFF2E4A82)
private val OfferTopBarStart = Color(0xFF1A2E5A)
private val OfferTopBarEnd = Color(0xFF2E4A82)
private val OfferAccent = Color(0xFFFC820C)
private val OfferAccentLight = Color(0xFFFFA040)
private val OfferCardBackground = Color(0xFFFFFFFF)
private val OfferFieldBackground = Color(0xFFF7F8FC)
private val OfferText = Color(0xFF1B1B1F)
private val OfferTextMuted = Color(0xFF5E6278)
private val OfferOutline = Color(0xFF9CA3AF)
private val OfferBorder = Color(0xFFE5E7EB)
private val OfferError = Color(0xFFDC2626)
private val OfferSuccess = Color(0xFF16A34A)
private val OfferPendingBackground = Color(0xFFFEF3C7)
private val OfferPendingText = Color(0xFF92400E)
private val OfferDisabled = Color(0xFFD1D5DB)
private val OfferSectionIcon = Color(0xFF6366F1)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    isDriverVerified: Boolean = false,
    pickupState: LocationFieldState = LocationFieldState(),
    destinationState: LocationFieldState = LocationFieldState(),
    onPickupTextChanged: (String) -> Unit = {},
    onPickupSuggestionSelected: (AddressSuggestion) -> Unit = {},
    onDestinationTextChanged: (String) -> Unit = {},
    onDestinationSuggestionSelected: (AddressSuggestion) -> Unit = {},
    onPostRideClick: (OfferRideRequest) -> Unit = {},
    errorMessage: String? = null,
    statusMessage: String? = null,
    onHomeClick: () -> Unit = {},
    onOfferRideClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    val context = LocalContext.current

    var rideDate by rememberSaveable { mutableStateOf(currentRideDateText()) }
    var rideTime by rememberSaveable { mutableStateOf(minimumRideTimeText()) }
    var availableSeats by rememberSaveable { mutableStateOf(3) }
    var farePerSeat by rememberSaveable { mutableStateOf("") }

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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = OfferTopBarStart
                )
            )
        },
        bottomBar = {
            StudentDriverBottomBar(
                selectedItem = StudentDriverBottomBarItem.OfferRide,
                onHomeClick = onHomeClick,
                onOfferRideClick = onOfferRideClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = OfferBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // ─── Hero / Header Section with gradient ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(OfferTopBarStart, OfferPrimaryLight),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 300f)
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 28.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Route,
                                contentDescription = null,
                                tint = OfferAccent,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Offer a Ride",
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = "Share your journey, split the costs",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ─── Route Details Card ──────────────────────────────────────────
            OfferSectionCard(
                title = "Route",
                icon = Icons.Outlined.LocationOn,
                iconTint = OfferSectionIcon
            ) {
                AutocompleteOfferField(
                    label = "Pickup Location",
                    state = pickupState,
                    placeholder = "Where are you leaving from?",
                    icon = Icons.Outlined.LocationOn,
                    onTextChanged = onPickupTextChanged,
                    onSuggestionSelected = onPickupSuggestionSelected
                )

                Spacer(modifier = Modifier.height(14.dp))

                AutocompleteOfferField(
                    label = "Destination",
                    state = destinationState,
                    placeholder = "Where are you headed?",
                    icon = Icons.Outlined.NearMe,
                    onTextChanged = onDestinationTextChanged,
                    onSuggestionSelected = onDestinationSuggestionSelected
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Schedule Card ───────────────────────────────────────────────
            OfferSectionCard(
                title = "Schedule",
                icon = Icons.Outlined.CalendarToday,
                iconTint = OfferAccent
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OfferPickerField(
                        value = rideDate,
                        label = "Date",
                        icon = Icons.Outlined.CalendarToday,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showRideDatePicker(
                                context = context,
                                selectedDate = rideDate,
                                onDateSelected = { selectedDate ->
                                    rideDate = selectedDate
                                    if (!isRideDateTimeAllowed(rideDate, rideTime)) {
                                        rideTime = minimumRideTimeText()
                                    }
                                }
                            )
                        }
                    )

                    OfferPickerField(
                        value = rideTime,
                        label = "Time",
                        icon = Icons.Outlined.Schedule,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showRideTimePicker(
                                context = context,
                                selectedTime = rideTime,
                                selectedDate = rideDate,
                                onTimeSelected = { selectedTime ->
                                    rideTime = selectedTime
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(OfferFieldBackground)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = OfferAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Departing at $rideTime",
                        color = OfferTextMuted,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Seats & Fare Card ───────────────────────────────────────────
            OfferSectionCard(
                title = "Seats & Fare",
                icon = Icons.Outlined.AirlineSeatReclineNormal,
                iconTint = OfferSuccess
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    OfferSeatStepper(
                        seats = availableSeats,
                        onDecreaseClick = {
                            if (availableSeats > 1) availableSeats--
                        },
                        onIncreaseClick = {
                            if (availableSeats < 7) availableSeats++
                        },
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "FARE PER SEAT",
                            color = OfferPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        OutlinedTextField(
                            value = farePerSeat,
                            onValueChange = { farePerSeat = it },
                            placeholder = {
                                Text("0.00", color = OfferOutline, fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Payments,
                                    contentDescription = null,
                                    tint = OfferSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = offerTextFieldColors()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ─── Error / Success Messages ────────────────────────────────────
            AnimatedVisibility(
                visible = !errorMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color(0xFFFEE2E2),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = OfferError,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = !statusMessage.isNullOrBlank(),
                enter = fadeIn() + slideInVertically()
            ) {
                if (!statusMessage.isNullOrBlank()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = OfferSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage,
                                color = OfferSuccess,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // ─── Pending Verification Banner ─────────────────────────────────
            if (!isDriverVerified) {
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    color = OfferPendingBackground,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFDE68A).copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = OfferPendingText,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Verification Pending",
                                color = OfferPendingText,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "You cannot post rides until the admin approves your documents.",
                                color = OfferPendingText.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ─── Submit Button ────────────────────────────────────────────────
            val buttonScale by animateFloatAsState(
                targetValue = if (isDriverVerified) 1f else 0.97f,
                animationSpec = tween(200),
                label = "buttonScale"
            )

            Button(
                onClick = {
                    onPostRideClick(
                        OfferRideRequest(
                            pickupLocation = pickupState.text.trim(),
                            destination = destinationState.text.trim(),
                            rideDate = rideDate.trim(),
                            rideTime = rideTime.trim(),
                            availableSeats = availableSeats,
                            farePerSeat = farePerSeat.toDoubleOrNull() ?: -1.0
                        )
                    )
                },
                enabled = isDriverVerified,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(58.dp)
                    .scale(buttonScale)
                    .shadow(
                        elevation = if (isDriverVerified) 8.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = OfferAccent.copy(alpha = 0.3f)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OfferAccent,
                    disabledContainerColor = OfferDisabled
                ),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Post Your Ride",
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// ─── Reusable Section Card ───────────────────────────────────────────────────
@Composable
private fun OfferSectionCard(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        color = OfferCardBackground,
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(iconTint.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title.uppercase(),
                    color = OfferPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }
            content()
        }
    }
}

// ─── Shared TextField Colors ─────────────────────────────────────────────────
@Composable
private fun offerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = OfferText,
    unfocusedTextColor = OfferText,
    cursorColor = OfferPrimary,
    focusedBorderColor = OfferPrimary,
    unfocusedBorderColor = OfferBorder,
    focusedContainerColor = Color.White,
    unfocusedContainerColor = OfferFieldBackground
)

// ─── Autocomplete Field ──────────────────────────────────────────────────────
@Composable
private fun AutocompleteOfferField(
    label: String,
    state: LocationFieldState,
    placeholder: String,
    icon: ImageVector,
    onTextChanged: (String) -> Unit,
    onSuggestionSelected: (AddressSuggestion) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label.uppercase(),
            color = OfferPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )

        OutlinedTextField(
            value = state.text,
            onValueChange = onTextChanged,
            placeholder = {
                Text(text = placeholder, color = OfferOutline, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OfferOutline,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (state.selected != null) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = "Selected",
                        tint = OfferSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = offerTextFieldColors()
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                color = OfferAccent,
                trackColor = OfferBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
        }

        if (state.suggestions.isNotEmpty() && state.selected == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp
            ) {
                Column {
                    state.suggestions.forEach { suggestion ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSuggestionSelected(suggestion) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(OfferSectionIcon.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.LocationOn,
                                    contentDescription = null,
                                    tint = OfferSectionIcon,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = suggestion.displayName,
                                fontSize = 13.sp,
                                color = OfferText,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Date/Time Picker Field ──────────────────────────────────────────────────
@Composable
private fun OfferPickerField(
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label.uppercase(),
            color = OfferPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )
        Box(modifier = Modifier.fillMaxWidth().height(56.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                readOnly = true,
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = OfferAccent,
                        modifier = Modifier.size(20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(12.dp),
                colors = offerTextFieldColors()
            )
            // Transparent overlay to capture click
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onClick() }
            )
        }
    }
}

// ─── Seat Stepper ────────────────────────────────────────────────────────────
@Composable
private fun OfferSeatStepper(
    seats: Int,
    onDecreaseClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "AVAILABLE SEATS",
            color = OfferPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = OfferFieldBackground,
            border = BorderStroke(1.dp, OfferBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDecreaseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (seats > 1) OfferPrimary.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Remove,
                        contentDescription = "Decrease seats",
                        tint = if (seats > 1) OfferPrimary else OfferDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = seats.toString(),
                        color = OfferPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (seats == 1) "seat" else "seats",
                        color = OfferTextMuted,
                        fontSize = 11.sp
                    )
                }

                IconButton(
                    onClick = onIncreaseClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (seats < 7) OfferAccent.copy(alpha = 0.1f)
                            else Color.Transparent
                        )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "Increase seats",
                        tint = if (seats < 7) OfferAccent else OfferDisabled,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// ─── Date Picker ─────────────────────────────────────────────────────────────
private fun showRideDatePicker(
    context: android.content.Context,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val calendar = parseRideDate(selectedDate) ?: Calendar.getInstance()

    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            onDateSelected(formatRideDate(selectedCalendar))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = startOfTodayMillis()
    }.show()
}

// ─── Time Picker ─────────────────────────────────────────────────────────────
private fun showRideTimePicker(
    context: android.content.Context,
    selectedTime: String,
    selectedDate: String,
    onTimeSelected: (String) -> Unit
) {
    val calendar = parseRideTime(selectedTime) ?: Calendar.getInstance().apply {
        add(Calendar.MINUTE, 30)
    }

    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val selectedTimeText = formatRideTime(hourOfDay, minute)
            onTimeSelected(
                if (isRideDateTimeAllowed(selectedDate, selectedTimeText)) {
                    selectedTimeText
                } else {
                    minimumRideTimeText()
                }
            )
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        true
    ).show()
}

// ─── Date/Time Helpers ───────────────────────────────────────────────────────
private fun currentRideDateText(): String {
    return formatRideDate(Calendar.getInstance())
}

private fun minimumRideTimeText(): String {
    return formatRideTime(
        Calendar.getInstance().apply { add(Calendar.MINUTE, 30) }
    )
}

private fun isRideDateTimeAllowed(rideDate: String, rideTime: String): Boolean {
    val selectedDate = parseRideDate(rideDate) ?: return false
    val selectedTime = parseRideTime(rideTime) ?: return false

    val selectedDateTime = Calendar.getInstance().apply {
        time = selectedDate.time
        set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
        set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val earliestAllowed = Calendar.getInstance().apply {
        add(Calendar.MINUTE, 30)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return !selectedDateTime.before(earliestAllowed)
}

private fun startOfTodayMillis(): Long {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

private fun parseRideDate(date: String): Calendar? {
    return runCatching {
        Calendar.getInstance().apply {
            time = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date)
                ?: return null
        }
    }.getOrNull()
}

private fun parseRideTime(time: String): Calendar? {
    return runCatching {
        Calendar.getInstance().apply {
            this.time = SimpleDateFormat("HH:mm", Locale.getDefault()).parse(time)
                ?: return null
        }
    }.getOrNull()
}

private fun formatRideDate(calendar: Calendar): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
}

private fun formatRideTime(calendar: Calendar): String {
    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
}

private fun formatRideTime(hourOfDay: Int, minute: Int): String {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
    }.let(::formatRideTime)
}

// ─── Preview ─────────────────────────────────────────────────────────────────
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OfferRideScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        OfferRideScreen(isDriverVerified = true)
    }
}
