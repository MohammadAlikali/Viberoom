package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.EntranceStyle
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Context.vibeRoomDataStore: DataStore<Preferences> by preferencesDataStore(name = "viberoom_preferences")

data class StoredAppPreferences(
    val appTheme: AppTheme = AppTheme.PURE_WHITE,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColorHex: Long? = null,
    val appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    val isDarkMode: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val dailyGoalTarget: Int = 5,
    val dailyExercisesDone: Int = 0,
    val studyStreakDays: Int = 0,
    val lastStudyDate: String = "",
    val defaultTranslateLanguage: String = "",
    val autoTranslateChat: Boolean = false,
    val showTranslationNotes: Boolean = true,
    val defaultEntranceStyle: String = EntranceStyle.STANDARD.id,
    val soundEffectsVolume: Float = 0.85f,
    val enableGiftEffects: Boolean = true,
    val enableEntranceAnimations: Boolean = true,
    val lowPowerVisualMode: Boolean = false,
    val favoriteSoundIds: Set<String> = setOf("welcome", "applause", "celebration")
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val APP_THEME = stringPreferencesKey("app_theme")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val ACCENT_COLOR_HEX = stringPreferencesKey("accent_color_hex")
        val APP_LANGUAGE = stringPreferencesKey("app_language")
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val DAILY_GOAL_TARGET = intPreferencesKey("daily_goal_target")
        val DAILY_EXERCISES_DONE = intPreferencesKey("daily_exercises_done")
        val STUDY_STREAK_DAYS = intPreferencesKey("study_streak_days")
        val LAST_STUDY_DATE = stringPreferencesKey("last_study_date")
        val DEFAULT_TRANSLATE_LANGUAGE = stringPreferencesKey("default_translate_language")
        val AUTO_TRANSLATE_CHAT = booleanPreferencesKey("auto_translate_chat")
        val SHOW_TRANSLATION_NOTES = booleanPreferencesKey("show_translation_notes")
        val DEFAULT_ENTRANCE_STYLE = stringPreferencesKey("vr_default_entrance_style")
        val SOUND_EFFECTS_VOLUME = floatPreferencesKey("vr_sound_effects_volume")
        val ENABLE_GIFT_EFFECTS = booleanPreferencesKey("vr_enable_gift_effects")
        val ENABLE_ENTRANCE_ANIMATIONS = booleanPreferencesKey("vr_enable_entrance_animations")
        val LOW_POWER_VISUAL_MODE = booleanPreferencesKey("vr_low_power_visual_mode")
        val FAVORITE_SOUND_IDS = stringSetPreferencesKey("vr_favorite_sound_ids")
    }

    private fun todayDateKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    val preferencesFlow: Flow<StoredAppPreferences> = context.applicationContext.vibeRoomDataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val themeName = prefs[Keys.APP_THEME]
            val resolvedTheme = AppTheme.fromStoredName(themeName)

            val savedMode = prefs[Keys.THEME_MODE]
            val legacyDark = prefs[Keys.IS_DARK_MODE]
            val resolvedMode = ThemeMode.entries.firstOrNull { it.name == savedMode || it.id == savedMode }
                ?: when {
                    legacyDark == true -> ThemeMode.DARK
                    legacyDark == false -> ThemeMode.LIGHT
                    else -> ThemeMode.SYSTEM
                }

            val accentRaw = prefs[Keys.ACCENT_COLOR_HEX]
            val resolvedAccent = accentRaw?.toLongOrNull()

            val langCode = prefs[Keys.APP_LANGUAGE]
            val resolvedLanguage = AppLanguage.entries.firstOrNull { it.code == langCode } ?: AppLanguage.DARI_PERSIAN

            val notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true
            val dailyTarget = (prefs[Keys.DAILY_GOAL_TARGET] ?: 5).coerceIn(1, 50)
            val savedDate = prefs[Keys.LAST_STUDY_DATE].orEmpty()
            val today = todayDateKey()
            val exercisesDone = if (savedDate == today) (prefs[Keys.DAILY_EXERCISES_DONE] ?: 0) else 0
            val streak = prefs[Keys.STUDY_STREAK_DAYS] ?: 0

            StoredAppPreferences(
                appTheme = resolvedTheme,
                themeMode = resolvedMode,
                accentColorHex = resolvedAccent,
                appLanguage = resolvedLanguage,
                isDarkMode = (resolvedMode == ThemeMode.DARK),
                notificationsEnabled = notificationsEnabled,
                dailyGoalTarget = dailyTarget,
                dailyExercisesDone = exercisesDone,
                studyStreakDays = streak,
                lastStudyDate = savedDate,
                defaultTranslateLanguage = prefs[Keys.DEFAULT_TRANSLATE_LANGUAGE].orEmpty(),
                autoTranslateChat = prefs[Keys.AUTO_TRANSLATE_CHAT] ?: false,
                showTranslationNotes = prefs[Keys.SHOW_TRANSLATION_NOTES] ?: true,
                defaultEntranceStyle = prefs[Keys.DEFAULT_ENTRANCE_STYLE] ?: EntranceStyle.STANDARD.id,
                soundEffectsVolume = (prefs[Keys.SOUND_EFFECTS_VOLUME] ?: 0.85f).coerceIn(0f, 1f),
                enableGiftEffects = prefs[Keys.ENABLE_GIFT_EFFECTS] ?: true,
                enableEntranceAnimations = prefs[Keys.ENABLE_ENTRANCE_ANIMATIONS] ?: true,
                lowPowerVisualMode = prefs[Keys.LOW_POWER_VISUAL_MODE] ?: false,
                favoriteSoundIds = prefs[Keys.FAVORITE_SOUND_IDS] ?: setOf("welcome", "applause", "celebration")
            )
        }

    suspend fun saveAppTheme(theme: AppTheme) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs[Keys.APP_THEME] = theme.name
        }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
            prefs[Keys.IS_DARK_MODE] = (mode == ThemeMode.DARK)
        }
    }

    suspend fun saveAccentColorHex(hex: Long?) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            if (hex == null) {
                prefs.remove(Keys.ACCENT_COLOR_HEX)
            } else {
                prefs[Keys.ACCENT_COLOR_HEX] = hex.toString()
            }
        }
    }

    suspend fun saveAppLanguage(language: AppLanguage) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs[Keys.APP_LANGUAGE] = language.code
        }
    }

    suspend fun saveDarkMode(enabled: Boolean) {
        saveThemeMode(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
    }

    suspend fun saveNotificationsEnabled(enabled: Boolean) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun saveDailyGoalTarget(target: Int) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs[Keys.DAILY_GOAL_TARGET] = target.coerceIn(1, 50)
        }
    }

    suspend fun saveTranslationSettings(
        defaultTranslateLanguage: String? = null,
        autoTranslateChat: Boolean? = null,
        showTranslationNotes: Boolean? = null
    ) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            if (defaultTranslateLanguage != null) {
                prefs[Keys.DEFAULT_TRANSLATE_LANGUAGE] = defaultTranslateLanguage
            }
            if (autoTranslateChat != null) {
                prefs[Keys.AUTO_TRANSLATE_CHAT] = autoTranslateChat
            }
            if (showTranslationNotes != null) {
                prefs[Keys.SHOW_TRANSLATION_NOTES] = showTranslationNotes
            }
        }
    }

    suspend fun saveVoiceRoomPreferences(
        defaultEntranceStyle: String? = null,
        soundEffectsVolume: Float? = null,
        enableGiftEffects: Boolean? = null,
        enableEntranceAnimations: Boolean? = null,
        lowPowerVisualMode: Boolean? = null,
        favoriteSoundIds: Set<String>? = null
    ) {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            if (defaultEntranceStyle != null) prefs[Keys.DEFAULT_ENTRANCE_STYLE] = defaultEntranceStyle
            if (soundEffectsVolume != null) prefs[Keys.SOUND_EFFECTS_VOLUME] = soundEffectsVolume.coerceIn(0f, 1f)
            if (enableGiftEffects != null) prefs[Keys.ENABLE_GIFT_EFFECTS] = enableGiftEffects
            if (enableEntranceAnimations != null) prefs[Keys.ENABLE_ENTRANCE_ANIMATIONS] = enableEntranceAnimations
            if (lowPowerVisualMode != null) prefs[Keys.LOW_POWER_VISUAL_MODE] = lowPowerVisualMode
            if (favoriteSoundIds != null) prefs[Keys.FAVORITE_SOUND_IDS] = favoriteSoundIds
        }
    }

    suspend fun recordLearningActivity() {
        val today = todayDateKey()
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            val lastDate = prefs[Keys.LAST_STUDY_DATE].orEmpty()
            val prevCount = if (lastDate == today) (prefs[Keys.DAILY_EXERCISES_DONE] ?: 0) else 0
            val prevStreak = prefs[Keys.STUDY_STREAK_DAYS] ?: 0
            val nextStreak = if (lastDate == today) {
                prevStreak.coerceAtLeast(1)
            } else {
                prevStreak + 1
            }
            prefs[Keys.LAST_STUDY_DATE] = today
            prefs[Keys.DAILY_EXERCISES_DONE] = prevCount + 1
            prefs[Keys.STUDY_STREAK_DAYS] = nextStreak
        }
    }

    suspend fun clearUserProgress() {
        context.applicationContext.vibeRoomDataStore.edit { prefs ->
            prefs.remove(Keys.DAILY_EXERCISES_DONE)
            prefs.remove(Keys.STUDY_STREAK_DAYS)
            prefs.remove(Keys.LAST_STUDY_DATE)
        }
    }
}
