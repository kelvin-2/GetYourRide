package com.example.getyourride.ui.screens.Carpool

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.UserSession
import com.example.getyourride.data.mapper.toCarpoolRide
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.data.repository.GeocodingRepository
import com.example.getyourride.ui.components.GyrMapBackground
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.screens.Carpool.components.*
import com.example.getyourride.ui.theme.*
import com.example.getyourride.viewmodel.CarpoolSearchViewModel
import com.example.getyourride.viewmodel.CarpoolSearchViewModelFactory
import com.example.getyourride.viewmodel.LocationFieldState
import com.example.getyourride.viewmodel.TripsUiState

private fun buildGeocodingRepository(): GeocodingRepository {
    return GeocodingRepository(com.example.getyourride.di.NetworkModule.geocodingApi)
}

// ── Local "on navy" palette — glassy translucent surfaces over GyrMapBackground ──
private val GlassSurface      = Color.White.copy(alpha = 0.07f)
private val GlassBorder       = Color.White.copy(alpha = 0.10f)
private val TextOnNavy        = Color.White
private val TextOnNavyMuted   = Color.White.copy(alpha = 0.65f)

@Composable
fun CarpoolHomeScreen(
    searchViewModel     : CarpoolSearchViewModel  = viewModel(
        factory = CarpoolSearchViewModelFactory(buildGeocodingRepository())
    ),
    uiState             : TripsUiState             = TripsUiState.Loading,
    onRetry             : () -> Unit               = {},
    onBookRide          : (rideId: String) -> Unit = {},
    onViewAllRides      : () -> Unit               = {},
    onViewAllTrips      : () -> Unit               = {},
    onSearchRides       : (pickup: AddressSuggestion, destination: AddressSuggestion) -> Unit = { _, _ -> },
    navController       : androidx.navigation.NavController = rememberNavController(),
)

{
    val pickupState by searchViewModel.pickup.collectAsState()
    val destinationState by searchViewModel.destination.collectAsState()

    StudentLayout(
        currentRoute  = GyrRoutes.HOME,
        navController = navController,
    ) {
        GyrMapBackground(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Spacer(Modifier.height(4.dp))

                FindCarpoolHeader(
                    pickupState                     = pickupState,
                    destinationState                = destinationState,
                    onPickupTextChanged             = searchViewModel::onPickupTextChanged,
                    onPickupSuggestionSelected      = searchViewModel::onPickupSuggestionSelected,
                    onDestinationTextChanged        = searchViewModel::onDestinationTextChanged,
                    onDestinationSuggestionSelected = searchViewModel::onDestinationSuggestionSelected,
                    onSearchRides = {
                        val pickup = pickupState.selected
                        val destination = destinationState.selected
                        if (pickup != null && destination != null) {
                            onSearchRides(pickup, destination)
                        }
                    },
                )

                AvailableRidesSection(
                    uiState    = uiState,
                    onRetry    = onRetry,
                    onBookRide = onBookRide,
                    onViewAll  = onViewAllRides,
                )

                RecentTripsSection(onViewAll = onViewAllTrips)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

// ── 1. Find a Carpool header ────────────────────────────────────────────────

@Composable
private fun FindCarpoolHeader(
    pickupState                     : LocationFieldState,
    destinationState                : LocationFieldState,
    onPickupTextChanged             : (String) -> Unit,
    onPickupSuggestionSelected      : (AddressSuggestion) -> Unit,
    onDestinationTextChanged        : (String) -> Unit,
    onDestinationSuggestionSelected : (AddressSuggestion) -> Unit,
    onSearchRides                   : () -> Unit,
) {
    Column {
        Text(
            text       = "Find a Carpool",
            fontSize   = 24.sp,
            fontWeight = FontWeight.Bold,
            color      = TextOnNavy,
        )
        Text(
            text     = "Connect with students traveling your way",
            fontSize = 13.sp,
            color    = TextOnNavyMuted,
        )

        Spacer(Modifier.height(16.dp))

        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = GlassSurface),
            border    = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                AutocompleteLocationField(
                    label                = "Pickup Location",
                    state                = pickupState,
                    icon                 = Icons.Outlined.LocationOn,
                    iconTint             = OrangeAccent,
                    onTextChanged        = onPickupTextChanged,
                    onSuggestionSelected = onPickupSuggestionSelected,
                )

                Spacer(Modifier.height(14.dp))

                AutocompleteLocationField(
                    label                = "Destination",
                    state                = destinationState,
                    icon                 = Icons.Outlined.Navigation,
                    iconTint             = OrangeAccent,
                    onTextChanged        = onDestinationTextChanged,
                    onSuggestionSelected = onDestinationSuggestionSelected,
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        Button(
            onClick        = onSearchRides,
            enabled        = pickupState.selected != null && destinationState.selected != null,
            shape          = RoundedCornerShape(24.dp),
            colors         = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
            contentPadding = PaddingValues(vertical = 14.dp),
            modifier       = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                text       = "Search Rides",
                fontSize   = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color      = Color.White,
            )
        }
    }
}

@Composable
private fun AutocompleteLocationField(
    label                : String,
    state                : LocationFieldState,
    icon                 : androidx.compose.ui.graphics.vector.ImageVector,
    iconTint             : Color,
    onTextChanged        : (String) -> Unit,
    onSuggestionSelected : (AddressSuggestion) -> Unit,
) {
    Column {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color      = TextOnNavyMuted,
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value          = state.text,
            onValueChange  = onTextChanged,
            leadingIcon    = {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
            },
            trailingIcon   = {
                if (state.selected != null) {
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(18.dp))
                }
            },
            placeholder    = { Text("Type an address...", fontSize = 13.sp, color = TextOnNavyMuted) },
            singleLine     = true,
            shape          = RoundedCornerShape(10.dp),
            colors         = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                focusedContainerColor   = Color.White.copy(alpha = 0.10f),
                unfocusedBorderColor    = Color.Transparent,
                focusedBorderColor      = OrangeAccent,
                unfocusedTextColor      = TextOnNavy,
                focusedTextColor        = TextOnNavy,
                cursorColor             = OrangeAccent,
            ),
            modifier       = Modifier.fillMaxWidth(),
        )

        if (state.isLoading) {
            LinearProgressIndicator(
                color    = OrangeAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
            )
        }

        if (state.suggestions.isNotEmpty() && state.selected == null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.08f)),
            ) {
                state.suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionSelected(suggestion) }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.LocationOn,
                            contentDescription = null,
                            tint     = TextOnNavyMuted,
                            modifier = Modifier.size(14.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text     = suggestion.displayName,
                            fontSize = 13.sp,
                            color    = TextOnNavy,
                        )
                    }
                }
            }
        }
    }
}

