package com.example.data

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

/**
 * Independent local Soundboard & FX synthesis engine using Android AudioTrack.
 * Uses free procedural PCM synthesis so zero paid external audio streams or licensed servers are needed,
 * and stays completely decoupled from WebRTC's MODE_IN_COMMUNICATION audio pipeline.
 */
class VoiceSoundboardEngine {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var activeTrack: AudioTrack? = null
    private var activeJob: Job? = null

    private val _playingSoundId = MutableStateFlow<String?>(null)
    val playingSoundId: StateFlow<String?> = _playingSoundId.asStateFlow()

    private val _lastTriggerMs = MutableStateFlow(0L)

    fun canTriggerNow(cooldownMs: Long = 1200L): Boolean {
        return System.currentTimeMillis() - _lastTriggerMs.value >= cooldownMs
    }

    fun playEffect(soundId: String, volume: Float = 0.85f, bypassCooldown: Boolean = true) {
        val clampedVol = volume.coerceIn(0f, 1f)
        if (clampedVol <= 0.01f) return
        if (!bypassCooldown && !canTriggerNow()) return
        _lastTriggerMs.value = System.currentTimeMillis()

        stopAll()
        activeJob = scope.launch {
            _playingSoundId.value = soundId
            try {
                val sampleRate = 22050
                val samples = synthesizeWaveform(soundId, sampleRate, clampedVol)
                val minBuf = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                ).coerceAtLeast(samples.size * 2)

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(minBuf)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                activeTrack = track
                track.write(samples, 0, samples.size)
                track.play()
                val durationMs = (samples.size * 1000L) / sampleRate
                kotlinx.coroutines.delay(durationMs + 40L)
            } catch (t: Throwable) {
                Log.d("VoiceSoundboardEngine", "AudioTrack unavailable in current runtime: ${t.message}")
            } finally {
                _playingSoundId.compareAndSet(soundId, null)
                releaseTrackOnly()
            }
        }
    }

    fun stopAll() {
        activeJob?.cancel()
        activeJob = null
        releaseTrackOnly()
        _playingSoundId.value = null
    }

    fun release() {
        stopAll()
        scope.cancel()
    }

    @Synchronized
    private fun releaseTrackOnly() {
        try {
            activeTrack?.stop()
        } catch (_: Throwable) {
        }
        try {
            activeTrack?.release()
        } catch (_: Throwable) {
        }
        activeTrack = null
    }

    private fun synthesizeWaveform(soundId: String, sampleRate: Int, volume: Float): ShortArray {
        val durationSec = when (soundId) {
            "vip_fanfare", "celebration", "spaceship_warp" -> 0.85
            "helicopter_rotor", "dragon_roar", "vehicle_rev" -> 0.90
            "applause" -> 0.80
            else -> 0.55
        }
        val count = (sampleRate * durationSec).toInt()
        val out = ShortArray(count)
        val rng = Random(soundId.hashCode())

        for (i in 0 until count) {
            val t = i.toDouble() / sampleRate
            val normT = i.toDouble() / count
            val envelope = when {
                normT < 0.06 -> normT / 0.06
                else -> exp(-3.4 * (normT - 0.06))
            }

            val raw: Double = when (soundId) {
                "welcome" -> {
                    val f = if (normT < 0.5) 523.25 else 659.25
                    0.6 * sin(2 * PI * f * t) + 0.3 * sin(2 * PI * f * 1.5 * t)
                }
                "applause" -> {
                    val burst = 0.5 + 0.5 * sin(2 * PI * 14.0 * t)
                    (rng.nextDouble() * 2.0 - 1.0) * burst * 0.75
                }
                "celebration", "vip_fanfare" -> {
                    val note = when {
                        normT < 0.25 -> 523.25
                        normT < 0.50 -> 659.25
                        normT < 0.75 -> 783.99
                        else -> 1046.50
                    }
                    0.55 * sin(2 * PI * note * t) + 0.25 * sin(2 * PI * note * 2.0 * t)
                }
                "funny_boing" -> {
                    val sweep = 240.0 + 360.0 * sin(2 * PI * 4.5 * t)
                    0.75 * sin(2 * PI * sweep * t)
                }
                "cinematic_boom" -> {
                    val sweep = 135.0 * exp(-3.2 * t) + 42.0
                    0.85 * sin(2 * PI * sweep * t) + 0.2 * (rng.nextDouble() * 2.0 - 1.0) * exp(-5.0 * t)
                }
                "vehicle_rev" -> {
                    val revFreq = 95.0 + 210.0 * normT * (1.0 - 0.2 * sin(2 * PI * 6.0 * t))
                    0.6 * sin(2 * PI * revFreq * t) + 0.35 * sin(2 * PI * revFreq * 2.0 * t)
                }
                "helicopter_rotor" -> {
                    val chop = 0.35 + 0.65 * (0.5 + 0.5 * sin(2 * PI * 16.0 * t))
                    (0.55 * sin(2 * PI * 92.0 * t) + 0.35 * (rng.nextDouble() * 2.0 - 1.0)) * chop
                }
                "jet_flyby" -> {
                    val whooshFreq = 680.0 * (1.0 - 0.55 * normT) + 180.0
                    0.5 * sin(2 * PI * whooshFreq * t) + 0.35 * (rng.nextDouble() * 2.0 - 1.0)
                }
                "spaceship_warp" -> {
                    val warp = 220.0 + 620.0 * normT * normT
                    0.55 * sin(2 * PI * warp * t) + 0.3 * sin(2 * PI * (warp * 1.5) * t)
                }
                "dragon_roar" -> {
                    val growl = 82.0 - 26.0 * normT
                    0.65 * sin(2 * PI * growl * t) + 0.45 * (rng.nextDouble() * 2.0 - 1.0) * (0.6 + 0.4 * sin(2 * PI * 19.0 * t))
                }
                "gift_crystal" -> {
                    0.5 * sin(2 * PI * 1174.66 * t) + 0.35 * sin(2 * PI * 1760.0 * t)
                }
                else -> { // gift_sparkle
                    val chime = 880.0 + 440.0 * normT
                    0.5 * sin(2 * PI * chime * t) + 0.3 * sin(2 * PI * chime * 1.5 * t)
                }
            }

            val sample = (raw * envelope * volume * Short.MAX_VALUE * 0.75)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = sample.toShort()
        }
        return out
    }
}
