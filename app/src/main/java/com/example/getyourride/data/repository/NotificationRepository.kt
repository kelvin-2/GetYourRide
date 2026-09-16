package com.example.getyourride.data.repository

import com.example.getyourride.data.remote.api.NotificationApi
import com.example.getyourride.data.remote.dto.NotificationResponse

/**
 * Thin wrapper over NotificationApi that surfaces results as Kotlin [Result]s.
 */
class NotificationRepository(
    private val api: NotificationApi
) {
    suspend fun getMyNotifications(): Result<List<NotificationResponse>> {
        return try {
            val response = api.getMyNotifications()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Failed to load notifications (${response.code()})."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUnreadCount(): Result<Long> {
        return try {
            val response = api.getUnreadCount()
            if (response.isSuccessful) {
                Result.success(response.body() ?: 0L)
            } else {
                Result.failure(Exception("Failed to load unread count (${response.code()})."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: Long): Result<NotificationResponse> {
        return try {
            val response = api.markAsRead(notificationId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to mark notification read (${response.code()})."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
