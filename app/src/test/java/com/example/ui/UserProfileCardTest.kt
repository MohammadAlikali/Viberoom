package com.example.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.model.UserProfile
import com.example.ui.components.EditBioAndInterestsContent
import com.example.ui.components.UserProfileCard
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserProfileCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun userProfileCard_displaysAvatarBioInterestsAndFollowButton() {
        var followClicked = false
        var waveClicked = false
        var messageClicked = false

        val testUser = UserProfile(
            id = "test_user_42",
            name = "Maya Lin",
            username = "mayalin",
            bio = "Architect & global traveler passionate about languages and cultural exchange.",
            countryName = "Japan",
            countryCode = "JP",
            flagEmoji = "🇯🇵",
            interests = listOf("Architecture", "Travel", "Languages"),
            isOnline = true,
            isFollowing = false
        )

        composeTestRule.setContent {
            UserProfileCard(
                user = testUser,
                onToggleFollow = { followClicked = true },
                onWaveClick = { waveClicked = true },
                onMessageClick = { messageClicked = true }
            )
        }

        // Check name & username displayed
        composeTestRule.onNodeWithText("Maya Lin").assertIsDisplayed()
        composeTestRule.onNodeWithText("@mayalin").assertIsDisplayed()

        // Check bio displayed
        composeTestRule.onNodeWithTag("user_bio_test_user_42", useUnmergedTree = true).assertExists()

        // Check social credibility stats row (Followers, Following, Rooms Hosted)
        composeTestRule.onNodeWithTag("user_credibility_stats_test_user_42", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("followers_count_test_user_42", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("following_count_test_user_42", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("rooms_hosted_count_test_user_42", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Followers", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Following", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Rooms Hosted", useUnmergedTree = true).assertExists()

        // Check interests displayed
        composeTestRule.onNodeWithText("Architecture", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Travel", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("Languages", useUnmergedTree = true).assertExists()

        // Check follow button and click
        composeTestRule.onNodeWithTag("follow_button_test_user_42", useUnmergedTree = true).performClick()
        assertTrue("Follow action should have been triggered", followClicked)

        // Check wave button and click
        composeTestRule.onNodeWithTag("wave_button_test_user_42", useUnmergedTree = true).performClick()
        assertTrue("Wave action should have been triggered", waveClicked)
    }

    @Test
    fun userProfileCard_displaysCustomFormattedSocialStats() {
        val testUser = UserProfile(
            id = "influencer_99",
            name = "Sofia Reyes",
            username = "sofiareyes",
            bio = "Digital nomad & electronic music producer.",
            countryName = "Spain",
            countryCode = "ES",
            flagEmoji = "🇪🇸",
            followersCount = 1420,
            followingCount = 310,
            roomsHostedCount = 45
        )

        composeTestRule.setContent {
            UserProfileCard(user = testUser)
        }

        // Verify formatted counts (1420 -> 1.4K, 310 -> 310, 45 -> 45)
        composeTestRule.onNodeWithTag("followers_count_influencer_99", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("1.4K", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("310", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithText("45", useUnmergedTree = true).assertExists()
    }

    @Test
    fun userProfileCard_editProfileButton_displaysAndTriggersClick() {
        var editClicked = false

        val testUser = UserProfile(
            id = "current_user_me",
            name = "Alex Vance",
            username = "alexv",
            bio = "Exploring new places and soundscapes.",
            countryName = "United States",
            countryCode = "US",
            flagEmoji = "🇺🇸",
            interests = listOf("Music", "Travel")
        )

        composeTestRule.setContent {
            UserProfileCard(
                user = testUser,
                showEditButton = true,
                onEditProfileClick = { editClicked = true }
            )
        }

        // Check Edit Profile button displayed and click
        composeTestRule.onNodeWithTag("edit_profile_button", useUnmergedTree = true).assertExists()
        composeTestRule.onNodeWithTag("edit_profile_button", useUnmergedTree = true).performClick()
        assertTrue("Edit profile button click callback should be triggered", editClicked)
    }

    @Test
    fun editBioAndInterestsContent_displaysInputsAndSavesChanges() {
        var savedBio = ""
        var savedInterests = emptyList<String>()
        var dismissed = false

        composeTestRule.setContent {
            EditBioAndInterestsContent(
                currentBio = "Exploring new places and soundscapes.",
                currentInterests = listOf("Music", "Travel"),
                onDismiss = { dismissed = true },
                onSave = { bio, interests ->
                    savedBio = bio
                    savedInterests = interests
                }
            )
        }

        // Check modal dialog surface and inputs exist with required testTags
        composeTestRule.onNodeWithTag("edit_profile_dialog").assertExists()
        composeTestRule.onNodeWithTag("edit_profile_bio_input").assertExists()
        composeTestRule.onNodeWithTag("edit_profile_interests_input").assertExists()

        // Click Save Changes button
        composeTestRule.onNodeWithTag("save_profile_button", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        assertEquals("Exploring new places and soundscapes.", savedBio)
        assertEquals(listOf("Music", "Travel"), savedInterests)
    }
}
