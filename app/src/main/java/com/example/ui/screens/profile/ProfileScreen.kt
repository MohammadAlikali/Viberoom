package com.example.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.VibeCatalogs
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.AvatarCropDialog
import com.example.ui.components.AvatarFrame
import com.example.ui.components.AvatarStudioBottomSheet
import com.example.ui.components.CountryFlagBadge
import com.example.ui.components.InterestChip
import com.example.ui.components.LearningLanguageRankBadge
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColorHex by viewModel.accentColorHex.collectAsState()
    val showEditDialog by viewModel.showEditProfileDialog.collectAsState()
    val blockedUserIds by viewModel.blockedUserIds.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()
    val defaultTranslateLanguage by viewModel.defaultTranslateLanguage.collectAsState()
    val autoTranslateChat by viewModel.autoTranslateChat.collectAsState()
    val showTranslationNotes by viewModel.showTranslationNotes.collectAsState()
    val dailyTarget by viewModel.dailyGoalTarget.collectAsState()
    val dailyDone by viewModel.dailyExercisesDone.collectAsState()
    val studyStreak by viewModel.studyStreakDays.collectAsState()
    val vocabularyCount by viewModel.vocabularyList.collectAsState()

    var showBlockedUsersDialog by remember { mutableStateOf(false) }
    var showDeleteAccountConfirm by remember { mutableStateOf(false) }
    var showAvatarSheet by remember { mutableStateOf(false) }
    var avatarSheetInitialTab by remember { mutableIntStateOf(0) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Hero Profile Header Card
        item {
            Surface(
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("my_profile", appLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )

                        Button(
                            onClick = viewModel::openEditProfileDialog,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("edit_profile", appLanguage),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Interactive Avatar & Live Frame Studio Trigger
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .clickable {
                                avatarSheetInitialTab = 1
                                showAvatarSheet = true
                            }
                            .testTag("change_avatar_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatar(
                            user = user,
                            size = 92.dp,
                            showOnlineStatus = user.showOnlineStatus
                        )
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CameraAlt,
                                contentDescription = AppStrings.t("change_avatar", appLanguage),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = user.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (user.isVerified) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Verified,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    CountryFlagBadge(
                        flagEmoji = user.flagEmoji,
                        countryName = if (user.cityName.isNotBlank()) "${user.cityName}, ${user.countryName}" else user.countryName
                    )

                    if (user.proficiencyLevel.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = "${AppStrings.t("proficiency_label", appLanguage)}: ${user.proficiencyLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (user.bio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = user.bio,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.openFollowersSheet(user) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("profile_followers_count")
                        ) {
                            Text(
                                text = "${user.followersCount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.t("followers", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.openFollowingSheet(user) }
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .testTag("profile_following_count")
                        ) {
                            Text(
                                text = "${user.followingCount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.t("following", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        )
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.testTag("profile_rooms_hosted_count")
                        ) {
                            Text(
                                text = "${user.roomsHostedCount}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.t("rooms_hosted", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Languages & Interests Section
        if (user.languagesSpoken.isNotEmpty() || user.nativeLanguages.isNotEmpty() || user.learningLanguages.isNotEmpty() || user.interests.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                    } else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        val spokenOrNative = (user.nativeLanguages.map { "🏠 $it" } +
                            user.languagesSpoken.filter { it !in user.nativeLanguages && it !in user.learningLanguages })
                        val learningEntries: List<Pair<String, Int?>> = if (user.learningLanguageLevels.isNotEmpty()) {
                            user.learningLanguageLevels.entries.map { it.key to it.value }
                        } else {
                            user.learningLanguages.map { it to null }
                        }

                        if (spokenOrNative.isNotEmpty() || learningEntries.isNotEmpty()) {
                            Text(
                                text = AppStrings.t("languages_section", appLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                spokenOrNative.forEach { lang ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = lang,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                                learningEntries.forEach { (langName, rank) ->
                                    LearningLanguageRankBadge(
                                        language = langName,
                                        rank = rank,
                                        appLanguage = appLanguage
                                    )
                                }
                            }
                            if (user.interests.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                            }
                        }

                        if (user.interests.isNotEmpty()) {
                            Text(
                                text = AppStrings.t("interests_section", appLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                user.interests.forEach { interest ->
                                    InterestChip(text = interest)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Animated Profile Frames Gallery (>5 Distinct Live Frames)
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("profile_frames_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = AppStrings.t("live_frames_gallery_title", appLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.t("live_frames_gallery_desc", appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(
                            onClick = {
                                avatarSheetInitialTab = 0
                                showAvatarSheet = true
                            },
                            modifier = Modifier.testTag("open_all_frames_button")
                        ) {
                            Text(
                                text = AppStrings.t("all_frames_cta", appLanguage),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AvatarFrame.entries.forEach { frame ->
                            val isSelected = AvatarFrame.fromId(user.avatarFrame) == frame
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.75f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                                },
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                ),
                                modifier = Modifier
                                    .width(88.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.updateAvatarFrame(frame.id) }
                                    .testTag("quick_frame_card_${frame.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    UserAvatar(
                                        user = user.copy(avatarFrame = frame.id),
                                        size = 46.dp,
                                        showOnlineStatus = false
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = AppStrings.t(frame.labelKey, appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Daily Study Goal & Language Studio Summary Card
        item {
            val goalProgress = (dailyDone.toFloat() / dailyTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.52f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clickable { viewModel.openLearningHub() }
                    .testTag("profile_learning_goal_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = AppStrings.t("daily_goal_title", appLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$dailyDone/$dailyTarget • 🔥 $studyStreak ${AppStrings.t("study_streak_label", appLanguage)} • 📚 ${vocabularyCount.size} ${AppStrings.t("tab_flashcards", appLanguage)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = viewModel::openLearningHub,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = AppStrings.t("tab_ai_coach", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { goalProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }

        // Vibe Level, XP & Unlocked Badges Card
        item {
            val xpProgress = VibeCatalogs.xpProgressInLevel(user.vibeXp)
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("profile_gamification_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ ${AppStrings.t("vibe_level_label", appLanguage)} ${user.vibeLevel.coerceAtLeast(1)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${user.vibeXp} ${AppStrings.t("vibe_xp_label", appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    if (user.unlockedBadges.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "🏅 ${AppStrings.t("badges_title", appLanguage)}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            user.unlockedBadges.forEach { badge ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "✓ ${badge.replace("_", " ").replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Theme Studio & App Language
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.t("preferences_title", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ThemeStudioSection(
                        appTheme = appTheme,
                        themeMode = themeMode,
                        accentColorHex = accentColorHex,
                        appLanguage = appLanguage,
                        onThemeSelected = viewModel::setAppTheme,
                        onModeSelected = viewModel::setThemeMode,
                        onAccentSelected = viewModel::setAccentColorHex
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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
                                    modifier = Modifier.padding(vertical = 8.dp),
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
                }
            }
        }

        // Translation Settings Card
        item {
            var showTranslateTargetMenu by remember { mutableStateOf(false) }
            val effectiveDefaultTarget = defaultTranslateLanguage.ifBlank {
                VibeCatalogs.defaultTranslateLanguageFor(appLanguage)
            }
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("profile_translation_settings_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = AppStrings.t("translation_settings_title", appLanguage),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = AppStrings.t("translation_settings_desc", appLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                                modifier = Modifier.testTag("default_translate_lang_selector")
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

                    Spacer(modifier = Modifier.height(8.dp))

                    SettingsToggleRow(
                        icon = Icons.Filled.Translate,
                        title = AppStrings.t("auto_translate_chat_title", appLanguage),
                        subtitle = AppStrings.t("auto_translate_chat_desc", appLanguage),
                        isChecked = autoTranslateChat,
                        onCheckedChange = viewModel::setAutoTranslateChat,
                        testTag = "auto_translate_chat_toggle"
                    )

                    Spacer(modifier = Modifier.height(6.dp))

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
        }

        // Privacy & Safety
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.t("privacy_safety", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = AppStrings.t("privacy_center_summary", appLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(8.dp))

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

                    Spacer(modifier = Modifier.height(10.dp))

                    // Last Seen Visibility Selector
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
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
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)) {
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

                    SettingsToggleRow(
                        icon = Icons.Filled.Notifications,
                        title = AppStrings.t("notifications_toggle", appLanguage),
                        subtitle = AppStrings.t("notifications_toggle_desc", appLanguage),
                        isChecked = notificationsEnabled,
                        onCheckedChange = viewModel::setNotificationsEnabled,
                        testTag = "notifications_enabled_toggle"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

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
                                modifier = Modifier.size(24.dp)
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
        }

        // Account Actions
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                } else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = AppStrings.t("account_actions", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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
        }
    }

    if (showAvatarSheet) {
        AvatarStudioBottomSheet(
            user = user,
            appLanguage = appLanguage,
            initialTab = avatarSheetInitialTab,
            onDismiss = { showAvatarSheet = false },
            onPickDevicePhoto = {
                showAvatarSheet = false
                avatarPickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            onRemovePhoto = {
                viewModel.removeAvatar()
                showAvatarSheet = false
            },
            onSaveCustomAvatarData = { dataUri ->
                viewModel.saveCroppedAvatar(dataUri)
                showAvatarSheet = false
            },
            onSelectFrame = { frameId ->
                viewModel.updateAvatarFrame(frameId)
            }
        )
    }

    pendingCropUri?.let { cropUri ->
        AvatarCropDialog(
            imageUri = cropUri,
            appLanguage = appLanguage,
            onDismiss = { pendingCropUri = null },
            onAvatarCropped = { encodedDataUri ->
                pendingCropUri = null
                viewModel.saveCroppedAvatar(encodedDataUri)
            }
        )
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

    if (showEditDialog) {
        EditProfileDialog(
            user = user,
            appLanguage = appLanguage,
            onDismiss = viewModel::closeEditProfileDialog,
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
                modifier = Modifier.size(24.dp)
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
