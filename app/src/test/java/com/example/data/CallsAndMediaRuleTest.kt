package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class CallsAndMediaRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun oneToOneVoiceCall_signalingAndThirdPartyIsolation(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)
        signInTestUser(CHARLIE_EMAIL)

        // Caller (Alice) starts a call
        signInTestUser(ALICE_EMAIL)
        val aliceCalls = FirestoreCallRepository(firestore)
        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(16)}"
        val createRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceCalls.createOutgoingCall(
                callId = callId,
                calleeId = bobUid,
                calleeName = "Bob",
                callerName = "Alice",
                callerFlag = "🇺🇸",
                conversationId = "dm_${aliceUid}_$bobUid",
                offerSdp = "v=0\r\no=- 1001 2 IN IP4 127.0.0.1\r\n"
            )
        }
        assertTrue("Create call should succeed: ${createRes.exceptionOrNull()}", createRes.isSuccess)

        // Third party (Charlie) cannot read or send ICE candidates
        signInTestUser(CHARLIE_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("calls").document(callId).get().await()
            }
            fail("Expected PERMISSION_DENIED when third party reads call")
        } catch (e: FirebaseFirestoreException) {
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, e.code)
        }

        // Callee (Bob) observes ringing call, answers, and sends ICE candidate
        signInTestUser(BOB_EMAIL)
        val bobCalls = FirestoreCallRepository(firestore)
        val ringing = withTimeout(FLOW_TIMEOUT_MS) {
            bobCalls.observeIncomingRingingCalls(bobUid).first { list -> list.any { it.id == callId } }
        }
        assertEquals(aliceUid, ringing.first { it.id == callId }.callerId)

        val ansRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobCalls.answerCall(callId, "v=0\r\no=- 2002 2 IN IP4 127.0.0.1\r\n")
        }
        assertTrue("Answer call should succeed: ${ansRes.exceptionOrNull()}", ansRes.isSuccess)

        val bobCandRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobCalls.sendCallIceCandidate(callId, "0", 0, "candidate:1 1 UDP 2122252543 192.0.2.2 50002 typ host")
        }
        assertTrue("Bob ICE candidate should succeed: ${bobCandRes.exceptionOrNull()}", bobCandRes.isSuccess)

        // Alice ends call
        signInTestUser(ALICE_EMAIL)
        val endRes = withTimeout(DEFAULT_TIMEOUT_MS) { aliceCalls.updateCallStatus(callId, "ended") }
        assertTrue("End call should succeed: ${endRes.exceptionOrNull()}", endRes.isSuccess)
    }

    @Test
    fun voiceRoomWebRtcMesh_peersAndSignalsEnforceRules(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val roomRepo = FirestoreVoiceRoomRepository(firestore)
        val room = withTimeout(DEFAULT_TIMEOUT_MS) {
            roomRepo.createVoiceRoom(
                title = "WebRTC Audio Room ${UUID.randomUUID().toString().take(6)}",
                description = "Live testing",
                category = com.example.model.RoomCategory.LANGUAGE_EXCHANGE,
                hostUser = com.example.model.UserProfile(
                    id = aliceUid,
                    name = "Alice",
                    username = "alice",
                    countryCode = "US",
                    countryName = "United States",
                    flagEmoji = "🇺🇸",
                    bio = "",
                    interests = emptyList(),
                    languagesSpoken = listOf("English")
                ),
                tags = emptyList()
            )
        }

        val aliceCalls = FirestoreCallRepository(firestore)
        val peerRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceCalls.upsertRoomPeer(room.id, "Alice", isSpeaker = true, isMuted = false)
        }
        assertTrue("Upsert own room peer should succeed: ${peerRes.exceptionOrNull()}", peerRes.isSuccess)

        signInTestUser(BOB_EMAIL)
        val bobCalls = FirestoreCallRepository(firestore)
        val bobPeerRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobCalls.upsertRoomPeer(room.id, "Bob", isSpeaker = false, isMuted = true)
        }
        assertTrue("Bob peer join should succeed: ${bobPeerRes.exceptionOrNull()}", bobPeerRes.isSuccess)

        val signalRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobCalls.sendRoomSignal(room.id, aliceUid, "offer", "v=0\r\no=- 3003 2 IN IP4 127.0.0.1\r\n")
        }
        assertTrue("Send room signal should succeed: ${signalRes.exceptionOrNull()}", signalRes.isSuccess)

        signInTestUser(ALICE_EMAIL)
        val signals = withTimeout(FLOW_TIMEOUT_MS) {
            aliceCalls.observeRoomSignalsForUser(room.id, aliceUid).first { it.any { s -> s.fromUserId == bobUid } }
        }
        assertEquals("offer", signals.first { it.fromUserId == bobUid }.type)
    }

    @Test
    fun chatAndFeedMedia_imageVoiceAndVideo_persistViaFirestore(): Unit = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val bobUid = signInTestUser(BOB_EMAIL)

        signInTestUser(ALICE_EMAIL)
        val chatRepo = FirestoreChatRepository(firestore)
        val convId = "dm_${minOf(aliceUid, bobUid)}_${maxOf(aliceUid, bobUid)}_${UUID.randomUUID().toString().take(6)}"

        val imgMsgRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendMessage(
                conversationId = convId,
                receiverId = bobUid,
                text = "📷 Photo",
                senderName = "Alice",
                messageId = "msg_img_${UUID.randomUUID().toString().take(6)}",
                mediaType = "image",
                mediaData = "data:image/jpeg;base64,/9j/4AAQSkZJRg=="
            )
        }
        assertTrue("Image message should succeed: ${imgMsgRes.exceptionOrNull()}", imgMsgRes.isSuccess)

        val voiceMsgRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            chatRepo.sendMessage(
                conversationId = convId,
                receiverId = bobUid,
                text = "🎙️ Voice message (5s)",
                senderName = "Alice",
                messageId = "msg_aud_${UUID.randomUUID().toString().take(6)}",
                mediaType = "voice",
                audioDurationSec = 5,
                mediaData = "data:audio/mp4;base64,AAAAFGZ0eXA="
            )
        }
        assertTrue("Voice message should succeed: ${voiceMsgRes.exceptionOrNull()}", voiceMsgRes.isSuccess)

        val postRepo = FirestorePostRepository(firestore)
        val imgPostId = withTimeout(DEFAULT_TIMEOUT_MS) {
            postRepo.createPost(
                content = "Photo moment",
                authorName = "Alice",
                authorCountry = "United States",
                authorFlag = "🇺🇸",
                category = "Culture",
                mediaType = "image",
                mediaData = "data:image/jpeg;base64,/9j/4AAQSkZJRg=="
            ).getOrThrow()
        }
        val vidPostId = withTimeout(DEFAULT_TIMEOUT_MS) {
            postRepo.createPost(
                content = "Video moment",
                authorName = "Alice",
                authorCountry = "United States",
                authorFlag = "🇺🇸",
                category = "Music",
                mediaType = "video",
                mediaUrl = "https://example.com/clip.mp4"
            ).getOrThrow()
        }

        val imgSnap = withTimeout(DEFAULT_TIMEOUT_MS) { firestore.collection("posts").document(imgPostId).get().await() }
        val vidSnap = withTimeout(DEFAULT_TIMEOUT_MS) { firestore.collection("posts").document(vidPostId).get().await() }
        assertEquals("image", imgSnap.getString("mediaType"))
        assertEquals("video", vidSnap.getString("mediaType"))
        assertEquals("https://example.com/clip.mp4", vidSnap.getString("mediaUrl"))
    }

    private companion object {
        const val ALICE_EMAIL = "alice_media@viberoom.app"
        const val BOB_EMAIL = "bob_media@viberoom.app"
        const val CHARLIE_EMAIL = "charlie_media@viberoom.app"
        const val DEFAULT_TIMEOUT_MS = 6000L
        const val FLOW_TIMEOUT_MS = 4000L
    }
}