// ── 2. Available rides ────────────────────────────────────────────────────

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun AvailableRidesSection(
    uiState    : TripsUiState,
    onRetry    : () -> Unit,
    onBookRide : (String) -> Unit,
    onViewAll  : () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderOnNavy(title = "Available Rides", onViewAll = onViewAll)

        when (uiState) {
            is TripsUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = OrangeAccent)
                }
            }

            is TripsUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = CardDefaults.cardColors(containerColor = GlassSurface),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text     = "Couldn't load rides — ${uiState.message}",
                            fontSize = 13.sp,
                            color    = TextOnNavyMuted,
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = onRetry,
                            colors  = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                        ) {
                            Text("Retry", color = Color.White)
                        }
                    }
                }
            }

            is TripsUiState.Success -> {
                if (uiState.trips.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(12.dp),
                        colors   = CardDefaults.cardColors(containerColor = GlassSurface),
                        border   = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                    ) {
                        Text(
                            text     = "No carpool rides available right now — check back soon.",
                            fontSize = 13.sp,
                            color    = TextOnNavyMuted,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                } else {
                    uiState.trips.forEach { trip: TripResponse ->
                        CarpoolBookingCard(
                            ride        = trip.toCarpoolRide(),
                            onBookClick = { onBookRide(trip.tripId.toString()) },
                        )
                    }
                }
            }
        }
    }
}

// ── 3. Recent trips ─────────────────────────────────────────────────────────

@Composable
private fun RecentTripsSection(onViewAll: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeaderOnNavy(title = "Recent Trips", onViewAll = onViewAll)

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(12.dp),
            colors    = CardDefaults.cardColors(containerColor = GlassSurface),
            border    = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
            elevation = CardDefaults.cardElevation(0.dp),
        ) {
            Row(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(50))
                            .background(OrangeAccent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.History, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Summerstrand → South Campus", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextOnNavy)
                        Text("Yesterday, 13 Oct", fontSize = 12.sp, color = TextOnNavyMuted)
                        Row {
                            repeat(4) {
                                Icon(Icons.Outlined.Star, contentDescription = null, tint = OrangeAccent, modifier = Modifier.size(14.dp))
                            }
                            Icon(Icons.Outlined.StarOutline, contentDescription = null, tint = TextOnNavyMuted, modifier = Modifier.size(14.dp))
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusCompleted.copy(alpha = 0.18f),
                ) {
                    Text(
                        text       = "Completed",
                        fontSize   = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color      = StatusCompleted,
                        modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderOnNavy(title: String, onViewAll: () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextOnNavy)
        Text(
            text     = "View All",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color    = OrangeAccent,
            modifier = Modifier.clickable { onViewAll() },
        )
    }
}

// ─── Preview ────────────────────────────────────────────────────────────────

@Preview(showBackground = true, showSystemUi = true, name = "Carpool Home")
@Composable
fun CarpoolHomePreview() {
    MaterialTheme { CarpoolHomeScreen() }
}