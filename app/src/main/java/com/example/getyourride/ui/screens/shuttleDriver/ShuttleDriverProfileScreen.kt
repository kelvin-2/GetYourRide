package com.example.getyourride.ui.screens.shuttleDriver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.ui.components.ShuttleDriverBottomBar
import com.example.getyourride.ui.components.ShuttleDriverBottomBarItem
import com.example.getyourride.ui.theme.GetYourRideTheme

/*
 * ShuttleDriverProfileScreen
 *
 * This page is for shuttle drivers only.
 *
 * Important system rule:
 * Shuttle drivers do not sign up inside the mobile app.
 * The admin creates the shuttle driver account and gives the driver login details.
 *
 * This screen only displays information that can come from the current database:
 * - driver
 * - vehicle
 * - trip
 * - trip_booking
 * - boarding_log
 *
 * We do not show:
 * - password
 * - verification status
 * - delete profile button
 * - document upload
 *
 * Shuttle drivers are already trusted/admin-created users when they log in.
 */

// Screen colours matching the existing GetYourRide design.
private val ShuttleProfileBackground = Color(0xFFFBF8FD)
private val ShuttleProfilePrimary = Color(0xFF011844)
private val ShuttleProfileTopBar = Color(0xFF1A2E5A)
private val ShuttleProfileCardBackground = Color(0xFFFFFFFF)
private val ShuttleProfileText = Color(0xFF1B1B1F)
private val ShuttleProfileTextMuted = Color(0xFF44464F)
private val ShuttleProfilePrimaryFixed = Color(0xFFDAE2FF)
private val ShuttleProfileBorder = Color(0xFFE3E2E6)
private val ShuttleProfileInfoBackground = Color(0xFFE3F2FD)
private val ShuttleProfileInfoText = Color(0xFF1565C0)

/*
 * Shuttle driver personal/account details.
 *
 * Database source later:
 * driver.driver_id
 * driver.first_name
 * driver.last_name
 * driver.email
 * driver.phone
 * driver.role
 * driver.join_date
 * driver.total_trips
 */
@Immutable
data class ShuttleDriverProfileDetails(
    val driverId: Long,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    val joinDate: String,
    val totalTrips: Int
)

/*
 * Vehicle assigned to the shuttle driver.
 *
 * Database source later:
 * vehicle.registration_number
 * vehicle.model
 * vehicle.vehicle_year
 * vehicle.colour
 * vehicle.capacity
 */
@Immutable
data class ShuttleDriverVehicleDetails(
    val registrationNumber: String,
    val model: String,
    val vehicleYear: Int?,
    val colour: String,
    val capacity: Int
)

/*
 * Summary values calculated from trip, trip_booking, and boarding_log.
 *
 * Database source later:
 * trip.status
 * trip.trip_type = 'Shuttle'
 * trip_booking.booking_id
 * boarding_log.boarded_at
 */
@Immutable
data class ShuttleDriverTripSummary(
    val currentTripRoute: String,
    val currentTripStatus: String,
    val scheduledTrips: Int,
    val inProgressTrips: Int,
    val completedTrips: Int,
    val cancelledTrips: Int,
    val studentsBookedToday: Int,
    val studentsBoardedToday: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShuttleDriverProfileScreen(
    profileDetails: ShuttleDriverProfileDetails = sampleShuttleDriverProfileDetails(),
    vehicleDetails: ShuttleDriverVehicleDetails = sampleShuttleDriverVehicleDetails(),
    tripSummary: ShuttleDriverTripSummary = sampleShuttleDriverTripSummary(),
    onScanQrCodeClick: () -> Unit = {},
    onBoardingClick: () -> Unit = {},
    onProfileClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            /*
             * Same top bar style as the Student Driver and Shuttle Boarding pages.
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
                    containerColor = ShuttleProfileTopBar
                )
            )
        },
        bottomBar = {
            /*
             * Profile is selected because the shuttle driver is currently on this page.
             */
            ShuttleDriverBottomBar(
                selectedItem = ShuttleDriverBottomBarItem.Profile,
                onScanQrCodeClick = onScanQrCodeClick,
                onBoardingClick = onBoardingClick,
                onProfileClick = onProfileClick
            )
        },
        containerColor = ShuttleProfileBackground
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ShuttleProfileBackground)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Profile",
                    color = ShuttleProfilePrimary,
                    fontSize = 26.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "View your shuttle driver account details.",
                    color = ShuttleProfileTextMuted,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            AccountManagedByAdminCard()

            ProfileSectionCard(
                title = "Driver Information",
                icon = Icons.Outlined.Person
            ) {
                ProfileDetailRow(
                    label = "Full Name",
                    value = "${profileDetails.firstName} ${profileDetails.lastName}"
                )

                ProfileDetailRow(
                    label = "Email",
                    value = profileDetails.email,
                    icon = Icons.Outlined.Email
                )

                ProfileDetailRow(
                    label = "Phone",
                    value = profileDetails.phone,
                    icon = Icons.Outlined.Phone
                )

                ProfileDetailRow(
                    label = "Role",
                    value = profileDetails.role
                )

                ProfileDetailRow(
                    label = "Join Date",
                    value = profileDetails.joinDate
                )

                ProfileDetailRow(
                    label = "Total Trips",
                    value = profileDetails.totalTrips.toString()
                )
            }

            ProfileSectionCard(
                title = "Assigned Vehicle",
                icon = Icons.Outlined.DirectionsCar
            ) {
                ProfileDetailRow(
                    label = "Registration Number",
                    value = vehicleDetails.registrationNumber
                )

                ProfileDetailRow(
                    label = "Model",
                    value = vehicleDetails.model
                )

                ProfileDetailRow(
                    label = "Year",
                    value = vehicleDetails.vehicleYear?.toString() ?: "Not set"
                )

                ProfileDetailRow(
                    label = "Colour",
                    value = vehicleDetails.colour
                )

                ProfileDetailRow(
                    label = "Capacity",
                    value = vehicleDetails.capacity.toString()
                )
            }

            ProfileSectionCard(
                title = "Trip Summary",
                icon = Icons.Outlined.VerifiedUser
            ) {
                ProfileDetailRow(
                    label = "Current Trip",
                    value = tripSummary.currentTripRoute
                )

                ProfileDetailRow(
                    label = "Current Trip Status",
                    value = tripSummary.currentTripStatus
                )

                TripStatusGrid(
                    tripSummary = tripSummary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/*
 * Shows that this shuttle driver account is controlled by the admin.
 */
@Composable
private fun AccountManagedByAdminCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = ShuttleProfileInfoBackground,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = ShuttleProfileInfoText,
                modifier = Modifier.size(24.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Admin Managed Account",
                    color = ShuttleProfileInfoText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Your shuttle driver account is created and managed by the administrator. Contact admin if your details are incorrect.",
                    color = ShuttleProfileInfoText,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }
    }
}

