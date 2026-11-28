package com.example.ui

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.VibeRoomApp
import com.example.data.VibeRoomRepository
import com.example.model.UserProfile
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
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
@Config(sdk = [36], qualifiers = "w900dp-h1000dp")
class TabletTwoPaneLayoutTest {

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
    fun tabletViewport_rendersNavigationRailAndTwoPaneListDetailChat() {
        val repo = VibeRoomRepository()
        val peer = UserProfile(
            id = "peer_tablet_1",
            name = "Soraya Nouri",
            username = "soraya_n",
            bio = "Practicing languages worldwide",
            countryName = "Afghanistan",
            countryCode = "AF",
            flagEmoji = "🇦🇫"
        )
        val vm = VibeRoomViewModel(repo)
        vm.selectTab(NavigationTab.CHATS)

        composeTestRule.setContent {
            VibeRoomTheme(appTheme = AppTheme.PURE_WHITE, themeMode = ThemeMode.LIGHT) {
                VibeRoomApp(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("vibe_navigation_rail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("two_pane_chat_layout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("two_pane_chat_empty").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_list_screen").assertIsDisplayed()

        // Starting chat selects the conversation in the right pane while preserving rail + list
        composeTestRule.runOnIdle {
            vm.startChatWithUser(peer)
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("vibe_navigation_rail").assertIsDisplayed()
        composeTestRule.onNodeWithTag("two_pane_chat_layout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_list_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_detail_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("chat_back_button").assertDoesNotExist()
    }
}
