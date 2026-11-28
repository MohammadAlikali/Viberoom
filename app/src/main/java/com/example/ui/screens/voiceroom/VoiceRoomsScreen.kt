package com.example.ui.screens.voiceroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.model.RoomBackgroundTheme
import com.example.model.RoomCategory
import com.example.model.VoiceRoom
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.InterestChip
import com.example.ui.components.SpeakingSoundWave
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
fun VoiceRoomsScreen(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier
) {
    val rooms by viewModel.filteredVoiceRooms.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedRoomCategory.collectAsStateWithLifecycle()
    val showCreateDialog by viewModel.showCreateRoomDialog.collectAsStateWithLifecycle()
    val isCreatingRoom by viewModel.isCreatingRoom.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount = remember(notifications) { notifications.count { !it.isRead } }

    var searchQuery by remember { mutableStateOf("") }
    val displayedRooms = remember(rooms, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            rooms
        } else {
            rooms.filter { room ->
                room.title.lowercase().contains(q) ||
                    room.description.lowercase().contains(q) ||
                    room.host.name.lowercase().contains(q) ||
                    room.tags.any { it.lowercase().contains(q) }
            }
        }
    }

    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreateRoomDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .testTag("create_room_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("create_room_cta", appLanguage),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("voice_rooms_screen"),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
                item(key = "vr_header", contentType = "header") {
                    VoiceRoomsHeader(
                        appLanguage = appLanguage,
                        activeCount = rooms.size,
                        unreadNotificationsCount = unreadNotificationsCount,
                        isLightTheme = isLightTheme,
                        onOpenNotifications = viewModel::openNotifications,
                        onCreateRoom = viewModel::openCreateRoomDialog
                    )
                }

                item(key = "vr_search", contentType = "search") {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = AppStrings.t("search_rooms_hint", appLanguage),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = AppStrings.t("search_rooms_hint", appLanguage),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = if (searchQuery.isNotEmpty()) {
                            {
                                IconButton(
                                    onClick = { searchQuery = "" },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = AppStrings.t("close", appLanguage),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(18.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("voice_rooms_search_input")
                    )
                }

                item(key = "vr_learning_banner", contentType = "banner") {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = if (isLightTheme) 0.52f else 0.34f
                            )
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.openLearningHub() }
                            .testTag("open_learning_studio_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.t("learning_hub_title", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = AppStrings.t("learning_hub_subtitle", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                item(key = "vr_categories", contentType = "categories") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoomCategory.entries.forEach { cat ->
                            InterestChip(
                                text = "${cat.iconEmoji} ${AppStrings.roomCategory(cat, appLanguage)}",
                                isSelected = selectedCategory == cat,
                                onClick = { viewModel.setSelectedRoomCategory(cat) },
                                modifier = Modifier.testTag("room_cat_${cat.name}")
                            )
                        }
                    }
                }

                if (displayedRooms.isEmpty()) {
                    item(key = "vr_empty", contentType = "empty") {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLightTheme) 0.72f else 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎙️", fontSize = 44.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = AppStrings.t("no_rooms_yet", appLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    if (searchQuery.isNotBlank() || selectedCategory != RoomCategory.ALL) {
                                        OutlinedButton(
                                            onClick = {
                                                searchQuery = ""
                                                viewModel.setSelectedRoomCategory(RoomCategory.ALL)
                                            },
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.heightIn(min = 48.dp)
                                        ) {
                                            Text(AppStrings.t("reset_filters", appLanguage))
                                        }
                                    }
                                    Button(
                                        onClick = viewModel::openCreateRoomDialog,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.heightIn(min = 48.dp)
                                    ) {
                                        Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = AppStrings.t("create_room_cta", appLanguage),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    items(
                        items = displayedRooms,
                        key = { it.id },
                        contentType = { "voice_room_card" }
                    ) { room ->
                        VoiceRoomCard(
                            room = room,
                            appLanguage = appLanguage,
                            onJoin = { viewModel.joinVoiceRoom(room) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateRoomDialog(
            appLanguage = appLanguage,
            isCreating = isCreatingRoom,
            onDismiss = { if (!isCreatingRoom) viewModel.closeCreateRoomDialog() },
            onCreate = { title, desc, cat, tags ->
                viewModel.createRoom(title, desc, cat, tags)
            },
            onCreateRoom = { title, desc, cat, tags, isPrivate ->
                viewModel.createRoom(title, desc, cat, tags, isPrivate)
            },
            onCreateConfigured = { title, desc, cat, tags, isPrivate, bgTheme, stageLocked ->
                viewModel.createRoom(
                    title = title,
                    description = desc,
                    category = cat,
                    tags = tags,
                    isPrivate = isPrivate,
                    backgroundTheme = bgTheme,
                    isStageLocked = stageLocked
                )
            }
        )
    }
}

@Composable
private fun VoiceRoomsHeader(
    appLanguage: AppLanguage,
    activeCount: Int,
    unreadNotificationsCount: Int,
    isLightTheme: Boolean,
    onOpenNotifications: () -> Unit,
    onCreateRoom: () -> Unit
) {
    val liveBadgeColor = if (isLightTheme) Color(0xFF047857) else Color(0xFF34D399)

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLightTheme) 0.7f else 0.35f)
        ),
        shadowElevation = if (isLightTheme) 1.dp else 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Image(
                painter = painterResource(id = R.drawable.viberoom_hero_banner),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .alpha(if (isLightTheme) 0.08f else 0.16f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = AppStrings.t("live_voice_rooms", appLanguage),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = AppStrings.t("voice_rooms_subtitle", appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = onOpenNotifications,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("voice_rooms_notifications_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge { Text("$unreadNotificationsCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Notifications,
                                contentDescription = AppStrings.t("notifications_title", appLanguage),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Button(
                        onClick = onCreateRoom,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("create_room_header_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = AppStrings.t("create_room_cta", appLanguage),
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = liveBadgeColor.copy(alpha = 0.14f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, liveBadgeColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(liveBadgeColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${AppStrings.t("live_badge", appLanguage)} ($activeCount)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = liveBadgeColor
                            )
                        }
                    }

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${AppStrings.t("webrtc_live_badge", appLanguage)} • ${AppStrings.t("max_peers_badge", appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VoiceRoomCard(
    room: VoiceRoom,
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onJoin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgTheme = remember(room.backgroundTheme) { RoomBackgroundTheme.fromId(room.backgroundTheme) }
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val rawAccent = Color(bgTheme.accentHex)
    val accent = if (isLight && rawAccent.luminance() > 0.45f) MaterialTheme.colorScheme.primary else rawAccent
    val accentGradient = listOf(accent, MaterialTheme.colorScheme.tertiary)
    val liveGreen = if (isLight) Color(0xFF047857) else Color(0xFF34D399)
    val privateWarnColor = if (isLight) Color(0xFFB45309) else Color(0xFFFACC15)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLight) 0.75f else 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLight) 1.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onJoin() }
            .testTag("room_card_${room.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Brush.horizontalGradient(accentGradient))
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FlowRow(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = accent.copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${room.category.iconEmoji} ${AppStrings.roomCategory(room.category, appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = bgTheme.emoji,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                        if (room.isPrivate) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = privateWarnColor.copy(alpha = 0.16f),
                                modifier = Modifier.testTag("room_private_badge_${room.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = AppStrings.t("private_badge", appLanguage),
                                        tint = privateWarnColor,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = AppStrings.t("private_badge", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = privateWarnColor
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!room.activeSpeakerId.isNullOrBlank()) {
                            SpeakingSoundWave(color = liveGreen)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Headphones,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${room.speakers.size}/${room.maxAudioParticipants} • ${room.listenersCount} ${AppStrings.t("listening_count", appLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = room.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (room.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = room.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        val speakerPreview = room.speakers.ifEmpty { listOf(room.host) }.take(4)
                        speakerPreview.forEach { speaker ->
                            Box(modifier = Modifier.padding(end = 4.dp)) {
                                UserAvatar(
                                    user = speaker,
                                    size = 38.dp,
                                    isSpeaking = speaker.id == room.activeSpeakerId,
                                    showOnlineStatus = false
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column {
                            Text(
                                text = "${AppStrings.t("host_label", appLanguage)}: ${room.host.name}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${room.host.flagEmoji} ${room.host.countryName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = onJoin,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("join_room_button_${room.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Headphones,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = AppStrings.t("join_room", appLanguage),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                if (room.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        room.tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
