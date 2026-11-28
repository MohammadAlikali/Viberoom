package com.example.ui

import android.Manifest
import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.WebRtcVoiceManager
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.voiceroom.MicPermissionBanner
import com.example.ui.screens.voiceroom.computeIsSpeakerActivelyTransmitting
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h891dp")
class VoiceRoomAudioPipelineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun webRtcVoiceManager_tracksRecordAudioPermissionAndRefusesCaptureWhenDenied() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val shadowApp = Shadows.shadowOf(app)

        shadowApp.denyPermissions(Manifest.permission.RECORD_AUDIO)
        val manager = WebRtcVoiceManager(app)
        assertFalse(manager.hasRecordAudioPermission())
        assertFalse(manager.hasMicPermission.value)
        assertFalse(manager.isMicCapturing.value)

        // Attempting to unmute while denied must keep capture disabled
        manager.setMicEnabled(true)
        assertFalse(manager.isMicCapturing.value)
        assertEquals(0f, manager.localAudioLevel.value)

        // Granting permission updates permission flow cleanly
        shadowApp.grantPermissions(Manifest.permission.RECORD_AUDIO)
        assertTrue(manager.refreshMicPermission())
        assertTrue(manager.hasMicPermission.value)

        manager.leaveRoomAudio()
        assertFalse(manager.isMicCapturing.value)
        assertEquals(0f, manager.localAudioLevel.value)
        manager.dispose()
    }

    @Test
    fun speakingIndicator_neverFakesPulseWithoutRealCaptureOrRemoteAudioStats() {
        // Solo host muted or missing permission: NEVER pulses as speaking
        assertFalse(
            computeIsSpeakerActivelyTransmitting(
                isSelf = true,
                hasMicPermission = false,
                isMicCapturing = false,
                isLocalMicMuted = false,
                localAudioLevel = 0.5f,
                isRemoteMuted = false,
                isPeerInActiveStats = false
            )
        )
        // Solo host unmuted & permitted, but silent (0f level): NEVER pulses
        assertFalse(
            computeIsSpeakerActivelyTransmitting(
                isSelf = true,
                hasMicPermission = true,
                isMicCapturing = true,
                isLocalMicMuted = false,
                localAudioLevel = 0.0f,
                isRemoteMuted = false,
                isPeerInActiveStats = false
            )
        )
        // Permitted, capturing, unmuted, and voice energy detected: pulses ON
        assertTrue(
            computeIsSpeakerActivelyTransmitting(
                isSelf = true,
                hasMicPermission = true,
                isMicCapturing = true,
                isLocalMicMuted = false,
                localAudioLevel = 0.18f,
                isRemoteMuted = false,
                isPeerInActiveStats = false
            )
        )
        // Remote unmuted peer with NO inbound RTP audio activity: NEVER pulses
        assertFalse(
            computeIsSpeakerActivelyTransmitting(
                isSelf = false,
                hasMicPermission = false,
                isMicCapturing = false,
                isLocalMicMuted = true,
                localAudioLevel = 0f,
                isRemoteMuted = false,
                isPeerInActiveStats = false
            )
        )
        // Remote unmuted peer WITH active inbound RTP audio stats: pulses ON
        assertTrue(
            computeIsSpeakerActivelyTransmitting(
                isSelf = false,
                hasMicPermission = false,
                isMicCapturing = false,
                isLocalMicMuted = true,
                localAudioLevel = 0f,
                isRemoteMuted = false,
                isPeerInActiveStats = true
            )
        )
    }

    @Test
    fun micPermissionBanner_displaysExplanationAndTriggersGrantAndSettingsActions() {
        var grantClicked = false
        var settingsClicked = false

        composeTestRule.setContent {
            MicPermissionBanner(
                appLanguage = AppLanguage.ENGLISH,
                onRequestPermission = { grantClicked = true },
                onOpenSettings = { settingsClicked = true }
            )
        }

        composeTestRule.onNodeWithTag("voice_room_mic_permission_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("grant_mic_permission_btn").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("open_mic_settings_btn").assertIsDisplayed().performClick()

        assertTrue(grantClicked)
        assertTrue(settingsClicked)
    }
}
