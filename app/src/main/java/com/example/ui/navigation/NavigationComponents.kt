package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MicNone
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.net.Uri
import com.example.model.VoiceRoom
import com.example.ui.NavigationTab
import com.example.ui.components.SpeakingSoundWave
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

object VibeRoutes {
    const val VOICE_ROOMS = "voice_rooms"
    const val FEED = "feed"
    const val CHATS = "chats"
    const val DISCOVER = "discover"
    const val PROFILE = "profile"
    const val SETTINGS = "settings"
    const val CHAT_DETAIL = "chat/{conversationId}"

    fun chatDetail(conversationId: String): String =
        "chat/${Uri.encode(conversationId)}"

    fun fromTab(tab: NavigationTab): String = when (tab) {
        NavigationTab.VOICE_ROOMS -> VOICE_ROOMS
        NavigationTab.FEED -> FEED
        NavigationTab.CHATS -> CHATS
        NavigationTab.DISCOVER -> DISCOVER
        NavigationTab.PROFILE -> PROFILE
    }

    fun toTab(route: String?): NavigationTab? = when (route) {
        VOICE_ROOMS -> NavigationTab.VOICE_ROOMS
        FEED -> NavigationTab.FEED
        CHATS -> NavigationTab.CHATS
        DISCOVER -> NavigationTab.DISCOVER
        PROFILE -> NavigationTab.PROFILE
        else -> null
    }
}

@Composable
fun VibeRoomNavigationRail(
    currentTab: NavigationTab,
    unreadChatsCount: Int,
    currentLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        modifier = modifier.testTag("vibe_navigation_rail"),
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets.safeDrawing.only(
            WindowInsetsSides.Vertical + WindowInsetsSides.Start
        )
    ) {
        NavigationTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            val (icon, selectedIcon) = when (tab) {
                NavigationTab.VOICE_ROOMS -> Icons.Outlined.MicNone to Icons.Filled.Mic
                NavigationTab.FEED -> Icons.Outlined.Home to Icons.Filled.Home
                NavigationTab.CHATS -> Icons.Outlined.ChatBubbleOutline to Icons.Filled.ChatBubble
                NavigationTab.DISCOVER -> Icons.Outlined.Explore to Icons.Filled.Explore
                NavigationTab.PROFILE -> Icons.Outlined.PersonOutline to Icons.Filled.Person
            }
            val localizedTitle = when (tab) {
                NavigationTab.VOICE_ROOMS -> AppStrings.t("nav_rooms", currentLanguage)
                NavigationTab.FEED -> AppStrings.t("nav_feed", currentLanguage)
                NavigationTab.CHATS -> AppStrings.t("nav_messages", currentLanguage)
                NavigationTab.DISCOVER -> AppStrings.t("nav_worldwide", currentLanguage)
                NavigationTab.PROFILE -> AppStrings.t("nav_profile", currentLanguage)
            }
            NavigationRailItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == NavigationTab.CHATS && unreadChatsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(unreadChatsCount.toString(), fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else icon,
                                contentDescription = localizedTitle
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else icon,
                            contentDescription = localizedTitle
                        )
                    }
                },
                label = {
                    Text(
                        text = localizedTitle,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun VibeRoomBottomBar(
    currentTab: NavigationTab,
    unreadChatsCount: Int,
    currentLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onTabSelected: (NavigationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("vibe_bottom_nav"),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        windowInsets = NavigationBarDefaults.windowInsets.union(
            WindowInsets.displayCutout.only(WindowInsetsSides.Horizontal)
        )
    ) {
        NavigationTab.entries.forEach { tab ->
            val isSelected = currentTab == tab
            val (icon, selectedIcon) = when (tab) {
                NavigationTab.VOICE_ROOMS -> Icons.Outlined.MicNone to Icons.Filled.Mic
                NavigationTab.FEED -> Icons.Outlined.Home to Icons.Filled.Home
                NavigationTab.CHATS -> Icons.Outlined.ChatBubbleOutline to Icons.Filled.ChatBubble
                NavigationTab.DISCOVER -> Icons.Outlined.Explore to Icons.Filled.Explore
                NavigationTab.PROFILE -> Icons.Outlined.PersonOutline to Icons.Filled.Person
            }

            val localizedTitle = when (tab) {
                NavigationTab.VOICE_ROOMS -> AppStrings.t("nav_rooms", currentLanguage)
                NavigationTab.FEED -> AppStrings.t("nav_feed", currentLanguage)
                NavigationTab.CHATS -> AppStrings.t("nav_messages", currentLanguage)
                NavigationTab.DISCOVER -> AppStrings.t("nav_worldwide", currentLanguage)
                NavigationTab.PROFILE -> AppStrings.t("nav_profile", currentLanguage)
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    if (tab == NavigationTab.CHATS && unreadChatsCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.error,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ) {
                                    Text(unreadChatsCount.toString(), fontSize = 10.sp)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else icon,
                                contentDescription = localizedTitle
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) selectedIcon else icon,
                            contentDescription = localizedTitle
                        )
                    }
                },
                label = {
                    Text(
                        text = localizedTitle,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                ),
                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
            )
        }
    }
}

@Composable
fun FloatingActiveVoiceBar(
    room: VoiceRoom?,
    isMuted: Boolean,
    currentLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onExpand: () -> Unit,
    onToggleMic: () -> Unit,
    onLeave: () -> Unit,
    modifier: Modifier = Modifier,
    barInsets: WindowInsets = WindowInsets(0, 0, 0, 0)
) {
    AnimatedVisibility(
        visible = room != null,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        if (room != null) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.Transparent,
                tonalElevation = 10.dp,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(barInsets)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    )
                    .clickable { onExpand() }
                    .testTag("floating_voice_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpeakingSoundWave(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 10.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = room.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${AppStrings.t("host_label", currentLanguage)}: ${room.host.name}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "• ${room.listenersCount} ${AppStrings.t("listening_count", currentLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                                maxLines = 1
                            )
                        }
                    }

                    IconButton(
                        onClick = onToggleMic,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                if (isMuted) MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
                                else MaterialTheme.colorScheme.primary
                            )
                            .testTag("floating_mic_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Filled.MicOff else Icons.Filled.Mic,
                            contentDescription = if (isMuted) AppStrings.t("mic_off", currentLanguage) else AppStrings.t("speaking", currentLanguage),
                            tint = if (isMuted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onLeave,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.35f))
                            .testTag("floating_leave_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = AppStrings.t("leave_stage", currentLanguage),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
