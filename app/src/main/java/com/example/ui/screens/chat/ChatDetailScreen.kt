package com.example.ui.screens.chat

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.VoiceNotePlayer
import com.example.data.VoiceNoteRecorder
import com.example.model.ChatMessage
import com.example.model.VibeCatalogs
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.PrivacyAwareLastSeenFormatter
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.VibeGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private sealed interface ChatListEntry {
    val key: String

    data class DateHeader(override val key: String, val label: String) : ChatListEntry
    data class Bubble(override val key: String, val message: ChatMessage) : ChatListEntry
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(
    conversationId: String,
    viewModel: VibeRoomViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val conversations by viewModel.conversations.collectAsState()
    val allMessages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.filteredUsers.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val inlineTranslations by viewModel.inlineTranslations.collectAsState()
    val inlineTranslatingKeys by viewModel.inlineTranslatingKeys.collectAsState()
    val inlineTranslationErrors by viewModel.inlineTranslationErrors.collectAsState()
    val isMediaUploading by viewModel.isMediaUploading.collectAsState()
    val chatTranslateLanguage by viewModel.chatTranslateLanguage.collectAsState()
    val defaultTranslateLanguage by viewModel.defaultTranslateLanguage.collectAsState()
    val autoTranslateChat by viewModel.autoTranslateChat.collectAsState()
    val showTranslationNotes by viewModel.showTranslationNotes.collectAsState()
    val disappearingTtlMs by viewModel.chatDisappearingTtlMs.collectAsState()
    val aiSmartReplies by viewModel.aiSmartReplies.collectAsState()
    val isLoadingSmartReplies by viewModel.isLoadingSmartReplies.collectAsState()

    val conversation = conversations.firstOrNull { it.id == conversationId }
    val allChatMessages = allMessages[conversationId].orEmpty()

    var inputMessage by remember { mutableStateOf("") }
    var showComposerTools by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showTranslateMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var chatSearchQuery by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var forwardingMessage by remember { mutableStateOf<ChatMessage?>(null) }

    val listState = rememberLazyListState()

    val voiceRecorder = remember(context) { VoiceNoteRecorder(context) }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var playingVoiceMessageId by remember { mutableStateOf<String?>(null) }
    var pendingMicAction by remember { mutableStateOf<String?>(null) }

    DisposableEffect(conversationId) {
        onDispose {
            voiceRecorder.cancelRecording()
            VoiceNotePlayer.stop()
        }
    }

    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingSeconds = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        val action = pendingMicAction
        pendingMicAction = null
        if (granted) {
            when (action) {
                "call" -> conversation?.let { viewModel.startVoiceCall(it.peerUser, conversationId) }
                "voice_note" -> {
                    if (voiceRecorder.startRecording()) {
                        isRecordingVoice = true
                    }
                }
            }
        } else {
            viewModel.showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage))
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.sendImageMessage(context, conversationId, uri)
        }
    }

    val effectiveTranslateTarget = remember(chatTranslateLanguage, defaultTranslateLanguage, appLanguage) {
        chatTranslateLanguage?.takeIf { it.isNotBlank() }
            ?: defaultTranslateLanguage.takeIf { it.isNotBlank() }
            ?: VibeCatalogs.defaultTranslateLanguageFor(appLanguage)
    }
    val translateLanguageChoices = remember { VibeCatalogs.translateTargetLanguages }

    LaunchedEffect(allChatMessages, autoTranslateChat, effectiveTranslateTarget) {
        if (autoTranslateChat && allChatMessages.isNotEmpty()) {
            viewModel.autoTranslateIncomingMessages(allChatMessages)
        }
    }

    val displayedMessages = remember(allChatMessages, chatSearchQuery) {
        val q = chatSearchQuery.trim()
        if (q.isEmpty()) allChatMessages
        else allChatMessages.filter { it.text.contains(q, ignoreCase = true) }
    }

    val timelineItems = remember(displayedMessages, appLanguage) {
        buildList<ChatListEntry> {
            var lastDayKey: String? = null
            displayedMessages.forEach { msg ->
                val day = chatDayKey(msg.createdAtMs)
                if (day != lastDayKey) {
                    lastDayKey = day
                    add(
                        ChatListEntry.DateHeader(
                            key = "date_${day}_${msg.id}",
                            label = formatChatDateLabel(msg.createdAtMs, appLanguage)
                        )
                    )
                }
                add(ChatListEntry.Bubble(key = msg.id, message = msg))
            }
        }
    }

    LaunchedEffect(timelineItems.size) {
        if (timelineItems.isNotEmpty()) {
            listState.animateScrollToItem(timelineItems.lastIndex)
        }
    }

    val showScrollToBottom by remember {
        derivedStateOf {
            val total = timelineItems.size
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            total > 4 && lastVisible < total - 2
        }
    }

    val icebreakers = remember(appLanguage) {
        listOf(
            AppStrings.t("icebreaker_1", appLanguage),
            AppStrings.t("icebreaker_2", appLanguage),
            AppStrings.t("icebreaker_3", appLanguage)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("chat_detail_screen")
    ) {
        TopAppBar(
            title = {
                if (conversation != null) {
                    val peer = conversation.peerUser
                    val showPresence = peer.canShowPresenceTo(peer.isFollowing)
                    val subtitle = PrivacyAwareLastSeenFormatter.formatPresenceSubtitle(peer, appLanguage, peer.isFollowing)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            viewModel.openUserProfile(peer)
                        }
                    ) {
                        UserAvatar(
                            user = peer,
                            size = 40.dp,
                            showOnlineStatus = showPresence
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = peer.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(peer.flagEmoji, fontSize = 12.sp)
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (showPresence && peer.isOnline) VibeGreen
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                } else {
                    Text(
                        text = AppStrings.t("nav_messages", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = AppStrings.t("back", appLanguage)
                    )
                }
            },
            actions = {
                if (conversation != null) {
                    IconButton(
                        onClick = {
                            val hasMic = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasMic) {
                                viewModel.startVoiceCall(conversation.peerUser, conversationId)
                            } else {
                                pendingMicAction = "call"
                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        modifier = Modifier.testTag("chat_voice_call_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = AppStrings.t("voice_call", appLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                IconButton(
                    onClick = {
                        showSearchBar = !showSearchBar
                        if (!showSearchBar) chatSearchQuery = ""
                    },
                    modifier = Modifier.testTag("chat_search_toggle_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = AppStrings.t("search_in_chat", appLanguage)
                    )
                }
                IconButton(
                    onClick = viewModel::openLearningHub,
                    modifier = Modifier.testTag("chat_ai_hub_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = AppStrings.t("learning_hub_title", appLanguage),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                if (conversation != null) {
                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.testTag("chat_options_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = AppStrings.t("view_full_profile", appLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(AppStrings.t("view_full_profile", appLanguage)) },
                                onClick = {
                                    showMenu = false
                                    viewModel.openUserProfile(conversation.peerUser)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.t("report_user", appLanguage),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.openReportDialog(conversation.peerUser)
                                }
                            )
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = AppStrings.t("block_user", appLanguage),
                                        color = MaterialTheme.colorScheme.error
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    viewModel.blockUser(conversation.peerUser.id)
                                    onBack()
                                }
                            )
                        }
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Chat Translation Target Language Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("chat_translate_to", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val nextTtl = when (disappearingTtlMs) {
                        0L -> 3_600_000L
                        3_600_000L -> 86_400_000L
                        else -> 0L
                    }
                    val ttlLabel = when (disappearingTtlMs) {
                        3_600_000L -> "1h"
                        86_400_000L -> "24h"
                        else -> AppStrings.t("timer_off", appLanguage)
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (disappearingTtlMs > 0L) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setChatDisappearingTtl(nextTtl) }
                            .testTag("chat_disappearing_timer_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AccessTime,
                                contentDescription = AppStrings.t("disappearing_label", appLanguage),
                                tint = if (disappearingTtlMs > 0L) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = ttlLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (disappearingTtlMs > 0L) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (autoTranslateChat) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewModel.setAutoTranslateChat(!autoTranslateChat) }
                            .testTag("chat_auto_translate_toggle")
                    ) {
                        Text(
                            text = if (autoTranslateChat) "✓ ${AppStrings.t("auto_translate_short", appLanguage)}"
                            else AppStrings.t("auto_translate_short", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (autoTranslateChat) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }

                    Box {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { showTranslateMenu = true }
                                .testTag("chat_translate_lang_selector")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = effectiveTranslateTarget,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = AppStrings.t("chat_translate_to", appLanguage),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTranslateMenu,
                            onDismissRequest = { showTranslateMenu = false }
                        ) {
                            translateLanguageChoices.forEach { langChoice ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = langChoice,
                                            fontWeight = if (langChoice == effectiveTranslateTarget) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = if (langChoice == effectiveTranslateTarget) {
                                        {
                                            Icon(
                                                imageVector = Icons.Filled.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        viewModel.setChatTranslateLanguage(langChoice)
                                        showTranslateMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showSearchBar) {
            OutlinedTextField(
                value = chatSearchQuery,
                onValueChange = { chatSearchQuery = it },
                placeholder = { Text(AppStrings.t("search_in_chat", appLanguage)) },
                singleLine = true,
                trailingIcon = {
                    if (chatSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { chatSearchQuery = "" }) {
                            Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("chat_search_input")
            )
        }

        val pinnedText = conversation?.pinnedMessageText
        if (!pinnedText.isNullOrBlank()) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pinned_message_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.t("pinned_message", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = pinnedText,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { viewModel.pinMessage(conversationId, null, null) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = AppStrings.t("unpin_label", appLanguage),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(timelineItems, key = { it.key }) { item ->
                    when (item) {
                        is ChatListEntry.DateHeader -> {
                            DateSeparatorChip(label = item.label)
                        }

                        is ChatListEntry.Bubble -> {
                            val message = item.message
                            val transKey = "msg_${message.id}"
                            val isPinned = conversation?.pinnedMessageId == message.id
                            MessageBubble(
                                message = message,
                                isPinned = isPinned,
                                isPlayingVoice = (playingVoiceMessageId == message.id),
                                translation = inlineTranslations[transKey],
                                isTranslating = transKey in inlineTranslatingKeys,
                                translationError = inlineTranslationErrors[transKey],
                                showTranslationNotes = showTranslationNotes,
                                appLanguage = appLanguage,
                                onToggleVoicePlayback = {
                                    VoiceNotePlayer.playOrToggle(
                                        context = context,
                                        messageId = message.id,
                                        mediaData = message.mediaData,
                                        mediaUrl = message.mediaUrl
                                    ) { activeId ->
                                        playingVoiceMessageId = activeId
                                    }
                                },
                                onReply = {
                                    editingMessage = null
                                    replyingToMessage = message
                                },
                                onReplyPreviewClick = {
                                    val targetId = message.replyToMessageId
                                    if (!targetId.isNullOrBlank()) {
                                        val targetIdx = timelineItems.indexOfFirst {
                                            it is ChatListEntry.Bubble && it.message.id == targetId
                                        }
                                        if (targetIdx >= 0) {
                                            scope.launch { listState.animateScrollToItem(targetIdx) }
                                        }
                                    }
                                },
                                onCopy = {
                                    clipboard.setText(AnnotatedString(message.text))
                                    viewModel.showInAppNotification(AppStrings.t("copied_notice", appLanguage))
                                },
                                onForward = { forwardingMessage = message },
                                onEdit = {
                                    replyingToMessage = null
                                    editingMessage = message
                                    inputMessage = message.text
                                },
                                onDelete = { viewModel.deleteMessage(conversationId, message.id) },
                                onToggleReaction = { emoji ->
                                    viewModel.toggleMessageReaction(
                                        conversationId = conversationId,
                                        messageId = message.id,
                                        currentReactions = message.reactions,
                                        emoji = emoji
                                    )
                                },
                                onPinToggle = {
                                    if (isPinned) {
                                        viewModel.pinMessage(conversationId, null, null)
                                    } else {
                                        viewModel.pinMessage(conversationId, message.id, message.text)
                                    }
                                },
                                onTranslate = {
                                    viewModel.translateText(
                                        text = message.text,
                                        targetLanguage = effectiveTranslateTarget,
                                        inlineItemKey = transKey
                                    )
                                },
                                onDismissTranslation = {
                                    viewModel.dismissInlineTranslation(transKey)
                                }
                            )
                        }
                    }
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showScrollToBottom,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(14.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        if (timelineItems.isNotEmpty()) {
                            scope.launch { listState.animateScrollToItem(timelineItems.lastIndex) }
                        }
                    },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.78f),
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = !isLoadingSmartReplies) { viewModel.fetchSmartReplies(conversationId) }
                    .testTag("ai_smart_replies_trigger")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isLoadingSmartReplies) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.5.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("ai_smart_replies", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            aiSmartReplies.forEach { reply ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            inputMessage = reply
                            viewModel.clearSmartReplies()
                        }
                ) {
                    Text(
                        text = reply,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            icebreakers.forEach { prompt ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.clickable {
                        viewModel.sendMessage(conversationId, prompt)
                    }
                ) {
                    Text(
                        text = prompt,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        if (replyingToMessage != null) {
            val target = replyingToMessage!!
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reply_preview_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Reply,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${AppStrings.t("reply_label", appLanguage)}: ${if (target.isMine) currentUser.name else conversation?.peerUser?.name ?: ""}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = target.text,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = { replyingToMessage = null },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (editingMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_preview_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.t("edit_label", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            editingMessage = null
                            inputMessage = ""
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(imageVector = Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        if (isRecordingVoice) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f))
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .testTag("voice_recording_bar"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${AppStrings.t("recording_voice", appLanguage)} 0:${recordingSeconds.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = {
                        voiceRecorder.cancelRecording()
                        isRecordingVoice = false
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("cancel_voice_recording_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = AppStrings.t("cancel", appLanguage),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                IconButton(
                    onClick = {
                        isRecordingVoice = false
                        val recorded = voiceRecorder.stopAndEncode()
                        if (recorded != null) {
                            viewModel.sendVoiceMessage(
                                conversationId = conversationId,
                                durationSec = recorded.durationSec,
                                mediaData = recorded.dataUri
                            )
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("send_voice_recording_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = AppStrings.t("send", appLanguage),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            val isTyping = inputMessage.isNotBlank()
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(
                    0.5.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chat_composer_surface")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isTyping || showComposerTools) {
                        IconButton(
                            onClick = {
                                val hasMic = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.RECORD_AUDIO
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasMic) {
                                    if (voiceRecorder.startRecording()) {
                                        isRecordingVoice = true
                                    }
                                } else {
                                    pendingMicAction = "voice_note"
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .testTag("voice_note_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = AppStrings.t("voice_note", appLanguage),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                if (!isMediaUploading) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            },
                            enabled = !isMediaUploading,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .testTag("chat_attach_photo_button")
                        ) {
                            if (isMediaUploading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Image,
                                    contentDescription = AppStrings.t("attach_photo", appLanguage),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    } else {
                        IconButton(
                            onClick = { showComposerTools = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                                .testTag("composer_expand_tools_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = AppStrings.t("composer_more_tools", appLanguage),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        border = BorderStroke(
                            1.dp,
                            if (isTyping) MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp, max = 140.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (inputMessage.isEmpty()) {
                                Text(
                                    text = AppStrings.t("type_message", appLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = inputMessage,
                                onValueChange = { next ->
                                    inputMessage = next
                                    if (next.isEmpty()) showComposerTools = false
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                maxLines = 5,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_text_input")
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (inputMessage.isNotBlank()) {
                                val activeEdit = editingMessage
                                if (activeEdit != null) {
                                    viewModel.editMessage(conversationId, activeEdit.id, inputMessage)
                                    editingMessage = null
                                } else {
                                    val reply = replyingToMessage
                                    viewModel.sendMessage(
                                        conversationId = conversationId,
                                        text = inputMessage,
                                        replyToMessageId = reply?.id,
                                        replyToText = reply?.text,
                                        replyToSenderName = reply?.let { if (it.isMine) currentUser.name else conversation?.peerUser?.name }
                                    )
                                    replyingToMessage = null
                                }
                                inputMessage = ""
                                showComposerTools = false
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (isTyping) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = AppStrings.t("send", appLanguage),
                            tint = if (isTyping) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    forwardingMessage?.let { msgToForward ->
        ForwardMessageSheet(
            conversations = conversations,
            suggestedUsers = allUsers,
            currentConversationId = conversationId,
            appLanguage = appLanguage,
            onSelectConversation = { targetConvId ->
                viewModel.forwardMessage(msgToForward, targetConvId)
            },
            onSelectUser = { peer ->
                val conv = viewModel.repository.getOrCreateConversationForUser(peer)
                viewModel.forwardMessage(msgToForward, conv.id)
            },
            onDismiss = { forwardingMessage = null }
        )
    }
}
