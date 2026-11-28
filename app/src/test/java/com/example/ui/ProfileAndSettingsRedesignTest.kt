package com.example.ui

import android.app.Application
import android.view.inspector.WindowInspector
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ApplicationProvider
import com.example.VibeRoomApp
import com.example.data.FirestorePost
import com.example.data.VibeRoomRepository
import com.example.ui.localization.AppLanguage
import com.example.ui.navigation.VibeRoutes
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SettingsScreen
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.VibeRoomTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class ProfileAndSettingsRedesignTest {

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

    private fun createPopulatedViewModel(): VibeRoomViewModel {
        val repo = VibeRoomRepository()
        repo.updateCurrentUser(
            name = "Nadia Rahimi",
            username = "nadia_r",
            bio = "Practicing Japanese and Dari daily from Kabul.",
            countryName = "Afghanistan",
            flagEmoji = "🇦🇫",
            cityName = "Kabul",
            interests = listOf("Languages", "Travel"),
            languagesSpoken = listOf("Persian (Dari)", "English"),
            nativeLanguages = listOf("Persian (Dari)"),
            learningLanguages = listOf("Japanese"),
            learningLanguageLevels = mapOf("Japanese" to 3),
            proficiencyLevel = "B1 Intermediate",
            vibeXp = 250,
            vibeLevel = 3,
            unlockedBadges = listOf("polyglot_spark")
        )
        return VibeRoomViewModel(repo).also {
            it.setAppLanguage(AppLanguage.ENGLISH)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun injectPosts(vm: VibeRoomViewModel, posts: List<FirestorePost>) {
        val field = VibeRoomViewModel::class.java.getDeclaredField("_firestorePosts")
        field.isAccessible = true
        val flow = field.get(vm) as MutableStateFlow<List<FirestorePost>>
        flow.value = posts
    }

    @Test
    fun profileScreen_displaysYeetalkIdentityBioEmptyPostsAndNoScatteredSettings() {
        val vm = createPopulatedViewModel()
        var settingsClicks = 0

        composeTestRule.setContent {
            VibeRoomTheme(appTheme = AppTheme.PURE_WHITE, themeMode = ThemeMode.LIGHT) {
                ProfileScreen(
                    viewModel = vm,
                    onOpenSettings = { settingsClicks++ }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Nadia Rahimi").assertIsDisplayed()
        composeTestRule.onNodeWithText("@nadia_r").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_bio_text").assertIsDisplayed()
        composeTestRule.onNodeWithText("Practicing Japanese and Dari daily from Kabul.").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_followers_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_following_count").assertIsDisplayed()
        composeTestRule.onNodeWithTag("edit_profile_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("share_profile_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_settings_button").assertIsDisplayed()

        // Ensure scattered settings were completely removed from ProfileScreen
        composeTestRule.onNodeWithTag("sign_out_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("delete_account_button").assertDoesNotExist()
        composeTestRule.onNodeWithTag("notifications_enabled_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("discovery_visibility_toggle").assertDoesNotExist()
        composeTestRule.onNodeWithTag("theme_mode_dark").assertDoesNotExist()
        composeTestRule.onNodeWithTag("language_option_en").assertDoesNotExist()

        composeTestRule.onNodeWithTag("profile_settings_button").performClick()
        assertEquals(1, settingsClicks)

        composeTestRule.onNodeWithTag("profile_screen")
            .performScrollToNode(hasTestTag("profile_posts_empty"))
        composeTestRule.onNodeWithTag("profile_posts_empty").assertIsDisplayed()
    }

    @Test
    fun profileScreen_showsOnlyRealCurrentUserPosts() {
        val vm = createPopulatedViewModel()
        val myId = vm.currentUser.value.id
        injectPosts(
            vm,
            listOf(
                FirestorePost(
                    id = "my_post_1",
                    authorId = myId,
                    authorName = "Nadia Rahimi",
                    content = "Today I learned 10 new Japanese phrases!",
                    category = "Languages",
                    likesCount = 7,
                    commentsCount = 2
                ),
                FirestorePost(
                    id = "other_post_99",
                    authorId = "someone_else_uid",
                    authorName = "Other Member",
                    content = "Should not appear on Nadia's personal profile",
                    category = "Casual"
                )
            )
        )

        composeTestRule.setContent {
            VibeRoomTheme {
                ProfileScreen(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_posts_empty").assertDoesNotExist()
        composeTestRule.onNodeWithTag("profile_screen")
            .performScrollToNode(hasTestTag("post_card_my_post_1"))
        composeTestRule.onNodeWithTag("post_card_my_post_1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Today I learned 10 new Japanese phrases!").assertIsDisplayed()
        composeTestRule.onNodeWithTag("post_card_other_post_99").assertDoesNotExist()
    }

    @Test
    fun vibeRoomApp_navigatesFromProfileGearToSettingsAndBack() {
        val vm = createPopulatedViewModel()
        vm.selectTab(NavigationTab.PROFILE)

        assertEquals("settings", VibeRoutes.SETTINGS)
        assertNull(VibeRoutes.toTab(VibeRoutes.SETTINGS))

        composeTestRule.setContent {
            VibeRoomTheme {
                VibeRoomApp(viewModel = vm)
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_settings_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_back_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vibe_bottom_nav").assertDoesNotExist()

        composeTestRule.onNodeWithTag("settings_back_button").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("profile_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("vibe_bottom_nav").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_rendersAllCategoriesAndPersistsRealSettingsChanges() {
        val vm = createPopulatedViewModel()
        var backClicks = 0

        composeTestRule.setContent {
            val currentTheme by vm.appTheme.collectAsState()
            val currentMode by vm.themeMode.collectAsState()
            val currentLang by vm.appLanguage.collectAsState()
            VibeRoomTheme(
                appTheme = currentTheme,
                themeMode = currentMode,
                appLanguage = currentLang
            ) {
                SettingsScreen(
                    viewModel = vm,
                    onBack = { backClicks++ }
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_section_account").assertIsDisplayed()

        // Privacy toggle
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_privacy"))
        composeTestRule.onNodeWithTag("settings_section_privacy").assertIsDisplayed()
        assertTrue(vm.currentUser.value.isDiscoveryVisible)
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("discovery_visibility_toggle"))
        composeTestRule.onNodeWithTag("discovery_visibility_toggle").performClick()
        composeTestRule.waitForIdle()
        assertFalse(vm.currentUser.value.isDiscoveryVisible)

        // Notifications toggle
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("notifications_enabled_toggle"))
        composeTestRule.onNodeWithTag("settings_section_notifications").assertIsDisplayed()
        assertTrue(vm.notificationsEnabled.value)
        composeTestRule.onNodeWithTag("notifications_enabled_toggle").performClick()
        composeTestRule.waitForIdle()
        assertFalse(vm.notificationsEnabled.value)

        // Language & Translation section
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("language_option_fa"))
        composeTestRule.onNodeWithTag("settings_section_language").assertIsDisplayed()
        composeTestRule.onNodeWithTag("language_option_fa").performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppLanguage.DARI_PERSIAN, vm.appLanguage.value)
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("language_option_en"))
        composeTestRule.onNodeWithTag("language_option_en").performClick()
        composeTestRule.waitForIdle()

        // Appearance & Theme section
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_appearance"))
        composeTestRule.onNodeWithTag("settings_section_appearance").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("theme_mode_dark"))
        composeTestRule.onNodeWithTag("theme_mode_dark").performClick()
        composeTestRule.waitForIdle()
        assertEquals(ThemeMode.DARK, vm.themeMode.value)

        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_visual_fx"))
        composeTestRule.onNodeWithTag("low_power_mode_toggle").performClick()
        composeTestRule.waitForIdle()
        assertTrue(vm.lowPowerVisualMode.value)

        // Audio, Permissions, About, Logout sections
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_audio"))
        composeTestRule.onNodeWithTag("settings_section_audio").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_permissions"))
        composeTestRule.onNodeWithTag("settings_section_permissions").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_about"))
        composeTestRule.onNodeWithTag("settings_section_about").assertIsDisplayed()
        composeTestRule.onNodeWithTag("settings_list")
            .performScrollToNode(hasTestTag("settings_section_logout"))
        composeTestRule.onNodeWithTag("settings_section_logout").assertIsDisplayed()
        composeTestRule.onNodeWithTag("sign_out_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("delete_account_button").assertIsDisplayed()

        // Top bar back button
        composeTestRule.onNodeWithTag("settings_back_button").performClick()
        assertEquals(1, backClicks)
    }

    @Test
    fun settingsScreen_respectsSafeDrawingInsetsInCompactViewport() {
        val vm = createPopulatedViewModel()
        var capturedDensity = Density(1f)

        composeTestRule.setContent {
            capturedDensity = LocalDensity.current
            VibeRoomTheme {
                SettingsScreen(viewModel = vm, onBack = {})
            }
        }
        composeTestRule.waitForIdle()

        val topPx = with(capturedDensity) { 24.dp.roundToPx() }
        val bottomPx = with(capturedDensity) { 48.dp.roundToPx() }
        val insets = WindowInsetsCompat.Builder()
            .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, topPx, 0, 0))
            .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottomPx))
            .build()
        composeTestRule.runOnUiThread {
            WindowInspector.getGlobalWindowViews().forEach { root ->
                ViewCompat.dispatchApplyWindowInsets(root, insets)
            }
        }
        composeTestRule.waitForIdle()

        val screenBounds = composeTestRule.onNodeWithTag("settings_screen").getBoundsInRoot()
        val backBounds = composeTestRule.onNodeWithTag("settings_back_button").getBoundsInRoot()
        assertTrue(
            "Expected settings back button (${backBounds.top}) below status bar (${screenBounds.top + 24.dp})",
            backBounds.top >= screenBounds.top + 24.dp - 1.dp
        )
    }
}
