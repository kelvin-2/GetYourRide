package com.example.getyourride.ui.screens.shuttleDriver

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.ShuttleDriverBottomBar
import com.example.getyourride.ui.components.ShuttleDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme

/*
 * ShuttleDriverBoardingScreen
 *
 * This is the Boarding page for the shuttle driver.
 *
 * Database tables this screen will later use:
 * - trip
 * - vehicle
 * - trip_booking
 * - student
 * - boarding_log
 * - shuttle_stop
 *
 * Important:
 * We do not create a new "boarding status" column in the database.
 * The frontend status is calculated from boarding_log.boarded_at:
 *
 * boarded_at == null     -> Pending
 * boarded_at is not null -> Boarded
 */

// App colours matching the existing GetYourRide style.
private val ShuttleBackground = Color(0xFFFBF8FD)
private val ShuttlePrimary = Color(0xFF011844)
private val ShuttleTopBar = Color(0xFF1A2E5A)
private val ShuttleAccent = Color(0xFFFC820C)
private val ShuttleCardBackground = Color(0xFFFFFFFF)
private val ShuttleFieldBackground = Color(0xFFF5F3F7)
private val ShuttleText = Color(0xFF1B1B1F)
private val ShuttleTextMuted = Color(0xFF44464F)
private val ShuttleOutline = Color(0xFF757780)
private val ShuttleBorder = Color(0xFFE3E2E6)
private val ShuttlePrimaryFixed = Color(0xFFDAE2FF)
private val ShuttlePendingBackground = Color(0xFFFFF3CD)
private val ShuttlePendingText = Color(0xFF8A5A00)
private val ShuttleBoardedBackground = Color(0xFFE8F5E9)
private val ShuttleBoardedText = Color(0xFF2E7D32)

/*
 * Current shuttle trip shown at the top of the Boarding page.
 *
 * Database source later:
 * - trip.trip_id
 * - trip.departure_time
 * - trip.arrival_time
 * - trip.status
 * - shuttle_stop.stop_name for departure and destination names
 * - vehicle.capacity
 */
@Immutable
data class ShuttleBoardingTrip(
    val tripId: Long,
    val departureStopName: String,
    val destinationStopName: String,
    val departureTime: String,
    val arrivalTime: String,
    val capacity: Int,
    val tripStatus: String
)

/*
 * One booked student for this shuttle trip.
 *
 * Database source later:
 * - trip_booking.booking_id
 * - trip_booking.booking_status
 * - student.student_id
 * - student.first_name
 * - student.last_name
 * - student.student_number
 * - boarding_log.boarded_at
 */
@Immutable
data class ShuttleBoardingStudent(
    val bookingId: Long,
    val studentId: Long,
    val firstName: String,
    val lastName: String,
    val studentNumber: String,
    val bookingStatus: String,
    val boardedAt: String?
)

/*
 * Helper value used only by the UI.
 *
 * This is not a database column.
 */
