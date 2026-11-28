package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Base64
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlin.math.min
import kotlin.math.roundToInt

sealed class VideoAttachmentResult {
    data class Ready(
        val mediaData: String? = null,
        val mediaUrl: String? = null
    ) : VideoAttachmentResult()

    data class Failed(val message: String) : VideoAttachmentResult()
}

object MediaAttachmentHelper {

    private const val MAX_IMAGE_DIMENSION = 960
    private const val MAX_FIRESTORE_IMAGE_BYTES = 350_000
    private const val MAX_AVATAR_BYTES = 220_000
    private const val MAX_FIRESTORE_VIDEO_BYTES = 550_000L
    private const val MAX_STORAGE_VIDEO_BYTES = 25L * 1024L * 1024L

    suspend fun loadOrientedBitmap(
        context: Context,
        uri: Uri,
        maxDim: Int = 1080
    ): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.applicationContext.contentResolver
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            }
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return@withContext null

            var sampleSize = 1
            val longest = maxOf(boundsOptions.outWidth, boundsOptions.outHeight)
            while (longest / sampleSize > maxDim * 2) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            val decoded = resolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext null

            val orientation = try {
                resolver.openInputStream(uri)?.use { exifStream ->
                    ExifInterface(exifStream).getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Exception) {
                ExifInterface.ORIENTATION_NORMAL
            }

            val oriented = applyExifOrientation(decoded, orientation)
            if (oriented !== decoded) decoded.recycle()
            val scaled = scaleDownIfNeeded(oriented, maxDim)
            if (scaled !== oriented) oriented.recycle()
            scaled
        } catch (_: Exception) {
            null
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }
        return try {
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (_: Exception) {
            bitmap
        }
    }

    suspend fun cropAndEncodeAvatarDataUri(
        source: Bitmap,
        zoom: Float,
        offsetXFraction: Float,
        offsetYFraction: Float,
        outputSizePx: Int = 360
    ): String? = withContext(Dispatchers.IO) {
        var cropped: Bitmap? = null
        try {
            val w = source.width.toFloat()
            val h = source.height.toFloat()
            val minSide = min(w, h)
            val safeZoom = zoom.coerceIn(1f, 4f)
            val cropSide = (minSide / safeZoom).coerceAtLeast(16f)

            val maxPanX = ((w - cropSide) / 2f).coerceAtLeast(0f)
            val maxPanY = ((h - cropSide) / 2f).coerceAtLeast(0f)
            val centerX = (w / 2f) - (offsetXFraction.coerceIn(-1f, 1f) * maxPanX)
            val centerY = (h / 2f) - (offsetYFraction.coerceIn(-1f, 1f) * maxPanY)

            val left = (centerX - cropSide / 2f).coerceIn(0f, (w - cropSide).coerceAtLeast(0f))
            val top = (centerY - cropSide / 2f).coerceIn(0f, (h - cropSide).coerceAtLeast(0f))
            val right = (left + cropSide).coerceAtMost(w)
            val bottom = (top + cropSide).coerceAtMost(h)

            val outBitmap = Bitmap.createBitmap(outputSizePx, outputSizePx, Bitmap.Config.ARGB_8888)
            cropped = outBitmap
            val canvas = Canvas(outBitmap)
            val srcRect = Rect(left.roundToInt(), top.roundToInt(), right.roundToInt(), bottom.roundToInt())
            val dstRect = Rect(0, 0, outputSizePx, outputSizePx)
            canvas.drawBitmap(source, srcRect, dstRect, Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG))

            val baos = ByteArrayOutputStream()
            var quality = 86
            var bytes: ByteArray
            do {
                baos.reset()
                outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                bytes = baos.toByteArray()
                quality -= 10
            } while (bytes.size > MAX_AVATAR_BYTES && quality >= 36)

            if (bytes.size > 320_000) return@withContext null
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        } catch (_: Exception) {
            null
        } finally {
            cropped?.recycle()
        }
    }

    suspend fun encodeImageDataUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val scaled = loadOrientedBitmap(context, uri, MAX_IMAGE_DIMENSION) ?: return@withContext null

            val baos = ByteArrayOutputStream()
            var quality = 80
            var bytes: ByteArray
            do {
                baos.reset()
                scaled.compress(Bitmap.CompressFormat.JPEG, quality, baos)
                bytes = baos.toByteArray()
                quality -= 12
            } while (bytes.size > MAX_FIRESTORE_IMAGE_BYTES && quality >= 30)

            scaled.recycle()
            if (bytes.size > 480_000) return@withContext null

            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        } catch (_: Exception) {
            null
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val w = bitmap.width
        val h = bitmap.height
        if (w <= maxDim && h <= maxDim) return bitmap
        val ratio = maxDim.toFloat() / maxOf(w, h).toFloat()
        val targetW = (w * ratio).toInt().coerceAtLeast(1)
        val targetH = (h * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, targetW, targetH, true)
    }

    suspend fun prepareVideoAttachment(context: Context, uri: Uri): VideoAttachmentResult = withContext(Dispatchers.IO) {
        try {
            val appContext = context.applicationContext
            val resolver = appContext.contentResolver
            val fileSize = queryFileSize(appContext, uri)

            if (fileSize in 1L..MAX_FIRESTORE_VIDEO_BYTES) {
                val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.size <= MAX_FIRESTORE_VIDEO_BYTES) {
                    val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                    return@withContext VideoAttachmentResult.Ready(
                        mediaData = "data:video/mp4;base64,$b64"
                    )
                }
            }

            if (fileSize > MAX_STORAGE_VIDEO_BYTES) {
                return@withContext VideoAttachmentResult.Failed(
                    "Video is too large (> 25 MB). Please choose a shorter clip."
                )
            }

            val uid = Firebase.auth.currentUser?.uid
                ?: return@withContext VideoAttachmentResult.Failed("Please sign in to upload videos.")

            val storageRef = Firebase.storage.reference
                .child("posts_media")
                .child(uid)
                .child("${UUID.randomUUID()}.mp4")

            val downloadUrl = withTimeout(15_000L) {
                storageRef.putFile(uri).await()
                storageRef.downloadUrl.await().toString()
            }
            VideoAttachmentResult.Ready(mediaUrl = downloadUrl)
        } catch (e: Exception) {
            val fallbackHint = if (e.message?.contains("Object does not exist", ignoreCase = true) == true ||
                e.message?.contains("404", ignoreCase = true) == true ||
                e.message?.contains("bucket", ignoreCase = true) == true ||
                e.message?.contains("permission", ignoreCase = true) == true
            ) {
                "Firebase Storage is not activated in Firebase Console yet. Short video clips (<= 550 KB), compressed photos, and direct video links work for free right now."
            } else {
                e.localizedMessage ?: "Video upload failed. Short clips (<= 550 KB) or video links work without Cloud Storage."
            }
            VideoAttachmentResult.Failed(fallbackHint)
        }
    }

    private fun queryFileSize(context: Context, uri: Uri): Long {
        return try {
            context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
                val idx = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (idx >= 0 && cursor.moveToFirst()) cursor.getLong(idx) else -1L
            } ?: -1L
        } catch (_: Exception) {
            -1L
        }
    }

    fun decodeDataUriBytes(mediaData: String?): ByteArray? {
        if (mediaData.isNullOrBlank()) return null
        return try {
            val base64Part = if (mediaData.startsWith("data:") && mediaData.contains(",")) {
                mediaData.substringAfter(",")
            } else {
                mediaData
            }
            Base64.decode(base64Part, Base64.DEFAULT)
        } catch (_: Exception) {
            null
        }
    }

    fun writeDataUriToCacheFile(
        context: Context,
        key: String,
        mediaData: String,
        extension: String
    ): File? {
        return try {
            val safeKey = key.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val target = File(context.cacheDir, "media_${safeKey}.$extension")
            if (target.exists() && target.length() > 0L) return target
            val bytes = decodeDataUriBytes(mediaData) ?: return null
            target.writeBytes(bytes)
            target
        } catch (_: Exception) {
            null
        }
    }
}
