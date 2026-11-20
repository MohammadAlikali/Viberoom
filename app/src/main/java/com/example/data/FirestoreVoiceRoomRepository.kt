package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.RoomCategory
import com.example.model.UserProfile
import com.example.model.VoiceRoom
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

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated to interact with voice rooms.")
    }

    /**
     * Real-time Flow of all currently active live voice rooms from Firestore.
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
                    snapshot.toObjects(FirestoreVoiceRoom::class.java).map { firestoreRoom ->
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

                        VoiceRoom(
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
                            aiSummary = firestoreRoom.aiSummary
                        )
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

    /**
     * Creates a new live voice room in Firestore.
     */
    suspend fun createVoiceRoom(
        title: String,
        description: String,
        category: RoomCategory,
        hostUser: UserProfile,
        tags: List<String>,
        isPrivate: Boolean = false
    ): VoiceRoom {
        val uid = requireUserId()
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
                isPrivate = isPrivate
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    /**
     * Increments listener count when joining a live room.
     */
    suspend fun joinVoiceRoom(roomId: String) {
        if (auth.currentUser == null) return
        val docRef = db.collection("voice_rooms").document(roomId)
        try {
            docRef.update(
                mapOf(
                    "listenersCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Log.w("VoiceRoomRepo", "Failed to increment listenersCount: ${e.message}")
        }
    }

    /**
     * Decrements listener count when leaving a live room, clamping at 0.
     */
    suspend fun leaveVoiceRoom(roomId: String) {
        if (auth.currentUser == null) return
        val docRef = db.collection("voice_rooms").document(roomId)
        try {
            db.runTransaction { transaction ->
                val snapshot = transaction.get(docRef)
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

    /**
     * Marks a voice room as ended (isLive = false) when the host closes the stage.
     */
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

    suspend fun endVoiceRoom(roomId: String) {
        if (auth.currentUser == null) return
        val docRef = db.collection("voice_rooms").document(roomId)
        try {
            docRef.update(
                mapOf(
                    "isLive" to false,
                    "listenersCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Log.w("VoiceRoomRepo", "Failed to end voice room: ${e.message}")
        }
    }
}
