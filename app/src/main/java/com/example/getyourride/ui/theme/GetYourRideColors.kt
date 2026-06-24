package com.example.getyourride.ui.theme

import androidx.compose.ui.graphics.Color

// ─── Brand Palette ───────────────────────────────────────────────────────────
val NavyPrimary    = Color(0xFF1A2E5A)  // Deep Navy Blue  – primary actions, headers
val OrangeAccent   = Color(0xFFF57C00)  // Bright Orange   – CTA buttons, active states
val GreenSuccess   = Color(0xFF2E7D32)  // Green           – success states
val SurfaceGrey    = Color(0xFFF5F6FA)  // Light Grey      – screen backgrounds
val CardWhite      = Color(0xFFFFFFFF)  // White           – card backgrounds
val DangerRed      = Color(0xFFC62828)  // Red             – destructive actions

// ─── Text ────────────────────────────────────────────────────────────────────
val TextPrimary    = Color(0xFF1A2E5A)
val TextMuted      = Color(0xFF1A2E5A).copy(alpha = 0.6f)
val TextHint       = Color(0xFF9E9E9E)

// ─── Status badges ───────────────────────────────────────────────────────────
val StatusPending   = Color(0xFFFFC107)
val StatusActive    = GreenSuccess
val StatusCancelled = DangerRed
val StatusCompleted = Color(0xFF757575)

// ─── Neutral ─────────────────────────────────────────────────────────────────
val BorderLight    = Color(0xFFE0E0E0)
val IconTint       = Color(0xFF757575)