package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.NotificationApi
import com.example.getyourride.data.remote.dto.NotificationResponse
import com.example.getyourride.data.remote.safeApiCall
import com.example.getyourride.data.remote.toResult

/**
 * Thin wrapper over NotificationApi that surfaces results as Kotlin [Result]s.
 * Failures carry an ApiException whose message is already user-friendly.
 */
class NotificationRepository(
    private val api: NotificationApi
) {
    suspend fun getMyNotifications(): Result<List<NotificationResponse>> =
        safeApiCall<List<NotificationResponse>>(onEmptyBody = { emptyList() }) {
            api.getMyNotifications()
        }.toResult()

    suspend fun getUnreadCount(): Result<Long> =
        safeApiCall<Long>(onEmptyBody = { 0L }) { api.getUnreadCount() }.toResult()

    suspend fun markAsRead(notificationId: Long): Result<NotificationResponse> =
        safeApiCall { api.markAsRead(notificationId) }.toResult()
}