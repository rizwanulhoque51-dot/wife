package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.withTransform
import kotlin.random.Random

data class HeartState(
    val xRatio: Float,
    val initialY: Float,
    val speed: Float,
    val scale: Float,
    val alpha: Float,
    val delay: Int,
    val phase: Float
)

@Composable
fun FloatingHeartsBackground(modifier: Modifier = Modifier, heartColor: Color = Color(0xFFFF4F87)) {
    // A single continuous infinite transition to drive all heart movements smoothly
    val transition = rememberInfiniteTransition(label = "hearts_anim")
    val time by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time_fraction"
    )

    // Pre-allocate a canonical heart Path at 0, 0
    val basePath = remember {
        Path().apply {
            moveTo(0f, -6f)
            cubicTo(-6f, -12f, -12f, -6f, -12f, 0f)
            cubicTo(-12f, 6f, -6f, 12f, 0f, 18f)
            cubicTo(6f, 12f, 12f, 6f, 12f, 0f)
            cubicTo(12f, -6f, 6f, -12f, 0f, -6f)
            close()
        }
    }

    // Remember stable hearts so they are never regenerated on recomposition
    val hearts = remember {
        List(20) {
            HeartState(
                xRatio = Random.nextFloat(),
                initialY = 1.15f, // Start below screen view
                speed = 0.35f + Random.nextFloat() * 0.35f, // Slow, elegant, fluid rise
                scale = 0.5f + Random.nextFloat() * 1.2f,  // Diverse sizes
                alpha = 0.2f + Random.nextFloat() * 0.45f, // Diverse opacities
                delay = Random.nextInt(0, 8000),           // Staggered delays
                phase = Random.nextFloat() * 2f * Math.PI.toFloat() // Random wave offsets
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        hearts.forEach { heart ->
            // Calculate relative progress (0.0 to 1.0) for this heart based on global timer and delay
            val progress = (time + (heart.delay / 8000f)) % 1.0f

            // Calculate vertical position (smooth bottom-to-top transition)
            val yPos = height * (heart.initialY - progress * heart.speed)

            // Dynamic horizontal swaying using an elegant sine wave with phase shift
            val swayAmplitude = 25f * heart.scale
            val swayOffset = kotlin.math.sin(progress * 2f * Math.PI + heart.phase).toFloat() * swayAmplitude
            val xPos = width * heart.xRatio + swayOffset

            // Soft fade-in when rising and elegant fade-out before disappearing
            val alphaFactor = when {
                progress < 0.15f -> progress / 0.15f // Fade in
                progress > 0.80f -> (1.0f - progress) / 0.20f // Fade out
                else -> 1.0f
            }
            val finalAlpha = heart.alpha * alphaFactor

            // Draw using scale/translate transforms on canvas drawing context directly
            if (yPos > -50f && yPos < height + 50f && finalAlpha > 0.01f) {
                withTransform({
                    translate(left = xPos, top = yPos)
                    scale(scaleX = heart.scale, scaleY = heart.scale, pivot = Offset.Zero)
                }) {
                    drawPath(
                        path = basePath,
                        color = heartColor.copy(alpha = finalAlpha)
                    )
                }
            }
        }
    }
}

