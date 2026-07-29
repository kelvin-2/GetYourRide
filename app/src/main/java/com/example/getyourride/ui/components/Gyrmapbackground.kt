package com.example.getyourride.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * GyrMapBackground
 *
 * Reusable decorative background matching the "navy map" look used across
 * GetYourRide screens — concentric radar rings, faint route lines, pin
 * markers, tiny car glyphs and compass stars scattered over NavyPrimary.
 *
 * Usage:
 *
 * GyrMapBackground {
 *     // your screen content here, e.g. Column { ... }
 * }
 *
 * It's just a Box with a Canvas drawn behind `content`, so it drops into
 * any screen without changing your existing layout code.
 *
 * IMPORTANT: This composable does NOT force fillMaxSize() on itself anymore.
 * It sizes to whatever `modifier` you pass in. If you want it full-screen,
 * pass Modifier.fillMaxSize() from the call site. If you want it to wrap a
 * fixed-height section (like a profile header), pass Modifier.fillMaxWidth()
 * with an explicit height, or let it size to its content's intrinsic height.
 */

// Reuse your existing color system where it applies.
private val NavyPrimary = Color(0xFF102A46)
private val LineColor = Color.White.copy(alpha = 0.06f)
private val LineColorStrong = Color.White.copy(alpha = 0.10f)

@Composable
fun GyrMapBackground(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NavyPrimary,
    content: @Composable BoxScope.() -> Unit
) {
    // CHANGED: was `modifier.fillMaxSize()` — that forced this Box to always
    // stretch to fill its parent, which is wrong when you just want to wrap
    // a fixed-size chunk of UI (like ProfileHeader) rather than the whole screen.
    // Now it just uses whatever `modifier` the caller supplies.
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = backgroundColor)
            drawRadarClusters(this)
            drawRoutes(this)
            drawPins(this)
            drawCars(this)
            drawCompassStars(this)
        }
        content()
    }
}

// ---- Radar / concentric circles -------------------------------------------------

private fun drawRadarClusters(scope: DrawScope) = with(scope) {
    val clusters = listOf(
        Offset(size.width * 0.5f, size.height * 0.5f) to size.width * 0.42f,
        Offset(size.width * -0.05f, size.height * 0.18f) to size.width * 0.32f,
        Offset(size.width * 1.05f, size.height * 0.1f) to size.width * 0.30f,
        Offset(size.width * -0.05f, size.height * 0.85f) to size.width * 0.30f,
        Offset(size.width * 1.05f, size.height * 0.9f) to size.width * 0.32f
    )

    clusters.forEach { (center, maxRadius) ->
        val ringCount = 4
        for (i in 1..ringCount) {
            val radius = maxRadius * (i / ringCount.toFloat())
            val dashed = i % 2 == 0
            drawCircle(
                color = LineColor,
                radius = radius,
                center = center,
                style = if (dashed) {
                    Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f)
                    )
                } else {
                    Stroke(width = 1.dp.toPx())
                }
            )
        }
    }
}

// ---- Winding dotted routes --------------------------------------------------

private fun drawRoutes(scope: DrawScope) = with(scope) {
    val routeSeeds = listOf(
        Offset(size.width * 0.20f, size.height * 0.28f),
        Offset(size.width * 0.65f, size.height * 0.62f)
    )

    routeSeeds.forEach { start ->
        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(start.x, start.y)
            cubicTo(
                start.x + size.width * 0.15f, start.y - size.height * 0.05f,
                start.x + size.width * 0.05f, start.y + size.height * 0.10f,
                start.x + size.width * 0.20f, start.y + size.height * 0.12f
            )
            cubicTo(
                start.x + size.width * 0.30f, start.y + size.height * 0.14f,
                start.x + size.width * 0.28f, start.y - size.height * 0.06f,
                start.x + size.width * 0.38f, start.y - size.height * 0.02f
            )
        }
        drawPath(
            path = path,
            color = LineColorStrong,
            style = Stroke(
                width = 1.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 7f), 0f)
            )
        )
    }
}

// ---- Pin / marker glyphs -----------------------------------------------------

private fun drawPins(scope: DrawScope) = with(scope) {
    val positions = listOf(
        Offset(size.width * 0.26f, size.height * 0.12f),
        Offset(size.width * 0.78f, size.height * 0.13f),
        Offset(size.width * 0.27f, size.height * 0.38f),
        Offset(size.width * 0.79f, size.height * 0.38f),
        Offset(size.width * 0.23f, size.height * 0.63f),
        Offset(size.width * 0.80f, size.height * 0.63f),
        Offset(size.width * 0.30f, size.height * 0.87f),
        Offset(size.width * 0.70f, size.height * 0.88f)
    )
    val pinSize = size.width * 0.045f
    positions.forEach { pos -> drawPinGlyph(this, pos, pinSize) }
}

