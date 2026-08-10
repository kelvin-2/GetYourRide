package com.example.getyourride.ui.screens.Tracking

import android.graphics.DashPathEffect
import androidx.appcompat.content.res.AppCompatResources
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
import androidx.compose.ui.graphics.toArgb
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
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

// Google Maps (Compose) — added for the default map, with osmdroid as fallback
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker as GoogleMarker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline as GooglePolyline
import com.google.maps.android.compose.rememberCameraPositionState

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
    navController: androidx.navigation.NavController,
    onBackClick: (() -> Unit)? = null,
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
        onBackClick = onBackClick
    ) {
        TrackingScreenContent(
            uiState = uiState,
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
    onMessageDriver: () -> Unit = {},
    onCallDriver: () -> Unit = {},
    onCancelRide: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            MapSection(uiState = uiState)
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


// --- Map selection: Google Maps default, osmdroid fallback -------------

/**
 * Master switch for which map renderer this screen uses.
 *
 * Currently FALSE because the Maps SDK for Android requires a billing-enabled
 * Google Cloud project. Without a billing account attached, every tile request
 * fails with "Authorization failure" and the map renders blank — the renderer
 * itself initializes fine, which is why this looked like a layout bug at first.
 *
 * osmdroid needs no API key and no billing, so it's the default. Flip this to
 * true once billing is enabled on the Cloud project AND the release/debug
 * SHA-1 fingerprints are allowlisted against the MAPS_API_KEY in
 * local.properties.
 */
private const val USE_GOOGLE_MAPS = false

@Composable
private fun MapSection(uiState: TrackingUiState) {
    val context = LocalContext.current

    // Play Services still matters even when Google Maps is enabled — an
    // unavailable/outdated GMS means the Maps SDK can't render at all.
    val playServicesOk = remember {
        GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
    }

    if (USE_GOOGLE_MAPS && playServicesOk) {
        GoogleMapSection(uiState = uiState)
    } else {
        OsmMapSection(uiState = uiState)
    }
}

@Composable
private fun GoogleMapSection(uiState: TrackingUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMapView(
            driverLocation = uiState.driverLocation,
            destinationLocation = uiState.destinationLocation,
            stops = uiState.stops,
            currentStopIndex = uiState.currentStopIndex,
            modifier = Modifier.fillMaxSize()
        )

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
private fun GoogleMapView(
    driverLocation: GeoPoint?,
    destinationLocation: GeoPoint?,
    stops: List<GeoPoint> = emptyList(),
    currentStopIndex: Int = 0,
    modifier: Modifier = Modifier
) {
    fun GeoPoint.toLatLng() = LatLng(latitude, longitude)

    val startTarget = driverLocation?.toLatLng() ?: LatLng(-33.9581, 25.6014)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(startTarget, 16f)
    }

    GoogleMap(
        modifier = modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
        cameraPositionState = cameraPositionState
    ) {
        driverLocation?.let {
            GoogleMarker(state = MarkerState(position = it.toLatLng()), title = "Driver")
        }
        destinationLocation?.let {
            GoogleMarker(state = MarkerState(position = it.toLatLng()), title = "Destination")
        }
        stops.forEachIndexed { index, point ->
            GoogleMarker(
                state = MarkerState(position = point.toLatLng()),
                title = when {
                    index < currentStopIndex -> "Passed Stop"
                    index == currentStopIndex -> "Next Stop"
                    else -> "Upcoming Stop"
                },
                alpha = if (index < currentStopIndex) 0.4f else 1.0f
            )
        }

        val remainingPoints = buildList {
            driverLocation?.let { add(it.toLatLng()) }
            for (i in currentStopIndex until stops.size) add(stops[i].toLatLng())
            destinationLocation?.let { add(it.toLatLng()) }
        }
        if (remainingPoints.size >= 2) {
            GooglePolyline(points = remainingPoints, color = UniRideNavy.copy(alpha = 0.6f))
        }
    }
}

// --- osmdroid fallback (unchanged) --------------------------------------

@Composable
private fun OsmMapSection(uiState: TrackingUiState) {
    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            driverLocation = uiState.driverLocation,
            destinationLocation = uiState.destinationLocation,
            stops = uiState.stops,
            currentStopIndex = uiState.currentStopIndex,
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
    stops: List<GeoPoint> = emptyList(),
    currentStopIndex: Int = 0,
    destinationLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val cartoDbPositron = remember {
        XYTileSource(
            "CartoDB Positron",
            1, 19, 256, ".png",
            arrayOf(
                "https://a.basemaps.cartocdn.com/light_all/",
                "https://b.basemaps.cartocdn.com/light_all/",
                "https://c.basemaps.cartocdn.com/light_all/",
                "https://d.basemaps.cartocdn.com/light_all/"
            ),
            "© OpenStreetMap contributors, © CARTO"
        )
    }

    val mapView = remember {
        Configuration.getInstance().userAgentValue = context.packageName
        MapView(context).apply {
            setTileSource(cartoDbPositron) // cleaner "light" tiles
            setMultiTouchControls(true)
            controller.setZoom(16.0)
        }
    }

    val driverMarker = remember {
        Marker(mapView).apply {
            icon = AppCompatResources.getDrawable(context, com.example.getyourride.R.drawable.ic_driver_marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }
    val destinationMarker = remember {
        Marker(mapView).apply {
            icon = AppCompatResources.getDrawable(context, com.example.getyourride.R.drawable.ic_destination_marker)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    }

    // Intermediate stops markers
    val stopMarkers = remember { mutableListOf<Marker>() }

    val routeLineTraveled = remember { Polyline(mapView).apply { outlinePaint.color = UniRideOrange.toArgb() } }
    val routeLineRemaining = remember {
        Polyline(mapView).apply {
            outlinePaint.color = UniRideNavy.copy(alpha = 0.6f).toArgb()
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    }

    DisposableEffect(Unit) {
        mapView.overlays.add(routeLineTraveled)
        mapView.overlays.add(routeLineRemaining)
        mapView.overlays.add(destinationMarker)
        mapView.overlays.add(driverMarker)
        onDispose {
            mapView.onDetach()
            stopMarkers.forEach { mapView.overlays.remove(it) }
        }
    }

    // Smooth Driver Marker Animation
    LaunchedEffect(driverLocation) {
        driverLocation?.let { target ->
            val start = driverMarker.position
            if (start == null || (start.latitude == 0.0 && start.longitude == 0.0)) {
                driverMarker.position = target
                mapView.controller.animateTo(target)
            } else {
                // Animate over 1.5s (slightly less than the 2s update interval)
                val duration = 1500L
                val startTime = System.currentTimeMillis()
                while (System.currentTimeMillis() - startTime < duration) {
                    val progress = (System.currentTimeMillis() - startTime).toFloat() / duration
                    val lat = start.latitude + (target.latitude - start.latitude) * progress
                    val lng = start.longitude + (target.longitude - start.longitude) * progress
                    driverMarker.position = GeoPoint(lat, lng)

                    // Update traveled route line to follow the marker
                    val traveledPoints = mutableListOf<GeoPoint>()
                    // Add all passed stops to the traveled line
                    for (i in 0 until currentStopIndex) {
                        if (i < stops.size) traveledPoints.add(stops[i])
                    }
                    traveledPoints.add(driverMarker.position)
                    routeLineTraveled.setPoints(traveledPoints)

                    mapView.invalidate()
                    kotlinx.coroutines.delay(16) // ~60fps
                }
                driverMarker.position = target
            }
        }
    }

    // Update markers and remaining route
    LaunchedEffect(driverLocation, destinationLocation, stops, currentStopIndex) {
        destinationLocation?.let { point ->
            destinationMarker.position = point
            destinationMarker.title = destinationLabel
        }

        // Update Stop Markers
        // Clear old stop markers from overlays
        stopMarkers.forEach { mapView.overlays.remove(it) }
        stopMarkers.clear()

        stops.forEachIndexed { index, point ->
            val marker = Marker(mapView).apply {
                position = point
                icon = AppCompatResources.getDrawable(context, com.example.getyourride.R.drawable.ic_stop_marker)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                // Dim passed stops
                if (index < currentStopIndex) {
                    alpha = 0.4f
                    title = "Passed Stop"
                } else if (index == currentStopIndex) {
                    alpha = 1.0f
                    title = "Next Stop"
                } else {
                    alpha = 0.8f
                    title = "Upcoming Stop"
                }
            }
            stopMarkers.add(marker)
            mapView.overlays.add(marker)
        }

        // Update remaining route line
        val remainingPoints = mutableListOf<GeoPoint>()
        driverLocation?.let { remainingPoints.add(it) }
        for (i in currentStopIndex until stops.size) {
            remainingPoints.add(stops[i])
        }
        destinationLocation?.let { remainingPoints.add(it) }
        routeLineRemaining.setPoints(remainingPoints)

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