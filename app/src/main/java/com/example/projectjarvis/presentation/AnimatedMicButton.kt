package com.example.projectjarvis.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun AnimatedMicButton(
    isListening: Boolean,
    onClick: () -> Unit
) {
    // Infinite transition for bar animation
    val transition = rememberInfiniteTransition()

    // Each bar has its own animated height
    val bar1Height by transition.animateFloat(
        initialValue = 20f,
        targetValue = if (isListening) 40f else 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar2Height by transition.animateFloat(
        initialValue = 25f,
        targetValue = if (isListening) 50f else 25f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val bar3Height by transition.animateFloat(
        initialValue = 12f,
        targetValue = if (isListening) 45f else 12f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Button(
        onClick = onClick,
        shape = CircleShape,
        modifier = Modifier.size(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isListening) Color(0xFF5168EF) else Color(0xFF4343E3)
        )
    ) {
        // Draw 3 bars inside the mic
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Canvas(modifier = Modifier.size(width = 6.dp, height = bar1Height.dp)) {
                drawRoundRect(color = Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            }
            Canvas(modifier = Modifier.size(width = 6.dp, height = bar2Height.dp)) {
                drawRoundRect(color = Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            }
            Canvas(modifier = Modifier.size(width = 6.dp, height = bar3Height.dp)) {
                drawRoundRect(color = Color.White, cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f))
            }
        }
    }
}