package com.example.getyourride.ui.screens.Tracking

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.rememberNavController
import com.example.getyourride.domain.model.RideStatus
import com.example.getyourride.domain.model.TripTrackingInfo
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.viewmodel.TrackingUiState
import com.example.getyourride.viewmodel.TrackingViewModel
import com.example.getyourride.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// Match these to your app's theme colors (Theme.kt) instead of hardcoding
private val UniRideOrange = Color(0xFFFF7A1A)
private val UniRideNavy = Color(0xFF141A33)
private val CardGrey = Color(0xFFF5F6F8)

/**
 * Stateful entry point — wired into NavHost in MainActivity.
 *
 * IMPORTANT: The ViewModel is now built and passed in by the caller
 * (MainActivity's "track/{rideId}" composable), since building it here
 * required a real socket instance at composition time. Keeping construction
 * in MainActivity means MainActivity controls what socket implementation
 * (real STOMP client vs MockRideLocationSocket) gets used, and this screen
 * doesn't need to know or care.
 */
@Composable
fun TrackingScreen(
    viewModel: TrackingViewModel,
    onBackClick: () -> Unit = {},
    navController: androidx.navigation.NavController = rememberNavController(),
    onMessageDriver: () -> Unit = {},
    onCallDriver: () -> Unit = {},
    onCancelRide: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.startTracking()
    }

    StudentLayout(
        currentRoute = GyrRoutes.TRACK,
        navController = navController,
        showBell = true
    ) {
        TrackingScreenContent(
            uiState = uiState,
            onBackClick = onBackClick,
            onMessageDriver = onMessageDriver,
            onCallDriver = onCallDriver,
            onCancelRide = { viewModel.cancelRide(onCancelRide) }
        )
    }
}

/**
 * Stateless UI — no ViewModel, no socket, just data in. This is what makes
 * the screen previewable, since @Preview can't satisfy a real STOMP connection.
 */
@Composable
fun TrackingScreenContent(
    uiState: TrackingUiState,
    onBackClick: () -> Unit = {},
    onMessageDriver: () -> Unit = {},
    onCallDriver: () -> Unit = {},
    onCancelRide: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            OsmMapSection(uiState = uiState)
        }
        uiState.tripInfo?.let { info ->
            DriverInfoCard(
                info = info,
                onMessageDriver = onMessageDriver,
                onCallDriver = onCallDriver,
                onCancelRide = onCancelRide
            )
        } ?: run {
            // Placeholder when no trip is active (e.g. from the Track tab)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No active ride to track.\nBook a ride to see live updates!",
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        }
    }
}

// --- Preview -----------------------------------------------------------

private val previewUiState = TrackingUiState(
    driverLocation = GeoPoint(-33.9581, 25.6014),      // sample NMU South Campus-ish coords
    destinationLocation = GeoPoint(-33.9615, 25.6089),
    isConnected = true,
    error = null,
    tripInfo = TripTrackingInfo(
        driverName = "Marcus Thompson",
        driverRating = 4.9,
        status = RideStatus.ON_THE_WAY,
        etaMinutes = 4,
        carModel = "Toyota Corolla",
        carColor = "White",
        carYear = 2022,
        plateNumber = "UNI-7842",
        isPlateVerified = true,
        destinationLabel = "Library North"
    )
)

@Preview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
private fun TrackingScreenPreview() {
    TrackingScreenContent(uiState = previewUiState)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Connecting state")
@Composable
private fun TrackingScreenConnectingPreview() {
    TrackingScreenContent(
        uiState = previewUiState.copy(
            isConnected = false,
            driverLocation = null,
            tripInfo = previewUiState.tripInfo?.copy(etaMinutes = null)
        )
    )
}

@Composable
private fun OsmMapSection(uiState: TrackingUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            driverLocation = uiState.driverLocation,
            destinationLocation = uiState.destinationLocation,
            destinationLabel = uiState.tripInfo?.destinationLabel ?: "Destination",
            modifier = Modifier.fillMaxSize()
        )

        // Destination chip, top-left over the map (mirrors "Library North" pill in the mockup)
        uiState.tripInfo?.destinationLabel?.let { label ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .background(UniRideNavy, RoundedCornerShape(20.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text(label, color = Color.White, fontSize = 13.sp)
            }
        }

        FloatingActionButton(
            onClick = { /* TODO: recenter map on the student's current location */ },
            containerColor = Color.White,
            contentColor = UniRideOrange,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Recenter")
        }
    }
}

@Composable
private fun OsmMapView(
    driverLocation: GeoPoint?,
    destinationLocation: GeoPoint?,
    destinationLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // free OpenStreetMap tiles, no API key
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    val driverMarker = remember { Marker(mapView) }
    val destinationMarker = remember { Marker(mapView) }
    val routeLine = remember { Polyline(mapView) }

    DisposableEffect(Unit) {
        mapView.overlays.add(routeLine)
        mapView.overlays.add(destinationMarker)
        mapView.overlays.add(driverMarker)
        onDispose { mapView.onDetach() }
    }

    LaunchedEffect(driverLocation, destinationLocation) {
        driverLocation?.let { point ->
            driverMarker.position = point
            driverMarker.title = "Your shuttle"
            mapView.controller.animateTo(point)
        }
        destinationLocation?.let { point ->
            destinationMarker.position = point
            destinationMarker.title = destinationLabel
        }
        if (driverLocation != null && destinationLocation != null) {
            routeLine.setPoints(listOf(driverLocation, destinationLocation))
        }
        mapView.invalidate()
    }

    AndroidView(factory = { mapView }, modifier = modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)))
}

@Composable
private fun DriverInfoCard(
    info: TripTrackingInfo,
    onMessageDriver: () -> Unit,
    onCallDriver: () -> Unit,
    onCancelRide: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Drag handle
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFDDDDDD), RoundedCornerShape(2.dp))
            )
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFE3CC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Person, contentDescription = null, tint = UniRideOrange)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.driverName, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusBadge(status = info.status)
                        Spacer(Modifier.width(6.dp))
                        Icon(Icons.Filled.Star, contentDescription = null, tint = UniRideOrange, modifier = Modifier.size(14.dp))
                        Text(" ${info.driverRating}", fontSize = 13.sp)
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("ETA", fontSize = 12.sp, color = Color.Gray)
                    Text(
                        info.etaMinutes?.let { "$it min" } ?: "--",
                        color = UniRideOrange,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoTile(
                    label = "Car Model",
                    value = info.carModel,
                    subtitle = "${info.carColor} • ${info.carYear}",
                    modifier = Modifier.weight(1f)
                )
                InfoTile(
                    label = "Plate Number",
                    value = info.plateNumber,
                    subtitle = if (info.isPlateVerified) "Verified" else "Unverified",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onMessageDriver,
                    colors = ButtonDefaults.buttonColors(containerColor = UniRideOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.weight(1f).height(52.dp)
                ) {
                    Text("Message", color = Color.White)
                }
                OutlinedIconButton(
                    onClick = onCallDriver,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Call driver", tint = UniRideOrange)
                }
                OutlinedIconButton(
                    onClick = onCancelRide,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.size(52.dp)
                ) {
                    Icon(Icons.Filled.Cancel, contentDescription = "Cancel ride", tint = Color(0xFFE0483E))
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: RideStatus) {
    Box(
        modifier = Modifier
            .background(Color(0xFFDFF5E3), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(status.label, fontSize = 10.sp, color = Color(0xFF2E9E4F), fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InfoTile(label: String, value: String, subtitle: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(CardGrey, RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(label, fontSize = 11.sp, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Text(subtitle, fontSize = 11.sp, color = Color.Gray)
    }
}