package com.example.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.model.AudioRouteMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.MediaStreamTrack
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.RtpTransceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.math.sqrt

class WebRtcVoiceManager(private val appContext: Context) {

    companion object {
        private const val TAG = "WebRtcVoiceManager"
        /** Practical P2P mesh limit without a paid SFU server. */
        const val MAX_AUDIO_PARTICIPANTS = 6
        @Volatile
        private var peerConnectionFactoryGlobalInitialized = false
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val rtcMutex = Mutex()
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    // Free public STUN servers by default; optional free self-hosted TURN/SFU servers can be appended via setCustomIceServers().
    private var iceServers: List<PeerConnection.IceServer> = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun3.l.google.com:19302").createIceServer()
    )

    private var factory: PeerConnectionFactory? = null
    private var audioDeviceModule: JavaAudioDeviceModule? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var singleCallConnection: PeerConnection? = null
    private val roomConnections = ConcurrentHashMap<String, PeerConnection>()
    private val pendingRoomCandidates = ConcurrentHashMap<String, MutableList<IceCandidate>>()
    private val pendingCallCandidates = mutableListOf<IceCandidate>()
    private var isRemoteSdpSetForSingleCall = false
    private val roomRemoteSdpSet = ConcurrentHashMap<String, Boolean>()
    private val iceRestartAttempted = ConcurrentHashMap<String, Boolean>()
    private val processedSignalIds: MutableSet<String> =
        Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())
    private var activeSignalCallback: ((toUid: String, type: String, sdp: String, sdpMid: String?, sdpMLineIndex: Int?) -> Unit)? = null
    private var audioStatsJob: Job? = null

    private var isInCommunicationAudioMode = false
    private var audioFocusRequest: AudioFocusRequest? = null
    @Volatile
    private var lastMicRmsSampleTimeMs = 0L
    @Volatile
    private var isHostMutedLock = false

    private val _hasMicPermission = MutableStateFlow(hasRecordAudioPermission())
    val hasMicPermission: StateFlow<Boolean> = _hasMicPermission.asStateFlow()

    private val _isMicCapturing = MutableStateFlow(false)
    val isMicCapturing: StateFlow<Boolean> = _isMicCapturing.asStateFlow()

    private val _connectedPeersCount = MutableStateFlow(0)
    val connectedPeersCount: StateFlow<Int> = _connectedPeersCount.asStateFlow()

    private val _connectedPeerIds = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeerIds: StateFlow<Set<String>> = _connectedPeerIds.asStateFlow()

    private val _activeSpeakingPeerIds = MutableStateFlow<Set<String>>(emptySet())
    val activeSpeakingPeerIds: StateFlow<Set<String>> = _activeSpeakingPeerIds.asStateFlow()

    private val _localAudioLevel = MutableStateFlow(0f)
    val localAudioLevel: StateFlow<Float> = _localAudioLevel.asStateFlow()

    private val _roomIceState = MutableStateFlow("idle")
    val roomIceState: StateFlow<String> = _roomIceState.asStateFlow()

    private val _isCallMediaConnected = MutableStateFlow(false)
    val isCallMediaConnected: StateFlow<Boolean> = _isCallMediaConnected.asStateFlow()

    private val _isRoomCapacityReached = MutableStateFlow(false)
    val isRoomCapacityReached: StateFlow<Boolean> = _isRoomCapacityReached.asStateFlow()

    private val _audioRoute = MutableStateFlow(AudioRouteMode.SPEAKER)
    val audioRoute: StateFlow<AudioRouteMode> = _audioRoute.asStateFlow()

    private val _isBluetoothAvailable = MutableStateFlow(false)
    val isBluetoothAvailable: StateFlow<Boolean> = _isBluetoothAvailable.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            val btNow = refreshBluetoothAvailability()
            if (isInCommunicationAudioMode && btNow && _audioRoute.value != AudioRouteMode.BLUETOOTH) {
                setAudioRoute(AudioRouteMode.BLUETOOTH)
            }
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            val btNow = refreshBluetoothAvailability()
            if (isInCommunicationAudioMode && !btNow && _audioRoute.value == AudioRouteMode.BLUETOOTH) {
                setAudioRoute(AudioRouteMode.SPEAKER)
            }
        }
    }

    init {
        try {
            audioManager?.registerAudioDeviceCallback(
                audioDeviceCallback,
                Handler(Looper.getMainLooper())
            )
            refreshBluetoothAvailability()
        } catch (_: Throwable) {
        }
    }

    fun hasRecordAudioPermission(): Boolean {
        return try {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (_: Throwable) {
            false
        }
    }

    fun refreshMicPermission(): Boolean {
        val granted = hasRecordAudioPermission()
        _hasMicPermission.value = granted
        if (!granted) {
            _isMicCapturing.value = false
            _localAudioLevel.value = 0f
        }
        return granted
    }

    fun setCustomIceServers(customServers: List<PeerConnection.IceServer>) {
        if (customServers.isNotEmpty()) {
            iceServers = customServers
        }
    }

    fun clearError() {
        _lastError.value = null
    }

    fun isHostMutedLocked(): Boolean = isHostMutedLock

    fun setHostMuted(hostMuted: Boolean) {
        isHostMutedLock = hostMuted
        if (hostMuted) {
            try {
                localAudioTrack?.setEnabled(false)
            } catch (_: Exception) {
            }
            _isMicCapturing.value = false
            _localAudioLevel.value = 0f
        }
    }

    /**
     * Initializes the WebRTC PeerConnectionFactory & JavaAudioDeviceModule (for playback and
     * audio routing). Does NOT create an AudioSource/AudioRecord unless RECORD_AUDIO is granted.
     */
    @Synchronized
    private fun ensureFactoryInitialized(): Boolean {
        if (factory != null) return true
        return try {
            if (!peerConnectionFactoryGlobalInitialized) {
                PeerConnectionFactory.initialize(
                    PeerConnectionFactory.InitializationOptions.builder(appContext)
                        .setEnableInternalTracer(false)
                        .createInitializationOptions()
                )
                peerConnectionFactoryGlobalInitialized = true
            }

            val adm = JavaAudioDeviceModule.builder(appContext)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .setAudioRecordErrorCallback(object : JavaAudioDeviceModule.AudioRecordErrorCallback {
                    override fun onWebRtcAudioRecordInitError(errorMessage: String?) {
                        Log.w(TAG, "AudioRecord init error: $errorMessage")
                        _isMicCapturing.value = false
                        _localAudioLevel.value = 0f
                        _lastError.value = errorMessage ?: "Microphone initialization failed."
                    }

                    override fun onWebRtcAudioRecordStartError(
                        errorCode: JavaAudioDeviceModule.AudioRecordStartErrorCode?,
                        errorMessage: String?
                    ) {
                        Log.w(TAG, "AudioRecord start error ($errorCode): $errorMessage")
                        _isMicCapturing.value = false
                        _localAudioLevel.value = 0f
                        _lastError.value = errorMessage ?: "Microphone capture could not start ($errorCode)."
                    }

                    override fun onWebRtcAudioRecordError(errorMessage: String?) {
                        Log.w(TAG, "AudioRecord runtime error: $errorMessage")
                        _isMicCapturing.value = false
                        _localAudioLevel.value = 0f
                        _lastError.value = errorMessage ?: "Microphone capture error."
                    }
                })
                .setAudioTrackErrorCallback(object : JavaAudioDeviceModule.AudioTrackErrorCallback {
                    override fun onWebRtcAudioTrackInitError(errorMessage: String?) {
                        Log.w(TAG, "AudioTrack init error: $errorMessage")
                        _lastError.value = errorMessage ?: "Audio output initialization failed."
                    }

                    override fun onWebRtcAudioTrackStartError(
                        errorCode: JavaAudioDeviceModule.AudioTrackStartErrorCode?,
                        errorMessage: String?
                    ) {
                        Log.w(TAG, "AudioTrack start error ($errorCode): $errorMessage")
                        _lastError.value = errorMessage ?: "Audio output playback error ($errorCode)."
                    }

                    override fun onWebRtcAudioTrackError(errorMessage: String?) {
                        Log.w(TAG, "AudioTrack runtime error: $errorMessage")
                        _lastError.value = errorMessage ?: "Audio playback error."
                    }
                })
                .setSamplesReadyCallback { audioSamples ->
                    if (localAudioTrack?.enabled() == true && _hasMicPermission.value) {
                        val now = System.currentTimeMillis()
                        if (now - lastMicRmsSampleTimeMs >= 110L) {
                            lastMicRmsSampleTimeMs = now
                            _localAudioLevel.value = computeNormalizedRms(audioSamples.data)
                        }
                    } else if (_localAudioLevel.value != 0f) {
                        _localAudioLevel.value = 0f
                    }
                }
                .createAudioDeviceModule()

            val createdFactory = PeerConnectionFactory.builder()
                .setAudioDeviceModule(adm)
                .createPeerConnectionFactory()

            audioDeviceModule = adm
            factory = createdFactory
            true
        } catch (t: Throwable) {
            Log.w(TAG, "WebRTC factory init unavailable on this runtime: ${t.message}")
            _isMicCapturing.value = false
            _lastError.value = "Audio engine unavailable on this device or emulator."
            false
        }
    }

    /**
     * Creates local microphone AudioSource & AudioTrack ONLY when RECORD_AUDIO permission is granted.
     * Calling this while unpermitted never touches native AudioRecord and keeps capture safely off.
     */
    @Synchronized
    private fun ensureLocalAudioTrackInitialized(): Boolean {
        val granted = refreshMicPermission()
        if (!granted) {
            disposeLocalAudioCaptureLocked()
            return false
        }
        val f = factory ?: if (ensureFactoryInitialized()) factory else null
        if (f == null) return false

        if (localAudioTrack != null && localAudioSource != null) {
            _isMicCapturing.value = (localAudioTrack?.enabled() == true)
            return true
        }

        return try {
            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googHighpassFilter", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googTypingNoiseDetection", "true"))
            }
            val source = f.createAudioSource(audioConstraints)
            val track = f.createAudioTrack("viberoom_audio_0", source).apply {
                setVolume(1.0)
                setEnabled(false)
            }
            localAudioSource = source
            localAudioTrack = track
            _isMicCapturing.value = false
            true
        } catch (t: Throwable) {
            Log.w(TAG, "Microphone AudioTrack creation failed: ${t.message}")
            _isMicCapturing.value = false
            _lastError.value = "Failed to initialize microphone capture: ${t.message}"
            false
        }
    }

    @Synchronized
    private fun disposeLocalAudioCaptureLocked() {
        try {
            localAudioTrack?.setEnabled(false)
            localAudioTrack?.dispose()
        } catch (_: Exception) {
        }
        try {
            localAudioSource?.dispose()
        } catch (_: Exception) {
        }
        localAudioTrack = null
        localAudioSource = null
        _isMicCapturing.value = false
        _localAudioLevel.value = 0f
    }

    private fun computeNormalizedRms(pcmBytes: ByteArray): Float {
        if (pcmBytes.size < 2) return 0f
        val sampleCount = pcmBytes.size / 2
        val buffer = ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN)
        var sumSq = 0.0
        for (i in 0 until sampleCount) {
            val sample = buffer.short.toDouble() / 32768.0
            sumSq += sample * sample
        }
        val rms = sqrt(sumSq / sampleCount.coerceAtLeast(1)).toFloat()
        // Boost low speech RMS slightly into a clean [0f, 1f] meter range while preserving true silence as ~0.
        return if (rms < 0.008f) 0f else (rms * 4.2f).coerceIn(0f, 1f)
    }

    fun refreshBluetoothAvailability(): Boolean {
        val am = audioManager ?: return false
        val hasBtPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
        val available = if (!hasBtPermission) {
            false
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            am.availableCommunicationDevices.any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
            }
        } else {
            @Suppress("DEPRECATION")
            am.getDevices(AudioManager.GET_DEVICES_OUTPUTS).any {
                it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
            }
        }
        _isBluetoothAvailable.value = available
        return available
    }

    fun preferredJoinRoute(): AudioRouteMode {
        return if (refreshBluetoothAvailability()) AudioRouteMode.BLUETOOTH else AudioRouteMode.SPEAKER
    }

    fun setAudioModeForCall(inCall: Boolean, speakerOn: Boolean) {
        val am = audioManager ?: return
        try {
            if (inCall) {
                if (!isInCommunicationAudioMode) {
                    requestCommunicationAudioFocus(am)
                    am.mode = AudioManager.MODE_IN_COMMUNICATION
                    isInCommunicationAudioMode = true
                    refreshBluetoothAvailability()
                    val route = when {
                        _isBluetoothAvailable.value -> AudioRouteMode.BLUETOOTH
                        speakerOn -> AudioRouteMode.SPEAKER
                        else -> AudioRouteMode.EARPIECE
                    }
                    setAudioRoute(route)
                }
            } else {
                isInCommunicationAudioMode = false
                stopBluetoothScoSafely(am)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    am.clearCommunicationDevice()
                } else {
                    @Suppress("DEPRECATION")
                    am.isSpeakerphoneOn = false
                }
                am.mode = AudioManager.MODE_NORMAL
                abandonCommunicationAudioFocus(am)
            }
        } catch (e: Exception) {
            Log.w(TAG, "AudioManager mode switch error: ${e.message}")
        }
    }

    private fun requestCommunicationAudioFocus(am: AudioManager) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val attrs = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(attrs)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener { /* Managed via PeerConnection volume/mute */ }
                    .build()
                audioFocusRequest = req
                am.requestAudioFocus(req)
            } else {
                @Suppress("DEPRECATION")
                am.requestAudioFocus(
                    null,
                    AudioManager.STREAM_VOICE_CALL,
                    AudioManager.AUDIOFOCUS_GAIN
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "Audio focus request failed: ${e.message}")
        }
    }

    private fun abandonCommunicationAudioFocus(am: AudioManager) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { am.abandonAudioFocusRequest(it) }
                audioFocusRequest = null
            } else {
                @Suppress("DEPRECATION")
                am.abandonAudioFocus(null)
            }
        } catch (_: Exception) {
        }
    }

    fun setAudioRoute(route: AudioRouteMode) {
        val am = audioManager ?: return
        _audioRoute.value = route
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val targetTypes = when (route) {
                    AudioRouteMode.BLUETOOTH -> listOf(
                        AudioDeviceInfo.TYPE_BLE_HEADSET,
                        AudioDeviceInfo.TYPE_BLUETOOTH_SCO
                    )
                    AudioRouteMode.SPEAKER -> listOf(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)
                    AudioRouteMode.EARPIECE -> listOf(
                        AudioDeviceInfo.TYPE_WIRED_HEADSET,
                        AudioDeviceInfo.TYPE_WIRED_HEADPHONES,
                        AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    )
                }
                val device = am.availableCommunicationDevices.firstOrNull { it.type in targetTypes }
                if (device != null) {
                    am.setCommunicationDevice(device)
                } else if (route == AudioRouteMode.BLUETOOTH) {
                    _lastError.value = "Bluetooth headset unavailable; switched to speaker."
                    _audioRoute.value = AudioRouteMode.SPEAKER
                    am.availableCommunicationDevices
                        .firstOrNull { it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER }
                        ?.let { am.setCommunicationDevice(it) }
                } else {
                    am.clearCommunicationDevice()
                }
            } else {
                when (route) {
                    AudioRouteMode.BLUETOOTH -> {
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = false
                        @Suppress("DEPRECATION")
                        am.startBluetoothSco()
                        @Suppress("DEPRECATION")
                        am.isBluetoothScoOn = true
                    }
                    AudioRouteMode.SPEAKER -> {
                        stopBluetoothScoSafely(am)
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = true
                    }
                    AudioRouteMode.EARPIECE -> {
                        stopBluetoothScoSafely(am)
                        @Suppress("DEPRECATION")
                        am.isSpeakerphoneOn = false
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to switch audio route to $route: ${e.message}")
        }
    }

    private fun stopBluetoothScoSafely(am: AudioManager) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            try {
                @Suppress("DEPRECATION")
                am.isBluetoothScoOn = false
                @Suppress("DEPRECATION")
                am.stopBluetoothSco()
            } catch (_: Exception) {
            }
        }
    }

    fun setSpeakerphoneEnabled(enabled: Boolean) {
        setAudioRoute(if (enabled) AudioRouteMode.SPEAKER else AudioRouteMode.EARPIECE)
    }

    fun setMicEnabled(enabled: Boolean) {
        val permitted = refreshMicPermission()
        if (!permitted || isHostMutedLock) {
            try {
                localAudioTrack?.setEnabled(false)
            } catch (_: Exception) {
            }
            _isMicCapturing.value = false
            _localAudioLevel.value = 0f
            return
        }
        val track = localAudioTrack
        if (track == null) {
            if (enabled && ensureLocalAudioTrackInitialized()) {
                localAudioTrack?.setEnabled(true)
                _isMicCapturing.value = (localAudioTrack?.enabled() == true)
            } else {
                _isMicCapturing.value = false
                _localAudioLevel.value = 0f
            }
            return
        }
        track.setEnabled(enabled)
        _isMicCapturing.value = enabled
        if (!enabled) {
            _localAudioLevel.value = 0f
        }
    }

    /**
     * Called when runtime RECORD_AUDIO permission changes (either granted mid-room or denied).
     * If granted and peers are already connected in RECV_ONLY mode, attaches the local AudioTrack
     * and triggers SDP offer renegotiation so remote peers hear the user cleanly.
     */
    fun onMicPermissionChanged(
        granted: Boolean,
        shouldEnableMic: Boolean,
        myUid: String,
        onSignalToPeer: (toUid: String, type: String, sdp: String, sdpMid: String?, sdpMLineIndex: Int?) -> Unit
    ) {
        scope.launch {
            rtcMutex.withLock {
                _hasMicPermission.value = granted
                if (!granted) {
                    disposeLocalAudioCaptureLocked()
                    return@withLock
                }
                if (!ensureFactoryInitialized()) return@withLock
                val trackReady = ensureLocalAudioTrackInitialized()
                if (!trackReady) return@withLock
                val activeEnable = shouldEnableMic && !isHostMutedLock
                localAudioTrack?.setEnabled(activeEnable)
                _isMicCapturing.value = activeEnable
                if (!activeEnable) _localAudioLevel.value = 0f

                if (myUid.isNotBlank() && roomConnections.isNotEmpty()) {
                    attachLocalTrackAndRenegotiateLocked(myUid, onSignalToPeer)
                }
            }
        }
    }

    private suspend fun attachLocalTrackAndRenegotiateLocked(
        myUid: String,
        onSignalToPeer: (toUid: String, type: String, sdp: String, sdpMid: String?, sdpMLineIndex: Int?) -> Unit
    ) {
        val track = localAudioTrack ?: return
        roomConnections.forEach { (peerId, pc) ->
            try {
                val existingAudioTransceiver = pc.transceivers.firstOrNull {
                    it.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO
                }
                val needsRenegotiation = if (existingAudioTransceiver != null) {
                    val alreadyHasTrack = existingAudioTransceiver.sender.track()?.id() == track.id()
                    if (!alreadyHasTrack) {
                        existingAudioTransceiver.sender.setTrack(track, false)
                    }
                    val wasRecvOnly = existingAudioTransceiver.direction == RtpTransceiver.RtpTransceiverDirection.RECV_ONLY
                    if (wasRecvOnly) {
                        existingAudioTransceiver.direction = RtpTransceiver.RtpTransceiverDirection.SEND_RECV
                    }
                    !alreadyHasTrack || wasRecvOnly
                } else {
                    pc.addTrack(track, listOf("viberoom_stream"))
                    true
                }

                if (needsRenegotiation && pc.signalingState() == PeerConnection.SignalingState.STABLE) {
                    val offer = createSdpOfferSuspending(pc, audioOnlyMediaConstraints())
                    if (offer != null && setLocalDescriptionSuspending(pc, offer)) {
                        onSignalToPeer(peerId, "offer", offer.description, null, null)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed attaching mic track to peer $peerId: ${e.message}")
            }
        }
    }

    // --- 1-to-1 Voice Call ---

    fun startOutgoingCall(
        onOfferCreated: (sdp: String) -> Unit,
        onLocalIceCandidate: (sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            rtcMutex.withLock {
                endSingleCallOnly()
                if (!ensureFactoryInitialized()) return@withLock
                if (!ensureLocalAudioTrackInitialized()) {
                    _lastError.value = "Microphone permission is required for voice calls."
                    return@withLock
                }
                setAudioModeForCall(inCall = true, speakerOn = false)
                localAudioTrack?.setEnabled(true)
                _isMicCapturing.value = true
                isRemoteSdpSetForSingleCall = false
                pendingCallCandidates.clear()

                val pc = createPeerConnection(
                    onIce = onLocalIceCandidate,
                    onStateChange = { state ->
                        _isCallMediaConnected.value = (state == PeerConnection.IceConnectionState.CONNECTED ||
                            state == PeerConnection.IceConnectionState.COMPLETED)
                        if (state == PeerConnection.IceConnectionState.FAILED) {
                            _lastError.value = "Call ICE failed (STUN traversal blocked by NAT/firewall)."
                        }
                    }
                ) ?: return@withLock
                singleCallConnection = pc

                val offer = createSdpOfferSuspending(pc, audioOnlyMediaConstraints()) ?: return@withLock
                if (setLocalDescriptionSuspending(pc, offer)) {
                    onOfferCreated(offer.description)
                }
            }
        }
    }

    fun acceptIncomingCall(
        offerSdp: String,
        onAnswerCreated: (sdp: String) -> Unit,
        onLocalIceCandidate: (sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            rtcMutex.withLock {
                endSingleCallOnly()
                if (!ensureFactoryInitialized()) return@withLock
                ensureLocalAudioTrackInitialized()
                setAudioModeForCall(inCall = true, speakerOn = false)
                localAudioTrack?.setEnabled(localAudioTrack != null)
                _isMicCapturing.value = localAudioTrack != null
                isRemoteSdpSetForSingleCall = false

                val pc = createPeerConnection(
                    onIce = onLocalIceCandidate,
                    onStateChange = { state ->
                        _isCallMediaConnected.value = (state == PeerConnection.IceConnectionState.CONNECTED ||
                            state == PeerConnection.IceConnectionState.COMPLETED)
                        if (state == PeerConnection.IceConnectionState.FAILED) {
                            _lastError.value = "Call ICE failed (STUN traversal blocked by NAT/firewall)."
                        }
                    }
                ) ?: return@withLock
                singleCallConnection = pc

                val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, offerSdp)
                if (!setRemoteDescriptionSuspending(pc, remoteDesc)) return@withLock
                isRemoteSdpSetForSingleCall = true
                synchronized(pendingCallCandidates) {
                    pendingCallCandidates.forEach { pc.addIceCandidate(it) }
                    pendingCallCandidates.clear()
                }
                val answer = createSdpAnswerSuspending(pc, audioOnlyMediaConstraints()) ?: return@withLock
                if (setLocalDescriptionSuspending(pc, answer)) {
                    onAnswerCreated(answer.description)
                }
            }
        }
    }

    fun applyCallAnswer(answerSdp: String) {
        val pc = singleCallConnection ?: return
        if (isRemoteSdpSetForSingleCall) return
        val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, answerSdp)
        pc.setRemoteDescription(object : SimpleSdpObserver() {
            override fun onSetSuccess() {
                isRemoteSdpSetForSingleCall = true
                synchronized(pendingCallCandidates) {
                    pendingCallCandidates.forEach { pc.addIceCandidate(it) }
                    pendingCallCandidates.clear()
                }
            }
        }, remoteDesc)
    }

    fun addCallRemoteIceCandidate(sdpMid: String, sdpMLineIndex: Int, sdp: String) {
        val candidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        val pc = singleCallConnection
        if (pc != null && isRemoteSdpSetForSingleCall) {
            pc.addIceCandidate(candidate)
        } else {
            synchronized(pendingCallCandidates) {
                pendingCallCandidates.add(candidate)
            }
        }
    }

    fun endCall() {
        scope.launch {
            rtcMutex.withLock {
                endSingleCallOnly()
                if (roomConnections.isEmpty()) {
                    disposeLocalAudioCaptureLocked()
                    setAudioModeForCall(inCall = false, speakerOn = false)
                }
            }
        }
    }

    private fun endSingleCallOnly() {
        try {
            singleCallConnection?.close()
            singleCallConnection?.dispose()
        } catch (_: Exception) {
        }
        singleCallConnection = null
        isRemoteSdpSetForSingleCall = false
        synchronized(pendingCallCandidates) { pendingCallCandidates.clear() }
        _isCallMediaConnected.value = false
    }

    // --- Voice Room Multi-Peer WebRTC Mesh Audio (Max 6 Peers) ---

    fun syncRoomPeers(
        myUid: String,
        remotePeerIds: List<String>,
        micEnabled: Boolean,
        onSignalToPeer: (toUid: String, type: String, sdp: String, sdpMid: String?, sdpMLineIndex: Int?) -> Unit
    ) {
        activeSignalCallback = onSignalToPeer
        scope.launch {
            rtcMutex.withLock {
                if (!ensureFactoryInitialized()) {
                    _roomIceState.value = "unavailable"
                    return@withLock
                }

                val hadLocalTrackBefore = (localAudioTrack != null)
                val permitted = refreshMicPermission()
                val effectiveMicEnabled = micEnabled && !isHostMutedLock
                if (permitted) {
                    ensureLocalAudioTrackInitialized()
                    localAudioTrack?.setEnabled(effectiveMicEnabled)
                    _isMicCapturing.value = effectiveMicEnabled
                    if (!effectiveMicEnabled) _localAudioLevel.value = 0f
                } else {
                    disposeLocalAudioCaptureLocked()
                }

                setAudioModeForCall(inCall = true, speakerOn = (_audioRoute.value == AudioRouteMode.SPEAKER))

                // If permission was just granted while peers were already in RECV_ONLY, upgrade & renegotiate
                if (!hadLocalTrackBefore && localAudioTrack != null && roomConnections.isNotEmpty()) {
                    attachLocalTrackAndRenegotiateLocked(myUid, onSignalToPeer)
                }

                val distinctRemotes = remotePeerIds.filter { it != myUid && it.isNotBlank() }.distinct()
                val maxRemotes = (MAX_AUDIO_PARTICIPANTS - 1).coerceAtLeast(1)
                _isRoomCapacityReached.value = distinctRemotes.size > maxRemotes

                val remoteSet = distinctRemotes.take(maxRemotes).toSet()
                val removed = roomConnections.keys.filter { it !in remoteSet }
                removed.forEach { peerId -> closeRoomPeer(peerId) }

                for (peerId in remoteSet) {
                    if (!roomConnections.containsKey(peerId)) {
                        val pc = createPeerConnection(
                            onIce = { mid, idx, candSdp ->
                                onSignalToPeer(peerId, "ice", candSdp, mid, idx)
                            },
                            onStateChange = { state ->
                                handleRoomPeerIceState(myUid, peerId, state)
                            }
                        )
                        if (pc != null) {
                            roomConnections[peerId] = pc
                            roomRemoteSdpSet[peerId] = false
                            iceRestartAttempted[peerId] = false
                            // Deterministic initiator avoids SDP glare between two peers
                            if (myUid < peerId) {
                                val offer = createSdpOfferSuspending(pc, audioOnlyMediaConstraints())
                                if (offer != null && setLocalDescriptionSuspending(pc, offer)) {
                                    onSignalToPeer(peerId, "offer", offer.description, null, null)
                                }
                            }
                        }
                    }
                }
                startAudioStatsPolling(myUid)
                updateConnectedRoomCount()
            }
        }
    }

    private fun handleRoomPeerIceState(
        myUid: String,
        peerId: String,
        state: PeerConnection.IceConnectionState
    ) {
        updateConnectedRoomCount()
        if (state == PeerConnection.IceConnectionState.FAILED ||
            state == PeerConnection.IceConnectionState.DISCONNECTED
        ) {
            val alreadyRestarted = iceRestartAttempted[peerId] == true
            val isInitiator = myUid.isNotBlank() && myUid < peerId
            if (!alreadyRestarted && isInitiator) {
                iceRestartAttempted[peerId] = true
                _roomIceState.value = "reconnecting"
                scope.launch {
                    rtcMutex.withLock {
                        val pc = roomConnections[peerId] ?: return@withLock
                        val restartConstraints = audioOnlyMediaConstraints().apply {
                            mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
                        }
                        val offer = createSdpOfferSuspending(pc, restartConstraints) ?: return@withLock
                        if (setLocalDescriptionSuspending(pc, offer)) {
                            activeSignalCallback?.invoke(peerId, "offer", offer.description, null, null)
                        }
                    }
                }
            } else if (state == PeerConnection.IceConnectionState.FAILED) {
                _roomIceState.value = "failed"
                _lastError.value =
                    "Peer audio connection failed (public STUN cannot traverse symmetric NAT/restrictive mobile networks without a TURN server)."
            }
        } else if (state == PeerConnection.IceConnectionState.CONNECTED ||
            state == PeerConnection.IceConnectionState.COMPLETED
        ) {
            iceRestartAttempted[peerId] = false
            if (_lastError.value?.contains("ICE", ignoreCase = true) == true ||
                _lastError.value?.contains("NAT", ignoreCase = true) == true ||
                _lastError.value?.contains("Peer audio", ignoreCase = true) == true
            ) {
                _lastError.value = null
            }
        }
    }

    private fun startAudioStatsPolling(myUid: String) {
        if (audioStatsJob?.isActive == true) return
        audioStatsJob = scope.launch {
            while (isActive) {
                delay(350L)
                val connections = roomConnections.entries.toList()
                if (connections.isEmpty()) {
                    _activeSpeakingPeerIds.value = emptySet()
                    if (localAudioTrack?.enabled() != true || !_hasMicPermission.value) {
                        _localAudioLevel.value = 0f
                    }
                    continue
                }

                val speakingMap = ConcurrentHashMap<String, Boolean>()
                connections.forEach { (peerId, pc) ->
                    try {
                        pc.getStats { report ->
                            report.statsMap.values.forEach { stats ->
                                when (stats.type) {
                                    "media-source" -> {
                                        val lvl = (stats.members["audioLevel"] as? Number)?.toFloat() ?: 0f
                                        if (lvl > 0.025f && localAudioTrack?.enabled() == true && _hasMicPermission.value) {
                                            speakingMap[myUid] = true
                                            _localAudioLevel.value = maxOf(_localAudioLevel.value, lvl)
                                        }
                                    }
                                    "inbound-rtp" -> {
                                        val kind = stats.members["kind"]?.toString()
                                        val lvl = (stats.members["audioLevel"] as? Number)?.toFloat() ?: 0f
                                        if (kind == "audio" && lvl > 0.025f) {
                                            speakingMap[peerId] = true
                                        }
                                    }
                                }
                            }
                            _activeSpeakingPeerIds.value = speakingMap.keys.toSet()
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
    }

    fun handleRoomSignal(
        myUid: String,
        signal: FirestoreRoomSignal,
        onAnswerToPeer: (toUid: String, sdp: String) -> Unit,
        onIceToPeer: (toUid: String, sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            rtcMutex.withLock {
                val fromUid = signal.fromUserId
                if (fromUid.isBlank() || fromUid == myUid) return@withLock
                if (signal.id.isNotBlank() && !processedSignalIds.add(signal.id)) return@withLock

                if (!ensureFactoryInitialized()) return@withLock
                if (refreshMicPermission()) {
                    ensureLocalAudioTrackInitialized()
                }
                setAudioModeForCall(inCall = true, speakerOn = (_audioRoute.value == AudioRouteMode.SPEAKER))

                val pc = roomConnections[fromUid] ?: createPeerConnection(
                    onIce = { mid, idx, candSdp -> onIceToPeer(fromUid, mid, idx, candSdp) },
                    onStateChange = { state -> handleRoomPeerIceState(myUid, fromUid, state) }
                )?.also {
                    roomConnections[fromUid] = it
                    roomRemoteSdpSet[fromUid] = false
                } ?: return@withLock

                when (signal.type) {
                    "offer" -> {
                        val signalingState = pc.signalingState()
                        if (signalingState == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                            // Impolite peer (smaller UID) ignores colliding offer; polite peer rolls back
                            if (myUid.isNotBlank() && myUid < fromUid) {
                                return@withLock
                            } else {
                                setLocalDescriptionSuspending(
                                    pc,
                                    SessionDescription(SessionDescription.Type.ROLLBACK, "")
                                )
                            }
                        }
                        val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, signal.sdp)
                        if (setRemoteDescriptionSuspending(pc, remoteDesc)) {
                            roomRemoteSdpSet[fromUid] = true
                            flushPendingRoomCandidates(fromUid, pc)
                            localAudioTrack?.let { track ->
                                val audioTransceiver = pc.transceivers.firstOrNull {
                                    it.mediaType == MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO
                                }
                                if (audioTransceiver != null) {
                                    if (audioTransceiver.sender.track()?.id() != track.id()) {
                                        audioTransceiver.sender.setTrack(track, false)
                                    }
                                    if (audioTransceiver.direction != RtpTransceiver.RtpTransceiverDirection.SEND_RECV) {
                                        audioTransceiver.direction = RtpTransceiver.RtpTransceiverDirection.SEND_RECV
                                    }
                                } else if (pc.senders.none { it.track()?.id() == track.id() }) {
                                    pc.addTrack(track, listOf("viberoom_stream"))
                                }
                            }
                            val answerDesc = createSdpAnswerSuspending(pc, audioOnlyMediaConstraints())
                            if (answerDesc != null && setLocalDescriptionSuspending(pc, answerDesc)) {
                                onAnswerToPeer(fromUid, answerDesc.description)
                            }
                        }
                    }
                    "answer" -> {
                        if (pc.signalingState() == PeerConnection.SignalingState.HAVE_LOCAL_OFFER) {
                            val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, signal.sdp)
                            if (setRemoteDescriptionSuspending(pc, remoteDesc)) {
                                roomRemoteSdpSet[fromUid] = true
                                flushPendingRoomCandidates(fromUid, pc)
                            }
                        }
                    }
                    "ice" -> {
                        val candidate = IceCandidate(
                            signal.sdpMid ?: "0",
                            signal.sdpMLineIndex ?: 0,
                            signal.sdp
                        )
                        if (roomRemoteSdpSet[fromUid] == true) {
                            pc.addIceCandidate(candidate)
                        } else {
                            pendingRoomCandidates.getOrPut(fromUid) { mutableListOf() }.add(candidate)
                        }
                    }
                }
                updateConnectedRoomCount()
            }
        }
    }

    private fun flushPendingRoomCandidates(peerId: String, pc: PeerConnection) {
        pendingRoomCandidates.remove(peerId)?.forEach { pc.addIceCandidate(it) }
    }

    private fun closeRoomPeer(peerId: String) {
        try {
            roomConnections.remove(peerId)?.apply {
                close()
                dispose()
            }
        } catch (_: Exception) {
        }
        roomRemoteSdpSet.remove(peerId)
        pendingRoomCandidates.remove(peerId)
        iceRestartAttempted.remove(peerId)
    }

    fun leaveRoomAudio() {
        audioStatsJob?.cancel()
        audioStatsJob = null
        activeSignalCallback = null
        isHostMutedLock = false
        try {
            localAudioTrack?.setEnabled(false)
        } catch (_: Exception) {
        }
        _isMicCapturing.value = false
        _localAudioLevel.value = 0f
        scope.launch {
            rtcMutex.withLock {
                roomConnections.keys.toList().forEach { closeRoomPeer(it) }
                processedSignalIds.clear()
                _connectedPeersCount.value = 0
                _connectedPeerIds.value = emptySet()
                _activeSpeakingPeerIds.value = emptySet()
                _localAudioLevel.value = 0f
                _isRoomCapacityReached.value = false
                _roomIceState.value = "idle"
                _lastError.value = null
                if (singleCallConnection == null) {
                    disposeLocalAudioCaptureLocked()
                    setAudioModeForCall(inCall = false, speakerOn = false)
                }
            }
        }
    }

    fun dispose() {
        audioStatsJob?.cancel()
        audioStatsJob = null
        activeSignalCallback = null
        isHostMutedLock = false
        try {
            audioManager?.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Throwable) {
        }
        try {
            roomConnections.keys.toList().forEach { closeRoomPeer(it) }
            endSingleCallOnly()
            disposeLocalAudioCaptureLocked()
            setAudioModeForCall(inCall = false, speakerOn = false)
            factory?.dispose()
            factory = null
            audioDeviceModule?.release()
            audioDeviceModule = null
        } catch (e: Exception) {
            Log.w(TAG, "WebRtcVoiceManager dispose warning: ${e.message}")
        }
        scope.cancel()
    }

    private fun updateConnectedRoomCount() {
        val connectedSet = roomConnections.entries.mapNotNull { (peerId, pc) ->
            val s = pc.iceConnectionState()
            if (s == PeerConnection.IceConnectionState.CONNECTED ||
                s == PeerConnection.IceConnectionState.COMPLETED
            ) peerId else null
        }.toSet()
        _connectedPeerIds.value = connectedSet
        _connectedPeersCount.value = connectedSet.size
        _roomIceState.value = when {
            connectedSet.isNotEmpty() -> "connected"
            roomConnections.isNotEmpty() -> "connecting"
            else -> "ready"
        }
    }

    private fun createPeerConnection(
        onIce: (sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit,
        onStateChange: (PeerConnection.IceConnectionState) -> Unit
    ): PeerConnection? {
        val f = factory ?: return null
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers).apply {
            sdpSemantics = PeerConnection.SdpSemantics.UNIFIED_PLAN
            continualGatheringPolicy = PeerConnection.ContinualGatheringPolicy.GATHER_CONTINUALLY
        }
        val pc = f.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(newState: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(newState: PeerConnection.IceConnectionState?) {
                if (newState != null) onStateChange(newState)
            }
            override fun onIceConnectionReceivingChange(receiving: Boolean) {}
            override fun onIceGatheringChange(newState: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                if (candidate != null) {
                    onIce(candidate.sdpMid ?: "0", candidate.sdpMLineIndex, candidate.sdp)
                }
            }
            override fun onIceCandidatesRemoved(candidates: Array<out IceCandidate>?) {}
            override fun onAddStream(stream: MediaStream?) {
                stream?.audioTracks?.forEach { remoteTrack ->
                    try {
                        remoteTrack.setEnabled(true)
                        remoteTrack.setVolume(1.0)
                    } catch (_: Exception) {
                    }
                }
            }
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {
                val track = receiver?.track()
                if (track is AudioTrack) {
                    try {
                        track.setEnabled(true)
                        track.setVolume(1.0)
                    } catch (_: Exception) {
                    }
                }
            }
        }) ?: return null

        val track = localAudioTrack
        if (track != null) {
            pc.addTrack(track, listOf("viberoom_stream"))
        } else {
            // Listeners or speakers without RECORD_AUDIO yet still need a RECV_ONLY audio
            // transceiver in Unified Plan so remote speaker audio is received and played.
            try {
                pc.addTransceiver(
                    MediaStreamTrack.MediaType.MEDIA_TYPE_AUDIO,
                    RtpTransceiver.RtpTransceiverInit(
                        RtpTransceiver.RtpTransceiverDirection.RECV_ONLY,
                        listOf("viberoom_stream")
                    )
                )
            } catch (e: Exception) {
                Log.w(TAG, "Failed adding RECV_ONLY audio transceiver: ${e.message}")
            }
        }
        return pc
    }

    private suspend fun createSdpOfferSuspending(
        pc: PeerConnection,
        constraints: MediaConstraints
    ): SessionDescription? = suspendCancellableCoroutine { cont ->
        pc.createOffer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (cont.isActive) cont.resume(desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.w(TAG, "SDP offer create error: $error")
                if (cont.isActive) cont.resume(null)
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private suspend fun createSdpAnswerSuspending(
        pc: PeerConnection,
        constraints: MediaConstraints
    ): SessionDescription? = suspendCancellableCoroutine { cont ->
        pc.createAnswer(object : SdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                if (cont.isActive) cont.resume(desc)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(error: String?) {
                Log.w(TAG, "SDP answer create error: $error")
                if (cont.isActive) cont.resume(null)
            }
            override fun onSetFailure(error: String?) {}
        }, constraints)
    }

    private suspend fun setLocalDescriptionSuspending(
        pc: PeerConnection,
        desc: SessionDescription
    ): Boolean = suspendCancellableCoroutine { cont ->
        pc.setLocalDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                if (cont.isActive) cont.resume(true)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.w(TAG, "SDP local set error: $error")
                if (cont.isActive) cont.resume(false)
            }
        }, desc)
    }

    private suspend fun setRemoteDescriptionSuspending(
        pc: PeerConnection,
        desc: SessionDescription
    ): Boolean = suspendCancellableCoroutine { cont ->
        pc.setRemoteDescription(object : SdpObserver {
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onSetSuccess() {
                if (cont.isActive) cont.resume(true)
            }
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(error: String?) {
                Log.w(TAG, "SDP remote set error: $error")
                if (cont.isActive) cont.resume(false)
            }
        }, desc)
    }

    private fun audioOnlyMediaConstraints(): MediaConstraints {
        return MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "false"))
        }
    }

    private open class SimpleSdpObserver : SdpObserver {
        override fun onCreateSuccess(desc: SessionDescription?) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(error: String?) {
            Log.w(TAG, "SDP create error: $error")
        }
        override fun onSetFailure(error: String?) {
            Log.w(TAG, "SDP set error: $error")
        }
    }
}
