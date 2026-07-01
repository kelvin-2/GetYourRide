// ─────────────────────────────────────────────────────────────────────────────
// UserSession.kt
// Package: com.example.getyourride
//
// PURPOSE — Holds the logged-in user's data for the entire app session.
//
// Think of this as your "global user state" — once the student logs in,
// their data is stored here and any screen can read it.
//
// HOW TO READ from any screen:
//   val user = UserSession.current   // null if not logged in
//   Text("Hello ${user?.firstName}") // safe null check
//
// HOW TO CLEAR on logout:
//   UserSession.clear()
//
// ⚠️ This is an in-memory store — it clears when the app is killed.
// For persistence across app restarts, we'd use DataStore (future TODO).
// ─────────────────────────────────────────────────────────────────────────────

package com.example.getyourride

import com.example.getyourride.data.remote.dto.AuthResponse

object UserSession {

    // The full response from login/signup — null means no one is logged in
    var current: AuthResponse? = null
        private set

    // JWT token — sent with every authenticated API request
    val token: String? get() = current?.token

    // Quick access shortcuts — used directly in screen composables
    val firstName:  String?  get() = current?.firstName
    val lastName:   String?  get() = current?.lastName
    val fullName:   String?  get() = "${current?.firstName} ${current?.lastName}".trim()
    val email:      String?  get() = current?.email
    val id:         Long?    get() = current?.id
    val isFunded:   Boolean  get() = current?.isFunded ?: false
    val isStudent:  Boolean  get() = current?.type == "STUDENT"
    val isDriver:   Boolean  get() = current?.type == "DRIVER"

    // Called from MainActivity after successful login/signup
    fun save(response: AuthResponse) {
        current = response
    }

    // Called on logout — clears everything
    fun clear() {
        current = null
    }
}