package com.example.getyourride.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.OfferRideRequest
import com.example.getyourride.ui.theme.GetYourRideTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val OfferBackground = Color(0xFFFBF8FD)
private val OfferPrimary = Color(0xFF011844)
private val OfferTopBar = Color(0xFF1A2E5A)
private val OfferAccent = Color(0xFFFC820C)
private val OfferCardBackground = Color(0xFFFFFFFF)
private val OfferFieldBackground = Color(0xFFF5F3F7)
private val OfferText = Color(0xFF1B1B1F)
private val OfferTextMuted = Color(0xFF44464F)
private val OfferOutline = Color(0xFF757780)
private val OfferBorder = Color(0xFFC5C6D0)
private val OfferError = Color(0xFFC62828)
private val OfferSuccess = Color(0xFF2E7D32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfferRideScreen(
    onPostRideClick: (OfferRideRequest) -> Unit = {},
    errorMessage: String? = null,
    statusMessage: String? = null
) {
    val context = LocalContext.current

    var pickupLocation by rememberSaveable {
        mutableStateOf("")
    }

    var destination by rememberSaveable {
        mutableStateOf("")
    }

    var rideDate by rememberSaveable {
        mutableStateOf(currentRideDateText())
    }

    var rideTime by rememberSaveable {
        mutableStateOf(minimumRideTimeText())
    }

    var availableSeats by rememberSaveable {
        mutableStateOf(3)
    }

    var farePerSeat by rememberSaveable {
        mutableStateOf("")
    }

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
                    containerColor = OfferTopBar
                )
            )
        },
        containerColor = OfferBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(OfferBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Offer a Ride",
                    color = OfferPrimary,
                    fontSize = 24.sp,
                    lineHeight = 31.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Share your journey and split costs with fellow students.",
                    color = OfferOutline,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = OfferCardBackground,
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 2.dp,
                shadowElevation = 2.dp,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    OfferBorder.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "ROUTE DETAILS",
                        color = OfferPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.24.sp
                    )

                    OfferTextField(
                        value = pickupLocation,
                        onValueChange = { pickupLocation = it },
                        placeholder = "Pickup Location (e.g. Science Park)",
                        icon = Icons.Outlined.LocationOn
                    )

                    OfferTextField(
                        value = destination,
                        onValueChange = { destination = it },
                        placeholder = "Destination (e.g. Main Library)",
                        icon = Icons.Outlined.NearMe
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
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

                    Text(
                        text = "This ride will start at $rideTime.",
                        color = OfferTextMuted,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OfferSeatStepper(
                            seats = availableSeats,
                            onDecreaseClick = {
                                if (availableSeats > 1) {
                                    availableSeats--
                                }
                            },
                            onIncreaseClick = {
                                if (availableSeats < 7) {
                                    availableSeats++
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )

                        OfferTextField(
                            value = farePerSeat,
                            onValueChange = { farePerSeat = it },
                            placeholder = "0.00",
                            iconText = "R",
                            keyboardType = KeyboardType.Decimal,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        OfferMessageText(
                            text = errorMessage,
                            color = OfferError
                        )
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        OfferMessageText(
                            text = statusMessage,
                            color = OfferSuccess
                        )
                    }
                }
            }

            Button(
                onClick = {
                    onPostRideClick(
                        OfferRideRequest(
                            pickupLocation = pickupLocation.trim(),
                            destination = destination.trim(),
                            rideDate = rideDate.trim(),
                            rideTime = rideTime.trim(),
                            availableSeats = availableSeats,
                            farePerSeat = farePerSeat.toDoubleOrNull() ?: -1.0
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OfferAccent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.RocketLaunch,
                    contentDescription = null,
                    tint = Color.White
                )

                Spacer(
                    modifier = Modifier.width(12.dp)
                )

                Text(
                    text = "Post Ride",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun OfferTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = OfferOutline,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OfferOutline,
                    modifier = Modifier.size(22.dp)
                )
            } else if (iconText != null) {
                Text(
                    text = iconText,
                    color = OfferPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = OfferText,
            unfocusedTextColor = OfferText,
            cursorColor = OfferPrimary,
            focusedBorderColor = OfferTopBar,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = OfferFieldBackground,
            unfocusedContainerColor = OfferFieldBackground
        )
    )
}

@Composable
private fun OfferPickerField(
    value: String,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            placeholder = {
                Text(
                    text = label,
                    color = OfferOutline,
                    fontSize = 14.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = OfferOutline,
                    modifier = Modifier.size(22.dp)
                )
            },
            singleLine = true,
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = OfferText,
                unfocusedTextColor = OfferText,
                cursorColor = OfferPrimary,
                focusedBorderColor = OfferTopBar,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = OfferFieldBackground,
                unfocusedContainerColor = OfferFieldBackground
            )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    onClick()
                }
        )
    }
}

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

private fun currentRideDateText(): String {
    return formatRideDate(Calendar.getInstance())
}

private fun minimumRideTimeText(): String {
    return formatRideTime(
        Calendar.getInstance().apply {
            add(Calendar.MINUTE, 30)
        }
    )
}

private fun isRideDateTimeAllowed(
    rideDate: String,
    rideTime: String
): Boolean {
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

private fun formatRideTime(
    hourOfDay: Int,
    minute: Int
): String {
    return Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hourOfDay)
        set(Calendar.MINUTE, minute)
    }.let(::formatRideTime)
}

@Composable
private fun OfferSeatStepper(
    seats: Int,
    onDecreaseClick: () -> Unit,
    onIncreaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "SEATS AVAILABLE",
            color = OfferPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.24.sp
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(OfferFieldBackground)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onDecreaseClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Remove,
                    contentDescription = "Decrease seats",
                    tint = OfferPrimary
                )
            }

            Text(
                text = seats.toString(),
                color = OfferPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(
                onClick = onIncreaseClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Increase seats",
                    tint = OfferPrimary
                )
            }
        }
    }
}

@Composable
private fun OfferMessageText(
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
fun OfferRideScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        OfferRideScreen()
    }
}