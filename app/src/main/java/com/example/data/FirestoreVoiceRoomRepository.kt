package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.RoomCategory
import com.example.model.RoomLifecycleState
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.model.VoiceRoomLiveEvent
import com.example.model.VoiceRoomPoll
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreVoiceRoomRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    companion object {
        private const val STALE_ROOM_TTL_MS = 15L * 60L * 1000L
        private const val HEARTBEAT_STALE_MS = 120_000L
        private const val PEER_STALE_MS = 75_000L
    }

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to interact with voice rooms.")
    }

    private fun mapToDomainRoom(firestoreRoom: FirestoreVoiceRoom): VoiceRoom {
        val cat = try {
            RoomCategory.valueOf(firestoreRoom.category)
        } catch (_: Exception) {
            RoomCategory.ALL
        }
        val hostProfile = UserProfile(
            id = firestoreRoom.hostId,
            name = firestoreRoom.hostName.ifBlank { "Host" },
            username = firestoreRoom.hostName.lowercase().replace(" ", "_").ifBlank { "host" },
            bio = firestoreRoom.description.ifBlank { "Voice stage host" },
            countryName = firestoreRoom.hostCountry ?: "Worldwide",
            countryCode = "GL",
            flagEmoji = firestoreRoom.hostFlagEmoji ?: "🌐",
            avatarColorHex = firestoreRoom.hostAvatarColor?.toLongOrNull() ?: 0xFF6C5CE7
        )
        val lifecycle = RoomLifecycleState.fromString(firestoreRoom.lifecycleState)
        val updatedMs = firestoreRoom.updatedAt?.toDate()?.time ?: 0L

        return VoiceRoom(
            id = firestoreRoom.id,
            title = firestoreRoom.title,
            description = firestoreRoom.description,
            category = cat,
            host = hostProfile,
            speakers = listOf(hostProfile),
            listeners = emptyList(),
            activeSpeakerId = firestoreRoom.activeSpeakerId,
            listenersCount = firestoreRoom.listenersCount.toInt().coerceAtLeast(0),
            isLive = firestoreRoom.isLive,
            tags = firestoreRoom.tags,
            isPrivate = firestoreRoom.isPrivate,
            coHostIds = firestoreRoom.coHostIds.toSet(),
            aiSummary = firestoreRoom.aiSummary,
            lifecycleState = lifecycle,
            backgroundTheme = firestoreRoom.backgroundTheme.ifBlank { "aurora" },
            isStageLocked = firestoreRoom.isStageLocked,
            moderatorIds = firestoreRoom.moderatorIds.toSet(),
            bannedIds = firestoreRoom.bannedIds.toSet(),
            invitedSpeakerIds = firestoreRoom.invitedSpeakerIds.toSet(),
            successorHostId = firestoreRoom.successorHostId,
            pinnedNotice = firestoreRoom.pinnedNotice,
            maxAudioParticipants = firestoreRoom.maxAudioParticipants.toInt().coerceIn(2, 6),
            updatedAtMs = updatedMs,
            temporaryBanExpiries = firestoreRoom.temporaryBans,
            hostLastHeartbeatMs = firestoreRoom.hostLastHeartbeatMs
        )
    }

    /**
     * Real-time Flow of all currently discoverable live voice rooms from Firestore.
     * Filters out ENDED/EXPIRED/stale rooms using host heartbeat liveness and updatedAt fallback.
     */
    fun observeActiveVoiceRooms(): Flow<List<VoiceRoom>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "voice_rooms"
        emitAll(
            db.collection(path)
                .whereEqualTo("isLive", true)
                .snapshots()
                .map { snapshot ->
                    val now = System.currentTimeMillis()
                    snapshot.toObjects(FirestoreVoiceRoom::class.java)
                        .map { mapToDomainRoom(it) }
                        .filter { room ->
                            val isHeartbeatStale = room.hostLastHeartbeatMs > 0L &&
                                (now - room.hostLastHeartbeatMs) > HEARTBEAT_STALE_MS
                            val isLegacyStale = room.hostLastHeartbeatMs == 0L &&
                                room.updatedAtMs > 0L &&
                                (now - room.updatedAtMs) > STALE_ROOM_TTL_MS
                            !isHeartbeatStale && !isLegacyStale && room.lifecycleState.isDiscoverable()
                        }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser != null) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    if (auth.currentUser == null) {
                        emit(emptyList())
                    } else {
                        throw error
                    }
                }
        )
    }

    fun observeSingleVoiceRoom(roomId: String): Flow<VoiceRoom?> = flow {
        if (auth.currentUser == null || roomId.isBlank()) {
            emit(null)
            return@flow
        }
        emitAll(
            db.collection("voice_rooms").document(roomId)
                .snapshots()
                .map { snap ->
                    val obj = snap.toObject(FirestoreVoiceRoom::class.java) ?: return@map null
                    mapToDomainRoom(obj)
                }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(null)
                }
        )
    }

    suspend fun expireStaleHostRooms(hostUid: String, expireAllLive: Boolean = false) {
        if (auth.currentUser == null || hostUid.isBlank()) return
        try {
            val now = System.currentTimeMillis()
            val snap = db.collection("voice_rooms")
                .whereEqualTo("hostId", hostUid)
                .whereEqualTo("isLive", true)
                .get()
                .await()
            snap.toObjects(FirestoreVoiceRoom::class.java).forEach { room ->
                val hb = room.hostLastHeartbeatMs
                val updated = room.updatedAt?.toDate()?.time ?: 0L
                val isStale = if (expireAllLive) {
                    true
                } else if (hb > 0L) {
                    (now - hb) > HEARTBEAT_STALE_MS
                } else {
                    updated > 0L && (now - updated) > HEARTBEAT_STALE_MS
                }
                if (isStale) {
                    endVoiceRoom(room.id, RoomLifecycleState.EXPIRED)
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("VoiceRoomRepo", "Stale host room cleanup skipped: ${e.message}")
        }
    }

    /**
     * Creates a new live voice room in Firestore.
     */
    suspend fun createVoiceRoom(
        title: String,
        description: String,
        category: RoomCategory,
        hostUser: UserProfile,
        tags: List<String>,
        isPrivate: Boolean = false,
        backgroundTheme: String = "aurora",
        isStageLocked: Boolean = false
    ): VoiceRoom {
        val uid = requireUserId()
        val nowMs = System.currentTimeMillis()
        val docRef = db.collection("voice_rooms").document()
        val roomId = docRef.id

        val payload = mapOf(
            "title" to title.trim(),
            "description" to description.trim(),
            "category" to category.name,
            "hostId" to uid,
            "hostName" to hostUser.name.ifBlank { "Host" },
            "hostCountry" to hostUser.countryName,
            "hostFlagEmoji" to hostUser.flagEmoji,
            "hostAvatarColor" to hostUser.avatarColorHex.toString(),
            "isLive" to true,
            "listenersCount" to 1L,
            "speakersCount" to 1L,
            "activeSpeakerId" to uid,
            "tags" to tags,
            "isPrivate" to isPrivate,
            "coHostIds" to emptyList<String>(),
            "lifecycleState" to RoomLifecycleState.LIVE.name,
            "backgroundTheme" to backgroundTheme.ifBlank { "aurora" },
            "isStageLocked" to isStageLocked,
            "moderatorIds" to emptyList<String>(),
            "bannedIds" to emptyList<String>(),
            "invitedSpeakerIds" to emptyList<String>(),
            "temporaryBans" to emptyMap<String, Long>(),
            "maxAudioParticipants" to WebRtcVoiceManager.MAX_AUDIO_PARTICIPANTS.toLong(),
            "hostLastHeartbeatMs" to nowMs,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )

        try {
            docRef.set(payload).await()
            Log.d("VoiceRoomRepo", "Successfully created Firestore voice room: $roomId")
            return VoiceRoom(
                id = roomId,
                title = title,
                description = description,
                category = category,
                host = hostUser,
                speakers = listOf(hostUser),
                listeners = emptyList(),
                activeSpeakerId = uid,
                listenersCount = 1,
                isLive = true,
                tags = tags,
                isPrivate = isPrivate,
                lifecycleState = RoomLifecycleState.LIVE,
                backgroundTheme = backgroundTheme.ifBlank { "aurora" },
                isStageLocked = isStageLocked,
                maxAudioParticipants = WebRtcVoiceManager.MAX_AUDIO_PARTICIPANTS,
                updatedAtMs = nowMs,
                hostLastHeartbeatMs = nowMs,
                seatAssignments = mapOf(0 to uid)
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun sendHostHeartbeat(
        roomId: String,
        activeListenersCount: Int? = null,
        activeSpeakersCount: Int? = null
    ) {
        if (auth.currentUser == null || roomId.isBlank()) return
        try {
            val updates = mutableMapOf<String, Any>(
                "hostLastHeartbeatMs" to System.currentTimeMillis(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (activeListenersCount != null) {
                updates["listenersCount"] = activeListenersCount.coerceAtLeast(1).toLong()
            }
            if (activeSpeakersCount != null) {
                updates["speakersCount"] = activeSpeakersCount.coerceIn(1, 6).toLong()
            }
            db.collection("voice_rooms").document(roomId).update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("VoiceRoomRepo", "Host heartbeat update failed: ${e.message}")
        }
    }

    suspend fun claimStageSeat(
        roomId: String,
        requestedSeatIndex: Int,
        isStageLocked: Boolean,
        isModeratorOrInvited: Boolean
    ): Result<Int> {
        return try {
            val uid = requireUserId()
            val roomSnap = db.collection("voice_rooms").document(roomId).get().await()
            val roomIsLive = roomSnap.exists() && roomSnap.getBoolean("isLive") == true &&
                RoomLifecycleState.fromString(roomSnap.getString("lifecycleState")).isDiscoverable()
            if (!roomIsLive) {
                return Result.failure(IllegalStateException("Voice room has already ended."))
            }
            val effectiveStageLocked = roomSnap.getBoolean("isStageLocked") ?: isStageLocked
            if (effectiveStageLocked && !isModeratorOrInvited) {
                return Result.failure(IllegalStateException("Stage is locked by host."))
            }
            val now = System.currentTimeMillis()
            val peersRef = db.collection("voice_rooms").document(roomId).collection("peers")
            val peersSnap = peersRef.get().await()
            val allPeers = peersSnap.toObjects(FirestoreRoomPeer::class.java)
            val activeSpeakers = allPeers.filter { peer ->
                val pid = peer.userId.ifBlank { peer.id }
                val alive = peer.lastHeartbeatMs == 0L || (now - peer.lastHeartbeatMs) <= PEER_STALE_MS
                alive && peer.isSpeaker && pid != uid
            }
            if (activeSpeakers.size >= WebRtcVoiceManager.MAX_AUDIO_PARTICIPANTS) {
                return Result.failure(IllegalStateException("All 6 microphone seats are full."))
            }
            val takenSeats = activeSpeakers.mapNotNull { p -> p.seatIndex.takeIf { it in 0..5 } }.toSet()
            val targetSeat = if (requestedSeatIndex in 0..5 && requestedSeatIndex !in takenSeats) {
                requestedSeatIndex
            } else {
                (0..5).firstOrNull { it !in takenSeats }
                    ?: return Result.failure(IllegalStateException("All 6 microphone seats are full."))
            }
            peersRef.document(uid).update(
                mapOf(
                    "isSpeaker" to true,
                    "seatIndex" to targetSeat,
                    "isHandRaised" to false,
                    "lastHeartbeatMs" to now,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Post-write re-read to resolve concurrent seat/capacity races cleanly
            val verifySnap = peersRef.get().await()
            val verifyPeers = verifySnap.toObjects(FirestoreRoomPeer::class.java).filter { peer ->
                val alive = peer.lastHeartbeatMs == 0L || (now - peer.lastHeartbeatMs) <= PEER_STALE_MS
                alive && peer.isSpeaker
            }
            val hostId = roomSnap.getString("hostId").orEmpty()
            val sameSeatPeers = verifyPeers.filter { it.seatIndex == targetSeat }
                .sortedWith(compareByDescending<FirestoreRoomPeer> {
                    (it.userId.ifBlank { it.id }) == hostId
                }.thenBy { it.userId.ifBlank { it.id } })
            val lostSeatCollision = sameSeatPeers.size > 1 &&
                (sameSeatPeers.first().userId.ifBlank { sameSeatPeers.first().id }) != uid
            val exceededMaxCapacity = verifyPeers.size > WebRtcVoiceManager.MAX_AUDIO_PARTICIPANTS &&
                verifyPeers.sortedWith(compareByDescending<FirestoreRoomPeer> {
                    (it.userId.ifBlank { it.id }) == hostId
                }.thenBy { it.seatIndex }.thenBy { it.userId.ifBlank { it.id } })
                    .take(WebRtcVoiceManager.MAX_AUDIO_PARTICIPANTS)
                    .none { (it.userId.ifBlank { it.id }) == uid }
            if (lostSeatCollision || exceededMaxCapacity) {
                peersRef.document(uid).update(
                    mapOf(
                        "isSpeaker" to false,
                        "isMuted" to true,
                        "seatIndex" to -1,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                return Result.failure(IllegalStateException("Seat was just claimed by another speaker."))
            }
            Result.success(targetSeat)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Atomically verifies the room is still live/discoverable and increments listener count.
     */
    suspend fun joinVoiceRoom(roomId: String): Result<Unit> {
        if (auth.currentUser == null) return Result.failure(IllegalStateException("Not authenticated"))
        val docRef = db.collection("voice_rooms").document(roomId)
        return try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val isLive = snapshot.exists() && snapshot.getBoolean("isLive") == true
                val lifecycle = RoomLifecycleState.fromString(snapshot.getString("lifecycleState"))
                if (!isLive || !lifecycle.isDiscoverable()) {
                    throw IllegalStateException("Voice room has already ended.")
                }
                val currentCount = snapshot.getLong("listenersCount") ?: 0L
                transaction.update(
                    docRef,
                    mapOf(
                        "listenersCount" to currentCount + 1L,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            if (e !is IllegalStateException) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            }
            Log.w("VoiceRoomRepo", "Failed to join voice room $roomId: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Decrements listener count when leaving a live room, clamping at 0 and skipping already-ended rooms.
     */
    suspend fun leaveVoiceRoom(roomId: String) {
        if (auth.currentUser == null) return
        val docRef = db.collection("voice_rooms").document(roomId)
        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                if (!snapshot.exists() || snapshot.getBoolean("isLive") != true) {
                    return@runTransaction
                }
                val currentCount = snapshot.getLong("listenersCount") ?: 0L
                val nextCount = (currentCount - 1L).coerceAtLeast(0L)
                transaction.update(
                    docRef,
                    mapOf(
                        "listenersCount" to nextCount,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }.await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Log.w("VoiceRoomRepo", "Failed to decrement listenersCount: ${e.message}")
        }
    }

    suspend fun updateCoHosts(roomId: String, coHostIds: List<String>): Result<Unit> {
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId).update(
                mapOf(
                    "coHostIds" to coHostIds.distinct().take(10),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun updateRoomConfiguration(
        roomId: String,
        backgroundTheme: String? = null,
        isStageLocked: Boolean? = null,
        pinnedNotice: String? = null,
        clearPinnedNotice: Boolean = false,
        successorHostId: String? = null,
        lifecycleState: RoomLifecycleState? = null
    ): Result<Unit> {
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (backgroundTheme != null) updates["backgroundTheme"] = backgroundTheme.take(40)
            if (isStageLocked != null) updates["isStageLocked"] = isStageLocked
            if (clearPinnedNotice) {
                updates["pinnedNotice"] = null
            } else if (pinnedNotice != null) {
                updates["pinnedNotice"] = pinnedNotice.trim().take(300)
            }
            if (successorHostId != null) {
                updates["successorHostId"] = successorHostId.ifBlank { null }
            }
            if (lifecycleState != null) {
                updates["lifecycleState"] = lifecycleState.name
                updates["isLive"] = lifecycleState.isDiscoverable()
            }
            db.collection("voice_rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun updateRoomRoles(
        roomId: String,
        coHostIds: List<String>? = null,
        moderatorIds: List<String>? = null,
        bannedIds: List<String>? = null,
        invitedSpeakerIds: List<String>? = null
    ): Result<Unit> {
        return try {
            requireUserId()
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (coHostIds != null) updates["coHostIds"] = coHostIds.distinct().take(10)
            if (moderatorIds != null) updates["moderatorIds"] = moderatorIds.distinct().take(20)
            if (bannedIds != null) updates["bannedIds"] = bannedIds.distinct().take(100)
            if (invitedSpeakerIds != null) updates["invitedSpeakerIds"] = invitedSpeakerIds.distinct().take(30)
            db.collection("voice_rooms").document(roomId).update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun muteAllSpeakers(roomId: String, speakerUids: List<String>, hostUid: String): Result<Unit> {
        return try {
            requireUserId()
            val targets = speakerUids.filter { it.isNotBlank() && it != hostUid }
            if (targets.isEmpty()) return Result.success(Unit)
            val batch = db.batch()
            val peersRef = db.collection("voice_rooms").document(roomId).collection("peers")
            targets.forEach { targetUid ->
                batch.update(
                    peersRef.document(targetUid),
                    mapOf(
                        "isMuted" to true,
                        "hostMuted" to true,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId/peers")
            Result.failure(e)
        }
    }

    suspend fun kickPeerFromRoom(roomId: String, targetUid: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId)
                .collection("peers").document(targetUid)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun applyTemporaryOrPermanentBan(
        roomId: String,
        targetUid: String,
        currentBannedIds: Set<String>,
        banDurationMs: Long?
    ): Result<Unit> {
        return try {
            requireUserId()
            try {
                db.collection("voice_rooms").document(roomId)
                    .collection("peers").document(targetUid)
                    .delete().await()
            } catch (_: Exception) {
            }
            val roomRef = db.collection("voice_rooms").document(roomId)
            if (banDurationMs == null) {
                roomRef.update(
                    mapOf(
                        "bannedIds" to (currentBannedIds + targetUid).distinct().take(100),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                val expiresAt = System.currentTimeMillis() + banDurationMs
                roomRef.update(
                    mapOf(
                        "temporaryBans.$targetUid" to expiresAt,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun transferHostAndLeave(
        roomId: String,
        newHostId: String,
        newHostName: String,
        newHostCountry: String?,
        newHostFlag: String?
    ): Result<Unit> {
        return try {
            requireUserId()
            val docRef = db.collection("voice_rooms").document(roomId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
                val currentCount = snapshot.getLong("listenersCount") ?: 1L
                val nextCount = (currentCount - 1L).coerceAtLeast(1L)
                val updates = mutableMapOf<String, Any?>(
                    "hostId" to newHostId,
                    "hostName" to newHostName.ifBlank { "Host" }.take(100),
                    "hostCountry" to newHostCountry,
                    "hostFlagEmoji" to newHostFlag,
                    "successorHostId" to null,
                    "listenersCount" to nextCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                transaction.update(docRef, updates)
            }.await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun saveRoomSummary(roomId: String, summary: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId).update(
                mapOf(
                    "aiSummary" to summary.trim().take(1500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId")
            Result.failure(e)
        }
    }

    suspend fun endVoiceRoom(roomId: String, state: RoomLifecycleState = RoomLifecycleState.ENDED) {
        if (auth.currentUser == null) return
        val docRef = db.collection("voice_rooms").document(roomId)
        try {
            // Transition through ENDING -> ENDED / EXPIRED and clear active peers/events authored by host
            docRef.update(
                mapOf(
                    "lifecycleState" to RoomLifecycleState.ENDING.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            docRef.update(
                mapOf(
                    "isLive" to false,
                    "lifecycleState" to state.name,
                    "listenersCount" to 0L,
                    "speakersCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            try {
                val peersSnap = docRef.collection("peers").get().await()
                val batch = db.batch()
                peersSnap.documents.forEach { batch.delete(it.reference) }
                if (!peersSnap.isEmpty) batch.commit().await()
            } catch (_: Exception) {
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Log.w("VoiceRoomRepo", "Failed to end voice room: ${e.message}")
        }
    }

    // --- Live Room Events (Entrances, Gifts, Soundboard, Chat, System) ---

    suspend fun sendRoomEvent(event: VoiceRoomLiveEvent): Result<Unit> {
        return try {
            val uid = requireUserId()
            val payload = mutableMapOf<String, Any?>(
                "roomId" to event.roomId,
                "type" to event.type,
                "senderId" to uid,
                "senderName" to event.senderName.ifBlank { "Member" }.take(100),
                "senderFlag" to event.senderFlag.take(12),
                "senderVipLevel" to event.senderVipLevel.coerceIn(1, 100),
                "payloadId" to event.payloadId.take(80),
                "text" to event.text.take(600),
                "recipientId" to event.recipientId,
                "recipientName" to event.recipientName?.take(100),
                "giftValueXp" to event.giftValueXp.coerceIn(0, 1000).toLong(),
                "timestampMs" to event.timestampMs,
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("voice_rooms").document(event.roomId)
                .collection("events")
                .add(payload)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val fe = e as? FirebaseFirestoreException
            if (fe?.code != FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                handleFirestoreError(e, OperationType.CREATE, "voice_rooms/${event.roomId}/events")
            }
            Result.failure(e)
        }
    }

    fun observeRoomEvents(roomId: String): Flow<List<VoiceRoomLiveEvent>> = flow {
        if (auth.currentUser == null || roomId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("events")
                .orderBy("timestampMs", Query.Direction.ASCENDING)
                .limitToLast(50)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FirestoreVoiceRoomEvent::class.java).map { fe ->
                        VoiceRoomLiveEvent(
                            id = fe.id,
                            roomId = fe.roomId.ifBlank { roomId },
                            type = fe.type,
                            senderId = fe.senderId,
                            senderName = fe.senderName,
                            senderFlag = fe.senderFlag,
                            senderVipLevel = fe.senderVipLevel,
                            payloadId = fe.payloadId,
                            text = fe.text,
                            recipientId = fe.recipientId,
                            recipientName = fe.recipientName,
                            giftValueXp = fe.giftValueXp.toInt(),
                            timestampMs = fe.timestampMs
                        )
                    }
                }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }

    // --- Live Room Polls ---

    suspend fun createRoomPoll(
        roomId: String,
        question: String,
        options: List<String>,
        creatorName: String
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val cleanOptions = options.map { it.trim() }.filter { it.isNotEmpty() }.take(5)
            if (cleanOptions.size < 2) {
                return Result.failure(IllegalArgumentException("Poll needs at least 2 options."))
            }
            val payload = mapOf(
                "roomId" to roomId,
                "question" to question.trim().take(200),
                "options" to cleanOptions,
                "votes" to emptyMap<String, Int>(),
                "createdById" to uid,
                "createdByName" to creatorName.ifBlank { "Member" }.take(100),
                "isClosed" to false,
                "createdAtMs" to System.currentTimeMillis(),
                "createdAt" to FieldValue.serverTimestamp()
            )
            db.collection("voice_rooms").document(roomId).collection("polls").add(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "voice_rooms/$roomId/polls")
            Result.failure(e)
        }
    }

    suspend fun voteRoomPoll(roomId: String, pollId: String, optionIndex: Int): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("voice_rooms").document(roomId)
                .collection("polls").document(pollId)
                .update("votes.$uid", optionIndex)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "voice_rooms/$roomId/polls/$pollId")
            Result.failure(e)
        }
    }

    suspend fun closeRoomPoll(roomId: String, pollId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("voice_rooms").document(roomId)
                .collection("polls").document(pollId)
                .update("isClosed", true)
                .await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeRoomPolls(roomId: String): Flow<List<VoiceRoomPoll>> = flow {
        if (auth.currentUser == null || roomId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        emitAll(
            db.collection("voice_rooms").document(roomId).collection("polls")
                .orderBy("createdAtMs", Query.Direction.DESCENDING)
                .limit(10)
                .snapshots()
                .map { snap ->
                    snap.toObjects(FirestoreVoiceRoomPoll::class.java).map { fp ->
                        VoiceRoomPoll(
                            id = fp.id,
                            roomId = fp.roomId.ifBlank { roomId },
                            question = fp.question,
                            options = fp.options,
                            votes = fp.votes,
                            createdById = fp.createdById,
                            createdByName = fp.createdByName,
                            isClosed = fp.isClosed,
                            createdAtMs = fp.createdAtMs
                        )
                    }
                }
                .catch { error ->
                    if (error is CancellationException) throw error
                    emit(emptyList())
                }
        )
    }
}
