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

class FirestoreSocialRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth: FirebaseAuth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in before accessing social features.")
    }

    // --- Follows ---

    suspend fun followUser(targetUserId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(targetUserId.isNotBlank() && targetUserId != uid) { "Invalid target user ID" }
            val followId = "${uid}_${targetUserId}"
            val docRef = db.collection("follows").document(followId)
            val payload = mapOf(
                "followerId" to uid,
                "followingId" to targetUserId,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "follows")
            Result.failure(e)
        }
    }

    suspend fun unfollowUser(targetUserId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val followId = "${uid}_${targetUserId}"
            val docRef = db.collection("follows").document(followId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "follows")
            Result.failure(e)
        }
    }

    fun observeFollowingIds(): Flow<Set<String>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptySet())
            return@flow
        }
        val path = "follows"
        emitAll(
            db.collection(path)
                .whereEqualTo("followerId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFollow::class.java)
                        .mapNotNull { it.followingId.takeIf(String::isNotBlank) }
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

    fun observeFollowerIds(): Flow<Set<String>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptySet())
            return@flow
        }
        val path = "follows"
        emitAll(
            db.collection(path)
                .whereEqualTo("followingId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFollow::class.java)
                        .mapNotNull { it.followerId.takeIf(String::isNotBlank) }
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

    fun observeAllFollowEdges(): Flow<List<FirestoreFollow>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "follows"
        emitAll(
            db.collection(path)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFollow::class.java)
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

    // --- Blocked Users ---

    suspend fun blockUser(targetUserId: String, reason: String? = null): Result<Unit> {
        return try {
            val uid = requireUserId()
            require(targetUserId.isNotBlank() && targetUserId != uid) { "Invalid blocked user ID" }
            val docRef = db.collection("users").document(uid).collection("blocked").document(targetUserId)
            val payload = mutableMapOf<String, Any?>(
                "blockedUserId" to targetUserId,
                "reason" to reason?.trim()?.take(300)?.takeIf { it.isNotBlank() },
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload.filterValues { it != null }).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "users/blocked/$targetUserId")
            Result.failure(e)
        }
    }

    suspend fun unblockUser(targetUserId: String): Result<Unit> {
        return try {
            val uid = requireUserId()
            val docRef = db.collection("users").document(uid).collection("blocked").document(targetUserId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/blocked/$targetUserId")
            Result.failure(e)
        }
    }

    fun observeBlockedUserIds(): Flow<Set<String>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptySet())
            return@flow
        }
        val path = "users/$uid/blocked"
        emitAll(
            db.collection("users").document(uid).collection("blocked")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreBlockedUser::class.java)
                        .mapNotNull { it.blockedUserId.ifBlank { it.id }.takeIf(String::isNotBlank) }
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

    // --- Reports ---

    suspend fun submitReport(
        targetType: String,
        targetId: String,
        reason: String,
        details: String = ""
    ): Result<String> {
        return try {
            val uid = requireUserId()
            require(targetType in setOf("user", "voice_room", "post")) { "Invalid report targetType" }
            require(targetId.isNotBlank() && reason.isNotBlank()) { "Target ID and reason required" }
            val docRef = db.collection("reports").document()
            val payload = mutableMapOf<String, Any?>(
                "reporterId" to uid,
                "targetType" to targetType,
                "targetId" to targetId.take(128),
                "reason" to reason.trim().take(120),
                "details" to details.trim().take(1000).takeIf { it.isNotBlank() },
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload.filterValues { it != null }).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "reports")
            Result.failure(e)
        }
    }

    // --- Friend Requests ---

    suspend fun sendFriendRequest(
        receiverId: String,
        senderName: String? = null,
        receiverName: String? = null
    ): Result<String> {
        return try {
            val uid = requireUserId()
            require(receiverId.isNotBlank() && receiverId != uid) { "Invalid receiverId" }
            val requestId = "${uid}_${receiverId}"
            val docRef = db.collection("friend_requests").document(requestId)
            val payload = mutableMapOf<String, Any?>(
                "senderId" to uid,
                "receiverId" to receiverId,
                "senderName" to senderName?.trim()?.take(100)?.takeIf { it.isNotBlank() },
                "receiverName" to receiverName?.trim()?.take(100)?.takeIf { it.isNotBlank() },
                "status" to "pending",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload.filterValues { it != null }).await()
            Result.success(requestId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, "friend_requests")
            Result.failure(e)
        }
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean): Result<Unit> {
        return try {
            requireUserId()
            val docRef = db.collection("friend_requests").document(requestId)
            docRef.update(
                mapOf(
                    "status" to if (accept) "accepted" else "rejected",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "friend_requests/$requestId")
            Result.failure(e)
        }
    }

    suspend fun cancelFriendRequest(requestId: String): Result<Unit> {
        return try {
            requireUserId()
            val docRef = db.collection("friend_requests").document(requestId)
            docRef.update(
                mapOf(
                    "status" to "cancelled",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, "friend_requests/$requestId")
            Result.failure(e)
        }
    }

    fun observeIncomingFriendRequests(): Flow<List<FirestoreFriendRequest>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "friend_requests"
        emitAll(
            db.collection(path)
                .whereEqualTo("receiverId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFriendRequest::class.java)
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
                    if (auth.currentUser == null) emit(emptyList()) else throw error
                }
        )
    }

    fun observeOutgoingFriendRequests(): Flow<List<FirestoreFriendRequest>> = flow {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            emit(emptyList())
            return@flow
        }
        val path = "friend_requests"
        emitAll(
            db.collection(path)
                .whereEqualTo("senderId", uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(FirestoreFriendRequest::class.java)
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
                    if (auth.currentUser == null) emit(emptyList()) else throw error
                }
        )
    }
}
