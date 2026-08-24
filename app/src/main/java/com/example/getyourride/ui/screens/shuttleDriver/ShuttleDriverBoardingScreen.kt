package com.example.getyourride.ui.screens.shuttleDriver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.remote.dto.BoardedStudentResponse
import com.example.getyourride.data.remote.dto.ShuttleDriverActiveTripResponse
import com.example.getyourride.ui.components.ShuttleDriverBottomBar
import com.example.getyourride.ui.components.ShuttleDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme
import com.example.getyourride.viewmodel.BoardingUiState
import com.example.getyourride.viewmodel.STANDARD_TIME_SLOTS
import com.example.getyourride.viewmodel.TimeSlot

// ── Design tokens (matching profile screen) ─────────────────────────────────
private val BoardingBackground = Color(0xFFF6F8FC)
private val BoardingPrimary = Color(0xFF0D1B4A)
private val BoardingAccent = Color(0xFFFC820C)
private val BoardingTopBarStart = Color(0xFF0D1B4A)
private val BoardingTopBarEnd = Color(0xFF1A3A7A)
private val BoardingCardBg = Color(0xFFFFFFFF)
private val BoardingText = Color(0xFF1B1B1F)
private val BoardingTextMuted = Color(0xFF5E6278)
private val BoardingIconBg = Color(0xFFEDF1FA)
private val BoardingDivider = Color(0xFFE8EBF0)
private val BoardingFieldBg = Color(0xFFF2F4F8)
private val BoardingSuccessBg = Color(0xFFE8F5E9)
private val BoardingSuccessText = Color(0xFF2E7D32)
private val BoardingWarningBg = Color(0xFFFFF8E1)
private val BoardingWarningText = Color(0xFFE65100)
private val BoardingInfoBg = Color(0xFFE8F4FD)
private val BoardingInfoText = Color(0xFF1565C0)
private val BoardingErrorBg = Color(0xFFFFEBEE)
private val BoardingErrorText = Color(0xFFC62828)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleDriverBoardingScreen(
    uiState: BoardingUiState = BoardingUiState.Loading,
    markingBookingId: Long? = null,
    onLoadData: () -> Unit = {},
    onMarkAsBoarded: (Long) -> Unit = {},
    onSelectTimeSlot: (TimeSlot) -> Unit = {},
    onScanQrCodeClick: () -> Unit = {},
    onBoardingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    LaunchedEffect(Unit) {
        onLoadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DirectionsBus,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
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
                    containerColor = BoardingTopBarStart
                )
            )
        },
        bottomBar = {
            ShuttleDriverBottomBar(
                selectedItem = ShuttleDriverBottomBarItem.Boarding,
                onScanQrCodeClick = onScanQrCodeClick,
                onBoardingClick = onBoardingClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = BoardingBackground
    ) { innerPadding ->

        when (uiState) {
            is BoardingUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = BoardingAccent,
                            strokeWidth = 3.dp
                        )
                        Text(
                            text = "Loading boarding data...",
                            color = BoardingTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is BoardingUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BoardingErrorBg,
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = BoardingErrorText,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Text(
                            text = uiState.message,
                            color = BoardingErrorText,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onLoadData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BoardingPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            is BoardingUiState.NoTrip -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BoardingInfoBg,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Outlined.EventBusy,
                                    contentDescription = null,
                                    tint = BoardingInfoText,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Text(
                            text = "No Active Trip",
                            color = BoardingPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = uiState.message,
                            color = BoardingTextMuted,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = onLoadData,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BoardingPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Refresh", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            is BoardingUiState.Success -> {
                BoardingContent(
                    trip = uiState.trip,
                    students = uiState.students,
                    timeSlots = uiState.timeSlots,
                    selectedSlot = uiState.selectedSlot,
                    markingBookingId = markingBookingId,
                    onMarkAsBoarded = onMarkAsBoarded,
                    onSelectTimeSlot = onSelectTimeSlot,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}


// ── Main Content ────────────────────────────────────────────────────────────

@Composable
private fun BoardingContent(
    trip: ShuttleDriverActiveTripResponse,
    students: List<BoardedStudentResponse>,
    timeSlots: List<TimeSlot>,
    selectedSlot: TimeSlot,
    markingBookingId: Long?,
    onMarkAsBoarded: (Long) -> Unit,
    onSelectTimeSlot: (TimeSlot) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by rememberSaveable { mutableStateOf("") }

    val filteredStudents = students.filter { student ->
        val query = searchText.trim()
        query.isBlank() ||
                student.studentNumber.contains(query, ignoreCase = true) ||
                student.firstName.contains(query, ignoreCase = true) ||
                student.lastName.contains(query, ignoreCase = true)
    }

    val boardedCount = students.count { it.boardedAt != null }
    val pendingCount = students.count { it.boardedAt == null }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ── Page Header ─────────────────────────────────────────────────
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Boarding",
                color = BoardingPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Manage student boarding for your active trip.",
                color = BoardingTextMuted,
                fontSize = 14.sp
            )
        }

        // ── Time Slot Filter ────────────────────────────────────────────
        TimeSlotFilterRow(
            timeSlots = timeSlots,
            selectedSlot = selectedSlot,
            onSelectSlot = onSelectTimeSlot
        )

        // ── Trip Header Card (gradient like profile) ────────────────────
        TripHeaderCard(trip = trip, boardedCount = boardedCount)

        // ── Stats Row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BoardingStatCard(
                label = "Total Booked",
                value = students.size.toString(),
                color = BoardingInfoText,
                bgColor = BoardingInfoBg,
                modifier = Modifier.weight(1f)
            )
            BoardingStatCard(
                label = "Boarded",
                value = boardedCount.toString(),
                color = BoardingSuccessText,
                bgColor = BoardingSuccessBg,
                modifier = Modifier.weight(1f)
            )
            BoardingStatCard(
                label = "Pending",
                value = pendingCount.toString(),
                color = BoardingWarningText,
                bgColor = BoardingWarningBg,
                modifier = Modifier.weight(1f)
            )
        }

        // ── Student List Section ────────────────────────────────────────
        BoardingSectionCard(
            title = "Booked Students",
            icon = Icons.Outlined.Groups
        ) {
            // Search bar
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = {
                    Text(
                        text = "Search by name or student number",
                        color = BoardingTextMuted,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = BoardingTextMuted
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = BoardingText,
                    unfocusedTextColor = BoardingText,
                    cursorColor = BoardingPrimary,
                    focusedBorderColor = BoardingPrimary,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = BoardingFieldBg,
                    unfocusedContainerColor = BoardingFieldBg
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (filteredStudents.isEmpty()) {
                EmptySearchState()
            } else {
                filteredStudents.forEach { student ->
                    StudentBoardingCard(
                        student = student,
                        isMarking = markingBookingId == student.bookingId,
                        onMarkAsBoarded = { onMarkAsBoarded(student.bookingId) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}


// ── Time Slot Filter Row ────────────────────────────────────────────────────

@Composable
private fun TimeSlotFilterRow(
    timeSlots: List<TimeSlot>,
    selectedSlot: TimeSlot,
    onSelectSlot: (TimeSlot) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BoardingCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = BoardingPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Trip Schedule",
                    color = BoardingPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Morning slots
            Text(
                text = "Morning",
                color = BoardingTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.filter { it.period == "Morning" }.forEach { slot ->
                    TimeSlotChip(
                        slot = slot,
                        isSelected = slot.slotId == selectedSlot.slotId,
                        onClick = { onSelectSlot(slot) }
                    )
                }
            }

            // Afternoon slots
            Text(
                text = "Afternoon",
                color = BoardingTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                timeSlots.filter { it.period == "Afternoon" }.forEach { slot ->
                    TimeSlotChip(
                        slot = slot,
                        isSelected = slot.slotId == selectedSlot.slotId,
                        onClick = { onSelectSlot(slot) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeSlotChip(
    slot: TimeSlot,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) BoardingPrimary else BoardingFieldBg
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = slot.label,
                color = if (isSelected) Color.White else BoardingText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = slot.arrives.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
                color = if (isSelected) Color.White.copy(alpha = 0.7f) else BoardingTextMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}


// ── Trip Header Card ────────────────────────────────────────────────────────

@Composable
private fun TripHeaderCard(
    trip: ShuttleDriverActiveTripResponse,
    boardedCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(BoardingTopBarStart, BoardingTopBarEnd)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Route and status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "ACTIVE TRIP",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${trip.departureStop} \u2192 ${trip.destinationStop}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            lineHeight = 24.sp
                        )
                    }
                    TripStatusChip(status = trip.status)
                }

                // Info boxes
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TripMetricBox(
                        label = "Departure",
                        value = trip.departureTime,
                        modifier = Modifier.weight(1f)
                    )
                    TripMetricBox(
                        label = "Boarded",
                        value = "$boardedCount/${trip.capacity}",
                        modifier = Modifier.weight(1f)
                    )
                    TripMetricBox(
                        label = "Vehicle",
                        value = trip.registrationNumber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TripStatusChip(status: String) {
    val (bg, text) = when (status.uppercase()) {
        "IN_PROGRESS", "IN PROGRESS" -> BoardingWarningBg to BoardingWarningText
        "SCHEDULED" -> BoardingInfoBg to BoardingInfoText
        "COMPLETED" -> BoardingSuccessBg to BoardingSuccessText
        else -> BoardingInfoBg to BoardingInfoText
    }
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg.copy(alpha = 0.9f)
    ) {
        Text(
            text = status.uppercase().replace("_", " "),
            color = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun TripMetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ── Stat Card ───────────────────────────────────────────────────────────────

@Composable
private fun BoardingStatCard(
    label: String,
    value: String,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                color = color,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = color.copy(alpha = 0.75f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Section Card (same pattern as profile) ──────────────────────────────────

@Composable
private fun BoardingSectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = BoardingCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BoardingIconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = BoardingPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Text(
                    text = title,
                    color = BoardingPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BoardingDivider)
            )

            content()
        }
    }
}


// ── Student Boarding Card ───────────────────────────────────────────────────

@Composable
private fun StudentBoardingCard(
    student: BoardedStudentResponse,
    isMarking: Boolean,
    onMarkAsBoarded: () -> Unit
) {
    val isBoarded = student.boardedAt != null

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isBoarded) BoardingSuccessBg.copy(alpha = 0.3f) else BoardingFieldBg,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Avatar with initials
                Surface(
                    shape = CircleShape,
                    color = if (isBoarded) BoardingSuccessBg else BoardingIconBg,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isBoarded) {
                            Icon(
                                imageVector = Icons.Outlined.CheckCircle,
                                contentDescription = null,
                                tint = BoardingSuccessText,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "${student.firstName.firstOrNull() ?: ""}${student.lastName.firstOrNull() ?: ""}",
                                color = BoardingPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Student info
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        color = BoardingText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = student.studentNumber,
                        color = BoardingTextMuted,
                        fontSize = 12.sp
                    )
                }

                // Status chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isBoarded) BoardingSuccessBg else BoardingWarningBg
                ) {
                    Text(
                        text = if (isBoarded) "BOARDED" else "PENDING",
                        color = if (isBoarded) BoardingSuccessText else BoardingWarningText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Mark as Boarded button (only for pending students)
            if (!isBoarded) {
                Button(
                    onClick = onMarkAsBoarded,
                    enabled = !isMarking,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BoardingAccent,
                        contentColor = Color.White,
                        disabledContainerColor = BoardingAccent.copy(alpha = 0.5f)
                    ),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    if (isMarking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Marking...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.FactCheck,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mark as Boarded",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Boarded confirmation row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = BoardingSuccessText,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Boarded at ${student.boardedAt}",
                        color = BoardingSuccessText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// ── Empty State ─────────────────────────────────────────────────────────────

@Composable
private fun EmptySearchState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BoardingFieldBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = BoardingTextMuted,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = "No students found",
                color = BoardingTextMuted,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Try a different search term.",
                color = BoardingTextMuted.copy(alpha = 0.7f),
                fontSize = 12.sp
            )
        }
    }
}


// ── Preview ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ShuttleDriverBoardingScreenPreview() {
    val sampleTrip = ShuttleDriverActiveTripResponse(
        tripId = 24,
        departureStop = "North Campus",
        destinationStop = "South Campus",
        departureTime = "12:30 - 13:15",
        arrivalTime = "13:15",
        status = "IN_PROGRESS",
        capacity = 15,
        registrationNumber = "NMU001EC",
        totalBooked = 5,
        totalBoarded = 2
    )

    val sampleStudents = listOf(
        BoardedStudentResponse(1, 101, "Kevin", "De Bruyne", "229875460", "Confirmed", null),
        BoardedStudentResponse(2, 102, "Elena", "Rodriguez", "240968674", "Confirmed", null),
        BoardedStudentResponse(3, 103, "Jordan", "Smith", "224958672", "Confirmed", "12:35"),
        BoardedStudentResponse(4, 104, "Rafael", "Leao", "22489852", "Confirmed", "12:37"),
        BoardedStudentResponse(5, 105, "Pedro", "Neto", "209865642", "Confirmed", null)
    )

    GetYourRideTheme(dynamicColor = false) {
        ShuttleDriverBoardingScreen(
            uiState = BoardingUiState.Success(
                trip = sampleTrip,
                students = sampleStudents,
                timeSlots = STANDARD_TIME_SLOTS,
                selectedSlot = STANDARD_TIME_SLOTS[4], // 12:30 slot
                selectedDate = java.time.LocalDate.now(),
                driverTrips = emptyList()
            )
        )
    }
}
