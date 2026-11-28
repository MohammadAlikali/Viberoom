package com.example.ui.screens.discover

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.FirestoreFriendRequest
import com.example.model.UserProfile
import com.example.model.VibeCatalogs
import com.example.ui.NavigationTab
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.InterestChip
import com.example.ui.components.UserAvatar
import com.example.ui.components.UserProfileCard
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val onlyOnlineUsers by viewModel.onlyOnlineUsers.collectAsStateWithLifecycle()
    val users by viewModel.filteredUsers.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val incomingRequests by viewModel.incomingFriendRequests.collectAsStateWithLifecycle()
    val selectedCountryFilter by viewModel.selectedCountryFilter.collectAsStateWithLifecycle()
    val languageMatchOnly by viewModel.languageMatchOnly.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val dailyExercisesDone by viewModel.dailyExercisesDone.collectAsStateWithLifecycle()
    val allUsers by viewModel.repository.users.collectAsStateWithLifecycle()
    val leaderboardUsers = remember(allUsers, currentUser) {
        (listOf(currentUser) + allUsers.filter { it.id != currentUser.id })
            .distinctBy { it.id }
            .sortedByDescending { it.vibeXp }
            .take(5)
    }
    val unreadNotificationsCount = remember(notifications) { notifications.count { !it.isRead } }
    val availableCountries = remember(users, selectedCountryFilter) {
        val dynamicCountries = users.map { it.countryName }.filter {
            it.isNotBlank() && !it.equals("Global", ignoreCase = true)
        }
        (listOf("All") + dynamicCountries + listOf(selectedCountryFilter))
            .filter { it.isNotBlank() }
            .distinct()
    }

    val pendingIncoming = remember(incomingRequests) {
        incomingRequests.filter { it.status == "pending" }
    }

    val categories = remember {
        listOf(
            "All",
            "Languages",
            "Culture",
            "Music",
            "Technology",
            "Travel",
            "Books",
            "Art",
            "Fitness"
        )
    }

    val onlineCount = remember(users) { users.count { it.isOnline } }

    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 680.dp)
                .testTag("discover_screen"),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
        item {
            DiscoverHeader(
                appLanguage = appLanguage,
                unreadNotifications = unreadNotificationsCount,
                onOpenLearningHub = viewModel::openLearningHub,
                onOpenNotifications = viewModel::openNotifications
            )
        }

        item {
            HeroSpotlightCard(
                onlineCount = onlineCount,
                appLanguage = appLanguage,
                onExploreClick = { viewModel.setSelectedCategory("Languages") }
            )
        }

        item {
            GamificationMissionsAndLeaderboardSection(
                currentUser = currentUser,
                dailyExercisesDone = dailyExercisesDone,
                leaderboardUsers = leaderboardUsers,
                appLanguage = appLanguage,
                onPracticeAiClick = viewModel::openLearningHub,
                onShareMomentClick = { viewModel.selectTab(NavigationTab.FEED) },
                onLeaderboardUserClick = viewModel::openUserProfile
            )
        }

        item {
            SearchBarSection(
                query = searchQuery,
                placeholder = AppStrings.t("search_people_hint", appLanguage),
                onQueryChange = viewModel::setSearchQuery
            )
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InterestChip(
                    text = "🟢 ${AppStrings.t("online_only_filter", appLanguage)}",
                    isSelected = onlyOnlineUsers,
                    onClick = { viewModel.setOnlyOnlineUsers(!onlyOnlineUsers) },
                    modifier = Modifier.testTag("online_only_filter")
                )
                InterestChip(
                    text = "✨ ${AppStrings.t("language_match_filter", appLanguage)}",
                    isSelected = languageMatchOnly,
                    onClick = { viewModel.setLanguageMatchOnly(!languageMatchOnly) },
                    modifier = Modifier.testTag("language_match_filter")
                )
                categories.forEach { category ->
                    InterestChip(
                        text = localizedCategoryLabel(category, appLanguage),
                        isSelected = selectedCategory == category,
                        onClick = { viewModel.setSelectedCategory(category) },
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }
        }

        if (availableCountries.size > 1) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    availableCountries.forEach { country ->
                        val label = if (country == "All") {
                            "🌍 ${AppStrings.t("country_filter_all", appLanguage)}"
                        } else {
                            country
                        }
                        InterestChip(
                            text = label,
                            isSelected = selectedCountryFilter.equals(country, ignoreCase = true),
                            onClick = { viewModel.setSelectedCountryFilter(country) },
                            modifier = Modifier.testTag("country_filter_$country")
                        )
                    }
                }
            }
        }

        if (pendingIncoming.isNotEmpty()) {
            item {
                PendingFriendRequestsBanner(
                    requests = pendingIncoming,
                    appLanguage = appLanguage,
                    onAccept = { viewModel.acceptFriendRequest(it) },
                    onDecline = { viewModel.declineFriendRequest(it) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (selectedCategory == "All") {
                            AppStrings.t("nav_worldwide", appLanguage)
                        } else {
                            localizedCategoryLabel(selectedCategory, appLanguage)
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${users.size} ${AppStrings.t("members_discovered", appLanguage)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (users.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = AppStrings.t("no_profiles_found", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = {
                                viewModel.setSearchQuery("")
                                viewModel.setSelectedCategory("All")
                                viewModel.setOnlyOnlineUsers(false)
                                viewModel.setSelectedCountryFilter("All")
                                viewModel.setLanguageMatchOnly(false)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(AppStrings.t("reset_filters", appLanguage))
                        }
                    }
                }
            }
        } else {
            items(
                items = users,
                key = { it.id },
                contentType = { "user_profile_card" }
            ) { user ->
                val isOwn = (user.id == currentUser.id)
                UserProfileCard(
                    user = user,
                    onCardClick = { viewModel.openUserProfile(user) },
                    onWaveClick = { viewModel.waveAtUser(user) },
                    onToggleFollow = { viewModel.toggleFollowUser(user.id) },
                    onMessageClick = { viewModel.startChatWithUser(user) },
                    onFollowersClick = { viewModel.openFollowersSheet(user) },
                    onFollowingClick = { viewModel.openFollowingSheet(user) },
                    showEditButton = isOwn,
                    onEditProfileClick = if (isOwn) { { viewModel.openEditProfileDialog() } } else null,
                    onSaveProfile = if (isOwn) { { bio, interests -> viewModel.updateBioAndInterests(bio, interests) } } else null,
                    appLanguage = appLanguage,
                    matchPercent = if (!isOwn) VibeCatalogs.computeVibeMatchPercent(currentUser, user) else null
                )
            }
        }
        }
    }
}

private fun localizedCategoryLabel(category: String, lang: AppLanguage): String = when (category) {
    "All" -> AppStrings.t("cat_all", lang)
    "Languages" -> AppStrings.t("cat_language_exchange", lang)
    "Culture" -> AppStrings.t("cat_culture", lang)
    "Music" -> AppStrings.t("cat_music", lang)
    "Technology" -> AppStrings.t("cat_tech", lang)
    else -> category
}

@Composable
private fun DiscoverHeader(
    appLanguage: AppLanguage,
    unreadNotifications: Int,
    onOpenLearningHub: () -> Unit,
    onOpenNotifications: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VR",
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 15.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "VibeRoom",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = AppStrings.t("worldwide_subtitle", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .clickable { onOpenLearningHub() }
                    .testTag("open_learning_hub_btn")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = AppStrings.t("learning_hub_title", appLanguage),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("tab_ai_coach", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            IconButton(
                onClick = onOpenNotifications,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("open_notifications_btn")
            ) {
                BadgedBox(
                    badge = {
                        if (unreadNotifications > 0) {
                            Badge { Text("$unreadNotifications") }
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = AppStrings.t("notifications_title", appLanguage)
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSpotlightCard(
    onlineCount: Int,
    appLanguage: AppLanguage,
    onExploreClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("hero_spotlight_card"),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = AppStrings.t("hero_badge", appLanguage),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "$onlineCount ${AppStrings.t("online_now_count", appLanguage)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = AppStrings.t("hero_title", appLanguage),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = AppStrings.t("hero_desc", appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onExploreClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = AppStrings.t("cat_language_exchange", appLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchBarSection(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = placeholder,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                }
            }
        },
        shape = RoundedCornerShape(18.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("search_people_input")
    )
}

@Composable
private fun PendingFriendRequestsBanner(
    requests: List<FirestoreFriendRequest>,
    appLanguage: AppLanguage,
    onAccept: (String) -> Unit,
    onDecline: (String) -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("pending_friend_requests_card")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "${AppStrings.t("friend_requests", appLanguage)} (${requests.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            requests.take(3).forEach { req ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = req.senderName?.takeIf { it.isNotBlank() } ?: "Member ${req.senderId.take(6)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = { onDecline(req.id) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = AppStrings.t("decline", appLanguage),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Button(
                            onClick = { onAccept(req.id) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = AppStrings.t("accept", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GamificationMissionsAndLeaderboardSection(
    currentUser: UserProfile,
    dailyExercisesDone: Int,
    leaderboardUsers: List<UserProfile>,
    appLanguage: AppLanguage,
    onPracticeAiClick: () -> Unit,
    onShareMomentClick: () -> Unit,
    onLeaderboardUserClick: (UserProfile) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLight) 0.7f else 0.35f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("discover_gamification_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 ${AppStrings.t("daily_missions_title", appLanguage)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${AppStrings.t("vibe_level_label", appLanguage)} ${currentUser.vibeLevel} • ${currentUser.vibeXp} XP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { VibeCatalogs.xpProgressInLevel(currentUser.vibeXp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onPracticeAiClick() }
                        .testTag("mission_ai_practice_btn")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if (dailyExercisesDone > 0) "✅ ${AppStrings.t("mission_practice_ai", appLanguage)}"
                            else "⚡ ${AppStrings.t("mission_practice_ai", appLanguage)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+10 XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onShareMomentClick() }
                        .testTag("mission_share_moment_btn")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = if ("first_post" in currentUser.unlockedBadges) "✅ ${AppStrings.t("mission_share_moment", appLanguage)}"
                            else "✨ ${AppStrings.t("mission_share_moment", appLanguage)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+15 XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }

            if (leaderboardUsers.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "🏆 ${AppStrings.t("leaderboard_title", appLanguage)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    leaderboardUsers.forEachIndexed { index, member ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .clickable { onLeaderboardUserClick(member) }
                                .testTag("leaderboard_user_${member.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1} ${member.flagEmoji}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${member.name.split(" ").firstOrNull().orEmpty()} • Lv.${member.vibeLevel} (${member.vibeXp})",
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
