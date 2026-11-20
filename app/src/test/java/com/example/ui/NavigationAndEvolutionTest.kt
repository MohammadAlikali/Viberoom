package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VibeCatalogs
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.navigation.VibeRoomBottomBar
import com.example.ui.navigation.VibeRoutes
import com.example.ui.screens.voiceroom.VoiceRoomCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NavigationAndEvolutionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun vibeRoutes_roundTripAndChatEncoding() {
        NavigationTab.entries.forEach { tab ->
            val route = VibeRoutes.fromTab(tab)
            assertEquals(tab, VibeRoutes.toTab(route))
        }
        assertEquals("voice_rooms", VibeRoutes.VOICE_ROOMS)
        assertEquals("feed", VibeRoutes.FEED)
        assertEquals("profile", VibeRoutes.PROFILE)
        assertEquals("chat/conv_123", VibeRoutes.chatDetail("conv_123"))
    }

    @Test
    fun vibeRoomBottomBar_clicksTriggerTabSelection() {
        val clickedTabs = mutableListOf<NavigationTab>()

        composeTestRule.setContent {
            VibeRoomBottomBar(
                currentTab = NavigationTab.VOICE_ROOMS,
                unreadChatsCount = 2,
                currentLanguage = AppLanguage.ENGLISH,
                onTabSelected = { clickedTabs.add(it) }
            )
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("vibe_bottom_nav").assertIsDisplayed()
        composeTestRule.onNodeWithTag("nav_feed").performClick()
        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.waitForIdle()

        assertEquals(listOf(NavigationTab.FEED, NavigationTab.PROFILE), clickedTabs)
    }

    @Test
    fun navHost_transitionsBetweenVoiceRoomsFeedAndProfile() {
        composeTestRule.setContent {
            val navController = rememberNavController()
            androidx.compose.foundation.layout.Column(
                modifier = androidx.compose.ui.Modifier.fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = VibeRoutes.VOICE_ROOMS,
                    modifier = androidx.compose.ui.Modifier.weight(1f)
                ) {
                    composable(VibeRoutes.VOICE_ROOMS) {
                        Text("Voice Rooms Destination")
                    }
                    composable(VibeRoutes.FEED) {
                        Text("Home Feed Destination")
                    }
                    composable(VibeRoutes.PROFILE) {
                        Text("User Profile Destination")
                    }
                }

                VibeRoomBottomBar(
                    currentTab = NavigationTab.VOICE_ROOMS,
                    unreadChatsCount = 0,
                    currentLanguage = AppLanguage.ENGLISH,
                    onTabSelected = { tab ->
                        navController.navigate(VibeRoutes.fromTab(tab)) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Voice Rooms Destination").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_feed").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Home Feed Destination").assertIsDisplayed()

        composeTestRule.onNodeWithTag("nav_profile").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("User Profile Destination").assertIsDisplayed()
    }

    @Test
    @Config(qualifiers = "w411dp-h891dp")
    fun voiceRoomCard_showsPrivateBadgeWhenRoomIsPrivate() {
        val host = UserProfile(
            id = "host_1",
            name = "Sara Ahmadi",
            username = "sara_a",
            bio = "Language learner and host",
            countryName = "Afghanistan",
            countryCode = "AF",
            flagEmoji = "🇦🇫"
        )
        val privateRoom = VoiceRoom(
            id = "room_priv_1",
            title = "Private Study Circle",
            description = "Invite only practice",
            category = RoomCategory.LANGUAGE_EXCHANGE,
            host = host,
            listenersCount = 5,
            isLive = true,
            isPrivate = true
        )

        composeTestRule.setContent {
            VoiceRoomCard(
                room = privateRoom,
                appLanguage = AppLanguage.ENGLISH,
                onJoin = {}
            )
        }
        composeTestRule.waitForIdle()
        val node = composeTestRule.onNodeWithTag("room_private_badge_room_priv_1").fetchSemanticsNode()
        println("PRIVATE_BADGE_DEBUG: bounds=${node.boundsInRoot}, size=${node.size}, placed=${node.layoutInfo.isPlaced}, rootBounds=${composeTestRule.onNodeWithTag("room_card_room_priv_1").fetchSemanticsNode().boundsInRoot}")

        composeTestRule.onNodeWithTag("room_private_badge_room_priv_1").assertExists()
        composeTestRule.onNodeWithTag("room_private_badge_room_priv_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("join_room_button_room_priv_1").assertIsDisplayed()
    }

    @Test
    fun vibeCatalogs_gamificationAndMatchCalculationsAreDeterministic() {
        assertEquals(1, VibeCatalogs.levelFromXp(0))
        assertEquals(1, VibeCatalogs.levelFromXp(99))
        assertEquals(2, VibeCatalogs.levelFromXp(100))
        assertEquals(0.45f, VibeCatalogs.xpProgressInLevel(245), 0.001f)

        val alice = UserProfile(
            id = "u_alice",
            name = "Alice",
            username = "alice_vibe",
            bio = "Music & Coding",
            countryName = "Germany",
            countryCode = "DE",
            flagEmoji = "🇩🇪",
            interests = listOf("Music", "Coding", "Languages"),
            nativeLanguages = listOf("German"),
            learningLanguages = listOf("Dari / Persian")
        )
        val bob = UserProfile(
            id = "u_bob",
            name = "Bob",
            username = "bob_vibe",
            bio = "Exploring tech and poetry",
            countryName = "Afghanistan",
            countryCode = "AF",
            flagEmoji = "🇦🇫",
            interests = listOf("Music", "Coding"),
            nativeLanguages = listOf("Dari / Persian"),
            learningLanguages = listOf("German")
        )
        val match = VibeCatalogs.computeVibeMatchPercent(alice, bob)
        assertTrue("Expected high Vibe DNA match, got $match", match >= 80)
        assertEquals(100, VibeCatalogs.computeVibeMatchPercent(alice, alice))
    }
}
