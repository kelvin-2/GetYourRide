package com.example.getyourride.ui.screens.Tracking

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
// Data layer (TrackingViewModel, mappers) still speaks in osmdroid's GeoPoint — kept as-is so
// swapping the map renderer doesn't ripple into the ViewModel/mapper layer. Converted to
// Google's LatLng only at the render boundary below.
import org.osmdroid.util.GeoPoint
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI
import kotlinx.coroutines.launch

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
        TrackingScreenContent(uiState = uiState)
    }
}

/**
 * Stateless UI — no ViewModel, no socket, just data in. This is what makes
 * the screen previewable, since @Preview can't satisfy a real STOMP connection.
 */
@Composable
fun TrackingScreenContent(
    uiState: TrackingUiState
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
            DriverInfoCard(info = uiState.data.tripInfo)
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
        GoogleTrackingMapView(
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
private fun GoogleTrackingMapView(
    driverLocation: GeoPoint?,
    destinationLocation: GeoPoint?,
    stops: List<GeoPoint> = emptyList(),
    currentStopIndex: Int = 0,
    destinationLabel: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // BitmapDescriptorFactory (used below to build marker icons) throws
    // "IBitmapDescriptorFactory is not initialized" if called before the Maps SDK has been
    // set up — which normally only happens once a MapView/GoogleMap actually attaches.
    // Forcing it here, synchronously, before the icon `remember` blocks run guarantees it's
    // ready in time. Safe to call repeatedly; it's a no-op after the first successful call.
    remember { com.google.android.gms.maps.MapsInitializer.initialize(context.applicationContext) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            (driverLocation ?: destinationLocation ?: DefaultMapCenter).toLatLng(),
            DefaultZoom.toFloat()
        )
    }

    val carIcon = remember {
        bitmapDescriptorFromVector(context, com.example.getyourride.R.drawable.ic_driver_marker)
    }
    val destinationIcon = remember {
        bitmapDescriptorFromVector(context, com.example.getyourride.R.drawable.ic_destination_marker)
    }
    val stopIcon = remember {
        bitmapDescriptorFromVector(context, com.example.getyourride.R.drawable.ic_stop_marker)
    }

    // Animated + rotated driver position. Animatable drives both the marker's LatLng and the
    // "traveled" polyline every frame, same idea as the old osmdroid while-loop but expressed
    // as a proper Compose animation instead of a manual coroutine clock.
    val animatedLat = remember { Animatable((driverLocation?.latitude ?: DefaultMapCenter.latitude).toFloat()) }
    val animatedLng = remember { Animatable((driverLocation?.longitude ?: DefaultMapCenter.longitude).toFloat()) }
    var bearing by remember { mutableStateOf(0f) }
    var hasPlacedDriver by remember { mutableStateOf(false) }

    LaunchedEffect(driverLocation) {
        val target = driverLocation ?: return@LaunchedEffect

        if (!hasPlacedDriver) {
            // First position: snap, don't animate from the default centre.
            animatedLat.snapTo(target.latitude.toFloat())
            animatedLng.snapTo(target.longitude.toFloat())
            hasPlacedDriver = true
            return@LaunchedEffect
        }

        val start = LatLng(animatedLat.value.toDouble(), animatedLng.value.toDouble())
        bearing = bearingBetween(start, target.toLatLng())

        // 1.5s, slightly under the ~2s backend tick, so movement finishes before the next update.
        launch {
            animatedLat.animateTo(target.latitude.toFloat(), animationSpec = tween(1500, easing = LinearEasing))
        }
        animatedLng.animateTo(target.longitude.toFloat(), animationSpec = tween(1500, easing = LinearEasing))
    }

    // Camera auto-follow: recentres on the vehicle as it moves. The FAB below still lets the
    // student zoom out to the whole route without the camera immediately snapping back — it only
    // re-engages follow on the next position update.
    LaunchedEffect(driverLocation) {
        val target = driverLocation ?: return@LaunchedEffect
        cameraPositionState.animate(
            update = com.google.android.gms.maps.CameraUpdateFactory.newLatLng(target.toLatLng())
        )
    }

    // Before the driver starts (no live position yet) there's no vehicle to follow, so frame the
    // route context instead: destination + any stops. This is what makes "Track" on a confirmed
    // upcoming ride land on a useful map (the destination and route) rather than a bare default
    // centre. Runs only while driverLocation is null; once a position arrives, the follow effect
    // above takes over.
    LaunchedEffect(driverLocation, destinationLocation, stops) {
        if (driverLocation != null) return@LaunchedEffect
        val routePoints = buildList {
            addAll(stops.map { it.toLatLng() })
            destinationLocation?.let { add(it.toLatLng()) }
        }
        when {
            routePoints.size > 1 -> {
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                routePoints.forEach { boundsBuilder.include(it) }
                cameraPositionState.move(
                    com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(
                        boundsBuilder.build(), 120
                    )
                )
            }
            routePoints.size == 1 -> cameraPositionState.animate(
                update = com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                    routePoints.first(), DefaultZoom.toFloat()
                )
            )
        }
    }

    val animatedDriverLatLng = LatLng(animatedLat.value.toDouble(), animatedLng.value.toDouble())

    val traveledPoints = remember(animatedDriverLatLng, currentStopIndex) {
        buildList {
            for (i in 0 until currentStopIndex) {
                if (i < stops.size) add(stops[i].toLatLng())
            }
            if (hasPlacedDriver) add(animatedDriverLatLng)
        }
    }
    val remainingPoints = remember(driverLocation, currentStopIndex, stops, destinationLocation) {
        buildList {
            driverLocation?.let { add(it.toLatLng()) }
            for (i in currentStopIndex until stops.size) add(stops[i].toLatLng())
            destinationLocation?.let { add(it.toLatLng()) }
        }
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(isMyLocationEnabled = false),
            uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)
        ) {
            if (traveledPoints.size >= 2) {
                Polyline(points = traveledPoints, color = UniRideOrange, width = 6f)
            }
            if (remainingPoints.size >= 2) {
                Polyline(
                    points = remainingPoints,
                    color = UniRideNavy.copy(alpha = 0.6f),
                    width = 5f,
                    pattern = listOf(
                        com.google.android.gms.maps.model.Dash(20f),
                        com.google.android.gms.maps.model.Gap(14f)
                    )
                )
            }

            stops.forEachIndexed { index, point ->
                Marker(
                    state = MarkerState(position = point.toLatLng()),
                    icon = stopIcon,
                    alpha = when {
                        index < currentStopIndex -> 0.4f
                        index == currentStopIndex -> 1.0f
                        else -> 0.8f
                    },
                    title = when {
                        index < currentStopIndex -> "Passed Stop"
                        index == currentStopIndex -> "Next Stop"
                        else -> "Upcoming Stop"
                    },
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                )
            }

            destinationLocation?.let { dest ->
                Marker(
                    state = MarkerState(position = dest.toLatLng()),
                    icon = destinationIcon,
                    title = destinationLabel,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 1.0f)
                )
            }

            if (hasPlacedDriver) {
                Marker(
                    state = MarkerState(position = animatedDriverLatLng),
                    icon = carIcon,
                    rotation = bearing,
                    flat = true,
                    anchor = androidx.compose.ui.geometry.Offset(0.5f, 0.5f)
                )
            }
        }

        // Recenters on the whole route (driver + stops + destination). Deliberately not the
        // device location: the app holds no location permission, so that would silently no-op.
        FloatingActionButton(
            onClick = {
                val points = buildList {
                    driverLocation?.let { add(it.toLatLng()) }
                    addAll(stops.map { it.toLatLng() })
                    destinationLocation?.let { add(it.toLatLng()) }
                }
                val boundsBuilder = com.google.android.gms.maps.model.LatLngBounds.Builder()
                when {
                    points.size > 1 -> {
                        points.forEach { boundsBuilder.include(it) }
                        cameraPositionState.move(
                            com.google.android.gms.maps.CameraUpdateFactory.newLatLngBounds(
                                boundsBuilder.build(), 96
                            )
                        )
                    }
                    points.size == 1 -> cameraPositionState.move(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLng(points.first())
                    )
                    else -> cameraPositionState.move(
                        com.google.android.gms.maps.CameraUpdateFactory.newLatLng(DefaultMapCenter.toLatLng())
                    )
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

private fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

/** Compass bearing in degrees (0-360, 0 = north) from [start] to [end], for rotating the car icon. */
private fun bearingBetween(start: LatLng, end: LatLng): Float {
    val lat1 = start.latitude * PI / 180.0
    val lat2 = end.latitude * PI / 180.0
    val dLng = (end.longitude - start.longitude) * PI / 180.0
    val y = sin(dLng) * cos(lat2)
    val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLng)
    val bearingRad = atan2(y, x)
    val bearingDeg = bearingRad * 180.0 / PI
    return ((bearingDeg + 360) % 360).toFloat()
}

/**
 * Renders a vector drawable to a [BitmapDescriptor] for use as a Google Maps marker icon.
 *
 * The Maps SDK's Marker only accepts bitmap-backed icons, not a Composable or a vector
 * resource directly, so this rasterises it once (memoised via `remember` at each call site)
 * rather than on every recomposition.
 */
private fun bitmapDescriptorFromVector(context: android.content.Context, resId: Int): BitmapDescriptor {
    val drawable = AppCompatResources.getDrawable(context, resId)
        ?: return BitmapDescriptorFactory.defaultMarker()
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth.coerceAtLeast(1),
        drawable.intrinsicHeight.coerceAtLeast(1),
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

@Composable
private fun DriverInfoCard(
    info: TripTrackingInfo
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