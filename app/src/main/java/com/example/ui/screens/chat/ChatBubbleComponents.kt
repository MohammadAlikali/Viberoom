package com.example.ui.screens.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Forward
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessage
import com.example.model.Conversation
import com.example.model.TranslationResult
import com.example.model.UserProfile
import com.example.ui.components.DataUriOrUrlImage
import com.example.ui.components.SpeakingSoundWave
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun chatDayKey(createdAtMs: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(createdAtMs))
}

fun formatChatDateLabel(createdAtMs: Long, appLanguage: AppLanguage): String {
    val targetCal = Calendar.getInstance().apply { timeInMillis = createdAtMs }
    val todayCal = Calendar.getInstance()
    val yesterdayCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    return when {
        targetCal.get(Calendar.YEAR) == todayCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == todayCal.get(Calendar.DAY_OF_YEAR) ->
            AppStrings.t("date_today", appLanguage)

        targetCal.get(Calendar.YEAR) == yesterdayCal.get(Calendar.YEAR) &&
            targetCal.get(Calendar.DAY_OF_YEAR) == yesterdayCal.get(Calendar.DAY_OF_YEAR) ->
            AppStrings.t("date_yesterday", appLanguage)

        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(createdAtMs))
    }
}

@Composable
fun DateSeparatorChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
            tonalElevation = 1.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MessageBubble(
    message: ChatMessage,
    isPinned: Boolean = false,
    isPlayingVoice: Boolean = false,
    translation: TranslationResult? = null,
    isTranslating: Boolean = false,
    translationError: String? = null,
    showTranslationNotes: Boolean = true,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onToggleVoicePlayback: () -> Unit = {},
    onReply: () -> Unit = {},
    onReplyPreviewClick: () -> Unit = {},
    onCopy: () -> Unit = {},
    onForward: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    onToggleReaction: (String) -> Unit = {},
    onPinToggle: () -> Unit = {},
    onTranslate: () -> Unit = {},
    onDismissTranslation: () -> Unit = {}
) {
    val isMine = message.isMine
    val bubbleColor = when {
        message.isDeleted -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        isMine -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surface
    }
    val textColor = when {
        message.isDeleted -> MaterialTheme.colorScheme.onSurfaceVariant
        isMine -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val bubbleShape = RoundedCornerShape(
        topStart = 20.dp,
        topEnd = 20.dp,
        bottomStart = if (isMine) 20.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 20.dp
    )
    var showActionsMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                tonalElevation = if (isMine || message.isDeleted) 0.dp else 2.dp,
                shadowElevation = if (message.isDeleted) 0.dp else 1.dp,
                border = if (!isMine && !message.isDeleted) {
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)
                    )
                } else null,
                modifier = Modifier
                    .widthIn(min = 84.dp, max = 304.dp)
                    .clip(bubbleShape)
                    .combinedClickable(
                        enabled = !message.isDeleted,
                        onClick = { showActionsMenu = true },
                        onLongClick = { showActionsMenu = true }
                    )
                    .testTag("message_bubble_${message.id}")
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)) {
                    if (message.isForwarded && !message.isDeleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Forward,
                                contentDescription = null,
                                tint = textColor.copy(alpha = 0.78f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.t("forwarded_badge", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontStyle = FontStyle.Italic,
                                color = textColor.copy(alpha = 0.78f)
                            )
                        }
                    }

                    if (!message.replyToText.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isMine) {
                                Color.Black.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onReplyPreviewClick() }
                        ) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)) {
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(28.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (isMine) textColor.copy(alpha = 0.85f)
                                            else MaterialTheme.colorScheme.primary
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    if (!message.replyToSenderName.isNullOrBlank()) {
                                        Text(
                                            text = message.replyToSenderName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMine) textColor else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Text(
                                        text = message.replyToText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = textColor.copy(alpha = 0.82f),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (message.mediaType == "image" && !message.isDeleted &&
                        (!message.mediaData.isNullOrBlank() || !message.mediaUrl.isNullOrBlank())
                    ) {
                        DataUriOrUrlImage(
                            mediaData = message.mediaData,
                            mediaUrl = message.mediaUrl,
                            contentDescription = message.text.ifBlank { AppStrings.t("attach_photo", appLanguage) },
                            height = 195.dp
                        )
                        if (message.text.isNotBlank() && message.text != "📷 Photo") {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = message.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor
                            )
                        }
                    } else if (message.mediaType == "voice" && !message.isDeleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onToggleVoicePlayback,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isMine) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.22f)
                                        else MaterialTheme.colorScheme.primary
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPlayingVoice) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = AppStrings.t("voice_note", appLanguage),
                                    tint = if (isMine) textColor else MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    SpeakingSoundWave(
                                        color = if (isPlayingVoice) {
                                            if (isMine) textColor else MaterialTheme.colorScheme.primary
                                        } else textColor.copy(alpha = 0.65f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "0:${(message.audioDurationSec ?: 12).toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Text(
                                    text = if (isPlayingVoice) {
                                        AppStrings.t("playing_voice", appLanguage)
                                    } else {
                                        AppStrings.t("voice_note", appLanguage)
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.78f)
                                )
                            }
                        }
                    } else {
                        Text(
                            text = message.text,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = if (message.isDeleted) FontStyle.Italic else FontStyle.Normal,
                            color = textColor
                        )
                    }

                    if (isTranslating) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = textColor,
                                strokeWidth = 1.5.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("translating_label", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.85f)
                            )
                        }
                    } else if (translation != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = textColor.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(5.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🌐 ${translation.detectedSourceLanguage} → ${translation.targetLanguage}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissTranslation,
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = AppStrings.t("hide_translation", appLanguage),
                                    tint = textColor.copy(alpha = 0.72f),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                        Text(
                            text = translation.translatedText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = textColor.copy(alpha = 0.96f)
                        )
                        if (showTranslationNotes && translation.pronunciationNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🗣️ ${translation.pronunciationNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                    } else if (!translationError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(color = textColor.copy(alpha = 0.25f))
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = AppStrings.t("translation_error_retry", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isMine) textColor.copy(alpha = 0.9f) else MaterialTheme.colorScheme.error,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isMine) textColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onTranslate() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Refresh,
                                        contentDescription = AppStrings.t("retry_label", appLanguage),
                                        tint = if (isMine) textColor else MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = AppStrings.t("retry_label", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isMine) textColor else MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.mediaType == "text" && !message.isDeleted && message.text.isNotBlank() && !isTranslating) {
                            IconButton(
                                onClick = onTranslate,
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("bubble_translate_btn_${message.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Translate,
                                    contentDescription = AppStrings.t("translate_label", appLanguage),
                                    tint = if (translation != null) {
                                        if (isMine) textColor else MaterialTheme.colorScheme.primary
                                    } else {
                                        textColor.copy(alpha = 0.68f)
                                    },
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        if (message.expiresAtMs != null && !message.isDeleted) {
                            Text(
                                text = "⏱️ ",
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.72f)
                            )
                        }
                        if (message.isEdited && !message.isDeleted) {
                            Text(
                                text = "(${AppStrings.t("edited_badge", appLanguage)}) ",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = message.timestamp,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = textColor.copy(alpha = 0.72f)
                        )
                        if (isMine && !message.isDeleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (message.isRead) "✓✓" else "✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (message.isRead) textColor else textColor.copy(alpha = 0.65f)
                            )
                        }
                    }
                }
            }

            DropdownMenu(
                expanded = showActionsMenu,
                onDismissRequest = { showActionsMenu = false }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("❤️", "😂", "🔥", "👍", "🎉").forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    showActionsMenu = false
                                    onToggleReaction(emoji)
                                }
                                .padding(6.dp)
                        )
                    }
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = { Text(AppStrings.t("reply_label", appLanguage)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                    onClick = {
                        showActionsMenu = false
                        onReply()
                    }
                )
                if (message.text.isNotBlank()) {
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("copy_label", appLanguage)) },
                        leadingIcon = { Icon(Icons.Filled.ContentCopy, contentDescription = null) },
                        onClick = {
                            showActionsMenu = false
                            onCopy()
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text(AppStrings.t("forward_label", appLanguage)) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Forward, contentDescription = null) },
                    onClick = {
                        showActionsMenu = false
                        onForward()
                    }
                )
                if (message.mediaType == "text") {
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("translate_label", appLanguage)) },
                        leadingIcon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                        onClick = {
                            showActionsMenu = false
                            onTranslate()
                        }
                    )
                }
                DropdownMenuItem(
                    text = {
                        Text(
                            if (isPinned) AppStrings.t("unpin_label", appLanguage)
                            else AppStrings.t("pin_label", appLanguage)
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.PushPin, contentDescription = null) },
                    onClick = {
                        showActionsMenu = false
                        onPinToggle()
                    }
                )
                if (isMine && message.mediaType == "text") {
                    DropdownMenuItem(
                        text = { Text(AppStrings.t("edit_label", appLanguage)) },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            showActionsMenu = false
                            onEdit()
                        }
                    )
                }
                if (isMine) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                AppStrings.t("delete_post", appLanguage),
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.DeleteOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showActionsMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }

        if (message.reactions.isNotEmpty() && !message.isDeleted) {
            val grouped = message.reactions.values.groupingBy { it }.eachCount()
            Row(
                modifier = Modifier.padding(top = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                grouped.forEach { (emoji, count) ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        tonalElevation = 1.dp,
                        modifier = Modifier.clickable { onToggleReaction(emoji) }
                    ) {
                        Text(
                            text = if (count > 1) "$emoji $count" else emoji,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForwardMessageSheet(
    conversations: List<Conversation>,
    suggestedUsers: List<UserProfile>,
    currentConversationId: String,
    appLanguage: AppLanguage,
    onSelectConversation: (String) -> Unit,
    onSelectUser: (UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val existingPeerIds = remember(conversations) { conversations.map { it.peerUser.id }.toSet() }
    val otherConversations = remember(conversations, currentConversationId) {
        conversations.filter { it.id != currentConversationId }
    }
    val extraUsers = remember(suggestedUsers, existingPeerIds) {
        suggestedUsers.filter { it.id !in existingPeerIds && it.allowDirectMessages }.take(12)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Text(
                text = AppStrings.t("forward_to_title", appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(otherConversations, key = { "fwd_conv_${it.id}" }) { conv ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectConversation(conv.id)
                                onDismiss()
                            }
                            .testTag("forward_target_${conv.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(user = conv.peerUser, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = conv.peerUser.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${conv.peerUser.flagEmoji} ${conv.peerUser.countryName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Forward,
                                contentDescription = AppStrings.t("forward_label", appLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                items(extraUsers, key = { "fwd_user_${it.id}" }) { peer ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onSelectUser(peer)
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(user = peer, size = 40.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${peer.flagEmoji} ${peer.countryName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Forward,
                                contentDescription = AppStrings.t("forward_label", appLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
