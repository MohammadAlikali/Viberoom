package com.example.ui

import android.app.Application
import android.view.inspector.WindowInspector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.AiLanguageAssistantService
import com.example.data.VibeRoomRepository
import com.example.model.AiCoachTurn
import com.example.model.AiTaskState
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.learning.LanguageLearningHubModal
import com.example.ui.screens.voiceroom.ActiveVoiceRoomModal
import com.example.ui.screens.voiceroom.CreateRoomDialog
import com.example.ui.theme.VibeRoomTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.ai.type.asTextOrNull
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class ReconstructionAndOptimizationTest {

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

    private fun dispatchSimulatedSystemInsets(
        density: Density,
        statusBarDp: Int = 24,
        navBarBottomDp: Int = 48,
        imeBottomDp: Int = 0
    ) {
        val topPx = with(density) { statusBarDp.dp.roundToPx() }
        val navBottomPx = with(density) { navBarBottomDp.dp.roundToPx() }
        val imeBottomPx = with(density) { imeBottomDp.dp.roundToPx() }
        val simulated = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, topPx, 0, 0))
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, navBottomPx))
            .setInsets(WindowInsetsCompat.Type.ime(), Insets.of(0, 0, 0, imeBottomPx))
            .build()

        composeTestRule.runOnUiThread {
            WindowInspector.getGlobalWindowViews().forEach { rootView ->
                ViewCompat.dispatchApplyWindowInsets(rootView, simulated)
            }
        }
        composeTestRule.waitForIdle()
    }

    private fun configuredRepo(): VibeRoomRepository {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "SaraLearner",
            bio = "Practicing languages daily",
            countryName = "United States",
            flagEmoji = "🇺🇸",
            interests = listOf("Languages", "Travel"),
            languagesSpoken = listOf("English"),
            learningLanguages = listOf("Spanish", "Persian")
        )
        return repo
    }

    private fun sixSpeakerRoom(repo: VibeRoomRepository): VoiceRoom {
        val self = repo.currentUser.value
        val host = UserProfile(
            id = "u_host_1",
            name = "Mateo Host",
            username = "mateo",
            bio = "Room host",
            countryName = "Spain",
            countryCode = "ES",
            flagEmoji = "🇪🇸"
        )
        val peers = (2..5).map { idx ->
            UserProfile(
                id = "u_peer_$idx",
                name = "Speaker$idx",
                username = "speaker$idx",
                bio = "",
                countryName = "Canada",
                countryCode = "CA",
                flagEmoji = "🇨🇦"
            )
        }
        val listener = UserProfile(
            id = "u_listener_1",
            name = "LinaListener",
            username = "lina",
            bio = "",
            countryName = "Japan",
            countryCode = "JP",
            flagEmoji = "🇯🇵"
        )
        val speakers = listOf(host, self) + peers
        return VoiceRoom(
            id = "room_6_seats",
            title = "Global Fluency Lounge with Six Active Seats",
            description = "Practice speaking naturally with peers around the world.",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            speakers = speakers,
            listeners = listOf(listener),
            listenersCount = 7,
            isLive = true,
            tags = listOf("Spanish", "English"),
            maxAudioParticipants = 6
        )
    }

    @Test
    fun aiService_buildBoundedChatHistoryCapsTurnsAndLengthAndFiltersIncompleteTurns() {
        val service = AiLanguageAssistantService()
        val longText = "A".repeat(600)
        val turns = (1..10).map { i ->
            AiCoachTurn(
                id = "t_$i",
                isUser = i % 2 == 1,
                text = if (i == 10) longText else "Message #$i",
                isStreaming = false
            )
        } + listOf(
            AiCoachTurn(id = "streaming_partial", isUser = false, text = "partial...", isStreaming = true),
            AiCoachTurn(id = "blank_turn", isUser = true, text = "   ", isStreaming = false)
        )

        val bounded = service.buildBoundedChatHistory(turns)
        assertEquals(6, bounded.size)
        assertEquals("user", bounded.first().role)
        assertEquals("Message #5", bounded.first().parts.first().asTextOrNull())
        val lastText = bounded.last().parts.first().asTextOrNull().orEmpty()
        assertTrue(lastText.length <= 451)
        assertTrue(lastText.endsWith("…"))
    }

    @Test
    fun smartCoach_streamsMultipleMessages_preventsDuplicates_andSupportsStop() {
        val repo = configuredRepo()
        val capturedHistories = mutableListOf<List<AiCoachTurn>>()
        var streamInvocations = 0
        val gateChannel = Channel<String>(Channel.UNLIMITED)

        val fakeAiService = AiLanguageAssistantService { prompt, _, _, _, _, history ->
            streamInvocations += 1
            capturedHistories += history
            flow {
                emit("Part 1 for: $prompt")
                val nextChunk = gateChannel.receive()
                emit("Part 1 for: $prompt — $nextChunk")
            }
        }
        val vm = VibeRoomViewModel(repo, fakeAiService)

        composeTestRule.setContent {
            VibeRoomTheme {
                LanguageLearningHubModal(viewModel = vm, onDismiss = {})
            }
        }
        composeTestRule.waitForIdle()

        // Turn 1: send prompt via UI
        composeTestRule.onNodeWithTag("ai_coach_input").performTextInput("How do I greet politely?")
        composeTestRule.onNodeWithTag("ai_coach_send_button").performClick()
        composeTestRule.waitForIdle()

        // Duplicate identical request while first stream is waiting should be ignored
        vm.sendAiCoachPrompt("How do I greet politely?", "Spanish", "conversation")
        composeTestRule.waitForIdle()
        assertEquals(1, streamInvocations)

        // Stop button is displayed while active; clicking stops stream cleanly
        composeTestRule.onNodeWithTag("ai_coach_stop_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        assertTrue(vm.aiCoachState.value is AiTaskState.Idle)
        assertFalse(vm.aiCoachTurns.value.any { it.isStreaming })

        // Complete a second and third prompt sequentially
        gateChannel.trySend("Complete reply 2")
        vm.sendAiCoachPrompt("Correct my sentence: Yo tiene hambre", "Spanish", "grammar")
        composeTestRule.waitForIdle()

        gateChannel.trySend("Complete reply 3")
        vm.sendAiCoachPrompt("Give me a follow-up phrase", "Spanish", "conversation")
        composeTestRule.waitForIdle()

        assertEquals(3, streamInvocations)
        // Third call received prior turns as history
        assertTrue(capturedHistories.last().size >= 2)
        assertTrue(vm.aiCoachTurns.value.size >= 4)

        val lastTurn = vm.aiCoachTurns.value.last()
        assertFalse(lastTurn.isUser)
        assertFalse(lastTurn.isStreaming)
        composeTestRule.onNodeWithTag("coach_ai_bubble_${lastTurn.id}").assertIsDisplayed()
    }

    @Test
    fun smartCoach_longConversationWithImeOpen_keepsComposerAndSendAboveKeyboard() {
        val repo = configuredRepo()
        val fakeAiService = AiLanguageAssistantService { prompt, _, _, _, _, _ ->
            flow {
                emit("Detailed explanation for '$prompt' with practical bilingual examples and usage tips.")
            }
        }
        val vm = VibeRoomViewModel(repo, fakeAiService)
        repeat(8) { idx ->
            vm.sendAiCoachPrompt("Long conversation turn number ${idx + 1} asking about verbs", "Spanish", "conversation")
        }

        var capturedDensity = Density(1f)
        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                LanguageLearningHubModal(viewModel = vm, onDismiss = {})
            }
        }
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(
            density = capturedDensity,
            statusBarDp = 24,
            navBarBottomDp = 48,
            imeBottomDp = 220
        )

        composeTestRule.onNodeWithTag("ai_coach_message_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_send_button").assertIsDisplayed()

        val inputBounds = composeTestRule.onNodeWithTag("ai_coach_input").getBoundsInRoot()
        val sendBounds = composeTestRule.onNodeWithTag("ai_coach_send_button").getBoundsInRoot()
        val maxBottomAllowed = 568.dp - 220.dp + 1.dp

        assertTrue(
            "Expected ai_coach_input bottom (${inputBounds.bottom}) above IME ($maxBottomAllowed)",
            inputBounds.bottom <= maxBottomAllowed
        )
        assertTrue(
            "Expected ai_coach_send_button bottom (${sendBounds.bottom}) above IME ($maxBottomAllowed)",
            sendBounds.bottom <= maxBottomAllowed
        )
    }

    @Test
    fun createRoom_compactWithImeOpen_scrollsLongFormAndKeepsCreateButtonAboveKeyboard() {
        var capturedDensity = Density(1f)
        var createdTitle: String? = null
        var createdStageLocked: Boolean? = null

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                CreateRoomDialog(
                    appLanguage = AppLanguage.ENGLISH,
                    isCreating = false,
                    onDismiss = {},
                    onCreate = { _, _, _, _ -> },
                    onCreateConfigured = { title, _, _, _, _, _, isStageLocked ->
                        createdTitle = title
                        createdStageLocked = isStageLocked
                    }
                )
            }
        }
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(
            density = capturedDensity,
            statusBarDp = 24,
            navBarBottomDp = 48,
            imeBottomDp = 220
        )

        composeTestRule.onNodeWithTag("create_room_title_input").performTextInput("Kabul & Madrid Exchange")
        composeTestRule.onNodeWithTag("create_room_description_input").performTextInput("Friendly evening practice.")
        composeTestRule.onNodeWithTag("create_room_stage_lock_switch").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        val submitBounds = composeTestRule.onNodeWithTag("submit_create_room").getBoundsInRoot()
        val maxBottomAllowed = 568.dp - 220.dp + 1.dp
        composeTestRule.onNodeWithTag("submit_create_room").assertIsDisplayed()
        assertTrue(
            "Expected submit_create_room bottom (${submitBounds.bottom}) above IME ($maxBottomAllowed)",
            submitBounds.bottom <= maxBottomAllowed
        )

        composeTestRule.onNodeWithTag("submit_create_room").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Kabul & Madrid Exchange", createdTitle)
        assertEquals(true, createdStageLocked)
    }

    @Test
    fun activeVoiceRoom_sixSpeakersCompact_togglesMicOpensChatWithImeAndLeavesCleanly() {
        val repo = configuredRepo()
        val room = sixSpeakerRoom(repo)
        repo.joinVoiceRoom(room)
        val vm = VibeRoomViewModel(repo)
        var leaveInvoked = false
        var capturedDensity = Density(1f)

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                ActiveVoiceRoomModal(
                    room = room,
                    viewModel = vm,
                    onMinimize = {},
                    onLeave = { leaveInvoked = true }
                )
            }
        }
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(capturedDensity, statusBarDp = 24, navBarBottomDp = 48, imeBottomDp = 0)

        // All 6 speaker seats remain accessible in 2-col responsive grid on 320dp
        composeTestRule.onNodeWithTag("speaker_tile_u_host_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_stage_grid")
            .performScrollToNode(hasTestTag("speaker_tile_u_peer_5"))
        composeTestRule.onNodeWithTag("speaker_tile_u_peer_5").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_mic_toggle").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("open_studio_tools_btn").assertIsDisplayed()

        // Open Room Chat & Polls and simulate software keyboard
        composeTestRule.onNodeWithTag("open_room_chat_btn").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(capturedDensity, statusBarDp = 24, navBarBottomDp = 48, imeBottomDp = 220)

        composeTestRule.onNodeWithTag("room_chat_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("room_chat_send_btn").assertIsDisplayed()
        val chatInputBounds = composeTestRule.onNodeWithTag("room_chat_input").getBoundsInRoot()
        val maxBottomAllowed = 568.dp - 220.dp + 1.dp
        assertTrue(
            "Expected room_chat_input bottom (${chatInputBounds.bottom}) above IME ($maxBottomAllowed)",
            chatInputBounds.bottom <= maxBottomAllowed
        )

        // Dismiss chat dialog and verify Leave button works cleanly
        composeTestRule.onNodeWithTag("room_chat_polls_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("close_room_chat_btn").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("leave_room_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        assertTrue(leaveInvoked)
    }

    @Test
    fun activeVoiceRoom_leaveAndRejoinPreservesSixSeatInvariant() {
        val repo = configuredRepo()
        val room = sixSpeakerRoom(repo)
        repo.joinVoiceRoom(room)
        val vm = VibeRoomViewModel(repo)
        var leaveCount = 0

        composeTestRule.setContent {
            VibeRoomTheme {
                ActiveVoiceRoomModal(
                    room = room,
                    viewModel = vm,
                    onMinimize = {},
                    onLeave = { leaveCount += 1 }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("leave_room_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, leaveCount)

        // Rejoin another room and confirm seat ordering stays strictly bounded at 6
        repo.leaveVoiceRoom()
        repo.joinVoiceRoom(room)
        assertEquals(6, room.orderedSeatSlots().size)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h600dp")
    fun wideTabletViewport_smartCoachAndSixSpeakerRoomRenderUnclipped() {
        val repo = configuredRepo()
        val room = sixSpeakerRoom(repo)
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

        // On 840dp wide viewport, all 6 speakers fit in 3x2 grid and all dock controls are visible
        composeTestRule.onNodeWithTag("speaker_tile_u_host_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("speaker_tile_u_peer_5").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_mic_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("voice_room_speaker_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dock_gift_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_room_chat_btn").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_studio_tools_btn").assertIsDisplayed()
    }

    @Test
    @Config(sdk = [36], qualifiers = "w840dp-h600dp")
    fun wideTabletViewport_smartCoachRendersTabsMessagesAndComposerUnclipped() {
        val repo = configuredRepo()
        val fakeAiService = AiLanguageAssistantService { prompt, _, _, _, _, _ ->
            flow { emit("Wide tablet response for: $prompt") }
        }
        val vm = VibeRoomViewModel(repo, fakeAiService)
        vm.sendAiCoachPrompt("Test tablet layout", "Spanish", "conversation")

        composeTestRule.setContent {
            VibeRoomTheme {
                LanguageLearningHubModal(viewModel = vm, onDismiss = {})
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("learning_hub_modal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("learning_tab_0").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_message_list").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("ai_coach_send_button").assertIsDisplayed()
    }
}
