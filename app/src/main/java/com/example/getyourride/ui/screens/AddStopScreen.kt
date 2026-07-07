package com.example.getyourride.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.getyourride.data.remote.dto.AddressSuggestion
import com.example.getyourride.viewmodel.CurrentLocationState
import com.example.getyourride.viewmodel.StopSearchViewModel
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

// ---- Color palette (matches your existing app theme) ----
private val NavyDark = Color(0xFF16214B)
private val OrangeAccent = Color(0xFFF7941D)
private val BlueLink = Color(0xFF2F6FE0)
private val CardBg = Color.White
private val ScreenBg = Color(0xFFF4F5FA)
private val TextGray = Color(0xFF6B7280)

/** What gets handed back to whatever screen pushed this one. */
data class StopResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)

/**
 * NOTE: recentLocations is still a hardcoded list of labels (per your earlier
 * call). Tapping one resolves it into real coordinates via
 * viewModel.resolveRecentLocation(), the same precise /api/geocode endpoint
 * CarpoolSearchViewModel uses for typed text - so a recent behaves exactly
 * like a freshly searched address once picked.
 *
 * NOTE: I still don't have whatever manages the trip's stop list (a
 * TripCreationViewModel or similar, if one exists). For now onStopChosen
 * defaults to sending the result back via savedStateHandle and popping the
 * back stack. If there's a shared ViewModel that should receive this stop
 * directly instead, point me at it and I'll wire it in properly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStopScreen(
    navController: NavController,
    recentLocations: List<String> = listOf(
        "Engineering Bldg",
        "Main Library",
        "Student Res A",
        "Science Park"
    ),
    viewModel: StopSearchViewModel,
    onStopChosen: (StopResult) -> Unit = { stop ->
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.set("selected_stop", stop)
        navController.popBackStack()
    }
) {
    val context = LocalContext.current

    val fieldState by viewModel.field.collectAsState()
    val currentLocationState by viewModel.currentLocation.collectAsState()

    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    fun requestLiveLocation() {
        if (!hasLocationPermission()) {
            viewModel.markCurrentLocationFailed("Location permission not granted")
            return
        }
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        try {
            fusedClient.getCurrentLocation(request, null)
                .addOnSuccessListener { location ->
                    if (location == null) {
                        viewModel.markCurrentLocationFailed("Couldn't get a GPS fix")
                    } else {
                        viewModel.resolveCurrentLocation(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener {
                    viewModel.markCurrentLocationFailed(it.message ?: "Location request failed")
                }
        } catch (se: SecurityException) {
            viewModel.markCurrentLocationFailed("Location permission not granted")
        }
    }

    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) requestLiveLocation() else viewModel.markCurrentLocationFailed("Permission denied") }

    Scaffold(
        containerColor = ScreenBg,
        topBar = {
            TopAppBar(
                title = { Text("Add a Stop", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NavyDark)
            )
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(ScreenBg).padding(16.dp)) {
                Button(
                    onClick = {
                        val resolvedCurrent = (currentLocationState as? CurrentLocationState.Resolved)?.address
                        val selected = fieldState.selected
                        when {
                            selected != null -> onStopChosen(
                                StopResult(selected.displayName, selected.lat, selected.lon)
                            )
                            resolvedCurrent != null -> onStopChosen(
                                StopResult(resolvedCurrent.displayName, resolvedCurrent.lat, resolvedCurrent.lon, isCurrentLocation = true)
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add This Stop", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchPill(value = fieldState.text, onValueChange = { viewModel.onTextChanged(it) })

            Spacer(modifier = Modifier.height(12.dp))

            CurrentLocationCard(
                state = currentLocationState,
                onClick = {
                    if (hasLocationPermission()) requestLiveLocation()
                    else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (fieldState.text.isNotBlank() && fieldState.suggestions.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(fieldState.suggestions) { suggestion: AddressSuggestion ->
                        RecentLocationRow(
                            label = suggestion.displayName,
                            onClick = {
                                viewModel.onSuggestionSelected(suggestion)
                                onStopChosen(StopResult(suggestion.displayName, suggestion.lat, suggestion.lon))
                            }
                        )
                    }
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(recentLocations) { label ->
                        RecentLocationRow(
                            label = label,
                            onClick = {
                                viewModel.resolveRecentLocation(label) { resolved ->
                                    if (resolved != null) {
                                        onStopChosen(StopResult(resolved.displayName, resolved.lat, resolved.lon))
                                    }
                                    // If resolution fails, we simply don't navigate - the
                                    // student stays on this screen and can try search instead.
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchPill(value: String, onValueChange: (String) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFEDEEF6)).padding(horizontal = 14.dp, vertical = 14.dp)
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BlueLink)
        Spacer(modifier = Modifier.width(8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search for an address or place") },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedTextColor = BlueLink,
                focusedTextColor = BlueLink,
                cursorColor = BlueLink
            ),
            textStyle = androidx.compose.ui.text.TextStyle(fontWeight = FontWeight.Medium, fontSize = 16.sp)
        )
    }
}

@Composable
private fun CurrentLocationCard(state: CurrentLocationState, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(CardBg).clickable { onClick() }.padding(14.dp)
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(RoundedCornerShape(10.dp)).background(OrangeAccent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            if (state is CurrentLocationState.Locating) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = OrangeAccent, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Filled.MyLocation, contentDescription = null, tint = OrangeAccent)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Current Location", color = OrangeAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            val subtitle = when (state) {
                is CurrentLocationState.Idle -> "Using GPS for precision"
                is CurrentLocationState.Locating -> "Finding your location..."
                is CurrentLocationState.Resolved -> state.address.displayName
                is CurrentLocationState.Failed -> state.message
            }
            Text(subtitle, color = BlueLink, fontSize = 13.sp)
        }
    }
}

@Composable
private fun RecentLocationRow(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(CardBg).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 16.dp)
    ) {
        Icon(Icons.Filled.History, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = Color(0xFF1F2937), fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextGray)
    }
}