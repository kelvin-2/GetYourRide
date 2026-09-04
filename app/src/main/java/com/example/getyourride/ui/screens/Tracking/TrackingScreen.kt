package com.example.getyourride.ui.screens.Tracking

import android.content.Context
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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.getyourride.domain.model.RideStatus
import com.example.getyourride.domain.model.TripTrackingInfo
import com.example.getyourride.ui.components.GyrRoutes
import com.example.getyourride.ui.components.StudentLayout
import com.example.getyourride.ui.theme.*
import com.example.getyourride.viewmodel.TrackingData
import com.example.getyourride.viewmodel.TrackingUiState
import com.example.getyourride.viewmodel.TrackingViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.io.File

// Match these to your app's theme colors (Theme.kt) instead of hardcoding
private val UniRideOrange = Color(0xFFFF7A1A)
private val UniRideNavy = Color(0xFF141A33)
private val CardGrey = Color(0xFFF5F6F8)

/**
 * Fallback map centre (NMU South Campus, Gqeberha).
 *
 * osmdroid's MapView starts centred on lat/lng 0,0 — open ocean — so a map opened before
 * any position arrives renders as blank/near-empty tiles. Always seeding a centre is what
 * keeps the map looking like a map from the first frame.
 */
private val DefaultMapCenter = GeoPoint(-33.9581, 25.6014)
private const val DefaultZoom = 15.0

