package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.luminance
import com.example.model.UserProfile
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UserProfileCard(
    user: UserProfile,
    modifier: Modifier = Modifier,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    onCardClick: () -> Unit = {},
    onToggleFollow: () -> Unit = {},
    onWaveClick: (() -> Unit)? = null,
    onMessageClick: (() -> Unit)? = null,
    onEditProfileClick: (() -> Unit)? = null,
    onSaveProfile: ((bio: String, interests: List<String>) -> Unit)? = null,
    onFollowersClick: (() -> Unit)? = null,
    onFollowingClick: (() -> Unit)? = null,
    showEditButton: Boolean = false,
    maxInterests: Int = 5,
    showActionButtons: Boolean = true,
    matchPercent: Int? = null
) {
    var isBioExpanded by remember { mutableStateOf(false) }
    var isEditDialogOpen by remember { mutableStateOf(false) }
    var localBio by remember(user.bio) { mutableStateOf(user.bio) }
    var localInterests by remember(user.interests) { mutableStateOf(user.interests) }
    val avatarBg = Color(user.avatarColorHex)

    val followButtonBg by animateColorAsState(
        targetValue = if (user.isFollowing) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            MaterialTheme.colorScheme.primary
        },
        animationSpec = spring(),
        label = "follow_button_bg"
    )

    val followButtonContentColor by animateColorAsState(
        targetValue = if (user.isFollowing) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onPrimary
        },
        animationSpec = spring(),
        label = "follow_button_content_color"
    )

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 6.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 7.dp)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) 0.72f else 0.35f
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable { onCardClick() }
            .testTag("user_profile_card_${user.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                avatarBg.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.75f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.65f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            user = user,
                            size = 56.dp,
                            showOnlineStatus = true
                        )

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = user.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.testTag("user_name_${user.id}")
                                )
                                if (user.isVerified) {
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Icon(
                                        imageVector = Icons.Filled.Verified,
                                        contentDescription = "Verified",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                            }

                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CountryFlagBadge(
                                    flagEmoji = user.flagEmoji,
                                    countryName = user.countryName
                                )

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "⭐ Lv.${user.vibeLevel.coerceAtLeast(1)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                if (matchPercent != null && matchPercent > 0) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.85f),
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "✨ $matchPercent% ${AppStrings.t("vibe_match_label", appLanguage)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                if (user.distanceKm != null) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.NearMe,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.size(11.dp)
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = "${user.distanceKm} km",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    if (showEditButton && (onEditProfileClick != null || onSaveProfile != null)) {
                        FilledTonalButton(
                            onClick = {
                                if (onEditProfileClick != null) {
                                    onEditProfileClick()
                                } else {
                                    isEditDialogOpen = true
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("edit_profile_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = AppStrings.t("edit_profile", appLanguage),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = AppStrings.t("edit_profile", appLanguage),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Button(
                            onClick = onToggleFollow,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = followButtonBg,
                                contentColor = followButtonContentColor
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = if (user.isFollowing) 0.dp else 2.dp
                            ),
                            modifier = Modifier
                                .height(40.dp)
                                .testTag("follow_button_${user.id}")
                        ) {
                            AnimatedContent(
                                targetState = user.isFollowing,
                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                label = "follow_state_anim"
                            ) { isFollowing ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isFollowing) Icons.Filled.Check else Icons.Filled.PersonAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isFollowing) AppStrings.t("following", appLanguage) else AppStrings.t("follow", appLanguage),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                if (localBio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = localBio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f),
                        lineHeight = 20.sp,
                        maxLines = if (isBioExpanded) 10 else 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isBioExpanded = !isBioExpanded }
                            .testTag("user_bio_${user.id}")
                    )
                }

                val learningEntries = remember(user.learningLanguageLevels, user.learningLanguages) {
                    if (user.learningLanguageLevels.isNotEmpty()) {
                        user.learningLanguageLevels.entries.map { it.key to it.value }
                    } else {
                        user.learningLanguages.map { it to null }
                    }
                }
                if (learningEntries.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        learningEntries.take(3).forEach { (langName, rank) ->
                            LearningLanguageRankBadge(
                                language = langName,
                                rank = rank,
                                appLanguage = appLanguage
                            )
                        }
                    }
                }

                if (localInterests.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_interests_${user.id}")
                    ) {
                        localInterests.take(maxInterests).forEach { interest ->
                            InterestChip(text = interest)
                        }
                        if (localInterests.size > maxInterests) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            ) {
                                Text(
                                    text = "+${localInterests.size - maxInterests}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("user_credibility_stats_${user.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SocialStatItem(
                            count = user.followersCount,
                            label = AppStrings.t("followers", appLanguage),
                            testTag = "followers_count_${user.id}",
                            onClick = onFollowersClick,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        )
                        SocialStatItem(
                            count = user.followingCount,
                            label = AppStrings.t("following", appLanguage),
                            testTag = "following_count_${user.id}",
                            onClick = onFollowingClick,
                            modifier = Modifier.weight(1f)
                        )
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(26.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        )
                        SocialStatItem(
                            count = user.roomsHostedCount,
                            label = AppStrings.t("rooms_hosted", appLanguage),
                            testTag = "rooms_hosted_count_${user.id}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                if (showActionButtons) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val onlineAccent = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                                Color(0xFF15803D)
                            } else {
                                Color(0xFF34D399)
                            }
                            if (user.isOnline && user.showOnlineStatus) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(onlineAccent)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.t("online_status", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = onlineAccent,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Text(
                                    text = AppStrings.t("offline_status", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onWaveClick != null) {
                                FilledTonalButton(
                                    onClick = onWaveClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    colors = ButtonDefaults.filledTonalButtonColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("wave_button_${user.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.WavingHand,
                                        contentDescription = AppStrings.t("wave", appLanguage),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = AppStrings.t("wave", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (onMessageClick != null) {
                                OutlinedButton(
                                    onClick = onMessageClick,
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("message_button_${user.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = AppStrings.t("chat", appLanguage),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = AppStrings.t("chat", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isEditDialogOpen && showEditButton) {
        EditBioAndInterestsDialog(
            currentBio = localBio,
            currentInterests = localInterests,
            onDismiss = { isEditDialogOpen = false },
            onSave = { updatedBio, updatedInterests ->
                localBio = updatedBio
                localInterests = updatedInterests
                isEditDialogOpen = false
                onSaveProfile?.invoke(updatedBio, updatedInterests)
            }
        )
    }
}

@Composable
fun SocialStatItem(
    count: Int,
    label: String,
    testTag: String,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 4.dp)
            .testTag(testTag)
    ) {
        Text(
            text = formatStatCount(count),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = if (onClick != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun formatStatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format(java.util.Locale.US, "%.1fM", count / 1_000_000f)
        count >= 1_000 -> {
            val formatted = String.format(java.util.Locale.US, "%.1fK", count / 1_000f)
            if (formatted.endsWith(".0K")) "${count / 1_000}K" else formatted
        }
        else -> count.toString()
    }
}

@Composable
fun EditBioAndInterestsDialog(
    currentBio: String,
    currentInterests: List<String>,
    onDismiss: () -> Unit,
    onSave: (bio: String, interests: List<String>) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        EditBioAndInterestsContent(
            currentBio = currentBio,
            currentInterests = currentInterests,
            onDismiss = onDismiss,
            onSave = onSave
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditBioAndInterestsContent(
    currentBio: String,
    currentInterests: List<String>,
    onDismiss: () -> Unit,
    onSave: (bio: String, interests: List<String>) -> Unit,
    appLanguage: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    var bioText by remember { mutableStateOf(currentBio) }
    var selectedInterestsList by remember(currentInterests) { mutableStateOf(currentInterests) }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .testTag("edit_profile_dialog")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = AppStrings.t("edit_profile", appLanguage),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = AppStrings.t("close", appLanguage),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("short_bio", appLanguage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = bioText,
                onValueChange = { if (it.length <= 500) bioText = it },
                placeholder = { Text("Share what you love, your background, or topics you'd enjoy discussing...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_profile_bio_input"),
                shape = RoundedCornerShape(14.dp),
                minLines = 3,
                maxLines = 5,
                supportingText = {
                    Text(
                        text = "${bioText.length}/500",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("pick_interests_from_list", appLanguage),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            CategorizedInterestsPicker(
                selectedInterests = selectedInterestsList,
                onToggleInterest = { topic ->
                    selectedInterestsList = if (selectedInterestsList.any { it.equals(topic, ignoreCase = true) }) {
                        selectedInterestsList.filterNot { it.equals(topic, ignoreCase = true) }
                    } else {
                        selectedInterestsList + topic
                    }
                },
                appLanguage = appLanguage,
                containerTag = "edit_profile_interests_input"
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(AppStrings.t("cancel", appLanguage))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    onClick = { onSave(bioText.trim(), selectedInterestsList) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text(AppStrings.t("save_changes", appLanguage))
                }
            }
        }
    }
}
