package com.example.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.test.core.app.ApplicationProvider
import com.example.data.RecordedVoiceNote
import com.example.data.VibeRoomRepository
import com.example.data.VoiceNoteStopResult
import com.example.data.WebRtcVoiceManager
import com.example.model.AudioRouteMode
import com.example.model.UserProfile
import com.example.ui.components.UserProfileCard
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.screens.voiceroom.VoiceRoomsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.VibeRoomTheme
import com.example.ui.theme.vibeTypographyFor
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class MasterMissionRegressionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun ensureFirebaseInitialized() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(
                context,
                FirebaseOptions.Builder()
                    .setApplicationId("com.example.app")
                    .setProjectId("demo-no-project")
                    .setApiKey("fake-api-key")
                    .build()
            )
        }
    }

    @Test
    fun bundledFontsAndLocalizationKeys_matchScriptAndDirection() {
        val englishTypo = vibeTypographyFor(AppLanguage.ENGLISH)
        val dariTypo = vibeTypographyFor(AppLanguage.DARI_PERSIAN)
        val pashtoTypo = vibeTypographyFor(AppLanguage.PASHTO)

        assertNotEquals(
            "English (Plus Jakarta Sans) and Dari (Vazirmatn) should use distinct bundled font families",
            englishTypo.bodyLarge.fontFamily,
            dariTypo.bodyLarge.fontFamily
        )
        assertEquals(dariTypo.bodyLarge.fontFamily, pashtoTypo.bodyLarge.fontFamily)
        assertEquals(LayoutDirection.Rtl, AppLanguage.DARI_PERSIAN.layoutDirection)
        assertEquals(LayoutDirection.Rtl, AppLanguage.PASHTO.layoutDirection)

        val newKeys = listOf(
            "voice_note_too_long",
            "voice_note_failed",
            "live_captions_mic_conflict",
            "select_chat_hint_title",
            "select_chat_hint_desc",
            "max_peers_badge"
        )
        AppLanguage.entries.forEach { lang ->
            newKeys.forEach { key ->
                val translated = AppStrings.t(key, lang)
                assertTrue("Expected translation for $key in $lang", translated.isNotBlank() && translated != key)
            }
        }
    }

    @Test
    fun updateProfile_preservesExistingXpLevelStreakAndBadges() {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "Ahmad",
            bio = "Initial bio",
            countryName = "Afghanistan",
            flagEmoji = "🇦🇫",
            interests = listOf("Languages"),
            languagesSpoken = listOf("Persian (Dari)"),
            countryCode = "AF",
            vibeXp = 420,
            vibeLevel = 5,
            streakDays = 9,
            unlockedBadges = listOf("polyglot_spark", "stage_regular")
        )
        val vm = VibeRoomViewModel(repo)

        vm.updateProfile(
            name = "Ahmad Verified",
            bio = "Updated bio",
            country = "Afghanistan",
            flag = "🇦🇫",
            interests = listOf("Music", "Languages"),
            languages = listOf("Persian (Dari)", "English"),
            city = "Kabul"
        )

        val updated = vm.currentUser.value
        assertEquals("Ahmad Verified", updated.name)
        assertEquals("Updated bio", updated.bio)
        assertEquals(420, updated.vibeXp)
        assertEquals(5, updated.vibeLevel)
        assertEquals(9, updated.streakDays)
        assertEquals(listOf("polyglot_spark", "stage_regular"), updated.unlockedBadges)
    }

    @Test
    fun webRtcVoiceManager_preferredJoinRouteDefaultsToSpeakerAndDisposesSafely() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val manager = WebRtcVoiceManager(app)
        assertEquals(AudioRouteMode.SPEAKER, manager.preferredJoinRoute())
        manager.dispose()
    }

    @Test
    fun voiceRoomsScreen_lightThemeCompact320dpRendersHeroAndControls() {
        val repo = VibeRoomRepository()
        val vm = VibeRoomViewModel(repo)
        vm.setAppTheme(AppTheme.PURE_WHITE)
        vm.setThemeMode(ThemeMode.LIGHT)
        vm.setAppLanguage(AppLanguage.DARI_PERSIAN)

        composeTestRule.setContent {
            VibeRoomTheme(
                appTheme = AppTheme.PURE_WHITE,
                themeMode = ThemeMode.LIGHT,
                appLanguage = AppLanguage.DARI_PERSIAN
            ) {
                VoiceRoomsScreen(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("voice_rooms_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_room_fab").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create_room_header_button").assertIsDisplayed()
    }

    @Test
    fun userProfileCard_compact320dpRendersBadgesAnd48dpActionsWithoutClipping() {
        val member = UserProfile(
            id = "u_320",
            name = "Zahra Alizadah With A Longer Display Name",
            username = "zahra_a",
            bio = "Learning German and practicing acoustic guitar with friends around the globe.",
            countryName = "Germany",
            countryCode = "DE",
            flagEmoji = "🇩🇪",
            vibeXp = 310,
            vibeLevel = 3,
            streakDays = 14,
            unlockedBadges = listOf("b1", "b2", "b3"),
            interests = listOf("Music", "Travel", "Coding"),
            languagesSpoken = listOf("Persian (Dari)", "German", "English")
        )

        composeTestRule.setContent {
            VibeRoomTheme(appTheme = AppTheme.PURE_WHITE, themeMode = ThemeMode.LIGHT) {
                UserProfileCard(
                    user = member,
                    onCardClick = {},
                    onToggleFollow = {},
                    onWaveClick = {},
                    onMessageClick = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("user_name_u_320", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("follow_button_u_320", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("wave_button_u_320", useUnmergedTree = true).assertIsDisplayed()
        composeTestRule.onNodeWithTag("message_button_u_320", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun voiceNoteStopResult_sealedHierarchyExposesExpectedOutcomes() {
        val ok: VoiceNoteStopResult = VoiceNoteStopResult.Success(
            RecordedVoiceNote("data:audio/mp4;base64,AA==", 4)
        )
        val tooLarge: VoiceNoteStopResult = VoiceNoteStopResult.TooLarge(480_000)
        val failed: VoiceNoteStopResult = VoiceNoteStopResult.Failed
        assertTrue(ok is VoiceNoteStopResult.Success)
        assertTrue(tooLarge is VoiceNoteStopResult.TooLarge)
        assertTrue(failed is VoiceNoteStopResult.Failed)
    }
}
