package com.example.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.model.UserProfile
import com.example.ui.components.PrivacyAwareLastSeenFormatter
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.screens.chat.DateSeparatorChip
import com.example.ui.screens.chat.formatChatDateLabel
import com.example.ui.screens.profile.ThemeStudioSection
import com.example.ui.theme.AccentColorPresets
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.VibeRoomTheme
import com.example.ui.theme.buildSchemeForTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ThemeAndPreferencesTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun allTenThemesBuildValidLightAndDarkColorSchemes() {
        assertEquals(10, AppTheme.entries.size)
        AppTheme.entries.forEach { theme ->
            val light = buildSchemeForTheme(appTheme = theme, isDark = false)
            val dark = buildSchemeForTheme(appTheme = theme, isDark = true)
            assertNotEquals("Primary and background must differ in light mode for $theme", light.primary, light.background)
            assertNotEquals("Primary and background must differ in dark mode for $theme", dark.primary, dark.background)
            assertNotEquals("onSurface and surface must differ in light mode for $theme", light.onSurface, light.surface)
            assertNotEquals("onSurface and surface must differ in dark mode for $theme", dark.onSurface, dark.surface)
        }
    }

    @Test
    fun appTheme_fromStoredNameMigratesLegacyEnumsSafely() {
        assertEquals(AppTheme.LAVENDER, AppTheme.fromStoredName("NEON_VIBE"))
        assertEquals(AppTheme.ROSE, AppTheme.fromStoredName("SUNSET_GLOW"))
        assertEquals(AppTheme.DEEP_NAVY, AppTheme.fromStoredName("OCEAN_MIDNIGHT"))
        assertEquals(AppTheme.EMERALD, AppTheme.fromStoredName("EMERALD_NIGHT"))
        assertEquals(AppTheme.AMOLED_BLACK, AppTheme.fromStoredName("amoled_black"))
        assertEquals(AppTheme.PURE_WHITE, AppTheme.fromStoredName("UNKNOWN_LEGACY"))
        assertEquals(AppTheme.PURE_WHITE, AppTheme.fromStoredName(null))
    }

    @Test
    fun customAccentComputesAccessibleOnPrimaryContrast() {
        val brightAccent = 0xFFFFEB3B // Bright yellow -> dark onPrimary
        val deepAccent = 0xFF311B92   // Deep indigo -> white onPrimary

        val brightScheme = buildSchemeForTheme(AppTheme.PURE_WHITE, isDark = false, customAccentHex = brightAccent)
        val deepScheme = buildSchemeForTheme(AppTheme.MIDNIGHT_BLACK, isDark = true, customAccentHex = deepAccent)

        assertEquals(Color(brightAccent), brightScheme.primary)
        assertEquals(Color(0xFF0F172A), brightScheme.onPrimary)

        assertEquals(Color(deepAccent), deepScheme.primary)
        assertEquals(Color.White, deepScheme.onPrimary)
    }

    @Test
    fun presenceVisibilityRespectsOnlineAndLastSeenPrivacyRules() {
        val hiddenOnlineUser = UserProfile(
            id = "u1",
            name = "Aria",
            username = "aria",
            bio = "Hi",
            countryName = "Canada",
            countryCode = "CA",
            flagEmoji = "🇨🇦",
            isOnline = true,
            showOnlineStatus = false,
            lastSeenVisibility = "nobody",
            isFollowing = true
        )
        assertFalse(hiddenOnlineUser.canShowPresenceTo(isContactOrFollowing = true))
        assertEquals(
            "🇨🇦 Canada",
            PrivacyAwareLastSeenFormatter.formatPresenceSubtitle(hiddenOnlineUser, AppLanguage.ENGLISH)
        )

        val followersOnlyUser = hiddenOnlineUser.copy(
            isOnline = false,
            showOnlineStatus = true,
            lastSeenVisibility = "contacts",
            isFollowing = false,
            lastSeenAtMs = System.currentTimeMillis() - 120_000L
        )
        assertFalse(followersOnlyUser.canShowPresenceTo(isContactOrFollowing = false))
        assertTrue(followersOnlyUser.canShowPresenceTo(isContactOrFollowing = true))
        assertEquals(
            "🇨🇦 Canada",
            PrivacyAwareLastSeenFormatter.formatPresenceSubtitle(followersOnlyUser, AppLanguage.ENGLISH)
        )

        val onlineFollower = hiddenOnlineUser.copy(
            isOnline = true,
            showOnlineStatus = true,
            lastSeenVisibility = "everyone"
        )
        assertTrue(onlineFollower.canShowPresenceTo(isContactOrFollowing = false))
        assertEquals(
            "online • 🇨🇦 Canada",
            PrivacyAwareLastSeenFormatter.formatPresenceSubtitle(onlineFollower, AppLanguage.ENGLISH)
        )
    }

    @Test
    fun themeStudioSection_allowsSwitchingModeTenThemesAndAccents() {
        var latestTheme = AppTheme.PURE_WHITE
        var latestMode = ThemeMode.SYSTEM
        var latestAccent: Long? = null

        composeTestRule.setContent {
            var currentTheme by remember { mutableStateOf(AppTheme.PURE_WHITE) }
            var currentMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
            var currentAccent by remember { mutableStateOf<Long?>(null) }

            VibeRoomTheme(
                appTheme = currentTheme,
                themeMode = currentMode,
                accentColorHex = currentAccent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    ThemeStudioSection(
                        appTheme = currentTheme,
                        themeMode = currentMode,
                        accentColorHex = currentAccent,
                        appLanguage = AppLanguage.ENGLISH,
                        onThemeSelected = {
                            currentTheme = it
                            latestTheme = it
                        },
                        onModeSelected = {
                            currentMode = it
                            latestMode = it
                        },
                        onAccentSelected = {
                            currentAccent = it
                            latestAccent = it
                        }
                    )
                }
            }
        }

        // All 3 theme modes exist and switch
        composeTestRule.onNodeWithTag("theme_mode_dark").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(ThemeMode.DARK, latestMode)

        // All 10 theme cards exist and can be selected
        AppTheme.entries.forEach { theme ->
            val tag = "theme_option_${theme.name.lowercase()}"
            composeTestRule.onNodeWithTag(tag).performScrollTo().assertIsDisplayed()
        }

        composeTestRule.onNodeWithTag("theme_option_ocean_blue").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(AppTheme.OCEAN_BLUE, latestTheme)

        // Accent presets exist and update custom accent
        val emeraldPreset = AccentColorPresets.first { it.id == "emerald" }
        composeTestRule.onNodeWithTag("accent_option_emerald").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(emeraldPreset.hex, latestAccent)
    }

    @Test
    fun dateSeparatorChip_rendersTodayAndYesterdayAccurately() {
        val now = System.currentTimeMillis()
        val yesterday = now - 24 * 60 * 60 * 1000L
        val todayLabel = formatChatDateLabel(now, AppLanguage.ENGLISH)
        val yesterdayLabel = formatChatDateLabel(yesterday, AppLanguage.ENGLISH)

        assertEquals(AppStrings.t("date_today", AppLanguage.ENGLISH), todayLabel)
        assertEquals(AppStrings.t("date_yesterday", AppLanguage.ENGLISH), yesterdayLabel)

        composeTestRule.setContent {
            VibeRoomTheme {
                DateSeparatorChip(label = todayLabel)
            }
        }
        composeTestRule.onNodeWithText("Today").assertIsDisplayed()
    }
}
