package com.example.ui.components

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.MediaAttachmentHelper
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import kotlinx.coroutines.launch

@Composable
fun AvatarCropDialog(
    imageUri: Uri,
    appLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onAvatarCropped: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var sourceBitmap by remember(imageUri) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(imageUri) { mutableStateOf(true) }
    var loadError by remember(imageUri) { mutableStateOf<String?>(null) }
    var zoom by remember(imageUri) { mutableFloatStateOf(1.1f) }
    var offsetXFraction by remember(imageUri) { mutableFloatStateOf(0f) }
    var offsetYFraction by remember(imageUri) { mutableFloatStateOf(0f) }
    var previewDataUri by remember(imageUri) { mutableStateOf<String?>(null) }
    var isEncoding by remember { mutableStateOf(false) }

    LaunchedEffect(imageUri) {
        isLoading = true
        loadError = null
        val loaded = MediaAttachmentHelper.loadOrientedBitmap(context, imageUri, maxDim = 1080)
        if (loaded == null) {
            loadError = AppStrings.t("err_load_image", appLanguage)
        } else {
            sourceBitmap = loaded
        }
        isLoading = false
    }

    DisposableEffect(sourceBitmap) {
        val bmp = sourceBitmap
        onDispose {
            if (bmp != null && !bmp.isRecycled) {
                bmp.recycle()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.72f))
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = AppStrings.t("crop_avatar_title", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = AppStrings.t("cancel", appLanguage),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        loadError != null -> {
                            Text(
                                text = loadError.orEmpty(),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(vertical = 32.dp)
                            )
                        }

                        previewDataUri != null -> {
                            val previewBytes = remember(previewDataUri) {
                                MediaAttachmentHelper.decodeDataUriBytes(previewDataUri)
                            }
                            Text(
                                text = AppStrings.t("preview_save", appLanguage),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .size(168.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = previewBytes,
                                    contentDescription = AppStrings.t("crop_avatar_title", appLanguage),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { previewDataUri = null },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(AppStrings.t("back", appLanguage))
                                }
                                Button(
                                    onClick = { onAvatarCropped(previewDataUri!!) },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("confirm_avatar_save_button")
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(AppStrings.t("save", appLanguage))
                                }
                            }
                        }

                        sourceBitmap != null -> {
                            val bmp = sourceBitmap!!
                            val imageBitmap = remember(bmp) { bmp.asImageBitmap() }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF0E1117))
                                    .pointerInput(Unit) {
                                        detectTransformGestures { _, pan, gestureZoom, _ ->
                                            val newZoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                            zoom = newZoom
                                            val divisor = (size.width / 2f).coerceAtLeast(1f)
                                            offsetXFraction = (offsetXFraction + (pan.x / divisor)).coerceIn(-1f, 1f)
                                            offsetYFraction = (offsetYFraction + (pan.y / divisor)).coerceIn(-1f, 1f)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = imageBitmap,
                                    contentDescription = AppStrings.t("crop_avatar_title", appLanguage),
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = zoom
                                            scaleY = zoom
                                            translationX = offsetXFraction * (size.width * 0.25f)
                                            translationY = offsetYFraction * (size.height * 0.25f)
                                        }
                                )

                                // Circular crop overlay mask
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val radius = size.minDimension * 0.42f
                                    val circlePath = Path().apply {
                                        addOval(
                                            Rect(
                                                center = Offset(size.width / 2f, size.height / 2f),
                                                radius = radius
                                            )
                                        )
                                    }
                                    clipPath(circlePath, clipOp = ClipOp.Difference) {
                                        drawRect(Color.Black.copy(alpha = 0.56f))
                                    }
                                    drawCircle(
                                        color = Color.White.copy(alpha = 0.9f),
                                        radius = radius,
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = AppStrings.t("zoom_label", appLanguage),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Slider(
                                    value = zoom,
                                    onValueChange = { zoom = it },
                                    valueRange = 1f..4f,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(AppStrings.t("cancel", appLanguage))
                                }
                                Button(
                                    onClick = {
                                        if (isEncoding) return@Button
                                        isEncoding = true
                                        scope.launch {
                                            val encoded = MediaAttachmentHelper.cropAndEncodeAvatarDataUri(
                                                source = bmp,
                                                zoom = zoom,
                                                offsetXFraction = offsetXFraction,
                                                offsetYFraction = offsetYFraction
                                            )
                                            isEncoding = false
                                            if (encoded != null) {
                                                previewDataUri = encoded
                                            } else {
                                                loadError = AppStrings.t("err_load_image", appLanguage)
                                            }
                                        }
                                    },
                                    enabled = !isEncoding,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("preview_cropped_avatar_button")
                                ) {
                                    if (isEncoding) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Text(AppStrings.t("preview_save", appLanguage))
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
