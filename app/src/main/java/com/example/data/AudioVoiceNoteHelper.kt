package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.util.Base64
import java.io.File

data class RecordedVoiceNote(
    val dataUri: String,
    val durationSec: Int
)

sealed class VoiceNoteStopResult {
    data class Success(val note: RecordedVoiceNote) : VoiceNoteStopResult()
    data class TooLarge(val maxBytes: Int) : VoiceNoteStopResult()
    object Failed : VoiceNoteStopResult()
}

class VoiceNoteRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startTimeMs: Long = 0L

    companion object {
        const val MAX_VOICE_NOTE_BYTES = 480_000
    }

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

    fun stopAndEncodeDetailed(): VoiceNoteStopResult {
        val file = outputFile ?: return VoiceNoteStopResult.Failed
        val elapsedSec = ((System.currentTimeMillis() - startTimeMs) / 1000L).toInt().coerceIn(1, 600)
        return try {
            recorder?.stop()
            recorder?.release()
            recorder = null
            if (!file.exists() || file.length() == 0L) {
                file.delete()
                outputFile = null
                return VoiceNoteStopResult.Failed
            }
            val bytes = file.readBytes()
            file.delete()
            outputFile = null
            if (bytes.size > MAX_VOICE_NOTE_BYTES) {
                return VoiceNoteStopResult.TooLarge(MAX_VOICE_NOTE_BYTES)
            }
            val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            VoiceNoteStopResult.Success(
                RecordedVoiceNote(
                    dataUri = "data:audio/mp4;base64,$b64",
                    durationSec = elapsedSec
                )
            )
        } catch (_: Exception) {
            stopSilently()
            VoiceNoteStopResult.Failed
        }
    }

    fun stopAndEncode(): RecordedVoiceNote? {
        return (stopAndEncodeDetailed() as? VoiceNoteStopResult.Success)?.note
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
    private var appAudioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null

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

        val am = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        appAudioManager = am
        requestTransientFocus(am)

        try {
            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
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
        abandonTransientFocus()
    }

    private fun requestTransientFocus(am: AudioManager?) {
        if (am == null) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener { change ->
                        if (change == AudioManager.AUDIOFOCUS_LOSS ||
                            change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
                        ) {
                            stop()
                        }
                    }
                    .build()
                focusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            }
        } catch (_: Exception) {
        }
    }

    private fun abandonTransientFocus() {
        val am = appAudioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                focusRequest?.let { am.abandonAudioFocusRequest(it) }
                focusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Exception) {
        }
    }
}
