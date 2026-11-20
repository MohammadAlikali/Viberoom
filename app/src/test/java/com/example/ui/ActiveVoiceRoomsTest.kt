package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.screens.home.ActiveVoiceRoomCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ActiveVoiceRoomsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun activeVoiceRoomCard_displaysTitleHostAndListenerCount() {
        var joinClicked = false

        val testHost = UserProfile(
            id = "host_101",
            name = "Elena Rostova",
            username = "elena_r",
            bio = "Building startups and exploring Europe.",
            countryName = "Germany",
            countryCode = "DE",
            flagEmoji = "🇩🇪"
        )

        val testRoom = VoiceRoom(
            id = "room_berlin_tech",
            title = "Berlin Tech Founders Jam",
            description = "Building AI and Android startups in Europe",
            category = RoomCategory.TECH_TALK,
            host = testHost,
            listenersCount = 42,
            isLive = true,
            tags = listOf("Startups", "Android")
        )

        composeTestRule.setContent {
            ActiveVoiceRoomCard(
                room = testRoom,
                onJoin = { joinClicked = true }
            )
        }

        // Verify Title and Description displayed
        composeTestRule.onNodeWithText("Berlin Tech Founders Jam").assertIsDisplayed()
        composeTestRule.onNodeWithText("Building AI and Android startups in Europe").assertIsDisplayed()

        // Verify Host & Listeners Count
        composeTestRule.onNodeWithText("Elena Rostova").assertIsDisplayed()
        composeTestRule.onNodeWithText("42 listening").assertIsDisplayed()

        // Verify Join Stage button and click
        composeTestRule.onNodeWithTag("join_voice_room_room_berlin_tech").assertIsDisplayed()
        composeTestRule.onNodeWithTag("join_voice_room_room_berlin_tech").performClick()
        assertTrue("Join button should trigger onJoin callback", joinClicked)
    }

    @Test
    fun activeVoiceRoomCard_cardClickTriggersJoin() {
        var joinClicked = false

        val testHost = UserProfile(
            id = "host_202",
            name = "Kenji Sato",
            username = "kenji_s",
            bio = "Music producer and night owl in Tokyo.",
            countryName = "Japan",
            countryCode = "JP",
            flagEmoji = "🇯🇵"
        )

        val testRoom = VoiceRoom(
            id = "room_tokyo_chill",
            title = "Late Night Tokyo Chill & Beats",
            description = "Lo-fi tunes and friendly casual conversations",
            category = RoomCategory.MUSIC_JAM,
            host = testHost,
            listenersCount = 88,
            isLive = true,
            tags = listOf("Lo-Fi", "Music")
        )

        composeTestRule.setContent {
            ActiveVoiceRoomCard(
                room = testRoom,
                onJoin = { joinClicked = true }
            )
        }

        // Clicking outer card triggers join
        composeTestRule.onNodeWithTag("voice_room_card_room_tokyo_chill").performClick()
        assertTrue("Card click should trigger onJoin callback", joinClicked)
    }
}
