package com.example.ui

import android.app.Application
import android.view.inspector.WindowInspector
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.VibeRoomRepository
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.navigation.FloatingActiveVoiceBar
import com.example.ui.navigation.VibeRoomBottomBar
import com.example.ui.screens.auth.PermissionIntroScreen
import com.example.ui.screens.voiceroom.ActiveVoiceRoomModal
import com.example.ui.screens.voiceroom.CreateRoomDialog
import com.example.ui.theme.VibeRoomTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class WindowInsetsSafeAreaTest {

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

    private fun sampleRoom(): Pair<VibeRoomRepository, VoiceRoom> {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "HostUser",
            bio = "Safe area test",
            countryName = "United States",
            flagEmoji = "🇺🇸",
            interests = listOf("Languages"),
            languagesSpoken = listOf("English"),
            learningLanguages = listOf("Spanish")
        )
        val host = repo.currentUser.value
        val room = VoiceRoom(
            id = "room_safe_area",
            title = "Safe Area Check Stage",
            description = "Verifies bottom controls stay above system navigation bar",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            speakers = listOf(host),
            listeners = listOf(
                UserProfile(
                    id = "u_listener",
                    name = "Listener",
                    username = "listener",
                    bio = "",
                    countryName = "Canada",
                    countryCode = "CA",
                    flagEmoji = "🇨🇦"
                )
            ),
            listenersCount = 2,
            isLive = true
        )
        repo.joinVoiceRoom(room)
        return repo to room
    }

    @Test
    fun bottomNavigationBarAndFloatingVoiceBar_stayAboveThreeButtonNavBar() {
        var capturedDensity = Density(1f)
        val (_, room) = sampleRoom()

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("safe_area_scaffold_root"),
                    bottomBar = {
                        androidx.compose.foundation.layout.Column {
                            FloatingActiveVoiceBar(
                                room = room,
                                isMuted = false,
                                currentLanguage = AppLanguage.ENGLISH,
                                onExpand = {},
                                onToggleMic = {},
                                onLeave = {}
                            )
                            VibeRoomBottomBar(
                                currentTab = NavigationTab.VOICE_ROOMS,
                                unreadChatsCount = 1,
                                currentLanguage = AppLanguage.ENGLISH,
                                onTabSelected = {}
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("safe_area_content_box")
                    ) {
                        // Reference padding usage
                        innerPadding.calculateBottomPadding()
                    }
                }
            }
        }
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(capturedDensity, statusBarDp = 24, navBarBottomDp = 48)

        val rootBounds = composeTestRule.onNodeWithTag("safe_area_scaffold_root").getBoundsInRoot()
        val navBarBounds = composeTestRule.onNodeWithTag("vibe_bottom_nav").getBoundsInRoot()
        val roomsTabBounds = composeTestRule.onNodeWithTag("nav_voice_rooms").getBoundsInRoot()
        val floatingBarBounds = composeTestRule.onNodeWithTag("floating_voice_bar").getBoundsInRoot()

        val navBottomInsetDp = 48.dp
        val maxAllowedItemBottom = rootBounds.bottom - navBottomInsetDp + 1.dp

        assertTrue(
            "Expected NavigationBar item bottom (${roomsTabBounds.bottom}) <= safe bottom ($maxAllowedItemBottom)",
            roomsTabBounds.bottom <= maxAllowedItemBottom
        )
        assertTrue(
            "Expected NavigationBar background (${navBarBounds.bottom}) to extend edge-to-edge under nav bar (${rootBounds.bottom})",
            navBarBounds.bottom >= rootBounds.bottom - 1.dp
        )
        assertTrue(
            "Expected FloatingActiveVoiceBar (${floatingBarBounds.bottom}) above NavigationBar (${navBarBounds.top})",
            floatingBarBounds.bottom <= navBarBounds.top + 1.dp
        )
    }

    @Test
    fun activeVoiceRoomModal_micControlsStayAboveThreeButtonNavBar() {
        var capturedDensity = Density(1f)
        val (repo, room) = sampleRoom()
        val vm = VibeRoomViewModel(repo)

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
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
        dispatchSimulatedSystemInsets(capturedDensity, statusBarDp = 24, navBarBottomDp = 48)

        val modalBounds = composeTestRule.onNodeWithTag("active_voice_room_modal").getBoundsInRoot()
        val micBounds = composeTestRule.onNodeWithTag("voice_room_mic_toggle").getBoundsInRoot()
        val roomChatBounds = composeTestRule.onNodeWithTag("open_room_chat_btn").getBoundsInRoot()
        val minTopAllowed = modalBounds.top + 24.dp - 1.dp
        val maxBottomAllowed = modalBounds.bottom - 48.dp + 1.dp
        val minimizeBounds = composeTestRule.onNodeWithTag("minimize_room_button").getBoundsInRoot()

        composeTestRule.onNodeWithTag("voice_room_mic_toggle").assertIsDisplayed()
        composeTestRule.onNodeWithTag("open_room_chat_btn").assertIsDisplayed()
        assertTrue(
            "Expected top minimize button (${minimizeBounds.top}) below status bar ($minTopAllowed)",
            minimizeBounds.top >= minTopAllowed
        )
        assertTrue(
            "Expected mic control bottom (${micBounds.bottom}) above 48dp nav bar ($maxBottomAllowed)",
            micBounds.bottom <= maxBottomAllowed
        )
        assertTrue(
            "Expected room chat control bottom (${roomChatBounds.bottom}) above 48dp nav bar ($maxBottomAllowed)",
            roomChatBounds.bottom <= maxBottomAllowed
        )
    }

    @Test
    fun createRoomDialog_buttonsRemainVisibleAboveNavBarAndImeOnCompactViewport() {
        var capturedDensity = Density(1f)

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                CreateRoomDialog(
                    appLanguage = AppLanguage.ENGLISH,
                    isCreating = false,
                    onDismiss = {},
                    onCreate = { _, _, _, _ -> }
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

        composeTestRule.onNodeWithTag("submit_create_room").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()

        val submitBounds = composeTestRule.onNodeWithTag("submit_create_room").getBoundsInRoot()
        val cancelBounds = composeTestRule.onNodeWithText("Cancel").getBoundsInRoot()
        val maxBottomAllowed = 568.dp - 220.dp + 1.dp

        assertTrue(
            "Expected Go Live button bottom (${submitBounds.bottom}) above IME ($maxBottomAllowed)",
            submitBounds.bottom <= maxBottomAllowed
        )
        assertTrue(
            "Expected Cancel button bottom (${cancelBounds.bottom}) above IME ($maxBottomAllowed)",
            cancelBounds.bottom <= maxBottomAllowed
        )
    }

    @Test
    fun permissionIntroScreen_finishButtonRemainsDisplayedOnCompact320dpViewport() {
        var capturedDensity = Density(1f)

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("permission_outer_root")
                ) {
                    PermissionIntroScreen(
                        appLanguage = AppLanguage.ENGLISH,
                        isSaving = false,
                        onFinish = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()
        dispatchSimulatedSystemInsets(capturedDensity, statusBarDp = 24, navBarBottomDp = 48)

        val rootBounds = composeTestRule.onNodeWithTag("permission_outer_root").getBoundsInRoot()
        val finishBounds = composeTestRule.onNodeWithTag("finish_onboarding_button").getBoundsInRoot()

        composeTestRule.onNodeWithTag("finish_onboarding_button").assertIsDisplayed()
        assertTrue(
            "Expected onboarding finish button (${finishBounds.bottom}) above 48dp nav bar (${rootBounds.bottom - 48.dp})",
            finishBounds.bottom <= rootBounds.bottom - 48.dp + 1.dp
        )
    }
}
