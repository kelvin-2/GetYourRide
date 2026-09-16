package com.example.getyourride.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.RequiresPermission
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
import androidx.compose.ui.tooling.preview.Preview
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
import com.google.android.gms.tasks.CancellationTokenSource

// ---- Color palette (matches your existing app theme) ----
private val NavyDark = Color(0xFF16214B)
private val OrangeAccent = Color(0xFFF7941D)
private val BlueLink = Color(0xFF2F6FE0)
private val CardBg = Color.White
private val ScreenBg = Color(0xFFF4F5FA)
private val TextGray = Color(0xFF6B7280)
private val InfoCardBg = Color(0xFFEDF1FE)
private val InfoIconBg = Color(0xFFDCE4FB)

/** What gets handed back to whatever screen pushed this one. */
data class StopResult(
    val displayName: String,
    val latitude: Double,
    val longitude: Double,
    val isCurrentLocation: Boolean = false
)

/**
 * Redesigned to match the "Trip Customizer" mockup:
 * - Extended navy header with title + eyebrow label + subtitle + info icon,
 *   inset-aware via statusBarsPadding() so it doesn't draw under the status bar
 * - Search pill
 * - Current Location card (with "GPS" badge)
 * - Intelligent Route Optimization info card
 * - Search suggestions list (only shown once the student types something —
 *   the previously hardcoded `recentLocations` list has been removed
 *   entirely, along with the old default-list behavior)
 * - "Confirm Stop Location" CTA, lifted off the bottom edge via
 *   navigationBarsPadding() + extra vertical padding
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
    tripId: Long,
    viewModel: StopSearchViewModel,
    onInfoClick: () -> Unit = {},
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

    // Recreated each time we start a fresh location lookup so a previous,
    // still-in-flight request can be cancelled independently of a new one.
    var cancellationTokenSource by remember { mutableStateOf(CancellationTokenSource()) }

    // If the student navigates away while a GPS fix is still being resolved,
    // cancel it so the callback doesn't fire and update state after this
    // composable (and its ViewModel scope) is gone.
    DisposableEffect(Unit) {
        onDispose {
            cancellationTokenSource.cancel()
        }
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun requestLiveLocation() {
        if (!hasLocationPermission()) {
            viewModel.markCurrentLocationFailed("Location permission not granted")
            return
        }

        // Cancel any prior in-flight lookup before starting a new one (e.g. the
        // student taps "Current Location" again while the first is still resolving).
        cancellationTokenSource.cancel()
        cancellationTokenSource = CancellationTokenSource()

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()
        try {
            fusedClient.getCurrentLocation(request, cancellationTokenSource.token)
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
            AddStopHeader(
                onBackClick = { navController.popBackStack() },
                onInfoClick = onInfoClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBg)
                    .navigationBarsPadding()
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp)
            ) {
                Button(
                    onClick = {
                        val resolvedCurrent = (currentLocationState as? CurrentLocationState.Resolved)?.address
                        val selected = fieldState.selected
                        when {
                            selected != null -> onStopChosen(
                                StopResult(selected.displayName, selected.latitude, selected.longitude)
                            )
                            resolvedCurrent != null -> onStopChosen(
                                StopResult(resolvedCurrent.displayName, resolvedCurrent.latitude, resolvedCurrent.longitude, isCurrentLocation = true)
                            )
                            fieldState.text.isNotBlank() -> {
                                // Nothing was tapped from suggestions (likely because
                                // suggestions came back empty) but the student typed
                                // something — fall back to the precise geocode
                                // endpoint, same one resolveRecentLocation() uses.
                                viewModel.resolveTypedAddress(fieldState.text) { resolved ->
                                    if (resolved != null) {
                                        onStopChosen(StopResult(resolved.displayName, resolved.latitude, resolved.longitude))
                                    }
                                    // If this also fails, the student stays on the
                                    // screen — see note below about surfacing that.
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Filled.AddLocationAlt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Stop Location", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchPill(value = fieldState.text, onValueChange = { viewModel.onTextChanged(it) })

            Spacer(modifier = Modifier.height(12.dp))

            // Once the student starts typing, this becomes a focused search-results
            // view: Current Location and Route Optimization drop away, and come
            // back automatically once the field is cleared.
            val isSearching = fieldState.text.isNotBlank()

            if (!isSearching) {
                CurrentLocationCard(
                    state = currentLocationState,
                    onClick = {
                        if (hasLocationPermission()) requestLiveLocation()
                        else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                RouteOptimizationInfoCard()

                Spacer(modifier = Modifier.height(20.dp))
            } else if (fieldState.suggestions.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(fieldState.suggestions) { suggestion: AddressSuggestion ->
                        SuggestionRow(
                            label = suggestion.displayName,
                            onClick = {
                                viewModel.onSuggestionSelected(suggestion)
                                onStopChosen(StopResult(suggestion.displayName, suggestion.latitude, suggestion.longitude))
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddStopHeader(onBackClick: () -> Unit, onInfoClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(NavyDark)
            .statusBarsPadding()
            .padding(top = 4.dp, bottom = 20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp, start = 4.dp, end = 4.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Text(
                "Add a Stop",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(onClick = onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Info", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "TRIP CUSTOMIZER",
                color = OrangeAccent,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Customize your journey with an additional campus or city stop along your route.",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
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
        Icon(Icons.Filled.Search, contentDescription = null, tint = TextGray)
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
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Use Current Location", color = Color(0xFF1F2937), fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(OrangeAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("GPS", color = OrangeAccent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            val subtitle = when (state) {
                is CurrentLocationState.Idle -> "Pinpoint accurate pickup or drop-off"
                is CurrentLocationState.Locating -> "Finding your location..."
                is CurrentLocationState.Resolved -> state.address.displayName
                is CurrentLocationState.Failed -> state.message
            }
            Text(subtitle, color = TextGray, fontSize = 13.sp)
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextGray)
    }
}

@Composable
private fun RouteOptimizationInfoCard() {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(InfoCardBg).padding(14.dp)
    ) {
        Box(
            modifier = Modifier.size(30.dp).clip(RoundedCornerShape(8.dp)).background(InfoIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.SwapVert, contentDescription = null, tint = BlueLink, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text("Intelligent Route Optimization", color = Color(0xFF1F2937), fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Stops are automatically sequenced to minimize extra travel time and avoid shuttle delays.",
                color = BlueLink,
                fontSize = 13.sp
            )
        }
    }
}

// Themed to match CurrentLocationCard / RouteOptimizationInfoCard instead of
// sitting as a plain white row: soft lavender-blue background, a colored icon
// chip, and blue-tinted text — so suggestions read as part of the same
// design system rather than a generic list.
@Composable
private fun SuggestionRow(label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(InfoCardBg).clickable { onClick() }.padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(RoundedCornerShape(9.dp)).background(InfoIconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = null, tint = BlueLink, modifier = Modifier.size(17.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, color = NavyDark, fontWeight = FontWeight.Medium, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = BlueLink)
    }
}

// =====================================================================
// PREVIEWS — Android Studio design-pane only, not shipped in the app.
//
// AddStopScreen itself needs a real NavController + StopSearchViewModel
// (and their CurrentLocationState / AddressSuggestion types), which are
// awkward to fake convincingly in a @Preview. Instead, AddStopPreviewShell
// below reuses your actual UI building blocks (AddStopHeader, SearchPill,
// CurrentLocationCard, RouteOptimizationInfoCard, SuggestionRow) driven by
// plain local values, so what you see here matches the real screen.
//
// ASSUMPTION TO VERIFY: this assumes CurrentLocationState.Idle and
// .Locating are parameterless (object) and .Failed takes a single String
// message — matching how they're read elsewhere in this file
// (`is CurrentLocationState.Failed -> state.message`). If your actual
// sealed class differs, adjust the three preview calls below.
// =====================================================================

@Composable
private fun AddStopPreviewShell(
    searchText: String,
    currentLocationState: CurrentLocationState,
    previewSuggestions: List<String> = emptyList()
) {
    val isSearching = searchText.isNotBlank()
    Scaffold(
        containerColor = ScreenBg,
        topBar = { AddStopHeader(onBackClick = {}, onInfoClick = {}) },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ScreenBg)
                    .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 28.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                ) {
                    Icon(Icons.Filled.AddLocationAlt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Confirm Stop Location", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(16.dp))

            SearchPill(value = searchText, onValueChange = {})

            Spacer(modifier = Modifier.height(12.dp))

            if (!isSearching) {
                CurrentLocationCard(state = currentLocationState, onClick = {})
                Spacer(modifier = Modifier.height(12.dp))
                RouteOptimizationInfoCard()
                Spacer(modifier = Modifier.height(20.dp))
            } else if (previewSuggestions.isNotEmpty()) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(previewSuggestions) { label ->
                        SuggestionRow(label = label, onClick = {})
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Add Stop – Browsing (empty)")
@Composable
private fun AddStopScreenEmptyPreview() {
    AddStopPreviewShell(
        searchText = "",
        currentLocationState = CurrentLocationState.Idle
    )
}

@Preview(showBackground = true, name = "Add Stop – Locating GPS")
@Composable
private fun AddStopScreenLocatingPreview() {
    AddStopPreviewShell(
        searchText = "",
        currentLocationState = CurrentLocationState.Locating
    )
}

@Preview(showBackground = true, name = "Add Stop – Permission denied")
@Composable
private fun AddStopScreenPermissionDeniedPreview() {
    AddStopPreviewShell(
        searchText = "",
        currentLocationState = CurrentLocationState.Failed("Permission denied")
    )
}

@Preview(showBackground = true, name = "Add Stop – Searching with results")
@Composable
private fun AddStopScreenSearchingPreview() {
    AddStopPreviewShell(
        searchText = "humewood",
        currentLocationState = CurrentLocationState.Failed("Permission denied"),
        previewSuggestions = listOf(
            "Humewood, Gqeberha, South Africa",
            "Humewood Beach, Summerstrand, South Africa"
        )
    )
}

@Preview(showBackground = true, name = "Add Stop – Header only")
@Composable
private fun AddStopHeaderPreview() {
    AddStopHeader(onBackClick = {}, onInfoClick = {})
}

@Preview(showBackground = true, name = "Suggestion row")
@Composable
private fun SuggestionRowPreview() {
    Box(modifier = Modifier.background(ScreenBg).padding(16.dp)) {
        SuggestionRow(label = "Humewood, Gqeberha, South Africa", onClick = {})
    }
}