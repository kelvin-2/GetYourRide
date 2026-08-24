// ─────────────────────────────────────────────────────────────────────────────
// TripMapper.kt
// Package: com.example.getyourride.data.mapper
//
// PURPOSE — Converts TripResponse / TripBookingResponse (raw API data) into UI models.
//
// Four mappers live here:
//   toCarpoolRide()        → CarpoolRide       used by CarpoolHomeScreen available rides
//   toRideRequestDetails() → RideRequestDetails used by RequestRideScreen booking flow
//   toRideCardData()       → RideCardData      (TripResponse variant) used by driver "my trips"
//   toRideCardData()       → RideCardData      (TripBookingResponse variant) used by MyRidesScreen
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.getyourride.data.remote.dto.TripBookingResponse
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.components.RideCardData
import com.example.getyourride.ui.components.RideStatus
import com.example.getyourride.ui.components.RideStopInfo
import com.example.getyourride.ui.screens.Carpool.components.CarpoolRide
import com.example.getyourride.ui.screens.Rides.RideRequestDetails
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ⚠️ Confirm this matches your backend's actual TripType enum value.
// CreateTripRequest.kt's comment lists "SHUTTLE" / "STUDENT_DRIVER" — if
// carpool trips are actually saved as "STUDENT_DRIVER", change this constant.
const val CARPOOL_TRIP_TYPE = "CARPOOL"

@RequiresApi(Build.VERSION_CODES.O)
private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

@RequiresApi(Build.VERSION_CODES.O)
private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

// ─── 1. TripResponse → CarpoolRide (available rides on home screen) ───────────

@RequiresApi(Build.VERSION_CODES.O)
fun TripResponse.toCarpoolRide(): CarpoolRide {
    val initials = driverName
        ?.trim()
        ?.split(" ")
        ?.filter { it.isNotBlank() }
        ?.take(2)
        ?.joinToString("") { it.first().uppercase() }
        ?: "??"

    val formattedTime = try {
        LocalDateTime.parse(departureTime).format(timeFormatter)
    } catch (e: DateTimeParseException) {
        departureTime // fall back to raw string rather than crashing the screen
    }

    return CarpoolRide(
        driverName     = driverName ?: "Unknown Driver",
        driverInitials = initials,
        rating         = 5.0,   // placeholder — no rating field on backend yet
        ratingCount    = 0,     // placeholder — same reason
        seatsLeft      = availableSeats,
        departureTime  = formattedTime,
        fromLocation   = departureStop,
        toLocation     = destinationStop,
        pricePerSeat   = "R${price.setScale(2)}",
    )
}

// ─── 2. TripResponse → RideRequestDetails (booking flow on RequestRideScreen) ─

@RequiresApi(Build.VERSION_CODES.O)
fun TripResponse.toRideRequestDetails(): RideRequestDetails {
    return RideRequestDetails(
        tripId = tripId,
        driverName = driverName ?: "Driver",
        driverRating = 4.8,           // TODO: not in TripResponse yet — backend needs to add this
        driverRidesCompleted = 0,     // TODO: not in TripResponse yet — backend needs to add this
        driverAvatarUrl = null,
        carDescription = listOfNotNull(vehicleColour, vehicleModel)
            .joinToString(" ")
            .ifBlank { "Vehicle details unavailable" },
        plate = registrationNumber ?: "—",
        pickupLabel = departureStop,
        pickupLat = departureLat,
        pickupLng = departureLng,
        destinationLabel = destinationStop,
        destinationLat = destinationLat,
        destinationLng = destinationLng,
        departureTime = departureTime,   // still a raw ISO string — format this for display, see note below
        arrivalEstimate = arrivalTime ?: "—",
        seatsAvailable = availableSeats,
        pricePerSeat = price.toDouble(),
    )
}

// ─── 3. TripResponse → RideCardData (driver's posted trips) ──────────────────

@RequiresApi(Build.VERSION_CODES.O)
fun TripResponse.toRideCardData(): RideCardData {
    val dateTime = try {
        LocalDateTime.parse(departureTime)
    } catch (e: DateTimeParseException) {
        null
    }

    return RideCardData(
        id = tripId.toString(),
        driverName = driverName ?: "Unknown Driver",
        carDescription = listOfNotNull(vehicleColour, vehicleModel)
            .joinToString(" ")
            .ifBlank { "Unknown Vehicle" },
        plate = registrationNumber ?: "—",
        status = when (status.lowercase()) {
            "scheduled" -> RideStatus.SCHEDULED
            "active"    -> RideStatus.ACTIVE
            "completed" -> RideStatus.COMPLETED
            "cancelled" -> RideStatus.CANCELLED
            else        -> RideStatus.SCHEDULED
        },
        pickup  = departureStop,
        dropoff = destinationStop,
        dateLabel = dateTime?.format(dateFormatter) ?: departureTime.take(10),
        timeLabel = dateTime?.format(timeFormatter) ?: departureTime.takeLast(8),
        slotTime = slotTime,
        vehicleCapacity = vehicleCapacity,
        stops = stops.map {
            RideStopInfo(
                stopName = it.stopName,
                stopOrder = it.stopOrder,
                studentName = it.studentName,
            )
        },
    )
}

// ─── 4. TripBookingResponse → RideCardData (booked rides on MyRidesScreen) ───

@RequiresApi(Build.VERSION_CODES.O)
fun TripBookingResponse.toRideCardData(): RideCardData {
    val trip = this.trip

    val dateTime = try {
        LocalDateTime.parse(trip.departureTime)
    } catch (e: DateTimeParseException) {
        null
    }

    return RideCardData(
        id = bookingId.toString(),
        driverName = trip.driverName ?: "Unknown Driver",
        carDescription = listOfNotNull(trip.vehicleColour, trip.vehicleModel)
            .joinToString(" ")
            .ifBlank { "Unknown Vehicle" },
        plate = trip.registrationNumber ?: "—",
        status = when (bookingStatus?.lowercase()) {
            "confirmed" -> RideStatus.ACTIVE
            "pending"   -> RideStatus.SCHEDULED
            "cancelled" -> RideStatus.CANCELLED
            "completed" -> RideStatus.COMPLETED
            else        -> RideStatus.SCHEDULED
        },
        pickup  = trip.departureStop,
        dropoff = trip.destinationStop,
        dateLabel = dateTime?.format(dateFormatter) ?: trip.departureTime.take(10),
        timeLabel = dateTime?.format(timeFormatter) ?: trip.departureTime.takeLast(8),
        slotTime = trip.slotTime,
        vehicleCapacity = trip.vehicleCapacity,
        stops = trip.stops.map {
            RideStopInfo(
                stopName = it.stopName,
                stopOrder = it.stopOrder,
                studentName = it.studentName,
            )
        },
    )
}