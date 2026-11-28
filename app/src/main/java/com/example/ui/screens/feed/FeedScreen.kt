package com.example.ui.screens.feed

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.FirestoreComment
import com.example.data.FirestorePost
import com.example.data.FirestoreStory
import com.example.model.TranslationResult
import com.example.model.UserProfile
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.DataUriOrUrlImage
import com.example.ui.components.UserAvatar
import com.example.ui.components.VideoPlayerBox
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by viewModel.firestorePosts.collectAsStateWithLifecycle()
    val stories by viewModel.stories.collectAsStateWithLifecycle()
    val savedPostIds by viewModel.savedPostIds.collectAsStateWithLifecycle()
    val followingIds by viewModel.followingIds.collectAsStateWithLifecycle()
    val blockedUserIds by viewModel.blockedUserIds.collectAsStateWithLifecycle()
    val inlineTranslations by viewModel.inlineTranslations.collectAsStateWithLifecycle()
    val inlineTranslatingKeys by viewModel.inlineTranslatingKeys.collectAsStateWithLifecycle()
    val inlineTranslationErrors by viewModel.inlineTranslationErrors.collectAsStateWithLifecycle()
    val defaultTranslateLanguage by viewModel.defaultTranslateLanguage.collectAsStateWithLifecycle()
    val showTranslationNotes by viewModel.showTranslationNotes.collectAsStateWithLifecycle()
    val isMediaUploading by viewModel.isMediaUploading.collectAsStateWithLifecycle()
    val isPolishingPost by viewModel.isPolishingPost.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allUsers by viewModel.repository.users.collectAsStateWithLifecycle()
    val currentUid = Firebase.auth.currentUser?.uid ?: currentUser.id

    var selectedFeedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePostDialog by remember { mutableStateOf(false) }
    var showCreateStoryDialog by remember { mutableStateOf(false) }
    var activeStory by remember { mutableStateOf<FirestoreStory?>(null) }
    var activeCommentPost by remember { mutableStateOf<FirestorePost?>(null) }

    val preferredTranslateTarget = remember(defaultTranslateLanguage, appLanguage, currentUser.nativeLanguages) {
        defaultTranslateLanguage.takeIf { it.isNotBlank() }
            ?: currentUser.nativeLanguages.firstOrNull()?.takeIf { it.isNotBlank() }
            ?: when (appLanguage) {
                AppLanguage.DARI_PERSIAN -> "Persian (Dari)"
                AppLanguage.PASHTO -> "Pashto"
                AppLanguage.ENGLISH -> "English"
            }
    }

    val visibleStories = remember(stories, blockedUserIds) {
        stories.filterNot { it.authorId in blockedUserIds }
    }

    val visiblePosts = remember(posts, blockedUserIds, selectedFeedTab, followingIds, savedPostIds, searchQuery, currentUid) {
        posts
            .filterNot { it.authorId in blockedUserIds }
            .filter { post ->
                when (selectedFeedTab) {
                    1 -> post.authorId in followingIds || post.authorId == currentUid
                    2 -> post.id in savedPostIds
                    else -> true
                }
            }
            .filter { post ->
                val q = searchQuery.trim()
                if (q.isEmpty()) true
                else {
                    val tagClean = q.removePrefix("#").lowercase()
                    post.content.contains(q, ignoreCase = true) ||
                        post.authorName.contains(q, ignoreCase = true) ||
                        post.category.contains(q, ignoreCase = true) ||
                        post.hashtags.any { it.contains(tagClean, ignoreCase = true) }
                }
            }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreatePostDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.testTag("create_post_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = AppStrings.t("create_post", appLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 680.dp)
                    .testTag("feed_screen"),
                contentPadding = PaddingValues(bottom = 84.dp)
            ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = AppStrings.t("nav_feed", appLanguage),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = AppStrings.t("feed_subtitle", appLanguage),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 24h Stories Row
            item {
                StoriesRow(
                    stories = visibleStories,
                    currentUid = currentUid,
                    appLanguage = appLanguage,
                    onAddStoryClick = { showCreateStoryDialog = true },
                    onStoryClick = { story -> activeStory = story }
                )
            }

            // Feed Filter Tabs: Global / Following / Saved
            item {
                val tabs = listOf(
                    AppStrings.t("feed_global", appLanguage),
                    AppStrings.t("feed_following", appLanguage),
                    AppStrings.t("feed_saved", appLanguage)
                )
                TabRow(
                    selectedTabIndex = selectedFeedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    tabs.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedFeedTab == idx,
                            onClick = { selectedFeedTab = idx },
                            text = { Text(label, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.testTag("feed_tab_$idx")
                        )
                    }
                }
            }

            // Feed Search / Hashtag Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(AppStrings.t("search_feed_hint", appLanguage)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = AppStrings.t("search_feed_hint", appLanguage),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Filled.Clear, contentDescription = null)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_feed_input")
                )
            }

            if (visiblePosts.isEmpty()) {
                item {
                    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        border = if (isLight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)) else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .testTag("empty_feed_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🌍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = AppStrings.t("no_posts_yet", appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showCreatePostDialog = true },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(AppStrings.t("create_post", appLanguage))
                            }
                        }
                    }
                }
            } else {
                items(
                    items = visiblePosts,
                    key = { it.id },
                    contentType = { "feed_post" }
                ) { post ->
                    val isLiked = post.likedUserIds.contains(currentUid)
                    val isSaved = post.id in savedPostIds
                    val translationKey = "post_${post.id}"
                    val authorProfile = remember(post.authorId, allUsers, currentUser) {
                        if (post.authorId == currentUser.id) currentUser
                        else allUsers.firstOrNull { it.id == post.authorId }
                    }
                    PostCardItem(
                        post = post,
                        authorProfile = authorProfile,
                        isLiked = isLiked,
                        isSaved = isSaved,
                        canDelete = (post.authorId == currentUid),
                        translation = inlineTranslations[translationKey],
                        isTranslating = translationKey in inlineTranslatingKeys,
                        translationError = inlineTranslationErrors[translationKey],
                        showTranslationNotes = showTranslationNotes,
                        onAuthorClick = {
                            viewModel.openUserProfileById(
                                userId = post.authorId,
                                fallbackName = post.authorName,
                                fallbackCountry = post.authorCountry ?: "Global",
                                fallbackFlag = post.authorFlag ?: "🌐"
                            )
                        },
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
                        onHashtagClick = { tag -> searchQuery = "#$tag" },
                        onDeleteClick = { viewModel.deletePost(post.id) },
                        currentUid = currentUid,
                        onVotePoll = { optionIdx -> viewModel.voteOnPoll(post, optionIdx) },
                        appLanguage = appLanguage,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
            }
        }
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

    if (showCreateStoryDialog) {
        CreateStoryDialog(
            appLanguage = appLanguage,
            onDismiss = { showCreateStoryDialog = false },
            onPublish = { text, category, bgHex ->
                viewModel.createStory(text, category, bgHex)
                showCreateStoryDialog = false
            }
        )
    }

    if (activeStory != null) {
        val currentStory = stories.firstOrNull { it.id == activeStory!!.id } ?: activeStory!!
        StoryViewerDialog(
            story = currentStory,
            isAuthor = currentStory.authorId == currentUid,
            appLanguage = appLanguage,
            onViewed = { viewModel.markStoryViewed(currentStory.id) },
            onReact = { emoji -> viewModel.reactToStory(currentStory, emoji) },
            onDelete = {
                viewModel.deleteStory(currentStory.id)
                activeStory = null
            },
            onAuthorClick = {
                activeStory = null
                viewModel.openUserProfileById(
                    userId = currentStory.authorId,
                    fallbackName = currentStory.authorName,
                    fallbackCountry = currentStory.authorCountry ?: "Global",
                    fallbackFlag = currentStory.authorFlag ?: "🌐"
                )
            },
            onDismiss = { activeStory = null }
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
private fun StoriesRow(
    stories: List<FirestoreStory>,
    currentUid: String,
    appLanguage: AppLanguage,
    onAddStoryClick: () -> Unit,
    onStoryClick: (FirestoreStory) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = AppStrings.t("stories_title", appLanguage),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(68.dp)
                    .clickable { onAddStoryClick() }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = AppStrings.t("add_story", appLanguage),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = AppStrings.t("add_story", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            stories.forEach { story ->
                val isViewed = currentUid in story.viewedByIds
                val borderColor = if (isViewed) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.primary
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(68.dp)
                        .clickable { onStoryClick(story) }
                        .testTag("story_circle_${story.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(width = 2.5.dp, color = borderColor, shape = CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(Color(story.backgroundHex)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = story.authorFlag?.takeIf { it.isNotBlank() } ?: story.authorName.take(1).uppercase(),
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.authorName.split(" ").firstOrNull().orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun CreateStoryDialog(
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onPublish: (text: String, category: String, backgroundHex: Long) -> Unit
) {
    var storyText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Daily") }
    val swatches = listOf(0xFF6A1B9AL, 0xFF00695CL, 0xFFC62828L, 0xFF1565C0L, 0xFFEF6C00L)
    var selectedBg by remember { mutableLongStateOf(swatches.first()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("create_story", appLanguage),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                            Icon(imageVector = Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        swatches.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .clickable { selectedBg = hex },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Color(hex))
                                        .border(
                                            width = if (selectedBg == hex) 3.dp else 0.dp,
                                            color = if (selectedBg == hex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = storyText,
                        onValueChange = { if (it.length <= 400) storyText = it },
                        placeholder = { Text(AppStrings.t("story_placeholder", appLanguage)) },
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("story_text_input")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Daily", "Languages", "Culture", "Music", "Travel").forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedCategory == cat) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable { selectedCategory = cat }
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedCategory == cat) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(AppStrings.t("cancel", appLanguage))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onPublish(storyText.trim(), selectedCategory, selectedBg) },
                        enabled = storyText.isNotBlank(),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("publish_story_button")
                    ) {
                        Text(AppStrings.t("send", appLanguage))
                    }
                }
            }
        }
    }
}

@Composable
private fun StoryViewerDialog(
    story: FirestoreStory,
    isAuthor: Boolean,
    appLanguage: AppLanguage,
    onViewed: () -> Unit,
    onReact: (String) -> Unit,
    onDelete: () -> Unit,
    onAuthorClick: () -> Unit = {},
    onDismiss: () -> Unit
) {
    LaunchedEffect(story.id) {
        onViewed()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(story.backgroundHex),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .testTag("story_viewer_dialog")
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onAuthorClick() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag("story_author_${story.id}")
                    ) {
                        Text(
                            text = "${story.authorFlag ?: "🌍"} ${story.authorName}",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isAuthor) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("delete_story_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteOutline,
                                    contentDescription = AppStrings.t("delete_post", appLanguage),
                                    tint = Color.White
                                )
                            }
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = AppStrings.t("close", appLanguage),
                                tint = Color.White
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = story.category,
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = story.text,
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Visibility,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${story.viewedByIds.size} ${AppStrings.t("story_views", appLanguage)}",
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("❤️", "🔥", "👏", "😂", "😍").forEach { emoji ->
                            val count = story.reactions.values.count { it == emoji }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.clickable { onReact(emoji) }
                            ) {
                                Text(
                                    text = if (count > 0) "$emoji $count" else emoji,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostCardItem(
    post: FirestorePost,
    authorProfile: UserProfile? = null,
    isLiked: Boolean,
    isSaved: Boolean = false,
    canDelete: Boolean = false,
    translation: TranslationResult? = null,
    isTranslating: Boolean = false,
    translationError: String? = null,
    showTranslationNotes: Boolean = true,
    onAuthorClick: () -> Unit = {},
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit = {},
    onTranslateClick: () -> Unit = {},
    onSaveTranslatedPhrase: (TranslationResult) -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    currentUid: String = "",
    onVotePoll: (Int) -> Unit = {},
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val timeStr = post.createdAt?.toDate()?.let { timeFormat.format(it) } ?: AppStrings.t("just_now", appLanguage)
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val displayAuthor = remember(authorProfile, post) {
        authorProfile ?: UserProfile(
            id = post.authorId,
            name = post.authorName.ifBlank { "Member" },
            username = post.authorName.filter { it.isLetterOrDigit() }.lowercase().take(16),
            bio = "",
            countryName = post.authorCountry ?: "Global",
            countryCode = "GL",
            flagEmoji = post.authorFlag?.takeIf { it.isNotBlank() } ?: "🌍"
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (isLight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isLight) 1.dp else 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("post_card_${post.id}")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onAuthorClick() }
                        .padding(end = 6.dp, top = 2.dp, bottom = 2.dp)
                        .testTag("post_author_${post.id}")
                ) {
                    UserAvatar(
                        user = displayAuthor,
                        size = 44.dp,
                        showOnlineStatus = false
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            if (!post.authorFlag.isNullOrBlank()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = post.authorFlag)
                            }
                        }
                        Text(
                            text = "${post.authorCountry ?: AppStrings.t("global_label", appLanguage)} • $timeStr",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = post.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("save_post_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = AppStrings.t("feed_saved", appLanguage),
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    if (canDelete) {
                        IconButton(
                            onClick = onDeleteClick,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("delete_post_button_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = AppStrings.t("delete_post", appLanguage),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (post.postType == "qa") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "❓ ${AppStrings.t("post_type_qa", appLanguage)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (post.postType == "poll" && post.pollOptions.size >= 2) {
                Spacer(modifier = Modifier.height(10.dp))
                val totalVotes = post.pollVotes.size
                val myVoteIdx = post.pollVotes[currentUid]
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    post.pollOptions.forEachIndexed { idx, optionText ->
                        val optionVotes = post.pollVotes.values.count { it == idx }
                        val ratio = if (totalVotes > 0) optionVotes.toFloat() / totalVotes.toFloat() else 0f
                        val isSelectedOption = myVoteIdx == idx
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelectedOption) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelectedOption) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onVotePoll(idx) }
                                .testTag("poll_option_${post.id}_$idx")
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isSelectedOption) "✓ $optionText" else optionText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelectedOption) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (totalVotes > 0) {
                                        Text(
                                            text = "${(ratio * 100).toInt()}%",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                if (totalVotes > 0) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                    )
                                }
                            }
                        }
                    }
                    Text(
                        text = "$totalVotes ${AppStrings.t("total_votes_label", appLanguage)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (post.mediaType == "image" && (!post.mediaData.isNullOrBlank() || !post.mediaUrl.isNullOrBlank())) {
                Spacer(modifier = Modifier.height(10.dp))
                DataUriOrUrlImage(
                    mediaData = post.mediaData,
                    mediaUrl = post.mediaUrl,
                    contentDescription = post.content.ifBlank { AppStrings.t("attach_photo", appLanguage) },
                    height = 220.dp
                )
            } else if (post.mediaType == "video" && (!post.mediaData.isNullOrBlank() || !post.mediaUrl.isNullOrBlank())) {
                Spacer(modifier = Modifier.height(10.dp))
                VideoPlayerBox(
                    cacheKey = post.id,
                    mediaData = post.mediaData,
                    mediaUrl = post.mediaUrl,
                    height = 220.dp
                )
            }

            if (post.hashtags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    post.hashtags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.clickable { onHashtagClick(tag) }
                        ) {
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            if (translation != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${translation.detectedSourceLanguage} → ${translation.targetLanguage}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { onSaveTranslatedPhrase(translation) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.BookmarkAdd,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.t("save_phrase", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = translation.translatedText,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        if (showTranslationNotes && translation.pronunciationNote.isNotBlank()) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "🗣️ ${translation.pronunciationNote}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (!translationError.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onTranslateClick() }
                ) {
                    Text(
                        text = AppStrings.t("translation_error_retry", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLiked) Color(0xFFFF5286).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onLikeClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = AppStrings.t("like", appLanguage),
                            tint = if (isLiked) Color(0xFFFF5286) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.likesCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isLiked) Color(0xFFFF5286) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.clickable { onCommentClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ChatBubbleOutline,
                            contentDescription = AppStrings.t("comments", appLanguage),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${post.commentsCount}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable(enabled = !isTranslating) { onTranslateClick() }
                        .testTag("translate_post_button_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = AppStrings.t("translate_label", appLanguage),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isTranslating) AppStrings.t("translating_label", appLanguage)
                            else AppStrings.t("translate_label", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatePostDialog(
    isUploading: Boolean = false,
    isPolishing: Boolean = false,
    onPolishDraft: (suspend (String, String) -> Result<String>)? = null,
    onDismiss: () -> Unit,
    onPublish: (
        content: String,
        category: String,
        imageUri: Uri?,
        videoUri: Uri?,
        externalVideoUrl: String?,
        postType: String,
        pollOptions: List<String>,
        onComplete: (String?) -> Unit
    ) -> Unit,
    appLanguage: AppLanguage
) {
    val scope = rememberCoroutineScope()
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Casual") }
    var postType by remember { mutableStateOf("standard") }
    val pollOptions = remember { mutableStateListOf("", "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showVideoUrlField by remember { mutableStateOf(false) }
    var externalVideoUrl by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    val categories = listOf("Casual", "Music", "Languages", "Culture", "Tech")

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            selectedVideoUri = null
            externalVideoUrl = ""
            errorMsg = null
        }
    }

    val videoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedVideoUri = uri
            selectedImageUri = null
            externalVideoUrl = ""
            errorMsg = null
        }
    }

    val validPollOptions = pollOptions.map { it.trim() }.filter { it.isNotBlank() }
    val isPollValid = postType != "poll" || (content.isNotBlank() && validPollOptions.size >= 2)
    val canPublish = !isUploading && !isPolishing && isPollValid && (
        content.isNotBlank() ||
            selectedImageUri != null ||
            selectedVideoUri != null ||
            externalVideoUrl.isNotBlank()
        )

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = AppStrings.t("create_post", appLanguage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { if (!isUploading) onDismiss() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = AppStrings.t("close", appLanguage)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "standard" to AppStrings.t("post_type_standard", appLanguage),
                        "poll" to AppStrings.t("post_type_poll", appLanguage),
                        "qa" to AppStrings.t("post_type_qa", appLanguage)
                    ).forEach { (typeKey, typeLabel) ->
                        val isSelected = postType == typeKey
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { postType = typeKey }
                                .testTag("post_type_chip_$typeKey")
                        ) {
                            Text(
                                text = typeLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = content,
                    onValueChange = { if (it.length <= 1000) content = it },
                    placeholder = { Text(AppStrings.t("post_placeholder", appLanguage)) },
                    minLines = 3,
                    maxLines = 6,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_post_input")
                )

                if (onPolishDraft != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedButton(
                        onClick = {
                            if (content.isNotBlank() && !isPolishing) {
                                scope.launch {
                                    onPolishDraft(content, selectedCategory)
                                        .onSuccess { polished -> content = polished.take(1000) }
                                        .onFailure { err -> errorMsg = err.localizedMessage }
                                }
                            }
                        },
                        enabled = content.isNotBlank() && !isPolishing && !isUploading,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("ai_polish_draft_button")
                    ) {
                        if (isPolishing) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("ai_polish_draft", appLanguage))
                    }
                }

                if (postType == "poll") {
                    Spacer(modifier = Modifier.height(10.dp))
                    pollOptions.forEachIndexed { idx, optVal ->
                        OutlinedTextField(
                            value = optVal,
                            onValueChange = { if (it.length <= 120) pollOptions[idx] = it },
                            placeholder = { Text("${AppStrings.t("poll_option_hint", appLanguage)} ${idx + 1}") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .testTag("poll_option_input_$idx")
                        )
                    }
                    if (pollOptions.size < 4) {
                        OutlinedButton(
                            onClick = { pollOptions.add("") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(AppStrings.t("add_poll_option", appLanguage))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            imagePicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("attach_post_photo_button")
                    ) {
                        Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("attach_photo", appLanguage))
                    }

                    OutlinedButton(
                        onClick = {
                            videoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("attach_post_video_button")
                    ) {
                        Icon(Icons.Filled.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(AppStrings.t("attach_video", appLanguage))
                    }

                    OutlinedButton(
                        onClick = { showVideoUrlField = !showVideoUrlField },
                        enabled = !isUploading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("toggle_video_url_button")
                    ) {
                        Icon(Icons.Filled.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }

                if (showVideoUrlField) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = externalVideoUrl,
                        onValueChange = {
                            externalVideoUrl = it
                            if (it.isNotBlank()) {
                                selectedImageUri = null
                                selectedVideoUri = null
                            }
                            errorMsg = null
                        },
                        placeholder = { Text(AppStrings.t("video_url_optional", appLanguage)) },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("post_video_url_input")
                    )
                }

                if (selectedImageUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = AppStrings.t("attach_photo", appLanguage),
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(14.dp))
                        )
                        IconButton(
                            onClick = { selectedImageUri = null },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(6.dp)
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), tint = Color.White, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                if (selectedVideoUri != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Videocam, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.t("attach_video", appLanguage),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { selectedVideoUri = null },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = AppStrings.t("close", appLanguage), modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                if (!errorMsg.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMsg!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = AppStrings.t("category_label", appLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val catLabel = when (cat) {
                            "Casual" -> AppStrings.t("cat_casual", appLanguage)
                            "Music" -> AppStrings.t("cat_music", appLanguage)
                            "Languages" -> AppStrings.t("cat_language_exchange", appLanguage)
                            "Culture" -> AppStrings.t("cat_culture", appLanguage)
                            "Tech" -> AppStrings.t("cat_tech", appLanguage)
                            else -> cat
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedCategory == cat) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                text = catLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedCategory == cat) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (canPublish) {
                                errorMsg = null
                                onPublish(
                                    content.trim(),
                                    selectedCategory,
                                    selectedImageUri,
                                    selectedVideoUri,
                                    externalVideoUrl.trim().takeIf { it.isNotBlank() },
                                    postType,
                                    if (postType == "poll") validPollOptions else emptyList()
                                ) { err ->
                                    if (err == null) {
                                        onDismiss()
                                    } else {
                                        errorMsg = err
                                    }
                                }
                            }
                        },
                        enabled = canPublish,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("publish_post_button")
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(AppStrings.t("send", appLanguage))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    post: FirestorePost,
    viewModel: VibeRoomViewModel,
    onDismiss: () -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val comments by viewModel.observeCommentsForPost(post.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val timeFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        contentWindowInsets = { BottomSheetDefaults.windowInsets }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "${AppStrings.t("comments", appLanguage)} (${maxOf(post.commentsCount.toInt(), comments.size)})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = AppStrings.t("no_comments_yet", appLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp, max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        CommentRowItem(
                            comment = comment,
                            timeLabel = comment.createdAt?.toDate()?.let { timeFormat.format(it) }
                                ?: AppStrings.t("just_now", appLanguage),
                            onAuthorClick = {
                                onDismiss()
                                viewModel.openUserProfileById(
                                    userId = comment.authorId,
                                    fallbackName = comment.authorName,
                                    fallbackCountry = "Global",
                                    fallbackFlag = comment.authorFlag ?: "🌐"
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text(AppStrings.t("write_comment", appLanguage)) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("comment_input")
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            viewModel.addComment(
                                postId = post.id,
                                content = commentText.trim(),
                                postAuthorId = post.authorId
                            )
                            commentText = ""
                        }
                    },
                    modifier = Modifier.testTag("send_comment_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = AppStrings.t("send", appLanguage),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CommentRowItem(
    comment: FirestoreComment,
    timeLabel: String,
    onAuthorClick: () -> Unit = {}
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isLight) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = if (isLight) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onAuthorClick() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("comment_author_${comment.id}")
                ) {
                    Text(
                        text = comment.authorName,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!comment.authorFlag.isNullOrBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = comment.authorFlag)
                    }
                }
                Text(
                    text = timeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
