package com.example.ui.screens.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Conversation
import com.example.ui.NavigationTab
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.CountryFlagBadge
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppStrings

private enum class ChatFilter(val key: String, val tagSuffix: String) {
    ALL("filter_all", "all"),
    UNREAD("filter_unread", "unread"),
    ONLINE("filter_online", "online")
}

@Composable
fun ChatListScreen(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier,
    selectedConversationId: String? = null
) {
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(ChatFilter.ALL) }

    val filteredConversations = remember(conversations, searchQuery, activeFilter) {
        conversations.filter { conv ->
            val matchesQuery = searchQuery.isBlank() ||
                conv.peerUser.name.contains(searchQuery, ignoreCase = true) ||
                conv.peerUser.username.contains(searchQuery, ignoreCase = true) ||
                conv.peerUser.countryName.contains(searchQuery, ignoreCase = true) ||
                conv.lastMessage.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (activeFilter) {
                ChatFilter.ALL -> true
                ChatFilter.UNREAD -> conv.unreadCount > 0
                ChatFilter.ONLINE -> conv.peerUser.canShowPresenceTo(conv.peerUser.isFollowing) && conv.peerUser.isOnline
            }
            matchesQuery && matchesFilter
        }
    }

    val totalUnread = remember(conversations) { conversations.sumOf { it.unreadCount } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("chat_list_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.t("nav_messages", appLanguage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = AppStrings.t("messages_subtitle", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (totalUnread > 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "$totalUnread",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(AppStrings.t("search_conversations", appLanguage)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = AppStrings.t("search_conversations", appLanguage),
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("chat_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChatFilter.entries.forEach { filter ->
                    val selected = activeFilter == filter
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { activeFilter = filter }
                            .testTag("chat_filter_${filter.tagSuffix}")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = AppStrings.t(filter.key, appLanguage),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredConversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppStrings.t("no_chats_yet", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = AppStrings.t("no_chats_hint", appLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.selectTab(NavigationTab.DISCOVER) },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("browse_worldwide_from_chats")
                        ) {
                            Text(AppStrings.t("find_people_btn", appLanguage))
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 24.dp, top = 4.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = filteredConversations,
                        key = { it.id },
                        contentType = { "conversation_item" }
                    ) { conversation ->
                        ConversationItem(
                            conversation = conversation,
                            isSelected = conversation.id == selectedConversationId,
                            onClick = { viewModel.openChat(conversation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false
) {
    val showPresence = conversation.peerUser.canShowPresenceTo(conversation.peerUser.isFollowing)
    val isUnread = conversation.unreadCount > 0

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = when {
            isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.42f)
            isUnread -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = when {
            isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
            isLight -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
            else -> null
        },
        tonalElevation = if (isSelected || isUnread) 2.dp else 0.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("conversation_item_${conversation.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                user = conversation.peerUser,
                size = 54.dp,
                showOnlineStatus = showPresence
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = conversation.peerUser.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        CountryFlagBadge(
                            flagEmoji = conversation.peerUser.flagEmoji,
                            countryName = conversation.peerUser.countryCode
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = conversation.lastTimestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = conversation.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (isUnread) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (isUnread) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .heightIn(min = 22.dp)
                                .widthIn(min = 22.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (conversation.unreadCount > 99) "99+" else "${conversation.unreadCount}",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
