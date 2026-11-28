package com.example.ui.screens.learning

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AiCoachTurn
import com.example.model.AiTaskState
import com.example.model.VibeCatalogs
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.InterestChip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
fun LanguageLearningHubModal(
    viewModel: VibeRoomViewModel,
    onDismiss: () -> Unit
) {
    BackHandler { onDismiss() }

    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val dailyTarget by viewModel.dailyGoalTarget.collectAsStateWithLifecycle()
    val dailyDone by viewModel.dailyExercisesDone.collectAsStateWithLifecycle()
    val streakDays by viewModel.studyStreakDays.collectAsStateWithLifecycle()
    val defaultTranslateLang by viewModel.defaultTranslateLanguage.collectAsStateWithLifecycle()

    val aiCoachTurns by viewModel.aiCoachTurns.collectAsStateWithLifecycle()
    val aiCoachState by viewModel.aiCoachState.collectAsStateWithLifecycle()
    val translationState by viewModel.translationState.collectAsStateWithLifecycle()
    val translationHistory by viewModel.translationHistory.collectAsStateWithLifecycle()
    val exerciseState by viewModel.exerciseState.collectAsStateWithLifecycle()
    val vocabularyList by viewModel.vocabularyList.collectAsStateWithLifecycle()

    val availableLanguages = remember(currentUser.learningLanguages, defaultTranslateLang) {
        (currentUser.learningLanguages + VibeCatalogs.translateTargetLanguages + listOfNotNull(defaultTranslateLang.takeIf { it.isNotBlank() }))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    var selectedTargetLang by remember(availableLanguages, defaultTranslateLang) {
        mutableStateOf(
            defaultTranslateLang.takeIf { it.isNotBlank() }
                ?: currentUser.learningLanguages.firstOrNull()?.takeIf { it.isNotBlank() }
                ?: availableLanguages.first()
        )
    }
    var selectedTab by remember { mutableIntStateOf(0) }
    var showLanguageMenu by remember { mutableStateOf(false) }
    val isImeOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0

    LaunchedEffect(Unit) {
        viewModel.warmupAiCoach()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("learning_hub_modal"),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 720.dp)
                ) {
                    // Clean Minimal Header with Inline Target Language Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = if (isImeOpen) 4.dp else 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(if (isImeOpen) 32.dp else 36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(if (isImeOpen) 16.dp else 18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = AppStrings.t("learning_hub_title", appLanguage),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                AnimatedVisibility(visible = !isImeOpen) {
                                    Text(
                                        text = AppStrings.t("learning_hub_subtitle", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Inline Target Language Pill
                        Box {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { showLanguageMenu = true }
                                    .testTag("learning_lang_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedTargetLang,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.widthIn(max = 110.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showLanguageMenu,
                                onDismissRequest = { showLanguageMenu = false }
                            ) {
                                availableLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = lang,
                                                fontWeight = if (lang.equals(selectedTargetLang, ignoreCase = true)) {
                                                    FontWeight.Bold
                                                } else {
                                                    FontWeight.Normal
                                                }
                                            )
                                        },
                                        onClick = {
                                            selectedTargetLang = lang
                                            showLanguageMenu = false
                                        },
                                        modifier = Modifier.testTag("learning_lang_chip_$lang")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_learning_hub_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = AppStrings.t("close", appLanguage)
                            )
                        }
                    }

                    val tabs = listOf(
                        AppStrings.t("tab_ai_coach", appLanguage),
                        AppStrings.t("tab_translator", appLanguage),
                        AppStrings.t("tab_flashcards", appLanguage)
                    )
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.background
                    ) {
                        tabs.forEachIndexed { idx, label ->
                            Tab(
                                selected = selectedTab == idx,
                                onClick = { selectedTab = idx },
                                text = {
                                    Text(
                                        text = label,
                                        fontWeight = if (selectedTab == idx) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier
                                    .heightIn(min = 44.dp)
                                    .testTag("learning_tab_$idx")
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (selectedTab) {
                            0 -> AiCoachTab(
                                targetLanguage = selectedTargetLang,
                                turns = aiCoachTurns,
                                state = aiCoachState,
                                appLanguage = appLanguage,
                                isImeOpen = isImeOpen,
                                onClearCoach = viewModel::clearAiCoach,
                                onCancelCoach = viewModel::cancelAiCoach,
                                onCopyResponse = {
                                    viewModel.showInAppNotification(AppStrings.t("copied_notice", appLanguage))
                                },
                                onSendPrompt = { prompt, mode ->
                                    viewModel.sendAiCoachPrompt(prompt, selectedTargetLang, mode)
                                }
                            )
                            1 -> TranslatorTab(
                                targetLanguage = selectedTargetLang,
                                availableLanguages = availableLanguages,
                                translationState = translationState,
                                history = translationHistory,
                                appLanguage = appLanguage,
                                isImeOpen = isImeOpen,
                                onTranslate = { text, src, tgt ->
                                    viewModel.translateText(text, targetLanguage = tgt, sourceLanguageHint = src)
                                },
                                onCopyText = {
                                    viewModel.showInAppNotification(AppStrings.t("copied_notice", appLanguage))
                                },
                                onSavePhrase = { res ->
                                    viewModel.saveVocabCard(
                                        wordOrPhrase = res.sourceText,
                                        translation = res.translatedText,
                                        language = res.targetLanguage,
                                        exampleSentence = res.usefulPhrase
                                    )
                                }
                            )
                            else -> FlashcardsAndQuizTab(
                                targetLanguage = selectedTargetLang,
                                dailyDone = dailyDone,
                                dailyTarget = dailyTarget,
                                streakDays = streakDays,
                                exerciseState = exerciseState,
                                vocabularyList = vocabularyList,
                                appLanguage = appLanguage,
                                onAdjustDailyTarget = { delta ->
                                    viewModel.setDailyGoalTarget(dailyTarget + delta)
                                },
                                onGenerateQuiz = { topic ->
                                    viewModel.generatePracticeExercise(selectedTargetLang, topic)
                                },
                                onQuizAnsweredCorrectly = viewModel::recordExerciseCompleted,
                                onAdvanceMastery = viewModel::advanceVocabMastery,
                                onDeleteVocab = viewModel::deleteVocabCard,
                                onAddVocab = { word, translation, example ->
                                    viewModel.saveVocabCard(word, translation, selectedTargetLang, example)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AiCoachTab(
    targetLanguage: String,
    turns: List<AiCoachTurn>,
    state: AiTaskState<Unit>,
    appLanguage: AppLanguage,
    isImeOpen: Boolean,
    onClearCoach: () -> Unit,
    onCancelCoach: () -> Unit,
    onCopyResponse: () -> Unit,
    onSendPrompt: (prompt: String, mode: String) -> Unit
) {
    val modes = listOf(
        "conversation" to AppStrings.t("coach_mode_conversation", appLanguage),
        "grammar" to AppStrings.t("coach_mode_grammar", appLanguage),
        "pronunciation" to AppStrings.t("coach_mode_pronunciation", appLanguage),
        "culture" to AppStrings.t("coach_mode_culture", appLanguage)
    )
    var selectedMode by remember { mutableStateOf("conversation") }
    var promptInput by remember { mutableStateOf("") }
    var lastSubmittedPrompt by remember { mutableStateOf("") }
    val coachListState = rememberLazyListState()

    val isGenerating = state is AiTaskState.Loading || state is AiTaskState.Streaming
    val showThinkingRow = state is AiTaskState.Loading && turns.lastOrNull()?.isStreaming != true

    // Scroll smoothly only when a new turn or thinking/error row appears — never jump on every token delta.
    LaunchedEffect(turns.size, showThinkingRow, state is AiTaskState.Error) {
        val totalItems = when {
            turns.isEmpty() -> 1
            else -> turns.size + (if (showThinkingRow) 1 else 0) + (if (state is AiTaskState.Error) 1 else 0)
        }
        if (totalItems > 0) {
            coachListState.animateScrollToItem((totalItems - 1).coerceAtLeast(0))
        }
    }

    fun submitCurrentPrompt() {
        val clean = promptInput.trim()
        if (clean.isEmpty() || isGenerating) return
        lastSubmittedPrompt = clean
        promptInput = ""
        onSendPrompt(clean, selectedMode)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val compactVerticalSpace = isImeOpen && maxHeight < 360.dp

        Column(modifier = Modifier.fillMaxSize()) {
            // Mode & New-Chat Control Bar (automatically tightens when IME reduces height)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = if (compactVerticalSpace) 4.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    modes.forEach { (key, label) ->
                        InterestChip(
                            text = label,
                            isSelected = selectedMode == key,
                            onClick = { selectedMode = key },
                            modifier = Modifier.testTag("coach_mode_$key")
                        )
                    }
                }
                if (turns.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onClearCoach,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ai_coach_clear_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.DeleteOutline,
                            contentDescription = AppStrings.t("clear_chat", appLanguage),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Spacious Scrollable Conversation Canvas
            LazyColumn(
                state = coachListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("ai_coach_message_list"),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (turns.isEmpty()) {
                    item(key = "coach_empty_intro") {
                        CoachWelcomeCard(
                            targetLanguage = targetLanguage,
                            selectedMode = selectedMode,
                            isGenerating = isGenerating,
                            onStarterClick = { suggestion ->
                                if (!isGenerating) {
                                    lastSubmittedPrompt = suggestion
                                    onSendPrompt(suggestion, selectedMode)
                                }
                            }
                        )
                    }
                } else {
                    items(turns, key = { it.id }) { turn ->
                        CoachMessageBubble(
                            turn = turn,
                            appLanguage = appLanguage,
                            onCopy = onCopyResponse
                        )
                    }
                }

                if (showThinkingRow) {
                    item(key = "coach_thinking_row") {
                        CoachThinkingIndicator(
                            label = AppStrings.t("coach_thinking", appLanguage)
                        )
                    }
                }

                if (state is AiTaskState.Error) {
                    item(key = "coach_error_card") {
                        CoachErrorCard(
                            message = state.message,
                            retryLabel = AppStrings.t("retry_label", appLanguage),
                            canRetry = lastSubmittedPrompt.isNotBlank() && !isGenerating,
                            onRetry = {
                                if (lastSubmittedPrompt.isNotBlank() && !isGenerating) {
                                    onSendPrompt(lastSubmittedPrompt, selectedMode)
                                }
                            }
                        )
                    }
                }
            }

            // Pinned Bottom Composer (stays cleanly above keyboard & system navigation bar)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_coach_composer")
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )

                    AnimatedVisibility(visible = isGenerating) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.t("coach_thinking", appLanguage),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier
                                    .heightIn(min = 32.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onCancelCoach() }
                                    .testTag("ai_coach_stop_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = AppStrings.t("coach_stop", appLanguage),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }

                    val canSend = promptInput.isNotBlank() && !isGenerating
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        OutlinedTextField(
                            value = promptInput,
                            onValueChange = { promptInput = it },
                            placeholder = {
                                Text(
                                    text = "Ask AI Coach ($targetLanguage)...",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            shape = RoundedCornerShape(22.dp),
                            maxLines = 4,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { submitCurrentPrompt() }),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_coach_input")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { submitCurrentPrompt() },
                            enabled = canSend,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (canSend) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .testTag("ai_coach_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = AppStrings.t("send", appLanguage),
                                tint = if (canSend) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachWelcomeCard(
    targetLanguage: String,
    selectedMode: String,
    isGenerating: Boolean,
    onStarterClick: (String) -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLight) 0.42f else 0.35f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLight) 0.65f else 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$targetLanguage Smart Coach",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (selectedMode) {
                            "grammar" -> "Grammar feedback & natural rewrites"
                            "pronunciation" -> "Phonetics, stress & speaking practice"
                            "culture" -> "Idioms & cultural etiquette"
                            else -> "Natural daily conversation practice"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val starters = listOf(
                "How do I introduce myself politely in $targetLanguage?",
                "Correct this sentence for me in $targetLanguage.",
                "Teach me 3 useful daily expressions in $targetLanguage."
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                starters.forEach { suggestion ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLight) 0.6f else 0.35f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(enabled = !isGenerating) { onStarterClick(suggestion) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachMessageBubble(
    turn: AiCoachTurn,
    appLanguage: AppLanguage,
    onCopy: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(if (turn.isUser) "coach_user_bubble_${turn.id}" else "coach_ai_bubble_${turn.id}"),
        horizontalArrangement = if (turn.isUser) Arrangement.End else Arrangement.Start
    ) {
        val bubbleShape = if (turn.isUser) {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 18.dp, bottomEnd = 4.dp)
        } else {
            RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomStart = 4.dp, bottomEnd = 18.dp)
        }

        Surface(
            shape = bubbleShape,
            color = if (turn.isUser) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLight) 0.55f else 0.7f)
            },
            border = if (!turn.isUser) {
                BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLight) 0.55f else 0.3f)
                )
            } else null,
            modifier = Modifier.widthIn(max = 520.dp).fillMaxWidth(0.88f)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (!turn.isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = AppStrings.t("tab_ai_coach", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!turn.isStreaming && turn.text.isNotBlank()) {
                            IconButton(
                                onClick = {
                                    clipboard.setText(AnnotatedString(turn.text))
                                    onCopy()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.ContentCopy,
                                    contentDescription = AppStrings.t("copy_label", appLanguage),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Text(
                    text = turn.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp),
                    color = if (turn.isUser) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurface
                )

                if (turn.isStreaming) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachThinkingIndicator(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("ai_coach_thinking_indicator"),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CoachErrorCard(
    message: String,
    retryLabel: String,
    canRetry: Boolean,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.errorContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )
            if (canRetry) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onRetry,
                    modifier = Modifier.heightIn(min = 40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = retryLabel,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