private fun drawPinGlyph(scope: DrawScope, center: Offset, r: Float) = with(scope) {
    val path = androidx.compose.ui.graphics.Path().apply {
        moveTo(center.x, center.y + r * 1.6f)
        cubicTo(
            center.x - r * 1.1f, center.y + r * 0.4f,
            center.x - r * 1.1f, center.y - r * 0.9f,
            center.x, center.y - r * 1.1f
        )
        cubicTo(
            center.x + r * 1.1f, center.y - r * 0.9f,
            center.x + r * 1.1f, center.y + r * 0.4f,
            center.x, center.y + r * 1.6f
        )
        close()
    }
    drawPath(path = path, color = LineColor, style = Stroke(width = 1.dp.toPx()))
    drawCircle(color = LineColor, radius = r * 0.35f, center = Offset(center.x, center.y - r * 0.2f), style = Stroke(width = 1.dp.toPx()))
}

// ---- Tiny car glyphs, rotated at odd angles ----------------------------------

private fun drawCars(scope: DrawScope) = with(scope) {
    val cars = listOf(
        Triple(Offset(size.width * 0.75f, size.height * 0.20f), size.width * 0.05f, 35f),
        Triple(Offset(size.width * 0.06f, size.height * 0.55f), size.width * 0.05f, -20f),
        Triple(Offset(size.width * 0.92f, size.height * 0.45f), size.width * 0.05f, 25f),
        Triple(Offset(size.width * 0.40f, size.height * 0.70f), size.width * 0.05f, -15f),
        Triple(Offset(size.width * 0.63f, size.height * 0.75f), size.width * 0.05f, 40f)
    )
    cars.forEach { (pos, s, angle) -> drawCarGlyph(this, pos, s, angle) }
}

private fun drawCarGlyph(scope: DrawScope, center: Offset, s: Float, angleDeg: Float) = with(scope) {
    rotate(degrees = angleDeg, pivot = center) {
        val bodyPath = androidx.compose.ui.graphics.Path().apply {
            val left = center.x - s
            val top = center.y - s * 1.6f
            val right = center.x + s
            val bottom = center.y + s * 1.6f
            moveTo(left, top + s * 0.5f)
            cubicTo(left, top, right, top, right, top + s * 0.5f)
            lineTo(right, bottom - s * 0.5f)
            cubicTo(right, bottom, left, bottom, left, bottom - s * 0.5f)
            close()
        }
        drawPath(path = bodyPath, color = LineColor, style = Stroke(width = 1.dp.toPx()))
        // windshield line
        drawLine(
            color = LineColor,
            start = Offset(center.x - s * 0.5f, center.y - s * 0.6f),
            end = Offset(center.x + s * 0.5f, center.y - s * 0.6f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// ---- Compass / star markers --------------------------------------------------

private fun drawCompassStars(scope: DrawScope) = with(scope) {
    val positions = listOf(
        Offset(size.width * 0.60f, size.height * 0.33f),
        Offset(size.width * 0.50f, size.height * 0.78f)
    )
    positions.forEach { pos -> drawCompassStar(this, pos, size.width * 0.035f) }
}

private fun drawCompassStar(scope: DrawScope, center: Offset, r: Float) = with(scope) {
    drawCircle(color = LineColorStrong, radius = r * 1.6f, center = center, style = Stroke(width = 1.dp.toPx()))
    val path = androidx.compose.ui.graphics.Path()
    for (i in 0 until 4) {
        val angle = Math.toRadians((i * 90).toDouble())
        val outer = Offset(
            center.x + (r * cos(angle)).toFloat(),
            center.y + (r * sin(angle)).toFloat()
        )
        val innerAngle = Math.toRadians((i * 90 + 45).toDouble())
        val inner = Offset(
            center.x + (r * 0.35f * cos(innerAngle)).toFloat(),
            center.y + (r * 0.35f * sin(innerAngle)).toFloat()
        )
        if (i == 0) path.moveTo(outer.x, outer.y) else path.lineTo(outer.x, outer.y)
        path.lineTo(inner.x, inner.y)
    }
    path.close()
    drawPath(path = path, color = LineColorStrong, style = Stroke(width = 1.dp.toPx()))
}