package com.example.getyourride.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.getyourride.data.remote.dto.TripReviewDetailResponse
import com.example.getyourride.viewmodel.TripReviewsUiState

// ── Colors (matching the driver screens) ────────────────────────────────────
private val ReviewBackground = Color(0xFFF4F6FB)
private val ReviewPrimary = Color(0xFF1A2E5A)
private val ReviewTopBar = Color(0xFF1A2E5A)
private val ReviewAccent = Color(0xFFFC820C)
private val ReviewCardBackground = Color(0xFFFFFFFF)
private val ReviewText = Color(0xFF1B1B1F)
private val ReviewTextMuted = Color(0xFF5E6278)
private val ReviewError = Color(0xFFDC2626)
private val ReviewStar = Color(0xFFF5A623)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripReviewsScreen(
    uiState: TripReviewsUiState,
    onBackClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                title = {
                    Text(
                        text = "Trip Ratings",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReviewTopBar)
            )
        },
        containerColor = ReviewBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState) {
                is TripReviewsUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ReviewAccent)
                    }
                }

                is TripReviewsUiState.Error -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ErrorOutline,
                            contentDescription = null,
                            tint = ReviewError,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = uiState.message,
                            color = ReviewTextMuted,
                            fontSize = 14.sp
                        )
                    }
                }

                is TripReviewsUiState.Success -> {
                    if (uiState.reviews.isEmpty()) {
                        EmptyReviews()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            items(uiState.reviews) { review ->
                                ReviewCard(review = review)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyReviews() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.StarOutline,
            contentDescription = null,
            tint = ReviewTextMuted,
            modifier = Modifier.size(44.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "No ratings yet",
            color = ReviewText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Ratings from your passengers for this trip will appear here.",
            color = ReviewTextMuted,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ReviewCard(review: TripReviewDetailResponse) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = ReviewCardBackground,
        shadowElevation = 2.dp,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: student avatar + name + date
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ReviewPrimary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initialsOf(review.studentName),
                        color = ReviewPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = review.studentName ?: "Student",
                        color = ReviewText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val date = formatReviewDate(review.reviewDate)
                    if (date.isNotBlank()) {
                        Text(
                            text = date,
                            color = ReviewTextMuted,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Star rating
            StarRow(rating = review.rating)

            // Comment
            if (!review.review.isNullOrBlank()) {
                Text(
                    text = review.review,
                    color = ReviewText,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }

            // Tags
            if (review.tags.isNotEmpty()) {
                TagChips(tags = review.tags)
            }
        }
    }
}

@Composable
private fun StarRow(rating: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (i in 1..5) {
            Icon(
                imageVector = if (i <= rating) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                contentDescription = null,
                tint = if (i <= rating) ReviewStar else ReviewTextMuted.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = "$rating/5",
            color = ReviewTextMuted,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagChips(tags: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = ReviewAccent.copy(alpha = 0.12f)
            ) {
                Text(
                    text = tag,
                    color = ReviewAccent,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

// ── Helpers ──────────────────────────────────────────────────────────────────
private fun initialsOf(name: String?): String {
    if (name.isNullOrBlank()) return "S"
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    val first = parts.getOrNull(0)?.firstOrNull()?.toString() ?: ""
    val second = parts.getOrNull(1)?.firstOrNull()?.toString() ?: ""
    return (first + second).ifBlank { "S" }.uppercase()
}

/** Turns an ISO datetime like "2026-09-23T10:37:49" into "2026-09-23 • 10:37". */
private fun formatReviewDate(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val datePart = raw.take(10)
    val timePart = raw.drop(11).take(5)
    return if (timePart.isNotBlank()) "$datePart • $timePart" else datePart
}
