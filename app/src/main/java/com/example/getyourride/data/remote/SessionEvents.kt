// ─────────────────────────────────────────────────────────────────────────────
// SessionEvents.kt
// Package: com.example.getyourride.data.remote
//
// PURPOSE — Lets the network layer tell the UI "the JWT was rejected".
// MainActivity collects [expired], clears UserSession and goes to login.
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride.data.remote

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

object SessionEvents {
    private val _expired = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val expired: SharedFlow<Unit> = _expired

    fun notifyExpired() {
        _expired.tryEmit(Unit)
    }
}