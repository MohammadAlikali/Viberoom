package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.util.Base64
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import java.io.ByteArrayOutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class AvatarFrame(
    val id: String,
    val labelKey: String,
    val emoji: String,
    val colors: List<Color>
) {
    NONE("none", "frame_none", "⚪", emptyList()),
    NEON_PULSE(
        id = "neon_pulse",
        labelKey = "frame_neon_pulse",
        emoji = "💜",
        colors = listOf(Color(0xFF7C3AED), Color(0xFFEC4899), Color(0xFF06B6D4), Color(0xFF7C3AED))
    ),
    GOLDEN_HALO(
        id = "golden_halo",
        labelKey = "frame_golden_halo",
        emoji = "👑",
        colors = listOf(Color(0xFFF59E0B), Color(0xFFFDE047), Color(0xFFEA580C), Color(0xFFF59E0B))
    ),
    AURORA_WAVE(
        id = "aurora_wave",
        labelKey = "frame_aurora_wave",
        emoji = "🌌",
        colors = listOf(Color(0xFF10B981), Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFF10B981))
    ),
    CYBER_PRISM(
        id = "cyber_prism",
        labelKey = "frame_cyber_prism",
        emoji = "💎",
        colors = listOf(Color(0xFF0EA5E9), Color(0xFF8B5CF6), Color(0xFF38BDF8), Color(0xFF0EA5E9))
    ),
    EMERALD_FLAME(
        id = "emerald_flame",
        labelKey = "frame_emerald_flame",
        emoji = "💚",
        colors = listOf(Color(0xFF059669), Color(0xFF84CC16), Color(0xFF14B8A6), Color(0xFF059669))
    ),
    BLOSSOM_GLOW(
        id = "blossom_glow",
        labelKey = "frame_blossom_glow",
        emoji = "🌸",
        colors = listOf(Color(0xFFF43F5E), Color(0xFFF472B6), Color(0xFFFB923C), Color(0xFFF43F5E))
    ),
    COSMIC_RING(
        id = "cosmic_ring",
        labelKey = "frame_cosmic_ring",
        emoji = "🪐",
        colors = listOf(Color(0xFF4F46E5), Color(0xFFA855F7), Color(0xFFF43F5E), Color(0xFF4F46E5))
    ),
    ROYAL_CROWN_ORB(
        id = "royal_crown_orb",
        labelKey = "frame_royal_crown_orb",
        emoji = "🔱",
        colors = listOf(Color(0xFFD97706), Color(0xFFFBBF24), Color(0xFF9333EA), Color(0xFFD97706))
    ),
    SOLAR_FLARE(
        id = "solar_flare",
        labelKey = "frame_solar_flare",
        emoji = "☀️",
        colors = listOf(Color(0xFFEA580C), Color(0xFFF97316), Color(0xFFFACC15), Color(0xFFEA580C))
    );

    companion object {
        fun fromId(raw: String?): AvatarFrame = entries.firstOrNull { it.id == raw } ?: NONE
    }
}

