package com.example.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.LanguageProficiencyRank
import com.example.model.UserProfile
import com.example.model.VibeCatalogs
import com.example.ui.components.CategorizedInterestsPicker
import com.example.ui.components.LearningLanguagesRankPicker
import com.example.ui.components.SpokenLanguagesMenuPicker
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AccentColorPresets
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ThemeStudioSection(
    appTheme: AppTheme,
    themeMode: ThemeMode,
    accentColorHex: Long?,
    appLanguage: AppLanguage,
    onThemeSelected: (AppTheme) -> Unit,
    onModeSelected: (ThemeMode) -> Unit,
    onAccentSelected: (Long?) -> Unit
) {
    var showCustomHueSlider by remember { mutableStateOf(false) }
    var customHue by remember { mutableFloatStateOf(215f) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Light / Dark / System Mode Switcher
        Text(
            text = AppStrings.t("theme_mode_label", appLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val modes = listOf(
                Triple(ThemeMode.LIGHT, "theme_mode_light", Icons.Filled.LightMode),
                Triple(ThemeMode.DARK, "theme_mode_dark", Icons.Filled.DarkMode),
                Triple(ThemeMode.SYSTEM, "theme_mode_system", Icons.Filled.BrightnessAuto)
            )
            modes.forEach { (mode, labelKey, icon) ->
                val selected = themeMode == mode
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onModeSelected(mode) }
                        .testTag("theme_mode_${mode.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = AppStrings.t(labelKey, appLanguage),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 10 Themes Grid (2 columns -> responsive from 320dp to 480dp+)
        Text(
            text = AppStrings.t("select_theme", appLanguage),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))

        val chunkedThemes = AppTheme.entries.chunked(2)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            chunkedThemes.forEach { rowThemes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowThemes.forEach { theme ->
                        val isSelected = appTheme == theme
                        val swatchColor = Color(theme.swatchHex)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            border = if (isSelected) {
                                androidx.compose.foundation.BorderStroke(
                                    width = 1.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onThemeSelected(theme) }
                                .testTag("theme_option_${theme.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(swatchColor)
                                        .border(
                                            1.5.dp,
                                            if (theme.defaultIsDark) Color(0xFF1F2937) else Color.White,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Filled.Check,
                                            contentDescription = null,
                                            tint = if (swatchColor.luminance() > 0.45f) Color(0xFF0F172A) else Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = AppStrings.themeName(theme, appLanguage),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isSelected) {
                                            MaterialTheme.colorScheme.onPrimaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    text = theme.iconEmoji,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                    if (rowThemes.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main Accent Color Swatches + Custom Hue
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = AppStrings.t("accent_color_label", appLanguage),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = { showCustomHueSlider = !showCustomHueSlider },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = AppStrings.t("accent_custom", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccentColorPresets.forEach { preset ->
                val isSelected = accentColorHex == preset.hex
                val swatch = preset.hex?.let { Color(it) } ?: Color(appTheme.swatchHex)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .clickable { onAccentSelected(preset.hex) }
                        .testTag("accent_option_${preset.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                if (preset.hex == null) {
                                    Brush.linearGradient(listOf(swatch, swatch.copy(alpha = 0.55f)))
                                } else {
                                    Brush.linearGradient(listOf(swatch, swatch))
                                }
                            )
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = AppStrings.t(preset.labelKey, appLanguage),
                                tint = if (swatch.luminance() > 0.45f) Color(0xFF0F172A) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = showCustomHueSlider) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                val previewColor = Color.hsl(hue = customHue, saturation = 0.78f, lightness = 0.48f)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Slider(
                        value = customHue,
                        onValueChange = { hue ->
                            customHue = hue
                            val argbLong = Color.hsl(hue = hue, saturation = 0.78f, lightness = 0.48f)
                                .toArgb().toLong() and 0xFFFFFFFFL
                            onAccentSelected(argbLong)
                        },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = previewColor,
                            activeTrackColor = previewColor
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarActionSheet(
    hasCustomAvatar: Boolean,
    appLanguage: AppLanguage,
    onPickPhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = AppStrings.t("profile_photo_title", appLanguage),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable {
                        onDismiss()
                        onPickPhoto()
                    }
                    .testTag("pick_avatar_photo_option")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = AppStrings.t("change_avatar", appLanguage),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            if (hasCustomAvatar) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            onDismiss()
                            onRemovePhoto()
                        }
                        .testTag("remove_avatar_option")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = AppStrings.t("remove_avatar", appLanguage),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EditProfileDialog(
    user: UserProfile,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        username: String,
        bio: String,
        country: String,
        flag: String,
        interests: List<String>,
        languages: List<String>,
        city: String,
        nativeLanguages: List<String>,
        learningLanguages: List<String>,
        learningLanguageLevels: Map<String, Int>,
        proficiencyLevel: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(user.name) }
    var usernameInput by remember { mutableStateOf(user.username) }
    var city by remember { mutableStateOf(user.cityName) }
    var bio by remember { mutableStateOf(user.bio) }
    var selectedInterests by remember(user.interests) { mutableStateOf(user.interests) }
    var selectedSpokenLanguages by remember(user.languagesSpoken) { mutableStateOf(user.languagesSpoken) }
    var selectedNativeLanguages by remember(user.nativeLanguages) { mutableStateOf(user.nativeLanguages) }
    var learningLevels by remember(user.learningLanguages, user.learningLanguageLevels, user.proficiencyLevel) {
        mutableStateOf(
            if (user.learningLanguageLevels.isNotEmpty()) {
                user.learningLanguageLevels
            } else {
                val defaultRank = LanguageProficiencyRank.fromLegacyString(user.proficiencyLevel).rank
                user.learningLanguages.associateWith { defaultRank }
            }
        )
    }

    val cleanedUsername = usernameInput.trim().removePrefix("@").lowercase()
        .filter { it.isLetterOrDigit() || it == '_' }
        .take(30)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag("edit_profile_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Text(
                    text = AppStrings.t("edit_profile", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(AppStrings.t("display_name", appLanguage)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = usernameInput,
                    onValueChange = { input ->
                        usernameInput = input.lowercase().filter { it.isLetterOrDigit() || it == '_' }.take(30)
                    },
                    prefix = { Text("@") },
                    label = { Text(AppStrings.t("username_label", appLanguage)) },
                    supportingText = if (cleanedUsername.length < 2) {
                        { Text(AppStrings.t("username_invalid_hint", appLanguage)) }
                    } else null,
                    isError = cleanedUsername.length < 2,
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_username_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("locked_country_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = user.flagEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = user.countryName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = AppStrings.t("country_locked_note", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = AppStrings.t("country_locked_note", appLanguage),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(AppStrings.t("city_label", appLanguage)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_city_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(AppStrings.t("short_bio", appLanguage)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("edit_profile_bio_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                SpokenLanguagesMenuPicker(
                    title = AppStrings.t("native_languages_menu", appLanguage),
                    selectedLanguages = selectedNativeLanguages,
                    onToggleLanguage = { lang ->
                        selectedNativeLanguages = if (lang in selectedNativeLanguages) {
                            selectedNativeLanguages - lang
                        } else {
                            selectedNativeLanguages + lang
                        }
                    },
                    appLanguage = appLanguage,
                    menuButtonTag = "native_languages_menu_btn"
                )

                Spacer(modifier = Modifier.height(14.dp))

                SpokenLanguagesMenuPicker(
                    title = AppStrings.t("spoken_languages_menu", appLanguage),
                    selectedLanguages = selectedSpokenLanguages,
                    onToggleLanguage = { lang ->
                        selectedSpokenLanguages = if (lang in selectedSpokenLanguages) {
                            selectedSpokenLanguages - lang
                        } else {
                            selectedSpokenLanguages + lang
                        }
                    },
                    appLanguage = appLanguage,
                    menuButtonTag = "spoken_languages_menu_btn"
                )

                Spacer(modifier = Modifier.height(16.dp))

                LearningLanguagesRankPicker(
                    learningLanguageLevels = learningLevels,
                    onSetLanguageRank = { lang, rank ->
                        learningLevels = learningLevels + (lang to rank.coerceIn(1, 5))
                    },
                    onRemoveLanguage = { lang ->
                        learningLevels = learningLevels - lang
                    },
                    appLanguage = appLanguage
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.t("pick_interests_from_list", appLanguage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                CategorizedInterestsPicker(
                    selectedInterests = selectedInterests,
                    onToggleInterest = { topic ->
                        selectedInterests = if (selectedInterests.any { it.equals(topic, ignoreCase = true) }) {
                            selectedInterests.filterNot { it.equals(topic, ignoreCase = true) }
                        } else {
                            selectedInterests + topic
                        }
                    },
                    appLanguage = appLanguage,
                    containerTag = "edit_profile_interests_input"
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text(AppStrings.t("cancel", appLanguage))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val learningList = learningLevels.keys.toList()
                            val combinedLanguages = (selectedNativeLanguages + selectedSpokenLanguages + learningList)
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .distinct()
                            val summaryProficiency = VibeCatalogs.summarizeProficiency(learningLevels, appLanguage)
                            onSave(
                                name.trim().ifEmpty { user.name },
                                cleanedUsername.ifEmpty { user.username },
                                bio,
                                user.countryName,
                                user.flagEmoji,
                                selectedInterests,
                                combinedLanguages,
                                city.trim(),
                                selectedNativeLanguages,
                                learningList,
                                learningLevels,
                                summaryProficiency
                            )
                        },
                        enabled = cleanedUsername.length >= 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_profile_button")
                    ) {
                        Text(AppStrings.t("save_changes", appLanguage))
                    }
                }
            }
        }
    }
}

@Composable
fun BlockedUsersDialog(
    blockedIds: Set<String>,
    blockedProfiles: List<UserProfile>,
    appLanguage: AppLanguage,
    onUnblock: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val profileById = remember(blockedProfiles) { blockedProfiles.associateBy { it.id } }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp)
                .testTag("blocked_users_dialog")
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = AppStrings.t("blocked_users_title", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (blockedIds.isEmpty()) {
                    Text(
                        text = AppStrings.t("no_blocked_users", appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        blockedIds.forEach { blockedId ->
                            val peer = profileById[blockedId]
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = peer?.name ?: "Member ${blockedId.take(6)}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "@${peer?.username ?: blockedId.take(10)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onUnblock(blockedId) },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("unblock_user_button_$blockedId")
                                    ) {
                                        Text(
                                            text = AppStrings.t("unblock", appLanguage),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                        Text(AppStrings.t("close", appLanguage))
                    }
                }
            }
        }
    }
}
