package com.example.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LanguageProficiencyRank
import com.example.model.VibeCatalogs
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

/**
 * Curated dropdown + multi-select chip menu for spoken or native languages.
 * Eliminates manual comma-separated typing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpokenLanguagesMenuPicker(
    title: String,
    selectedLanguages: List<String>,
    onToggleLanguage: (String) -> Unit,
    appLanguage: AppLanguage,
    menuButtonTag: String = "spoken_languages_menu_btn",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag(menuButtonTag)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("add_language_from_menu", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 320.dp)
                ) {
                    VibeCatalogs.languages.forEach { catalogLang ->
                        val isChosen = selectedLanguages.any {
                            it.equals(catalogLang.canonicalName, ignoreCase = true)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${catalogLang.flagEmoji} ${catalogLang.canonicalName} (${catalogLang.nativeName})",
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
                                onToggleLanguage(catalogLang.canonicalName)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (selectedLanguages.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                selectedLanguages.forEach { langName ->
                    val info = VibeCatalogs.findLanguage(langName)
                    val flag = info?.flagEmoji ?: "🗣️"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onToggleLanguage(langName) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$flag $langName",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = AppStrings.t("close", appLanguage),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Curated dropdown menu to pick learning languages AND select one of 5 proficiency ranks (A1..C1-C2) per language.
 */
@Composable
fun LearningLanguagesRankPicker(
    learningLanguageLevels: Map<String, Int>,
    onSetLanguageRank: (language: String, rank: Int) -> Unit,
    onRemoveLanguage: (language: String) -> Unit,
    appLanguage: AppLanguage,
    menuButtonTag: String = "learning_languages_menu_btn",
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = AppStrings.t("learning_languages_label", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = AppStrings.t("learning_languages_ranks_hint", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                OutlinedButton(
                    onClick = { expanded = true },
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 42.dp)
                        .testTag(menuButtonTag)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Translate,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = AppStrings.t("add_language_from_menu", appLanguage),
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.heightIn(max = 340.dp)
                ) {
                    VibeCatalogs.languages.forEach { catalogLang ->
                        val isChosen = learningLanguageLevels.keys.any {
                            it.equals(catalogLang.canonicalName, ignoreCase = true)
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "${catalogLang.flagEmoji} ${catalogLang.canonicalName} (${catalogLang.nativeName})",
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
                                if (isChosen) {
                                    onRemoveLanguage(catalogLang.canonicalName)
                                } else {
                                    onSetLanguageRank(catalogLang.canonicalName, 1)
                                }
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (learningLanguageLevels.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = AppStrings.t("no_languages_added", appLanguage),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                learningLanguageLevels.entries.forEach { (langName, rank) ->
                    val langInfo = VibeCatalogs.findLanguage(langName)
                    val flag = langInfo?.flagEmoji ?: "📖"
                    val currentRank = LanguageProficiencyRank.fromRank(rank)

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                        border = BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("learning_lang_rank_card_$langName")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = "$flag $langName",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            text = AppStrings.t(currentRank.labelKey, appLanguage),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { onRemoveLanguage(langName) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = AppStrings.t("close", appLanguage),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // 5 Clickable Proficiency Rank Buttons (1..5)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LanguageProficiencyRank.entries.forEach { option ->
                                    val isSelected = currentRank == option
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surface,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        border = BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 40.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable { onSetLanguageRank(langName, option.rank) }
                                            .testTag("rank_pill_${langName}_${option.rank}")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "${option.rank}",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                            Text(
                                                text = option.cefr,
                                                fontSize = 9.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Categorized clickable list of curated interests — no manual text typing needed.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorizedInterestsPicker(
    selectedInterests: List<String>,
    onToggleInterest: (String) -> Unit,
    appLanguage: AppLanguage,
    containerTag: String = "edit_profile_interests_input",
    modifier: Modifier = Modifier
) {
    val grouped = remember { VibeCatalogs.interests.groupBy { it.categoryKey } }
    val customExtra = remember(selectedInterests) {
        val catalogSet = VibeCatalogs.interests.map { it.canonicalName.lowercase() }.toSet()
        selectedInterests.filter { it.lowercase() !in catalogSet }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(containerTag),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (customExtra.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                customExtra.forEach { extra ->
                    InterestChip(
                        text = "✓ $extra",
                        isSelected = true,
                        onClick = { onToggleInterest(extra) },
                        modifier = Modifier.testTag("interest_option_$extra")
                    )
                }
            }
        }

        grouped.forEach { (categoryKey, items) ->
            Column {
                Text(
                    text = AppStrings.t(categoryKey, appLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items.forEach { item ->
                        val isSelected = selectedInterests.any {
                            it.equals(item.canonicalName, ignoreCase = true)
                        }
                        InterestChip(
                            text = if (isSelected) "✓ ${item.emoji} ${item.canonicalName}" else "${item.emoji} ${item.canonicalName}",
                            isSelected = isSelected,
                            onClick = { onToggleInterest(item.canonicalName) },
                            modifier = Modifier.testTag("interest_option_${item.canonicalName}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LearningLanguageRankBadge(
    language: String,
    rank: Int?,
    appLanguage: AppLanguage,
    modifier: Modifier = Modifier
) {
    val langInfo = VibeCatalogs.findLanguage(language)
    val flag = langInfo?.flagEmoji ?: "📖"
    val cleanRank = rank?.coerceIn(1, 5)
    val text = if (cleanRank != null) {
        "$flag $language • ${VibeCatalogs.rankStars(cleanRank)} (${LanguageProficiencyRank.fromRank(cleanRank).cefr})"
    } else {
        "$flag $language"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
        modifier = modifier
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}
