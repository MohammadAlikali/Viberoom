package com.example.ui.screens.voiceroom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.FrontHand
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.util.Locale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.ReactionFloatingBubble
import com.example.ui.components.SpeakingSoundWave
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
fun ActiveVoiceRoomModal(
    room: VoiceRoom,
    viewModel: VibeRoomViewModel,
    onMinimize: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val isHandRaised by viewModel.isHandRaised.collectAsState()
    val activeReactions by viewModel.activeReactions.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val connectedPeers by viewModel.voiceRoomConnectedPeers.collectAsState()
    val connectedRoomPeerIds by viewModel.connectedRoomPeerIds.collectAsState()
    val roomIceState by viewModel.roomIceState.collectAsState()
    val isSpeaker by viewModel.isSpeaker.collectAsState()
    val isRoomSpeakerphone by viewModel.isRoomSpeakerphone.collectAsState()
    val roomAssistantOutput by viewModel.roomAssistantOutput.collectAsState()
    val isRoomAssistantLoading by viewModel.isRoomAssistantLoading.collectAsState()

    var showReportRoomDialog by remember { mutableStateOf(false) }
    var showSummaryConsentDialog by remember { mutableStateOf(false) }
    var showLiveCaptions by remember { mutableStateOf(false) }
    var liveCaptionText by remember { mutableStateOf("") }

    val isCurrentUserHost = room.host.id == currentUser.id
    val isCoHost = currentUser.id in room.coHostIds
    val isModerator = isCurrentUserHost || isCoHost
    val canSpeakOnStage = isSpeaker || isCurrentUserHost

    val raisedHandUsers = remember(room.raisedHandIds, room.listeners, room.speakers) {
        val allRoomUsers = (room.speakers + room.listeners).associateBy { it.id }
        room.raisedHandIds.mapNotNull { uid -> allRoomUsers[uid] }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            if (isMicMuted) viewModel.toggleMic()
        } else {
            viewModel.showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage))
        }
    }

    val captionPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            showLiveCaptions = true
        } else {
            viewModel.showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage))
        }
    }

    DisposableEffect(showLiveCaptions, appLanguage) {
        if (!showLiveCaptions) {
            return@DisposableEffect onDispose {}
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            showLiveCaptions = false
            return@DisposableEffect onDispose {}
        }
        val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                when (appLanguage) {
                    AppLanguage.DARI_PERSIAN -> "fa-AF"
                    AppLanguage.PASHTO -> "ps-AF"
                    AppLanguage.ENGLISH -> Locale.getDefault().toLanguageTag()
                }
            )
        }
        var active = true
        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (active && error != SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    try {
                        recognizer.startListening(intent)
                    } catch (_: Exception) {
                    }
                }
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) liveCaptionText = text
                if (active) {
                    try {
                        recognizer.startListening(intent)
                    } catch (_: Exception) {
                    }
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) liveCaptionText = text
            }
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        try {
            recognizer.startListening(intent)
        } catch (_: Exception) {
            showLiveCaptions = false
        }
        onDispose {
            active = false
            try {
                recognizer.stopListening()
                recognizer.destroy()
            } catch (_: Exception) {
            }
        }
    }

    Dialog(
        onDismissRequest = onMinimize,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("active_voice_room_modal"),
            contentAlignment = Alignment.TopCenter
        ) {
            activeReactions.forEach { reaction ->
                ReactionFloatingBubble(
                    emoji = reaction.emoji,
                    offsetX = reaction.offsetX,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .padding(horizontal = 18.dp, vertical = 10.dp)
            ) {
                // Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                            .testTag("minimize_room_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = AppStrings.t("close", appLanguage),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    val isAudioConnected = roomIceState == "connected" || connectedPeers > 0
                    val statusTint = if (isAudioConnected) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = statusTint.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, statusTint.copy(alpha = 0.35f)),
                        modifier = Modifier.testTag("voice_room_webrtc_status_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isAudioConnected) {
                                SpeakingSoundWave(color = statusTint)
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Headphones,
                                    contentDescription = null,
                                    tint = statusTint,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${room.listenersCount} ${AppStrings.t("listening_count", appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            val rtcLabel = when {
                                connectedPeers > 0 ->
                                    "• $connectedPeers ${AppStrings.t("webrtc_peers_connected", appLanguage)}"
                                roomIceState == "connecting" ->
                                    "• ${AppStrings.t("webrtc_connecting_badge", appLanguage)}"
                                else ->
                                    "• ${AppStrings.t("webrtc_live_badge", appLanguage)}"
                            }
                            Text(
                                text = rtcLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusTint,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isCurrentUserHost) {
                        Button(
                            onClick = { viewModel.leaveVoiceRoom() },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("end_room_button")
                        ) {
                            Text(
                                text = AppStrings.t("end_stage", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showReportRoomDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("report_room_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Flag,
                                    contentDescription = AppStrings.t("report_room", appLanguage),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            OutlinedButton(
                                onClick = onLeave,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("leave_room_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = AppStrings.t("leave_stage", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Room Title & Description Card
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = room.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (room.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = room.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Host / Co-Host Raised Hands Queue Banner
                if (isModerator && raisedHandUsers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                        ),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("raised_hands_queue_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.FrontHand,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${AppStrings.t("raised_hands_queue", appLanguage)} (${raisedHandUsers.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(raisedHandUsers, key = { it.id }) { requester ->
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            UserAvatar(user = requester, size = 32.dp, showOnlineStatus = false)
                                            Column {
                                                Text(
                                                    text = requester.name.split(" ").first(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = requester.flagEmoji,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Button(
                                                onClick = { viewModel.promotePeerToSpeaker(requester.id) },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier
                                                    .heightIn(min = 38.dp)
                                                    .testTag("promote_peer_${requester.id}")
                                            ) {
                                                Text(
                                                    text = AppStrings.t("allow_to_speak", appLanguage),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            OutlinedButton(
                                                onClick = { viewModel.demotePeerToListener(requester.id) },
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.heightIn(min = 38.dp)
                                            ) {
                                                Text(
                                                    text = AppStrings.t("dismiss_hand", appLanguage),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${AppStrings.t("stage_speakers", appLanguage)} (${room.speakers.size})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Stage Grid
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 104.dp),
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(room.speakers, key = { it.id }) { speaker ->
                        val isRemoteMuted = speaker.id in room.mutedPeerIds
                        val isSpeakerActive = if (speaker.id == currentUser.id) {
                            !isMicMuted
                        } else {
                            !isRemoteMuted && (speaker.id == room.activeSpeakerId || speaker.id in connectedRoomPeerIds)
                        }
                        val isRtcConnected = speaker.id in connectedRoomPeerIds || speaker.id == currentUser.id
                        val speakerIsCoHost = speaker.id in room.coHostIds

                        var showHostMenu by remember(speaker.id) { mutableStateOf(false) }

                        Box {
                            SpeakerStageTile(
                                speaker = speaker,
                                isHost = speaker.id == room.host.id,
                                hostLabel = AppStrings.t("host_label", appLanguage),
                                isCoHost = speakerIsCoHost,
                                coHostLabel = AppStrings.t("cohost_badge", appLanguage),
                                isSpeaking = isSpeakerActive,
                                isRtcConnected = isRtcConnected,
                                onClick = {
                                    if (isModerator && speaker.id != currentUser.id && speaker.id != room.host.id) {
                                        showHostMenu = true
                                    } else {
                                        viewModel.openUserProfile(speaker)
                                    }
                                }
                            )

                            DropdownMenu(
                                expanded = showHostMenu,
                                onDismissRequest = { showHostMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(AppStrings.t("nav_profile", appLanguage)) },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    onClick = {
                                        showHostMenu = false
                                        viewModel.openUserProfile(speaker)
                                    }
                                )
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isRemoteMuted) AppStrings.t("unmute_speaker", appLanguage)
                                            else AppStrings.t("mute_speaker", appLanguage)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            if (isRemoteMuted) Icons.Filled.Mic else Icons.Filled.MicOff,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showHostMenu = false
                                        viewModel.mutePeerByHost(speaker.id, !isRemoteMuted)
                                    }
                                )
                                if (isCurrentUserHost && speaker.id != room.host.id) {
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                if (speakerIsCoHost) AppStrings.t("remove_cohost", appLanguage)
                                                else AppStrings.t("make_cohost", appLanguage)
                                            )
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Star, contentDescription = null) },
                                        onClick = {
                                            showHostMenu = false
                                            viewModel.toggleCoHost(speaker.id)
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text(AppStrings.t("move_to_audience", appLanguage)) },
                                    leadingIcon = { Icon(Icons.Filled.Headphones, contentDescription = null) },
                                    onClick = {
                                        showHostMenu = false
                                        viewModel.demotePeerToListener(speaker.id)
                                    }
                                )
                            }
                        }
                    }
                }

                // Audience Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🎧 ${AppStrings.t("in_audience", appLanguage)} (${room.listenersCount})",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                room.listeners.take(5).forEach { listener ->
                                    var listenerMenuOpen by remember(listener.id) { mutableStateOf(false) }
                                    val listenerRaisedHand = listener.id in room.raisedHandIds

                                    Box(modifier = Modifier.padding(start = 6.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .clickable {
                                                    if (isModerator) listenerMenuOpen = true
                                                    else viewModel.openUserProfile(listener)
                                                }
                                                .testTag("listener_avatar_${listener.id}")
                                        ) {
                                            UserAvatar(
                                                user = listener,
                                                size = 32.dp,
                                                showOnlineStatus = false
                                            )
                                            if (listenerRaisedHand) {
                                                Text(
                                                    text = "✋",
                                                    fontSize = 11.sp,
                                                    modifier = Modifier.align(Alignment.BottomEnd)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = listenerMenuOpen,
                                            onDismissRequest = { listenerMenuOpen = false }
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text(AppStrings.t("allow_to_speak", appLanguage)) },
                                                leadingIcon = { Icon(Icons.Filled.Mic, contentDescription = null) },
                                                onClick = {
                                                    listenerMenuOpen = false
                                                    viewModel.promotePeerToSpeaker(listener.id)
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text(AppStrings.t("nav_profile", appLanguage)) },
                                                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                                onClick = {
                                                    listenerMenuOpen = false
                                                    viewModel.openUserProfile(listener)
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // AI Stage Tools: Party Game, Live Captions & Summary
                if (showLiveCaptions) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("live_captions_banner")
                    ) {
                        Text(
                            text = "💬 ${liveCaptionText.ifBlank { AppStrings.t("listening_count", appLanguage) }}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                if (isRoomAssistantLoading || !roomAssistantOutput.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp)
                            .testTag("room_assistant_output_card")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isRoomAssistantLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = roomAssistantOutput.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isRoomAssistantLoading) {
                                IconButton(
                                    onClick = viewModel::clearRoomAssistantOutput,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRoomAssistantLoading) {
                                viewModel.generateRoomAssistant("game")
                            }
                            .testTag("room_party_game_btn")
                    ) {
                        Text(
                            text = "🎮 ${AppStrings.t("party_game_cta", appLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (showLiveCaptions) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (showLiveCaptions) {
                                    showLiveCaptions = false
                                } else {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        showLiveCaptions = true
                                    } else {
                                        captionPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                            }
                            .testTag("room_live_captions_btn")
                    ) {
                        Text(
                            text = "💬 ${AppStrings.t("live_captions_cta", appLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (showLiveCaptions) MaterialTheme.colorScheme.onTertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isRoomAssistantLoading) {
                                showSummaryConsentDialog = true
                            }
                            .testTag("room_ai_summary_btn")
                    ) {
                        Text(
                            text = "✨ ${AppStrings.t("ai_summary_cta", appLanguage)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }

                // Reactions Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reactionList = listOf("🔥", "❤️", "👏", "🎵", "🌍", "💡")
                    reactionList.forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.sendReaction(emoji) }
                                .testTag("reaction_button_$emoji")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bottom Control Dock
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    tonalElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = viewModel::toggleRaiseHand,
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isHandRaised) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isHandRaised) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("raise_hand_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FrontHand,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isHandRaised) AppStrings.t("hand_raised", appLanguage) else AppStrings.t("raise_hand", appLanguage),
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = viewModel::toggleRoomSpeakerphone,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRoomSpeakerphone) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.surface
                                )
                                .testTag("voice_room_speaker_toggle")
                        ) {
                            Icon(
                                imageVector = if (isRoomSpeakerphone) Icons.Filled.VolumeUp else Icons.Filled.PhoneInTalk,
                                contentDescription = AppStrings.t("speakerphone", appLanguage),
                                tint = if (isRoomSpeakerphone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                if (!canSpeakOnStage) {
                                    viewModel.toggleMic()
                                } else if (isMicMuted) {
                                    val hasMic = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.RECORD_AUDIO
                                    ) == PackageManager.PERMISSION_GRANTED
                                    if (hasMic) {
                                        viewModel.toggleMic()
                                    } else {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                } else {
                                    viewModel.toggleMic()
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = when {
                                    !canSpeakOnStage -> MaterialTheme.colorScheme.secondaryContainer
                                    isMicMuted -> MaterialTheme.colorScheme.surface
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                contentColor = when {
                                    !canSpeakOnStage -> MaterialTheme.colorScheme.onSecondaryContainer
                                    isMicMuted -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onPrimary
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 50.dp)
                                .testTag("voice_room_mic_toggle")
                        ) {
                            Icon(
                                imageVector = when {
                                    !canSpeakOnStage -> Icons.Filled.FrontHand
                                    isMicMuted -> Icons.Filled.MicOff
                                    else -> Icons.Filled.Mic
                                },
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = when {
                                    !canSpeakOnStage -> AppStrings.t("request_stage", appLanguage)
                                    isMicMuted -> AppStrings.t("mic_off", appLanguage)
                                    else -> AppStrings.t("speaking", appLanguage)
                                },
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReportRoomDialog) {
        val reasons = listOf("Spam / Promotion", "Harassment", "Inappropriate Content", "Misleading Topic")
        var selectedReason by remember { mutableStateOf(reasons.first()) }
        var details by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showReportRoomDialog = false }) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.t("report_room", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    reasons.forEach { reason ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = reason }
                                .padding(vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(
                                        color = if (selectedReason == reason) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = reason, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = details,
                        onValueChange = { details = it },
                        label = { Text(AppStrings.t("description_label", appLanguage)) },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = { showReportRoomDialog = false },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(AppStrings.t("cancel", appLanguage))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = {
                                viewModel.reportVoiceRoom(room.id, selectedReason, details)
                                showReportRoomDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(AppStrings.t("send", appLanguage), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showSummaryConsentDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryConsentDialog = false },
            title = {
                Text(
                    text = AppStrings.t("ai_summary_consent_title", appLanguage),
                    fontWeight = FontWeight.Bold
                )
            },
            text = { Text(AppStrings.t("ai_summary_consent_body", appLanguage)) },
            confirmButton = {
                Button(
                    onClick = {
                        showSummaryConsentDialog = false
                        viewModel.generateAndSaveRoomSummary()
                    },
                    modifier = Modifier.testTag("confirm_room_summary_btn")
                ) {
                    Text(AppStrings.t("accept", appLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSummaryConsentDialog = false }) {
                    Text(AppStrings.t("cancel", appLanguage))
                }
            }
        )
    }
}

@Composable
fun SpeakerStageTile(
    speaker: UserProfile,
    isHost: Boolean,
    hostLabel: String = "HOST",
    isCoHost: Boolean = false,
    coHostLabel: String = "CO-HOST",
    isSpeaking: Boolean,
    isRtcConnected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(6.dp)
            .testTag("speaker_tile_${speaker.id}")
    ) {
        Box(contentAlignment = Alignment.Center) {
            UserAvatar(
                user = speaker,
                size = 68.dp,
                isSpeaking = isSpeaking,
                showOnlineStatus = false
            )

            if (isRtcConnected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSpeaking) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isSpeaking) Icons.Filled.VolumeUp else Icons.Filled.MicOff,
                    contentDescription = null,
                    tint = if (isSpeaking) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = speaker.name.split(" ").first(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = speaker.flagEmoji, fontSize = 11.sp)
        }

        if (isHost) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = hostLabel,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        } else if (isCoHost) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = coHostLabel,
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
