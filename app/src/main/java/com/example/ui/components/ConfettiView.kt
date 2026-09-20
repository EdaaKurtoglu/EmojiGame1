package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val startX: Float,
    val speedY: Float,
    val speedX: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color
)

@Composable
fun ConfettiView(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isActive) return

    val progress = remember { Animatable(0f) }
    val particles = remember {
        val colors = listOf(
            Color(0xFFFFC107), // Gold
            Color(0xFF4CAF50), // Green
            Color(0xFFE91E63), // Pink
            Color(0xFF9C27B0), // Purple
            Color(0xFF00BCD4), // Cyan
            Color(0xFFFF5722)  // Orange
        )
        List(40) {
            ConfettiParticle(
                startX = Random.nextFloat(),
                speedY = Random.nextFloat() * 0.8f + 0.5f,
                speedX = (Random.nextFloat() - 0.5f) * 0.3f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 14f + 8f,
                color = colors.random()
            )
        }
    }

    LaunchedEffect(isActive) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1800, easing = LinearEasing)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val p = progress.value

        particles.forEach { particle ->
            val x = (particle.startX * w + particle.speedX * w * p).coerceIn(0f, w)
            val y = (particle.speedY * h * p) - 20f
            val rotation = particle.rotationSpeed * p
            val alpha = (1f - p).coerceIn(0f, 1f)

            if (y in 0f..h) {
                rotate(rotation, pivot = Offset(x, y)) {
                    drawRect(
                        color = particle.color.copy(alpha = alpha),
                        topLeft = Offset(x, y),
                        size = Size(particle.size, particle.size * 0.6f)
                    )
                }
            }
        }
    }
}
