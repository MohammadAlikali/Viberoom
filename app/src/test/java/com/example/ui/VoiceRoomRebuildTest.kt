package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.VoiceSoundboardEngine
import com.example.model.AudioRouteMode
import com.example.model.EntranceStyle
import com.example.model.GiftCategory
import com.example.model.RoomBackgroundTheme
import com.example.model.RoomCategory
import com.example.model.RoomLifecycleState
import com.example.model.RoomRole
import com.example.model.SoundboardCatalog
import com.example.model.SupporterRankEntry
import com.example.model.UserProfile
import com.example.model.VoiceGiftCatalog
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.voiceroom.CreateRoomDialogContent
import com.example.ui.screens.voiceroom.VoiceRoomBackgroundCanvas
import com.example.ui.screens.voiceroom.VoiceRoomGiftCenterDialog
import com.example.ui.screens.voiceroom.VoiceRoomModeratorAndSettingsDialog
import com.example.ui.screens.voiceroom.VoiceRoomSoundboardDialog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class VoiceRoomRebuildTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val sampleHost = UserProfile(
        id = "host_1",
        name = "Amina",
        username = "amina_vibe",
        bio = "Music & Languages",
        countryName = "Afghanistan",
        countryCode = "AF",
        flagEmoji = "🇦🇫",
        vibeXp = 320,
        vibeLevel = 4
    )

    private val sampleRoom = VoiceRoom(
        id = "room_rebuild_1",
        title = "Midnight Acoustic & Poetry",
        description = "Live stage with real WebRTC audio",
        category = RoomCategory.MUSIC_JAM,
        host = sampleHost,
        speakers = listOf(sampleHost),
        coHostIds = setOf("cohost_2"),
        moderatorIds = setOf("mod_3"),
        listenersCount = 14,
        isLive = true,
        backgroundTheme = RoomBackgroundTheme.GALAXY.id,
        isStageLocked = false
    )

    @Test
    fun catalogs_haveExpectedVarietyAndFreeZeroCostGifts() {
        assertTrue(VoiceGiftCatalog.gifts.size >= 15)
        assertTrue(VoiceGiftCatalog.gifts.any { it.xpCost == 0 && it.category == GiftCategory.BASIC })
        assertTrue(VoiceGiftCatalog.gifts.any { it.category == GiftCategory.VEHICLE && it.isFullScreen })
        assertTrue(VoiceGiftCatalog.gifts.any { it.category == GiftCategory.FANTASY && it.id == "dragon" })
        assertTrue(VoiceGiftCatalog.gifts.any { it.category == GiftCategory.SPECIAL })
        assertNotNull(VoiceGiftCatalog.findById("luxury_car"))
        assertTrue(EntranceStyle.entries.size >= 12)
        assertTrue(RoomBackgroundTheme.entries.size == 8)
        assertTrue(SoundboardCatalog.effects.size >= 12)
    }

    @Test
    fun voiceRoom_lifecycleAndRoleResolutionAreAccurate() {
        assertTrue(RoomLifecycleState.CREATED.isDiscoverable())
        assertTrue(RoomLifecycleState.WAITING.isDiscoverable())
        assertTrue(RoomLifecycleState.LIVE.isDiscoverable())
        assertFalse(RoomLifecycleState.ENDING.isDiscoverable())
        assertFalse(RoomLifecycleState.ENDED.isDiscoverable())
        assertFalse(RoomLifecycleState.EXPIRED.isDiscoverable())

        assertEquals(RoomRole.HOST, sampleRoom.roleOf("host_1"))
        assertEquals(RoomRole.CO_HOST, sampleRoom.roleOf("cohost_2"))
        assertEquals(RoomRole.MODERATOR, sampleRoom.roleOf("mod_3"))
        assertEquals(RoomRole.LISTENER, sampleRoom.roleOf("guest_99"))
        assertTrue(sampleRoom.isModeratorOrHigher("host_1"))
        assertTrue(sampleRoom.isModeratorOrHigher("cohost_2"))
        assertTrue(sampleRoom.isModeratorOrHigher("mod_3"))
        assertFalse(sampleRoom.isModeratorOrHigher("guest_99"))
    }

    @Test
    fun soundboardEngine_cooldownAndStopDoNotCrashUnderJvm() {
        val engine = VoiceSoundboardEngine()
        assertTrue(engine.canTriggerNow())
        engine.playEffect(soundId = "applause", volume = 0.8f, bypassCooldown = false)
        assertFalse(engine.canTriggerNow(cooldownMs = 5000L))
        engine.stopAll()
        engine.release()
    }

    @Test
    fun createRoomDialogContent_configuresThemeStageLockAndPrivacy() {
        var createdTitle = ""
        var createdTheme = ""
        var createdStageLocked = false
        var createdPrivate = false

        composeTestRule.setContent {
            CreateRoomDialogContent(
                appLanguage = AppLanguage.ENGLISH,
                onDismiss = {},
                onCreate = { _, _, _, _ -> },
                onCreateConfigured = { title, _, _, _, isPrivate, bgTheme, isLocked ->
                    createdTitle = title
                    createdPrivate = isPrivate
                    createdTheme = bgTheme
                    createdStageLocked = isLocked
                }
            )
        }

        composeTestRule.onNodeWithTag("create_room_title_input")
            .performScrollTo()
            .performTextInput("Global Guitar Lounge")
        composeTestRule.onNodeWithTag("room_theme_galaxy")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("create_room_private_switch")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("create_room_stage_lock_switch")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("submit_create_room")
            .assertIsDisplayed()
            .performClick()

        assertEquals("Global Guitar Lounge", createdTitle)
        assertEquals(RoomBackgroundTheme.GALAXY.id, createdTheme)
        assertTrue(createdPrivate)
        assertTrue(createdStageLocked)
    }

    @Test
    fun voiceRoomBackgroundCanvas_rendersInBothNormalAndLowPowerModes() {
        composeTestRule.setContent {
            VoiceRoomBackgroundCanvas(
                theme = RoomBackgroundTheme.NEON_CITY,
                audioEnergy = 0.65f,
                lowPowerMode = true
            )
        }
        composeTestRule.waitForIdle()
    }

    @Test
    fun giftCenterDialog_filtersCategoriesAndTriggersGiftSend() {
        var sentGiftId = ""
        val supporters = listOf(
            SupporterRankEntry("u_2", "Tariq", "🇦🇫", 150, 3)
        )

        composeTestRule.setContent {
            VoiceRoomGiftCenterDialog(
                room = sampleRoom,
                currentUser = sampleHost,
                supporters = supporters,
                appLanguage = AppLanguage.ENGLISH,
                onSendGift = { gift, _ -> sentGiftId = gift.id },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithTag("gift_studio_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("send_gift_rose").performScrollTo().performClick()
        assertEquals("rose", sentGiftId)

        composeTestRule.onNodeWithTag("gift_cat_VEHICLE").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("send_gift_luxury_car").performScrollTo().assertIsDisplayed()

        composeTestRule.onNodeWithText("Supporters (1)").performClick()
        composeTestRule.onNodeWithText("Tariq", substring = true).assertIsDisplayed()
    }

    @Test
    fun soundboardDialog_displaysEffectsAndTriggersBroadcast() {
        var triggeredId = ""

        composeTestRule.setContent {
            VoiceRoomSoundboardDialog(
                playingSoundId = null,
                favoriteSoundIds = setOf("applause"),
                volume = 0.85f,
                appLanguage = AppLanguage.ENGLISH,
                onVolumeChange = {},
                onPreviewSound = {},
                onTriggerSound = { triggeredId = it },
                onToggleFavorite = {},
                onStopAll = {},
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithTag("soundboard_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("soundboard_play_applause").performClick()
        assertEquals("applause", triggeredId)
    }

    @Test
    fun moderatorAndSettingsDialog_changesThemeAndMutesAllSpeakers() {
        var selectedThemeId = ""
        var muteAllCalled = false

        composeTestRule.setContent {
            VoiceRoomModeratorAndSettingsDialog(
                room = sampleRoom,
                currentUid = sampleHost.id,
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
                onSelectBackgroundTheme = { selectedThemeId = it.id },
                onSelectEntranceStyle = {},
                onToggleGiftEffects = {},
                onToggleEntranceAnimations = {},
                onToggleLowPower = {},
                onToggleStageLock = {},
                onMuteAllSpeakers = { muteAllCalled = true },
                onSetSuccessorHost = {},
                onToggleCoHost = {},
                onToggleModerator = {},
                onKickOrBan = { _, _ -> },
                onDismiss = {}
            )
        }

        composeTestRule.onNodeWithTag("room_settings_mod_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("theme_chip_cyberpunk").performScrollTo().performClick()
        assertEquals("cyberpunk", selectedThemeId)

        composeTestRule.onNodeWithTag("mute_all_speakers_btn").performScrollTo().performClick()
        assertTrue(muteAllCalled)
    }
}
