package com.example.getyourride.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.getyourride.data.remote.dto.NotificationResponse
import com.example.getyourride.data.repository.NotificationRepository
import kotlinx.coroutines.launch

/**
 * Backs the notification bell + popup on the student's carpool home screen.
 * Holds the list of notifications and the unread count that drives the badge.
 */
class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    var notifications by mutableStateOf<List<NotificationResponse>>(emptyList())
        private set

    var unreadCount by mutableStateOf(0L)
        private set

    var isLoading by mutableStateOf(false)
        private set

    /** Loads notifications and refreshes the unread count. Safe to call on screen open. */
    fun load() {
        viewModelScope.launch {
            isLoading = true
            repository.getMyNotifications().onSuccess { list ->
                notifications = list
                unreadCount = list.count { !it.read }.toLong()
            }
            isLoading = false
        }
    }

    /** Lightweight refresh of just the unread count (e.g. for the badge on resume). */
    fun refreshUnreadCount() {
        viewModelScope.launch {
            repository.getUnreadCount().onSuccess { count ->
                unreadCount = count
            }
        }
    }

    /** Marks one notification read and updates local state without a full reload. */
    fun markAsRead(notificationId: Long) {
        viewModelScope.launch {
            repository.markAsRead(notificationId).onSuccess { updated ->
                notifications = notifications.map {
                    if (it.notificationId == updated.notificationId) updated else it
                }
                unreadCount = notifications.count { !it.read }.toLong()
            }
        }
    }
}

class NotificationViewModelFactory(
    private val repository: NotificationRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return NotificationViewModel(repository) as T
    }
}
