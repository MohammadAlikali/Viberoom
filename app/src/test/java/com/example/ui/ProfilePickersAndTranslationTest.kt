package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.model.RoomCategory
import com.example.model.VibeCatalogs
import com.example.ui.components.AvatarFrame
import com.example.ui.components.CategorizedInterestsPicker
import com.example.ui.components.LearningLanguagesRankPicker
import com.example.ui.localization.AppLanguage
import com.example.ui.screens.voiceroom.CreateRoomDialogContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ProfilePickersAndTranslationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun avatarFrameCatalog_hasMoreThanFiveLiveAnimatedFrames() {
        val animatedFrames = AvatarFrame.entries.filter { it != AvatarFrame.NONE }
        assertTrue(
            "Expected more than 5 live animated avatar frames, found ${animatedFrames.size}",
            animatedFrames.size > 5
        )
        assertEquals(animatedFrames.size, animatedFrames.map { it.id }.distinct().size)
    }

    @Test
    fun learningLanguagesRankPicker_rendersFiveRanksAndUpdatesRankSelection() {
        var updatedLang = ""
        var updatedRank = -1

        composeTestRule.setContent {
            LearningLanguagesRankPicker(
                learningLanguageLevels = mapOf("English" to 2),
                onSetLanguageRank = { lang, rank ->
                    updatedLang = lang
                    updatedRank = rank
                },
                onRemoveLanguage = {},
                appLanguage = AppLanguage.ENGLISH
            )
        }

        composeTestRule.onNodeWithTag("learning_lang_rank_card_English").assertIsDisplayed()
        for (rank in 1..5) {
            composeTestRule.onNodeWithTag("rank_pill_English_$rank", useUnmergedTree = true).assertExists()
        }

        composeTestRule.onNodeWithTag("rank_pill_English_4", useUnmergedTree = true)
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals("English", updatedLang)
        assertEquals(4, updatedRank)
    }

    @Test
    fun categorizedInterestsPicker_togglesCuratedChipOnClick() {
        var toggledInterest = ""

        composeTestRule.setContent {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                CategorizedInterestsPicker(
                    selectedInterests = listOf("Music"),
                    onToggleInterest = { toggledInterest = it },
                    appLanguage = AppLanguage.ENGLISH
                )
            }
        }

        composeTestRule.onNodeWithTag("interest_option_Coding", useUnmergedTree = true)
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals("Coding", toggledInterest)
    }

    @Test
    fun createRoomDialog_usesCuratedRoomTagsAndSubmits() {
        var createdTitle = ""
        var createdCategory: RoomCategory? = null
        var createdTags = emptyList<String>()

        composeTestRule.setContent {
            CreateRoomDialogContent(
                appLanguage = AppLanguage.ENGLISH,
                onDismiss = {},
                onCreate = { title, _, cat, tags ->
                    createdTitle = title
                    createdCategory = cat
                    createdTags = tags
                }
            )
        }

        composeTestRule.onNodeWithTag("create_room_tags_picker").assertExists()
        composeTestRule.onNodeWithTag("create_room_title_input").performTextInput("Dari & English Club")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("submit_create_room")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals("Dari & English Club", createdTitle)
        assertEquals(RoomCategory.LANGUAGE_EXCHANGE, createdCategory)
        assertTrue(createdTags.contains("Daily Practice"))
    }

    @Test
    fun translationCatalogs_defaultsMatchAppLanguages() {
        assertEquals("Persian (Dari)", VibeCatalogs.defaultTranslateLanguageFor(AppLanguage.DARI_PERSIAN))
        assertEquals("Pashto", VibeCatalogs.defaultTranslateLanguageFor(AppLanguage.PASHTO))
        assertEquals("English", VibeCatalogs.defaultTranslateLanguageFor(AppLanguage.ENGLISH))
        assertTrue(VibeCatalogs.summarizeProficiency(mapOf("English" to 4), AppLanguage.ENGLISH).contains("B2"))
    }
}
