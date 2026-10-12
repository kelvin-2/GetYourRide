package com.example.getyourride.data.remote.dto

import java.time.LocalTime

/**
 * Parses slot times such as "06:45:00", "06:45", "08:00 AM" or "12:30 PM" into a LocalTime.
 * Returns null when the text can't be understood.
 */
private fun parseSlotTime(raw: String): LocalTime? {
    val text = raw.trim().uppercase()
    val isPm = text.endsWith("PM")
    val isAm = text.endsWith("AM")
    val parts = text.removeSuffix("PM").removeSuffix("AM").trim().split(":")
    val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: return null
    val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
    val hour24 = when {
        isPm && hour < 12 -> hour + 12
        isAm && hour == 12 -> 0
        else -> hour
    }
    return try {
        LocalTime.of(hour24, minute)
    } catch (e: Exception) {
        null
    }
}

/**
 * True when this slot's departure time has already passed today. Shuttles can only be booked
 * for the current day, so a past slot can never be booked. Unparseable times are treated as
 * still bookable so a formatting quirk never blocks a student.
 */
fun ShuttleTimeSlot.hasDepartedToday(now: LocalTime = LocalTime.now()): Boolean {
    val departure = parseSlotTime(departs) ?: return false
    return !departure.isAfter(now)
}
