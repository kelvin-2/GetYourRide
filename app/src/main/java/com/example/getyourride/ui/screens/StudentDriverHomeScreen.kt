package com.example.getyourride.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalTaxi
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.getyourride.ui.theme.GetYourRideTheme
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import com.example.getyourride.ui.screens.StudentDriverHomeScreen
import com.example.getyourride.ui.screens.StudentDriverPostedRide
import com.example.getyourride.ui.screens.RideAcceptedStudent

/*
 * Student Driver Home Screen
 *
 * This screen is shown after a student driver signs up or logs in.
 * It gives the driver quick access to:
 * 1. Home dashboard
 * 2. Offer Ride screen
 * 3. Profile / verification status screen
 *
 * The posted rides section shows rides created by the student driver.
 * It also shows students who accepted each ride.
 */

// App colours used by the student driver screens.
private val DriverBackground = Color(0xFFFBF8FD)
private val DriverPrimary = Color(0xFF011844)
private val DriverTopBar = Color(0xFF1A2E5A)
private val DriverAccent = Color(0xFFFC820C)
private val DriverCardBackground = Color(0xFFFFFFFF)
private val DriverText = Color(0xFF1B1B1F)
private val DriverTextMuted = Color(0xFF44464F)
private val DriverPendingBackground = Color(0xFFFFF3CD)
private val DriverPendingText = Color(0xFF8A5A00)
private val DriverPrimaryFixed = Color(0xFFDAE2FF)
private val DriverSuccessBackground = Color(0xFFE8F5E9)
private val DriverSuccessText = Color(0xFF2E7D32)
private val DriverSoftBorder = Color(0xFFE3E2E6)
private val DriverWarningBackground = Color(0xFFFFF3CD)
private val DriverWarningText = Color(0xFF8A5A00)

/*
 * Represents one ride posted by the student driver.
 *
 * availableSeats means the number of seats the driver originally offered.
 * The remaining seats are calculated by subtracting acceptedStudents.size.
 *
 * Example:
 * availableSeats = 3
 * acceptedStudents.size = 2
 * remaining seats = 1
 */
@Immutable
data class StudentDriverPostedRide(
    val rideId: String,
    val pickupLocation: String,
    val destination: String,
    val date: String,
    val time: String,
    val availableSeats: Int,
    val farePerSeat: String,
    val acceptedStudents: List<RideAcceptedStudent>
)

/*
 * Represents a student who accepted/booked the posted ride.
 */
