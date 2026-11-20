package com.example.model

data class ChatMessage(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val text: String,
    val timestamp: String,
    val isMine: Boolean,
    val isRead: Boolean = true,
    val mediaType: String = "text", // "text", "voice", "image"
    val audioDurationSec: Int? = null,
    val mediaData: String? = null,
    val mediaUrl: String? = null,
    val replyToMessageId: String? = null,
    val replyToText: String? = null,
    val replyToSenderName: String? = null,
    val reactions: Map<String, String> = emptyMap(),
    val isEdited: Boolean = false,
    val isDeleted: Boolean = false,
    val isForwarded: Boolean = false,
    val createdAtMs: Long = System.currentTimeMillis(),
    val expiresAtMs: Long? = null
)

data class Conversation(
    val id: String,
    val peerUser: UserProfile,
    val lastMessage: String,
    val lastTimestamp: String,
    val unreadCount: Int = 0,
    val pinnedMessageId: String? = null,
    val pinnedMessageText: String? = null
)
