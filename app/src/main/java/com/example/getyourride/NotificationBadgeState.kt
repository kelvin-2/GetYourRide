package com.example.getyourride

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Small app-wide holder for the student's unread-notification count.
 *
 * The bottom navigation bar (GyrBottomNav) observes this to show a badge on the
 * Rides tab, while the Rides screen updates it after loading or reading
 * notifications. Keeping it here means the nav bar doesn't need the count threaded
 * through every student screen.
 *
 * In-memory only — resets when the app process is killed, same as UserSession.
 */
object NotificationBadgeState {
    var unreadCount by mutableStateOf(0L)

    fun clear() {
        unreadCount = 0L
    }
}
