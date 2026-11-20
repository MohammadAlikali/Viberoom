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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.RoomCategory
import com.example.model.VibeCatalogs
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
    val rooms by viewModel.filteredVoiceRooms.collectAsState()
    val selectedCategory by viewModel.selectedRoomCategory.collectAsState()
    val showCreateDialog by viewModel.showCreateRoomDialog.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::openCreateRoomDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 12.dp)
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
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 680.dp)
                    .testTag("voice_rooms_screen"),
                contentPadding = PaddingValues(bottom = 112.dp)
            ) {
                item {
                    VoiceRoomsHeader(
                        appLanguage = appLanguage,
                        activeCount = rooms.size,
                        unreadNotificationsCount = unreadNotificationsCount,
                        onOpenNotifications = viewModel::openNotifications,
                        onCreateRoom = viewModel::openCreateRoomDialog
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = AppStrings.t("search_rooms_hint", appLanguage),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
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
                                        contentDescription = AppStrings.t("close", appLanguage)
                                    )
                                }
                            }
                        } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .testTag("voice_rooms_search_input")
                    )
                }

                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.t("learning_hub_title", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
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

                item {
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
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎙️", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = AppStrings.t("no_rooms_yet", appLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
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
                } else {
                    items(displayedRooms, key = { it.id }) { room ->
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
            onDismiss = viewModel::closeCreateRoomDialog,
            onCreate = { title, desc, cat, tags ->
                viewModel.createRoom(title, desc, cat, tags)
            },
            onCreateRoom = { title, desc, cat, tags, isPrivate ->
                viewModel.createRoom(title, desc, cat, tags, isPrivate)
            }
        )
    }
}

@Composable
private fun VoiceRoomsHeader(
    appLanguage: AppLanguage,
    activeCount: Int,
    unreadNotificationsCount: Int,
    onOpenNotifications: () -> Unit,
    onCreateRoom: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val liveBadgeColor = if (isLight) Color(0xFF15803D) else Color(0xFF34D399)

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
                    fontWeight = FontWeight.Bold
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
                        contentDescription = AppStrings.t("notifications_title", appLanguage)
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
                    fontWeight = FontWeight.Bold,
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
                shape = RoundedCornerShape(10.dp)
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
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Headphones,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("webrtc_live_badge", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
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
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val accentGradient = when (room.category) {
        RoomCategory.LANGUAGE_EXCHANGE -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
        RoomCategory.MUSIC_JAM -> listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))
        RoomCategory.TECH_TALK -> listOf(Color(0xFF06B6D4), Color(0xFF3B82F6))
        RoomCategory.CULTURE -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        RoomCategory.CHILL_VIBES -> listOf(Color(0xFF10B981), Color(0xFF0EA5E9))
        RoomCategory.STORYTELLING -> listOf(Color(0xFFA855F7), Color(0xFFEC4899))
        RoomCategory.ALL -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            width = 1.dp,
            color = if (isLight) {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.75f)
            } else {
                accentGradient.first().copy(alpha = 0.25f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLight) 1.dp else 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
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
                            color = accentGradient.first().copy(alpha = 0.14f)
                        ) {
                            Text(
                                text = "${room.category.iconEmoji} ${AppStrings.roomCategory(room.category, appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = accentGradient.first(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        if (room.isPrivate) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.testTag("room_private_badge_${room.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Lock,
                                        contentDescription = AppStrings.t("private_badge", appLanguage),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = AppStrings.t("private_badge", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SpeakingSoundWave(color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${room.listenersCount} ${AppStrings.t("listening_count", appLanguage)}",
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
                    fontWeight = FontWeight.Bold
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
                                    isSpeaking = speaker.id == room.activeSpeakerId || speaker.id == room.host.id,
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
                        Text(AppStrings.t("join_room", appLanguage), fontWeight = FontWeight.Bold)
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
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
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

@Composable
fun CreateRoomDialog(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: RoomCategory, tags: List<String>) -> Unit,
    onCreateRoom: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean) -> Unit)? = null
) {
    Dialog(onDismissRequest = onDismiss) {
        CreateRoomDialogContent(
            appLanguage = appLanguage,
            onDismiss = onDismiss,
            onCreate = onCreate,
            onCreateRoom = onCreateRoom
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRoomDialogContent(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: RoomCategory, tags: List<String>) -> Unit,
    onCreateRoom: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean) -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RoomCategory.LANGUAGE_EXCHANGE) }
    val selectedTags = remember { mutableStateListOf("Daily Practice") }
    var isPrivate by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(22.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "🎙️ ${AppStrings.t("create_room_title", appLanguage)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(AppStrings.t("room_title_label", appLanguage)) },
                placeholder = { Text(AppStrings.t("room_title_hint", appLanguage)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_room_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(AppStrings.t("description_label", appLanguage)) },
                placeholder = { Text(AppStrings.t("room_desc_hint", appLanguage)) },
                maxLines = 3,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("select_category", appLanguage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                RoomCategory.entries.filterNot { it == RoomCategory.ALL }.forEach { cat ->
                    InterestChip(
                        text = "${cat.iconEmoji} ${AppStrings.roomCategory(cat, appLanguage)}",
                        isSelected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("pick_room_tags", appLanguage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.testTag("create_room_tags_picker")
            ) {
                VibeCatalogs.roomTags.forEach { tag ->
                    val isPicked = tag in selectedTags
                    InterestChip(
                        text = if (isPicked) "✓ #$tag" else "#$tag",
                        isSelected = isPicked,
                        onClick = {
                            if (isPicked) selectedTags.remove(tag) else selectedTags.add(tag)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable { isPrivate = !isPrivate },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = AppStrings.t("private_room_toggle", appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Switch(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it },
                    modifier = Modifier.testTag("create_room_private_switch")
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(AppStrings.t("cancel", appLanguage))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val cleanTitle = title.trim()
                            val cleanDesc = description.trim()
                            val tags = selectedTags.toList()
                            if (onCreateRoom != null) {
                                onCreateRoom(cleanTitle, cleanDesc, selectedCategory, tags, isPrivate)
                            } else {
                                onCreate(cleanTitle, cleanDesc, selectedCategory, tags)
                            }
                        }
                    },
                    enabled = title.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("submit_create_room")
                ) {
                    Text(AppStrings.t("go_live", appLanguage), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
