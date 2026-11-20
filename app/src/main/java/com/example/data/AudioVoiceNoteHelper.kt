package com.example.data

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File

data class RecordedVoiceNote(
    val dataUri: String,
    val durationSec: Int
)

class VoiceNoteRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    fun startRecording(): Boolean {
        stopSilently()
        return try {
            val file = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            outputFile = file
            val rec = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context.applicationContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            rec.setAudioSource(MediaRecorder.AudioSource.MIC)
            rec.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            rec.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            rec.setAudioChannels(1)
            rec.setAudioSamplingRate(16000)
            rec.setAudioEncodingBitRate(16000)
            rec.setMaxDuration(30_000)
            rec.setOutputFile(file.absolutePath)
            rec.prepare()
            rec.start()
            recorder = rec
            startTimeMs = System.currentTimeMillis()
            true
        } catch (_: Exception) {
            stopSilently()
            false
        }
    }

    fun stopAndEncode(): RecordedVoiceNote? {
        val file = outputFile ?: return null
        val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt().coerceIn(1, 600)
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            if (!file.exists() || file.length() == 0L) return null
            val bytes = file.readBytes()
            file.delete()
            outputFile = null
            if (bytes.size > 480_000) return null
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            RecordedVoiceNote(
                dataUri = "data:audio/mp4;base64,$b64",
                durationSec = elapsedSec
            )
        } catch (_: Exception) {
            stopSilently()
            null
        }
    }

    fun cancelRecording() {
        stopSilently()
    }

    private fun stopSilently() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
        }
        try {
            recorder?.release()
        } catch (_: Exception) {
        }
        recorder = null
        outputFile?.delete()
        outputFile = null
    }
}

object VoiceNotePlayer {
    private var mediaPlayer: MediaPlayer? = null
    private var activeId: String? = null

    fun playOrToggle(
        context: Context,
        messageId: String,
        mediaData: String?,
        mediaUrl: String?,
        onStateChanged: (playingMessageId: String?) -> Unit
    ) {
        if (activeId == messageId) {
            stop()
            onStateChanged(null)
            return
        }
        stop()

        val sourcePath: String? = when {
            !mediaData.isNullOrBlank() ->
                MediaAttachmentHelper.writeDataUriToCacheFile(context, messageId, mediaData, "m4a")?.absolutePath
            !mediaUrl.isNullOrBlank() -> mediaUrl
            else -> null
        }

        if (sourcePath == null) {
            onStateChanged(null)
            return
        }

        try {
            val player = MediaPlayer().apply {
                setDataSource(sourcePath)
                setOnPreparedListener {
                    it.start()
                    activeId = messageId
                    onStateChanged(messageId)
                }
                setOnCompletionListener {
                    stop()
                    onStateChanged(null)
                }
                setOnErrorListener { _, _, _ ->
                    stop()
                    onStateChanged(null)
                    true
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (_: Exception) {
            stop()
            onStateChanged(null)
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        activeId = null
    }
}
