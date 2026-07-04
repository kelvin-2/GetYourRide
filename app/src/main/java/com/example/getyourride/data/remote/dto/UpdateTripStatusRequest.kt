package com.example.getyourride.data.remote.dto

// Request body sent to PUT /api/trips/{id}/status
// Matches whatever field your Spring Boot controller expects on the
// request DTO — currently just the new status value, e.g. "CANCELLED".
data class UpdateTripStatusRequest(
    val status: String
)