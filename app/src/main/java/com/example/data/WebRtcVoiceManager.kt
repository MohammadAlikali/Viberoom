package com.example.data

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.webrtc.AudioSource
import org.webrtc.AudioTrack
import org.webrtc.DataChannel
import org.webrtc.IceCandidate
import org.webrtc.MediaConstraints
import org.webrtc.MediaStream
import org.webrtc.PeerConnection
import org.webrtc.PeerConnectionFactory
import org.webrtc.RtpReceiver
import org.webrtc.SdpObserver
import org.webrtc.SessionDescription
import org.webrtc.audio.JavaAudioDeviceModule
import java.util.concurrent.ConcurrentHashMap

class WebRtcVoiceManager(private val appContext: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun2.l.google.com:19302").createIceServer()
    )

    private var factory: PeerConnectionFactory? = null
    private var localAudioSource: AudioSource? = null
    private var localAudioTrack: AudioTrack? = null
    private var singleCallConnection: PeerConnection? = null
    private val roomConnections = ConcurrentHashMap<String, PeerConnection>()
    private val pendingRoomCandidates = ConcurrentHashMap<String, MutableList<IceCandidate>>()
    private val pendingCallCandidates = mutableListOf<IceCandidate>()
    private var isRemoteSdpSetForSingleCall = false
    private val roomRemoteSdpSet = ConcurrentHashMap<String, Boolean>()

    private val _connectedPeersCount = MutableStateFlow(0)
    val connectedPeersCount: StateFlow<Int> = _connectedPeersCount.asStateFlow()

    private val _connectedPeerIds = MutableStateFlow<Set<String>>(emptySet())
    val connectedPeerIds: StateFlow<Set<String>> = _connectedPeerIds.asStateFlow()

    private val _roomIceState = MutableStateFlow("idle")
    val roomIceState: StateFlow<String> = _roomIceState.asStateFlow()

    private val _isCallMediaConnected = MutableStateFlow(false)
    val isCallMediaConnected: StateFlow<Boolean> = _isCallMediaConnected.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    @Synchronized
    private fun ensureInitialized(): Boolean {
        if (factory != null && localAudioTrack != null) return true
        return try {
            PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(appContext)
                    .setEnableInternalTracer(false)
                    .createInitializationOptions()
            )
            val audioDeviceModule = JavaAudioDeviceModule.builder(appContext)
                .setUseHardwareAcousticEchoCanceler(true)
                .setUseHardwareNoiseSuppressor(true)
                .createAudioDeviceModule()

            val createdFactory = PeerConnectionFactory.builder()
                .setAudioDeviceModule(audioDeviceModule)
                .createPeerConnectionFactory()

            val audioConstraints = MediaConstraints().apply {
                mandatory.add(MediaConstraints.KeyValuePair("googEchoCancellation", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googNoiseSuppression", "true"))
                mandatory.add(MediaConstraints.KeyValuePair("googAutoGainControl", "true"))
            }
            val source = createdFactory.createAudioSource(audioConstraints)
            val track = createdFactory.createAudioTrack("viberoom_audio_0", source).apply {
                setEnabled(true)
            }
            factory = createdFactory
            localAudioSource = source
            localAudioTrack = track
            true
        } catch (t: Throwable) {
            Log.w("WebRtcVoiceManager", "WebRTC init unavailable on this runtime: ${t.message}")
            _lastError.value = "Audio engine unavailable on this device."
            false
        }
    }

    fun setAudioModeForCall(inCall: Boolean, speakerOn: Boolean) {
        val am = audioManager ?: return
        try {
            if (inCall) {
                am.mode = AudioManager.MODE_IN_COMMUNICATION
                setSpeakerphoneEnabled(speakerOn)
            } else {
                setSpeakerphoneEnabled(false)
                am.mode = AudioManager.MODE_NORMAL
            }
        } catch (_: Exception) {
        }
    }

    fun setSpeakerphoneEnabled(enabled: Boolean) {
        val am = audioManager ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (enabled) {
                    val speaker = am.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                    if (speaker != null) am.setCommunicationDevice(speaker)
                } else {
                    val earpiece = am.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                    if (earpiece != null) am.setCommunicationDevice(earpiece) else am.clearCommunicationDevice()
                }
            } else {
                @Suppress("DEPRECATION")
                am.isSpeakerphoneOn = enabled
            }
        } catch (_: Exception) {
        }
    }

    fun setMicEnabled(enabled: Boolean) {
        localAudioTrack?.setEnabled(enabled)
    }

    // --- 1-to-1 Voice Call ---

    fun startOutgoingCall(
        onOfferCreated: (sdp: String) -> Unit,
        onLocalIceCandidate: (sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            endSingleCallOnly()
            if (!ensureInitialized()) return@launch
            setAudioModeForCall(inCall = true, speakerOn = false)
            localAudioTrack?.setEnabled(true)
            isRemoteSdpSetForSingleCall = false
            pendingCallCandidates.clear()

            val pc = createPeerConnection(
                onIce = onLocalIceCandidate,
                onStateChange = { state ->
                    _isCallMediaConnected.value = (state == PeerConnection.IceConnectionState.CONNECTED ||
                        state == PeerConnection.IceConnectionState.COMPLETED)
                }
            ) ?: return@launch
            singleCallConnection = pc

            pc.createOffer(object : SimpleSdpObserver() {
                override fun onCreateSuccess(desc: SessionDescription?) {
                    if (desc == null) return
                    pc.setLocalDescription(SimpleSdpObserver(), desc)
                    onOfferCreated(desc.description)
                }
            }, audioOnlyMediaConstraints())
        }
    }

    fun acceptIncomingCall(
        offerSdp: String,
        onAnswerCreated: (sdp: String) -> Unit,
        onLocalIceCandidate: (sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            endSingleCallOnly()
            if (!ensureInitialized()) return@launch
            setAudioModeForCall(inCall = true, speakerOn = false)
            localAudioTrack?.setEnabled(true)
            isRemoteSdpSetForSingleCall = false

            val pc = createPeerConnection(
                onIce = onLocalIceCandidate,
                onStateChange = { state ->
                    _isCallMediaConnected.value = (state == PeerConnection.IceConnectionState.CONNECTED ||
                        state == PeerConnection.IceConnectionState.COMPLETED)
                }
            ) ?: return@launch
            singleCallConnection = pc

            val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, offerSdp)
            pc.setRemoteDescription(object : SimpleSdpObserver() {
                override fun onSetSuccess() {
                    isRemoteSdpSetForSingleCall = true
                    synchronized(pendingCallCandidates) {
                        pendingCallCandidates.forEach { pc.addIceCandidate(it) }
                        pendingCallCandidates.clear()
                    }
                    pc.createAnswer(object : SimpleSdpObserver() {
                        override fun onCreateSuccess(answerDesc: SessionDescription?) {
                            if (answerDesc == null) return
                            pc.setLocalDescription(SimpleSdpObserver(), answerDesc)
                            onAnswerCreated(answerDesc.description)
                        }
                    }, audioOnlyMediaConstraints())
                }
            }, remoteDesc)
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
        endSingleCallOnly()
        if (roomConnections.isEmpty()) {
            setAudioModeForCall(inCall = false, speakerOn = false)
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

    // --- Voice Room Multi-Peer WebRTC Mesh Audio ---

    fun syncRoomPeers(
        myUid: String,
        remotePeerIds: List<String>,
        micEnabled: Boolean,
        onSignalToPeer: (toUid: String, type: String, sdp: String, sdpMid: String?, sdpMLineIndex: Int?) -> Unit
    ) {
        scope.launch {
            if (!ensureInitialized()) {
                _roomIceState.value = "ready_local"
                return@launch
            }
            setAudioModeForCall(inCall = true, speakerOn = true)
            localAudioTrack?.setEnabled(micEnabled)
            if (remotePeerIds.isEmpty()) {
                _roomIceState.value = "ready"
            } else if (_connectedPeerIds.value.isEmpty()) {
                _roomIceState.value = "connecting"
            }

            val remoteSet = remotePeerIds.filter { it != myUid && it.isNotBlank() }.take(6).toSet()
            val removed = roomConnections.keys.filter { it !in remoteSet }
            removed.forEach { peerId ->
                closeRoomPeer(peerId)
            }

            remoteSet.forEach { peerId ->
                if (!roomConnections.containsKey(peerId)) {
                    val pc = createPeerConnection(
                        onIce = { mid, idx, candSdp ->
                            onSignalToPeer(peerId, "ice", candSdp, mid, idx)
                        },
                        onStateChange = { updateConnectedRoomCount() }
                    )
                    if (pc != null) {
                        roomConnections[peerId] = pc
                        roomRemoteSdpSet[peerId] = false
                        // Deterministic initiator avoids SDP glare between two peers
                        if (myUid < peerId) {
                            pc.createOffer(object : SimpleSdpObserver() {
                                override fun onCreateSuccess(desc: SessionDescription?) {
                                    if (desc == null) return
                                    pc.setLocalDescription(SimpleSdpObserver(), desc)
                                    onSignalToPeer(peerId, "offer", desc.description, null, null)
                                }
                            }, audioOnlyMediaConstraints())
                        }
                    }
                }
            }
            updateConnectedRoomCount()
        }
    }

    fun handleRoomSignal(
        signal: FirestoreRoomSignal,
        onAnswerToPeer: (toUid: String, sdp: String) -> Unit,
        onIceToPeer: (toUid: String, sdpMid: String, sdpMLineIndex: Int, sdp: String) -> Unit
    ) {
        scope.launch {
            if (!ensureInitialized()) return@launch
            val fromUid = signal.fromUserId
            if (fromUid.isBlank()) return@launch

            val pc = roomConnections[fromUid] ?: createPeerConnection(
                onIce = { mid, idx, candSdp -> onIceToPeer(fromUid, mid, idx, candSdp) },
                onStateChange = { updateConnectedRoomCount() }
            )?.also {
                roomConnections[fromUid] = it
                roomRemoteSdpSet[fromUid] = false
            } ?: return@launch

            when (signal.type) {
                "offer" -> {
                    val remoteDesc = SessionDescription(SessionDescription.Type.OFFER, signal.sdp)
                    pc.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            roomRemoteSdpSet[fromUid] = true
                            flushPendingRoomCandidates(fromUid, pc)
                            pc.createAnswer(object : SimpleSdpObserver() {
                                override fun onCreateSuccess(answerDesc: SessionDescription?) {
                                    if (answerDesc == null) return
                                    pc.setLocalDescription(SimpleSdpObserver(), answerDesc)
                                    onAnswerToPeer(fromUid, answerDesc.description)
                                }
                            }, audioOnlyMediaConstraints())
                        }
                    }, remoteDesc)
                }
                "answer" -> {
                    val remoteDesc = SessionDescription(SessionDescription.Type.ANSWER, signal.sdp)
                    pc.setRemoteDescription(object : SimpleSdpObserver() {
                        override fun onSetSuccess() {
                            roomRemoteSdpSet[fromUid] = true
                            flushPendingRoomCandidates(fromUid, pc)
                        }
                    }, remoteDesc)
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
    }

    fun leaveRoomAudio() {
        roomConnections.keys.toList().forEach { closeRoomPeer(it) }
        _connectedPeersCount.value = 0
        _connectedPeerIds.value = emptySet()
        _roomIceState.value = "idle"
        if (singleCallConnection == null) {
            setAudioModeForCall(inCall = false, speakerOn = false)
        }
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
            override fun onAddStream(stream: MediaStream?) {}
            override fun onRemoveStream(stream: MediaStream?) {}
            override fun onDataChannel(channel: DataChannel?) {}
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(receiver: RtpReceiver?, mediaStreams: Array<out MediaStream>?) {}
        }) ?: return null

        localAudioTrack?.let { track ->
            pc.addTrack(track, listOf("viberoom_stream"))
        }
        return pc
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
            Log.w("WebRtcVoiceManager", "SDP create error: $error")
        }
        override fun onSetFailure(error: String?) {
            Log.w("WebRtcVoiceManager", "SDP set error: $error")
        }
    }
}
