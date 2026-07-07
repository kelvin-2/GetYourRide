package com.example.getyourride.data.remote.dto

data class BookCarpoolRequest(
    val pickupStop: TripStopRequest,
    val dropOffStop: TripStopRequest? = null
)