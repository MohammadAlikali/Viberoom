package com.example.ui.screens.profile

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.BuildConfig
import com.example.model.EntranceStyle
import com.example.model.VibeCatalogs
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: VibeRoomViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val accentColorHex by viewModel.accentColorHex.collectAsStateWithLifecycle()
    val blockedUserIds by viewModel.blockedUserIds.collectAsStateWithLifecycle()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val defaultTranslateLanguage by viewModel.defaultTranslateLanguage.collectAsStateWithLifecycle()
    val autoTranslateChat by viewModel.autoTranslateChat.collectAsStateWithLifecycle()
    val showTranslationNotes by viewModel.showTranslationNotes.collectAsStateWithLifecycle()
    val soundEffectsVolume by viewModel.soundEffectsVolume.collectAsStateWithLifecycle()
    val defaultEntranceStyle by viewModel.defaultEntranceStyle.collectAsStateWithLifecycle()
    val enableGiftEffects by viewModel.enableGiftEffects.collectAsStateWithLifecycle()
    val enableEntranceAnimations by viewModel.enableEntranceAnimations.collectAsStateWithLifecycle()
    val lowPowerVisualMode by viewModel.lowPowerVisualMode.collectAsStateWithLifecycle()

    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var showLocalEditProfileDialog by remember { mutableStateOf(false) }
    var micPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        micPermissionGranted = granted
        if (!granted) {
            viewModel.showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage))
        }
    }

    BackHandler { onBack() }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = AppStrings.t("settings_title", appLanguage),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = AppStrings.t("back", appLanguage)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .testTag("settings_list"),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Account Settings
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_account", appLanguage),
                    icon = Icons.Filled.Person,
                    testTag = "settings_section_account"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            user = user,
                            size = 46.dp,
                            showOnlineStatus = false
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (user.id.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    clipboard?.setPrimaryClip(ClipData.newPlainText("VibeRoom ID", user.id))
                                    viewModel.showInAppNotification(AppStrings.t("copied_notice", appLanguage))
                                }
                                .padding(vertical = 8.dp)
                                .testTag("settings_copy_uid_button"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = AppStrings.t("settings_user_id_label", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = user.id,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Filled.ContentCopy,
                                contentDescription = AppStrings.t("settings_copy_id", appLanguage),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsNavigationRow(
                        icon = Icons.Filled.Edit,
                        title = AppStrings.t("edit_profile", appLanguage),
                        subtitle = "${user.name} • @${user.username}",
                        onClick = { showLocalEditProfileDialog = true },
                        testTag = "settings_edit_profile_row"
                    )
                }
            }

            // 2. Privacy & Security
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_privacy", appLanguage),
                    icon = Icons.Filled.Shield,
                    subtitle = AppStrings.t("privacy_center_summary", appLanguage),
                    testTag = "settings_section_privacy"
                ) {
                    SettingsToggleRow(
                        icon = Icons.Filled.Shield,
                        title = AppStrings.t("global_visibility_title", appLanguage),
                        subtitle = AppStrings.t("global_visibility_desc", appLanguage),
                        isChecked = user.isDiscoveryVisible,
                        onCheckedChange = { enabled ->
                            viewModel.updatePrivacySettings(
                                isDiscoveryVisible = enabled,
                                allowDirectMessages = user.allowDirectMessages
                            )
                        },
                        testTag = "discovery_visibility_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsToggleRow(
                        icon = Icons.Filled.Lock,
                        title = AppStrings.t("dm_allowed_title", appLanguage),
                        subtitle = AppStrings.t("dm_allowed_desc", appLanguage),
                        isChecked = user.allowDirectMessages,
                        onCheckedChange = { enabled ->
                            viewModel.updatePrivacySettings(
                                isDiscoveryVisible = user.isDiscoveryVisible,
                                allowDirectMessages = enabled
                            )
                        },
                        testTag = "direct_messages_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsToggleRow(
                        icon = Icons.Filled.Visibility,
                        title = AppStrings.t("online_privacy_title", appLanguage),
                        subtitle = AppStrings.t("online_privacy_desc", appLanguage),
                        isChecked = user.showOnlineStatus,
                        onCheckedChange = { enabled ->
                            viewModel.updatePrivacySettings(
                                isDiscoveryVisible = user.isDiscoveryVisible,
                                allowDirectMessages = user.allowDirectMessages,
                                showOnlineStatus = enabled,
                                lastSeenVisibility = user.lastSeenVisibility
                            )
                        },
                        testTag = "online_status_privacy_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = AppStrings.t("last_seen_title", appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val lastSeenOptions = listOf(
                            "everyone" to "last_seen_everyone",
                            "contacts" to "last_seen_contacts",
                            "nobody" to "last_seen_nobody"
                        )
                        lastSeenOptions.forEach { (key, labelKey) ->
                            val selected = user.lastSeenVisibility == key
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.updatePrivacySettings(
                                            isDiscoveryVisible = user.isDiscoveryVisible,
                                            allowDirectMessages = user.allowDirectMessages,
                                            showOnlineStatus = user.showOnlineStatus,
                                            lastSeenVisibility = key
                                        )
                                    }
                                    .testTag("last_seen_$key")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                ) {
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

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showBlockedUsersDialog = true }
                            .padding(vertical = 8.dp)
                            .testTag("blocked_users_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Block,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.t("blocked_users_title", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = AppStrings.t("blocked_users_desc", appLanguage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (blockedUserIds.isNotEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "${blockedUserIds.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = AppStrings.t("blocked_users_title", appLanguage),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 3. Notifications
            item {
                val unreadNotificationsCount = remember(notifications) {
                    notifications.count { !it.isRead }
                }
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_notifications", appLanguage),
                    icon = Icons.Filled.Notifications,
                    testTag = "settings_section_notifications"
                ) {
                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = AppStrings.t("notifications_toggle", appLanguage),
                        subtitle = AppStrings.t("notifications_toggle_desc", appLanguage),
                        isChecked = notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        testTag = "notifications_enabled_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsNavigationRow(
                        icon = Icons.Filled.Notifications,
                        title = AppStrings.t("settings_open_notifications", appLanguage),
                        subtitle = if (unreadNotificationsCount > 0) {
                            "$unreadNotificationsCount ${AppStrings.t("filter_unread", appLanguage)}"
                        } else {
                            AppStrings.t("notifications_title", appLanguage)
                        },
                        onClick = viewModel::openNotifications,
                        testTag = "settings_open_notifications_row"
                    )
                }
            }

            // 4. Language & Translation
            item {
                var showTranslateTargetMenu by remember { mutableStateOf(false) }
                val effectiveDefaultTarget = defaultTranslateLanguage.ifBlank {
                    VibeCatalogs.defaultTranslateLanguageFor(appLanguage)
                }

                SettingsGroupCard(
                    title = AppStrings.t("settings_section_language", appLanguage),
                    icon = Icons.Filled.Translate,
                    subtitle = AppStrings.t("translation_settings_desc", appLanguage),
                    testTag = "settings_section_language"
                ) {
                    Text(
                        text = AppStrings.t("select_language", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AppLanguage.entries.forEach { lang ->
                            val isSelected = appLanguage == lang
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setAppLanguage(lang) }
                                    .testTag("language_option_${lang.code}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = lang.nativeName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = lang.code.uppercase(),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("default_translate_target", appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box {
                            OutlinedButton(
                                onClick = { showTranslateTargetMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("default_translate_lang_selector")
                            ) {
                                Text(
                                    text = effectiveDefaultTarget,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Filled.KeyboardArrowDown,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            DropdownMenu(
                                expanded = showTranslateTargetMenu,
                                onDismissRequest = { showTranslateTargetMenu = false }
                            ) {
                                VibeCatalogs.translateTargetLanguages.forEach { langChoice ->
                                    val isChosen = langChoice == effectiveDefaultTarget
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = langChoice,
                                                fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        trailingIcon = if (isChosen) {
                                            {
                                                Icon(
                                                    imageVector = Icons.Filled.Check,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        } else null,
                                        onClick = {
                                            viewModel.setDefaultTranslateLanguage(langChoice)
                                            showTranslateTargetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    SettingsToggleRow(
                        icon = Icons.Filled.Translate,
                        title = AppStrings.t("auto_translate_chat_title", appLanguage),
                        subtitle = AppStrings.t("auto_translate_chat_desc", appLanguage),
                        isChecked = autoTranslateChat,
                        onCheckedChange = viewModel::setAutoTranslateChat,
                        testTag = "auto_translate_chat_toggle"
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsToggleRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = AppStrings.t("show_translation_notes_title", appLanguage),
                        subtitle = AppStrings.t("show_translation_notes_desc", appLanguage),
                        isChecked = showTranslationNotes,
                        onCheckedChange = viewModel::setShowTranslationNotes,
                        testTag = "show_translation_notes_toggle"
                    )
                }
            }

            // 5. Appearance & Theme
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_appearance", appLanguage),
                    icon = Icons.Filled.Palette,
                    testTag = "settings_section_appearance"
                ) {
                    ThemeStudioSection(
                        appTheme = appTheme,
                        themeMode = themeMode,
                        accentColorHex = accentColorHex,
                        appLanguage = appLanguage,
                        onThemeSelected = viewModel::setAppTheme,
                        onModeSelected = viewModel::setThemeMode,
                        onAccentSelected = viewModel::setAccentColorHex
                    )
                }
            }

            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_visual_fx_title", appLanguage),
                    icon = Icons.Filled.AutoAwesome,
                    testTag = "settings_section_visual_fx"
                ) {
                    SettingsToggleRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = AppStrings.t("vr_low_power_mode", appLanguage),
                        subtitle = AppStrings.t("settings_low_power_desc", appLanguage),
                        isChecked = lowPowerVisualMode,
                        onCheckedChange = viewModel::setLowPowerVisualMode,
                        testTag = "low_power_mode_toggle"
                    )

                    SettingsToggleRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = AppStrings.t("vr_disable_gift_fx", appLanguage),
                        subtitle = AppStrings.t("vr_gifts_title", appLanguage),
                        isChecked = enableGiftEffects,
                        onCheckedChange = viewModel::setEnableGiftEffects,
                        testTag = "gift_effects_toggle"
                    )

                    SettingsToggleRow(
                        icon = Icons.Filled.AutoAwesome,
                        title = AppStrings.t("vr_disable_entrance_fx", appLanguage),
                        subtitle = AppStrings.t("vr_entrance_styles", appLanguage),
                        isChecked = enableEntranceAnimations,
                        onCheckedChange = viewModel::setEnableEntranceAnimations,
                        testTag = "entrance_animations_toggle"
                    )
                }
            }

            // 6. Audio & Microphone
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_audio", appLanguage),
                    icon = Icons.Filled.VolumeUp,
                    testTag = "settings_section_audio"
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("vr_sound_volume", appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${(soundEffectsVolume * 100f).roundToInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Slider(
                        value = soundEffectsVolume,
                        onValueChange = viewModel::setSoundEffectsVolume,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sound_effects_volume_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = AppStrings.t("vr_entrance_styles", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EntranceStyle.entries.forEach { style ->
                            val selected = defaultEntranceStyle == style
                            val styleLabel = when (appLanguage) {
                                AppLanguage.ENGLISH -> style.titleEn
                                AppLanguage.DARI_PERSIAN -> style.titleFa
                                AppLanguage.PASHTO -> style.titlePs
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.setDefaultEntranceStyle(style) }
                                    .testTag("entrance_style_option_${style.id}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = "${style.emoji} $styleLabel",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Permissions
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_permissions", appLanguage),
                    icon = Icons.Filled.Mic,
                    testTag = "settings_section_permissions"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (!micPermissionGranted) {
                                    micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                            .padding(vertical = 8.dp)
                            .testTag("settings_mic_permission_row"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Mic,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = AppStrings.t("settings_mic_permission", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (micPermissionGranted) {
                                        AppStrings.t("settings_permission_granted", appLanguage)
                                    } else {
                                        AppStrings.t("settings_permission_needed", appLanguage)
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (micPermissionGranted) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    }
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (micPermissionGranted) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        ) {
                            Text(
                                text = if (micPermissionGranted) {
                                    AppStrings.t("settings_permission_granted", appLanguage)
                                } else {
                                    AppStrings.t("vr_grant_mic_btn", appLanguage)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (micPermissionGranted) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onErrorContainer
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                    SettingsNavigationRow(
                        icon = Icons.Filled.Lock,
                        title = AppStrings.t("settings_system_settings", appLanguage),
                        subtitle = context.packageName,
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            runCatching { context.startActivity(intent) }
                        },
                        testTag = "settings_system_permissions_button"
                    )
                }
            }

            // 8. About VibeRoom
            item {
                SettingsGroupCard(
                    title = AppStrings.t("settings_section_about", appLanguage),
                    icon = Icons.Filled.Info,
                    testTag = "settings_section_about"
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("settings_app_version", appLanguage),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "VibeRoom v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 9. Logout & Danger Zone
            item {
                SettingsGroupCard(
                    title = AppStrings.t("account_actions", appLanguage),
                    icon = Icons.AutoMirrored.Filled.Logout,
                    testTag = "settings_section_logout"
                ) {
                    OutlinedButton(
                        onClick = { viewModel.signOut(context) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("sign_out_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Logout,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.t("sign_out", appLanguage),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showDeleteAccountConfirm = true },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("delete_account_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteForever,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = AppStrings.t("delete_account", appLanguage),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (showDeleteAccountConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountConfirm = false },
            title = { Text(AppStrings.t("delete_account", appLanguage), fontWeight = FontWeight.Bold) },
            text = { Text(AppStrings.t("delete_account_confirm", appLanguage)) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountConfirm = false
                        viewModel.deleteAccount(context)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.testTag("confirm_delete_account_button")
                ) {
                    Text(AppStrings.t("delete_post", appLanguage))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountConfirm = false }) {
                    Text(AppStrings.t("cancel", appLanguage))
                }
            }
        )
    }

    if (showLocalEditProfileDialog) {
        EditProfileDialog(
            user = user,
            appLanguage = appLanguage,
            onDismiss = { showLocalEditProfileDialog = false },
            onSave = { name, username, bio, country, flag, interests, languages, city, nativeLangs, learningLangs, learningLevels, proficiency ->
                viewModel.updateProfile(
                    name = name,
                    bio = bio,
                    country = country,
                    flag = flag,
                    interests = interests,
                    languages = languages,
                    city = city,
                    username = username,
                    nativeLanguages = nativeLangs,
                    learningLanguages = learningLangs,
                    learningLanguageLevels = learningLevels,
                    proficiencyLevel = proficiency
                )
                showLocalEditProfileDialog = false
            }
        )
    }

    if (showBlockedUsersDialog) {
        BlockedUsersDialog(
            blockedIds = blockedUserIds,
            blockedProfiles = viewModel.repository.getBlockedUserProfiles(),
            appLanguage = appLanguage,
            onUnblock = { userId -> viewModel.unblockUser(userId) },
            onDismiss = { showBlockedUsersDialog = false }
        )
    }
}

@Composable
private fun SettingsGroupCard(
    title: String,
    icon: ImageVector,
    subtitle: String? = null,
    testTag: String,
    content: @Composable ColumnScope.() -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isLight) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        } else null,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag(testTag)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SettingsNavigationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
