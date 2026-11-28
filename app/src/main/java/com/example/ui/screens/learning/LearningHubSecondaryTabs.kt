package com.example.ui.screens.learning

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.FirestoreVocabItem
import com.example.model.AiTaskState
import com.example.model.LanguageExercise
import com.example.model.TranslationResult
import com.example.ui.components.InterestChip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
internal fun DailyGoalBanner(
    dailyDone: Int,
    dailyTarget: Int,
    streakDays: Int,
    appLanguage: AppLanguage,
    onAdjustTarget: (Int) -> Unit
) {
    val progress = (dailyDone.toFloat() / dailyTarget.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("daily_goal_banner")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.t("daily_goal_title", appLanguage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$dailyDone / $dailyTarget",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalFireDepartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$streakDays ${AppStrings.t("study_streak_label", appLanguage)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    OutlinedButton(
                        onClick = { onAdjustTarget(-1) },
                        enabled = dailyTarget > 1,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) { Text("-") }

                    Spacer(modifier = Modifier.width(4.dp))

                    OutlinedButton(
                        onClick = { onAdjustTarget(1) },
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) { Text("+") }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
            )
        }
    }
}

@Composable
internal fun TranslatorTab(
    targetLanguage: String,
    availableLanguages: List<String>,
    translationState: AiTaskState<TranslationResult>,
    history: List<TranslationResult>,
    appLanguage: AppLanguage,
    isImeOpen: Boolean = false,
    onTranslate: (text: String, sourceHint: String, targetLang: String) -> Unit,
    onCopyText: (String) -> Unit,
    onSavePhrase: (TranslationResult) -> Unit
) {
    var sourceHint by remember { mutableStateOf("Auto-detect") }
    var targetLang by remember(targetLanguage) { mutableStateOf(targetLanguage) }
    var textInput by remember { mutableStateOf("") }
    var showSourceMenu by remember { mutableStateOf(false) }
    var showTargetMenu by remember { mutableStateOf(false) }
    val sourceOptions = remember(availableLanguages) {
        (listOf("Auto-detect") + availableLanguages).distinct()
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) 0.7f else 0.35f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showSourceMenu = true }
                                    .testTag("translator_source_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = sourceHint,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showSourceMenu,
                                onDismissRequest = { showSourceMenu = false }
                            ) {
                                sourceOptions.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            sourceHint = lang
                                            showSourceMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = {
                                if (!sourceHint.equals("Auto-detect", ignoreCase = true) && !sourceHint.equals(targetLang, ignoreCase = true)) {
                                    val prevTarget = targetLang
                                    targetLang = sourceHint
                                    sourceHint = prevTarget
                                } else {
                                    val nextSource = targetLang
                                    val nextTarget = availableLanguages.firstOrNull { !it.equals(nextSource, ignoreCase = true) }
                                        ?: if (nextSource.equals("English", ignoreCase = true)) "Persian (Dari)" else "English"
                                    sourceHint = nextSource
                                    targetLang = nextTarget
                                }
                            },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SwapHoriz,
                                contentDescription = AppStrings.t("swap_languages", appLanguage)
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showTargetMenu = true }
                                    .testTag("translator_target_selector")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = targetLang,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = showTargetMenu,
                                onDismissRequest = { showTargetMenu = false }
                            ) {
                                availableLanguages.forEach { lang ->
                                    DropdownMenuItem(
                                        text = { Text(lang) },
                                        onClick = {
                                            targetLang = lang
                                            showTargetMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { Text("Enter text or phrase to translate...") },
                        minLines = if (isImeOpen) 2 else 3,
                        maxLines = if (isImeOpen) 3 else 5,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("translator_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onTranslate(textInput, sourceHint, targetLang) },
                        enabled = textInput.isNotBlank() && translationState !is AiTaskState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("translator_submit_button")
                    ) {
                        if (translationState is AiTaskState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(AppStrings.t("translating_label", appLanguage))
                        } else {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = AppStrings.t("translate_label", appLanguage),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        when (translationState) {
            is AiTaskState.Error -> {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = translationState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            if (textInput.isNotBlank()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                TextButton(onClick = { onTranslate(textInput, sourceHint, targetLang) }) {
                                    Text(
                                        text = AppStrings.t("retry_label", appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
            is AiTaskState.Success -> {
                val res = translationState.data
                item {
                    TranslationResultCard(
                        result = res,
                        appLanguage = appLanguage,
                        onCopy = onCopyText,
                        onSavePhrase = { onSavePhrase(res) }
                    )
                }
            }
            else -> Unit
        }

        if (history.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Recent Translations (${history.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            items(history.take(10), key = { "${it.timestampMs}_${it.sourceText.hashCode()}" }) { item ->
                TranslationResultCard(
                    result = item,
                    appLanguage = appLanguage,
                    onCopy = onCopyText,
                    onSavePhrase = { onSavePhrase(item) }
                )
            }
        }
    }
}

@Composable
private fun TranslationResultCard(
    result: TranslationResult,
    appLanguage: AppLanguage,
    onCopy: (String) -> Unit,
    onSavePhrase: () -> Unit
) {
    val clipboard = LocalClipboardManager.current
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${result.detectedSourceLanguage} → ${result.targetLanguage}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            clipboard.setText(AnnotatedString(result.translatedText))
                            onCopy(result.translatedText)
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = AppStrings.t("copy_label", appLanguage),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    OutlinedButton(
                        onClick = onSavePhrase,
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("save_phrase_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BookmarkAdd,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
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
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = result.translatedText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (result.pronunciationNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "🗣️ ${result.pronunciationNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (result.usefulPhrase.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "💡 ${result.usefulPhrase}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
internal fun FlashcardsAndQuizTab(
    targetLanguage: String,
    dailyDone: Int,
    dailyTarget: Int,
    streakDays: Int,
    exerciseState: AiTaskState<LanguageExercise>,
    vocabularyList: List<FirestoreVocabItem>,
    appLanguage: AppLanguage,
    onAdjustDailyTarget: (Int) -> Unit,
    onGenerateQuiz: (topic: String) -> Unit,
    onQuizAnsweredCorrectly: () -> Unit,
    onAdvanceMastery: (FirestoreVocabItem) -> Unit,
    onDeleteVocab: (String) -> Unit,
    onAddVocab: (word: String, translation: String, example: String) -> Unit
) {
    val quizTopics = remember {
        listOf("Daily Conversation", "Travel & Airport", "Food & Dining", "Work & Tech", "Grammar & Idioms")
    }
    var selectedTopic by remember { mutableStateOf(quizTopics.first()) }
    var selectedQuizOption by remember(exerciseState) { mutableStateOf<Int?>(null) }
    var showAddVocabDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DailyGoalBanner(
                dailyDone = dailyDone,
                dailyTarget = dailyTarget,
                streakDays = streakDays,
                appLanguage = appLanguage,
                onAdjustTarget = onAdjustDailyTarget
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(
                        alpha = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) 0.7f else 0.35f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Quiz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$targetLanguage Practice Quiz",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = AppStrings.t("quiz_topic_label", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quizTopics.forEach { topic ->
                            InterestChip(
                                text = topic,
                                isSelected = selectedTopic == topic,
                                onClick = { selectedTopic = topic }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onGenerateQuiz(selectedTopic) },
                        enabled = exerciseState !is AiTaskState.Loading,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("practice_quiz_btn")
                    ) {
                        if (exerciseState is AiTaskState.Loading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = AppStrings.t("practice_quiz_btn", appLanguage),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    when (exerciseState) {
                        is AiTaskState.Error -> {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = exerciseState.message,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                        is AiTaskState.Success -> {
                            val ex = exerciseState.data
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = ex.question,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            ex.options.forEachIndexed { idx, opt ->
                                val isChosen = selectedQuizOption == idx
                                val isCorrect = idx == ex.correctIndex
                                val bgColor = when {
                                    selectedQuizOption == null -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    isCorrect -> MaterialTheme.colorScheme.primaryContainer
                                    isChosen -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = bgColor,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .clickable(enabled = selectedQuizOption == null) {
                                            selectedQuizOption = idx
                                            if (idx == ex.correctIndex) {
                                                onQuizAnsweredCorrectly()
                                            }
                                        }
                                ) {
                                    Text(
                                        text = "${idx + 1}. $opt",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isChosen || (selectedQuizOption != null && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                            if (selectedQuizOption != null) {
                                val answeredCorrect = selectedQuizOption == ex.correctIndex
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (answeredCorrect) "✓ Correct!" else "✗ Answer: ${ex.options.getOrNull(ex.correctIndex).orEmpty()}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (answeredCorrect) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = ex.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (ex.culturalTip.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🌍 ${ex.culturalTip}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { onGenerateQuiz(selectedTopic) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .testTag("next_quiz_question_btn")
                                ) {
                                    Text(
                                        text = AppStrings.t("next_question", appLanguage),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        else -> Unit
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${AppStrings.t("tab_flashcards", appLanguage)} (${vocabularyList.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                OutlinedButton(
                    onClick = { showAddVocabDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .heightIn(min = 44.dp)
                        .testTag("add_vocab_button")
                ) {
                    Icon(imageVector = Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Word")
                }
            }
        }

        if (vocabularyList.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("📚", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Save translated phrases or add vocabulary cards to build your personal flashcard deck.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(vocabularyList, key = { it.id }) { card ->
                var isFlipped by remember(card.id) { mutableStateOf(false) }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(
                            alpha = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) 0.7f else 0.35f
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFlipped = !isFlipped }
                        .testTag("vocab_card_${card.id}")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = card.language,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    modifier = Modifier.clickable { onAdvanceMastery(card) }
                                ) {
                                    Text(
                                        text = "★ ${card.masteryLevel}/5",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteVocab(card.id) },
                                    modifier = Modifier.size(48.dp)
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isFlipped) card.translation else card.wordOrPhrase,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (isFlipped) card.wordOrPhrase else "Tap card to reveal translation",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isFlipped && card.exampleSentence.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = card.exampleSentence,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddVocabDialog) {
        AddVocabDialog(
            targetLanguage = targetLanguage,
            appLanguage = appLanguage,
            onDismiss = { showAddVocabDialog = false },
            onSave = { word, trans, ex ->
                onAddVocab(word, trans, ex)
                showAddVocabDialog = false
            }
        )
    }
}

@Composable
private fun AddVocabDialog(
    targetLanguage: String,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (word: String, translation: String, example: String) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var translation by remember { mutableStateOf("") }
    var example by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Add $targetLanguage Flashcard",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Word or Phrase") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = translation,
                        onValueChange = { translation = it },
                        label = { Text("Meaning / Translation") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = example,
                        onValueChange = { example = it },
                        label = { Text("Example Sentence (Optional)") },
                        maxLines = 2,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
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
                        onClick = { onSave(word, translation, example) },
                        enabled = word.isNotBlank() && translation.isNotBlank(),
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Text(AppStrings.t("save_changes", appLanguage))
                    }
                }
            }
        }
    }
}
