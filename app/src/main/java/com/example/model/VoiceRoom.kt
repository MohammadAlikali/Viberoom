package com.example.model

enum class RoomCategory(val displayName: String, val iconEmoji: String) {
    ALL("All Vibe Rooms", "🌐"),
    LANGUAGE_EXCHANGE("Language Practice", "🗣️"),
    CHILL_VIBES("Chill & Chat", "☕"),
    MUSIC_JAM("Music & Beats", "🎵"),
    TECH_TALK("Tech & Startups", "💻"),
    STORYTELLING("World Stories", "✨"),
    CULTURE("Culture & Travel", "✈️")
}

data class VoiceRoom(
    val id: String,
    val title: String,
    val description: String,
    val category: RoomCategory,
    val host: UserProfile,
    val speakers: List<UserProfile> = emptyList(),
    val listeners: List<UserProfile> = emptyList(),
    val activeSpeakerId: String? = null,
    val listenersCount: Int = 12,
    val isLive: Boolean = true,
    val tags: List<String> = emptyList(),
    val raisedHandIds: Set<String> = emptySet(),
    val mutedPeerIds: Set<String> = emptySet(),
    val connectedPeerIds: Set<String> = emptySet(),
    val isPrivate: Boolean = false,
    val coHostIds: Set<String> = emptySet(),
    val aiSummary: String? = null
)

data class RoomReaction(
    val id: String,
    val emoji: String,
    val senderName: String,
    val offsetX: Float = 0f
)