@Immutable
data class RideAcceptedStudent(
    val name: String,
    val studentNumber: String,
    val status: String = "Confirmed"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDriverHomeScreen(
    driverName: String = "Student Driver",
    verificationStatus: String = "Pending Verification",
    postedRides: List<StudentDriverPostedRide> = emptyList(),
    isRefreshing: Boolean = false,
    onRefreshClick: () -> Unit = {},
    onHomeClick: () -> Unit = {},
    onOfferRideClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            /*
             * Shared-style top bar for student driver screens.
             *
             * The refresh button allows the driver to reload the latest:
             * - verification status
             * - posted rides
             * - accepted students
             * - remaining seats
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
                actions = {
                    IconButton(
                        onClick = onRefreshClick,
                        enabled = !isRefreshing
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh,
                            contentDescription = "Refresh home page",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DriverTopBar
                )
            )
        },
        bottomBar = {
            StudentDriverBottomBar(
                selectedItem = StudentDriverBottomBarItem.Home,
                onHomeClick = onHomeClick,
                onOfferRideClick = onOfferRideClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = DriverBackground
    ) { innerPadding ->

        // The main content scrolls so posted ride cards do not get hidden on small screens.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DriverBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Welcome back,",
                color = DriverTextMuted,
                fontSize = 16.sp
            )

            Text(
                text = driverName,
                color = DriverPrimary,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                fontWeight = FontWeight.Bold
            )

            VerificationStatusCard(
                status = verificationStatus
            )
            /*
            * Small loading row shown when the Home page is refreshing.
            * Later this will be shown while the app is getting fresh data from Spring Boot.
            */
            if (isRefreshing) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = DriverPrimary
                    )

                    Text(
                        text = "Refreshing latest driver dashboard...",
                        color = DriverTextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            StudentDriverInfoCard(
                icon = Icons.Outlined.DirectionsCar,
                title = "Student Driver Dashboard",
                description = "Manage your driver account, check your verification status, and view rides you have posted."
            )

            PostedRidesSection(
                postedRides = postedRides,
                onOfferRideClick = onOfferRideClick
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun PostedRidesSection(
    postedRides: List<StudentDriverPostedRide>,
    onOfferRideClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Posted Rides",
            color = DriverPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "See the rides you have offered and the students who accepted them.",
            color = DriverTextMuted,
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        if (postedRides.isEmpty()) {
            EmptyPostedRidesCard(
                onOfferRideClick = onOfferRideClick
            )
        } else {
            postedRides.forEach { ride ->
                PostedRideCard(
                    ride = ride
                )
            }
        }
    }
}

@Composable
private fun EmptyPostedRidesCard(
    onOfferRideClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DriverPrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalTaxi,
                        contentDescription = null,
                        tint = DriverPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "No rides posted yet",
                        color = DriverText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Once you offer a ride, it will appear here with accepted students.",
                        color = DriverTextMuted,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Button(
                onClick = onOfferRideClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DriverAccent
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text(
                    text = "Offer a Ride",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun PostedRideCard(
    ride: StudentDriverPostedRide
) {
    /*
     * Seat calculation:
     * Every accepted student takes one seat.
     * Remaining seats cannot go below 0.
     */
    val acceptedCount = ride.acceptedStudents.size
    val remainingSeats = (ride.availableSeats - acceptedCount).coerceAtLeast(0)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DriverPrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DirectionsCar,
                        contentDescription = null,
                        tint = DriverPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${ride.pickupLocation} → ${ride.destination}",
                        color = DriverText,
                        fontSize = 17.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "${ride.date} at ${ride.time}",
                        color = DriverTextMuted,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "${ride.farePerSeat} per seat",
                        color = DriverTextMuted,
                        fontSize = 14.sp
                    )
                }
            }

            RideSeatSummaryRow(
                totalSeats = ride.availableSeats,
                remainingSeats = remainingSeats,
                acceptedCount = acceptedCount
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(DriverSoftBorder)
            )

            Text(
                text = "Accepted Students",
                color = DriverPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            if (ride.acceptedStudents.isEmpty()) {
                Text(
                    text = "No students have accepted this ride yet.",
                    color = DriverTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ride.acceptedStudents.forEach { student ->
                        AcceptedStudentRow(
                            student = student
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RideSeatSummaryRow(
    totalSeats: Int,
    remainingSeats: Int,
    acceptedCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SeatInfoChip(
            text = "$remainingSeats left",
            backgroundColor = if (remainingSeats == 0) DriverWarningBackground else DriverSuccessBackground,
            textColor = if (remainingSeats == 0) DriverWarningText else DriverSuccessText,
            modifier = Modifier.weight(1f)
        )

        SeatInfoChip(
            text = "$acceptedCount accepted",
            backgroundColor = DriverPrimaryFixed,
            textColor = DriverPrimary,
            modifier = Modifier.weight(1f)
        )

        SeatInfoChip(
            text = "$totalSeats total",
            backgroundColor = DriverBackground,
            textColor = DriverTextMuted,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SeatInfoChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AcceptedStudentRow(
    student: RideAcceptedStudent
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DriverBackground)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(DriverPrimaryFixed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = DriverPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = student.name,
                color = DriverText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Student No: ${student.studentNumber}",
                color = DriverTextMuted,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(DriverSuccessBackground)
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = student.status,
                color = DriverSuccessText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun VerificationStatusCard(
    status: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(DriverPendingBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.VerifiedUser,
                    contentDescription = null,
                    tint = DriverPendingText,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Verification Status",
                    color = DriverTextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = status,
                    color = DriverPendingText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StudentDriverInfoCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = DriverCardBackground
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DriverPrimaryFixed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DriverPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = DriverText,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = description,
                    color = DriverTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

private enum class StudentDriverBottomBarItem {
    Home,
    OfferRide,
    Profile
}

@Composable
private fun StudentDriverBottomBar(
    selectedItem: StudentDriverBottomBarItem,
    onHomeClick: () -> Unit,
    onOfferRideClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        NavigationBar(
            containerColor = Color.White,
            tonalElevation = 0.dp
        ) {
            NavigationBarItem(
                selected = selectedItem == StudentDriverBottomBarItem.Home,
                onClick = onHomeClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Home,
                        contentDescription = "Home"
                    )
                },
                label = {
                    Text(text = "Home")
                },
                colors = studentDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == StudentDriverBottomBarItem.OfferRide,
                onClick = onOfferRideClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.LocalTaxi,
                        contentDescription = "Offer Ride"
                    )
                },
                label = {
                    Text(text = "Offer Ride")
                },
                colors = studentDriverNavigationColors()
            )

            NavigationBarItem(
                selected = selectedItem == StudentDriverBottomBarItem.Profile,
                onClick = onProfileClick,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Person,
                        contentDescription = "Profile"
                    )
                },
                label = {
                    Text(text = "Profile")
                },
                colors = studentDriverNavigationColors()
            )
        }
    }
}

@Composable
private fun studentDriverNavigationColors() =
    NavigationBarItemDefaults.colors(
        selectedIconColor = DriverPrimary,
        selectedTextColor = DriverPrimary,
        indicatorColor = DriverPrimaryFixed,
        unselectedIconColor = DriverTextMuted,
        unselectedTextColor = DriverTextMuted
    )
/*
 * Preview used only by Android Studio.
 *
 * This does not run in the real app.
 * It helps us see how the screen looks while designing the UI.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun StudentDriverHomeScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        StudentDriverHomeScreen(
            driverName = "Ayabulela",
            verificationStatus = "Pending Verification",
            postedRides = listOf(
                StudentDriverPostedRide(
                    rideId = "1",
                    pickupLocation = "South Campus",
                    destination = "North Campus",
                    date = "2026-07-01",
                    time = "08:30",
                    availableSeats = 1,
                    farePerSeat = "R20.00",
                    acceptedStudents = listOf(
                        RideAcceptedStudent(
                            name = "Lanele Maqina",
                            studentNumber = "223456789"
                        ),
                        RideAcceptedStudent(
                            name = "Tichaona Mudingwa",
                            studentNumber = "224567890"
                        )
                    )
                )
            )
        )
    }
}