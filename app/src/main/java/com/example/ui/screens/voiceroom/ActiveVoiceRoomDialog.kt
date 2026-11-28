package com.example.ui.screens.voiceroom

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AudioRouteMode
import com.example.model.RoomBackgroundTheme
import com.example.model.VoiceRoom
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.ReactionFloatingBubble
import com.example.ui.components.SpeakingSoundWave
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import java.util.Locale

@Composable
fun ActiveVoiceRoomModal(
    room: VoiceRoom,
    viewModel: VibeRoomViewModel,
    onMinimize: () -> Unit,
    onLeave: () -> Unit
) {
    val context = LocalContext.current
    val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()
    val isHostMuted by viewModel.isHostMuted.collectAsStateWithLifecycle()
    val isHandRaised by viewModel.isHandRaised.collectAsStateWithLifecycle()
    val activeReactions by viewModel.activeReactions.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val connectedPeers by viewModel.voiceRoomConnectedPeers.collectAsStateWithLifecycle()
    val connectedRoomPeerIds by viewModel.connectedRoomPeerIds.collectAsStateWithLifecycle()
    val activeSpeakingPeerIds by viewModel.activeSpeakingPeerIds.collectAsStateWithLifecycle()
    val localAudioLevel by viewModel.localAudioLevel.collectAsStateWithLifecycle()
    val roomIceState by viewModel.roomIceState.collectAsStateWithLifecycle()
    val isSpeaker by viewModel.isSpeaker.collectAsStateWithLifecycle()
    val audioRoute by viewModel.audioRoute.collectAsStateWithLifecycle()
    val isBluetoothAvailable by viewModel.isBluetoothAvailable.collectAsStateWithLifecycle()
    val webrtcError by viewModel.webrtcError.collectAsStateWithLifecycle()
    val isCapacityReached by viewModel.isRoomCapacityReached.collectAsStateWithLifecycle()
    val hasMicPermission by viewModel.hasMicPermission.collectAsStateWithLifecycle()
    val isMicCapturing by viewModel.isMicCapturing.collectAsStateWithLifecycle()
    val micPermissionDeniedInRoom by viewModel.micPermissionDeniedInRoom.collectAsStateWithLifecycle()
    val roomAssistantOutput by viewModel.roomAssistantOutput.collectAsStateWithLifecycle()
    val isRoomAssistantLoading by viewModel.isRoomAssistantLoading.collectAsStateWithLifecycle()

    val roomEvents by viewModel.roomEvents.collectAsStateWithLifecycle()
    val roomPolls by viewModel.roomPolls.collectAsStateWithLifecycle()
    val supporterLeaderboard by viewModel.supporterLeaderboard.collectAsStateWithLifecycle()
    val currentGiftVisual by viewModel.currentGiftVisual.collectAsStateWithLifecycle()
    val currentEntranceVisual by viewModel.currentEntranceVisual.collectAsStateWithLifecycle()
    val defaultEntranceStyle by viewModel.defaultEntranceStyle.collectAsStateWithLifecycle()
    val soundEffectsVolume by viewModel.soundEffectsVolume.collectAsStateWithLifecycle()
    val enableGiftEffects by viewModel.enableGiftEffects.collectAsStateWithLifecycle()
    val enableEntranceAnimations by viewModel.enableEntranceAnimations.collectAsStateWithLifecycle()
    val lowPowerVisualMode by viewModel.lowPowerVisualMode.collectAsStateWithLifecycle()
    val favoriteSoundIds by viewModel.favoriteSoundIds.collectAsStateWithLifecycle()
    val playingSoundId by viewModel.playingSoundId.collectAsStateWithLifecycle()

    var showReportRoomDialog by remember { mutableStateOf(false) }
    var showSummaryConsentDialog by remember { mutableStateOf(false) }
    var showLiveCaptions by remember { mutableStateOf(false) }
    var liveCaptionText by remember { mutableStateOf("") }
    var showGiftCenterDialog by remember { mutableStateOf(false) }
    var showSoundboardDialog by remember { mutableStateOf(false) }
    var showChatPollsDialog by remember { mutableStateOf(false) }
    var showSettingsModDialog by remember { mutableStateOf(false) }
    var showStudioToolsSheet by remember { mutableStateOf(false) }

    val isCurrentUserHost = room.host.id == currentUser.id
    val isCoHost = currentUser.id in room.coHostIds
    val isRoomMod = currentUser.id in room.moderatorIds
    val isModerator = isCurrentUserHost || isCoHost || isRoomMod
    val canSpeakOnStage = isSpeaker || isCurrentUserHost
    val backgroundTheme = remember(room.backgroundTheme) { RoomBackgroundTheme.fromId(room.backgroundTheme) }
    val seatSlots = remember(room.speakers, room.seatAssignments, room.maxAudioParticipants) {
        room.orderedSeatSlots()
    }

    val raisedHandUsers = remember(room.raisedHandIds, room.listeners, room.speakers) {
        val allRoomUsers = (room.speakers + room.listeners).associateBy { it.id }
        room.raisedHandIds.mapNotNull { uid -> allRoomUsers[uid] }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.onMicPermissionResult(granted)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshMicPermissionStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(room.id) {
        viewModel.refreshMicPermissionStatus()
    }

    var hasAutoRequestedMic by remember(room.id) { mutableStateOf(false) }
    LaunchedEffect(room.id, canSpeakOnStage, hasMicPermission) {
        if (canSpeakOnStage && !hasMicPermission && !hasAutoRequestedMic) {
            hasAutoRequestedMic = true
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
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

    val isMicActivelyCapturingForRtc = canSpeakOnStage && isMicCapturing && !isMicMuted && !isHostMuted
    DisposableEffect(showLiveCaptions, appLanguage, isMicActivelyCapturingForRtc) {
        if (!showLiveCaptions) {
            return@DisposableEffect onDispose {}
        }
        if (isMicActivelyCapturingForRtc) {
            showLiveCaptions = false
            viewModel.showInAppNotification(AppStrings.t("live_captions_mic_conflict", appLanguage))
            return@DisposableEffect onDispose {}
        }
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            showLiveCaptions = false
            viewModel.showInAppNotification(AppStrings.t("live_captions_unavailable", appLanguage))
            return@DisposableEffect onDispose {}
        }
        val mainHandler = Handler(Looper.getMainLooper())
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
        var consecutiveErrors = 0
        var restartRunnable: Runnable? = null

        fun scheduleRestart(delayMs: Long) {
            if (!active) return
            restartRunnable?.let { mainHandler.removeCallbacks(it) }
            val next = Runnable {
                if (active) {
                    try {
                        recognizer.startListening(intent)
                    } catch (_: Exception) {
                        active = false
                        showLiveCaptions = false
                    }
                }
            }
            restartRunnable = next
            mainHandler.postDelayed(next, delayMs)
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                consecutiveErrors = 0
            }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                if (!active) return
                val fatalError = error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ||
                    error == SpeechRecognizer.ERROR_AUDIO ||
                    error == SpeechRecognizer.ERROR_CLIENT ||
                    error == SpeechRecognizer.ERROR_NETWORK ||
                    error == SpeechRecognizer.ERROR_SERVER
                consecutiveErrors++
                if (fatalError || consecutiveErrors > 3) {
                    active = false
                    showLiveCaptions = false
                    viewModel.showInAppNotification(AppStrings.t("live_captions_unavailable", appLanguage))
                } else {
                    scheduleRestart(1200L * consecutiveErrors)
                }
            }
            override fun onResults(results: Bundle?) {
                consecutiveErrors = 0
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull().orEmpty()
                if (text.isNotBlank()) liveCaptionText = text
                scheduleRestart(250L)
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
            viewModel.showInAppNotification(AppStrings.t("live_captions_unavailable", appLanguage))
        }
        onDispose {
            active = false
            restartRunnable?.let { mainHandler.removeCallbacks(it) }
            try {
                recognizer.stopListening()
                recognizer.destroy()
            } catch (_: Exception) {
            }
        }
    }

    Dialog(
        onDismissRequest = onMinimize,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .testTag("active_voice_room_modal"),
            contentAlignment = Alignment.TopCenter
        ) {
            VoiceRoomBackgroundCanvas(
                theme = backgroundTheme,
                lowPowerMode = lowPowerVisualMode,
                audioEnergy = if (hasMicPermission && isMicCapturing && !isMicMuted && !isHostMuted) localAudioLevel else 0f
            )

            activeReactions.forEach { reaction ->
                ReactionFloatingBubble(
                    emoji = reaction.emoji,
                    offsetX = reaction.offsetX,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout)),
                contentAlignment = Alignment.TopCenter
            ) {
                val availableWidth = maxWidth
                val stageColumns = if (availableWidth < 370.dp) 2 else 3
                val horizontalPad = if (availableWidth < 370.dp) 12.dp else 16.dp
                val showTopReportBtn = !isCurrentUserHost && availableWidth >= 360.dp
                val showDockSpeakerBtn = availableWidth >= 370.dp
                val showDockGiftBtn = availableWidth >= 420.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 760.dp)
                        .padding(horizontal = horizontalPad, vertical = 8.dp)
                ) {
                    // Minimal Top Action & Audio Status Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = onMinimize,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A).copy(alpha = 0.72f))
                                .testTag("minimize_room_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowDown,
                                contentDescription = AppStrings.t("close", appLanguage),
                                tint = Color.White
                            )
                        }

                        val isAudioConnected = roomIceState == "connected" || connectedPeers > 0
                        val needsMicForStage = canSpeakOnStage && !hasMicPermission
                        val statusTint = when {
                            webrtcError != null || roomIceState == "failed" -> Color(0xFFEF4444)
                            isHostMuted && canSpeakOnStage -> Color(0xFFF59E0B)
                            needsMicForStage -> Color(0xFFF59E0B)
                            isAudioConnected -> Color(0xFF10B981)
                            else -> Color(backgroundTheme.accentHex)
                        }
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A).copy(alpha = 0.78f),
                            border = BorderStroke(1.dp, statusTint.copy(alpha = 0.45f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("voice_room_webrtc_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isAudioConnected || (canSpeakOnStage && hasMicPermission && isMicCapturing && !isMicMuted && !isHostMuted)) {
                                    SpeakingSoundWave(color = statusTint)
                                } else {
                                    Icon(
                                        imageVector = when {
                                            isHostMuted && canSpeakOnStage -> Icons.Filled.Lock
                                            needsMicForStage -> Icons.Filled.MicOff
                                            else -> Icons.Filled.Headphones
                                        },
                                        contentDescription = null,
                                        tint = statusTint,
                                        modifier = Modifier.size(15.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${room.listenersCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                val rtcLabel = when {
                                    webrtcError != null || roomIceState == "failed" ->
                                        "• ${AppStrings.t("vr_audio_failed", appLanguage)}"
                                    isHostMuted && canSpeakOnStage ->
                                        "• ${AppStrings.t("vr_host_muted_badge", appLanguage)}"
                                    needsMicForStage ->
                                        "• ${AppStrings.t("vr_mic_needed_badge", appLanguage)}"
                                    connectedPeers > 0 ->
                                        "• $connectedPeers/${room.maxAudioParticipants}"
                                    roomIceState == "connecting" ->
                                        "• ${AppStrings.t("webrtc_connecting_badge", appLanguage)}"
                                    canSpeakOnStage && hasMicPermission && isMicCapturing && !isMicMuted ->
                                        "• ${AppStrings.t("vr_mic_live", appLanguage)}"
                                    else ->
                                        "• ${AppStrings.t("vr_audio_ready", appLanguage)}"
                                }
                                Text(
                                    text = rtcLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = statusTint,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        IconButton(
                            onClick = { showSettingsModDialog = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F172A).copy(alpha = 0.72f))
                                .testTag("open_moderator_deck_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = AppStrings.t("vr_moderator_title", appLanguage),
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (isCurrentUserHost) {
                            Button(
                                onClick = { viewModel.endVoiceRoomNow() },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444),
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("end_room_button")
                            ) {
                                Text(
                                    text = AppStrings.t("end_stage", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        } else {
                            if (showTopReportBtn) {
                                IconButton(
                                    onClick = { showReportRoomDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("report_room_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Flag,
                                        contentDescription = AppStrings.t("report_room", appLanguage),
                                        tint = Color(0xFFCBD5E1),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = onLeave,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.7f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                                contentPadding = PaddingValues(
                                    horizontal = if (availableWidth < 370.dp) 8.dp else 10.dp,
                                    vertical = 6.dp
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("leave_room_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = AppStrings.t("leave_stage", appLanguage),
                                    modifier = Modifier.size(16.dp)
                                )
                                if (availableWidth >= 350.dp) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.t("leave_stage", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Unified Scrollable Stage & Audience Container
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(stageColumns),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .testTag("voice_room_stage_grid"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 10.dp)
                    ) {
                        // Minimal Room Info Header
                        item(span = { GridItemSpan(stageColumns) }, key = "room_header_card") {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.66f),
                                border = BorderStroke(1.dp, Color(backgroundTheme.accentHex).copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = room.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (room.isStageLocked) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF59E0B).copy(alpha = 0.22f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Lock,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFACC15),
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "LOCKED",
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = Color(0xFFFACC15)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    val primaryTag = room.tags.firstOrNull()?.takeIf { it.isNotBlank() }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(backgroundTheme.accentHex).copy(alpha = 0.18f)
                                        ) {
                                            Text(
                                                text = "${room.category.iconEmoji} ${room.category.displayName}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(backgroundTheme.accentHex),
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        if (primaryTag != null) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF1E293B)
                                            ) {
                                                Text(
                                                    text = primaryTag,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFCBD5E1),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }

                                    if (room.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(5.dp))
                                        Text(
                                            text = room.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFFCBD5E1),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (!room.pinnedNotice.isNullOrBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(backgroundTheme.accentHex).copy(alpha = 0.16f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("voice_room_pinned_notice")
                                        ) {
                                            Text(
                                                text = "📌 ${room.pinnedNotice}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Host-Muted Lock Notice Banner
                        if (isHostMuted && !isCurrentUserHost) {
                            item(span = { GridItemSpan(stageColumns) }, key = "host_muted_banner") {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF451A03).copy(alpha = 0.90f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.65f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("voice_room_host_muted_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFFBBF24),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = AppStrings.t("vr_host_muted_banner", appLanguage),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFFEF3C7)
                                        )
                                    }
                                }
                            }
                        }

                        // Microphone Permission Banner
                        if ((canSpeakOnStage && !hasMicPermission) || micPermissionDeniedInRoom) {
                            item(span = { GridItemSpan(stageColumns) }, key = "mic_permission_banner") {
                                MicPermissionBanner(
                                    appLanguage = appLanguage,
                                    onRequestPermission = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    },
                                    onOpenSettings = {
                                        try {
                                            val settingsIntent = Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", context.packageName, null)
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(settingsIntent)
                                        } catch (_: Exception) {
                                        }
                                    }
                                )
                            }
                        }

                        // WebRTC Connection / Capacity Warning Banner
                        if (webrtcError != null || isCapacityReached || roomIceState == "failed") {
                            item(span = { GridItemSpan(stageColumns) }, key = "webrtc_error_banner") {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF450A0A).copy(alpha = 0.88f),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("voice_room_webrtc_error_banner")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = webrtcError
                                                ?: if (isCapacityReached) AppStrings.t("vr_stage_capacity_reached", appLanguage)
                                                else AppStrings.t("vr_nat_warning", appLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color(0xFFFECACA),
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { viewModel.retryRoomAudioConnection() },
                                            modifier = Modifier.size(48.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Refresh,
                                                contentDescription = AppStrings.t("vr_retry_audio", appLanguage),
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Raised Hands Queue
                        if (isModerator && raisedHandUsers.isNotEmpty()) {
                            item(span = { GridItemSpan(stageColumns) }, key = "raised_hands_queue") {
                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.88f)),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("raised_hands_queue_card")
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Filled.FrontHand,
                                                contentDescription = null,
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "${AppStrings.t("raised_hands_queue", appLanguage)} (${raisedHandUsers.size})",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            items(raisedHandUsers, key = { it.id }) { requester ->
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFF0F172A),
                                                    border = BorderStroke(1.dp, Color(0xFF334155))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        UserAvatar(user = requester, size = 30.dp, showOnlineStatus = false)
                                                        Text(
                                                            text = "${requester.flagEmoji} ${requester.name.split(" ").first()}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color.White
                                                        )
                                                        Button(
                                                            onClick = { viewModel.promotePeerToSpeaker(requester.id) },
                                                            shape = RoundedCornerShape(10.dp),
                                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                            modifier = Modifier
                                                                .heightIn(min = 40.dp)
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
                                                            modifier = Modifier.heightIn(min = 40.dp)
                                                        ) {
                                                            Text(
                                                                text = AppStrings.t("dismiss_hand", appLanguage),
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = Color.White
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

                        // Stage Divider Header
                        item(span = { GridItemSpan(stageColumns) }, key = "stage_section_header") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${AppStrings.t("stage_speakers", appLanguage)} (${room.speakers.size}/${room.maxAudioParticipants})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(backgroundTheme.accentHex),
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(1.dp)
                                        .background(Color(backgroundTheme.accentHex).copy(alpha = 0.30f))
                                )
                            }
                        }

                        // 6-Seat Symmetrical Microphone Stage Grid (2x3 on narrow phones, 3x2 on regular/wide)
                        itemsIndexed(seatSlots, key = { idx, _ -> "stage_seat_$idx" }) { seatIdx, speaker ->
                            if (speaker == null) {
                                EmptyStageSeatTile(
                                    seatNumber = seatIdx + 1,
                                    seatIndex = seatIdx,
                                    isStageLocked = room.isStageLocked && !isModerator,
                                    accentColor = Color(backgroundTheme.accentHex),
                                    appLanguage = appLanguage,
                                    onClick = {
                                        if (room.isStageLocked && !isModerator) {
                                            viewModel.toggleRaiseHand()
                                        } else {
                                            viewModel.claimStageSeat(seatIdx)
                                        }
                                    }
                                )
                            } else {
                                val isSelf = speaker.id == currentUser.id
                                val isSpeakerHostMuted = (speaker.id in room.hostMutedPeerIds) || (isSelf && isHostMuted)
                                val isRemoteMuted = (speaker.id in room.mutedPeerIds) || isSpeakerHostMuted
                                val isRealAudioSpeaking = computeIsSpeakerActivelyTransmitting(
                                    isSelf = isSelf,
                                    hasMicPermission = hasMicPermission,
                                    isMicCapturing = isMicCapturing,
                                    isLocalMicMuted = isMicMuted || isHostMuted,
                                    localAudioLevel = localAudioLevel,
                                    isRemoteMuted = isRemoteMuted,
                                    isPeerInActiveStats = speaker.id in activeSpeakingPeerIds
                                ) && !isSpeakerHostMuted
                                val isUnmutedOnStage = if (isSelf) {
                                    hasMicPermission && isMicCapturing && !isMicMuted && !isHostMuted
                                } else {
                                    !isRemoteMuted
                                }
                                val isRtcConnected = speaker.id in connectedRoomPeerIds || isSelf
                                val speakerIsCoHost = speaker.id in room.coHostIds
                                val speakerIsMod = speaker.id in room.moderatorIds

                                var showHostMenu by remember(speaker.id) { mutableStateOf(false) }

                                Box {
                                    SpeakerStageTile(
                                        speaker = speaker,
                                        seatNumber = seatIdx + 1,
                                        isHost = speaker.id == room.host.id,
                                        hostLabel = AppStrings.t("host_label", appLanguage),
                                        isCoHost = speakerIsCoHost,
                                        coHostLabel = AppStrings.t("cohost_badge", appLanguage),
                                        isRoomModerator = speakerIsMod,
                                        modLabel = AppStrings.t("vr_mod_badge", appLanguage),
                                        isHostMuted = isSpeakerHostMuted,
                                        hostMutedLabel = AppStrings.t("vr_host_muted_badge", appLanguage),
                                        isSpeaking = isRealAudioSpeaking,
                                        isUnmutedIdle = isUnmutedOnStage,
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
                                                    if (isSpeakerHostMuted) AppStrings.t("vr_allow_mic", appLanguage)
                                                    else AppStrings.t("mute_speaker", appLanguage)
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = if (isSpeakerHostMuted) Icons.Filled.Mic else Icons.Filled.MicOff,
                                                    contentDescription = null
                                                )
                                            },
                                            onClick = {
                                                showHostMenu = false
                                                viewModel.mutePeerByHost(speaker.id, !isSpeakerHostMuted)
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
                                        DropdownMenuItem(
                                            text = { Text(AppStrings.t("vr_ban_15m", appLanguage), color = MaterialTheme.colorScheme.error) },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Filled.Close,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            },
                                            onClick = {
                                                showHostMenu = false
                                                viewModel.kickOrBanUser(speaker.id, banUser = true, tempDurationMs = 900_000L)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Listeners Section Below Stage
                        item(span = { GridItemSpan(stageColumns) }, key = "audience_section") {
                            AudienceSectionCard(
                                room = room,
                                currentUserId = currentUser.id,
                                isModerator = isModerator,
                                appLanguage = appLanguage,
                                onPromoteToSpeaker = viewModel::promotePeerToSpeaker,
                                onOpenProfile = viewModel::openUserProfile,
                                onKickUser = { uid -> viewModel.kickOrBanUser(uid, banUser = false) }
                            )
                        }
                    }

                    // Live Captions & AI Stage Assistant Banners
                    if (showLiveCaptions) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.88f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .testTag("live_captions_banner")
                        ) {
                            Text(
                                text = "💬 ${liveCaptionText.ifBlank { AppStrings.t("listening_count", appLanguage) }}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (isRoomAssistantLoading || !roomAssistantOutput.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF1E1B4B).copy(alpha = 0.90f),
                            border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.4f)),
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
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color(0xFF38BDF8))
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = roomAssistantOutput.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                                if (!isRoomAssistantLoading) {
                                    IconButton(
                                        onClick = viewModel::clearRoomAssistantOutput,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(Icons.Filled.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Responsive Unclipped Bottom Control Dock
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF0F172A).copy(alpha = 0.92f),
                        border = BorderStroke(1.dp, Color(backgroundTheme.accentHex).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Primary Mic / Stage Action Button
                            Button(
                                onClick = {
                                    when {
                                        !canSpeakOnStage -> {
                                            if (room.isStageLocked && !isModerator) {
                                                viewModel.toggleRaiseHand()
                                            } else {
                                                viewModel.claimStageSeat(-1)
                                            }
                                        }
                                        isHostMuted && !isCurrentUserHost -> {
                                            viewModel.showInAppNotification(AppStrings.t("vr_host_muted_banner", appLanguage))
                                        }
                                        !hasMicPermission -> {
                                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                        else -> viewModel.toggleMic()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = when {
                                        !canSpeakOnStage -> Color(0xFF06B6D4)
                                        isHostMuted && !isCurrentUserHost -> Color(0xFF78350F)
                                        !hasMicPermission -> Color(0xFFD97706)
                                        isMicMuted || !isMicCapturing -> Color(0xFF334155)
                                        else -> Color(0xFF06B6D4)
                                    },
                                    contentColor = when {
                                        !canSpeakOnStage -> Color(0xFF04101B)
                                        isHostMuted || isMicMuted || !isMicCapturing || !hasMicPermission -> Color.White
                                        else -> Color(0xFF04101B)
                                    }
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("voice_room_mic_toggle")
                            ) {
                                Icon(
                                    imageVector = when {
                                        !canSpeakOnStage && room.isStageLocked && !isModerator -> Icons.Filled.FrontHand
                                        !canSpeakOnStage -> Icons.Filled.Mic
                                        isHostMuted && !isCurrentUserHost -> Icons.Filled.Lock
                                        !hasMicPermission || isMicMuted || !isMicCapturing -> Icons.Filled.MicOff
                                        else -> Icons.Filled.Mic
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        !canSpeakOnStage && room.isStageLocked && !isModerator ->
                                            if (isHandRaised) AppStrings.t("hand_raised", appLanguage)
                                            else AppStrings.t("request_stage", appLanguage)
                                        !canSpeakOnStage -> AppStrings.t("vr_join_mic_seat", appLanguage)
                                        isHostMuted && !isCurrentUserHost -> AppStrings.t("vr_host_muted_badge", appLanguage)
                                        !hasMicPermission -> AppStrings.t("vr_grant_mic_btn", appLanguage)
                                        isMicMuted || !isMicCapturing -> AppStrings.t("mic_off", appLanguage)
                                        else -> AppStrings.t("vr_mic_live", appLanguage)
                                    },
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Step Down (non-host speaker) or Raise Hand (listener)
                            if (canSpeakOnStage && !isCurrentUserHost) {
                                IconButton(
                                    onClick = viewModel::stepDownFromStage,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .testTag("step_down_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Headphones,
                                        contentDescription = AppStrings.t("vr_step_down", appLanguage),
                                        tint = Color(0xFFCBD5E1)
                                    )
                                }
                            } else if (!canSpeakOnStage) {
                                IconButton(
                                    onClick = viewModel::toggleRaiseHand,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(if (isHandRaised) Color(0xFFA855F7) else Color(0xFF1E293B))
                                        .testTag("raise_hand_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.FrontHand,
                                        contentDescription = AppStrings.t("raise_hand", appLanguage),
                                        tint = Color.White
                                    )
                                }
                            }

                            // Room Chat & Polls
                            IconButton(
                                onClick = { showChatPollsDialog = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E293B))
                                    .testTag("open_room_chat_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ChatBubbleOutline,
                                    contentDescription = AppStrings.t("vr_room_chat_title", appLanguage),
                                    tint = Color(0xFF22D3EE)
                                )
                            }

                            // Audio Route (shown directly on >=370dp; always 1 tap away in Studio Tools on narrow screens)
                            if (showDockSpeakerBtn) {
                                IconButton(
                                    onClick = viewModel::toggleRoomSpeakerphone,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E293B))
                                        .testTag("voice_room_speaker_toggle")
                                ) {
                                    Icon(
                                        imageVector = when (audioRoute) {
                                            AudioRouteMode.SPEAKER -> Icons.Filled.VolumeUp
                                            AudioRouteMode.EARPIECE -> Icons.Filled.PhoneInTalk
                                            AudioRouteMode.BLUETOOTH -> Icons.Filled.Headphones
                                        },
                                        contentDescription = AppStrings.t("speakerphone", appLanguage),
                                        tint = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            // Gifts Quick Button (shown directly on >=420dp; always 1 tap away in Studio Tools)
                            if (showDockGiftBtn) {
                                IconButton(
                                    onClick = { showGiftCenterDialog = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEC4899).copy(alpha = 0.22f))
                                        .testTag("dock_gift_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Star,
                                        contentDescription = AppStrings.t("vr_gifts_title", appLanguage),
                                        tint = Color(0xFFF472B6)
                                    )
                                }
                            }

                            // Studio Tools Sheet Trigger
                            IconButton(
                                onClick = { showStudioToolsSheet = true },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFA855F7).copy(alpha = 0.25f))
                                    .testTag("open_studio_tools_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = AppStrings.t("vr_studio_tools", appLanguage),
                                    tint = Color(0xFFC084FC)
                                )
                            }
                        }
                    }
                }
            }

            EntranceAnimationOverlay(
                entrance = currentEntranceVisual,
                appLanguage = appLanguage,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 64.dp)
            )

            GiftAnimationOverlay(
                visual = currentGiftVisual,
                appLanguage = appLanguage
            )
        }
    }

    if (showStudioToolsSheet) {
        StudioToolsDialog(
            appLanguage = appLanguage,
            audioRoute = audioRoute,
            showLiveCaptions = showLiveCaptions,
            isRoomAssistantLoading = isRoomAssistantLoading,
            isHost = isCurrentUserHost,
            onToggleAudioRoute = viewModel::toggleRoomSpeakerphone,
            onSendReaction = { emoji ->
                viewModel.sendReaction(emoji)
                showStudioToolsSheet = false
            },
            onOpenGifts = {
                showStudioToolsSheet = false
                showGiftCenterDialog = true
            },
            onOpenSoundboard = {
                showStudioToolsSheet = false
                showSoundboardDialog = true
            },
            onPartyGame = {
                showStudioToolsSheet = false
                if (!isRoomAssistantLoading) viewModel.generateRoomAssistant("game")
            },
            onToggleCaptions = {
                showStudioToolsSheet = false
                if (showLiveCaptions) {
                    showLiveCaptions = false
                } else {
                    val hasMic = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                    if (hasMic) showLiveCaptions = true
                    else captionPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onAiSummary = {
                showStudioToolsSheet = false
                if (!isRoomAssistantLoading) showSummaryConsentDialog = true
            },
            onOpenSettings = {
                showStudioToolsSheet = false
                showSettingsModDialog = true
            },
            onReportRoom = {
                showStudioToolsSheet = false
                showReportRoomDialog = true
            },
            onDismiss = { showStudioToolsSheet = false }
        )
    }

    if (showGiftCenterDialog) {
        VoiceRoomGiftCenterDialog(
            room = room,
            currentUser = currentUser,
            supporters = supporterLeaderboard,
            appLanguage = appLanguage,
            onSendGift = { gift, recipient -> viewModel.sendVoiceGift(gift, recipient) },
            onDismiss = { showGiftCenterDialog = false }
        )
    }

    if (showSoundboardDialog) {
        VoiceRoomSoundboardDialog(
            playingSoundId = playingSoundId,
            favoriteSoundIds = favoriteSoundIds,
            volume = soundEffectsVolume,
            appLanguage = appLanguage,
            onVolumeChange = viewModel::setSoundEffectsVolume,
            onPreviewSound = viewModel::previewSoundEffect,
            onTriggerSound = viewModel::triggerSoundEffect,
            onToggleFavorite = viewModel::toggleFavoriteSound,
            onStopAll = viewModel::stopSoundEffects,
            onDismiss = { showSoundboardDialog = false }
        )
    }

    if (showChatPollsDialog) {
        VoiceRoomChatAndPollsDialog(
            room = room,
            events = roomEvents,
            polls = roomPolls,
            currentUid = currentUser.id,
            isModerator = isModerator,
            appLanguage = appLanguage,
            onSendChat = viewModel::sendRoomChatMessage,
            onCreatePoll = viewModel::createRoomPoll,
            onVotePoll = viewModel::voteRoomPoll,
            onClosePoll = viewModel::closeRoomPoll,
            onUpdatePinnedNotice = viewModel::updatePinnedNotice,
            onDismiss = { showChatPollsDialog = false }
        )
    }

    if (showSettingsModDialog) {
        VoiceRoomModeratorAndSettingsDialog(
            room = room,
            currentUid = currentUser.id,
            audioRoute = audioRoute,
            isBluetoothAvailable = isBluetoothAvailable,
            webrtcError = webrtcError,
            defaultEntranceStyle = defaultEntranceStyle,
            enableGiftEffects = enableGiftEffects,
            enableEntranceAnimations = enableEntranceAnimations,
            lowPowerVisualMode = lowPowerVisualMode,
            appLanguage = appLanguage,
            onSelectAudioRoute = viewModel::setAudioRoute,
            onRetryAudio = viewModel::retryRoomAudioConnection,
            onSelectBackgroundTheme = viewModel::setRoomBackgroundTheme,
            onSelectEntranceStyle = viewModel::setDefaultEntranceStyle,
            onToggleGiftEffects = viewModel::setEnableGiftEffects,
            onToggleEntranceAnimations = viewModel::setEnableEntranceAnimations,
            onToggleLowPower = viewModel::setLowPowerVisualMode,
            onToggleStageLock = viewModel::toggleStageLock,
            onMuteAllSpeakers = viewModel::muteAllSpeakers,
            onSetSuccessorHost = viewModel::setSuccessorHost,
            onToggleCoHost = viewModel::toggleCoHost,
            onToggleModerator = viewModel::toggleRoomModerator,
            onKickOrBan = { targetUid, ban -> viewModel.kickOrBanUser(targetUid, ban) },
            onMutePeerByHost = viewModel::mutePeerByHost,
            onTimedBan = { targetUid, durationMs ->
                viewModel.kickOrBanUser(targetUid, banUser = true, tempDurationMs = durationMs)
            },
            onDismiss = { showSettingsModDialog = false }
        )
    }

    if (showReportRoomDialog) {
        ReportRoomDialog(
            appLanguage = appLanguage,
            onDismiss = { showReportRoomDialog = false },
            onSubmitReport = { selectedReason, details ->
                viewModel.reportVoiceRoom(room.id, selectedReason, details)
                showReportRoomDialog = false
            }
        )
    }

    if (showSummaryConsentDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryConsentDialog = false },
            title = { Text(text = AppStrings.t("ai_summary_consent_title", appLanguage), fontWeight = FontWeight.Bold) },
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
