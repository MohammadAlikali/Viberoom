package com.example.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.VibeRoomRepository
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.learning.LanguageLearningHubModal
import com.example.ui.screens.voiceroom.ActiveVoiceRoomModal
import com.example.ui.theme.VibeRoomTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class SmallScreenRegressionTest {

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

    private fun sampleUser(id: String, name: String) = UserProfile(
        id = id,
        name = name,
        username = id,
        bio = "Tester",
        countryName = "United States",
        countryCode = "US",
        flagEmoji = "🇺🇸",
        nativeLanguages = listOf("English"),
        learningLanguages = listOf("Spanish", "Japanese")
    )

    @Test
    fun activeVoiceRoomModal_compact320dpViewportShowsTopControlsAndBottomMicDockUnclipped() {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "Listener",
            bio = "Tester",
            countryName = "United States",
            flagEmoji = "🇺🇸",
            interests = listOf("Music"),
            languagesSpoken = listOf("English"),
            learningLanguages = listOf("Spanish")
        )
        val host = sampleUser("u_host", "Host")
        val listener = repo.currentUser.value
        val room = VoiceRoom(
            id = "r_compact",
            title = "Very Long Compact Viewport Room Title For Overflow Testing",
            description = "Compact layout check",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            speakers = listOf(host),
            listeners = listOf(listener),
            listenersCount = 2,
            isLive = true
        )
        repo.joinVoiceRoom(room)
        val vm = VibeRoomViewModel(repo)

        composeTestRule.setContent {
            VibeRoomTheme {
                ActiveVoiceRoomModal(
                    room = room,
                    viewModel = vm,
                    onMinimize = {},
                    onLeave = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("minimize_room_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_webrtc_status_pill").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_moderator_deck_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("leave_room_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_mic_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_room_chat_btn").assertIsDisplayed()
    }

    @Test
    fun languageLearningHubModal_compact320dpViewportRendersTabsAndComposerUnclipped() {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "Learner",
            bio = "Tester",
            countryName = "United States",
            flagEmoji = "🇺🇸",
            interests = listOf("Languages"),
            languagesSpoken = listOf("English"),
            learningLanguages = listOf("Spanish", "Japanese")
        )
        val vm = VibeRoomViewModel(repo)

        composeTestRule.setContent {
            VibeRoomTheme {
                LanguageLearningHubModal(
                    viewModel = vm,
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("learning_hub_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_learning_hub_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("learning_tab_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_send_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("learning_tab_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("translator_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("translator_submit_button").assertIsDisplayed()
    }

    @Test
    fun chatDetailScreen_compact320dpViewportRendersHeaderAndComposerUnclipped() {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "Me",
            bio = "Tester",
            countryName = "United States",
            flagEmoji = "🇺🇸",
            interests = listOf("Chat"),
            languagesSpoken = listOf("English")
        )
        val peer = sampleUser("u_peer", "Sophia")
        val conv = repo.getOrCreateConversationForUser(peer)
        repo.sendMessage(conv.id, "Testing compact chat screen layout")
        val vm = VibeRoomViewModel(repo)

        composeTestRule.setContent {
            VibeRoomTheme {
                ChatDetailScreen(
                    conversationId = conv.id,
                    viewModel = vm,
                    onBack = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("chat_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_composer_surface").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_text_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_send_button").assertIsDisplayed()
    }
}
