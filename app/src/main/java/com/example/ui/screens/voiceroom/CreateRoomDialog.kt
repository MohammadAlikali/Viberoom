package com.example.ui.screens.voiceroom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.model.RoomBackgroundTheme
import com.example.model.RoomCategory
import com.example.model.VibeCatalogs
import com.example.ui.components.InterestChip
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings

@Composable
fun CreateRoomDialog(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    isCreating: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: RoomCategory, tags: List<String>) -> Unit,
    onCreateRoom: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean) -> Unit)? = null,
    onCreateConfigured: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean, backgroundTheme: String, isStageLocked: Boolean) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        CreateRoomDialogContent(
            appLanguage = appLanguage,
            isCreating = isCreating,
            onDismiss = onDismiss,
            onCreate = onCreate,
            onCreateRoom = onCreateRoom,
            onCreateConfigured = onCreateConfigured
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateRoomDialogContent(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    isCreating: Boolean = false,
    onDismiss: () -> Unit,
    onCreate: (title: String, description: String, category: RoomCategory, tags: List<String>) -> Unit,
    onCreateRoom: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean) -> Unit)? = null,
    onCreateConfigured: ((title: String, description: String, category: RoomCategory, tags: List<String>, isPrivate: Boolean, backgroundTheme: String, isStageLocked: Boolean) -> Unit)? = null
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(RoomCategory.LANGUAGE_EXCHANGE) }
    var selectedTheme by remember { mutableStateOf(RoomBackgroundTheme.AURORA) }
    val selectedTags = remember { mutableStateListOf("Daily Practice") }
    var isPrivate by remember { mutableStateOf(false) }
    var isStageLocked by remember { mutableStateOf(false) }

    val isLightSurface = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLightSurface) 0.7f else 0.35f)
        ),
        tonalElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 560.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("create_room_dialog")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Compact Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 8.dp, top = 12.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = AppStrings.t("create_room_title", appLanguage),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = AppStrings.t("create_room_subtitle", appLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    enabled = !isCreating,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("create_room_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = AppStrings.t("close", appLanguage)
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )

            // Scrollable Form Body (shrinks cleanly when IME opens)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(AppStrings.t("room_title_label", appLanguage)) },
                    placeholder = { Text(AppStrings.t("room_title_hint", appLanguage)) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_room_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(AppStrings.t("description_label", appLanguage)) },
                    placeholder = { Text(AppStrings.t("room_desc_hint", appLanguage)) },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_room_description_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.t("select_category", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomCategory.entries.filterNot { it == RoomCategory.ALL }.forEach { cat ->
                        InterestChip(
                            text = "${cat.iconEmoji} ${AppStrings.roomCategory(cat, appLanguage)}",
                            isSelected = selectedCategory == cat,
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.t("pick_room_tags", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("create_room_tags_picker")
                ) {
                    VibeCatalogs.roomTags.forEach { tag ->
                        val isPicked = tag in selectedTags
                        InterestChip(
                            text = if (isPicked) "✓ #$tag" else "#$tag",
                            isSelected = isPicked,
                            onClick = {
                                if (isPicked) selectedTags.remove(tag) else selectedTags.add(tag)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.t("vr_background_themes", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    RoomBackgroundTheme.entries.forEach { bg ->
                        val label = when (appLanguage) {
                            AppLanguage.DARI_PERSIAN -> bg.titleFa
                            AppLanguage.PASHTO -> bg.titlePs
                            AppLanguage.ENGLISH -> bg.titleEn
                        }
                        FilterChip(
                            selected = selectedTheme == bg,
                            onClick = { selectedTheme = bg },
                            label = { Text("${bg.emoji} $label") },
                            modifier = Modifier
                                .heightIn(min = 44.dp)
                                .testTag("room_theme_${bg.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = AppStrings.t("create_room_settings_section", appLanguage),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isLightSurface) 0.42f else 0.5f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isLightSurface) 0.55f else 0.3f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { isPrivate = !isPrivate },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.t("private_room_toggle", appLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Switch(
                                checked = isPrivate,
                                onCheckedChange = { isPrivate = it },
                                modifier = Modifier.testTag("create_room_private_switch")
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 48.dp)
                                .clickable { isStageLocked = !isStageLocked },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = AppStrings.t("vr_stage_lock", appLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Switch(
                                checked = isStageLocked,
                                onCheckedChange = { isStageLocked = it },
                                modifier = Modifier.testTag("create_room_stage_lock_switch")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )

            // Pinned Bottom Action Bar (Always accessible above keyboard & system navigation bar)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isCreating,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(AppStrings.t("cancel", appLanguage))
                }
                Button(
                    onClick = {
                        if (title.isNotBlank() && !isCreating) {
                            val cleanTitle = title.trim()
                            val cleanDesc = description.trim()
                            val tags = selectedTags.toList()
                            when {
                                onCreateConfigured != null -> onCreateConfigured(
                                    cleanTitle,
                                    cleanDesc,
                                    selectedCategory,
                                    tags,
                                    isPrivate,
                                    selectedTheme.id,
                                    isStageLocked
                                )
                                onCreateRoom != null -> onCreateRoom(cleanTitle, cleanDesc, selectedCategory, tags, isPrivate)
                                else -> onCreate(cleanTitle, cleanDesc, selectedCategory, tags)
                            }
                        }
                    },
                    enabled = title.isNotBlank() && !isCreating,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("submit_create_room")
                ) {
                    if (isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = AppStrings.t("go_live", appLanguage),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