@Composable
fun AvatarAnimatedFrameOverlay(
    frame: AvatarFrame,
    size: Dp,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp
) {
    if (frame == AvatarFrame.NONE || frame.colors.isEmpty()) return

    val infiniteTransition = rememberInfiniteTransition(label = "avatar_frame_${frame.id}")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame_rot"
    )
    val counterRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "frame_counter_rot"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "frame_glow"
    )

    Canvas(modifier = modifier.size(size)) {
        val strokePx = strokeWidth.toPx()
        val diameter = this.size.minDimension - strokePx * 1.4f
        if (diameter <= 0f) return@Canvas
        val radius = diameter / 2f
        val topLeft = Offset((this.size.width - diameter) / 2f, (this.size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)

        when (frame) {
            AvatarFrame.NEON_PULSE -> {
                // Counter-rotating dual neon arcs + outer aura pulse
                drawCircle(
                    color = frame.colors.first().copy(alpha = 0.22f * glowAlpha),
                    radius = radius + strokePx * 0.55f,
                    style = Stroke(width = strokePx * 0.6f)
                )
                rotate(degrees = rotation) {
                    drawArc(
                        brush = Brush.sweepGradient(frame.colors.map { it.copy(alpha = glowAlpha) }),
                        startAngle = 0f,
                        sweepAngle = 210f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
                rotate(degrees = counterRotation) {
                    drawArc(
                        color = frame.colors[1].copy(alpha = 0.9f),
                        startAngle = 30f,
                        sweepAngle = 95f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 0.72f, cap = StrokeCap.Round)
                    )
                }
            }

            AvatarFrame.GOLDEN_HALO -> {
                // Warm golden ring with rotating shimmer & sunburst cardinal ticks
                rotate(degrees = rotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors),
                        radius = radius,
                        style = Stroke(width = strokePx * 1.05f, cap = StrokeCap.Round)
                    )
                }
                rotate(degrees = counterRotation * 0.5f) {
                    for (deg in 0 until 360 step 45) {
                        val rad = deg * PI.toFloat() / 180f
                        val inner = radius - strokePx * 0.1f
                        val outer = radius + strokePx * 0.9f
                        drawLine(
                            color = Color(0xFFFDE047).copy(alpha = glowAlpha),
                            start = Offset(center.x + cos(rad) * inner, center.y + sin(rad) * inner),
                            end = Offset(center.x + cos(rad) * outer, center.y + sin(rad) * outer),
                            strokeWidth = (strokePx * 0.45f).coerceAtLeast(1.4f),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            AvatarFrame.AURORA_WAVE -> {
                // Ribbon breathing ring + twin floating aurora lights
                rotate(degrees = rotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors.map { it.copy(alpha = 0.9f * glowAlpha) }),
                        radius = radius,
                        style = Stroke(width = strokePx * (0.9f + 0.35f * glowAlpha))
                    )
                }
                for (offsetAngle in listOf(rotation, rotation + 180f)) {
                    val rad = offsetAngle * PI.toFloat() / 180f
                    drawCircle(
                        color = Color(0xFFA7F3D0).copy(alpha = glowAlpha),
                        radius = strokePx * 0.95f,
                        center = Offset(center.x + cos(rad) * radius, center.y + sin(rad) * radius)
                    )
                }
            }

            AvatarFrame.CYBER_PRISM -> {
                // Tech segmented dashed ring + diamond vertices
                rotate(degrees = rotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors),
                        radius = radius,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Butt,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(strokePx * 3.2f, strokePx * 1.4f), 0f)
                        )
                    )
                }
                rotate(degrees = counterRotation) {
                    for (deg in listOf(0f, 90f, 180f, 270f)) {
                        val rad = deg * PI.toFloat() / 180f
                        drawCircle(
                            color = Color(0xFF38BDF8),
                            radius = strokePx * 0.8f,
                            center = Offset(center.x + cos(rad) * radius, center.y + sin(rad) * radius)
                        )
                    }
                }
            }

            AvatarFrame.EMERALD_FLAME -> {
                // Flame tail sweeps chased by glowing emerald sparks
                drawCircle(
                    color = frame.colors.first().copy(alpha = 0.25f),
                    radius = radius,
                    style = Stroke(width = strokePx * 0.65f)
                )
                rotate(degrees = rotation * 1.25f) {
                    drawArc(
                        brush = Brush.sweepGradient(frame.colors),
                        startAngle = 0f,
                        sweepAngle = 150f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 1.1f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        brush = Brush.sweepGradient(frame.colors.reversed()),
                        startAngle = 180f,
                        sweepAngle = 140f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx * 1.1f, cap = StrokeCap.Round)
                    )
                }
            }

            AvatarFrame.BLOSSOM_GLOW -> {
                // Soft rose ring + 6 orbiting blossom petals
                drawCircle(
                    brush = Brush.sweepGradient(frame.colors.map { it.copy(alpha = 0.72f) }),
                    radius = radius,
                    style = Stroke(width = strokePx * 0.85f)
                )
                for (i in 0 until 6) {
                    val rad = (rotation + i * 60f) * PI.toFloat() / 180f
                    val px = center.x + cos(rad) * radius
                    val py = center.y + sin(rad) * radius
                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFF472B6) else Color(0xFFFDA4AF),
                        radius = strokePx * 0.95f * glowAlpha,
                        center = Offset(px, py)
                    )
                }
            }

            AvatarFrame.COSMIC_RING -> {
                // Deep nebula halo + elliptical satellite orbiter
                rotate(degrees = counterRotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors),
                        radius = radius,
                        style = Stroke(width = strokePx)
                    )
                }
                val planetRad = rotation * PI.toFloat() / 180f
                val satX = center.x + cos(planetRad) * radius
                val satY = center.y + sin(planetRad) * radius
                drawCircle(
                    color = Color.White,
                    radius = strokePx * 1.05f,
                    center = Offset(satX, satY)
                )
                drawCircle(
                    color = Color(0xFFA855F7).copy(alpha = 0.45f),
                    radius = strokePx * 1.8f,
                    center = Offset(satX, satY)
                )
            }

            AvatarFrame.ROYAL_CROWN_ORB -> {
                // Double concentric royal band + 3 jewel accents
                rotate(degrees = rotation * 0.75f) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors),
                        radius = radius,
                        style = Stroke(width = strokePx * 1.1f)
                    )
                }
                drawCircle(
                    color = Color(0xFF9333EA).copy(alpha = 0.85f * glowAlpha),
                    radius = radius - strokePx * 0.9f,
                    style = Stroke(width = (strokePx * 0.4f).coerceAtLeast(1.2f))
                )
                for (i in 0 until 3) {
                    val rad = (counterRotation + i * 120f) * PI.toFloat() / 180f
                    drawCircle(
                        color = Color(0xFFFDE047),
                        radius = strokePx * 0.95f,
                        center = Offset(center.x + cos(rad) * radius, center.y + sin(rad) * radius)
                    )
                }
            }

            AvatarFrame.SOLAR_FLARE -> {
                // Energetic golden-orange ring with rotating outer corona rays
                rotate(degrees = rotation) {
                    drawCircle(
                        brush = Brush.sweepGradient(frame.colors),
                        radius = radius,
                        style = Stroke(width = strokePx)
                    )
                    for (deg in 0 until 360 step 30) {
                        val rad = deg * PI.toFloat() / 180f
                        val len = if (deg % 60 == 0) strokePx * 1.15f else strokePx * 0.65f
                        drawLine(
                            color = Color(0xFFFACC15).copy(alpha = glowAlpha),
                            start = Offset(center.x + cos(rad) * (radius + strokePx * 0.1f), center.y + sin(rad) * (radius + strokePx * 0.1f)),
                            end = Offset(center.x + cos(rad) * (radius + len), center.y + sin(rad) * (radius + len)),
                            strokeWidth = (strokePx * 0.48f).coerceAtLeast(1.3f),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            AvatarFrame.NONE -> Unit
        }
    }
}

private data class CustomAvatarGradient(
    val id: String,
    val startArgb: Int,
    val endArgb: Int
)

private val CustomAvatarGradients = listOf(
    CustomAvatarGradient("royal", 0xFF4F46E5.toInt(), 0xFF06B6D4.toInt()),
    CustomAvatarGradient("sunset", 0xFFE11D48.toInt(), 0xFFF59E0B.toInt()),
    CustomAvatarGradient("emerald", 0xFF059669.toInt(), 0xFF0EA5E9.toInt()),
    CustomAvatarGradient("orchid", 0xFF7C3AED.toInt(), 0xFFEC4899.toInt()),
    CustomAvatarGradient("midnight", 0xFF0F172A.toInt(), 0xFF3B82F6.toInt()),
    CustomAvatarGradient("coral", 0xFFEA580C.toInt(), 0xFFF43F5E.toInt()),
    CustomAvatarGradient("teal", 0xFF0D9488.toInt(), 0xFF22C55E.toInt()),
    CustomAvatarGradient("gold", 0xFFD97706.toInt(), 0xFF9333EA.toInt())
)

private val CustomAvatarSymbols = listOf(
    "✨", "🦊", "🦁", "🐼", "🦋", "🐉",
    "🚀", "🎧", "🎨", "☕", "🌸", "🌍",
    "💎", "⚡", "🕊️", "🦉", "🌙", "👑"
)

fun renderCustomAvatarDataUri(
    startArgb: Int,
    endArgb: Int,
    glyphText: String
): String {
    val size = 320
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            size.toFloat(),
            size.toFloat(),
            startArgb,
            endArgb,
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, size.toFloat(), size.toFloat(), bgPaint)

    val highlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(38, 255, 255, 255)
    }
    canvas.drawCircle(size * 0.25f, size * 0.22f, size * 0.42f, highlightPaint)

    val text = glyphText.ifBlank { "V" }
    val isSingleOrTwoAscii = text.all { it.code < 128 } && text.length <= 2
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = if (isSingleOrTwoAscii) 128f else 148f
        isFakeBoldText = true
    }
    val bounds = Rect()
    textPaint.getTextBounds(text, 0, text.length, bounds)
    val y = size / 2f - bounds.exactCenterY()
    canvas.drawText(text, size / 2f, y, textPaint)

    val out = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
    bitmap.recycle()
    val b64 = Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    return "data:image/jpeg;base64,$b64"
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AvatarStudioBottomSheet(
    user: UserProfile,
    appLanguage: AppLanguage,
    initialTab: Int = 0,
    onDismiss: () -> Unit,
    onPickDevicePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onSaveCustomAvatarData: (String) -> Unit,
    onSelectFrame: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var selectedFrameId by remember(user.avatarFrame) {
        mutableStateOf(AvatarFrame.fromId(user.avatarFrame).id)
    }
    var selectedGradient by remember { mutableStateOf(CustomAvatarGradients.first()) }
    var useInitials by remember { mutableStateOf(false) }
    var selectedSymbol by remember { mutableStateOf(CustomAvatarSymbols.first()) }

    val previewUser = remember(user, selectedFrameId) {
        user.copy(avatarFrame = selectedFrameId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
                .testTag("avatar_studio_sheet"),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.t("avatar_studio_title", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = AppStrings.t("close", appLanguage)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Live Avatar + Animated Frame Preview
            UserAvatar(
                user = previewUser,
                size = 96.dp,
                showOnlineStatus = false
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Photo Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPickDevicePhoto,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("studio_pick_photo_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("change_avatar", appLanguage),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (user.hasCustomAvatar) {
                    OutlinedButton(
                        onClick = onRemovePhoto,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("studio_remove_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = AppStrings.t("remove_avatar", appLanguage),
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text(AppStrings.t("avatar_frames_tab", appLanguage), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("avatar_studio_frames_tab")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text(AppStrings.t("create_avatar_tab", appLanguage), fontWeight = FontWeight.Bold) },
                    modifier = Modifier.testTag("avatar_studio_creator_tab")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                Text(
                    text = AppStrings.t("live_frames_subtitle", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                val chunked = AvatarFrame.entries.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    chunked.forEach { rowFrames ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowFrames.forEach { frame ->
                                val isSelected = selectedFrameId == frame.id
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                                    },
                                    border = BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedFrameId = frame.id
                                            onSelectFrame(frame.id)
                                        }
                                        .testTag("frame_option_${frame.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            user = user.copy(avatarFrame = frame.id),
                                            size = 40.dp,
                                            showOnlineStatus = false
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = AppStrings.t(frame.labelKey, appLanguage),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Custom Avatar Creator Preview
                val customGlyph = if (useInitials) user.displayInitials.ifBlank { "V" } else selectedSymbol
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(selectedGradient.startArgb), Color(selectedGradient.endArgb))
                            )
                        )
                        .border(2.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = customGlyph,
                        fontSize = if (useInitials) 30.sp else 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = AppStrings.t("pick_backdrop", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CustomAvatarGradients.forEach { grad ->
                        val isChosen = selectedGradient.id == grad.id
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(grad.startArgb), Color(grad.endArgb))))
                                .border(
                                    width = if (isChosen) 3.dp else 1.dp,
                                    color = if (isChosen) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { selectedGradient = grad },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isChosen) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.t("pick_symbol_or_initials", appLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { useInitials = !useInitials }) {
                        Text(
                            text = if (useInitials) AppStrings.t("use_emoji_symbol", appLanguage)
                            else AppStrings.t("use_initials_label", appLanguage)
                        )
                    }
                }

                if (!useInitials) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomAvatarSymbols.distinct().forEach { sym ->
                            val isChosen = selectedSymbol == sym
                            Surface(
                                shape = CircleShape,
                                color = if (isChosen) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(
                                    width = if (isChosen) 1.5.dp else 1.dp,
                                    color = if (isChosen) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedSymbol = sym }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = sym, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val dataUri = renderCustomAvatarDataUri(
                            startArgb = selectedGradient.startArgb,
                            endArgb = selectedGradient.endArgb,
                            glyphText = customGlyph
                        )
                        onSaveCustomAvatarData(dataUri)
                    },
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apply_custom_avatar_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.t("apply_custom_avatar", appLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
    }
}