/**
 * Stateful entry point — wired into NavHost in MainActivity.
 *
 * The ViewModel is built and passed in by the caller so MainActivity controls which
 * socket implementation is used, and this screen doesn't need to know or care.
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
    when (uiState) {
        is TrackingUiState.Loading -> CenteredMessage {
            CircularProgressIndicator(color = UniRideOrange)
            Spacer(Modifier.height(16.dp))
            Text("Checking for active rides…", color = TextMuted, fontSize = 14.sp)
        }

        is TrackingUiState.NoRidesAvailable -> CenteredMessage {
            Icon(
                Icons.Filled.DirectionsBus,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No rides currently available to track.",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Book a ride to see its live position here.",
                textAlign = TextAlign.Center,
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        is TrackingUiState.Error -> CenteredMessage {
            Icon(
                Icons.Filled.ErrorOutline,
                contentDescription = null,
                tint = Color(0xFFE0483E),
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = uiState.message,
                textAlign = TextAlign.Center,
                color = TextMuted,
                fontSize = 14.sp
            )
        }

        is TrackingUiState.Active -> Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                OsmMapSection(data = uiState.data)
            }
            DriverInfoCard(
                info = uiState.data.tripInfo,
                onMessageDriver = onMessageDriver,
                onCallDriver = onCallDriver,
                onCancelRide = onCancelRide
            )
        }
    }
}

@Composable
private fun CenteredMessage(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { content() }
    }
}

@Composable
private fun OsmMapSection(data: TrackingData) {
    Box(modifier = Modifier.fillMaxSize()) {
        OsmMapView(
            driverLocation = data.driverLocation,
            destinationLocation = data.destinationLocation,
            stops = data.stops,
            currentStopIndex = data.currentStopIndex,
            destinationLabel = data.tripInfo.destinationLabel,
            modifier = Modifier.fillMaxSize()
        )

        // Destination chip, top-left over the map (mirrors "Library North" pill in the mockup)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(UniRideNavy, RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(data.tripInfo.destinationLabel, color = Color.White, fontSize = 13.sp)
        }

        // Live indicator, top-right over the map. Three states so the student can see at a glance
        // whether the trip is actually updating:
        //  - a network hiccup on a poll (connectionError set) -> "Reconnecting…"
        //  - waiting for the driver to start / first position   -> "Waiting for driver…"
        //  - a position is flowing in                           -> green "● LIVE"
        val (indicatorText, dotColor, bgColor) = when {
            data.connectionError != null ->
                Triple("Reconnecting…", Color(0xFFFFC107), UniRideNavy.copy(alpha = 0.85f))
            data.driverLocation == null ->
                Triple("Waiting for driver…", Color(0xFFFFC107), UniRideNavy.copy(alpha = 0.85f))
            else ->
                Triple("LIVE", Color(0xFF39D98A), UniRideNavy.copy(alpha = 0.85f))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(bgColor, RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(Modifier.width(6.dp))
            Text(text = indicatorText, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
    val lifecycleOwner = LocalLifecycleOwner.current

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
        configureOsmdroid(context)
        MapView(context).apply {
            setTileSource(cartoDbPositron)
            setMultiTouchControls(true)
            setUseDataConnection(true)
            // Seed a real centre + zoom before the first draw. Without this osmdroid sits at
            // 0,0 and the screen looks like a broken/blank map until a location arrives.
            controller.setZoom(DefaultZoom)
            controller.setCenter(driverLocation ?: destinationLocation ?: DefaultMapCenter)
        }
    }

    val driverMarker = remember {
        Marker(mapView).apply {
            icon = AppCompatResources.getDrawable(
                context,
                com.example.getyourride.R.drawable.ic_driver_marker
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        }
    }
    val destinationMarker = remember {
        Marker(mapView).apply {
            icon = AppCompatResources.getDrawable(
                context,
                com.example.getyourride.R.drawable.ic_destination_marker
            )
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        }
    }

    // Intermediate stops markers
    val stopMarkers = remember { mutableListOf<Marker>() }

    val routeLineTraveled = remember {
        Polyline(mapView).apply { outlinePaint.color = UniRideOrange.toArgb() }
    }
    val routeLineRemaining = remember {
        Polyline(mapView).apply {
            outlinePaint.color = UniRideNavy.copy(alpha = 0.6f).toArgb()
            outlinePaint.pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
        }
    }

    DisposableEffect(Unit) {
        mapView.overlays.add(routeLineTraveled)
        mapView.overlays.add(routeLineRemaining)
        onDispose {
            stopMarkers.forEach { mapView.overlays.remove(it) }
            stopMarkers.clear()
            mapView.overlays.clear()
            mapView.onDetach()
        }
    }

    // osmdroid's MapView is a plain Android View: it needs onResume/onPause to restart its
    // tile-downloader threads and re-read config. Skipping this leaves the map frozen or
    // blank after the screen has been backgrounded.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.onResume()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onPause()
        }
    }

    // Smooth Driver Marker Animation
    LaunchedEffect(driverLocation) {
        val target = driverLocation ?: return@LaunchedEffect

        // Only attach the driver marker once it has a genuine position, otherwise osmdroid
        // draws it at 0,0 (Marker's default) which looks like a misplaced marker.
        if (!mapView.overlays.contains(driverMarker)) {
            driverMarker.position = target
            mapView.overlays.add(driverMarker)
            mapView.controller.animateTo(target)
            mapView.invalidate()
            return@LaunchedEffect
        }

        val start = driverMarker.position
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
            for (i in 0 until currentStopIndex) {
                if (i < stops.size) traveledPoints.add(stops[i])
            }
            traveledPoints.add(driverMarker.position)
            routeLineTraveled.setPoints(traveledPoints)

            mapView.invalidate()
            kotlinx.coroutines.delay(16) // ~60fps
        }
        driverMarker.position = target
        mapView.invalidate()
    }

    // Update markers and remaining route
    LaunchedEffect(driverLocation, destinationLocation, stops, currentStopIndex) {
        if (destinationLocation != null) {
            destinationMarker.position = destinationLocation
            destinationMarker.title = destinationLabel
            if (!mapView.overlays.contains(destinationMarker)) {
                mapView.overlays.add(destinationMarker)
            }
        } else {
            mapView.overlays.remove(destinationMarker)
        }

        // Update Stop Markers
        stopMarkers.forEach { mapView.overlays.remove(it) }
        stopMarkers.clear()

        stops.forEachIndexed { index, point ->
            val marker = Marker(mapView).apply {
                position = point
                icon = AppCompatResources.getDrawable(
                    context,
                    com.example.getyourride.R.drawable.ic_stop_marker
                )
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)

                // Dim passed stops
                when {
                    index < currentStopIndex -> {
                        alpha = 0.4f
                        title = "Passed Stop"
                    }
                    index == currentStopIndex -> {
                        alpha = 1.0f
                        title = "Next Stop"
                    }
                    else -> {
                        alpha = 0.8f
                        title = "Upcoming Stop"
                    }
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

    Box(modifier = modifier) {
        AndroidView(
            factory = { mapView },
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
        )

        // Recenters on the whole route (driver + stops + destination). Deliberately not the
        // device location: the app holds no location permission, so that would silently no-op.
        FloatingActionButton(
            onClick = {
                val points = buildList {
                    driverLocation?.let { add(it) }
                    addAll(stops)
                    destinationLocation?.let { add(it) }
                }
                when {
                    points.size > 1 ->
                        mapView.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), true, 96)
                    points.size == 1 -> mapView.controller.animateTo(points.first())
                    else -> mapView.controller.animateTo(DefaultMapCenter)
                }
            },
            containerColor = Color.White,
            contentColor = UniRideOrange,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Filled.MyLocation, contentDescription = "Recenter map on route")
        }
    }
}

/**
 * osmdroid needs its config loaded before the first MapView is created.
 *
 * The default base path lives on external storage, which app processes cannot write to on
 * API 29+; the tile cache then fails to open and no tiles are ever rendered. Pointing it at
 * app-private cache dirs fixes the blank map and needs no storage permission.
 */
