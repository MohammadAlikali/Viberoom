package com.example.model

data class CommunityChannel(
    val id: String,
    val name: String,
    val type: String = "general", // "general", "announcements", "voice"
    val description: String = ""
)

data class CommunityPost(
    val id: String,
    val authorName: String,
    val authorHandle: String,
    val authorFlag: String,
    val authorColorHex: Long,
    val content: String,
    val timestamp: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0,
    val isLiked: Boolean = false
)

data class Community(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val iconEmoji: String,
    val memberCount: Int,
    val isPrivate: Boolean = false,
    val isMember: Boolean = false,
    val channels: List<CommunityChannel> = emptyList(),
    val posts: List<CommunityPost> = emptyList()
)