private enum class BoardingDisplayStatus(
    val label: String
) {
    Pending("Pending"),
    Boarded("Boarded")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleDriverBoardingScreen(
    trip: ShuttleBoardingTrip = sampleBoardingTrip(),
    students: List<ShuttleBoardingStudent> = sampleBoardingStudents(),
    onMarkAsBoardedClick: (Long) -> Unit = {},
    onScanQrCodeClick: () -> Unit = {},
    onBoardingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    /*
     * Local state for now.
     *
     * Later, this list will come from the backend.
     * When the driver marks a student as boarded, the backend should update
     * boarding_log.boarded_at using the student's booking_id.
     */
    var bookedStudents by remember {
        mutableStateOf(students)
    }

    var searchText by rememberSaveable {
        mutableStateOf("")
    }

    val totalBookedStudents = bookedStudents.size

    val boardedStudentsCount = bookedStudents.count { student ->
        student.boardedAt != null
    }

    /*
     * The user asked to search by student number.
     * We also allow name/surname search to make testing easier.
     */
    val filteredStudents = bookedStudents.filter { student ->
        val query = searchText.trim()

        query.isBlank() ||
                student.studentNumber.contains(query, ignoreCase = true) ||
                student.firstName.contains(query, ignoreCase = true) ||
                student.lastName.contains(query, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            /*
             * Same top bar style used on Student Driver pages.
             */
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
                    containerColor = ShuttleTopBar
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
        containerColor = ShuttleBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ShuttleBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text(
                text = "Boarding",
                color = ShuttlePrimary,
                fontSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold
            )

            CurrentTripCard(
                trip = trip,
                boardedStudentsCount = boardedStudentsCount
            )

            StudentListHeader(
                totalBookedStudents = totalBookedStudents
            )

            StudentSearchBar(
                searchText = searchText,
                onSearchTextChange = { searchText = it }
            )

            if (filteredStudents.isEmpty()) {
                EmptyStudentSearchCard()
            } else {
                filteredStudents.forEach { student ->
                    ShuttleStudentBoardingCard(
                        student = student,
                        onMarkAsBoardedClick = {
                            /*
                             * Frontend-only update for now.
                             *
                             * Later:
                             * onMarkAsBoardedClick(student.bookingId) should call backend.
                             * Backend should update boarding_log.boarded_at.
                             */
                            onMarkAsBoardedClick(student.bookingId)

                            bookedStudents = bookedStudents.map { currentStudent ->
                                if (currentStudent.bookingId == student.bookingId) {
                                    currentStudent.copy(
                                        boardedAt = "Now"
                                    )
                                } else {
                                    currentStudent
                                }
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/*
 * Shows the current shuttle trip and trip statistics.
 */
@Composable
private fun CurrentTripCard(
    trip: ShuttleBoardingTrip,
    boardedStudentsCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShuttleCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
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
                        text = "CURRENT TRIP",
                        color = ShuttleTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.4.sp
                    )

                    Text(
                        text = "${trip.departureStopName} → ${trip.destinationStopName}",
                        color = ShuttlePrimary,
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TripStatusBadge(
                    status = trip.tripStatus
                )
            }

            DividerLine()

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TripInfoBox(
                    label = "Departure",
                    value = trip.departureTime,
                    modifier = Modifier.weight(1f)
                )

                TripInfoBox(
                    label = "Arrival",
                    value = trip.arrivalTime,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                TripInfoBox(
                    label = "Boarded",
                    value = boardedStudentsCount.toString(),
                    modifier = Modifier.weight(1f)
                )

                TripInfoBox(
                    label = "Capacity",
                    value = trip.capacity.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/*
 * Badge for trip.status.
 */
@Composable
private fun TripStatusBadge(
    status: String
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(ShuttlePendingBackground)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.uppercase(),
            color = ShuttlePendingText,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/*
 * Small box used for departure, arrival, boarded count, and capacity.
 */
@Composable
private fun TripInfoBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ShuttleFieldBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = ShuttleTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            color = ShuttlePrimary,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/*
 * Header above the booked student list.
 */
@Composable
private fun StudentListHeader(
    totalBookedStudents: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                text = "List of Students",
                color = ShuttlePrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Students who booked this shuttle.",
                color = ShuttleTextMuted,
                fontSize = 13.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(ShuttlePrimary)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "$totalBookedStudents Total",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/*
 * Search field for finding a booked student.
 */
@Composable
private fun StudentSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit
) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        placeholder = {
            Text(
                text = "Search by student number",
                color = ShuttleOutline,
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                tint = ShuttleOutline
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = ShuttleText,
            unfocusedTextColor = ShuttleText,
            cursorColor = ShuttlePrimary,
            focusedBorderColor = ShuttleTopBar,
            unfocusedBorderColor = Color.Transparent,
            focusedContainerColor = ShuttleFieldBackground,
            unfocusedContainerColor = ShuttleFieldBackground
        )
    )
}

/*
 * Card for one booked student.
 */
@Composable
private fun ShuttleStudentBoardingCard(
    student: ShuttleBoardingStudent,
    onMarkAsBoardedClick: () -> Unit
) {
    val displayStatus = if (student.boardedAt == null) {
        BoardingDisplayStatus.Pending
    } else {
        BoardingDisplayStatus.Boarded
    }

    val isBoarded = displayStatus == BoardingDisplayStatus.Boarded

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShuttleCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StudentAvatar()

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = "${student.firstName} ${student.lastName}",
                        color = ShuttleText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Student No: ${student.studentNumber}",
                        color = ShuttleTextMuted,
                        fontSize = 12.sp
                    )
                }

                StudentBoardingStatusBadge(
                    status = displayStatus
                )
            }

            if (!isBoarded) {
                Button(
                    onClick = onMarkAsBoardedClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ShuttleAccent,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "Mark as Boarded",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = ShuttleBoardedText,
                        modifier = Modifier.size(20.dp)
                    )

                    Text(
                        text = "Student has been marked as boarded.",
                        color = ShuttleBoardedText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/*
 * Simple student icon/avatar.
 */
@Composable
private fun StudentAvatar() {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(ShuttlePrimaryFixed),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Person,
            contentDescription = null,
            tint = ShuttlePrimary,
            modifier = Modifier.size(24.dp)
        )
    }
}

/*
 * Status badge shown on each student card.
 */
@Composable
private fun StudentBoardingStatusBadge(
    status: BoardingDisplayStatus
) {
    val backgroundColor = when (status) {
        BoardingDisplayStatus.Pending -> ShuttlePendingBackground
        BoardingDisplayStatus.Boarded -> ShuttleBoardedBackground
    }

    val textColor = when (status) {
        BoardingDisplayStatus.Pending -> ShuttlePendingText
        BoardingDisplayStatus.Boarded -> ShuttleBoardedText
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = status.label.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/*
 * Empty state shown when no student matches the search.
 */
@Composable
private fun EmptyStudentSearchCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShuttleCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.VerifiedUser,
                contentDescription = null,
                tint = ShuttleTextMuted,
                modifier = Modifier.size(24.dp)
            )

            Text(
                text = "No booked student found for that search.",
                color = ShuttleTextMuted,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

/*
 * Small divider used inside cards.
 */
@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ShuttleBorder)
    )
}

/*
 * Sample current trip for preview/testing.
 *
 * Later this should come from:
 * trip + vehicle + shuttle_stop.
 */
private fun sampleBoardingTrip(): ShuttleBoardingTrip {
    return ShuttleBoardingTrip(
        tripId = 1L,
        departureStopName = "South Campus",
        destinationStopName = "Central",
        departureTime = "12:30",
        arrivalTime = "13:10",
        capacity = 16,
        tripStatus = "In Progress"
    )
}

/*
 * Sample booked students for preview/testing.
 *
 * boardedAt = null means Pending.
 * boardedAt has a value means Boarded.
 */
private fun sampleBoardingStudents(): List<ShuttleBoardingStudent> {
    return listOf(
        ShuttleBoardingStudent(
            bookingId = 1L,
            studentId = 101L,
            firstName = "Kevin",
            lastName = "De Bruyne",
            studentNumber = "229875460",
            bookingStatus = "Confirmed",
            boardedAt = null
        ),
        ShuttleBoardingStudent(
            bookingId = 6L,
            studentId = 109L,
            firstName = "Cristiano",
            lastName = "Ronaldo",
            studentNumber = "226988957",
            bookingStatus = "Confirmed",
            boardedAt = null
        ),
        ShuttleBoardingStudent(
            bookingId = 2L,
            studentId = 102L,
            firstName = "Elena",
            lastName = "Rodriguez",
            studentNumber = "240968674",
            bookingStatus = "Confirmed",
            boardedAt = null
        ),
        ShuttleBoardingStudent(
            bookingId = 3L,
            studentId = 103L,
            firstName = "Jordan",
            lastName = "Smith",
            studentNumber = "224958672",
            bookingStatus = "Confirmed",
            boardedAt = "2026-07-02 07:25"
        ),
        ShuttleBoardingStudent(
            bookingId = 4L,
            studentId = 104L,
            firstName = "Rafael",
            lastName = "Leao",
            studentNumber = "22489852",
            bookingStatus = "Confirmed",
            boardedAt = "2026-07-02 07:25"
        ),
        ShuttleBoardingStudent(
            bookingId = 6L,
            studentId = 105L,
            firstName = "Pedro",
            lastName = "Neto",
            studentNumber = "209865642",
            bookingStatus = "Confirmed",
            boardedAt = null
        )
    )
}

/*
 * Android Studio preview.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShuttleDriverBoardingScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        ShuttleDriverBoardingScreen()
    }
}