package com.example.ui.screens.voiceroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.AudioRouteMode
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

fun computeIsSpeakerActivelyTransmitting(
    isSelf: Boolean,
    hasMicPermission: Boolean,
    isMicCapturing: Boolean,
    isLocalMicMuted: Boolean,
    localAudioLevel: Float,
    isRemoteMuted: Boolean,
    isPeerInActiveStats: Boolean
): Boolean {
    return if (isSelf) {
        hasMicPermission && isMicCapturing && !isLocalMicMuted && localAudioLevel > 0.02f
    } else {
        !isRemoteMuted && isPeerInActiveStats
    }
}

@Composable
fun EmptyStageSeatTile(
    seatNumber: Int,
    seatIndex: Int,
    isStageLocked: Boolean,
    accentColor: Color,
    appLanguage: AppLanguage,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.44f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isStageLocked) Color(0xFF334155) else accentColor.copy(alpha = 0.34f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 122.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("stage_seat_empty_$seatIndex")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 122.dp)
                .padding(horizontal = 8.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(
                        if (isStageLocked) Color(0xFF1E293B)
                        else accentColor.copy(alpha = 0.16f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStageLocked) Icons.Filled.Lock else Icons.Filled.Add,
                    contentDescription = null,
                    tint = if (isStageLocked) Color(0xFF64748B) else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (isStageLocked) {
                    "${AppStrings.t("vr_seat_label", appLanguage)} $seatNumber"
                } else {
                    AppStrings.t("vr_join_mic_seat", appLanguage)
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = if (isStageLocked) Color(0xFF64748B) else Color(0xFFE2E8F0),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "#$seatNumber",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun SpeakerStageTile(
    speaker: UserProfile,
    seatNumber: Int = 1,
    isHost: Boolean,
    hostLabel: String = "HOST",
    isCoHost: Boolean = false,
    coHostLabel: String = "CO-HOST",
    isRoomModerator: Boolean = false,
    modLabel: String = "MOD",
    isHostMuted: Boolean = false,
    hostMutedLabel: String = "Muted by Host",
    isSpeaking: Boolean,
    isUnmutedIdle: Boolean = false,
    isRtcConnected: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF0F172A).copy(alpha = if (isSpeaking) 0.78f else 0.62f),
        border = BorderStroke(
            width = if (isSpeaking) 1.5.dp else 1.dp,
            color = when {
                isSpeaking -> Color(0xFF06B6D4).copy(alpha = 0.85f)
                isHostMuted -> Color(0xFFF59E0B).copy(alpha = 0.55f)
                isHost -> Color(0xFF38BDF8).copy(alpha = 0.45f)
                else -> Color(0xFF334155).copy(alpha = 0.6f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 122.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .testTag("speaker_tile_${speaker.id}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 122.dp)
        ) {
            Text(
                text = "#$seatNumber",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF94A3B8),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 8.dp, top = 6.dp)
            )

            if (isRtcConnected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 8.dp, top = 8.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    UserAvatar(
                        user = speaker,
                        size = 54.dp,
                        isSpeaking = isSpeaking,
                        showOnlineStatus = false
                    )

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isHostMuted -> Color(0xFF92400E)
                                    isSpeaking -> Color(0xFF06B6D4)
                                    isUnmutedIdle -> Color(0xFF0F766E)
                                    else -> Color(0xFF334155)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isHostMuted -> Icons.Filled.Lock
                                isSpeaking -> Icons.Filled.VolumeUp
                                isUnmutedIdle -> Icons.Filled.Mic
                                else -> Icons.Filled.MicOff
                            },
                            contentDescription = null,
                            tint = when {
                                isHostMuted -> Color(0xFFFDE68A)
                                isSpeaking -> Color(0xFF04101B)
                                else -> Color(0xFFE2E8F0)
                            },
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = speaker.name.split(" ").first(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (speaker.flagEmoji.isNotBlank()) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = speaker.flagEmoji, fontSize = 11.sp)
                    }
                }

                when {
                    isHost -> RolePill(hostLabel, Color(0xFF06B6D4), Color(0xFF04101B))
                    isHostMuted -> RolePill(hostMutedLabel, Color(0xFFF59E0B), Color(0xFF1E1B4B))
                    isCoHost -> RolePill(coHostLabel, Color(0xFFA855F7), Color.White)
                    isRoomModerator -> RolePill(modLabel, Color(0xFFFACC15), Color(0xFF04101B))
                }
            }
        }
    }
}

