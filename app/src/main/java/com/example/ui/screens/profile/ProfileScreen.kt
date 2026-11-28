package com.example.ui.screens.profile

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirestorePost
import com.example.model.VibeCatalogs
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.AvatarCropDialog
import com.example.ui.components.AvatarStudioBottomSheet
import com.example.ui.components.CountryFlagBadge
import com.example.ui.components.InterestChip
import com.example.ui.components.LearningLanguageRankBadge
import com.example.ui.components.UserAvatar
import com.example.ui.localization.AppStrings
import com.example.ui.screens.feed.CommentBottomSheet
import com.example.ui.screens.feed.CreatePostDialog
import com.example.ui.screens.feed.PostCardItem
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
    viewModel: VibeRoomViewModel,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val showEditDialog by viewModel.showEditProfileDialog.collectAsStateWithLifecycle()
    val posts by viewModel.firestorePosts.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()
    val inlineTranslations by viewModel.inlineTranslations.collectAsStateWithLifecycle()
    val inlineTranslatingKeys by viewModel.inlineTranslatingKeys.collectAsStateWithLifecycle()
    val inlineTranslationErrors by viewModel.inlineTranslationErrors.collectAsStateWithLifecycle()
    val defaultTranslateLanguage by viewModel.defaultTranslateLanguage.collectAsStateWithLifecycle()
    val showTranslationNotes by viewModel.showTranslationNotes.collectAsStateWithLifecycle()
    val isMediaUploading by viewModel.isMediaUploading.collectAsStateWithLifecycle()
    val isPolishingPost by viewModel.isPolishingPost.collectAsStateWithLifecycle()
    val dailyTarget by viewModel.dailyGoalTarget.collectAsStateWithLifecycle()
    val dailyDone by viewModel.dailyExercisesDone.collectAsStateWithLifecycle()

    val currentUid = Firebase.auth.currentUser?.uid ?: user.id
    val userPosts = remember(posts, user.id, currentUid) {
        posts.filter { it.authorId == user.id || it.authorId == currentUid }
    }
    val preferredTranslateTarget = remember(defaultTranslateLanguage, appLanguage, user.nativeLanguages) {
        defaultTranslateLanguage.takeIf { it.isNotBlank() }
            ?: user.nativeLanguages.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: VibeCatalogs.defaultTranslateLanguageFor(appLanguage)
    }

    var showAvatarSheet by remember { mutableStateOf(false) }
    var avatarSheetInitialTab by remember { mutableIntStateOf(0) }
    var pendingCropUri by remember { mutableStateOf<Uri?>(null) }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var activeCommentPost by remember { mutableStateOf<FirestorePost?>(null) }

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            pendingCropUri = uri
        }
    }

    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .testTag("profile_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Yeetalk-Inspired Top Profile Header
        item {
            Surface(
                color = MaterialTheme.colorScheme.surface,
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
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
                                    MaterialTheme.colorScheme.surface
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    // Compact Top Bar with single Settings Gear & Share actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("my_profile", appLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val shareSummary = buildString {
                                        append("${user.name} (@${user.username})")
                                        if (user.countryName.isNotBlank()) {
                                            append(" • ${user.flagEmoji} ${user.countryName}")
                                        }
                                        if (user.languagesSpoken.isNotEmpty()) {
                                            append("\n${user.languagesSpoken.joinToString(", ")}")
                                        }
                                        if (user.bio.isNotBlank()) {
                                            append("\n${user.bio}")
                                        }
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareSummary)
                                    }
                                    val chooser = Intent.createChooser(
                                        sendIntent,
                                        AppStrings.t("share_profile", appLanguage)
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    runCatching { context.startActivity(chooser) }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("share_profile_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Share,
                                    contentDescription = AppStrings.t("share_profile", appLanguage),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            IconButton(
                                onClick = onOpenSettings,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("profile_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Settings,
                                    contentDescription = AppStrings.t("settings_title", appLanguage),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Identity Row (Avatar + Name + Handle + Location + Online Status)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
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
                                size = 82.dp,
                                showOnlineStatus = user.showOnlineStatus
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(2.dp)
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CameraAlt,
                                    contentDescription = AppStrings.t("change_avatar", appLanguage),
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "@${user.username}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (user.showOnlineStatus && user.isOnline) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF22C55E))
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = AppStrings.t("online_status", appLanguage),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                CountryFlagBadge(
                                    flagEmoji = user.flagEmoji,
                                    countryName = if (user.cityName.isNotBlank()) "${user.cityName}, ${user.countryName}" else user.countryName
                                )

                                if (user.id.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                                clipboard?.setPrimaryClip(ClipData.newPlainText("VibeRoom ID", user.id))
                                                viewModel.showInAppNotification(AppStrings.t("copied_notice", appLanguage))
                                            }
                                            .testTag("profile_user_id_chip")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ID: ${user.id.take(8)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Filled.ContentCopy,
                                                contentDescription = AppStrings.t("settings_copy_id", appLanguage),
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Directly Visible Biography
                    Text(
                        text = user.bio.ifBlank { AppStrings.t("profile_bio_empty", appLanguage) },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (user.bio.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_bio_text")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compact Yeetalk-style Social Stats Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ProfileStatCell(
                            count = "${user.followersCount}",
                            label = AppStrings.t("followers", appLanguage),
                            highlightLabel = true,
                            onClick = { viewModel.openFollowersSheet(user) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_followers_count")
                        )
                        VerticalStatDivider()
                        ProfileStatCell(
                            count = "${user.followingCount}",
                            label = AppStrings.t("following", appLanguage),
                            highlightLabel = true,
                            onClick = { viewModel.openFollowingSheet(user) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_following_count")
                        )
                        VerticalStatDivider()
                        ProfileStatCell(
                            count = "${user.roomsHostedCount}",
                            label = AppStrings.t("rooms_hosted", appLanguage),
                            highlightLabel = false,
                            onClick = null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_rooms_hosted_count")
                        )
                        VerticalStatDivider()
                        ProfileStatCell(
                            count = "${userPosts.size}",
                            label = AppStrings.t("posts_count_label", appLanguage),
                            highlightLabel = false,
                            onClick = null,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("profile_posts_count")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Compact Profile Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = viewModel::openEditProfileDialog,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
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
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        FilledTonalButton(
                            onClick = {
                                avatarSheetInitialTab = 0
                                showAvatarSheet = true
                            },
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("open_all_frames_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("all_frames_cta", appLanguage),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = viewModel::openLearningHub,
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("profile_learning_goal_card")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Lv.${user.vibeLevel.coerceAtLeast(1)} • $dailyDone/$dailyTarget",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Compact Public Profile Information (Languages, Proficiency, Interests, Badges)
        val hasProfileInfo = user.languagesSpoken.isNotEmpty() ||
            user.nativeLanguages.isNotEmpty() ||
            user.learningLanguages.isNotEmpty() ||
            user.interests.isNotEmpty() ||
            user.proficiencyLevel.isNotBlank() ||
            user.unlockedBadges.isNotEmpty()

        if (hasProfileInfo) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = if (isLight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)) else null,
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("profile_info_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val spokenOrNative = (user.nativeLanguages.map { "🏠 $it" } +
                            user.languagesSpoken.filter { it !in user.nativeLanguages && it !in user.learningLanguages })
                        val learningEntries: List<Pair<String, Int?>> = if (user.learningLanguageLevels.isNotEmpty()) {
                            user.learningLanguageLevels.entries.map { it.key to it.value }
                        } else {
                            user.learningLanguages.map { it to null }
                        }

                        if (spokenOrNative.isNotEmpty() || learningEntries.isNotEmpty() || user.proficiencyLevel.isNotBlank()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = AppStrings.t("languages_section", appLanguage),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                if (user.proficiencyLevel.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer
                                    ) {
                                        Text(
                                            text = user.proficiencyLevel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                            if (spokenOrNative.isNotEmpty() || learningEntries.isNotEmpty()) {
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
                            }
                        }

                        if (user.interests.isNotEmpty()) {
                            if (spokenOrNative.isNotEmpty() || learningEntries.isNotEmpty() || user.proficiencyLevel.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            Text(
                                text = AppStrings.t("interests_section", appLanguage),
                                style = MaterialTheme.typography.titleSmall,
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

                        if (user.unlockedBadges.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🏅 ${AppStrings.t("badges_title", appLanguage)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${user.vibeXp} ${AppStrings.t("vibe_xp_label", appLanguage)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
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
        }

        // Moments / Published Posts Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = AppStrings.t("profile_posts_title", appLanguage),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${userPosts.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                FilledTonalButton(
                    onClick = { showCreatePostDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("profile_create_post_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("create_post", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (userPosts.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = if (isLight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("profile_posts_empty")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📝", fontSize = 34.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = AppStrings.t("profile_posts_empty", appLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(
                items = userPosts,
                key = { it.id },
                contentType = { "profile_post" }
            ) { post ->
                val isLiked = post.likedUserIds.contains(currentUid) || post.likedUserIds.contains(user.id)
                val isSaved = post.id in savedPostIds
                val translationKey = "post_${post.id}"
                PostCardItem(
                    post = post,
                    authorProfile = user,
                    isLiked = isLiked,
                    isSaved = isSaved,
                    canDelete = true,
                    translation = inlineTranslations[translationKey],
                    isTranslating = translationKey in inlineTranslatingKeys,
                    translationError = inlineTranslationErrors[translationKey],
                    showTranslationNotes = showTranslationNotes,
                    onAuthorClick = {},
                    onLikeClick = { viewModel.toggleLikePost(post) },
                    onCommentClick = { activeCommentPost = post },
                    onSaveClick = { viewModel.toggleSavePost(post) },
                    onTranslateClick = {
                        viewModel.translateText(
                            text = post.content,
                            targetLanguage = preferredTranslateTarget,
                            inlineItemKey = translationKey
                        )
                    },
                    onSaveTranslatedPhrase = { res ->
                        viewModel.saveVocabCard(
                            wordOrPhrase = res.sourceText,
                            translation = res.translatedText,
                            language = res.targetLanguage,
                            exampleSentence = res.usefulPhrase
                        )
                    },
                    onHashtagClick = {},
                    onDeleteClick = { viewModel.deletePost(post.id) },
                    currentUid = currentUid,
                    onVotePoll = { optionIdx -> viewModel.voteOnPoll(post, optionIdx) },
                    appLanguage = appLanguage,
                    modifier = Modifier
                        .widthIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
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

    if (showCreatePostDialog) {
        CreatePostDialog(
            isUploading = isMediaUploading,
            isPolishing = isPolishingPost,
            onPolishDraft = { draft, category -> viewModel.polishPostDraft(draft, category) },
            onDismiss = { showCreatePostDialog = false },
            onPublish = { content, category, imageUri, videoUri, externalVideoUrl, postType, pollOptions, onComplete ->
                viewModel.createMediaPost(
                    context = context,
                    content = content,
                    category = category,
                    imageUri = imageUri,
                    videoUri = videoUri,
                    externalVideoUrl = externalVideoUrl,
                    postType = postType,
                    pollOptions = pollOptions,
                    onComplete = onComplete
                )
            },
            appLanguage = appLanguage
        )
    }

    if (activeCommentPost != null) {
        CommentBottomSheet(
            post = activeCommentPost!!,
            viewModel = viewModel,
            onDismiss = { activeCommentPost = null }
        )
    }
}

@Composable
private fun ProfileStatCell(
    count: String,
    label: String,
    highlightLabel: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val clickMod = if (onClick != null) {
        Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    } else {
        Modifier
    }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .heightIn(min = 48.dp)
            .then(clickMod)
            .padding(horizontal = 4.dp, vertical = 4.dp)
    ) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (highlightLabel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (highlightLabel) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun VerticalStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(26.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
    )
}
