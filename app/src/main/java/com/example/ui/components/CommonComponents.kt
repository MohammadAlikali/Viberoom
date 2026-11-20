package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.MediaAttachmentHelper
import com.example.model.UserProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.VibeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

object PrivacyAwareLastSeenFormatter {
    fun formatPresenceSubtitle(
        user: UserProfile,
        appLanguage: AppLanguage,
        isContactOrFollowing: Boolean = user.isFollowing
    ): String {
        if (!user.canShowPresenceTo(isContactOrFollowing)) {
            return "${user.flagEmoji} ${user.countryName}"
        }
        if (user.isOnline) {
            return "${AppStrings.t("online", appLanguage)} • ${user.flagEmoji} ${user.countryName}"
        }
        val ts = user.lastSeenAtMs
        if (ts != null && ts > 0L) {
            val diffMin = ((System.currentTimeMillis() - ts) / 60_000L).coerceAtLeast(0L)
            val whenText = when {
                diffMin < 2L -> AppStrings.t("last_seen_recently", appLanguage)
                diffMin < 60L -> "${diffMin}m"
                diffMin < 1440L -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(ts))
                else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(ts))
            }
            return "$whenText • ${user.flagEmoji} ${user.countryName}"
        }
        return "${user.flagEmoji} ${user.countryName}"
    }
}

@Composable
fun UserAvatar(
    user: UserProfile,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    showOnlineStatus: Boolean = true,
    isSpeaking: Boolean = false
) {
    val activeFrame = androidx.compose.runtime.remember(user.avatarFrame) {
        AvatarFrame.fromId(user.avatarFrame)
    }
    val hasFrame = activeFrame != AvatarFrame.NONE
    val pulseScale = if (isSpeaking) {
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val anim by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.18f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "speaking_pulse"
        )
        anim
    } else 1f

    val avatarModel = androidx.compose.runtime.remember(user.avatarData, user.avatarUrl) {
        MediaAttachmentHelper.decodeDataUriBytes(user.avatarData)
            ?: user.avatarUrl?.takeIf { it.isNotBlank() }
    }

    val outerPadding = when {
        isSpeaking -> 8.dp
        hasFrame -> 6.dp
        else -> 0.dp
    }

    Box(
        modifier = modifier.size(size + outerPadding),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing pulse ring when speaking
        if (isSpeaking) {
            Box(
                modifier = Modifier
                    .size(size + 8.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
            )
        } else if (hasFrame) {
            AvatarAnimatedFrameOverlay(
                frame = activeFrame,
                size = size + 6.dp,
                strokeWidth = if (size >= 72.dp) 3.5.dp else 2.5.dp
            )
        }

        // Avatar Core
        val avatarBg = Color(user.avatarColorHex)
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(avatarBg, avatarBg.copy(alpha = 0.78f))
                    )
                )
                .border(
                    width = when {
                        isSpeaking -> 2.5.dp
                        hasFrame -> 1.dp
                        else -> 1.5.dp
                    },
                    color = when {
                        isSpeaking -> MaterialTheme.colorScheme.primary
                        hasFrame -> MaterialTheme.colorScheme.surface
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (avatarModel != null) {
                AsyncImage(
                    model = avatarModel,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size).clip(CircleShape)
                )
            } else {
                Text(
                    text = user.displayInitials.ifBlank { "V" },
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = (size.value * 0.36f).sp
                )
            }
        }

        // Online Indicator dot (respects user's privacy toggle)
        if (showOnlineStatus && user.showOnlineStatus && user.isOnline) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .size((size.value * 0.26f).coerceIn(10f, 14f).dp)
                    .clip(CircleShape)
                    .background(VibeGreen)
                    .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
            )
        }
    }
}

@Composable
fun CountryFlagBadge(
    flagEmoji: String,
    countryName: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = flagEmoji,
            fontSize = 12.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = countryName,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun InterestChip(
    text: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val bg = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    }
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        modifier = modifier,
        onClick = { onClick?.invoke() },
        enabled = onClick != null
    ) {
        Text(
            text = text,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun SpeakingSoundWave(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    val bar1 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(300, easing = LinearEasing), RepeatMode.Reverse),
        label = "bar3"
    )

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .size(height = (14 * bar1).dp, width = 2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .size(height = (14 * bar2).dp, width = 2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .width(2.5.dp)
                .size(height = (14 * bar3).dp, width = 2.5.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
    }
}

@Composable
fun ReactionFloatingBubble(
    emoji: String,
    offsetX: Float,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "float")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -180f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rise"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fade"
    )

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .alpha(alpha)
    ) {
        Text(
            text = emoji,
            fontSize = 28.sp
        )
    }
}