@Composable
private fun RolePill(label: String, bg: Color, fg: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = Modifier.padding(top = 4.dp)
    ) {
        Text(
            text = label,
            color = fg,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

@Composable
internal fun AudienceSectionCard(
    room: VoiceRoom,
    currentUserId: String,
    isModerator: Boolean,
    appLanguage: AppLanguage,
    onPromoteToSpeaker: (String) -> Unit,
    onOpenProfile: (UserProfile) -> Unit,
    onKickUser: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A).copy(alpha = 0.68f),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
            .testTag("voice_room_audience_section")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Headphones,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "${AppStrings.t("vr_listeners_label", appLanguage)} (${room.listeners.size})",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (room.listeners.isEmpty()) {
                Text(
                    text = AppStrings.t("vr_audience_empty", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(room.listeners, key = { it.id }) { listener ->
                        var listenerMenuOpen by remember(listener.id) { mutableStateOf(false) }
                        val listenerRaisedHand = listener.id in room.raisedHandIds

                        Box {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .widthIn(min = 48.dp, max = 64.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        if (isModerator && listener.id != currentUserId) listenerMenuOpen = true
                                        else onOpenProfile(listener)
                                    }
                                    .padding(4.dp)
                                    .testTag("listener_avatar_${listener.id}")
                            ) {
                                Box {
                                    UserAvatar(user = listener, size = 38.dp, showOnlineStatus = false)
                                    if (listenerRaisedHand) {
                                        Text(
                                            text = "✋",
                                            fontSize = 11.sp,
                                            modifier = Modifier.align(Alignment.BottomEnd)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = listener.name.split(" ").first(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
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
                                        onPromoteToSpeaker(listener.id)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(AppStrings.t("nav_profile", appLanguage)) },
                                    leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                                    onClick = {
                                        listenerMenuOpen = false
                                        onOpenProfile(listener)
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(AppStrings.t("vr_kick", appLanguage), color = MaterialTheme.colorScheme.error) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    },
                                    onClick = {
                                        listenerMenuOpen = false
                                        onKickUser(listener.id)
                                    }
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
fun MicPermissionBanner(
    appLanguage: AppLanguage,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF451A03).copy(alpha = 0.92f),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.7f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voice_room_mic_permission_banner")
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = AppStrings.t("vr_mic_permission_banner_title", appLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFEF3C7)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = AppStrings.t("vr_mic_permission_banner_desc", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFDE68A)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF1E1B4B)
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("grant_mic_permission_btn")
                ) {
                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("vr_grant_mic_btn", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, Color(0xFFFCD34D).copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFEF3C7)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("open_mic_settings_btn")
                ) {
                    Text(
                        text = AppStrings.t("vr_open_settings_btn", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun StudioToolsDialog(
    appLanguage: AppLanguage,
    audioRoute: AudioRouteMode,
    showLiveCaptions: Boolean,
    isRoomAssistantLoading: Boolean,
    isHost: Boolean,
    onToggleAudioRoute: () -> Unit,
    onSendReaction: (String) -> Unit,
    onOpenGifts: () -> Unit,
    onOpenSoundboard: () -> Unit,
    onPartyGame: () -> Unit,
    onToggleCaptions: () -> Unit,
    onAiSummary: () -> Unit,
    onOpenSettings: () -> Unit,
    onReportRoom: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("studio_tools_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "✨ ${AppStrings.t("vr_studio_tools", appLanguage)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf("🔥", "❤️", "👏", "🎵", "🌍", "💡").forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { onSendReaction(emoji) }
                                .testTag("reaction_button_$emoji")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StudioActionPill(
                        text = "🔊 ${if (appLanguage == AppLanguage.DARI_PERSIAN) audioRoute.labelFa else audioRoute.labelEn}",
                        containerColor = Color(0xFF1E293B),
                        borderColor = Color(0xFF38BDF8),
                        onClick = onToggleAudioRoute,
                        tag = "tools_audio_route_btn"
                    )
                    StudioActionPill(
                        text = "🎁 ${AppStrings.t("vr_gifts_title", appLanguage)}",
                        containerColor = Color(0xFFEC4899).copy(alpha = 0.24f),
                        borderColor = Color(0xFFEC4899),
                        onClick = onOpenGifts,
                        tag = "open_gift_studio_btn"
                    )
                    StudioActionPill(
                        text = "🎛️ ${AppStrings.t("vr_soundboard_title", appLanguage)}",
                        containerColor = Color(0xFFA855F7).copy(alpha = 0.24f),
                        borderColor = Color(0xFFA855F7),
                        onClick = onOpenSoundboard,
                        tag = "open_soundboard_btn"
                    )
                    StudioActionPill(
                        text = "🎮 ${AppStrings.t("party_game_cta", appLanguage)}",
                        containerColor = Color(0xFF1E293B),
                        borderColor = Color(0xFF475569),
                        onClick = { if (!isRoomAssistantLoading) onPartyGame() },
                        tag = "room_party_game_btn"
                    )
                    StudioActionPill(
                        text = "🗣️ ${AppStrings.t("live_captions_cta", appLanguage)}",
                        containerColor = if (showLiveCaptions) Color(0xFF10B981) else Color(0xFF1E293B),
                        borderColor = if (showLiveCaptions) Color(0xFF10B981) else Color(0xFF475569),
                        onClick = onToggleCaptions,
                        tag = "room_live_captions_btn"
                    )
                    StudioActionPill(
                        text = "✨ ${AppStrings.t("ai_summary_cta", appLanguage)}",
                        containerColor = Color(0xFF1E293B),
                        borderColor = Color(0xFF475569),
                        onClick = { if (!isRoomAssistantLoading) onAiSummary() },
                        tag = "room_ai_summary_btn"
                    )
                    StudioActionPill(
                        text = "🛡️ ${AppStrings.t("vr_moderator_title", appLanguage)}",
                        containerColor = Color(0xFF1E293B),
                        borderColor = Color(0xFF818CF8),
                        onClick = onOpenSettings,
                        tag = "studio_tools_settings_btn"
                    )
                    if (!isHost) {
                        StudioActionPill(
                            text = "🚩 ${AppStrings.t("report_room", appLanguage)}",
                            containerColor = Color(0xFF1E293B),
                            borderColor = Color(0xFFEF4444),
                            onClick = onReportRoom,
                            tag = "studio_tools_report_btn"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StudioActionPill(
    text: String,
    containerColor: Color,
    borderColor: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor.copy(alpha = 0.65f)),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
internal fun ReportRoomDialog(
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitReport: (reason: String, details: String) -> Unit
) {
    val reasons = listOf("Spam / Promotion", "Harassment", "Inappropriate Content", "Misleading Topic")
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var details by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Flag,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            AppStrings.t("report_room", appLanguage),
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
                                .heightIn(min = 44.dp)
                                .clickable { selectedReason = reason }
                                .padding(vertical = 4.dp)
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
                }
                Spacer(modifier = Modifier.height(14.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(AppStrings.t("cancel", appLanguage))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = { onSubmitReport(selectedReason, details) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(AppStrings.t("send", appLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