/*
 * Reusable card section.
 *
 * We use this for:
 * - Driver Information
 * - Assigned Vehicle
 * - Trip Summary
 */
@Composable
private fun ProfileSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = ShuttleProfileCardBackground
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(ShuttleProfilePrimaryFixed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = ShuttleProfilePrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = title,
                    color = ShuttleProfilePrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            DividerLine()

            content()
        }
    }
}

/*
 * One row of profile information.
 */
@Composable
private fun ProfileDetailRow(
    label: String,
    value: String,
    icon: ImageVector? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ShuttleProfileTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                color = ShuttleProfileTextMuted,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = value,
                color = ShuttleProfileText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/*
 * Trip statistics shown in small cards.
 */
@Composable
private fun TripStatusGrid(
    tripSummary: ShuttleDriverTripSummary
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryBox(
                label = "Scheduled",
                value = tripSummary.scheduledTrips.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryBox(
                label = "In Progress",
                value = tripSummary.inProgressTrips.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryBox(
                label = "Completed",
                value = tripSummary.completedTrips.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryBox(
                label = "Cancelled",
                value = tripSummary.cancelledTrips.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryBox(
                label = "Booked Today",
                value = tripSummary.studentsBookedToday.toString(),
                modifier = Modifier.weight(1f)
            )

            SummaryBox(
                label = "Boarded Today",
                value = tripSummary.studentsBoardedToday.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/*
 * Small statistic box used in Trip Summary.
 */
@Composable
private fun SummaryBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(ShuttleProfileBackground)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            color = ShuttleProfileTextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )

        Text(
            text = value,
            color = ShuttleProfilePrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/*
 * Divider used inside section cards.
 */
@Composable
private fun DividerLine() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(ShuttleProfileBorder)
    )
}

/*
 * Temporary sample data for frontend preview/testing.
 *
 * Later this will come from the driver table.
 */
private fun sampleShuttleDriverProfileDetails(): ShuttleDriverProfileDetails {
    return ShuttleDriverProfileDetails(
        driverId = 1L,
        firstName = "Michael",
        lastName = "Johnson",
        email = "michael.driver@getyourride.co.za",
        phone = "071 555 0987",
        role = "Shuttle Driver",
        joinDate = "2026-01-15",
        totalTrips = 128
    )
}

/*
 * Temporary sample data for frontend preview/testing.
 *
 * Later this will come from the vehicle table.
 */
private fun sampleShuttleDriverVehicleDetails(): ShuttleDriverVehicleDetails {
    return ShuttleDriverVehicleDetails(
        registrationNumber = "SHU 245 EC",
        model = "Toyota Quantum",
        vehicleYear = 2022,
        colour = "White",
        capacity = 24
    )
}

/*
 * Temporary sample data for frontend preview/testing.
 *
 * Later this will be calculated from trip, trip_booking, and boarding_log.
 */
private fun sampleShuttleDriverTripSummary(): ShuttleDriverTripSummary {
    return ShuttleDriverTripSummary(
        currentTripRoute = "South Campus → Central",
        currentTripStatus = "In Progress",
        scheduledTrips = 3,
        inProgressTrips = 1,
        completedTrips = 124,
        cancelledTrips = 0,
        studentsBookedToday = 18,
        studentsBoardedToday = 12
    )
}

/*
 * Android Studio preview.
 */
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ShuttleDriverProfileScreenPreview() {
    GetYourRideTheme(dynamicColor = false) {
        ShuttleDriverProfileScreen()
    }
}