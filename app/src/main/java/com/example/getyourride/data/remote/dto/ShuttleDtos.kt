package com.example.getyourride.data.remote.dto

data class ShuttleStopResponse(
    val stopId: Int,
    val stopName: String,
    val area: String,
    val location: String,
    val latitude: Double?,
    val longitude: Double?
)

data class ShuttleTimeSlotResponse(
    val slotId: Int,
    val period: String,
    val departs: String,
    val arrives: String
)
