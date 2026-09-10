package com.example.getyourride.data.remote.dto

/**
 * A single in-app notification, matching the backend NotificationResponse field-for-field.
 * createdAt is kept as a String because the backend serializes LocalDateTime as an ISO string;
 * the UI formats it for display.
 */
data class NotificationResponse(
    val notificationId: Long,
    val message: String,
    val tripId: Long?,
    val type: String,
    val read: Boolean,
    val createdAt: String?
)
