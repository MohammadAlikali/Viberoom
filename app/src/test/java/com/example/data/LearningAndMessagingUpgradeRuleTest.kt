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

class LearningAndMessagingUpgradeRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun stories_createViewReactAndOwnerDelete_enforcesSecurityRules(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceLearning = FirestoreLearningRepository(firestore)
        val storyId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceLearning.createStory(
                text = "Learning Korean in Seoul ${UUID.randomUUID()}",
                authorName = "Alice",
                authorFlag = "🇺🇸",
                authorCountry = "United States",
                category = "Languages"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            aliceLearning.observeActiveStories().first { list -> list.any { it.id == storyId } }
        }
        assertTrue(emitted.any { it.id == storyId })

        val bobUid = signInTestUser(BOB_EMAIL)
        val bobLearning = FirestoreLearningRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) { bobLearning.markStoryViewed(storyId) }
        val reactRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobLearning.reactToStory(storyId, emptyMap(), "🔥")
        }
        assertTrue("Bob reaction should succeed: ${reactRes.exceptionOrNull()}", reactRes.isSuccess)

        val bobDelete = withTimeout(DEFAULT_TIMEOUT_MS) { bobLearning.deleteStory(storyId) }
        assertTrue("Non-author delete should fail", bobDelete.isFailure)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            bobDelete.exceptionOrNull()?.unwrapFirestoreException()?.code
        )

        val snap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("stories").document(storyId).get().await()
        }
        @Suppress("UNCHECKED_CAST")
        val viewed = snap.get("viewedByIds") as? List<String> ?: emptyList()
        assertTrue(viewed.contains(aliceUid) && viewed.contains(bobUid))

        signInTestUser(ALICE_EMAIL)
        val aliceDelete = withTimeout(DEFAULT_TIMEOUT_MS) { aliceLearning.deleteStory(storyId) }
        assertTrue("Owner delete should succeed", aliceDelete.isSuccess)
    }

    @Test
    fun vocabulary_ownerCrudAndCrossUserIsolation(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceLearning = FirestoreLearningRepository(firestore)
        val vocabId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceLearning.saveVocabItem(
                wordOrPhrase = "تشکر",
                translation = "Thank you",
                language = "Persian (Dari)",
                exampleSentence = "تشکر از شما!"
            ).getOrThrow()
        }

        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceLearning.updateVocabMastery(vocabId, 3)
        }
        assertTrue("Mastery update should succeed", updateRes.isSuccess)

        val list = withTimeout(FLOW_TIMEOUT_MS) {
            aliceLearning.observeVocabulary().first { items -> items.any { it.id == vocabId && it.masteryLevel == 3 } }
        }
        assertTrue(list.any { it.id == vocabId })

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid).collection("vocabulary").get().await()
            }
            fail("Expected PERMISSION_DENIED when reading another user's vocabulary")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }
    }

    @Test
    fun notifications_crossUserDeliveryAndRecipientRead(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val aliceLearning = FirestoreLearningRepository(firestore)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceLearning.createNotificationForUser(
                recipientId = bobUid,
                senderName = "Alice",
                type = "post_comment",
                title = "Alice commented on your post",
                body = "Welcome to VibeRoom!",
                targetId = "post_xyz"
            )
        }

        // Alice cannot list Bob's notifications
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(bobUid).collection("notifications").get().await()
            }
            fail("Expected PERMISSION_DENIED when Alice reads Bob's notifications")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }

        signInTestUser(BOB_EMAIL)
        val bobLearning = FirestoreLearningRepository(firestore)
        val items = withTimeout(FLOW_TIMEOUT_MS) {
            bobLearning.observeNotifications().first { it.any { n -> n.senderId == aliceUid } }
        }
        val target = items.first { it.senderId == aliceUid }
        assertFalse(target.isRead)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            bobLearning.markNotificationRead(target.id)
        }
        val updated = withTimeout(FLOW_TIMEOUT_MS) {
            bobLearning.observeNotifications().first { list ->
                list.any { it.id == target.id && it.isRead }
            }
        }
        assertTrue(updated.any { it.id == target.id && it.isRead })
    }

    @Test
    fun savedPosts_bookmarkAndUnbookmark(): Unit = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val postsRepo = FirestorePostRepository(firestore)
        val postId = withTimeout(DEFAULT_TIMEOUT_MS) {
            postsRepo.createPost(
                content = "Learning tips #dari #english",
                authorName = "Alice",
                authorCountry = "Canada",
                authorFlag = "🇨🇦"
            ).getOrThrow()
        }
        val postSnap = withTimeout(DEFAULT_TIMEOUT_MS) {
            firestore.collection("posts").document(postId).get().await()
        }.toObject(FirestorePost::class.java)!!

        withTimeout(DEFAULT_TIMEOUT_MS) { postsRepo.savePost(postSnap) }
        val savedIds = withTimeout(FLOW_TIMEOUT_MS) {
            postsRepo.observeSavedPostIds().first { postId in it }
        }
        assertTrue(postId in savedIds)

        withTimeout(DEFAULT_TIMEOUT_MS) { postsRepo.unsavePost(postId) }
        val afterUnsave = withTimeout(FLOW_TIMEOUT_MS) {
            postsRepo.observeSavedPostIds().first { postId !in it }
        }
        assertFalse(postId in afterUnsave)
    }

    @Test
    fun chatUpgrades_replyEditDeleteReactAndPin(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val chatRepo = FirestoreChatRepository(firestore)
        val convId = "dm_${minOf(aliceUid, bobUid)}_${maxOf(aliceUid, bobUid)}_${UUID.randomUUID().toString().take(6)}"
        val msgId = "msg_${UUID.randomUUID().toString().take(8)}"

        withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendMessage(
                conversationId = convId,
                receiverId = bobUid,
                text = "Original reply text",
                senderName = "Alice",
                messageId = msgId,
                replyToMessageId = "prev_1",
                replyToText = "Hi Alice",
                replyToSenderName = "Bob"
            )
        }

        val editRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.editMessage(convId, msgId, "Updated message text")
        }
        assertTrue("Edit own message should succeed: ${editRes.exceptionOrNull()}", editRes.isSuccess)

        val pinRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.pinMessage(convId, msgId, "Updated message text")
        }
        assertTrue("Pin message should succeed: ${pinRes.exceptionOrNull()}", pinRes.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobChatRepo = FirestoreChatRepository(firestore)
        val reactRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobChatRepo.toggleMessageReaction(convId, msgId, emptyMap(), "❤️")
        }
        assertTrue("Participant reaction should succeed: ${reactRes.exceptionOrNull()}", reactRes.isSuccess)

        val bobEdit = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobChatRepo.editMessage(convId, msgId, "Tamper")
        }
        assertTrue("Non-sender edit must fail", bobEdit.isFailure)

        signInTestUser(ALICE_EMAIL)
        val deleteRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.softDeleteMessage(convId, msgId)
        }
        assertTrue("Sender soft-delete should succeed: ${deleteRes.exceptionOrNull()}", deleteRes.isSuccess)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_phase3@viberoom.app"
        const val BOB_EMAIL = "bob_phase3@viberoom.app"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
