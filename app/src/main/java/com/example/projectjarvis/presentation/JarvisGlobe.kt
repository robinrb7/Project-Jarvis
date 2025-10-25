package com.example.projectjarvis.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlin.math.sin

@Composable
fun JarvisGlobe(
    modifier: Modifier = Modifier,
    isActive: Boolean
) {
    // Pulse animation
    val pulseAnim by animateFloatAsState(
        targetValue = if (isActive) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fluid animation offset
    val infiniteTransition = rememberInfiniteTransition()
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isActive) 800 else 2000, easing = LinearEasing)
        )
    )

    Canvas(
        modifier = modifier
            .size(180.dp)
            .graphicsLayer(scaleX = pulseAnim, scaleY = pulseAnim)
    ) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)

        // Outer circle (globe)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF2C89D3), Color(0xFF0E0E88)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center
        )

        // Inner fluid-like moving waves
        val waveCount = 3
        for (i in 0 until waveCount) {
            val waveRadius = radius * (0.6f + i * 0.1f)
            val angle = waveOffset + i * 120f
            val waveX = center.x + sin(Math.toRadians(angle.toDouble())).toFloat() * 10
            val waveY = center.y + sin(Math.toRadians((angle + 90).toDouble())).toFloat() * 10

            drawCircle(
                color = Color.White.copy(alpha = 0.15f + i * 0.05f),
                radius = waveRadius / 6f,
                center = Offset(waveX, waveY)
            )
        }
    }
}