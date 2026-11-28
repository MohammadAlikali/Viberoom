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

enum class RoomLifecycleState {
    CREATED,
    WAITING,
    LIVE,
    ENDING,
    ENDED,
    EXPIRED;

    fun isDiscoverable(): Boolean = this == CREATED || this == WAITING || this == LIVE

    companion object {
        fun fromString(raw: String?): RoomLifecycleState =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: LIVE
    }
}

enum class RoomRole {
    OWNER,
    HOST,
    CO_HOST,
    MODERATOR,
    SPEAKER,
    LISTENER
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
    val listenersCount: Int = 0,
    val isLive: Boolean = true,
    val tags: List<String> = emptyList(),
    val raisedHandIds: Set<String> = emptySet(),
    val mutedPeerIds: Set<String> = emptySet(),
    val hostMutedPeerIds: Set<String> = emptySet(),
    val connectedPeerIds: Set<String> = emptySet(),
    val seatAssignments: Map<Int, String> = emptyMap(),
    val isPrivate: Boolean = false,
    val coHostIds: Set<String> = emptySet(),
    val aiSummary: String? = null,
    val lifecycleState: RoomLifecycleState = if (isLive) RoomLifecycleState.LIVE else RoomLifecycleState.ENDED,
    val backgroundTheme: String = "aurora",
    val isStageLocked: Boolean = false,
    val moderatorIds: Set<String> = emptySet(),
    val bannedIds: Set<String> = emptySet(),
    val temporaryBanExpiries: Map<String, Long> = emptyMap(),
    val invitedSpeakerIds: Set<String> = emptySet(),
    val successorHostId: String? = null,
    val pinnedNotice: String? = null,
    val maxAudioParticipants: Int = 6,
    val hostLastHeartbeatMs: Long = 0L,
    val updatedAtMs: Long = 0L
) {
    fun roleOf(userId: String): RoomRole = when {
        userId == host.id -> RoomRole.HOST
        userId in coHostIds -> RoomRole.CO_HOST
        userId in moderatorIds -> RoomRole.MODERATOR
        speakers.any { it.id == userId } -> RoomRole.SPEAKER
        else -> RoomRole.LISTENER
    }

    fun isModeratorOrHigher(userId: String): Boolean =
        userId == host.id || userId in coHostIds || userId in moderatorIds

    fun isUserBanned(userId: String, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (userId in bannedIds) return true
        val expiry = temporaryBanExpiries[userId] ?: return false
        return expiry > nowMs
    }

    fun orderedSeatSlots(): List<UserProfile?> {
        val totalSeats = maxAudioParticipants.coerceIn(2, 6)
        val slots = arrayOfNulls<UserProfile>(totalSeats)
        val speakerById = speakers.associateBy { it.id }
        val assignedSpeakerIds = mutableSetOf<String>()

        // Place speakers who have an explicit valid seat assignment first
        seatAssignments.entries
            .sortedBy { it.key }
            .forEach { (seatIdx, uid) ->
                if (seatIdx in 0 until totalSeats && slots[seatIdx] == null) {
                    val profile = speakerById[uid]
                    if (profile != null && uid !in assignedSpeakerIds) {
                        slots[seatIdx] = profile
                        assignedSpeakerIds.add(uid)
                    }
                }
            }

        // Ensure host is placed (preferring seat 0 if free)
        speakerById[host.id]?.let { hostProfile ->
            if (host.id !in assignedSpeakerIds) {
                val targetIdx = if (slots[0] == null) 0 else slots.indexOfFirst { it == null }
                if (targetIdx >= 0) {
                    slots[targetIdx] = hostProfile
                    assignedSpeakerIds.add(host.id)
                }
            }
        }

        // Place remaining speakers deterministically into next available free seats
        speakers.forEach { sp ->
            if (sp.id !in assignedSpeakerIds) {
                val freeIdx = slots.indexOfFirst { it == null }
                if (freeIdx >= 0) {
                    slots[freeIdx] = sp
                    assignedSpeakerIds.add(sp.id)
                }
            }
        }
        return slots.toList()
    }
}

data class RoomReaction(
    val id: String,
    val emoji: String,
    val senderName: String,
    val offsetX: Float = 0f
)
