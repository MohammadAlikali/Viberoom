package com.example.ui

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.WebRtcVoiceManager
import com.example.model.AudioRouteMode
import com.example.model.EntranceStyle
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.voiceroom.EmptyStageSeatTile
import com.example.ui.screens.voiceroom.SpeakerStageTile
import com.example.ui.screens.voiceroom.VoiceRoomModeratorAndSettingsDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class VoiceRoomIntegrityAndUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun user(id: String, name: String) = UserProfile(
        id = id,
        name = name,
        username = id,
        bio = "",
        countryName = "Afghanistan",
        countryCode = "AF",
        flagEmoji = "🇦🇫",
        vibeXp = 100,
        vibeLevel = 2
    )

    @Test
    fun voiceRoom_defaultsHaveZeroFakeListenersAndAccurateSeatOrdering() {
        val host = user("u_host", "Amina Host")
        val guest2 = user("u_g2", "Tariq")
        val guest3 = user("u_g3", "Zara")

        val freshRoom = VoiceRoom(
            id = "r_real",
            title = "Real Room",
            description = "No fake count",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            speakers = listOf(host, guest2, guest3),
            seatAssignments = mapOf(
                0 to "u_host",
                4 to "u_g2",
                1 to "u_g3"
            ),
            maxAudioParticipants = 6
        )

        assertEquals(0, freshRoom.listenersCount)
        val slots = freshRoom.orderedSeatSlots()
        assertEquals(6, slots.size)
        assertEquals("u_host", slots[0]?.id)
        assertEquals("u_g3", slots[1]?.id)
        assertNull(slots[2])
        assertNull(slots[3])
        assertEquals("u_g2", slots[4]?.id)
        assertNull(slots[5])
    }

    @Test
    fun voiceRoom_isUserBannedChecksPermanentAndTemporaryBans() {
        val host = user("u_host", "Host")
        val now = 1_700_000_000_000L
        val room = VoiceRoom(
            id = "r_ban",
            title = "Mod Room",
            description = "",
            category = RoomCategory.CHILL_VIBES,
            host = host,
            bannedIds = setOf("perm_user"),
            temporaryBanExpiries = mapOf(
                "temp_active" to now + 600_000L,
                "temp_expired" to now - 5_000L
            )
        )

        assertTrue(room.isUserBanned("perm_user", now))
        assertTrue(room.isUserBanned("temp_active", now))
        assertFalse(room.isUserBanned("temp_expired", now))
        assertFalse(room.isUserBanned("clean_user", now))
    }

    @Test
    fun webRtcVoiceManager_hostMuteLockBlocksSelfUnmuteAndReleaseNeverForceUnmutes() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = WebRtcVoiceManager(app)

        manager.setHostMuted(true)
        assertTrue(manager.isHostMutedLocked())
        manager.setMicEnabled(true)
        assertFalse(manager.isMicCapturing.value)
        assertEquals(0f, manager.localAudioLevel.value)

        // Allowing mic (releasing host lock) must NOT automatically turn mic on
        manager.setHostMuted(false)
        assertFalse(manager.isHostMutedLocked())
        assertFalse(manager.isMicCapturing.value)

        manager.dispose()
    }

    @Test
    fun stageTiles_renderOccupiedHostMutedLockAndClaimableEmptySeat() {
        val speaker = user("u_sp", "Farhad")
        var emptyClicked = false

        composeTestRule.setContent {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    SpeakerStageTile(
                        speaker = speaker,
                        seatNumber = 2,
                        isHost = false,
                        isHostMuted = true,
                        hostMutedLabel = "Muted by Host",
                        isSpeaking = false,
                        isUnmutedIdle = false,
                        isRtcConnected = true,
                        onClick = {}
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    EmptyStageSeatTile(
                        seatNumber = 3,
                        seatIndex = 2,
                        isStageLocked = false,
                        accentColor = Color(0xFF06B6D4),
                        appLanguage = AppLanguage.ENGLISH,
                        onClick = { emptyClicked = true }
                    )
                }
            }
        }

        composeTestRule.onNodeWithTag("speaker_tile_u_sp").assertIsDisplayed()
        composeTestRule.onNodeWithText("Muted by Host").assertIsDisplayed()
        composeTestRule.onNodeWithTag("stage_seat_empty_2").assertIsDisplayed().performClick()
        assertTrue(emptyClicked)
    }

    @Test
    fun moderatorDialog_showsAllowMicInsteadOfForceUnmuteAndTimedBanButtons() {
        val host = user("u_host", "Host")
        val mutedPeer = user("u_peer", "Laila")
        val room = VoiceRoom(
            id = "r_mod_ui",
            title = "Mod UI",
            description = "",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            speakers = listOf(host, mutedPeer),
            hostMutedPeerIds = setOf("u_peer")
        )

        var muteCallArg: Pair<String, Boolean>? = null
        var timedBanArg: Pair<String, Long>? = null

        composeTestRule.setContent {
            VoiceRoomModeratorAndSettingsDialog(
                room = room,
                currentUid = host.id,
                audioRoute = AudioRouteMode.SPEAKER,
                isBluetoothAvailable = false,
                webrtcError = null,
                defaultEntranceStyle = EntranceStyle.STANDARD,
                enableGiftEffects = true,
                enableEntranceAnimations = true,
                lowPowerVisualMode = false,
                appLanguage = AppLanguage.ENGLISH,
                onSelectAudioRoute = {},
                onRetryAudio = {},
                onSelectBackgroundTheme = {},
                onSelectEntranceStyle = {},
                onToggleGiftEffects = {},
                onToggleEntranceAnimations = {},
                onToggleLowPower = {},
                onToggleStageLock = {},
                onMuteAllSpeakers = {},
                onSetSuccessorHost = {},
                onToggleCoHost = {},
                onToggleModerator = {},
                onKickOrBan = { _, _ -> },
                onMutePeerByHost = { uid, mute -> muteCallArg = uid to mute },
                onTimedBan = { uid, duration -> timedBanArg = uid to duration },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithTag("mod_mute_toggle_u_peer").performScrollTo().performClick()
        assertEquals("u_peer" to false, muteCallArg)

        composeTestRule.onNodeWithTag("mod_ban_15m_u_peer").performScrollTo().performClick()
        assertEquals("u_peer" to 900_000L, timedBanArg)
    }

    @Test
    fun syncActiveRoomPeers_capsSpeakersAtSixAndPrioritizesHostSeatZero() {
        val repo = com.example.data.VibeRoomRepository()
        val host = user("u_host", "Host")
        val room = VoiceRoom(
            id = "r_cap",
            title = "Cap Room",
            description = "",
            category = RoomCategory.CHILL_VIBES,
            host = host,
            maxAudioParticipants = 6,
            isLive = true
        )
        repo.joinVoiceRoom(room)

        val now = System.currentTimeMillis()
        val peers = (1..7).map { idx ->
            com.example.data.FirestoreRoomPeer(
                id = "u_s$idx",
                userId = "u_s$idx",
                displayName = "Speaker $idx",
                isSpeaker = true,
                isMuted = false,
                seatIndex = idx % 6,
                lastHeartbeatMs = now
            )
        }

        repo.syncActiveRoomPeers(peers, myUid = "u_s1", connectedWebRtcPeerIds = emptySet())
        val updated = repo.activeRoom.value!!
        assertEquals(6, updated.speakers.size)
        assertTrue(updated.speakers.any { it.id == "u_host" })
        assertTrue(updated.listeners.any { it.id.startsWith("u_s") })
        assertEquals(6, updated.seatAssignments.size)
        assertTrue(updated.seatAssignments.values.all { id -> updated.speakers.any { it.id == id } })
    }
}
