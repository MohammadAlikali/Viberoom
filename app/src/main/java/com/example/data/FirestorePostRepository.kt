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

class FirestorePostRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in to interact with posts.")
    }

    private fun extractHashtags(text: String): List<String> {
        return Regex("#([\\p{L}0-9_]{1,40})")
            .findAll(text)
            .map { it.groupValues[1].lowercase() }
            .distinct()
            .take(20)
            .toList()
    }

    suspend fun createPost(
        content: String,
        authorName: String,
        authorCountry: String,
        authorFlag: String,
        category: String = "General",
        mediaType: String = "text",
        mediaData: String? = null,
        mediaUrl: String? = null,
        postType: String = "standard",
        pollOptions: List<String> = emptyList()
    ): Result<String> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("posts").document()
            val cleanContent = content.trim().take(2000)
            val hashtags = extractHashtags(cleanContent)
            val normalizedType = when (mediaType) {
                "image", "video" -> mediaType
                else -> "text"
            }
            val cleanPollOptions = pollOptions.map { it.trim().take(80) }.filter { it.isNotEmpty() }.take(6)
            val normalizedPostType = when {
                postType == "poll" && cleanPollOptions.size >= 2 -> "poll"
                postType == "qa" -> "qa"
                else -> "standard"
            }
            val payload = mutableMapOf<String, Any>(
                "authorId" to uid,
                "authorName" to authorName.take(100),
                "authorFlag" to authorFlag.take(10),
                "authorCountry" to authorCountry.take(60),
                "content" to cleanContent,
                "category" to category.take(50),
                "hashtags" to hashtags,
                "mediaType" to normalizedType,
                "likesCount" to 0L,
                "commentsCount" to 0L,
                "likedUserIds" to emptyList<String>(),
                "postType" to normalizedPostType,
                "pollOptions" to if (normalizedPostType == "poll") cleanPollOptions else emptyList<String>(),
                "pollVotes" to emptyMap<String, Int>(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (!mediaData.isNullOrBlank()) {
                payload["mediaData"] = mediaData.take(750_000)
            }
            if (!mediaUrl.isNullOrBlank()) {
                payload["mediaUrl"] = mediaUrl.take(1000)
            }
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "posts")
            Result.failure(e)
        }
    }

    fun observePosts(): Flow<List<FirestorePost>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "posts"
        emitAll(
            db.collection(path)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestorePost::class.java) }
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

    suspend fun toggleLike(postId: String, currentLikes: Long, currentlyLiked: Boolean): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        val docRef = db.collection("posts").document(postId)
        return try {
            if (currentlyLiked) {
                docRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(-1),
                        "likedUserIds" to FieldValue.arrayRemove(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            } else {
                docRef.update(
                    mapOf(
                        "likesCount" to FieldValue.increment(1),
                        "likedUserIds" to FieldValue.arrayUnion(uid),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun voteOnPoll(postId: String, optionIndex: Int, currentVotes: Map<String, Int>): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(IllegalStateException("Not authenticated"))
        val docRef = db.collection("posts").document(postId)
        return try {
            val updated = currentVotes.toMutableMap()
            updated[uid] = optionIndex.coerceIn(0, 5)
            docRef.update(
                mapOf(
                    "pollVotes" to updated,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun savePost(post: FirestorePost): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("saved_posts").document(post.id)
            val payload = mapOf(
                "postId" to post.id,
                "authorName" to post.authorName.take(100),
                "contentPreview" to post.content.take(500),
                "savedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/saved_posts/${post.id}")
            Result.failure(e)
        }
    }

    suspend fun unsavePost(postId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            db.collection("users").document(uid).collection("saved_posts").document(postId)
                .delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/saved_posts/$postId")
            Result.failure(e)
        }
    }

    fun observeSavedPostIds(): Flow<Set<String>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptySet())
            return@flow
        }
        val path = "users/$uid/saved_posts"
        emitAll(
            db.collection("users").document(uid).collection("saved_posts")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreSavedPost::class.java)
                        .mapNotNull { it.postId.ifBlank { it.id }.takeIf(String::isNotBlank) }
                        .toSet()
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
                    if (auth.currentUser == null) emit(emptySet()) else throw error
                }
        )
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        return try {
            requireUserId()
            val docRef = db.collection("posts").document(postId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "posts/$postId")
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, authorName: String, authorFlag: String, content: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val postRef = db.collection("posts").document(postId)
            val commentRef = postRef.collection("comments").document()

            val commentPayload = mapOf(
                "postId" to postId,
                "authorId" to uid,
                "authorName" to authorName.take(100),
                "authorFlag" to authorFlag.take(10),
                "content" to content.trim().take(1000),
                "createdAt" to FieldValue.serverTimestamp()
            )
            commentRef.set(commentPayload).await()
            postRef.update(
                mapOf(
                    "commentsCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "posts/$postId/comments")
            Result.failure(e)
        }
    }

    fun observeComments(postId: String): Flow<List<FirestoreComment>> = flow {
        if (auth.currentUser == null) {
            emit(emptyList())
            return@flow
        }
        val path = "posts/$postId/comments"
        emitAll(
            db.collection("posts")
                .document(postId)
                .collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreComment::class.java) }
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
}
