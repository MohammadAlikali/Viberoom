package com.example.ui.screens.voiceroom

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import com.example.model.RoomBackgroundTheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class StarParticle(
    val xFrac: Float,
    val yFrac: Float,
    val radius: Float,
    val speed: Float,
    val phase: Float
)

@Composable
fun VoiceRoomBackgroundCanvas(
    theme: RoomBackgroundTheme,
    lowPowerMode: Boolean,
    audioEnergy: Float = 0f,
    modifier: Modifier = Modifier
) {
    val topColor = Color(theme.topHex)
    val midColor = Color(theme.midHex)
    val bottomColor = Color(theme.bottomHex)
    val accentColor = Color(theme.accentHex)

    val particles = remember(theme) {
        List(28) { idx ->
            val seed = (idx * 37 + theme.ordinal * 13)
            StarParticle(
                xFrac = ((seed * 17) % 100) / 100f,
                yFrac = ((seed * 29) % 100) / 100f,
                radius = 1.8f + (idx % 4) * 1.4f,
                speed = 0.4f + (idx % 5) * 0.22f,
                phase = (idx * 0.65f)
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(topColor, midColor, bottomColor)))
            .testTag("voice_room_background_canvas_${theme.id}")
    ) {
        if (lowPowerMode) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(accentColor.copy(alpha = 0.22f), Color.Transparent),
                        center = Offset(size.width * 0.5f, size.height * 0.26f),
                        radius = size.minDimension * 0.72f
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.26f),
                    radius = size.minDimension * 0.72f
                )
            }
            return@Box
        }

        val transition = rememberInfiniteTransition(label = "vr_bg_${theme.id}")
        val phase by transition.animateFloat(
            initialValue = 0f,
            targetValue = (2f * PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "bg_phase"
        )
        val pulse by transition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bg_pulse"
        )

        val energyBoost = 1f + audioEnergy.coerceIn(0f, 1f) * 0.35f

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Ambient radial stage halo reactive to live WebRTC audio activity
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.24f * pulse.coerceAtMost(1f)),
                        accentColor.copy(alpha = 0.06f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.28f),
                    radius = size.minDimension * 0.65f * energyBoost
                ),
                center = Offset(w * 0.5f, h * 0.28f),
                radius = size.minDimension * 0.65f * energyBoost
            )

            when (theme) {
                RoomBackgroundTheme.AURORA, RoomBackgroundTheme.OCEAN_WAVES -> {
                    for (layer in 0..2) {
                        val path = Path()
                        val baseY = h * (0.22f + layer * 0.17f)
                        val amp = h * (0.04f + layer * 0.015f) * energyBoost
                        path.moveTo(0f, baseY)
                        val steps = 20
                        for (s in 0..steps) {
                            val x = (w / steps) * s
                            val angle = phase * (1f + layer * 0.2f) + (s * 0.45f) + layer
                            val y = baseY + sin(angle) * amp
                            path.lineTo(x, y)
                        }
                        path.lineTo(w, h)
                        path.lineTo(0f, h)
                        path.close()
                        val waveColor = if (layer % 2 == 0) accentColor else Color(0xFF818CF8)
                        drawPath(
                            path = path,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    waveColor.copy(alpha = 0.16f - layer * 0.03f),
                                    Color.Transparent
                                ),
                                startY = baseY - amp,
                                endY = baseY + h * 0.35f
                            )
                        )
                    }
                }
                RoomBackgroundTheme.GALAXY -> {
                    for (ring in 1..3) {
                        drawCircle(
                            color = accentColor.copy(alpha = 0.14f / ring),
                            radius = size.minDimension * (0.22f * ring) * pulse,
                            center = Offset(w * 0.5f, h * 0.32f),
                            style = Stroke(width = 2f)
                        )
                    }
                    particles.forEach { p ->
                        val angle = phase * 0.35f * p.speed + p.phase
                        val dist = size.minDimension * (0.12f + p.yFrac * 0.48f)
                        val cx = w * 0.5f + cos(angle) * dist
                        val cy = h * 0.32f + sin(angle) * dist * 0.6f
                        val twinkle = (0.4f + 0.6f * ((sin(phase * 2f + p.phase) + 1f) / 2f))
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f * twinkle),
                            radius = p.radius,
                            center = Offset(cx, cy)
                        )
                    }
                }
                RoomBackgroundTheme.NEON_CITY, RoomBackgroundTheme.CYBERPUNK -> {
                    val horizonY = h * 0.68f
                    // Perspective cyber floor grid
                    for (i in -5..5) {
                        val topX = w * 0.5f + i * (w * 0.06f)
                        val bottomX = w * 0.5f + i * (w * 0.24f)
                        drawLine(
                            color = accentColor.copy(alpha = 0.18f),
                            start = Offset(topX, horizonY),
                            end = Offset(bottomX, h),
                            strokeWidth = 1.6f
                        )
                    }
                    for (row in 0..5) {
                        val frac = ((row / 6f) + (phase / (2f * PI.toFloat()))) % 1f
                        val y = horizonY + (h - horizonY) * (frac * frac)
                        drawLine(
                            color = accentColor.copy(alpha = 0.22f * frac),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.5f
                        )
                    }
                    // Neon vertical skyline beams
                    for (b in 0..5) {
                        val bx = w * (0.12f + b * 0.15f)
                        val beamH = h * (0.16f + (b % 3) * 0.08f) * pulse
                        val beamColor = if (b % 2 == 0) accentColor else Color(0xFFEC4899)
                        drawLine(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, beamColor.copy(alpha = 0.35f))
                            ),
                            start = Offset(bx, horizonY - beamH),
                            end = Offset(bx, horizonY),
                            strokeWidth = 14f
                        )
                    }
                }
                RoomBackgroundTheme.FLOATING_PARTICLES, RoomBackgroundTheme.FIRE_ATMOSPHERE -> {
                    val isFire = theme == RoomBackgroundTheme.FIRE_ATMOSPHERE
                    particles.forEach { p ->
                        val drift = (p.yFrac - (phase / (2f * PI.toFloat())) * p.speed).let {
                            var v = it % 1f
                            if (v < 0f) v += 1f
                            v
                        }
                        val px = (p.xFrac * w + sin(phase + p.phase) * 26f).coerceIn(0f, w)
                        val py = drift * h
                        val particleColor = when {
                            isFire && p.phase > 4f -> Color(0xFFFACC15)
                            isFire -> accentColor
                            else -> accentColor
                        }
                        drawCircle(
                            color = particleColor.copy(alpha = 0.45f),
                            radius = p.radius * 1.8f,
                            center = Offset(px, py)
                        )
                    }
                }
                RoomBackgroundTheme.LUXURY_ROYAL -> {
                    for (ray in 0..5) {
                        val rayAngle = (phase * 0.2f) + ray * (PI.toFloat() / 3f)
                        val endX = w * 0.5f + cos(rayAngle) * size.maxDimension
                        val endY = h * 0.25f + sin(rayAngle) * size.maxDimension
                        drawLine(
                            brush = Brush.linearGradient(
                                colors = listOf(accentColor.copy(alpha = 0.20f), Color.Transparent),
                                start = Offset(w * 0.5f, h * 0.25f),
                                end = Offset(endX, endY)
                            ),
                            start = Offset(w * 0.5f, h * 0.25f),
                            end = Offset(endX, endY),
                            strokeWidth = 28f
                        )
                    }
                    particles.take(18).forEach { p ->
                        val glint = 0.35f + 0.65f * ((sin(phase * 2f + p.phase) + 1f) / 2f)
                        drawCircle(
                            color = Color(0xFFFDE047).copy(alpha = 0.55f * glint),
                            radius = p.radius,
                            center = Offset(p.xFrac * w, p.yFrac * h)
                        )
                    }
                }
            }
        }
    }
}
