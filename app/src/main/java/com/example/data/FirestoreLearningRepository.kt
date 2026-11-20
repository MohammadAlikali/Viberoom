package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
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

class FirestoreLearningRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing learning or story data.")
    }

    // --- 24h Ephemeral Stories ---

    suspend fun createStory(
        text: String,
        authorName: String,
        authorFlag: String,
        authorCountry: String,
        category: String = "Daily",
        backgroundHex: Long = 0xFF6A1B9A
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val cleanText = text.trim().take(400)
            require(cleanText.isNotBlank()) { "Story text cannot be empty" }
            val nowMs = System.currentTimeMillis()
            val expiresAtMs = nowMs + 24L * 60L * 60L * 1000L
            val docRef = db.collection("stories").document()
            val payload = mapOf(
                "authorId" to uid,
                "authorName" to authorName.take(100),
                "authorFlag" to authorFlag.take(10),
                "authorCountry" to authorCountry.take(60),
                "text" to cleanText,
                "category" to category.take(50),
                "backgroundHex" to backgroundHex,
                "viewedByIds" to listOf(uid),
                "reactions" to emptyMap<String, String>(),
                "expiresAtMs" to expiresAtMs,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "stories")
            Result.failure(e)
        }
    }

    fun observeActiveStories(): Flow<List<FirestoreStory>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "stories"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(60)
                .snapshots()
                .map { snapshot ->
                    val now = System.currentTimeMillis()
                    snapshot.toObjects(FirestoreStory::class.java)
                        .filter { it.expiresAtMs == 0L || it.expiresAtMs > now }
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
                    if (auth.currentUser == null) emit(emptyList()) else throw error
                }
        )
    }

    suspend fun markStoryViewed(storyId: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("stories").document(storyId).update(
                mapOf(
                    "viewedByIds" to FieldValue.arrayUnion(uid),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    suspend fun reactToStory(storyId: String, currentReactions: Map<String, String>, emoji: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val updated = currentReactions.toMutableMap()
            updated[uid] = emoji
            db.collection("stories").document(storyId).update(
                mapOf(
                    "reactions" to updated,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "stories/$storyId")
            Result.failure(e)
        }
    }

    suspend fun deleteStory(storyId: String): Result<Unit> {
        return try {
            requireUserId()
            db.collection("stories").document(storyId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "stories/$storyId")
            Result.failure(e)
        }
    }

    // --- Personal Vocabulary Flashcards ---

    suspend fun saveVocabItem(
        wordOrPhrase: String,
        translation: String,
        language: String,
        exampleSentence: String = ""
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("vocabulary").document()
            val payload = mutableMapOf<String, Any>(
                "userId" to uid,
                "wordOrPhrase" to wordOrPhrase.trim().take(300),
                "translation" to translation.trim().take(500),
                "language" to language.trim().ifBlank { "English" }.take(60),
                "masteryLevel" to 1,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (exampleSentence.isNotBlank()) {
                payload["exampleSentence"] = exampleSentence.trim().take(500)
            }
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/vocabulary")
            Result.failure(e)
        }
    }

    suspend fun updateVocabMastery(vocabId: String, nextLevel: Int): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).collection("vocabulary").document(vocabId)
                .update(
                    mapOf(
                        "masteryLevel" to nextLevel.coerceIn(1, 5),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "users/vocabulary/$vocabId")
            Result.failure(e)
        }
    }

    suspend fun deleteVocabItem(vocabId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).collection("vocabulary").document(vocabId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/vocabulary/$vocabId")
            Result.failure(e)
        }
    }

    fun observeVocabulary(): Flow<List<FirestoreVocabItem>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/vocabulary"
        emitAll(
            db.collection("users").document(uid).collection("vocabulary")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .snapshots()
                .map { it.toObjects(FirestoreVocabItem::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    if (auth.currentUser == null) emit(emptyList()) else throw error
                }
        )
    }

    // --- Notifications ---

    suspend fun createNotificationForUser(
        recipientId: String,
        senderName: String,
        type: String,
        title: String,
        body: String,
        targetId: String? = null
    ) {
        val uid = auth.currentUser?.uid ?: return
        if (recipientId.isBlank() || recipientId == uid) return
        try {
            val docRef = db.collection("users").document(recipientId).collection("notifications").document()
            val payload = mutableMapOf<String, Any?>(
                "recipientId" to recipientId,
                "senderId" to uid,
                "senderName" to senderName.ifBlank { "Member" }.take(100),
                "type" to type,
                "title" to title.take(120),
                "body" to body.take(400),
                "targetId" to targetId?.take(128),
                "isRead" to false,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload.filterValues { it != null }).await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // Notification delivery should not block primary user action
        }
    }

    suspend fun markNotificationRead(notificationId: String) {
        val uid = auth.currentUser?.uid ?: return
        try {
            db.collection("users").document(uid).collection("notifications").document(notificationId)
                .update("isRead", true).await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    suspend fun markAllNotificationsRead() {
        val uid = auth.currentUser?.uid ?: return
        try {
            val unread = db.collection("users").document(uid).collection("notifications")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            if (unread.isEmpty) return
            val batch = db.batch()
            unread.documents.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    fun observeNotifications(): Flow<List<FirestoreNotification>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid).collection("notifications")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { it.toObjects(FirestoreNotification::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    if (auth.currentUser == null) emit(emptyList()) else throw error
                }
        )
    }
}
