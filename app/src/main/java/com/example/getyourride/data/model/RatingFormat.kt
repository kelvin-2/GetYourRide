package com.example.getyourride.data.model

/** Driver rating label: "New" when the backend returns null (unrated), otherwise one decimal (0.0 – 5.0). */
fun formatDriverRating(rating: Double?): String =
    if (rating == null) "New" else String.format(java.util.Locale.US, "%.1f", rating)
