package com.example.data

import com.example.model.ChatMessage
import com.example.model.Community
import com.example.model.CommunityPost
import com.example.model.Conversation
import com.example.model.Country
import com.example.model.OnboardingStep
import com.example.model.RegistrationForm
import com.example.model.RoomCategory
import com.example.model.RoomReaction
import com.example.model.UserProfile
import com.example.model.VoiceRoom
import com.example.ui.localization.AppLanguage
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class VibeRoomRepository(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) {

    // Onboarding & Auth State
    private val _onboardingStep = MutableStateFlow(OnboardingStep.SPLASH)
    val onboardingStep: StateFlow<OnboardingStep> = _onboardingStep.asStateFlow()

    private val _registrationForm = MutableStateFlow(
        RegistrationForm(country = CountryData.defaultCountry)
    )
    val registrationForm: StateFlow<RegistrationForm> = _registrationForm.asStateFlow()

    private val defaultUser = UserProfile(
        id = "guest_user",
        name = "New Member",
        username = "member",
        bio = "Welcome to VibeRoom! Connect with real people around the world.",
        countryName = "Global",
        countryCode = "GL",
        flagEmoji = "🌐",
        avatarColorHex = 0xFF8C52FF,
        interests = listOf("Voice Rooms", "Culture", "Languages"),
        languagesSpoken = listOf("English"),
        vibesScore = 100,
        isOnline = true
    )

    private val _currentUser = MutableStateFlow(defaultUser)
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    private val _users = MutableStateFlow<List<UserProfile>>(emptyList())
    val users: StateFlow<List<UserProfile>> = _users.asStateFlow()

    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()

    private val _messages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val messages: StateFlow<Map<String, List<ChatMessage>>> = _messages.asStateFlow()

    private val _voiceRooms = MutableStateFlow<List<VoiceRoom>>(emptyList())
    val voiceRooms: StateFlow<List<VoiceRoom>> = _voiceRooms.asStateFlow()

    private val _communities = MutableStateFlow<List<Community>>(emptyList())
    val communities: StateFlow<List<Community>> = _communities.asStateFlow()

    // Active Room State (Live Session)
    private val _activeRoom = MutableStateFlow<VoiceRoom?>(null)
    val activeRoom: StateFlow<VoiceRoom?> = _activeRoom.asStateFlow()

    private val _isMicMuted = MutableStateFlow(true)
    val isMicMuted: StateFlow<Boolean> = _isMicMuted.asStateFlow()

    private val _isHandRaised = MutableStateFlow(false)
    val isHandRaised: StateFlow<Boolean> = _isHandRaised.asStateFlow()

    private val _isSpeaker = MutableStateFlow(false)
    val isSpeaker: StateFlow<Boolean> = _isSpeaker.asStateFlow()

    private val _activeReactions = MutableStateFlow<List<RoomReaction>>(emptyList())
    val activeReactions: StateFlow<List<RoomReaction>> = _activeReactions.asStateFlow()

    // Preferences & Theme
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _appTheme = MutableStateFlow(AppTheme.PURE_WHITE)
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _accentColorHex = MutableStateFlow<Long?>(null)
    val accentColorHex: StateFlow<Long?> = _accentColorHex.asStateFlow()

    private val _appLanguage = MutableStateFlow(AppLanguage.DARI_PERSIAN)
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    // Location & Nearby Discovery
    private val _isLocationSharingEnabled = MutableStateFlow(true)
    val isLocationSharingEnabled: StateFlow<Boolean> = _isLocationSharingEnabled.asStateFlow()

    private val _discoveryRadiusKm = MutableStateFlow(50)
    val discoveryRadiusKm: StateFlow<Int> = _discoveryRadiusKm.asStateFlow()

    // Blocked user IDs & Follow state
    private val _blockedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val blockedUserIds: StateFlow<Set<String>> = _blockedUserIds.asStateFlow()

    private val _followingIds = MutableStateFlow<Set<String>>(emptySet())
    val followingIds: StateFlow<Set<String>> = _followingIds.asStateFlow()

    private val _followerIds = MutableStateFlow<Set<String>>(emptySet())
    val followerIds: StateFlow<Set<String>> = _followerIds.asStateFlow()

    private var rawFirestoreConversations: List<FirestoreConversation> = emptyList()
    private var rawFirestoreUsers: List<FirestoreUserProfile> = emptyList()
    private var followerIdsByUser: Map<String, Set<String>> = emptyMap()
    private var followingIdsByUser: Map<String, Set<String>> = emptyMap()
    private var syncedCurrentUid: String = ""

    fun setOnboardingStep(step: OnboardingStep) {
        _onboardingStep.value = step
    }

    fun updateRegistrationForm(form: RegistrationForm) {
        _registrationForm.value = form
    }

    fun completeOnboarding() {
        val form = _registrationForm.value
        val country = form.country ?: CountryData.defaultCountry
        _currentUser.update { current ->
            current.copy(
                name = "${form.firstName} ${form.lastName}".trim().ifEmpty { current.name },
                countryName = if (current.countryCode == "GL" || current.countryName == "Global") country.name else current.countryName,
                countryCode = if (current.countryCode == "GL" || current.countryName == "Global") country.code else current.countryCode,
                flagEmoji = if (current.countryCode == "GL" || current.countryName == "Global") country.flagEmoji else current.flagEmoji
            )
        }
        _onboardingStep.value = OnboardingStep.COMPLETED
    }

    fun handleGoogleUserSignedIn(
        user: com.google.firebase.auth.FirebaseUser,
        existingProfile: FirestoreUserProfile? = null,
        selectedCountry: Country? = null
    ) {
        val fallbackName = user.displayName?.ifBlank { null }
            ?: user.email?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
            ?: "Vibe Explorer"
        val cleanHandle = existingProfile?.username?.takeIf { it.isNotBlank() }
            ?: (fallbackName.filter { it.isLetterOrDigit() }.lowercase().take(20).ifBlank { "viber_${user.uid.take(6)}" })

        val resolvedCountry = CountryData.allCountries.find {
            it.code.equals(existingProfile?.countryCode, ignoreCase = true) ||
                it.name.equals(existingProfile?.country, ignoreCase = true)
        } ?: selectedCountry

        _currentUser.update { current ->
            current.copy(
                id = user.uid,
                name = existingProfile?.displayName?.takeIf { it.isNotBlank() } ?: fallbackName,
                username = cleanHandle.removePrefix("@"),
                bio = existingProfile?.bio?.takeIf { it.isNotBlank() } ?: current.bio,
                countryName = existingProfile?.country?.takeIf { it.isNotBlank() }
                    ?: resolvedCountry?.name ?: "Global",
                countryCode = existingProfile?.countryCode?.takeIf { it.isNotBlank() }
                    ?: resolvedCountry?.code ?: "GL",
                flagEmoji = resolvedCountry?.flagEmoji ?: "🌐",
                cityName = existingProfile?.city.orEmpty(),
                avatarUrl = existingProfile?.avatarUrl ?: user.photoUrl?.toString(),
                avatarData = existingProfile?.avatarData,
                avatarFrame = existingProfile?.avatarFrame?.takeIf { it.isNotBlank() } ?: "none",
                interests = existingProfile?.interests?.takeIf { it.isNotEmpty() } ?: current.interests,
                languagesSpoken = existingProfile?.languages?.takeIf { it.isNotEmpty() } ?: current.languagesSpoken,
                nativeLanguages = existingProfile?.nativeLanguages.orEmpty(),
                learningLanguages = existingProfile?.learningLanguages.orEmpty(),
                learningLanguageLevels = existingProfile?.learningLanguageLevels.orEmpty(),
                proficiencyLevel = existingProfile?.proficiencyLevel.orEmpty(),
                communicationGoal = existingProfile?.communicationGoal.orEmpty(),
                followersCount = _followerIds.value.size,
                followingCount = _followingIds.value.size,
                roomsHostedCount = 0,
                isDiscoveryVisible = existingProfile?.isDiscoveryVisible ?: true,
                allowDirectMessages = existingProfile?.allowDirectMessages ?: true,
                showOnlineStatus = existingProfile?.showOnlineStatus ?: true,
                lastSeenVisibility = existingProfile?.lastSeenVisibility?.takeIf { it.isNotBlank() } ?: "everyone",
                lastSeenAtMs = existingProfile?.lastSeenAtMs,
                isOnline = existingProfile?.showOnlineStatus ?: true,
                vibeXp = existingProfile?.vibeXp ?: 0,
                vibeLevel = com.example.model.VibeCatalogs.levelFromXp(existingProfile?.vibeXp ?: 0),
                streakDays = existingProfile?.streakDays ?: 0,
                unlockedBadges = existingProfile?.unlockedBadges.orEmpty()
            )
        }
    }

    fun clearSessionData() {
        rawFirestoreConversations = emptyList()
        rawFirestoreUsers = emptyList()
        followerIdsByUser = emptyMap()
        followingIdsByUser = emptyMap()
        syncedCurrentUid = ""
        _currentUser.value = defaultUser
        _users.value = emptyList()
        _conversations.value = emptyList()
        _messages.value = emptyMap()
        _activeRoom.value = null
        _blockedUserIds.value = emptySet()
        _followingIds.value = emptySet()
        _followerIds.value = emptySet()
        _registrationForm.value = RegistrationForm(country = CountryData.defaultCountry)
    }

    fun signOut() {
        clearSessionData()
        _onboardingStep.value = OnboardingStep.WELCOME
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }

    fun setAppLanguage(language: AppLanguage) {
        _appLanguage.value = language
    }

    fun setAppTheme(theme: AppTheme) {
        _appTheme.value = theme
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        _isDarkMode.value = (mode == ThemeMode.DARK)
    }

    fun setAccentColorHex(hex: Long?) {
        _accentColorHex.value = hex
    }

    fun setLocationSharing(enabled: Boolean) {
        _isLocationSharingEnabled.value = enabled
    }

    fun setDiscoveryRadius(radiusKm: Int) {
        _discoveryRadiusKm.value = radiusKm
    }

    fun blockUser(userId: String) {
        _blockedUserIds.update { it + userId }
        _users.update { list -> list.filterNot { it.id == userId } }
        _conversations.update { list -> list.filterNot { it.peerUser.id == userId } }
    }

    fun unblockUser(userId: String) {
        _blockedUserIds.update { it - userId }
        if (syncedCurrentUid.isNotBlank() && rawFirestoreUsers.isNotEmpty()) {
            rebuildUsers(syncedCurrentUid)
        }
        if (syncedCurrentUid.isNotBlank() && rawFirestoreConversations.isNotEmpty()) {
            rebuildConversations(rawFirestoreConversations, syncedCurrentUid)
        }
    }

    fun syncBlockedUserIds(blockedIds: Set<String>) {
        _blockedUserIds.value = blockedIds
        if (syncedCurrentUid.isNotBlank() && rawFirestoreUsers.isNotEmpty()) {
            rebuildUsers(syncedCurrentUid)
        }
        if (syncedCurrentUid.isNotBlank() && rawFirestoreConversations.isNotEmpty()) {
            rebuildConversations(rawFirestoreConversations, syncedCurrentUid)
        }
    }

    fun syncFollowingIds(followingIds: Set<String>) {
        _followingIds.value = followingIds
        _currentUser.update { it.copy(followingCount = followingIds.size) }
        _users.update { list ->
            list.map { user ->
                user.copy(isFollowing = user.id in followingIds)
            }
        }
    }

    fun syncFollowerIds(followerIds: Set<String>) {
        _followerIds.value = followerIds
        _currentUser.update { it.copy(followersCount = followerIds.size) }
    }

    fun getBlockedUserProfiles(): List<UserProfile> {
        val blocked = _blockedUserIds.value
        return rawFirestoreUsers
            .filter { (it.userId.ifBlank { it.id }) in blocked }
            .map { toDomainUser(it, isFollowing = false) }
    }

    fun reportUser(userId: String, reason: String, details: String) {
        // Delegated to FirestoreSocialRepository via VibeRoomViewModel
    }

    fun reportRoom(roomId: String, reason: String) {
        // Delegated to FirestoreSocialRepository via VibeRoomViewModel
    }

    fun syncAllFollowEdges(edges: List<FirestoreFollow>) {
        val followersMap = mutableMapOf<String, MutableSet<String>>()
        val followingMap = mutableMapOf<String, MutableSet<String>>()
        for (edge in edges) {
            val follower = edge.followerId.trim()
            val following = edge.followingId.trim()
            if (follower.isNotEmpty() && following.isNotEmpty() && follower != following) {
                followersMap.getOrPut(following) { linkedSetOf() }.add(follower)
                followingMap.getOrPut(follower) { linkedSetOf() }.add(following)
            }
        }
        followerIdsByUser = followersMap
        followingIdsByUser = followingMap

        val myUid = syncedCurrentUid.ifBlank { _currentUser.value.id }
        if (myUid.isNotBlank() && myUid != "guest_user") {
            val myFollowers = followersMap[myUid].orEmpty()
            val myFollowing = followingMap[myUid].orEmpty()
            _followerIds.value = myFollowers
            _followingIds.value = myFollowing
            _currentUser.update {
                it.copy(
                    followersCount = myFollowers.size,
                    followingCount = myFollowing.size
                )
            }
        }
        if (syncedCurrentUid.isNotBlank() && rawFirestoreUsers.isNotEmpty()) {
            rebuildUsers(syncedCurrentUid)
        }
    }

    fun getUserById(userId: String): UserProfile? {
        if (userId.isBlank()) return null
        if (_currentUser.value.id == userId) return _currentUser.value
        _users.value.firstOrNull { it.id == userId }?.let { return it }
        rawFirestoreUsers.firstOrNull { (it.userId.ifBlank { it.id }) == userId }?.let {
            return toDomainUser(it, isFollowing = userId in _followingIds.value)
        }
        return _conversations.value.firstOrNull { it.peerUser.id == userId }?.peerUser
    }

    fun getFollowerProfiles(userId: String): List<UserProfile> {
        val ids = if (userId == _currentUser.value.id) {
            (_followerIds.value + followerIdsByUser[userId].orEmpty())
        } else {
            followerIdsByUser[userId].orEmpty()
        }
        return ids.mapNotNull { getUserById(it) }.sortedBy { it.name.lowercase() }
    }

    fun getFollowingProfiles(userId: String): List<UserProfile> {
        val ids = if (userId == _currentUser.value.id) {
            (_followingIds.value + followingIdsByUser[userId].orEmpty())
        } else {
            followingIdsByUser[userId].orEmpty()
        }
        return ids.mapNotNull { getUserById(it) }.sortedBy { it.name.lowercase() }
    }

    fun setCurrentUserAvatar(avatarData: String?, clearAvatar: Boolean = false) {
        setCurrentUserAvatarAndFrame(
            avatarData = avatarData,
            clearAvatar = clearAvatar,
            avatarFrame = null
        )
    }

    fun setCurrentUserAvatarAndFrame(
        avatarData: String? = null,
        clearAvatar: Boolean = false,
        avatarFrame: String? = null
    ) {
        _currentUser.update { current ->
            current.copy(
                avatarData = if (clearAvatar) null else (avatarData ?: current.avatarData),
                avatarUrl = if (clearAvatar) null else current.avatarUrl,
                avatarFrame = avatarFrame?.takeIf { it.isNotBlank() } ?: current.avatarFrame
            )
        }
    }

    fun updateCurrentUser(
        name: String,
        bio: String,
        countryName: String,
        flagEmoji: String,
        interests: List<String>,
        languagesSpoken: List<String>,
        countryCode: String? = null,
        cityName: String? = null,
        username: String? = null,
        avatarUrl: String? = null,
        avatarData: String? = null,
        clearAvatar: Boolean = false,
        avatarFrame: String? = null,
        nativeLanguages: List<String>? = null,
        learningLanguages: List<String>? = null,
        learningLanguageLevels: Map<String, Int>? = null,
        proficiencyLevel: String? = null,
        communicationGoal: String? = null,
        isDiscoveryVisible: Boolean? = null,
        allowDirectMessages: Boolean? = null,
        showOnlineStatus: Boolean? = null,
        lastSeenVisibility: String? = null,
        lastSeenAtMs: Long? = null,
        vibeXp: Int? = null,
        vibeLevel: Int? = null,
        streakDays: Int? = null,
        unlockedBadges: List<String>? = null
    ) {
        _currentUser.update { current ->
            val isUnset = current.countryCode == "GL" || current.countryName == "Global" || current.countryName.isBlank()
            val resolvedCountryObj = CountryData.allCountries.find {
                (countryCode != null && it.code.equals(countryCode, ignoreCase = true)) ||
                    it.name.equals(countryName, ignoreCase = true)
            }
            val finalCountryName = if (isUnset && countryName.isNotBlank()) countryName else current.countryName
            val finalCountryCode = if (isUnset) (countryCode ?: resolvedCountryObj?.code ?: current.countryCode) else current.countryCode
            val finalFlag = if (isUnset) (resolvedCountryObj?.flagEmoji ?: flagEmoji) else (CountryData.allCountries.find { it.code.equals(finalCountryCode, ignoreCase = true) }?.flagEmoji ?: current.flagEmoji)
            val resolvedXp = vibeXp ?: current.vibeXp

            current.copy(
                name = name,
                username = username?.takeIf { it.isNotBlank() } ?: current.username,
                bio = bio,
                countryName = finalCountryName,
                countryCode = finalCountryCode,
                flagEmoji = finalFlag,
                cityName = cityName ?: current.cityName,
                avatarUrl = if (clearAvatar) null else (avatarUrl ?: current.avatarUrl),
                avatarData = if (clearAvatar) null else (avatarData ?: current.avatarData),
                avatarFrame = avatarFrame?.takeIf { it.isNotBlank() } ?: current.avatarFrame,
                interests = interests,
                languagesSpoken = languagesSpoken,
                nativeLanguages = nativeLanguages ?: current.nativeLanguages,
                learningLanguages = learningLanguages ?: current.learningLanguages,
                learningLanguageLevels = learningLanguageLevels ?: current.learningLanguageLevels,
                proficiencyLevel = proficiencyLevel ?: current.proficiencyLevel,
                communicationGoal = communicationGoal ?: current.communicationGoal,
                isDiscoveryVisible = isDiscoveryVisible ?: current.isDiscoveryVisible,
                allowDirectMessages = allowDirectMessages ?: current.allowDirectMessages,
                showOnlineStatus = showOnlineStatus ?: current.showOnlineStatus,
                lastSeenVisibility = lastSeenVisibility?.takeIf { it.isNotBlank() } ?: current.lastSeenVisibility,
                lastSeenAtMs = lastSeenAtMs ?: current.lastSeenAtMs,
                vibeXp = resolvedXp,
                vibeLevel = vibeLevel ?: com.example.model.VibeCatalogs.levelFromXp(resolvedXp),
                streakDays = streakDays ?: current.streakDays,
                unlockedBadges = unlockedBadges ?: current.unlockedBadges
            )
        }
    }

    fun awardLocalVibeXp(delta: Int, badge: String? = null): UserProfile {
        var updatedProfile = _currentUser.value
        _currentUser.update { current ->
            val nextXp = (current.vibeXp + delta).coerceAtLeast(0)
            val nextLevel = com.example.model.VibeCatalogs.levelFromXp(nextXp)
            val nextBadges = buildList {
                addAll(current.unlockedBadges)
                if (!badge.isNullOrBlank() && badge !in current.unlockedBadges) add(badge)
                if (nextXp >= 100 && "first_100_xp" !in current.unlockedBadges && badge != "first_100_xp") add("first_100_xp")
            }.distinct().take(20)
            updatedProfile = current.copy(
                vibeXp = nextXp,
                vibeLevel = nextLevel,
                vibesScore = current.vibesScore + delta,
                unlockedBadges = nextBadges
            )
            updatedProfile
        }
        return updatedProfile
    }

    fun updateCurrentUserPrivacy(
        isDiscoveryVisible: Boolean,
        allowDirectMessages: Boolean,
        showOnlineStatus: Boolean = _currentUser.value.showOnlineStatus,
        lastSeenVisibility: String = _currentUser.value.lastSeenVisibility
    ) {
        _currentUser.update {
            it.copy(
                isDiscoveryVisible = isDiscoveryVisible,
                allowDirectMessages = allowDirectMessages,
                showOnlineStatus = showOnlineStatus,
                lastSeenVisibility = lastSeenVisibility,
                isOnline = showOnlineStatus
            )
        }
    }

    fun toggleFollowUser(userId: String) {
        val currentlyFollowing = userId in _followingIds.value
        val nextFollowingIds = if (currentlyFollowing) {
            _followingIds.value - userId
        } else {
            _followingIds.value + userId
        }
        _followingIds.value = nextFollowingIds
        _currentUser.update { it.copy(followingCount = nextFollowingIds.size) }
        _users.update { list ->
            list.map { user ->
                if (user.id == userId) {
                    val newFollowing = !currentlyFollowing
                    user.copy(
                        isFollowing = newFollowing,
                        followersCount = if (newFollowing) user.followersCount + 1 else (user.followersCount - 1).coerceAtLeast(0),
                        connectionsCount = if (newFollowing) user.connectionsCount + 1 else (user.connectionsCount - 1).coerceAtLeast(0)
                    )
                } else user
            }
        }
    }

    private fun toDomainUser(fUser: FirestoreUserProfile, isFollowing: Boolean): UserProfile {
        val uid = fUser.userId.ifBlank { fUser.id }
        val cleanName = fUser.displayName?.ifBlank { null } ?: fUser.username.ifBlank { "Member" }
        val country = CountryData.allCountries.find {
            it.code.equals(fUser.countryCode, ignoreCase = true) || it.name.equals(fUser.country, ignoreCase = true)
        }
        return UserProfile(
            id = uid,
            name = cleanName,
            username = fUser.username.ifBlank { "viber_${uid.take(5)}" },
            bio = fUser.bio ?: "VibeRoom Member",
            countryName = fUser.country?.takeIf { it.isNotBlank() } ?: country?.name ?: "Global",
            countryCode = fUser.countryCode?.takeIf { it.isNotBlank() } ?: country?.code ?: "GL",
            flagEmoji = country?.flagEmoji ?: "🌐",
            cityName = fUser.city.orEmpty(),
            avatarUrl = fUser.avatarUrl,
            avatarData = fUser.avatarData,
            avatarFrame = fUser.avatarFrame?.takeIf { it.isNotBlank() } ?: "none",
            interests = fUser.interests,
            languagesSpoken = fUser.languages,
            nativeLanguages = fUser.nativeLanguages,
            learningLanguages = fUser.learningLanguages,
            learningLanguageLevels = fUser.learningLanguageLevels,
            proficiencyLevel = fUser.proficiencyLevel.orEmpty(),
            communicationGoal = fUser.communicationGoal.orEmpty(),
            followersCount = followerIdsByUser[uid]?.size ?: 0,
            followingCount = followingIdsByUser[uid]?.size ?: 0,
            roomsHostedCount = 0,
            connectionsCount = followerIdsByUser[uid]?.size ?: 0,
            roomsJoinedCount = 0,
            isOnline = fUser.isOnline && fUser.showOnlineStatus,
            showOnlineStatus = fUser.showOnlineStatus,
            lastSeenVisibility = fUser.lastSeenVisibility.ifBlank { "everyone" },
            lastSeenAtMs = fUser.lastSeenAtMs,
            isFollowing = isFollowing,
            isVerified = true,
            isDiscoveryVisible = fUser.isDiscoveryVisible,
            allowDirectMessages = fUser.allowDirectMessages,
            vibeXp = fUser.vibeXp,
            vibeLevel = com.example.model.VibeCatalogs.levelFromXp(fUser.vibeXp).coerceAtLeast(fUser.vibeLevel),
            streakDays = fUser.streakDays,
            unlockedBadges = fUser.unlockedBadges
        )
    }

    fun syncFirestoreUsers(firestoreUsers: List<FirestoreUserProfile>, currentUserId: String) {
        syncedCurrentUid = currentUserId
        rawFirestoreUsers = firestoreUsers
        rebuildUsers(currentUserId)
    }

    private fun rebuildUsers(currentUserId: String) {
        val blocked = _blockedUserIds.value
        val following = _followingIds.value
        val converted = rawFirestoreUsers
            .filterNot {
                val targetUid = it.userId.ifBlank { it.id }
                targetUid == currentUserId || targetUid in blocked || !it.isDiscoveryVisible
            }
            .map { fUser ->
                val targetUid = fUser.userId.ifBlank { fUser.id }
                toDomainUser(fUser, isFollowing = targetUid in following)
            }

        _users.value = converted

        if (rawFirestoreConversations.isNotEmpty()) {
            rebuildConversations(rawFirestoreConversations, currentUserId)
        }
    }

    fun syncFirestoreConversations(firestoreConversations: List<FirestoreConversation>, currentUserId: String) {
        syncedCurrentUid = currentUserId
        rawFirestoreConversations = firestoreConversations
        rebuildConversations(firestoreConversations, currentUserId)
    }

    private fun rebuildConversations(firestoreConversations: List<FirestoreConversation>, currentUserId: String) {
        val blocked = _blockedUserIds.value
        val usersMap = _users.value.associateBy { it.id }
        val rawUsersMap = rawFirestoreUsers.associateBy { it.userId.ifBlank { it.id } }
        val existingConvsMap = _conversations.value.associateBy { it.id }
        val messagesMap = _messages.value
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val updated = firestoreConversations.mapNotNull { fc ->
            val peerId = fc.participantIds.firstOrNull { it != currentUserId } ?: return@mapNotNull null
            if (peerId in blocked) return@mapNotNull null
            val existingPeer = existingConvsMap[fc.id]?.peerUser
            val rawPeer = rawUsersMap[peerId]?.let { toDomainUser(it, isFollowing = peerId in _followingIds.value) }
            val peer = usersMap[peerId] ?: rawPeer ?: existingPeer ?: UserProfile(
                id = peerId,
                name = "Vibe Member",
                username = "viber_${peerId.take(5)}",
                bio = "",
                countryName = "Global",
                countryCode = "GL",
                flagEmoji = "🌐",
                interests = emptyList(),
                languagesSpoken = emptyList(),
                isOnline = false
            )
            val timeStr = fc.updatedAt?.toDate()?.let { timeFormat.format(it) } ?: ""
            val unreadFromMsgs = messagesMap[fc.id]?.count { !it.isMine && !it.isRead } ?: 0
            Conversation(
                id = fc.id,
                peerUser = peer,
                lastMessage = fc.lastMessage.ifBlank { "..." },
                lastTimestamp = timeStr,
                unreadCount = unreadFromMsgs,
                pinnedMessageId = fc.pinnedMessageId,
                pinnedMessageText = fc.pinnedMessageText
            )
        }
        _conversations.value = updated
    }

    fun buildDirectConversationId(uidA: String, uidB: String): String {
        if (uidA.isBlank() || uidA == "guest_user" || uidB.isBlank()) {
            return "conv_${uidB}"
        }
        val sorted = listOf(uidA, uidB).sorted()
        return "dm_${sorted[0]}_${sorted[1]}".take(120)
    }

    fun getConversationIdForUser(userId: String): String {
        val myId = _currentUser.value.id
        val deterministicId = buildDirectConversationId(myId, userId)
        val existing = _conversations.value.firstOrNull { it.peerUser.id == userId || it.id == deterministicId }
        return existing?.id ?: deterministicId
    }

    fun getOrCreateConversationForUser(user: UserProfile): Conversation {
        val deterministicId = buildDirectConversationId(_currentUser.value.id, user.id)
        val existing = _conversations.value.firstOrNull { it.peerUser.id == user.id || it.id == deterministicId }
        if (existing != null) return existing

        val newConv = Conversation(
            id = deterministicId,
            peerUser = user,
            lastMessage = "",
            lastTimestamp = "",
            unreadCount = 0
        )
        _conversations.update { listOf(newConv) + it }
        return newConv
    }

    fun markConversationAsRead(conversationId: String) {
        _messages.update { currentMap ->
            val list = currentMap[conversationId] ?: return@update currentMap
            currentMap + (conversationId to list.map { msg -> if (!msg.isMine) msg.copy(isRead = true) else msg })
        }
        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) conv.copy(unreadCount = 0) else conv
            }
        }
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null,
        isForwarded: Boolean = false,
        expiresAtMs: Long? = null
    ): String? {
        if (text.isBlank()) return null
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        val timestamp = timeFormat.format(Date(nowMs))
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"

        val newMsg = ChatMessage(
            id = msgId,
            conversationId = conversationId,
            senderId = _currentUser.value.id,
            text = text.trim(),
            timestamp = timestamp,
            isMine = true,
            isRead = false,
            mediaType = "text",
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            isForwarded = isForwarded,
            createdAtMs = nowMs,
            expiresAtMs = expiresAtMs
        )

        _messages.update { currentMap ->
            val list = currentMap[conversationId].orEmpty()
            currentMap + (conversationId to (list + newMsg))
        }

        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(lastMessage = text.trim(), lastTimestamp = timestamp)
                } else conv
            }
        }
        return msgId
    }

    fun sendVoiceMessage(
        conversationId: String,
        durationSec: Int,
        mediaData: String? = null,
        isForwarded: Boolean = false
    ): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        val timestamp = timeFormat.format(Date(nowMs))
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val summaryText = "🎙️ Voice message (${durationSec}s)"

        val newMsg = ChatMessage(
            id = msgId,
            conversationId = conversationId,
            senderId = _currentUser.value.id,
            text = summaryText,
            timestamp = timestamp,
            isMine = true,
            isRead = false,
            mediaType = "voice",
            audioDurationSec = durationSec,
            mediaData = mediaData,
            isForwarded = isForwarded,
            createdAtMs = nowMs
        )

        _messages.update { currentMap ->
            val list = currentMap[conversationId].orEmpty()
            currentMap + (conversationId to (list + newMsg))
        }

        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(lastMessage = summaryText, lastTimestamp = timestamp)
                } else conv
            }
        }
        return msgId
    }

    fun sendImageMessage(
        conversationId: String,
        caption: String,
        mediaData: String,
        isForwarded: Boolean = false
    ): String {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val nowMs = System.currentTimeMillis()
        val timestamp = timeFormat.format(Date(nowMs))
        val msgId = "msg_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val summaryText = caption.ifBlank { "📷 Photo" }

        val newMsg = ChatMessage(
            id = msgId,
            conversationId = conversationId,
            senderId = _currentUser.value.id,
            text = summaryText,
            timestamp = timestamp,
            isMine = true,
            isRead = false,
            mediaType = "image",
            mediaData = mediaData,
            isForwarded = isForwarded,
            createdAtMs = nowMs
        )

        _messages.update { currentMap ->
            val list = currentMap[conversationId].orEmpty()
            currentMap + (conversationId to (list + newMsg))
        }

        _conversations.update { list ->
            list.map { conv ->
                if (conv.id == conversationId) {
                    conv.copy(lastMessage = summaryText, lastTimestamp = timestamp)
                } else conv
            }
        }
        return msgId
    }

    fun forwardMessage(targetConversationId: String, original: ChatMessage): String? {
        return when (original.mediaType) {
            "voice" -> sendVoiceMessage(
                conversationId = targetConversationId,
                durationSec = original.audioDurationSec ?: 1,
                mediaData = original.mediaData,
                isForwarded = true
            )
            "image" -> if (!original.mediaData.isNullOrBlank()) {
                sendImageMessage(
                    conversationId = targetConversationId,
                    caption = original.text,
                    mediaData = original.mediaData,
                    isForwarded = true
                )
            } else {
                sendMessage(
                    conversationId = targetConversationId,
                    text = original.text,
                    isForwarded = true
                )
            }
            else -> sendMessage(
                conversationId = targetConversationId,
                text = original.text,
                isForwarded = true
            )
        }
    }

    /**
     * Synchronize real messages received from Firestore into the local reactive state flow.
     */
    fun syncFirestoreMessages(conversationId: String, firestoreMessages: List<FirestoreChatMessage>) {
        val myUid = _currentUser.value.id
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        val nowMs = System.currentTimeMillis()
        val convertedList = firestoreMessages
            .filter { fm -> fm.expiresAtMs == null || fm.expiresAtMs > nowMs }
            .map { fm ->
                val createdMs = fm.createdAt?.toDate()?.time ?: nowMs
                val timestampStr = fm.createdAt?.toDate()?.let { timeFormat.format(it) } ?: "Just now"
                ChatMessage(
                    id = fm.id.ifBlank { UUID.randomUUID().toString() },
                    conversationId = conversationId,
                    senderId = fm.senderId,
                    text = fm.text,
                    timestamp = timestampStr,
                    isMine = (fm.senderId == myUid),
                    isRead = fm.isRead,
                    mediaType = fm.mediaType.ifBlank { "text" },
                    audioDurationSec = fm.audioDurationSec,
                    mediaData = fm.mediaData,
                    mediaUrl = fm.mediaUrl,
                    replyToMessageId = fm.replyToMessageId,
                    replyToText = fm.replyToText,
                    replyToSenderName = fm.replyToSenderName,
                    reactions = fm.reactions,
                    isEdited = fm.isEdited,
                    isDeleted = fm.isDeleted,
                    isForwarded = fm.isForwarded,
                    createdAtMs = createdMs,
                    expiresAtMs = fm.expiresAtMs
                )
            }

        _messages.update { currentMap ->
            val remoteIds = convertedList.map { it.id }.toSet()
            val pendingLocal = currentMap[conversationId].orEmpty().filter { it.id !in remoteIds && it.isMine }
            currentMap + (conversationId to (convertedList + pendingLocal))
        }

        val last = convertedList.lastOrNull()
        val unreadCount = convertedList.count { !it.isMine && !it.isRead }
        if (last != null) {
            _conversations.update { list ->
                list.map { conv ->
                    if (conv.id == conversationId) {
                        conv.copy(
                            lastMessage = last.text,
                            lastTimestamp = last.timestamp,
                            unreadCount = unreadCount
                        )
                    } else conv
                }
            }
        }
    }

    // Community Actions
    fun toggleJoinCommunity(communityId: String) {
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId) {
                    val newJoined = !c.isMember
                    c.copy(
                        isMember = newJoined,
                        memberCount = if (newJoined) c.memberCount + 1 else c.memberCount - 1
                    )
                } else c
            }
        }
    }

    fun toggleLikeCommunityPost(communityId: String, postId: String) {
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId) {
                    c.copy(
                        posts = c.posts.map { p ->
                            if (p.id == postId) {
                                val liked = !p.isLiked
                                p.copy(
                                    isLiked = liked,
                                    likesCount = if (liked) p.likesCount + 1 else p.likesCount - 1
                                )
                            } else p
                        }
                    )
                } else c
            }
        }
    }

    fun addCommunityPost(communityId: String, content: String) {
        if (content.isBlank()) return
        val me = _currentUser.value
        val newPost = CommunityPost(
            id = "p_${UUID.randomUUID()}",
            authorName = me.name,
            authorHandle = "@${me.username}",
            authorFlag = me.flagEmoji,
            authorColorHex = me.avatarColorHex,
            content = content.trim(),
            timestamp = "Just now",
            likesCount = 1,
            commentsCount = 0,
            isLiked = true
        )
        _communities.update { list ->
            list.map { c ->
                if (c.id == communityId) {
                    c.copy(posts = listOf(newPost) + c.posts)
                } else c
            }
        }
    }

    // Voice Room Actions
    fun joinVoiceRoom(room: VoiceRoom) {
        _activeRoom.value = room
        _isMicMuted.value = true
        _isHandRaised.value = false
        _isSpeaker.value = false
        _voiceRooms.update { list ->
            list.map { r ->
                if (r.id == room.id) {
                    r.copy(listenersCount = r.listenersCount + 1)
                } else r
            }
        }
    }

    fun leaveVoiceRoom() {
        val current = _activeRoom.value
        if (current != null) {
            _voiceRooms.update { list ->
                list.map { r ->
                    if (r.id == current.id && r.listenersCount > 0) {
                        r.copy(listenersCount = r.listenersCount - 1)
                    } else r
                }
            }
        }
        _activeRoom.value = null
        _isMicMuted.value = true
        _isHandRaised.value = false
        _isSpeaker.value = false
    }

    fun setMySpeakerState(speaker: Boolean, muted: Boolean, handRaised: Boolean = false) {
        _isSpeaker.value = speaker
        _isMicMuted.value = muted
        _isHandRaised.value = handRaised
    }

    fun syncActiveRoomPeers(
        peers: List<FirestoreRoomPeer>,
        myUid: String,
        connectedWebRtcPeerIds: Set<String> = emptySet()
    ) {
        val current = _activeRoom.value ?: return
        val speakerProfiles = mutableListOf<UserProfile>()
        val listenerProfiles = mutableListOf<UserProfile>()
        val raised = mutableSetOf<String>()
        val muted = mutableSetOf<String>()

        val myPeer = peers.firstOrNull { (it.userId.ifBlank { it.id }) == myUid }
        if (myPeer != null) {
            _isSpeaker.value = myPeer.isSpeaker
            _isMicMuted.value = myPeer.isMuted
            _isHandRaised.value = myPeer.isHandRaised
        }

        peers.forEach { peer ->
            val pid = peer.userId.ifBlank { peer.id }
            if (pid.isBlank()) return@forEach
            val profile = getUserById(pid) ?: UserProfile(
                id = pid,
                name = peer.displayName.ifBlank { "Member" },
                username = peer.displayName.lowercase().filter { it.isLetterOrDigit() }.ifBlank { "viber" },
                bio = "",
                countryName = "Global",
                countryCode = "GL",
                flagEmoji = peer.flagEmoji.ifBlank { "🌐" },
                avatarColorHex = peer.avatarColorHex,
                avatarFrame = peer.avatarFrame.ifBlank { "none" },
                isOnline = true
            )
            if (peer.isHandRaised) raised.add(pid)
            if (peer.isMuted) muted.add(pid)
            if (peer.isSpeaker || pid == current.host.id) {
                speakerProfiles.add(profile)
            } else {
                listenerProfiles.add(profile)
            }
        }

        if (speakerProfiles.none { it.id == current.host.id }) {
            speakerProfiles.add(0, current.host)
        }

        val updatedRoom = current.copy(
            speakers = speakerProfiles.distinctBy { it.id },
            listeners = listenerProfiles.distinctBy { it.id },
            listenersCount = (speakerProfiles.size + listenerProfiles.size).coerceAtLeast(current.listenersCount.coerceAtMost(1)),
            raisedHandIds = raised,
            mutedPeerIds = muted,
            connectedPeerIds = connectedWebRtcPeerIds,
            activeSpeakerId = speakerProfiles.firstOrNull { it.id !in muted }?.id ?: current.activeSpeakerId
        )
        _activeRoom.value = updatedRoom
    }

    fun toggleMic() {
        if (_isSpeaker.value) {
            _isMicMuted.update { !it }
        } else {
            _isSpeaker.value = true
            _isMicMuted.value = false
            _isHandRaised.value = false
            val me = _currentUser.value
            _activeRoom.update { room ->
                if (room != null && room.speakers.none { it.id == me.id }) {
                    room.copy(speakers = room.speakers + me, activeSpeakerId = me.id)
                } else room
            }
        }
    }

    fun toggleRaiseHand() {
        _isHandRaised.update { !it }
    }

    fun sendRoomReaction(emoji: String) {
        val reaction = RoomReaction(
            id = UUID.randomUUID().toString(),
            emoji = emoji,
            senderName = _currentUser.value.name,
            offsetX = ((-60..60).random()).toFloat()
        )
        _activeReactions.update { it + reaction }

        scope.launch {
            delay(2800)
            _activeReactions.update { list -> list.filterNot { it.id == reaction.id } }
        }
    }

    fun createVoiceRoom(
        title: String,
        description: String,
        category: RoomCategory,
        tags: List<String>,
        isPrivate: Boolean = false
    ): VoiceRoom {
        val me = _currentUser.value
        val newRoom = VoiceRoom(
            id = "room_${UUID.randomUUID()}",
            title = title,
            description = description,
            category = category,
            host = me,
            speakers = listOf(me),
            activeSpeakerId = me.id,
            listenersCount = 1,
            isLive = true,
            tags = tags,
            isPrivate = isPrivate
        )
        _voiceRooms.update { listOf(newRoom) + it }
        joinVoiceRoom(newRoom)
        _isSpeaker.value = true
        _isMicMuted.value = false
        return newRoom
    }

    fun updateActiveRoomMetadata(coHostIds: Set<String>? = null, aiSummary: String? = null) {
        _activeRoom.update { room ->
            room?.copy(
                coHostIds = coHostIds ?: room.coHostIds,
                aiSummary = aiSummary ?: room.aiSummary
            )
        }
    }
}
