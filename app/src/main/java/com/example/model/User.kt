package com.example.model

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val bio: String,
    val countryName: String,
    val countryCode: String,
    val flagEmoji: String,
    val cityName: String = "",
    val distanceKm: Double? = null,
    val avatarColorHex: Long = 0xFF8C52FF,
    val avatarInitials: String = "",
    val avatarUrl: String? = null,
    val avatarData: String? = null,
    val avatarFrame: String = "none",
    val interests: List<String> = emptyList(),
    val languagesSpoken: List<String> = emptyList(),
    val nativeLanguages: List<String> = emptyList(),
    val learningLanguages: List<String> = emptyList(),
    val learningLanguageLevels: Map<String, Int> = emptyMap(),
    val proficiencyLevel: String = "",
    val communicationGoal: String = "",
    val vibesScore: Int = 120,
    val connectionsCount: Int = 24,
    val roomsJoinedCount: Int = 8,
    val followersCount: Int = 342,
    val followingCount: Int = 186,
    val roomsHostedCount: Int = 14,
    val isOnline: Boolean = true,
    val showOnlineStatus: Boolean = true,
    val lastSeenVisibility: String = "everyone",
    val lastSeenAtMs: Long? = null,
    val lastActive: String = "Just now",
    val isFollowing: Boolean = false,
    val isVerified: Boolean = false,
    val isBlocked: Boolean = false,
    val isDiscoveryVisible: Boolean = true,
    val allowDirectMessages: Boolean = true,
    val vibeXp: Int = 0,
    val vibeLevel: Int = 1,
    val streakDays: Int = 0,
    val unlockedBadges: List<String> = emptyList()
) {
    val displayInitials: String
        get() = if (avatarInitials.isNotEmpty()) avatarInitials
        else name.split(" ").mapNotNull { it.firstOrNull()?.uppercase() }.take(2).joinToString("")

    val hasCustomAvatar: Boolean
        get() = !avatarData.isNullOrBlank() || !avatarUrl.isNullOrBlank()

    fun canShowPresenceTo(isContactOrFollowing: Boolean): Boolean {
        if (!showOnlineStatus) return false
        return when (lastSeenVisibility) {
            "nobody" -> false
            "contacts" -> isContactOrFollowing
            else -> true
        }
    }
}
