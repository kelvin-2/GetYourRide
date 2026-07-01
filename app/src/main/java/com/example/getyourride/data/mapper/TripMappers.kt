package com.example.getyourride.data.mapper

import com.example.getyourride.data.remote.dto.TripResponse
import com.example.getyourride.ui.screens.Carpool.components.CarpoolRide
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

// ⚠️ Confirm this matches your backend's actual TripType enum value.
// CreateTripRequest.kt's comment lists "SHUTTLE" / "STUDENT_DRIVER" — if
// carpool trips are actually saved as "STUDENT_DRIVER", change this constant.
const val CARPOOL_TRIP_TYPE = "CARPOOL"

private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a")

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