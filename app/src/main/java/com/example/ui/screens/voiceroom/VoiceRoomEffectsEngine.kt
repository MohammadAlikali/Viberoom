package com.example.ui.screens.voiceroom

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ActiveEntranceVisual
import com.example.model.ActiveGiftVisual
import com.example.model.EntranceStyle
import com.example.model.GiftAnimationStyle
import com.example.ui.localization.AppLanguage
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun EntranceAnimationOverlay(
    entrance: ActiveEntranceVisual?,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = entrance != null,
        enter = slideInHorizontally(initialOffsetX = { -it }) + fadeIn(tween(260)),
        exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(tween(280)),
        modifier = modifier
    ) {
        if (entrance != null) {
            val accent = Color(entrance.style.accentHex)
            val secondary = Color(entrance.style.secondaryHex)
            val progress = remember(entrance.eventId) { Animatable(0f) }

            LaunchedEffect(entrance.eventId) {
                progress.snapTo(0f)
                progress.animateTo(1f, animationSpec = tween(if (entrance.style.isVip) 2600 else 1600, easing = FastOutSlowInEasing))
            }

            val styleTitle = when (appLanguage) {
                AppLanguage.DARI_PERSIAN -> entrance.style.titleFa
                AppLanguage.PASHTO -> entrance.style.titlePs
                AppLanguage.ENGLISH -> entrance.style.titleEn
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("voice_room_entrance_banner")
            ) {
                if (entrance.style.isVip) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(68.dp)
                    ) {
                        val sweepX = size.width * progress.value
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, accent.copy(alpha = 0.85f), secondary.copy(alpha = 0.6f), Color.Transparent)
                            ),
                            start = Offset((sweepX - 180f).coerceAtLeast(0f), size.height * 0.5f),
                            end = Offset((sweepX + 120f).coerceAtMost(size.width), size.height * 0.5f),
                            strokeWidth = 6f
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.88f),
                    border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(accent, secondary))),
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(accent.copy(alpha = 0.45f), secondary.copy(alpha = 0.15f))))
                                .graphicsLayer {
                                    val bump = 1f + sin(progress.value * PI.toFloat() * 2f) * 0.12f
                                    scaleX = bump
                                    scaleY = bump
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = entrance.style.emoji, fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "${entrance.userFlag} ${entrance.userName}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = accent.copy(alpha = 0.25f)
                                ) {
                                    Text(
                                        text = if (entrance.style.isVip) "VIP • LV.${entrance.userLevel}" else "LV.${entrance.userLevel}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = accent,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = styleTitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GiftAnimationOverlay(
    visual: ActiveGiftVisual?,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    if (visual == null) return

    val gift = visual.gift
    val primary = Color(gift.primaryColorHex)
    val secondary = Color(gift.secondaryColorHex)
    val progress = remember(visual.eventId) { Animatable(0f) }

    LaunchedEffect(visual.eventId) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = if (gift.isFullScreen) 3500 else 2100, easing = LinearEasing)
        )
    }

    val p = progress.value
    val fadeAlpha = when {
        p < 0.12f -> p / 0.12f
        p > 0.85f -> ((1f - p) / 0.15f).coerceIn(0f, 1f)
        else -> 1f
    }

    val giftTitle = when (appLanguage) {
        AppLanguage.DARI_PERSIAN -> gift.nameFa
        AppLanguage.PASHTO -> gift.namePs
        AppLanguage.ENGLISH -> gift.nameEn
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer { alpha = fadeAlpha }
            .testTag("voice_room_gift_overlay_${gift.id}"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w * 0.5f
            val cy = h * 0.45f

            if (gift.isFullScreen) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(primary.copy(alpha = 0.22f), secondary.copy(alpha = 0.08f), Color.Transparent),
                        center = Offset(cx, cy),
                        radius = size.maxDimension * 0.6f
                    )
                )
            }

            when (gift.style) {
                GiftAnimationStyle.LUXURY_CAR_DRIVE, GiftAnimationStyle.MOTORCYCLE_DRIFT -> {
                    val roadY = h * 0.56f
                    val carX = -w * 0.15f + (w * 1.3f) * p
                    // Neon asphalt streak
                    drawLine(
                        brush = Brush.horizontalGradient(listOf(Color.Transparent, primary, secondary, Color.Transparent)),
                        start = Offset(0f, roadY + 28f),
                        end = Offset(w, roadY + 28f),
                        strokeWidth = 6f
                    )
                    // Headlight cone
                    val beam = Path().apply {
                        moveTo(carX + 20f, roadY)
                        lineTo(carX + 260f, roadY - 55f)
                        lineTo(carX + 260f, roadY + 55f)
                        close()
                    }
                    drawPath(
                        path = beam,
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.55f), Color.Transparent),
                            startX = carX,
                            endX = carX + 260f
                        )
                    )
                    // Speed particles behind vehicle
                    for (i in 0..8) {
                        val px = carX - 30f - i * 32f
                        val py = roadY + sin(i * 1.4f + p * 12f) * 16f
                        drawCircle(color = secondary.copy(alpha = 0.65f - i * 0.06f), radius = 6f - i * 0.4f, center = Offset(px, py))
                    }
                }
                GiftAnimationStyle.HELICOPTER_FLYOVER, GiftAnimationStyle.AIRPLANE_FLYBY -> {
                    val flyX = w * 1.15f - (w * 1.3f) * p
                    val flyY = if (gift.style == GiftAnimationStyle.AIRPLANE_FLYBY) h * (0.58f - 0.34f * p) else h * 0.34f + sin(p * 8f) * 18f
                    // Contrail or rotor disc
                    if (gift.style == GiftAnimationStyle.HELICOPTER_FLYOVER) {
                        val rotorSpan = 90f * cos(p * 36f)
                        drawLine(
                            color = Color.White.copy(alpha = 0.85f),
                            start = Offset(flyX - rotorSpan, flyY - 36f),
                            end = Offset(flyX + rotorSpan, flyY - 36f),
                            strokeWidth = 5f
                        )
                        drawCircle(
                            brush = Brush.radialGradient(listOf(primary.copy(alpha = 0.25f), Color.Transparent)),
                            radius = 160f,
                            center = Offset(flyX, flyY)
                        )
                    } else {
                        drawLine(
                            brush = Brush.linearGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.7f), primary)),
                            start = Offset(w, h * 0.58f),
                            end = Offset(flyX, flyY),
                            strokeWidth = 5f
                        )
                    }
                }
                GiftAnimationStyle.SPACESHIP_WARP -> {
                    val shipY = h * 0.82f - (h * 0.68f) * p
                    drawLine(
                        brush = Brush.verticalGradient(listOf(Color.Transparent, primary, Color.White)),
                        start = Offset(cx, shipY + 260f),
                        end = Offset(cx, shipY),
                        strokeWidth = 10f
                    )
                    for (s in 0..14) {
                        val sx = cx + ((s - 7) * w * 0.06f)
                        val sy = ((s * 97 + p * 900f) % h)
                        drawLine(
                            color = secondary.copy(alpha = 0.55f),
                            start = Offset(sx, sy),
                            end = Offset(sx, sy + 42f),
                            strokeWidth = 2.5f
                        )
                    }
                }
                GiftAnimationStyle.PORTAL_VORTEX, GiftAnimationStyle.FULLSCREEN_LEGENDARY -> {
                    for (ring in 1..4) {
                        val radius = size.minDimension * (0.09f * ring) * (0.65f + 0.5f * p)
                        drawCircle(
                            brush = Brush.sweepGradient(listOf(primary, secondary, Color.White, primary)),
                            radius = radius,
                            center = Offset(cx, cy),
                            style = Stroke(width = 6f - ring)
                        )
                    }
                    for (spark in 0..15) {
                        val a = spark * (2f * PI.toFloat() / 16f) + p * 6f
                        val dist = size.minDimension * 0.42f * p
                        drawCircle(
                            color = if (spark % 2 == 0) primary else secondary,
                            radius = 6f * (1f - p * 0.4f),
                            center = Offset(cx + cos(a) * dist, cy + sin(a) * dist)
                        )
                    }
                }
                GiftAnimationStyle.DRAGON_FLAME -> {
                    for (ember in 0..18) {
                        val angle = -PI.toFloat() * 0.5f + ((ember - 9) * 0.14f) + sin(p * 10f + ember) * 0.1f
                        val dist = size.minDimension * (0.1f + 0.44f * p)
                        drawCircle(
                            color = if (ember % 2 == 0) primary else Color(0xFFFACC15),
                            radius = 9f * (1f - p * 0.5f),
                            center = Offset(cx + cos(angle) * dist, cy + sin(angle) * dist)
                        )
                    }
                }
                GiftAnimationStyle.CROWN_GLOW, GiftAnimationStyle.PETALS_BURST -> {
                    for (i in 0..11) {
                        val a = i * (2f * PI.toFloat() / 12f) + p * 2.5f
                        val dist = size.minDimension * 0.28f * p
                        drawCircle(
                            color = if (i % 2 == 0) primary.copy(alpha = 0.85f) else secondary.copy(alpha = 0.85f),
                            radius = 7f,
                            center = Offset(cx + cos(a) * dist, cy + sin(a) * dist)
                        )
                    }
                }
            }
        }

        // Foreground Emoji Motion + Cinematic Gift Card
        val emojiOffsetX = when (gift.style) {
            GiftAnimationStyle.LUXURY_CAR_DRIVE, GiftAnimationStyle.MOTORCYCLE_DRIFT -> (-220f + 440f * p)
            GiftAnimationStyle.HELICOPTER_FLYOVER, GiftAnimationStyle.AIRPLANE_FLYBY -> (220f - 440f * p)
            else -> 0f
        }
        val emojiOffsetY = when (gift.style) {
            GiftAnimationStyle.SPACESHIP_WARP -> (160f - 320f * p)
            GiftAnimationStyle.AIRPLANE_FLYBY -> (90f - 180f * p)
            else -> (-24f * sin(p * PI.toFloat()))
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = gift.emoji,
                fontSize = if (gift.isFullScreen) 64.sp else 48.sp,
                modifier = Modifier.graphicsLayer {
                    translationX = emojiOffsetX
                    translationY = emojiOffsetY
                    val s = if (gift.isFullScreen) 1.15f + 0.15f * sin(p * PI.toFloat()) else 1f
                    scaleX = s
                    scaleY = s
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0B1120).copy(alpha = 0.90f),
                border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(primary, secondary))),
                shadowElevation = 10.dp
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "${visual.senderFlag} ${visual.senderName} → ${visual.recipientName}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Text(
                        text = "$giftTitle ${if (gift.xpCost > 0) "• ${gift.xpCost} XP" else "• FREE"}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                }
            }
        }
    }
}
