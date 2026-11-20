package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class UserProfileRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveUserProfile_authenticatedUser_createsDocumentSuccessfully(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = FirestoreUserRepository(firestore)

        val profile = FirestoreUserProfile(
            userId = uid,
            username = "alice_vibe",
            displayName = "Alice Wonderland",
            email = ALICE_EMAIL,
            country = "United States",
            countryCode = "US",
            bio = "Loving music and voice rooms"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveUserProfile(profile) }
        assertTrue("Save profile should succeed", result.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserProfile(uid) }
        assertTrue("Get profile should succeed", fetched.isSuccess)
        assertEquals("alice_vibe", fetched.getOrNull()?.username)
    }

    @Test
    fun getUserProfile_publicProfile_canBeReadByAnotherAuthenticatedUser(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreUserRepository(firestore)
        val profile = FirestoreUserProfile(
            userId = aliceUid,
            username = "alice_star",
            displayName = "Alice Star"
        )
        withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.saveUserProfile(profile) }

        // Bob signs in and reads Alice's public profile
        signInTestUser(BOB_EMAIL)
        val bobRepo = FirestoreUserRepository(firestore)
        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getUserProfile(aliceUid) }
        assertTrue("Fetch should succeed", fetched.isSuccess)
        assertEquals("alice_star", fetched.getOrNull()?.username)
    }

    @Test
    fun saveUserProfile_unauthenticatedUser_fails(): Unit = runBlocking {
        auth.signOut()
        val repository = FirestoreUserRepository(firestore)
        val profile = FirestoreUserProfile(
            userId = "unauth_user",
            username = "unauth"
        )
        val result = repository.saveUserProfile(profile)
        assertTrue("Unauthenticated save should fail", result.isFailure)
    }

    @Test
    fun saveUserProfile_crossUserWrite_failsWithPermissionDenied(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val bobUid = "bob_target_user_id"

        val docRef = firestore.collection("users").document(bobUid)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                docRef.set(
                    mapOf(
                        "userId" to bobUid,
                        "username" to "hacked",
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                ).await()
            }
            fail("Expected FirebaseFirestoreException PERMISSION_DENIED")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun updateAvatarAndFrame_ownerSucceedsAndCrossUserBioOrFrameEditFails(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = FirestoreUserRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveUserProfile(
                FirestoreUserProfile(
                    userId = aliceUid,
                    username = "alice_frames",
                    displayName = "Alice",
                    bio = "Original Alice bio",
                    avatarFrame = "none"
                )
            )
        }

        val frameResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.updateAvatarAndFrame(
                avatarData = "data:image/jpeg;base64,/9j/4AAQSkZJRg==",
                clearAvatar = false,
                avatarFrame = "neon_pulse"
            )
        }
        assertTrue("Owner avatar/frame update should succeed", frameResult.isSuccess)
        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { aliceRepo.getUserProfile(aliceUid).getOrNull() }
        assertEquals("neon_pulse", fetched?.avatarFrame)

        // Bob cannot modify Alice's bio or avatarFrame
        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .update(
                        mapOf(
                            "bio" to "Hijacked bio",
                            "avatarFrame" to "golden_halo",
                            "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                        )
                    )
                    .await()
            }
            fail("Expected PERMISSION_DENIED when non-owner edits Alice's bio/frame")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@viberoom.app"
        const val BOB_EMAIL = "bob@viberoom.app"
        const val DEFAULT_TIMEOUT_MS = 6000L
    }
}
