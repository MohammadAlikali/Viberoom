package com.example.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FirestoreUserProfile(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String? = null,
    val email: String? = null,
    val country: String? = null,
    val countryCode: String? = null,
    val city: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null,
    val avatarData: String? = null,
    val avatarFrame: String? = null,
    val interests: List<String> = emptyList(),
    val languages: List<String> = emptyList(),
    val nativeLanguages: List<String> = emptyList(),
    val learningLanguages: List<String> = emptyList(),
    val learningLanguageLevels: Map<String, Int> = emptyMap(),
    val proficiencyLevel: String? = null,
    val communicationGoal: String? = null,
    @get:PropertyName("isOnline")
    @field:PropertyName("isOnline")
    val isOnline: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val lastSeenVisibility: String = "everyone",
    val lastSeenAtMs: Long? = null,
    @get:PropertyName("isDiscoveryVisible")
    @field:PropertyName("isDiscoveryVisible")
    val isDiscoveryVisible: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val vibeXp: Int = 0,
    val vibeLevel: Int = 1,
    val streakDays: Int = 0,
    val unlockedBadges: List<String> = emptyList(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreFollow(
    @DocumentId
    val id: String = "",
    val followerId: String = "",
    val followingId: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreBlockedUser(
    @DocumentId
    val id: String = "",
    val blockedUserId: String = "",
    val reason: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreFriendRequest(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val senderName: String? = null,
    val receiverName: String? = null,
    val status: String = "pending",
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreReport(
    @DocumentId
    val id: String = "",
    val reporterId: String = "",
    val targetType: String = "user",
    val targetId: String = "",
    val reason: String = "",
    val details: String? = null,
    val status: String = "pending",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreConversation(
    @DocumentId
    val id: String = "",
    val participantIds: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastSenderId: String = "",
    val pinnedMessageId: String? = null,
    val pinnedMessageText: String? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreChatMessage(
    @DocumentId
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val receiverId: String = "",
    val text: String = "",
    @get:PropertyName("isRead")
    @field:PropertyName("isRead")
    val isRead: Boolean = false,
    val mediaType: String = "text",
    val audioDurationSec: Int? = null,
    val mediaData: String? = null,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    @get:PropertyName("isEdited")
    @field:PropertyName("isEdited")
    val isEdited: Boolean = false,
    @get:PropertyName("isDeleted")
    @field:PropertyName("isDeleted")
    val isDeleted: Boolean = false,
    @get:PropertyName("isForwarded")
    @field:PropertyName("isForwarded")
    val isForwarded: Boolean = false,
    val expiresAtMs: Long? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVoiceRoom(
    @DocumentId
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "ALL",
    val hostId: String = "",
    val hostName: String = "",
    val hostCountry: String? = null,
    val hostFlagEmoji: String? = null,
    val hostAvatarColor: String? = null,
    @get:PropertyName("isLive")
    @field:PropertyName("isLive")
    val isLive: Boolean = true,
    val listenersCount: Long = 0L,
    val speakersCount: Long = 1L,
    val activeSpeakerId: String? = null,
    val tags: List<String> = emptyList(),
    @get:PropertyName("isPrivate")
    @field:PropertyName("isPrivate")
    val isPrivate: Boolean = false,
    val coHostIds: List<String> = emptyList(),
    val aiSummary: String? = null,
    val lifecycleState: String = "LIVE",
    val backgroundTheme: String = "aurora",
    @get:PropertyName("isStageLocked")
    @field:PropertyName("isStageLocked")
    val isStageLocked: Boolean = false,
    val moderatorIds: List<String> = emptyList(),
    val bannedIds: List<String> = emptyList(),
    val temporaryBans: Map<String, Long> = emptyMap(),
    val invitedSpeakerIds: List<String> = emptyList(),
    val successorHostId: String? = null,
    val pinnedNotice: String? = null,
    val maxAudioParticipants: Long = 6L,
    val hostLastHeartbeatMs: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestorePost(
    @DocumentId
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorFlag: String? = null,
    val authorCountry: String? = null,
    val content: String = "",
    val category: String = "General",
    val hashtags: List<String> = emptyList(),
    val mediaType: String = "text",
    val mediaData: String? = null,
    val mediaUrl: String? = null,
    val likesCount: Long = 0L,
    val commentsCount: Long = 0L,
    val likedUserIds: List<String> = emptyList(),
    val postType: String = "standard", // "standard", "poll", "qa"
    val pollOptions: List<String> = emptyList(),
    val pollVotes: Map<String, Int> = emptyMap(),
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreComment(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorFlag: String? = null,
    val content: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreStory(
    @DocumentId
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorFlag: String? = null,
    val authorCountry: String? = null,
    val text: String = "",
    val category: String = "Daily",
    val backgroundHex: Long = 0xFF6A1B9A,
    val viewedByIds: List<String> = emptyList(),
    val reactions: Map<String, String> = emptyMap(),
    val expiresAtMs: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreSavedPost(
    @DocumentId
    val id: String = "",
    val postId: String = "",
    val authorName: String = "",
    val contentPreview: String = "",
    @ServerTimestamp
    val savedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreNotification(
    @DocumentId
    val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val type: String = "system",
    val title: String = "",
    val body: String = "",
    val targetId: String? = null,
    @get:PropertyName("isRead")
    @field:PropertyName("isRead")
    val isRead: Boolean = false,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVocabItem(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val wordOrPhrase: String = "",
    val translation: String = "",
    val language: String = "English",
    val exampleSentence: String = "",
    val masteryLevel: Int = 1,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVoiceCall(
    @DocumentId
    val id: String = "",
    val callerId: String = "",
    val callerName: String = "",
    val callerFlag: String? = null,
    val calleeId: String = "",
    val calleeName: String = "",
    val conversationId: String = "",
    val status: String = "ringing", // ringing, connected, declined, ended
    val offerSdp: String? = null,
    val answerSdp: String? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreIceCandidate(
    @DocumentId
    val id: String = "",
    val senderId: String = "",
    val sdpMid: String = "",
    val sdpMLineIndex: Int = 0,
    val sdp: String = "",
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreRoomPeer(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    @get:PropertyName("isSpeaker")
    @field:PropertyName("isSpeaker")
    val isSpeaker: Boolean = false,
    @get:PropertyName("isMuted")
    @field:PropertyName("isMuted")
    val isMuted: Boolean = true,
    @get:PropertyName("isHandRaised")
    @field:PropertyName("isHandRaised")
    val isHandRaised: Boolean = false,
    val hostMuted: Boolean = false,
    val seatIndex: Int = -1,
    val lastHeartbeatMs: Long = 0L,
    val flagEmoji: String = "🌐",
    val avatarFrame: String = "none",
    val avatarColorHex: Long = 0xFF8C52FF,
    @ServerTimestamp
    val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreRoomSignal(
    @DocumentId
    val id: String = "",
    val fromUserId: String = "",
    val toUserId: String = "",
    val type: String = "", // offer, answer, ice
    val sdp: String = "",
    val sdpMid: String? = null,
    val sdpMLineIndex: Int? = null,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVoiceRoomEvent(
    @DocumentId
    val id: String = "",
    val roomId: String = "",
    val type: String = "reaction", // entrance, gift, sound, reaction, chat, system
    val senderId: String = "",
    val senderName: String = "",
    val senderFlag: String = "🌐",
    val senderVipLevel: Int = 1,
    val payloadId: String = "",
    val text: String = "",
    val recipientId: String? = null,
    val recipientName: String? = null,
    val giftValueXp: Long = 0L,
    val timestampMs: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class FirestoreVoiceRoomPoll(
    @DocumentId
    val id: String = "",
    val roomId: String = "",
    val question: String = "",
    val options: List<String> = emptyList(),
    val votes: Map<String, Int> = emptyMap(),
    val createdById: String = "",
    val createdByName: String = "",
    @get:PropertyName("isClosed")
    @field:PropertyName("isClosed")
    val isClosed: Boolean = false,
    val createdAtMs: Long = 0L,
    @ServerTimestamp
    val createdAt: Timestamp? = null
)
