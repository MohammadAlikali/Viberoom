package com.example.ui.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.MediaAttachmentHelper

@Composable
fun DataUriOrUrlImage(
    mediaData: String?,
    mediaUrl: String?,
    contentDescription: String,
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    val imageModel = remember(mediaData, mediaUrl) {
        MediaAttachmentHelper.decodeDataUriBytes(mediaData) ?: mediaUrl
    }
    var showZoomDialog by remember { mutableStateOf(false) }

    if (imageModel != null) {
        AsyncImage(
            model = imageModel,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
                .clickable { showZoomDialog = true }
        )

        if (showZoomDialog) {
            Dialog(
                onDismissRequest = { showZoomDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.94f)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = imageModel,
                        contentDescription = contentDescription,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                    IconButton(
                        onClick = { showZoomDialog = false },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VideoPlayerBox(
    cacheKey: String,
    mediaData: String?,
    mediaUrl: String?,
    modifier: Modifier = Modifier,
    height: Dp = 220.dp
) {
    val context = LocalContext.current
    var isStarted by remember(cacheKey) { mutableStateOf(false) }

    val playbackUri: Uri? = remember(cacheKey, mediaData, mediaUrl) {
        when {
            !mediaUrl.isNullOrBlank() -> Uri.parse(mediaUrl)
            !mediaData.isNullOrBlank() -> {
                val file = MediaAttachmentHelper.writeDataUriToCacheFile(
                    context = context,
                    key = cacheKey,
                    mediaData = mediaData,
                    extension = "mp4"
                )
                file?.let { Uri.fromFile(it) }
            }
            else -> null
        }
    }

    if (playbackUri == null) return

    if (!isStarted) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.Black.copy(alpha = 0.85f),
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clickable { isStarted = true }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = "Play video",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    } else {
        val exoPlayer = remember(playbackUri) {
            ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(playbackUri))
                prepare()
                playWhenReady = true
            }
        }
        DisposableEffect(exoPlayer) {
            onDispose {
                exoPlayer.release()
            }
        }

        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = true
                }
            },
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(14.dp))
        )
    }
}