private fun configureOsmdroid(context: Context) {
    Configuration.getInstance().apply {
        load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        userAgentValue = context.packageName // CARTO/OSM tile servers reject blank user agents
        osmdroidBasePath = File(context.cacheDir, "osmdroid").apply { mkdirs() }
        osmdroidTileCache = File(osmdroidBasePath, "tiles").apply { mkdirs() }
    }
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
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = UniRideOrange,
                            modifier = Modifier.size(14.dp)
                        )
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
                    // carYear is 0 when the backend didn't supply one — don't render "• 0".
                    subtitle = if (info.carYear > 0) {
                        "${info.carColor} • ${info.carYear}"
                    } else {
                        info.carColor
                    },
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

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onMessageDriver,
                    colors = ButtonDefaults.buttonColors(containerColor = UniRideOrange),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
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
                    Icon(
                        Icons.Filled.Cancel,
                        contentDescription = "Cancel ride",
                        tint = Color(0xFFE0483E)
                    )
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

// --- Previews ----------------------------------------------------------
// Sample data below is private to this file and only referenced from @Preview functions,
// so it is not reachable from the runtime path.

private val previewData = TrackingData(
    tripId = 42L,
    driverLocation = GeoPoint(-33.9581, 25.6014),      // sample NMU South Campus-ish coords
    destinationLocation = GeoPoint(-33.9615, 25.6089),
    stops = listOf(
        GeoPoint(-33.9590, 25.6030),
        GeoPoint(-33.9600, 25.6050)
    ),
    stopIds = listOf(1L, 2L),
    currentStopIndex = 1,
    isConnected = true,
    connectionError = null,
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
    TrackingScreenContent(uiState = TrackingUiState.Active(previewData))
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Connecting state")
@Composable
private fun TrackingScreenConnectingPreview() {
    TrackingScreenContent(
        uiState = TrackingUiState.Active(
            previewData.copy(
                isConnected = false,
                driverLocation = null,
                tripInfo = previewData.tripInfo.copy(etaMinutes = null)
            )
        )
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "No rides to track")
@Composable
private fun TrackingScreenEmptyPreview() {
    TrackingScreenContent(uiState = TrackingUiState.NoRidesAvailable)
}

@Preview(showBackground = true, widthDp = 360, heightDp = 780, name = "Error")
@Composable
private fun TrackingScreenErrorPreview() {
    TrackingScreenContent(uiState = TrackingUiState.Error("Could not check for active rides."))
}
