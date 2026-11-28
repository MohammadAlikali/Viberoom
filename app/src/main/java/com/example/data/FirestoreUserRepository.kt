package com.example.data

import android.content.Context
import android.util.Log
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
import org.json.JSONArray
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val causes = generateSequence(exception) { it.cause }
    val rootError = causes.firstOrNull { it is FirebaseFirestoreException }
        ?: causes.firstOrNull { it !is CancellationException }
        ?: exception

    val auth = Firebase.auth
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", rootError.message ?: rootError.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class FirestoreUserRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing user profile.")
    }

    suspend fun getUserProfile(userId: String): Result<FirestoreUserProfile?> {
        val docRef = db.collection("users").document(userId)
        return try {
            val snapshot = docRef.get().await()
            val profile = snapshot.toObject(FirestoreUserProfile::class.java)
            Result.success(profile)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveUserProfile(profile: FirestoreUserProfile): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val existing = docRef.get().await()
            if (existing.exists()) {
                val existingCountry = existing.getString("country")
                val existingCountryCode = existing.getString("countryCode")
                val updates = mutableMapOf<String, Any?>()
                updates["username"] = profile.username.ifBlank { "viber_${uid.take(6)}" }
                profile.displayName?.let { updates["displayName"] = it }
                profile.email?.let { updates["email"] = it }
                if (existingCountry.isNullOrBlank()) {
                    profile.country?.takeIf { it.isNotBlank() }?.let { updates["country"] = it }
                    profile.countryCode?.takeIf { it.isNotBlank() }?.let { updates["countryCode"] = it }
                } else if (!existingCountryCode.isNullOrBlank()) {
                    updates["country"] = existingCountry
                    updates["countryCode"] = existingCountryCode
                }
                profile.city?.takeIf { it.isNotBlank() }?.let { updates["city"] = it }
                profile.bio?.let { updates["bio"] = it }
                profile.avatarUrl?.let { updates["avatarUrl"] = it }
                profile.avatarData?.let { updates["avatarData"] = it }
                profile.avatarFrame?.takeIf { it.isNotBlank() }?.let { updates["avatarFrame"] = it }
                updates["interests"] = profile.interests
                updates["languages"] = profile.languages
                updates["nativeLanguages"] = profile.nativeLanguages
                updates["learningLanguages"] = profile.learningLanguages
                updates["learningLanguageLevels"] = profile.learningLanguageLevels.mapValues { it.value.coerceIn(1, 5) }
                profile.proficiencyLevel?.takeIf { it.isNotBlank() }?.let { updates["proficiencyLevel"] = it }
                profile.communicationGoal?.takeIf { it.isNotBlank() }?.let { updates["communicationGoal"] = it }
                updates["isOnline"] = profile.isOnline
                updates["showOnlineStatus"] = profile.showOnlineStatus
                updates["lastSeenVisibility"] = profile.lastSeenVisibility.ifBlank { "everyone" }
                updates["lastSeenAtMs"] = profile.lastSeenAtMs ?: System.currentTimeMillis()
                updates["isDiscoveryVisible"] = profile.isDiscoveryVisible
                updates["allowDirectMessages"] = profile.allowDirectMessages
                updates["vibeXp"] = profile.vibeXp.coerceAtLeast(0)
                updates["vibeLevel"] = com.example.model.VibeCatalogs.levelFromXp(profile.vibeXp)
                updates["streakDays"] = profile.streakDays.coerceAtLeast(0)
                updates["unlockedBadges"] = profile.unlockedBadges.take(20)
                updates["updatedAt"] = FieldValue.serverTimestamp()

                docRef.update(updates.filterValues { it != null }).await()
            } else {
                val payload = mutableMapOf<String, Any?>()
                payload["userId"] = uid
                payload["username"] = profile.username.ifBlank { "viber_${uid.take(6)}" }
                profile.displayName?.let { payload["displayName"] = it }
                profile.email?.let { payload["email"] = it }
                profile.country?.let { payload["country"] = it }
                profile.countryCode?.let { payload["countryCode"] = it }
                profile.city?.takeIf { it.isNotBlank() }?.let { payload["city"] = it }
                profile.bio?.let { payload["bio"] = it }
                profile.avatarUrl?.let { payload["avatarUrl"] = it }
                profile.avatarData?.let { payload["avatarData"] = it }
                profile.avatarFrame?.takeIf { it.isNotBlank() }?.let { payload["avatarFrame"] = it }
                payload["interests"] = profile.interests
                payload["languages"] = profile.languages
                payload["nativeLanguages"] = profile.nativeLanguages
                payload["learningLanguages"] = profile.learningLanguages
                payload["learningLanguageLevels"] = profile.learningLanguageLevels.mapValues { it.value.coerceIn(1, 5) }
                profile.proficiencyLevel?.takeIf { it.isNotBlank() }?.let { payload["proficiencyLevel"] = it }
                profile.communicationGoal?.takeIf { it.isNotBlank() }?.let { payload["communicationGoal"] = it }
                payload["isOnline"] = profile.isOnline
                payload["showOnlineStatus"] = profile.showOnlineStatus
                payload["lastSeenVisibility"] = profile.lastSeenVisibility.ifBlank { "everyone" }
                payload["lastSeenAtMs"] = profile.lastSeenAtMs ?: System.currentTimeMillis()
                payload["isDiscoveryVisible"] = profile.isDiscoveryVisible
                payload["allowDirectMessages"] = profile.allowDirectMessages
                payload["vibeXp"] = profile.vibeXp.coerceAtLeast(0)
                payload["vibeLevel"] = com.example.model.VibeCatalogs.levelFromXp(profile.vibeXp)
                payload["streakDays"] = profile.streakDays.coerceAtLeast(0)
                payload["unlockedBadges"] = profile.unlockedBadges.take(20)
                payload["createdAt"] = FieldValue.serverTimestamp()
                payload["updatedAt"] = FieldValue.serverTimestamp()

                docRef.set(payload.filterValues { it != null }).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid
            val path = if (uid != null) "users/$uid" else "users/unauth"
            handleFirestoreError(e, OperationType.WRITE, path)
            Result.failure(e)
        }
    }

    suspend fun updateAvatarData(avatarData: String?): Result<Unit> {
        return updateAvatarAndFrame(
            avatarData = avatarData,
            clearAvatar = avatarData.isNullOrBlank(),
            avatarFrame = null
        )
    }

    suspend fun updateAvatarAndFrame(
        avatarData: String? = null,
        clearAvatar: Boolean = false,
        avatarFrame: String? = null
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val updates = mutableMapOf<String, Any?>(
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (clearAvatar) {
                updates["avatarData"] = null
                updates["avatarUrl"] = null
            } else if (!avatarData.isNullOrBlank()) {
                updates["avatarData"] = avatarData
            }
            if (avatarFrame != null) {
                updates["avatarFrame"] = avatarFrame.takeIf { it.isNotBlank() } ?: "none"
            }
            docRef.update(updates).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid
            handleFirestoreError(e, OperationType.UPDATE, "users/${uid ?: "unauth"}")
            Result.failure(e)
        }
    }

    suspend fun updatePrivacySettings(
        isDiscoveryVisible: Boolean,
        allowDirectMessages: Boolean,
        showOnlineStatus: Boolean = true,
        lastSeenVisibility: String = "everyone"
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid)
            val cleanLastSeen = if (lastSeenVisibility in listOf("everyone", "contacts", "nobody")) {
                lastSeenVisibility
            } else {
                "everyone"
            }
            docRef.update(
                mapOf(
                    "isDiscoveryVisible" to isDiscoveryVisible,
                    "allowDirectMessages" to allowDirectMessages,
                    "showOnlineStatus" to showOnlineStatus,
                    "lastSeenVisibility" to cleanLastSeen,
                    "isOnline" to showOnlineStatus,
                    "lastSeenAtMs" to System.currentTimeMillis(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid
            val path = if (uid != null) "users/$uid" else "users/unauth"
            handleFirestoreError(e, OperationType.UPDATE, path)
            Result.failure(e)
        }
    }

    fun observeUserProfile(userId: String): Flow<FirestoreUserProfile?> = flow {
        if (auth.currentUser == null) {
            emit(null)
            return@flow
        }
        val path = "users/$userId"
        val docRef = db.collection("users").document(userId)
        docRef.snapshots()
            .map { it.toObject(FirestoreUserProfile::class.java) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, path)
                }
                if (auth.currentUser == null) {
                    emit(null)
                } else {
                    throw error
                }
            }
            .collect { emit(it) }
    }

    /**
     * Streams real-time global member directory from Firestore.
     */
    fun observeAllUsers(): Flow<List<FirestoreUserProfile>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreUserProfile::class.java) }
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
     * Updates presence in Firestore so other members across the world can see the user online.
     */
    suspend fun setOnlinePresence(isOnline: Boolean, showOnlineStatus: Boolean = true) {
        val uid = auth.currentUser?.uid ?: return
        val docRef = db.collection("users").document(uid)
        try {
            docRef.update(
                mapOf(
                    "isOnline" to (isOnline && showOnlineStatus),
                    "lastSeenAtMs" to System.currentTimeMillis(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Log.d("UserRepo", "Updated presence for $uid: isOnline=${isOnline && showOnlineStatus}")
        } catch (e: Exception) {
            Log.w("UserRepo", "Failed updating online status: ${e.message}")
        }
    }

    suspend fun updateGamificationStats(
        vibeXp: Int,
        vibeLevel: Int,
        streakDays: Int,
        unlockedBadges: List<String>
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).update(
                mapOf(
                    "vibeXp" to vibeXp.coerceAtLeast(0),
                    "vibeLevel" to vibeLevel.coerceAtLeast(1),
                    "streakDays" to streakDays.coerceAtLeast(0),
                    "unlockedBadges" to unlockedBadges.distinct().take(20),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid
            handleFirestoreError(e, OperationType.UPDATE, "users/${uid ?: "unauth"}")
            Result.failure(e)
        }
    }

    private suspend fun deleteDocumentsInChunks(refs: List<com.google.firebase.firestore.DocumentReference>) {
        if (refs.isEmpty()) return
        refs.distinctBy { it.path }.chunked(450).forEach { chunk ->
            val batch = db.batch()
            chunk.forEach { batch.delete(it) }
            batch.commit().await()
        }
    }

    private suspend fun deleteBestEffort(label: String, block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w("FirestoreUserRepo", "Best-effort cleanup skipped for $label: ${e.message}")
        }
    }

    suspend fun deleteOwnAccountData(): Result<Unit> {
        return try {
            val uid = requireUserId()
            val userDocRef = db.collection("users").document(uid)

            // 1. Delete private user subcollections first (allowed for request.auth.uid == userId)
            listOf("blocked", "saved_posts", "vocabulary", "notifications").forEach { sub ->
                deleteBestEffort("users/$uid/$sub") {
                    val snap = userDocRef.collection(sub).get().await()
                    deleteDocumentsInChunks(snap.documents.map { it.reference })
                }
            }

            // 2. Delete outgoing follow edges created by this user
            deleteBestEffort("follows") {
                val followSnap = db.collection("follows")
                    .whereEqualTo("followerId", uid)
                    .get()
                    .await()
                deleteDocumentsInChunks(followSnap.documents.map { it.reference })
            }

            // 3. Delete sent and received friend requests
            deleteBestEffort("friend_requests") {
                val sent = db.collection("friend_requests").whereEqualTo("senderId", uid).get().await()
                val received = db.collection("friend_requests").whereEqualTo("receiverId", uid).get().await()
                deleteDocumentsInChunks((sent.documents + received.documents).map { it.reference })
            }

            // 4. Delete authored posts (and this user's comments under those posts)
            deleteBestEffort("posts") {
                val postSnap = db.collection("posts").whereEqualTo("authorId", uid).get().await()
                for (postDoc in postSnap.documents) {
                    deleteBestEffort("posts/${postDoc.id}/comments") {
                        val commentSnap = postDoc.reference.collection("comments")
                            .whereEqualTo("authorId", uid)
                            .get()
                            .await()
                        deleteDocumentsInChunks(commentSnap.documents.map { it.reference })
                    }
                }
                deleteDocumentsInChunks(postSnap.documents.map { it.reference })
            }

            // 5. Delete authored stories
            deleteBestEffort("stories") {
                val storySnap = db.collection("stories").whereEqualTo("authorId", uid).get().await()
                deleteDocumentsInChunks(storySnap.documents.map { it.reference })
            }

            // 6. Delete hosted voice rooms and their subcollections (must clean subcollections before room doc)
            deleteBestEffort("voice_rooms") {
                val roomSnap = db.collection("voice_rooms").whereEqualTo("hostId", uid).get().await()
                for (roomDoc in roomSnap.documents) {
                    val roomRef = roomDoc.reference
                    listOf("peers", "events", "polls").forEach { sub ->
                        deleteBestEffort("voice_rooms/${roomDoc.id}/$sub") {
                            val subSnap = roomRef.collection(sub).get().await()
                            deleteDocumentsInChunks(subSnap.documents.map { it.reference })
                        }
                    }
                    deleteBestEffort("voice_rooms/${roomDoc.id}/signals") {
                        val sentSignals = roomRef.collection("signals").whereEqualTo("fromUserId", uid).get().await()
                        val recvSignals = roomRef.collection("signals").whereEqualTo("toUserId", uid).get().await()
                        deleteDocumentsInChunks((sentSignals.documents + recvSignals.documents).map { it.reference })
                    }
                }
                deleteDocumentsInChunks(roomSnap.documents.map { it.reference })
            }

            // 7. Finally delete the primary user profile document
            userDocRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val uid = auth.currentUser?.uid
            handleFirestoreError(e, OperationType.DELETE, "users/$uid")
            Result.failure(e)
        }
    }
}
