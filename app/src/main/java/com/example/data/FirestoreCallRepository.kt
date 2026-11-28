package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreCallRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUid(): String =
        auth.currentUser?.uid ?: throw IllegalStateException("Authentication required for WebRTC signaling.")

    suspend fun createOutgoingCall(
        callId: String,
        calleeId: String,
        calleeName: String,
        callerName: String,
        callerFlag: String?,
        conversationId: String,
        offerSdp: String
    ): Result<String> {
        return try {
            val uid = requireUid()
            val payload = mutableMapOf<String, Any>(
                "callerId" to uid,
                "callerName" to callerName.take(100),
                "calleeId" to calleeId,
                "calleeName" to calleeName.take(100),
                "conversationId" to conversationId.take(128),
                "status" to "ringing",
                "offerSdp" to offerSdp.take(32000),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!callerFlag.isNullOrBlank()) {
                payload["callerFlag"] = callerFlag.take(10)
            }
            db.collection("calls").document(callId).set(payload).await()
            Result.success(callId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "calls/$callId")
            Result.failure(e)
        }
    }

    suspend fun answerCall(callId: String, answerSdp: String): Result<Unit> {
        return try {
            requireUid()
            db.collection("calls").document(callId).update(
                mapOf(
                    "status" to "connected",
                    "answerSdp" to answerSdp.take(32000),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "calls/$callId")
            Result.failure(e)
        }
    }

    suspend fun updateCallStatus(callId: String, status: String): Result<Unit> {
        return try {
            requireUid()
            db.collection("calls").document(callId).update(
                mapOf(
                    "status" to status,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "calls/$callId")
            Result.failure(e)
        }
    }

    suspend fun sendCallIceCandidate(
        callId: String,
        sdpMid: String,
        sdpMLineIndex: Int,
        sdp: String
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val payload = mapOf(
                "senderId" to uid,
                "sdpMid" to sdpMid.take(64),
                "sdpMLineIndex" to sdpMLineIndex,
                "sdp" to sdp.take(4000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("calls").document(callId).collection("candidates").add(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "calls/$callId/candidates")
            Result.failure(e)
        }
    }

    fun observeIncomingRingingCalls(uid: String): Flow<List<FirestoreVoiceCall>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("calls")
                .whereEqualTo("calleeId", uid)
                .whereEqualTo("status", "ringing")
                .snapshots()
                .map { snap -> snap.toObjects(FirestoreVoiceCall::class.java) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }

    fun observeCall(callId: String): Flow<FirestoreVoiceCall?> = flow {
        if (auth.currentUser == null) {
            emit(null)
            return@flow
        }
        emitAll(
            db.collection("calls").document(callId)
                .snapshots()
                .map { snap -> snap.toObject(FirestoreVoiceCall::class.java) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(null)
                }
        )
    }

    fun observeCallCandidates(callId: String): Flow<List<FirestoreIceCandidate>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("calls").document(callId).collection("candidates")
                .snapshots()
                .map { snap -> snap.toObjects(FirestoreIceCandidate::class.java) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }

    // --- Voice Room WebRTC Mesh Signaling ---

    suspend fun upsertRoomPeer(
        roomId: String,
        displayName: String,
        isSpeaker: Boolean,
        isMuted: Boolean,
        isHandRaised: Boolean = false,
        seatIndex: Int = -1,
        flagEmoji: String = "🌐",
        avatarFrame: String = "none",
        avatarColorHex: Long = 0xFF8C52FF,
        lastHeartbeatMs: Long = System.currentTimeMillis()
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val docRef = db.collection("voice_rooms").document(roomId)
                .collection("peers").document(uid)
            val existingSnap = try { docRef.get().await() } catch (_: Exception) { null }
            val existingHostMuted = existingSnap?.getBoolean("hostMuted") == true
            val safeMuted = if (existingHostMuted) true else isMuted

            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "displayName" to displayName.take(100),
                "isSpeaker" to isSpeaker,
                "isMuted" to safeMuted,
                "isHandRaised" to isHandRaised,
                "seatIndex" to seatIndex.coerceIn(-1, 5),
                "flagEmoji" to flagEmoji.take(10),
                "avatarFrame" to avatarFrame.take(40),
                "avatarColorHex" to avatarColorHex,
                "lastHeartbeatMs" to lastHeartbeatMs,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (existingSnap == null || !existingSnap.exists()) {
                payload["hostMuted"] = false
                docRef.set(payload).await()
            } else {
                docRef.set(payload, com.google.firebase.firestore.SetOptions.merge()).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "voice_rooms/$roomId/peers")
            Result.failure(e)
        }
    }

    suspend fun heartbeatRoomPeer(
        roomId: String,
        lastHeartbeatMs: Long = System.currentTimeMillis()
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(uid)
                .update(
                    mapOf(
                        "lastHeartbeatMs" to lastHeartbeatMs,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun muteOrRestrictPeer(
        roomId: String,
        targetUid: String,
        hostMuted: Boolean
    ): Result<Unit> {
        return try {
            requireUid()
            val updates = if (hostMuted) {
                mapOf(
                    "isMuted" to true,
                    "hostMuted" to true,
                    "isHandRaised" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            } else {
                // Only lift the host-mute restriction; never force-unmute another user's microphone.
                mapOf(
                    "hostMuted" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            }
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(targetUid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId/peers/$targetUid")
            Result.failure(e)
        }
    }

    suspend fun setPeerStageState(
        roomId: String,
        targetUid: String,
        isSpeaker: Boolean,
        seatIndex: Int,
        isHandRaised: Boolean = false
    ): Result<Unit> {
        return try {
            requireUid()
            val updates = mutableMapOf<String, Any>(
                "isSpeaker" to isSpeaker,
                "seatIndex" to (if (isSpeaker) seatIndex.coerceIn(0, 5) else -1),
                "isHandRaised" to isHandRaised,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!isSpeaker) {
                updates["isMuted"] = true
            }
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(targetUid)
                .update(updates)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId/peers/$targetUid")
            Result.failure(e)
        }
    }

    suspend fun moderateRoomPeer(
        roomId: String,
        targetUid: String,
        isSpeaker: Boolean,
        isMuted: Boolean,
        isHandRaised: Boolean = false,
        seatIndex: Int? = null
    ): Result<Unit> {
        return try {
            requireUid()
            val updates = mutableMapOf<String, Any>(
                "isSpeaker" to isSpeaker,
                "isHandRaised" to isHandRaised,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (isMuted) {
                updates["isMuted"] = true
            }
            if (seatIndex != null) {
                updates["seatIndex"] = seatIndex.coerceIn(-1, 5)
            }
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(targetUid)
                .update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId/peers/$targetUid")
            Result.failure(e)
        }
    }

    suspend fun removeRoomPeer(roomId: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.success(Unit)
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(uid)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoomPeers(roomId: String): Flow<List<FirestoreRoomPeer>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("peers")
                .snapshots()
                .map { snap -> snap.toObjects(FirestoreRoomPeer::class.java) }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }

    suspend fun sendRoomSignal(
        roomId: String,
        toUserId: String,
        type: String,
        sdp: String,
        sdpMid: String? = null,
        sdpMLineIndex: Int? = null
    ): Result<Unit> {
        return try {
            val uid = requireUid()
            val payload = mutableMapOf<String, Any>(
                "fromUserId" to uid,
                "toUserId" to toUserId,
                "type" to type,
                "sdp" to sdp.take(32000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (sdpMid != null) payload["sdpMid"] = sdpMid
            if (sdpMLineIndex != null) payload["sdpMLineIndex"] = sdpMLineIndex
            db.collection("voice_rooms").document(roomId).collection("signals")
                .add(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fe = e as? FirebaseFirestoreException
            if (fe?.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                handleFirestoreError(e, OperationType.CREATE, "voice_rooms/$roomId/signals")
            }
            Result.failure(e)
        }
    }

    fun observeRoomSignalsForUser(roomId: String, myUid: String): Flow<List<FirestoreRoomSignal>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val typeOrder = mapOf("offer" to 0, "answer" to 1, "ice" to 2)
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("signals")
                .whereEqualTo("toUserId", myUid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FirestoreRoomSignal::class.java)
                        .sortedWith(
                            compareBy<FirestoreRoomSignal>(
                                { it.createdAt?.toDate()?.time ?: Long.MAX_VALUE },
                                { typeOrder[it.type] ?: 3 }
                            )
                        )
                }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }

    suspend fun clearRoomSignalsForUser(roomId: String, myUid: String) {
        if (auth.currentUser == null || myUid.isBlank() || roomId.isBlank()) return
        val signalsRef = db.collection("voice_rooms").document(roomId).collection("signals")
        try {
            val incoming = signalsRef.whereEqualTo("toUserId", myUid).get().await()
            incoming.documents.forEach { doc ->
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
        try {
            val outgoing = signalsRef.whereEqualTo("fromUserId", myUid).get().await()
            outgoing.documents.forEach { doc ->
                try { doc.reference.delete().await() } catch (_: Exception) {}
            }
        } catch (_: Exception) {
        }
    }

    suspend fun deleteRoomSignal(roomId: String, signalId: String) {
        if (signalId.isBlank()) return
        try {
            db.collection("voice_rooms").document(roomId)
                .collection("signals").document(signalId)
                .delete().await()
        } catch (_: Exception) {
        }
    }
}
