// ─────────────────────────────────────────────────────────────────────────────
// TripMapper.kt
// Package: com.example.getyourride.data.mapper
//
// PURPOSE — Converts TripResponse (raw API data) into UI models.
//
// Two mappers live here:
//   toCarpoolRide()   → CarpoolRide  used by CarpoolHomeScreen available rides
//   toRideCardData()  → RideCardData used by MyRidesScreen booked rides
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.mapper

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.components.RideCardData
import com.example.getyourride.ui.components.RideStatus
import com.example.getyourride.ui.screens.Carpool.components.CarpoolRide
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

// ─── 2. TripResponse → RideCardData (booked rides on MyRidesScreen) ──────────

@RequiresApi(Build.VERSION_CODES.O)
fun TripResponse.toRideCardData(): RideCardData {
    // Parse the ISO datetime once — used for both date and time labels
    val dateTime = try {
        LocalDateTime.parse(departureTime)
    } catch (e: DateTimeParseException) {
        null
    }

    return RideCardData(
        id = tripId.toString(),

        driverName = driverName ?: "Unknown Driver",

        // Combine colour + model → "Black Ford Fiesta"
        // Falls back gracefully if either field is null
        carDescription = listOfNotNull(vehicleColour, vehicleModel)
            .joinToString(" ")
            .ifBlank { "Unknown Vehicle" },

        plate = registrationNumber ?: "—",

        // Map backend status string → RideStatus enum
        // Backend sends: "Scheduled", "Active", "Completed", "Cancelled"
        status = when (status.lowercase()) {
            "scheduled" -> RideStatus.SCHEDULED
            "active"    -> RideStatus.ACTIVE
            "completed" -> RideStatus.COMPLETED
            "cancelled" -> RideStatus.CANCELLED
            else        -> RideStatus.SCHEDULED
        },

        pickup  = departureStop,
        dropoff = destinationStop,

        // "2026-07-02T07:00:00" → "02 Jul 2026"
        dateLabel = dateTime?.format(dateFormatter) ?: departureTime.take(10),

        // "2026-07-02T07:00:00" → "07:00 AM"
        timeLabel = dateTime?.format(timeFormatter) ?: departureTime.takeLast(8),
    )
}