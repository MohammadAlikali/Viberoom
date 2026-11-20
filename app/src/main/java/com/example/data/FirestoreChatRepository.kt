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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreChatRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before participating in real chats.")
    }

    /**
     * Send a real text or voice message to another user through Cloud Firestore,
     * optionally replying to an existing message.
     */
    suspend fun sendMessage(
        conversationId: String,
        receiverId: String,
        text: String,
        senderName: String,
        messageId: String? = null,
        mediaType: String = "text",
        audioDurationSec: Int? = null,
        mediaData: String? = null,
        mediaUrl: String? = null,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        isForwarded: Boolean = false,
        expiresAtMs: Long? = null
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val cleanText = text.trim()
            require(cleanText.isNotEmpty()) { "Message text cannot be empty" }

            val convDocRef = db.collection("conversations").document(conversationId)
            val cleanMsgId = messageId?.takeIf { it.isNotBlank() }
            val msgDocRef = if (cleanMsgId != null) {
                convDocRef.collection("messages").document(cleanMsgId)
            } else {
                convDocRef.collection("messages").document()
            }

            val convData = mapOf(
                "participantIds" to listOf(uid, receiverId).distinct(),
                "lastMessage" to cleanText.take(500),
                "lastSenderId" to uid,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            convDocRef.set(convData, com.google.firebase.firestore.SetOptions.merge()).await()

            val normalizedMediaType = when (mediaType) {
                "voice", "image" -> mediaType
                else -> "text"
            }

            val messagePayload = mutableMapOf<String, Any>(
                "conversationId" to conversationId,
                "senderId" to uid,
                "senderName" to senderName.take(100),
                "receiverId" to receiverId,
                "text" to cleanText.take(2000),
                "isRead" to false,
                "mediaType" to normalizedMediaType,
                "reactions" to emptyMap<String, String>(),
                "isEdited" to false,
                "isDeleted" to false,
                "isForwarded" to isForwarded,
                "createdAt" to FieldValue.serverTimestamp()
            )
            if (expiresAtMs != null && expiresAtMs > 0L) {
                messagePayload["expiresAtMs"] = expiresAtMs
            }
            if (normalizedMediaType == "voice" && audioDurationSec != null && audioDurationSec in 1..600) {
                messagePayload["audioDurationSec"] = audioDurationSec
            }
            if (!mediaData.isNullOrBlank()) {
                messagePayload["mediaData"] = mediaData.take(750_000)
            }
            if (!mediaUrl.isNullOrBlank()) {
                messagePayload["mediaUrl"] = mediaUrl.take(1000)
            }
            if (!replyToMessageId.isNullOrBlank()) {
                messagePayload["replyToMessageId"] = replyToMessageId.take(128)
            }
            if (!replyToText.isNullOrBlank()) {
                messagePayload["replyToText"] = replyToText.take(500)
            }
            if (!replyToSenderName.isNullOrBlank()) {
                messagePayload["replyToSenderName"] = replyToSenderName.take(100)
            }
            msgDocRef.set(messagePayload).await()

            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val path = "conversations/$conversationId/messages"
            handleFirestoreError(e, OperationType.CREATE, path)
            Result.failure(e)
        }
    }

    suspend fun editMessage(conversationId: String, messageId: String, newText: String): Result<Unit> {
        return try {
            requireUserId()
            val clean = newText.trim().take(2000)
            require(clean.isNotEmpty()) { "Edited message cannot be empty" }
            val msgRef = db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
            msgRef.update(
                mapOf(
                    "text" to clean,
                    "isEdited" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "conversations/$conversationId/messages/$messageId")
            Result.failure(e)
        }
    }

    suspend fun softDeleteMessage(conversationId: String, messageId: String): Result<Unit> {
        return try {
            requireUserId()
            val msgRef = db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
            msgRef.update(
                mapOf(
                    "text" to "[Message deleted]",
                    "isDeleted" to true
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "conversations/$conversationId/messages/$messageId")
            Result.failure(e)
        }
    }

    suspend fun toggleMessageReaction(
        conversationId: String,
        messageId: String,
        currentReactions: Map<String, String>,
        emoji: String
    ): Result<Unit> {
        return try {
            val uid = requireUserId()
            val updated = currentReactions.toMutableMap()
            if (updated[uid] == emoji) {
                updated.remove(uid)
            } else {
                updated[uid] = emoji
            }
            val msgRef = db.collection("conversations").document(conversationId)
                .collection("messages").document(messageId)
            msgRef.update("reactions", updated).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "conversations/$conversationId/messages/$messageId")
            Result.failure(e)
        }
    }

    suspend fun pinMessage(conversationId: String, messageId: String?, messageText: String?): Result<Unit> {
        return try {
            requireUserId()
            val convRef = db.collection("conversations").document(conversationId)
            convRef.update(
                mapOf(
                    "pinnedMessageId" to messageId?.take(128),
                    "pinnedMessageText" to messageText?.take(500),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "conversations/$conversationId")
            Result.failure(e)
        }
    }

    suspend fun markConversationMessagesRead(conversationId: String) {
        val uid = auth.currentUser?.uid ?: return
        val convRef = db.collection("conversations").document(conversationId)
        try {
            val unreadSnapshot = convRef.collection("messages")
                .whereEqualTo("receiverId", uid)
                .whereEqualTo("isRead", false)
                .get()
                .await()
            if (unreadSnapshot.isEmpty) return

            val batch = db.batch()
            unreadSnapshot.documents.forEach { doc ->
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
        }
    }

    fun observeMessages(conversationId: String): Flow<List<FirestoreChatMessage>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "conversations/$conversationId/messages"
        val query = db.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("createdAt", Query.Direction.ASCENDING)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreChatMessage::class.java)
                }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                if (firestoreError?.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    emit(emptyList())
                    return@catch
                }
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
            .collect { emit(it) }
    }

    fun observeConversations(): Flow<List<FirestoreConversation>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "conversations"
        val query = db.collection("conversations")
            .whereArrayContains("participantIds", uid)

        query.snapshots()
            .map { snapshot ->
                snapshot.documents
                    .mapNotNull { doc -> doc.toObject(FirestoreConversation::class.java) }
                    .sortedByDescending { it.updatedAt?.toDate()?.time ?: 0L }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                if (auth.currentUser == null) {
                    emit(emptyList())
                } else {
                    throw error
                }
            }
            .collect { emit(it) }
    }
}
