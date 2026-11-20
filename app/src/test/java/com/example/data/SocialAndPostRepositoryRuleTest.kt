package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class SocialAndPostRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun posts_crossUserLikeAndComment_succeeds(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val alicePosts = FirestorePostRepository(firestore)
        val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
            alicePosts.createPost(
                content = "Hello from Alice ${UUID.randomUUID()}",
                authorName = "Alice",
                authorCountry = "Canada",
                authorFlag = "🇨🇦",
                category = "Languages"
            ).getOrThrow()
        }

        // Bob signs in and likes & comments on Alice's post
        val bobUid = signInTestUser(BOB_EMAIL)
        val bobPosts = FirestorePostRepository(firestore)

        val likeRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobPosts.toggleLike(postId = postId, currentLikes = 0L, currentlyLiked = false)
        }
        assertTrue("Cross-user like should succeed: ${likeRes.exceptionOrNull()}", likeRes.isSuccess)

        val commentRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobPosts.addComment(
                postId = postId,
                authorName = "Bob",
                authorFlag = "🇩🇪",
                content = "Great post!"
            )
        }
        assertTrue("Cross-user comment should succeed: ${commentRes.exceptionOrNull()}", commentRes.isSuccess)

        val snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("posts").document(postId).get().await()
        }
        assertEquals(1L, snapshot.getLong("likesCount"))
        assertEquals(1L, snapshot.getLong("commentsCount"))
        @Suppress("UNCHECKED_CAST")
        val likedIds = snapshot.get("likedUserIds") as? List<String> ?: emptyList()
        assertTrue(likedIds.contains(bobUid))
    }

    @Test
    fun posts_onlyAuthorCanDelete(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val alicePosts = FirestorePostRepository(firestore)
        val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
            alicePosts.createPost(
                content = "Ephemeral post ${UUID.randomUUID()}",
                authorName = "Alice",
                authorCountry = "Canada",
                authorFlag = "🇨🇦"
            ).getOrThrow()
        }

        // Bob cannot delete Alice's post
        signInTestUser(BOB_EMAIL)
        val bobPosts = FirestorePostRepository(firestore)
        val bobDelete = withTimeout(DEFAULT_TIMEOUT_MS) { bobPosts.deletePost(postId) }
        assertTrue("Non-author delete should fail", bobDelete.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            bobDelete.exceptionOrNull()?.unwrapFirestoreException()?.code
        )

        // Alice can delete her own post
        signInTestUser(ALICE_EMAIL)
        val aliceDelete = withTimeout(DEFAULT_TIMEOUT_MS) { alicePosts.deletePost(postId) }
        assertTrue("Author delete should succeed", aliceDelete.isSuccess)
    }

    @Test
    fun posts_unauthenticatedRead_failsWithPermissionDenied(): Unit = runBlocking {
        auth.signOut()
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("posts").get().await()
            }
            fail("Expected PERMISSION_DENIED when reading posts unauthenticated")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun followAndUnfollow_persistsAcrossListeners(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val aliceSocial = FirestoreSocialRepository(firestore)
        val followRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceSocial.followUser(bobUid) }
        assertTrue("Follow should succeed: ${followRes.exceptionOrNull()}", followRes.isSuccess)

        val following = withTimeout(FLOW_TIMEOUT_MS) {
            aliceSocial.observeFollowingIds().first { bobUid in it }
        }
        assertTrue(bobUid in following)

        signInTestUser(BOB_EMAIL)
        val bobSocial = FirestoreSocialRepository(firestore)
        val followers = withTimeout(FLOW_TIMEOUT_MS) {
            bobSocial.observeFollowerIds().first { aliceUid in it }
        }
        assertTrue(aliceUid in followers)

        val allEdges = withTimeout(FLOW_TIMEOUT_MS) {
            bobSocial.observeAllFollowEdges().first { edges ->
                edges.any { it.followerId == aliceUid && it.followingId == bobUid }
            }
        }
        assertTrue(allEdges.any { it.followerId == aliceUid && it.followingId == bobUid })

        signInTestUser(ALICE_EMAIL)
        val unfollowRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceSocial.unfollowUser(bobUid) }
        assertTrue("Unfollow should succeed", unfollowRes.isSuccess)

        val followingAfter = withTimeout(FLOW_TIMEOUT_MS) {
            aliceSocial.observeFollowingIds().first { bobUid !in it }
        }
        assertFalse(bobUid in followingAfter)
    }

    @Test
    fun blockAndUnblock_isolatesCrossUserReads(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val aliceSocial = FirestoreSocialRepository(firestore)
        val blockRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceSocial.blockUser(bobUid, "Spam") }
        assertTrue("Block should succeed: ${blockRes.exceptionOrNull()}", blockRes.isSuccess)

        val blocked = withTimeout(FLOW_TIMEOUT_MS) {
            aliceSocial.observeBlockedUserIds().first { bobUid in it }
        }
        assertTrue(bobUid in blocked)

        // Bob cannot read Alice's blocked subcollection
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("blocked").document(bobUid).get().await()
            }
            fail("Expected PERMISSION_DENIED when reading another user's blocked list")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }

        signInTestUser(ALICE_EMAIL)
        val unblockRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceSocial.unblockUser(bobUid) }
        assertTrue("Unblock should succeed", unblockRes.isSuccess)

        val blockedAfter = withTimeout(FLOW_TIMEOUT_MS) {
            aliceSocial.observeBlockedUserIds().first { bobUid !in it }
        }
        assertFalse(bobUid in blockedAfter)
    }

    @Test
    fun friendRequest_sendAndAccept_succeeds(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val aliceSocial = FirestoreSocialRepository(firestore)
        val reqId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceSocial.sendFriendRequest(bobUid, "Alice", "Bob").getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobSocial = FirestoreSocialRepository(firestore)
        val incoming = withTimeout(FLOW_TIMEOUT_MS) {
            bobSocial.observeIncomingFriendRequests().first { list -> list.any { it.id == reqId } }
        }
        assertEquals("pending", incoming.first { it.id == reqId }.status)

        val acceptRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobSocial.respondToFriendRequest(reqId, accept = true)
        }
        assertTrue("Accept friend request should succeed: ${acceptRes.exceptionOrNull()}", acceptRes.isSuccess)

        signInTestUser(ALICE_EMAIL)
        val outgoing = withTimeout(FLOW_TIMEOUT_MS) {
            aliceSocial.observeOutgoingFriendRequests().first { list ->
                list.any { it.id == reqId && it.status == "accepted" }
            }
        }
        assertTrue(outgoing.any { it.id == reqId && it.status == "accepted" })
    }

    @Test
    fun submitReport_persistsAndBlocksCrossUserReads(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val aliceSocial = FirestoreSocialRepository(firestore)
        val reportId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceSocial.submitReport("voice_room", "room_123", "Harassment", "Offensive speech").getOrThrow()
        }
        assertTrue(reportId.isNotBlank())

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("reports").document(reportId).get().await()
            }
            fail("Expected PERMISSION_DENIED when non-reporter reads report")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun updatePrivacyAndLanguages_persistsToUserProfile(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val userRepo = FirestoreUserRepository(firestore)

        val saveRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            userRepo.saveUserProfile(
                FirestoreUserProfile(
                    userId = aliceUid,
                    username = "alice_lang",
                    displayName = "Alice",
                    country = "Canada",
                    countryCode = "CA",
                    city = "Toronto",
                    nativeLanguages = listOf("English"),
                    learningLanguages = listOf("Persian", "Japanese"),
                    proficiencyLevel = "Intermediate",
                    isDiscoveryVisible = true,
                    allowDirectMessages = true
                )
            )
        }
        assertTrue("Save extended profile should succeed: ${saveRes.exceptionOrNull()}", saveRes.isSuccess)

        val privacyRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            userRepo.updatePrivacySettings(isDiscoveryVisible = false, allowDirectMessages = false)
        }
        assertTrue("Update privacy settings should succeed: ${privacyRes.exceptionOrNull()}", privacyRes.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { userRepo.getUserProfile(aliceUid).getOrThrow() }
        assertEquals("Toronto", fetched?.city)
        assertEquals(listOf("English"), fetched?.nativeLanguages)
        assertEquals(listOf("Persian", "Japanese"), fetched?.learningLanguages)
        assertEquals("Intermediate", fetched?.proficiencyLevel)
        assertEquals(false, fetched?.isDiscoveryVisible)
        assertEquals(false, fetched?.allowDirectMessages)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_social@viberoom.app"
        const val BOB_EMAIL = "bob_social@viberoom.app"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
