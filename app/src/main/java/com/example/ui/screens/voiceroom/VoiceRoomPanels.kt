package com.example.ui.screens.voiceroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioRouteMode
import com.example.model.EntranceStyle
import com.example.model.GiftCategory
import com.example.model.RoomBackgroundTheme
import com.example.model.SoundCategory
import com.example.model.SoundEffectItem
import com.example.model.SoundboardCatalog
import com.example.model.SupporterRankEntry
import com.example.model.UserProfile
import com.example.model.VoiceGift
import com.example.model.VoiceGiftCatalog
import com.example.model.VoiceRoom
import com.example.model.VoiceRoomLiveEvent
import com.example.model.VoiceRoomPoll
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
fun VoiceRoomGiftCenterDialog(
    room: VoiceRoom,
    currentUser: UserProfile,
    supporters: List<SupporterRankEntry>,
    appLanguage: AppLanguage,
    onSendGift: (VoiceGift, UserProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val recipients = remember(room.host, room.speakers) {
        (listOf(room.host) + room.speakers).distinctBy { it.id }
    }
    var selectedRecipient by remember(recipients) { mutableStateOf(recipients.firstOrNull() ?: room.host) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(GiftCategory.BASIC) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 20.dp)
                .testTag("gift_studio_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🎁 ${AppStrings.t("vr_gifts_title", appLanguage)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${AppStrings.t("vibe_xp_label", appLanguage)}: ${currentUser.vibeXp} XP",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White)
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(AppStrings.t("vr_gifts_title", appLanguage), fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("${AppStrings.t("vr_leaderboard_tab", appLanguage)} (${supporters.size})", fontWeight = FontWeight.Bold) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (selectedTab == 0) {
                    // Recipient Selector
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recipients.forEach { person ->
                            val isChosen = selectedRecipient.id == person.id
                            FilterChip(
                                selected = isChosen,
                                onClick = { selectedRecipient = person },
                                label = { Text("${person.flagEmoji} ${person.name.split(" ").first()}", maxLines = 1) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Category Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GiftCategory.entries.forEach { cat ->
                            val label = when (appLanguage) {
                                AppLanguage.DARI_PERSIAN -> cat.labelFa
                                AppLanguage.PASHTO -> cat.labelPs
                                AppLanguage.ENGLISH -> cat.labelEn
                            }
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text("${cat.emoji} $label") },
                                modifier = Modifier.testTag("gift_cat_${cat.name}")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val itemsInCat = remember(selectedCategory) {
                        VoiceGiftCatalog.gifts.filter { it.category == selectedCategory }
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .heightIn(min = 80.dp, max = 340.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(itemsInCat, key = { it.id }) { gift ->
                            val canAfford = gift.xpCost == 0 || currentUser.vibeXp >= gift.xpCost
                            val giftTitle = when (appLanguage) {
                                AppLanguage.DARI_PERSIAN -> gift.nameFa
                                AppLanguage.PASHTO -> gift.namePs
                                AppLanguage.ENGLISH -> gift.nameEn
                            }
                            val accent = Color(gift.primaryColorHex)
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                                border = BorderStroke(1.dp, accent.copy(alpha = if (canAfford) 0.55f else 0.18f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(text = gift.emoji, fontSize = 30.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = giftTitle,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (gift.xpCost == 0) AppStrings.t("vr_free_badge", appLanguage) else "${gift.xpCost} XP",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (canAfford) accent else Color(0xFF94A3B8),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Button(
                                        onClick = {
                                            onSendGift(gift, selectedRecipient)
                                            onDismiss()
                                        },
                                        enabled = canAfford,
                                        shape = RoundedCornerShape(10.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = accent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 40.dp)
                                            .testTag("send_gift_${gift.id}")
                                    ) {
                                        Text(
                                            text = AppStrings.t("vr_send_gift", appLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF090D16)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (supporters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ ${AppStrings.t("vr_leaderboard_tab", appLanguage)}",
                                color = Color(0xFF94A3B8)
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.heightIn(min = 160.dp, max = 320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(supporters, key = { it.userId }) { entry ->
                                val idx = supporters.indexOf(entry)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF1E293B),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = when (idx) {
                                                    0 -> "🥇"
                                                    1 -> "🥈"
                                                    2 -> "🥉"
                                                    else -> "#${idx + 1}"
                                                },
                                                fontSize = 18.sp
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = "${entry.flagEmoji} ${entry.userName}",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = "${entry.giftsCount} 🎁 • ${entry.totalXpGifted} XP",
                                            color = Color(0xFFFACC15),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceRoomSoundboardDialog(
    playingSoundId: String?,
    favoriteSoundIds: Set<String>,
    volume: Float,
    appLanguage: AppLanguage,
    onVolumeChange: (Float) -> Unit,
    onPreviewSound: (String) -> Unit,
    onTriggerSound: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onStopAll: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf<SoundCategory?>(null) }
    val filteredEffects = remember(selectedCategory, favoriteSoundIds) {
        val list = if (selectedCategory == null) {
            SoundboardCatalog.effects
        } else {
            SoundboardCatalog.effects.filter { it.category == selectedCategory }
        }
        list.sortedByDescending { it.id in favoriteSoundIds }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 20.dp)
                .testTag("soundboard_dialog")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🎛️ ${AppStrings.t("vr_soundboard_title", appLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = onStopAll,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFFF43F5E))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.t("vr_stop_sounds", appLanguage), color = Color(0xFFF43F5E), style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White)
                        }
                    }
                }

                // Independent Volume Slider
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.VolumeUp, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${(volume * 100).toInt()}%",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Category filter row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("All") }
                    )
                    SoundCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = {
                                Text(if (appLanguage == AppLanguage.DARI_PERSIAN) cat.labelFa else cat.labelEn)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 80.dp, max = 330.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredEffects, key = { it.id }) { item ->
                        SoundEffectRow(
                            item = item,
                            isPlaying = playingSoundId == item.id,
                            isFavorite = item.id in favoriteSoundIds,
                            appLanguage = appLanguage,
                            onPreview = { onPreviewSound(item.id) },
                            onBroadcast = { onTriggerSound(item.id) },
                            onToggleFav = { onToggleFavorite(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SoundEffectRow(
    item: SoundEffectItem,
    isPlaying: Boolean,
    isFavorite: Boolean,
    appLanguage: AppLanguage,
    onPreview: () -> Unit,
    onBroadcast: () -> Unit,
    onToggleFav: () -> Unit
) {
    val label = if (appLanguage == AppLanguage.DARI_PERSIAN) item.titleFa else item.titleEn
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isPlaying) Color(0xFF1E3A8A) else Color(0xFF1E293B),
        border = if (isPlaying) BorderStroke(1.dp, Color(0xFF38BDF8)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = item.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onToggleFav, modifier = Modifier.size(44.dp)) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color(0xFFFACC15) else Color(0xFF475569)
                )
            }
            IconButton(onClick = onPreview, modifier = Modifier.size(44.dp)) {
                Icon(Icons.Filled.PlayArrow, contentDescription = "Preview", tint = Color(0xFF38BDF8))
            }
            Button(
                onClick = onBroadcast,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA855F7)),
                modifier = Modifier
                    .heightIn(min = 40.dp)
                    .testTag("soundboard_play_${item.id}")
            ) {
                Icon(Icons.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun VoiceRoomChatAndPollsDialog(
    room: VoiceRoom,
    events: List<VoiceRoomLiveEvent>,
    polls: List<VoiceRoomPoll>,
    currentUid: String,
    isModerator: Boolean,
    appLanguage: AppLanguage,
    onSendChat: (String) -> Unit,
    onCreatePoll: (question: String, options: List<String>) -> Unit,
    onVotePoll: (pollId: String, optionIndex: Int) -> Unit,
    onClosePoll: (pollId: String) -> Unit,
    onUpdatePinnedNotice: (String?) -> Unit,
    onDismiss: () -> Unit
) {
    var messageInput by remember { mutableStateOf("") }
    var lastChatSendAtMs by remember { mutableLongStateOf(0L) }
    var showCreatePollForm by remember { mutableStateOf(false) }
    var pollQuestion by remember { mutableStateOf("") }
    val pollOptions = remember { mutableStateListOf("", "") }
    var noticeDraft by remember(room.pinnedNotice) { mutableStateOf(room.pinnedNotice.orEmpty()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF22D3EE).copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("room_chat_polls_dialog")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "💬 ${AppStrings.t("vr_room_chat_title", appLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showCreatePollForm = !showCreatePollForm },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.heightIn(min = 40.dp)
                        ) {
                            Text("+ Poll", color = Color(0xFF22D3EE), style = MaterialTheme.typography.labelSmall)
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_room_chat_btn")
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White)
                        }
                    }
                }

                if (isModerator) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = noticeDraft,
                            onValueChange = { noticeDraft = it },
                            label = { Text(AppStrings.t("vr_pinned_notice", appLanguage)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = { onUpdatePinnedNotice(noticeDraft) },
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(AppStrings.t("pin_label", appLanguage))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (showCreatePollForm) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            OutlinedTextField(
                                value = pollQuestion,
                                onValueChange = { pollQuestion = it },
                                label = { Text(AppStrings.t("vr_create_poll", appLanguage)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            pollOptions.forEachIndexed { idx, value ->
                                OutlinedTextField(
                                    value = value,
                                    onValueChange = { pollOptions[idx] = it },
                                    label = { Text("${AppStrings.t("poll_option_hint", appLanguage)} ${idx + 1}") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 4.dp)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (pollOptions.size < 4) {
                                    OutlinedButton(onClick = { pollOptions.add("") }) {
                                        Text(AppStrings.t("add_poll_option", appLanguage), color = Color.White)
                                    }
                                }
                                Button(
                                    onClick = {
                                        onCreatePoll(pollQuestion, pollOptions.toList())
                                        pollQuestion = ""
                                        pollOptions.clear()
                                        pollOptions.addAll(listOf("", ""))
                                        showCreatePollForm = false
                                    },
                                    enabled = pollQuestion.isNotBlank() && pollOptions.count { it.isNotBlank() } >= 2
                                ) {
                                    Text(AppStrings.t("send", appLanguage))
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(min = 56.dp, max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(polls, key = { "poll_${it.id}" }) { poll ->
                        val totalVotes = poll.votes.size.coerceAtLeast(1)
                        val myVote = poll.votes[currentUid]
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF172554)),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "📊 ${poll.question}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (!poll.isClosed && (isModerator || poll.createdById == currentUid)) {
                                        OutlinedButton(onClick = { onClosePoll(poll.id) }) {
                                            Text("Close", color = Color(0xFFFCA5A5), fontSize = 11.sp)
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                poll.options.forEachIndexed { idx, opt ->
                                    val count = poll.votes.values.count { it == idx }
                                    val frac = count.toFloat() / totalVotes.toFloat()
                                    val chosen = myVote == idx
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable(enabled = !poll.isClosed) { onVotePoll(poll.id, idx) }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = if (chosen) "✓ $opt" else opt,
                                                color = if (chosen) Color(0xFF38BDF8) else Color.White,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                            Text(
                                                text = "$count",
                                                color = Color(0xFF94A3B8),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        LinearProgressIndicator(
                                            progress = { frac },
                                            color = if (chosen) Color(0xFF38BDF8) else Color(0xFF6366F1),
                                            trackColor = Color(0xFF0F172A),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }

                    val chatFeed = events.filter { it.type in listOf("chat", "gift", "system") }
                    items(chatFeed, key = { "ev_${it.id}_${it.timestampMs}" }) { ev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (ev.type == "gift") Color(0xFF3B0764).copy(alpha = 0.65f)
                                    else Color(0xFF1E293B)
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${ev.senderFlag} ${ev.senderName}: ",
                                color = if (ev.type == "gift") Color(0xFFFACC15) else Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = ev.text,
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageInput,
                        onValueChange = { messageInput = it },
                        placeholder = { Text(AppStrings.t("vr_chat_placeholder", appLanguage)) },
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("room_chat_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            val draft = messageInput.trim()
                            if (draft.isNotEmpty()) {
                                val now = System.currentTimeMillis()
                                if (now - lastChatSendAtMs >= 900L) {
                                    lastChatSendAtMs = now
                                    messageInput = ""
                                    onSendChat(draft)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF06B6D4))
                            .testTag("room_chat_send_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = AppStrings.t("send", appLanguage), tint = Color(0xFF090D16))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRoomModeratorAndSettingsDialog(
    room: VoiceRoom,
    currentUid: String,
    audioRoute: AudioRouteMode,
    isBluetoothAvailable: Boolean,
    webrtcError: String?,
    defaultEntranceStyle: EntranceStyle,
    enableGiftEffects: Boolean,
    enableEntranceAnimations: Boolean,
    lowPowerVisualMode: Boolean,
    appLanguage: AppLanguage,
    onSelectAudioRoute: (AudioRouteMode) -> Unit,
    onRetryAudio: () -> Unit,
    onSelectBackgroundTheme: (RoomBackgroundTheme) -> Unit,
    onSelectEntranceStyle: (EntranceStyle) -> Unit,
    onToggleGiftEffects: (Boolean) -> Unit,
    onToggleEntranceAnimations: (Boolean) -> Unit,
    onToggleLowPower: (Boolean) -> Unit,
    onToggleStageLock: () -> Unit,
    onMuteAllSpeakers: () -> Unit,
    onSetSuccessorHost: (String?) -> Unit,
    onToggleCoHost: (String) -> Unit,
    onToggleModerator: (String) -> Unit,
    onKickOrBan: (String, Boolean) -> Unit,
    onMutePeerByHost: ((targetUid: String, shouldMute: Boolean) -> Unit)? = null,
    onTimedBan: ((targetUid: String, durationMs: Long) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val isHost = room.host.id == currentUid
    val isModerator = room.isModeratorOrHigher(currentUid)
    val participants = remember(room.speakers, room.listeners) {
        (room.speakers + room.listeners).distinctBy { it.id }.filter { it.id != room.host.id }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.4f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 600.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("room_settings_mod_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🛡️ ${AppStrings.t("vr_moderator_title", appLanguage)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White)
                    }
                }

                // Audio Output Route & WebRTC NAT Diagnostic
                Text(
                    text = AppStrings.t("vr_audio_route", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFF38BDF8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AudioRouteMode.entries.forEach { route ->
                        val enabled = route != AudioRouteMode.BLUETOOTH || isBluetoothAvailable
                        FilterChip(
                            selected = audioRoute == route,
                            enabled = enabled,
                            onClick = { onSelectAudioRoute(route) },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (route) {
                                        AudioRouteMode.SPEAKER -> Icons.Filled.VolumeUp
                                        AudioRouteMode.EARPIECE -> Icons.Filled.PhoneInTalk
                                        AudioRouteMode.BLUETOOTH -> Icons.Filled.Headphones
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(if (appLanguage == AppLanguage.DARI_PERSIAN) route.labelFa else route.labelEn)
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (webrtcError != null) Color(0xFF450A0A) else Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = webrtcError ?: AppStrings.t("vr_nat_warning", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (webrtcError != null) Color(0xFFFCA5A5) else Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onRetryAudio, modifier = Modifier.size(48.dp)) {
                            Icon(Icons.Filled.Refresh, contentDescription = AppStrings.t("vr_retry_audio", appLanguage), tint = Color(0xFF38BDF8))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))

                // Stage Theme Picker (Host / Moderator)
                if (isModerator) {
                    Text(
                        text = AppStrings.t("vr_background_themes", appLanguage),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoomBackgroundTheme.entries.forEach { bg ->
                            val isSelected = room.backgroundTheme.equals(bg.id, ignoreCase = true)
                            val title = when (appLanguage) {
                                AppLanguage.DARI_PERSIAN -> bg.titleFa
                                AppLanguage.PASHTO -> bg.titlePs
                                AppLanguage.ENGLISH -> bg.titleEn
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { onSelectBackgroundTheme(bg) },
                                label = { Text("${bg.emoji} $title") },
                                modifier = Modifier.testTag("theme_chip_${bg.id}")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // My Entrance Style Selector
                Text(
                    text = AppStrings.t("vr_entrance_styles", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFACC15),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    EntranceStyle.entries.forEach { style ->
                        val title = when (appLanguage) {
                            AppLanguage.DARI_PERSIAN -> style.titleFa
                            AppLanguage.PASHTO -> style.titlePs
                            AppLanguage.ENGLISH -> style.titleEn
                        }
                        FilterChip(
                            selected = defaultEntranceStyle == style,
                            onClick = { onSelectEntranceStyle(style) },
                            label = { Text("${style.emoji} $title") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                SettingSwitchRow(
                    title = AppStrings.t("vr_low_power_mode", appLanguage),
                    checked = lowPowerVisualMode,
                    onCheckedChange = onToggleLowPower
                )
                SettingSwitchRow(
                    title = AppStrings.t("vr_disable_gift_fx", appLanguage),
                    checked = enableGiftEffects,
                    onCheckedChange = onToggleGiftEffects
                )
                SettingSwitchRow(
                    title = AppStrings.t("vr_disable_entrance_fx", appLanguage),
                    checked = enableEntranceAnimations,
                    onCheckedChange = onToggleEntranceAnimations
                )

                if (isModerator) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = Color(0xFF334155))
                    SettingSwitchRow(
                        title = AppStrings.t("vr_stage_lock", appLanguage),
                        checked = room.isStageLocked,
                        onCheckedChange = { onToggleStageLock() }
                    )
                    Button(
                        onClick = onMuteAllSpeakers,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("mute_all_speakers_btn")
                    ) {
                        Icon(Icons.Filled.MicOff, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("vr_mute_all", appLanguage), fontWeight = FontWeight.Bold)
                    }

                    if (isHost && participants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = AppStrings.t("vr_successor_host", appLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF94A3B8)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            participants.forEach { p ->
                                FilterChip(
                                    selected = room.successorHostId == p.id,
                                    onClick = { onSetSuccessorHost(if (room.successorHostId == p.id) null else p.id) },
                                    label = { Text("${p.flagEmoji} ${p.name.split(" ").first()}") }
                                )
                            }
                        }
                    }

                    if (participants.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        participants.forEach { person ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF1E293B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = "${person.flagEmoji} ${person.name}",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (onMutePeerByHost != null) {
                                            val isPeerHostMuted = person.id in room.hostMutedPeerIds
                                            OutlinedButton(
                                                onClick = { onMutePeerByHost(person.id, !isPeerHostMuted) },
                                                modifier = Modifier.testTag("mod_mute_toggle_${person.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isPeerHostMuted) Icons.Filled.Mic else Icons.Filled.MicOff,
                                                    contentDescription = null,
                                                    tint = if (isPeerHostMuted) Color(0xFF34D399) else Color(0xFFFBBF24),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isPeerHostMuted) AppStrings.t("vr_allow_mic", appLanguage)
                                                    else AppStrings.t("mute_speaker", appLanguage),
                                                    color = if (isPeerHostMuted) Color(0xFF34D399) else Color(0xFFFBBF24),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        if (isHost) {
                                            OutlinedButton(onClick = { onToggleCoHost(person.id) }) {
                                                Text(
                                                    if (person.id in room.coHostIds) AppStrings.t("remove_cohost", appLanguage)
                                                    else AppStrings.t("make_cohost", appLanguage),
                                                    color = Color(0xFF38BDF8),
                                                    fontSize = 11.sp
                                                )
                                            }
                                            OutlinedButton(onClick = { onToggleModerator(person.id) }) {
                                                Text(
                                                    if (person.id in room.moderatorIds) AppStrings.t("vr_remove_moderator", appLanguage)
                                                    else AppStrings.t("vr_make_moderator", appLanguage),
                                                    color = Color(0xFFA855F7),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                        OutlinedButton(onClick = { onKickOrBan(person.id, false) }) {
                                            Text(AppStrings.t("vr_kick", appLanguage), color = Color(0xFFFBBF24), fontSize = 11.sp)
                                        }
                                        if (onTimedBan != null) {
                                            OutlinedButton(
                                                onClick = { onTimedBan(person.id, 900_000L) },
                                                modifier = Modifier.testTag("mod_ban_15m_${person.id}")
                                            ) {
                                                Text(AppStrings.t("vr_ban_15m", appLanguage), color = Color(0xFFFB923C), fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { onTimedBan(person.id, 3_600_000L) },
                                                modifier = Modifier.testTag("mod_ban_1h_${person.id}")
                                            ) {
                                                Text(AppStrings.t("vr_ban_1h", appLanguage), color = Color(0xFFFB7185), fontSize = 11.sp)
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { onKickOrBan(person.id, true) },
                                            modifier = Modifier.testTag("mod_ban_perm_${person.id}")
                                        ) {
                                            Text(AppStrings.t("vr_ban_perm", appLanguage), color = Color(0xFFF43F5E), fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, color = Color.White, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
