package com.example.getyourride.data.remote.api

import com.example.getyourride.data.remote.dto.NotificationResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

/**
 * Student-facing notification endpoints. The auth token is attached automatically by the
 * OkHttp interceptor in NetworkModule, so the backend resolves the caller from the JWT.
 */
interface NotificationApi {

    /** GET /api/notifications — my notifications, newest first. */
    @GET("api/notifications")
    suspend fun getMyNotifications(): Response<List<NotificationResponse>>

    /** GET /api/notifications/unread-count — number of my unread notifications. */
    @GET("api/notifications/unread-count")
    suspend fun getUnreadCount(): Response<Long>

    /** PATCH /api/notifications/{id}/read — mark one of my notifications as read. */
    @PATCH("api/notifications/{id}/read")
    suspend fun markAsRead(@Path("id") notificationId: Long): Response<NotificationResponse>
}
