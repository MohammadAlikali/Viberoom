package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AiLanguageAssistantService
import com.example.data.AuthService
import com.example.data.CountryData
import com.example.data.FirestoreCallRepository
import com.example.data.FirestoreChatRepository
import com.example.data.FirestoreComment
import com.example.data.FirestoreFriendRequest
import com.example.data.FirestoreLearningRepository
import com.example.data.FirestoreNotification
import com.example.data.FirestorePost
import com.example.data.FirestorePostRepository
import com.example.data.FirestoreSocialRepository
import com.example.data.FirestoreStory
import com.example.data.FirestoreUserProfile
import com.example.data.FirestoreUserRepository
import com.example.data.FirestoreVocabItem
import com.example.data.FirestoreVoiceCall
import com.example.data.FirestoreVoiceRoomRepository
import com.example.data.MediaAttachmentHelper
import com.example.data.UserPreferencesStore
import com.example.data.VibeRoomRepository
import com.example.data.VideoAttachmentResult
import com.example.data.VoiceSoundboardEngine
import com.example.data.WebRtcVoiceManager
import com.example.model.ActiveEntranceVisual
import com.example.model.ActiveGiftVisual
import com.example.model.AiCoachTurn
import com.example.model.AiTaskState
import com.example.model.AudioRouteMode
import com.example.model.ChatMessage
import com.example.model.Community
import com.example.model.Conversation
import com.example.model.Country
import com.example.model.EntranceStyle
import com.example.model.LanguageExercise
import com.example.model.OnboardingStep
import com.example.model.RegistrationForm
import com.example.model.RoomBackgroundTheme
import com.example.model.RoomCategory
import com.example.model.RoomLifecycleState
import com.example.model.RoomReaction
import com.example.model.SupporterRankEntry
import com.example.model.TranslationResult
import com.example.model.UserProfile
import com.example.model.VibeCatalogs
import com.example.model.VoiceGift
import com.example.model.VoiceGiftCatalog
import com.example.model.VoiceRoom
import com.example.model.VoiceRoomLiveEvent
import com.example.model.VoiceRoomPoll
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.AppTheme
import com.example.ui.theme.ThemeMode
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import java.util.UUID

enum class NavigationTab(val title: String) {
    VOICE_ROOMS("Voice Rooms"),
    FEED("Feed"),
    CHATS("Messages"),
    DISCOVER("Worldwide"),
    PROFILE("Profile")
}

data class FollowListSheetState(
    val targetUser: UserProfile,
    val isFollowers: Boolean
)

class VibeRoomViewModel(
    val repository: VibeRoomRepository = VibeRoomRepository()
) : ViewModel() {

    private var customAiLanguageService: AiLanguageAssistantService? = null

    internal constructor(
        repository: VibeRoomRepository,
        aiService: AiLanguageAssistantService
    ) : this(repository) {
        customAiLanguageService = aiService
    }

    // Onboarding & Auth
    val onboardingStep: StateFlow<OnboardingStep> = repository.onboardingStep
    val registrationForm: StateFlow<RegistrationForm> = repository.registrationForm

    private val _showCountryPicker = MutableStateFlow(false)
    val showCountryPicker: StateFlow<Boolean> = _showCountryPicker.asStateFlow()

    private val _isSavingProfile = MutableStateFlow(false)
    val isSavingProfile: StateFlow<Boolean> = _isSavingProfile.asStateFlow()

    private val _authFlowMessage = MutableStateFlow<String?>(null)
    val authFlowMessage: StateFlow<String?> = _authFlowMessage.asStateFlow()

    private val _incomingEmailLink = MutableStateFlow<String?>(null)
    val incomingEmailLink: StateFlow<String?> = _incomingEmailLink.asStateFlow()

    private val _isHandlingEmailLink = MutableStateFlow(false)
    val isHandlingEmailLink: StateFlow<Boolean> = _isHandlingEmailLink.asStateFlow()

    private val _emailVerifyReturnStep = MutableStateFlow(OnboardingStep.REGISTER)
    val emailVerifyReturnStep: StateFlow<OnboardingStep> = _emailVerifyReturnStep.asStateFlow()

    private val _isCurrentUserCountryLocked = MutableStateFlow(false)
    val isCurrentUserCountryLocked: StateFlow<Boolean> = _isCurrentUserCountryLocked.asStateFlow()

    // Initial profile setup drafts
    private var draftDisplayName: String = ""
    private var draftUsername: String = ""
    private var draftBio: String = ""
    private var draftInterests: List<String> = listOf("Music", "Technology", "Languages")
    private var draftLanguages: List<String> = listOf("English")
    private var draftLearningLanguageLevels: Map<String, Int> = emptyMap()

    // Tab Navigation
    private val _activeTab = MutableStateFlow(NavigationTab.VOICE_ROOMS)
    val activeTab: StateFlow<NavigationTab> = _activeTab.asStateFlow()

    // Sub-screen Navigation
    private val _activeChatConversationId = MutableStateFlow<String?>(null)
    val activeChatConversationId: StateFlow<String?> = _activeChatConversationId.asStateFlow()

    private val _selectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val selectedUserProfile: StateFlow<UserProfile?> = _selectedUserProfile.asStateFlow()

    private val _followListSheet = MutableStateFlow<FollowListSheetState?>(null)
    val followListSheet: StateFlow<FollowListSheetState?> = _followListSheet.asStateFlow()

    private val _chatTranslateLanguage = MutableStateFlow<String?>(null)
    val chatTranslateLanguage: StateFlow<String?> = _chatTranslateLanguage.asStateFlow()

    private val _chatDisappearingTtlMs = MutableStateFlow(0L)
    val chatDisappearingTtlMs: StateFlow<Long> = _chatDisappearingTtlMs.asStateFlow()

    private val _aiSmartReplies = MutableStateFlow<List<String>>(emptyList())
    val aiSmartReplies: StateFlow<List<String>> = _aiSmartReplies.asStateFlow()

    private val _isLoadingSmartReplies = MutableStateFlow(false)
    val isLoadingSmartReplies: StateFlow<Boolean> = _isLoadingSmartReplies.asStateFlow()

    private val _roomAssistantOutput = MutableStateFlow<String?>(null)
    val roomAssistantOutput: StateFlow<String?> = _roomAssistantOutput.asStateFlow()

    private val _isRoomAssistantLoading = MutableStateFlow(false)
    val isRoomAssistantLoading: StateFlow<Boolean> = _isRoomAssistantLoading.asStateFlow()

    private val _isPolishingPost = MutableStateFlow(false)
    val isPolishingPost: StateFlow<Boolean> = _isPolishingPost.asStateFlow()

    private val _showActiveRoomModal = MutableStateFlow(false)
    val showActiveRoomModal: StateFlow<Boolean> = _showActiveRoomModal.asStateFlow()

    private val _showCreateRoomDialog = MutableStateFlow(false)
    val showCreateRoomDialog: StateFlow<Boolean> = _showCreateRoomDialog.asStateFlow()

    private val _showEditProfileDialog = MutableStateFlow(false)
    val showEditProfileDialog: StateFlow<Boolean> = _showEditProfileDialog.asStateFlow()

    private val _reportingUser = MutableStateFlow<UserProfile?>(null)
    val reportingUser: StateFlow<UserProfile?> = _reportingUser.asStateFlow()

    // Discover Screen Search, Category & Nearby Filter
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _isNearbyMode = MutableStateFlow(false)
    val isNearbyMode: StateFlow<Boolean> = _isNearbyMode.asStateFlow()

    private val _onlyOnlineUsers = MutableStateFlow(false)
    val onlyOnlineUsers: StateFlow<Boolean> = _onlyOnlineUsers.asStateFlow()

    private val _selectedCountryFilter = MutableStateFlow("All")
    val selectedCountryFilter: StateFlow<String> = _selectedCountryFilter.asStateFlow()

    private val _languageMatchOnly = MutableStateFlow(false)
    val languageMatchOnly: StateFlow<Boolean> = _languageMatchOnly.asStateFlow()

    val isLocationSharingEnabled: StateFlow<Boolean> = repository.isLocationSharingEnabled
    val discoveryRadiusKm: StateFlow<Int> = repository.discoveryRadiusKm
    val blockedUserIds: StateFlow<Set<String>> = repository.blockedUserIds
    val followingIds: StateFlow<Set<String>> = repository.followingIds

    private val _incomingFriendRequests = MutableStateFlow<List<FirestoreFriendRequest>>(emptyList())
    val incomingFriendRequests: StateFlow<List<FirestoreFriendRequest>> = _incomingFriendRequests.asStateFlow()

    private val _outgoingFriendRequests = MutableStateFlow<List<FirestoreFriendRequest>>(emptyList())
    val outgoingFriendRequests: StateFlow<List<FirestoreFriendRequest>> = _outgoingFriendRequests.asStateFlow()

    // Modals: Notifications & Language Learning Studio
    private val _showLearningHubModal = MutableStateFlow(false)
    val showLearningHubModal: StateFlow<Boolean> = _showLearningHubModal.asStateFlow()

    private val _showNotificationsDialog = MutableStateFlow(false)
    val showNotificationsDialog: StateFlow<Boolean> = _showNotificationsDialog.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _dailyGoalTarget = MutableStateFlow(5)
    val dailyGoalTarget: StateFlow<Int> = _dailyGoalTarget.asStateFlow()

    private val _dailyExercisesDone = MutableStateFlow(0)
    val dailyExercisesDone: StateFlow<Int> = _dailyExercisesDone.asStateFlow()

    private val _studyStreakDays = MutableStateFlow(0)
    val studyStreakDays: StateFlow<Int> = _studyStreakDays.asStateFlow()

    private data class DiscoverFilterState(
        val query: String,
        val category: String,
        val isNearby: Boolean,
        val onlyOnline: Boolean,
        val countryFilter: String,
        val languageMatchOnly: Boolean
    )

    private val discoverFilterState = combine(
        _searchQuery,
        _selectedCategory,
        _isNearbyMode,
        _onlyOnlineUsers,
        _selectedCountryFilter
    ) { query, category, isNearby, onlyOnline, country ->
        DiscoverFilterState(query, category, isNearby, onlyOnline, country, _languageMatchOnly.value)
    }.combine(_languageMatchOnly) { state, matchOnly ->
        state.copy(languageMatchOnly = matchOnly)
    }

    fun isLanguagePartnerMatch(peer: UserProfile, me: UserProfile = currentUser.value): Boolean {
        val myLearning = me.learningLanguages.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val myNative = me.nativeLanguages.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.toSet()
        val peerAll = (peer.nativeLanguages + peer.languagesSpoken).map { it.trim().lowercase() }.toSet()
        val peerLearning = peer.learningLanguages.map { it.trim().lowercase() }.toSet()
        if (myLearning.isEmpty() && myNative.isEmpty()) {
            return peer.interests.any { it in me.interests } || peer.languagesSpoken.any { it in me.languagesSpoken }
        }
        return myLearning.any { it in peerAll } || myNative.any { it in peerLearning }
    }

    // Filtered Users Flow
    val filteredUsers: StateFlow<List<UserProfile>> = combine(
        repository.users,
        discoverFilterState,
        repository.discoveryRadiusKm,
        repository.currentUser
    ) { users, filter, radius, me ->
        val filtered = users.filter { user ->
            val q = filter.query
            val matchesQuery = q.isBlank() ||
                user.name.contains(q, ignoreCase = true) ||
                user.username.contains(q, ignoreCase = true) ||
                user.countryName.contains(q, ignoreCase = true) ||
                user.cityName.contains(q, ignoreCase = true) ||
                user.bio.contains(q, ignoreCase = true) ||
                user.interests.any { it.contains(q, ignoreCase = true) } ||
                user.languagesSpoken.any { it.contains(q, ignoreCase = true) } ||
                user.nativeLanguages.any { it.contains(q, ignoreCase = true) } ||
                user.learningLanguages.any { it.contains(q, ignoreCase = true) }

            val matchesCategory = filter.category == "All" ||
                user.interests.any { it.equals(filter.category, ignoreCase = true) }

            val matchesNearby = !filter.isNearby || (user.distanceKm != null && user.distanceKm <= radius)
            val matchesOnline = !filter.onlyOnline || user.isOnline
            val matchesCountry = filter.countryFilter == "All" ||
                user.countryName.equals(filter.countryFilter, ignoreCase = true)
            val matchesPartner = !filter.languageMatchOnly || isLanguagePartnerMatch(user, me)

            matchesQuery && matchesCategory && matchesNearby && matchesOnline && matchesCountry && matchesPartner
        }
        if (filter.languageMatchOnly) {
            filtered.sortedByDescending { isLanguagePartnerMatch(it, me) }
        } else {
            filtered
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Voice Rooms Flow (backed by Firestore)
    private val _selectedRoomCategory = MutableStateFlow(RoomCategory.ALL)
    val selectedRoomCategory: StateFlow<RoomCategory> = _selectedRoomCategory.asStateFlow()

    private val _firestoreVoiceRooms = MutableStateFlow<List<VoiceRoom>>(emptyList())
    val firestoreVoiceRooms: StateFlow<List<VoiceRoom>> = _firestoreVoiceRooms.asStateFlow()

    private val _isVoiceRoomsLoading = MutableStateFlow(false)
    val isVoiceRoomsLoading: StateFlow<Boolean> = _isVoiceRoomsLoading.asStateFlow()

    private val _voiceRoomsError = MutableStateFlow<String?>(null)
    val voiceRoomsError: StateFlow<String?> = _voiceRoomsError.asStateFlow()

    private val _isCreatingRoom = MutableStateFlow(false)
    val isCreatingRoom: StateFlow<Boolean> = _isCreatingRoom.asStateFlow()

    private val _roomEndedNoticeTitle = MutableStateFlow<String?>(null)
    val roomEndedNoticeTitle: StateFlow<String?> = _roomEndedNoticeTitle.asStateFlow()

    fun dismissRoomEndedNotice() {
        _roomEndedNoticeTitle.value = null
    }

    val filteredVoiceRooms: StateFlow<List<VoiceRoom>> = combine(
        _firestoreVoiceRooms,
        repository.voiceRooms,
        _selectedRoomCategory,
        repository.currentUser
    ) { firestoreRooms, localRooms, category, me ->
        val now = System.currentTimeMillis()
        val isSignedIn = me.id.isNotBlank() && me.id != "guest_user"
        val sourceRooms = if (isSignedIn) firestoreRooms else localRooms
        val visibleRooms = sourceRooms.filter { room ->
            room.isLive &&
                room.lifecycleState.isDiscoverable() &&
                !room.isUserBanned(me.id, now)
        }
        if (category == RoomCategory.ALL) visibleRooms
        else visibleRooms.filter { it.category == category }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Repository forwards
    val currentUser: StateFlow<UserProfile> = repository.currentUser
    val conversations: StateFlow<List<Conversation>> = repository.conversations
    val messages: StateFlow<Map<String, List<ChatMessage>>> = repository.messages
    val communities: StateFlow<List<Community>> = repository.communities
    val activeRoom: StateFlow<VoiceRoom?> = repository.activeRoom
    val isMicMuted: StateFlow<Boolean> = repository.isMicMuted
    val isHostMuted: StateFlow<Boolean> = repository.isHostMuted
    val isHandRaised: StateFlow<Boolean> = repository.isHandRaised
    val isSpeaker: StateFlow<Boolean> = repository.isSpeaker
    val mySeatIndex: StateFlow<Int> = repository.mySeatIndex
    val activeReactions: StateFlow<List<RoomReaction>> = repository.activeReactions
    val isDarkMode: StateFlow<Boolean> = repository.isDarkMode
    val appTheme: StateFlow<AppTheme> = repository.appTheme
    val themeMode: StateFlow<ThemeMode> = repository.themeMode
    val accentColorHex: StateFlow<Long?> = repository.accentColorHex
    val appLanguage: StateFlow<AppLanguage> = repository.appLanguage

    fun setAppTheme(theme: AppTheme) {
        repository.setAppTheme(theme)
        val currentMode = repository.themeMode.value
        val updatedMode: ThemeMode? = when {
            !theme.defaultIsDark && currentMode == ThemeMode.DARK -> ThemeMode.LIGHT
            theme.defaultIsDark && currentMode == ThemeMode.LIGHT -> ThemeMode.DARK
            else -> null
        }
        if (updatedMode != null) {
            repository.setThemeMode(updatedMode)
        }
        viewModelScope.launch {
            try {
                preferencesStore?.saveAppTheme(theme)
                if (updatedMode != null) {
                    preferencesStore?.saveThemeMode(updatedMode)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed saving theme preference: ${e.message}")
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        repository.setThemeMode(mode)
        viewModelScope.launch {
            try {
                preferencesStore?.saveThemeMode(mode)
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed saving theme mode: ${e.message}")
            }
        }
    }

    fun setAccentColorHex(hex: Long?) {
        repository.setAccentColorHex(hex)
        viewModelScope.launch {
            try {
                preferencesStore?.saveAccentColorHex(hex)
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed saving accent color: ${e.message}")
            }
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        repository.setAppLanguage(language)
        viewModelScope.launch {
            try {
                preferencesStore?.saveAppLanguage(language)
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed saving language preference: ${e.message}")
            }
        }
    }

    private val _inAppNotification = MutableStateFlow<String?>(null)
    val inAppNotification: StateFlow<String?> = _inAppNotification.asStateFlow()

    fun showInAppNotification(message: String) {
        _inAppNotification.value = message
    }

    fun dismissInAppNotification() {
        _inAppNotification.value = null
    }

    fun clearAuthFlowMessage() {
        _authFlowMessage.value = null
    }

    // Onboarding Actions
    fun openCountryPicker() {
        if (!_isCurrentUserCountryLocked.value) {
            _showCountryPicker.value = true
        }
    }

    fun closeCountryPicker() {
        _showCountryPicker.value = false
    }

    fun selectCountry(country: Country) {
        if (_isCurrentUserCountryLocked.value) {
            _showCountryPicker.value = false
            return
        }
        val form = repository.registrationForm.value
        repository.updateRegistrationForm(form.copy(country = country, hasExplicitCountry = true))
        _showCountryPicker.value = false
    }

    fun updateRegistrationForm(form: RegistrationForm) {
        repository.updateRegistrationForm(form)
    }

    fun goToOnboardingStep(step: OnboardingStep) {
        repository.setOnboardingStep(step)
    }

    fun setEmailVerifyReturnStep(step: OnboardingStep) {
        _emailVerifyReturnStep.value = step
    }

    fun savePersonalInfoDraft(displayName: String, username: String, bio: String) {
        draftDisplayName = displayName.trim()
        draftUsername = username.trim().removePrefix("@")
        draftBio = bio.trim()
        val form = repository.registrationForm.value
        val parts = draftDisplayName.split(" ")
        repository.updateRegistrationForm(
            form.copy(
                firstName = parts.firstOrNull().orEmpty(),
                lastName = parts.drop(1).joinToString(" ")
            )
        )
    }

    fun saveInterestsDraft(interests: List<String>) {
        if (interests.isNotEmpty()) {
            draftInterests = interests
        }
    }

    fun saveLanguagesDraft(
        languages: List<String>,
        learningLevels: Map<String, Int> = emptyMap()
    ) {
        if (languages.isNotEmpty()) {
            draftLanguages = languages
        }
        if (learningLevels.isNotEmpty()) {
            draftLearningLanguageLevels = learningLevels
        }
    }

    fun completeInitialProfileSetup(context: Context) {
        attachContext(context)
        val user = Firebase.auth.currentUser
        if (user == null) {
            goToOnboardingStep(OnboardingStep.WELCOME)
            return
        }
        val form = repository.registrationForm.value
        val chosenCountry = form.country ?: CountryData.defaultCountry
        val resolvedName = draftDisplayName.ifBlank {
            "${form.firstName} ${form.lastName}".trim().ifBlank {
                user.displayName?.ifBlank { null }
                    ?: user.email?.substringBefore("@")
                    ?: "Vibe Member"
            }
        }
        val resolvedHandle = draftUsername.ifBlank {
            resolvedName.filter { it.isLetterOrDigit() }.lowercase().take(20)
                .ifBlank { "viber_${user.uid.take(6)}" }
        }
        val resolvedBio = draftBio.ifBlank { repository.currentUser.value.bio }

        viewModelScope.launch {
            _isSavingProfile.value = true
            try {
                val repo = firestoreUserRepo ?: FirestoreUserRepository(context)
                val profile = FirestoreUserProfile(
                    userId = user.uid,
                    username = resolvedHandle,
                    displayName = resolvedName,
                    email = user.email ?: form.email.takeIf { it.isNotBlank() },
                    country = chosenCountry.name,
                    countryCode = chosenCountry.code,
                    bio = resolvedBio,
                    avatarUrl = user.photoUrl?.toString(),
                    interests = draftInterests,
                    languages = draftLanguages,
                    learningLanguages = draftLearningLanguageLevels.keys.toList(),
                    learningLanguageLevels = draftLearningLanguageLevels,
                    proficiencyLevel = VibeCatalogs.summarizeProficiency(draftLearningLanguageLevels, appLanguage.value).ifBlank { null },
                    isOnline = true
                )
                repo.saveUserProfile(profile)
                _isCurrentUserCountryLocked.value = true
                repository.handleGoogleUserSignedIn(user, profile, chosenCountry)
                repository.updateCurrentUser(
                    name = resolvedName,
                    bio = resolvedBio,
                    countryName = chosenCountry.name,
                    flagEmoji = chosenCountry.flagEmoji,
                    interests = draftInterests,
                    languagesSpoken = draftLanguages,
                    countryCode = chosenCountry.code,
                    learningLanguages = draftLearningLanguageLevels.keys.toList(),
                    learningLanguageLevels = draftLearningLanguageLevels,
                    proficiencyLevel = VibeCatalogs.summarizeProficiency(draftLearningLanguageLevels, appLanguage.value)
                )
                AuthService(context).clearPendingRegistration()
                startAuthenticatedListeners(user.uid)
                repository.setOnboardingStep(OnboardingStep.COMPLETED)
                showInAppNotification(AppStrings.t("notif_profile_saved", appLanguage.value))
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed saving initial profile: ${e.message}", e)
                _authFlowMessage.value = e.localizedMessage
            } finally {
                _isSavingProfile.value = false
            }
        }
    }

    private var appContext: Context? = null
    private var preferencesStore: UserPreferencesStore? = null
    private var firestoreUserRepo: FirestoreUserRepository? = null
    private var firestoreChatRepo: FirestoreChatRepository? = null
    private var firestoreVoiceRoomRepo: FirestoreVoiceRoomRepository? = null
    private var firestorePostRepo: FirestorePostRepository? = null
    private var firestoreSocialRepo: FirestoreSocialRepository? = null
    private var firestoreLearningRepo: FirestoreLearningRepository? = null
    private var firestoreCallRepo: FirestoreCallRepository? = null
    private var webRtcVoiceManager: WebRtcVoiceManager? = null
    private val soundboardEngine: VoiceSoundboardEngine by lazy { VoiceSoundboardEngine() }
    private val defaultAiLanguageService: AiLanguageAssistantService by lazy { AiLanguageAssistantService() }
    private val aiLanguageService: AiLanguageAssistantService
        get() = customAiLanguageService ?: defaultAiLanguageService

    private var preferencesJob: Job? = null
    private var userProfileJob: Job? = null
    private var voiceRoomsJob: Job? = null
    private var allUsersJob: Job? = null
    private var conversationsJob: Job? = null
    private var postsJob: Job? = null
    private var activeChatMessagesJob: Job? = null
    private var followingJob: Job? = null
    private var followerJob: Job? = null
    private var allFollowEdgesJob: Job? = null
    private var blockedJob: Job? = null
    private var incomingRequestsJob: Job? = null
    private var outgoingRequestsJob: Job? = null
    private var storiesJob: Job? = null
    private var savedPostsJob: Job? = null
    private var vocabularyJob: Job? = null
    private var notificationsJob: Job? = null
    private var incomingCallsJob: Job? = null
    private var activeCallWatchJob: Job? = null
    private var activeCallCandidatesJob: Job? = null
    private var activeCallTimerJob: Job? = null
    private var roomPeersJob: Job? = null
    private var roomSignalsJob: Job? = null
    private var activeRoomDocJob: Job? = null
    private var roomHeartbeatJob: Job? = null
    private var roomEventsJob: Job? = null
    private var roomPollsJob: Job? = null
    private var giftQueueRunnerJob: Job? = null
    private var entranceQueueRunnerJob: Job? = null
    private var aiCoachJob: Job? = null
    private var manualTranslationJob: Job? = null
    private var exerciseJob: Job? = null
    private var activeCoachRequestSignature: String? = null
    private val autoTranslateSemaphore = Semaphore(2)
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    private var roomJoinedAtMs: Long = 0L
    private var isInitiatedLocalLeave: Boolean = false
    private var lastNotifiedHostMuted: Boolean = false
    private var lastSentMessageSignature: String = ""
    private var lastSentMessageAtMs: Long = 0L
    private val processedRoomEventKeys = mutableSetOf<String>()
    private val pendingGiftQueue = ArrayDeque<ActiveGiftVisual>()
    private val pendingEntranceQueue = ArrayDeque<ActiveEntranceVisual>()

    private val _firestorePosts = MutableStateFlow<List<FirestorePost>>(emptyList())
    val firestorePosts: StateFlow<List<FirestorePost>> = _firestorePosts.asStateFlow()

    private val _stories = MutableStateFlow<List<FirestoreStory>>(emptyList())
    val stories: StateFlow<List<FirestoreStory>> = _stories.asStateFlow()

    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    private val _vocabularyList = MutableStateFlow<List<FirestoreVocabItem>>(emptyList())
    val vocabularyList: StateFlow<List<FirestoreVocabItem>> = _vocabularyList.asStateFlow()

    private val _notifications = MutableStateFlow<List<FirestoreNotification>>(emptyList())
    val notifications: StateFlow<List<FirestoreNotification>> = _notifications.asStateFlow()

    private val _isMediaUploading = MutableStateFlow(false)
    val isMediaUploading: StateFlow<Boolean> = _isMediaUploading.asStateFlow()

    // Live WebRTC 1-to-1 Voice Calls & Voice Room Audio States
    private val _incomingCall = MutableStateFlow<FirestoreVoiceCall?>(null)
    val incomingCall: StateFlow<FirestoreVoiceCall?> = _incomingCall.asStateFlow()

    private val _activeCall = MutableStateFlow<FirestoreVoiceCall?>(null)
    val activeCall: StateFlow<FirestoreVoiceCall?> = _activeCall.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isCallSpeakerOn = MutableStateFlow(false)
    val isCallSpeakerOn: StateFlow<Boolean> = _isCallSpeakerOn.asStateFlow()

    private val _callDurationSec = MutableStateFlow(0)
    val callDurationSec: StateFlow<Int> = _callDurationSec.asStateFlow()

    private val _isCallMediaConnected = MutableStateFlow(false)
    val isCallMediaConnected: StateFlow<Boolean> = _isCallMediaConnected.asStateFlow()

    private val _voiceRoomConnectedPeers = MutableStateFlow(0)
    val voiceRoomConnectedPeers: StateFlow<Int> = _voiceRoomConnectedPeers.asStateFlow()

    private val _connectedRoomPeerIds = MutableStateFlow<Set<String>>(emptySet())
    val connectedRoomPeerIds: StateFlow<Set<String>> = _connectedRoomPeerIds.asStateFlow()

    private val _activeSpeakingPeerIds = MutableStateFlow<Set<String>>(emptySet())
    val activeSpeakingPeerIds: StateFlow<Set<String>> = _activeSpeakingPeerIds.asStateFlow()

    private val _localAudioLevel = MutableStateFlow(0f)
    val localAudioLevel: StateFlow<Float> = _localAudioLevel.asStateFlow()

    private val _roomIceState = MutableStateFlow("idle")
    val roomIceState: StateFlow<String> = _roomIceState.asStateFlow()

    private val _isRoomSpeakerphone = MutableStateFlow(true)
    val isRoomSpeakerphone: StateFlow<Boolean> = _isRoomSpeakerphone.asStateFlow()

    private val _audioRoute = MutableStateFlow(AudioRouteMode.SPEAKER)
    val audioRoute: StateFlow<AudioRouteMode> = _audioRoute.asStateFlow()

    private val _isBluetoothAvailable = MutableStateFlow(false)
    val isBluetoothAvailable: StateFlow<Boolean> = _isBluetoothAvailable.asStateFlow()

    private val _webrtcError = MutableStateFlow<String?>(null)
    val webrtcError: StateFlow<String?> = _webrtcError.asStateFlow()

    private val _isRoomCapacityReached = MutableStateFlow(false)
    val isRoomCapacityReached: StateFlow<Boolean> = _isRoomCapacityReached.asStateFlow()

    private val _hasMicPermission = MutableStateFlow(false)
    val hasMicPermission: StateFlow<Boolean> = _hasMicPermission.asStateFlow()

    private val _isMicCapturing = MutableStateFlow(false)
    val isMicCapturing: StateFlow<Boolean> = _isMicCapturing.asStateFlow()

    private val _micPermissionDeniedInRoom = MutableStateFlow(false)
    val micPermissionDeniedInRoom: StateFlow<Boolean> = _micPermissionDeniedInRoom.asStateFlow()

    private var pendingUnmuteAfterPermission = false

    // Voice Room Interactive Engine States (Events, Chat, Polls, Gifts, Entrances, Soundboard, Preferences)
    private val _roomEvents = MutableStateFlow<List<VoiceRoomLiveEvent>>(emptyList())
    val roomEvents: StateFlow<List<VoiceRoomLiveEvent>> = _roomEvents.asStateFlow()

    private val _roomPolls = MutableStateFlow<List<VoiceRoomPoll>>(emptyList())
    val roomPolls: StateFlow<List<VoiceRoomPoll>> = _roomPolls.asStateFlow()

    private val _supporterLeaderboard = MutableStateFlow<List<SupporterRankEntry>>(emptyList())
    val supporterLeaderboard: StateFlow<List<SupporterRankEntry>> = _supporterLeaderboard.asStateFlow()

    private val _currentGiftVisual = MutableStateFlow<ActiveGiftVisual?>(null)
    val currentGiftVisual: StateFlow<ActiveGiftVisual?> = _currentGiftVisual.asStateFlow()

    private val _currentEntranceVisual = MutableStateFlow<ActiveEntranceVisual?>(null)
    val currentEntranceVisual: StateFlow<ActiveEntranceVisual?> = _currentEntranceVisual.asStateFlow()

    private val _defaultEntranceStyle = MutableStateFlow(EntranceStyle.STANDARD)
    val defaultEntranceStyle: StateFlow<EntranceStyle> = _defaultEntranceStyle.asStateFlow()

    private val _soundEffectsVolume = MutableStateFlow(0.85f)
    val soundEffectsVolume: StateFlow<Float> = _soundEffectsVolume.asStateFlow()

    private val _enableGiftEffects = MutableStateFlow(true)
    val enableGiftEffects: StateFlow<Boolean> = _enableGiftEffects.asStateFlow()

    private val _enableEntranceAnimations = MutableStateFlow(true)
    val enableEntranceAnimations: StateFlow<Boolean> = _enableEntranceAnimations.asStateFlow()

    private val _lowPowerVisualMode = MutableStateFlow(false)
    val lowPowerVisualMode: StateFlow<Boolean> = _lowPowerVisualMode.asStateFlow()

    private val _favoriteSoundIds = MutableStateFlow<Set<String>>(setOf("welcome", "applause", "celebration"))
    val favoriteSoundIds: StateFlow<Set<String>> = _favoriteSoundIds.asStateFlow()

    val playingSoundId: StateFlow<String?> = soundboardEngine.playingSoundId

    // Translation Preferences (persisted via DataStore)
    private val _defaultTranslateLanguage = MutableStateFlow("")
    val defaultTranslateLanguage: StateFlow<String> = _defaultTranslateLanguage.asStateFlow()

    private val _autoTranslateChat = MutableStateFlow(false)
    val autoTranslateChat: StateFlow<Boolean> = _autoTranslateChat.asStateFlow()

    private val _showTranslationNotes = MutableStateFlow(true)
    val showTranslationNotes: StateFlow<Boolean> = _showTranslationNotes.asStateFlow()

    // AI Language Assistant & Translation States
    private val _aiCoachTurns = MutableStateFlow<List<AiCoachTurn>>(emptyList())
    val aiCoachTurns: StateFlow<List<AiCoachTurn>> = _aiCoachTurns.asStateFlow()

    private val _aiCoachState = MutableStateFlow<AiTaskState<Unit>>(AiTaskState.Idle)
    val aiCoachState: StateFlow<AiTaskState<Unit>> = _aiCoachState.asStateFlow()

    private val _translationState = MutableStateFlow<AiTaskState<TranslationResult>>(AiTaskState.Idle)
    val translationState: StateFlow<AiTaskState<TranslationResult>> = _translationState.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<TranslationResult>>(emptyList())
    val translationHistory: StateFlow<List<TranslationResult>> = _translationHistory.asStateFlow()

    private val _inlineTranslations = MutableStateFlow<Map<String, TranslationResult>>(emptyMap())
    val inlineTranslations: StateFlow<Map<String, TranslationResult>> = _inlineTranslations.asStateFlow()

    private val _inlineTranslatingKeys = MutableStateFlow<Set<String>>(emptySet())
    val inlineTranslatingKeys: StateFlow<Set<String>> = _inlineTranslatingKeys.asStateFlow()

    private val _inlineTranslationErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val inlineTranslationErrors: StateFlow<Map<String, String>> = _inlineTranslationErrors.asStateFlow()

    private val _exerciseState = MutableStateFlow<AiTaskState<LanguageExercise>>(AiTaskState.Idle)
    val exerciseState: StateFlow<AiTaskState<LanguageExercise>> = _exerciseState.asStateFlow()

    private fun awardVibeXp(delta: Int, badge: String? = null) {
        val updated = repository.awardLocalVibeXp(delta, badge)
        val userRepo = firestoreUserRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            try {
                userRepo.updateGamificationStats(
                    vibeXp = updated.vibeXp,
                    vibeLevel = updated.vibeLevel,
                    streakDays = updated.streakDays,
                    unlockedBadges = updated.unlockedBadges
                )
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed syncing gamification stats: ${e.message}")
            }
        }
    }

    fun setChatDisappearingTtl(ttlMs: Long) {
        _chatDisappearingTtlMs.value = ttlMs.coerceAtLeast(0L)
    }

    fun clearSmartReplies() {
        _aiSmartReplies.value = emptyList()
    }

    fun fetchSmartReplies(conversationId: String) {
        val recent = messages.value[conversationId].orEmpty()
            .filter { !it.isDeleted && it.text.isNotBlank() }
            .takeLast(6)
            .map { "${if (it.isMine) "Me" else "Partner"}: ${it.text}" }
        val targetLang = currentUser.value.learningLanguages.firstOrNull()
            ?: effectiveDefaultTranslateLanguage()
        viewModelScope.launch {
            _isLoadingSmartReplies.value = true
            try {
                val res = aiLanguageService.suggestConversationReplies(
                    recentMessages = recent,
                    targetLanguage = targetLang,
                    learnerUiLanguage = appLanguage.value.nativeName
                )
                res.onSuccess { list ->
                    _aiSmartReplies.value = list
                }.onFailure { err ->
                    showInAppNotification(err.localizedMessage ?: "AI reply suggestions unavailable.")
                }
            } finally {
                _isLoadingSmartReplies.value = false
            }
        }
    }

    suspend fun polishPostDraft(draft: String, category: String): Result<String> {
        _isPolishingPost.value = true
        return try {
            aiLanguageService.polishPostDraft(
                draft = draft,
                category = category,
                uiLanguage = appLanguage.value.nativeName
            )
        } finally {
            _isPolishingPost.value = false
        }
    }

    fun clearRoomAssistantOutput() {
        _roomAssistantOutput.value = null
    }

    fun generateRoomAssistant(mode: String) {
        val room = activeRoom.value ?: return
        val speakers = room.speakers.map { it.name }.ifEmpty { listOf(room.host.name) }
        viewModelScope.launch {
            _isRoomAssistantLoading.value = true
            try {
                val res = aiLanguageService.generateRoomAssistantOutput(
                    roomTitle = room.title,
                    topic = room.description,
                    speakerNames = speakers,
                    mode = mode,
                    uiLanguage = appLanguage.value.nativeName
                )
                res.onSuccess { text ->
                    _roomAssistantOutput.value = text
                    awardVibeXp(5)
                }.onFailure { err ->
                    showInAppNotification(err.localizedMessage ?: "AI Stage assistant failed.")
                }
            } finally {
                _isRoomAssistantLoading.value = false
            }
        }
    }

    fun generateAndSaveRoomSummary() {
        val room = activeRoom.value ?: return
        val speakers = room.speakers.map { it.name }.ifEmpty { listOf(room.host.name) }
        viewModelScope.launch {
            _isRoomAssistantLoading.value = true
            try {
                val res = aiLanguageService.generateRoomAssistantOutput(
                    roomTitle = room.title,
                    topic = room.description,
                    speakerNames = speakers,
                    mode = "summary",
                    uiLanguage = appLanguage.value.nativeName
                )
                res.onSuccess { summary ->
                    _roomAssistantOutput.value = summary
                    repository.updateActiveRoomMetadata(aiSummary = summary)
                    firestoreVoiceRoomRepo?.saveRoomSummary(room.id, summary)
                    awardVibeXp(10)
                    showInAppNotification(AppStrings.t("done", appLanguage.value))
                }.onFailure { err ->
                    showInAppNotification(err.localizedMessage ?: "Failed to generate room summary.")
                }
            } finally {
                _isRoomAssistantLoading.value = false
            }
        }
    }

    fun effectiveDefaultTranslateLanguage(): String =
        _defaultTranslateLanguage.value.ifBlank {
            VibeCatalogs.defaultTranslateLanguageFor(appLanguage.value)
        }

    fun setDefaultTranslateLanguage(language: String) {
        val clean = language.trim()
        _defaultTranslateLanguage.value = clean
        if (_chatTranslateLanguage.value == null && clean.isNotBlank()) {
            _chatTranslateLanguage.value = clean
        }
        viewModelScope.launch {
            try {
                preferencesStore?.saveTranslationSettings(defaultTranslateLanguage = clean)
            } catch (_: Exception) {
            }
        }
    }

    fun setAutoTranslateChat(enabled: Boolean) {
        _autoTranslateChat.value = enabled
        viewModelScope.launch {
            try {
                preferencesStore?.saveTranslationSettings(autoTranslateChat = enabled)
            } catch (_: Exception) {
            }
        }
        if (enabled) {
            val activeConvId = _activeChatConversationId.value
            if (!activeConvId.isNullOrBlank()) {
                val msgs = messages.value[activeConvId].orEmpty()
                autoTranslateIncomingMessages(msgs)
            }
        }
    }

    fun setShowTranslationNotes(enabled: Boolean) {
        _showTranslationNotes.value = enabled
        viewModelScope.launch {
            try {
                preferencesStore?.saveTranslationSettings(showTranslationNotes = enabled)
            } catch (_: Exception) {
            }
        }
    }

    fun attachContext(context: Context) {
        val app = context.applicationContext
        if (this.appContext != null) return
        this.appContext = app
        val store = UserPreferencesStore(app)
        this.preferencesStore = store
        this.firestoreUserRepo = FirestoreUserRepository(app)
        this.firestoreChatRepo = FirestoreChatRepository(app)
        this.firestoreVoiceRoomRepo = FirestoreVoiceRoomRepository(app)
        this.firestorePostRepo = FirestorePostRepository(app)
        this.firestoreSocialRepo = FirestoreSocialRepository(app)
        this.firestoreLearningRepo = FirestoreLearningRepository(app)
        this.firestoreCallRepo = FirestoreCallRepository(app)
        val rtc = WebRtcVoiceManager(app)
        this.webRtcVoiceManager = rtc
        viewModelScope.launch {
            rtc.isCallMediaConnected.collect { _isCallMediaConnected.value = it }
        }
        viewModelScope.launch {
            rtc.connectedPeersCount.collect { _voiceRoomConnectedPeers.value = it }
        }
        viewModelScope.launch {
            rtc.connectedPeerIds.collect { _connectedRoomPeerIds.value = it }
        }
        viewModelScope.launch {
            rtc.roomIceState.collect { _roomIceState.value = it }
        }
        viewModelScope.launch {
            rtc.activeSpeakingPeerIds.collect { _activeSpeakingPeerIds.value = it }
        }
        viewModelScope.launch {
            rtc.localAudioLevel.collect { _localAudioLevel.value = it }
        }
        viewModelScope.launch {
            rtc.audioRoute.collect { route ->
                _audioRoute.value = route
                _isRoomSpeakerphone.value = (route == AudioRouteMode.SPEAKER)
            }
        }
        viewModelScope.launch {
            rtc.isBluetoothAvailable.collect { _isBluetoothAvailable.value = it }
        }
        viewModelScope.launch {
            rtc.lastError.collect { _webrtcError.value = it }
        }
        viewModelScope.launch {
            rtc.isRoomCapacityReached.collect { _isRoomCapacityReached.value = it }
        }
        viewModelScope.launch {
            rtc.hasMicPermission.collect { _hasMicPermission.value = it }
        }
        viewModelScope.launch {
            rtc.isMicCapturing.collect { _isMicCapturing.value = it }
        }
        refreshMicPermissionStatus()

        preferencesJob?.cancel()
        preferencesJob = viewModelScope.launch {
            store.preferencesFlow.collect { prefs ->
                repository.setAppTheme(prefs.appTheme)
                repository.setThemeMode(prefs.themeMode)
                repository.setAccentColorHex(prefs.accentColorHex)
                repository.setAppLanguage(prefs.appLanguage)
                _notificationsEnabled.value = prefs.notificationsEnabled
                _dailyGoalTarget.value = prefs.dailyGoalTarget
                _dailyExercisesDone.value = prefs.dailyExercisesDone
                _studyStreakDays.value = prefs.studyStreakDays
                _defaultTranslateLanguage.value = prefs.defaultTranslateLanguage
                _autoTranslateChat.value = prefs.autoTranslateChat
                _showTranslationNotes.value = prefs.showTranslationNotes
                _defaultEntranceStyle.value = EntranceStyle.fromId(prefs.defaultEntranceStyle)
                _soundEffectsVolume.value = prefs.soundEffectsVolume
                _enableGiftEffects.value = prefs.enableGiftEffects
                _enableEntranceAnimations.value = prefs.enableEntranceAnimations
                _lowPowerVisualMode.value = prefs.lowPowerVisualMode
                _favoriteSoundIds.value = prefs.favoriteSoundIds
                if (_chatTranslateLanguage.value == null && prefs.defaultTranslateLanguage.isNotBlank()) {
                    _chatTranslateLanguage.value = prefs.defaultTranslateLanguage
                }
            }
        }

        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val signedInUser = firebaseAuth.currentUser
            if (signedInUser != null) {
                startAuthenticatedListeners(signedInUser.uid)
            } else {
                stopFirestoreListeners()
            }
        }
        authStateListener = listener
        Firebase.auth.addAuthStateListener(listener)

        val currentUserAuth = Firebase.auth.currentUser
        if (currentUserAuth != null) {
            startAuthenticatedListeners(currentUserAuth.uid)
        }
    }

    private fun startAuthenticatedListeners(uid: String) {
        listenToUserProfile(uid)
        viewModelScope.launch {
            try {
                firestoreUserRepo?.setOnlinePresence(true, currentUser.value.showOnlineStatus)
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed to set online presence: ${e.message}")
            }
            try {
                firestoreVoiceRoomRepo?.expireStaleHostRooms(uid, expireAllLive = false)
            } catch (_: Exception) {
            }
        }
        listenToVoiceRooms()
        listenToAllUsers()
        listenToConversations()
        listenToPosts()
        listenToSocialState()
        listenToLearningAndStories()
        listenToIncomingCalls(uid)
    }

    private fun listenToIncomingCalls(uid: String) {
        val repo = firestoreCallRepo ?: return
        incomingCallsJob?.cancel()
        incomingCallsJob = viewModelScope.launch {
            repo.observeIncomingRingingCalls(uid).collect { ringingList ->
                if (_activeCall.value == null) {
                    _incomingCall.value = ringingList.firstOrNull { it.callerId != uid }
                }
            }
        }
    }

    private fun listenToSocialState() {
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return

        followingJob?.cancel()
        followingJob = viewModelScope.launch {
            try {
                social.observeFollowingIds().collect { ids ->
                    repository.syncFollowingIds(ids)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing followingIds: ${e.message}")
            }
        }

        followerJob?.cancel()
        followerJob = viewModelScope.launch {
            try {
                social.observeFollowerIds().collect { ids ->
                    repository.syncFollowerIds(ids)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing followerIds: ${e.message}")
            }
        }

        allFollowEdgesJob?.cancel()
        allFollowEdgesJob = viewModelScope.launch {
            try {
                social.observeAllFollowEdges().collect { edges ->
                    repository.syncAllFollowEdges(edges)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing all follow edges: ${e.message}")
            }
        }

        blockedJob?.cancel()
        blockedJob = viewModelScope.launch {
            try {
                social.observeBlockedUserIds().collect { ids ->
                    repository.syncBlockedUserIds(ids)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing blockedUserIds: ${e.message}")
            }
        }

        incomingRequestsJob?.cancel()
        incomingRequestsJob = viewModelScope.launch {
            try {
                social.observeIncomingFriendRequests().collect { list ->
                    _incomingFriendRequests.value = list
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing incoming requests: ${e.message}")
            }
        }

        outgoingRequestsJob?.cancel()
        outgoingRequestsJob = viewModelScope.launch {
            try {
                social.observeOutgoingFriendRequests().collect { list ->
                    _outgoingFriendRequests.value = list
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing outgoing requests: ${e.message}")
            }
        }
    }

    private fun listenToLearningAndStories() {
        val learning = firestoreLearningRepo ?: return
        val postRepo = firestorePostRepo
        if (Firebase.auth.currentUser == null) return

        storiesJob?.cancel()
        storiesJob = viewModelScope.launch {
            try {
                learning.observeActiveStories().collect { list ->
                    _stories.value = list
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing stories: ${e.message}")
            }
        }

        savedPostsJob?.cancel()
        savedPostsJob = viewModelScope.launch {
            try {
                postRepo?.observeSavedPostIds()?.collect { ids ->
                    _savedPostIds.value = ids
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing saved posts: ${e.message}")
            }
        }

        vocabularyJob?.cancel()
        vocabularyJob = viewModelScope.launch {
            try {
                learning.observeVocabulary().collect { list ->
                    _vocabularyList.value = list
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing vocabulary: ${e.message}")
            }
        }

        notificationsJob?.cancel()
        notificationsJob = viewModelScope.launch {
            try {
                learning.observeNotifications().collect { list ->
                    _notifications.value = list
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing notifications: ${e.message}")
            }
        }
    }

    private fun stopFirestoreListeners() {
        userProfileJob?.cancel()
        voiceRoomsJob?.cancel()
        allUsersJob?.cancel()
        conversationsJob?.cancel()
        postsJob?.cancel()
        activeChatMessagesJob?.cancel()
        followingJob?.cancel()
        followerJob?.cancel()
        allFollowEdgesJob?.cancel()
        blockedJob?.cancel()
        incomingRequestsJob?.cancel()
        outgoingRequestsJob?.cancel()
        storiesJob?.cancel()
        savedPostsJob?.cancel()
        vocabularyJob?.cancel()
        notificationsJob?.cancel()
        incomingCallsJob?.cancel()
        activeCallWatchJob?.cancel()
        activeCallCandidatesJob?.cancel()
        activeCallTimerJob?.cancel()
        roomPeersJob?.cancel()
        roomSignalsJob?.cancel()
        activeRoomDocJob?.cancel()
        roomHeartbeatJob?.cancel()
        roomEventsJob?.cancel()
        roomPollsJob?.cancel()
        giftQueueRunnerJob?.cancel()
        entranceQueueRunnerJob?.cancel()
        aiCoachJob?.cancel()
        manualTranslationJob?.cancel()
        exerciseJob?.cancel()
        activeCoachRequestSignature = null
        webRtcVoiceManager?.endCall()
        webRtcVoiceManager?.leaveRoomAudio()
        soundboardEngine.stopAll()
        _isCreatingRoom.value = false
        isInitiatedLocalLeave = false
        lastNotifiedHostMuted = false
        _isVoiceRoomsLoading.value = false
        _voiceRoomsError.value = null
        _firestoreVoiceRooms.value = emptyList()
        _roomEvents.value = emptyList()
        _roomPolls.value = emptyList()
        _supporterLeaderboard.value = emptyList()
        _currentGiftVisual.value = null
        _currentEntranceVisual.value = null
        _firestorePosts.value = emptyList()
        _incomingFriendRequests.value = emptyList()
        _outgoingFriendRequests.value = emptyList()
        _stories.value = emptyList()
        _savedPostIds.value = emptySet()
        _vocabularyList.value = emptyList()
        _notifications.value = emptyList()
        _incomingCall.value = null
        _activeCall.value = null
    }

    override fun onCleared() {
        super.onCleared()
        preferencesJob?.cancel()
        authStateListener?.let { Firebase.auth.removeAuthStateListener(it) }
        authStateListener = null
        stopFirestoreListeners()
        webRtcVoiceManager?.dispose()
        soundboardEngine.release()
    }

    fun restoreExistingSession(user: FirebaseUser, context: Context) {
        handleAuthenticatedUser(user, context, silentRestore = true)
    }

    fun onGoogleSignInSuccess(user: FirebaseUser, context: Context) {
        handleAuthenticatedUser(user, context, silentRestore = false)
    }

    fun handleIncomingEmailLink(link: String, context: Context) {
        attachContext(context)
        val authService = AuthService(context)
        if (!authService.isSignInWithEmailLink(link)) return

        val pendingEmail = authService.getPendingEmail()
            ?: repository.registrationForm.value.email.takeIf { it.isNotBlank() }

        if (pendingEmail.isNullOrBlank()) {
            _incomingEmailLink.value = link
            goToOnboardingStep(OnboardingStep.OTP_VERIFY)
            return
        }

        _isHandlingEmailLink.value = true
        authService.completeSignInWithEmailLink(
            email = pendingEmail,
            emailLink = link,
            scope = viewModelScope,
            onSuccess = { user ->
                _isHandlingEmailLink.value = false
                _incomingEmailLink.value = null
                handleAuthenticatedUser(user, context, silentRestore = false)
            },
            onError = { err ->
                _isHandlingEmailLink.value = false
                _authFlowMessage.value = err
                goToOnboardingStep(OnboardingStep.OTP_VERIFY)
            }
        )
    }

    fun handleAuthenticatedUser(
        user: FirebaseUser,
        context: Context,
        silentRestore: Boolean = false
    ) {
        attachContext(context)
        viewModelScope.launch {
            val firestoreRepo = firestoreUserRepo ?: FirestoreUserRepository(context)
            val authService = AuthService(context)
            val fetchedProfile = firestoreRepo.getUserProfile(user.uid).getOrNull()

            if (fetchedProfile != null && !fetchedProfile.country.isNullOrBlank()) {
                // Existing account with locked country -> restore full profile & chat history!
                _isCurrentUserCountryLocked.value = true
                val resolvedCountry = CountryData.allCountries.find {
                    it.code.equals(fetchedProfile.countryCode, ignoreCase = true) ||
                        it.name.equals(fetchedProfile.country, ignoreCase = true)
                }
                if (resolvedCountry != null) {
                    repository.updateRegistrationForm(
                        repository.registrationForm.value.copy(
                            country = resolvedCountry,
                            hasExplicitCountry = true
                        )
                    )
                }
                repository.handleGoogleUserSignedIn(user, fetchedProfile, resolvedCountry)
                firestoreRepo.setOnlinePresence(true, fetchedProfile.showOnlineStatus)
                authService.clearPendingRegistration()
                startAuthenticatedListeners(user.uid)
                repository.setOnboardingStep(OnboardingStep.COMPLETED)
                if (!silentRestore) {
                    showInAppNotification(AppStrings.t("notif_account_restored", appLanguage.value))
                }
            } else {
                // New user (or country not locked yet): merge any pending registration info
                _isCurrentUserCountryLocked.value = false
                val form = repository.registrationForm.value
                val savedCountryCode = authService.getPendingCountryCode()
                val savedCountryName = authService.getPendingCountryName()
                val pendingCountry = when {
                    form.hasExplicitCountry && form.country != null -> form.country
                    !savedCountryCode.isNullOrBlank() || !savedCountryName.isNullOrBlank() ->
                        CountryData.allCountries.find {
                            (!savedCountryCode.isNullOrBlank() && it.code.equals(savedCountryCode, ignoreCase = true)) ||
                                (!savedCountryName.isNullOrBlank() && it.name.equals(savedCountryName, ignoreCase = true))
                        }
                    else -> null
                }

                val pendingFullName = authService.getPendingFullName().ifBlank {
                    "${form.firstName} ${form.lastName}".trim()
                }
                val resolvedName = pendingFullName.ifBlank {
                    fetchedProfile?.displayName?.takeIf { it.isNotBlank() }
                        ?: user.displayName?.ifBlank { null }
                        ?: user.email?.substringBefore("@")
                        ?: ""
                }
                val resolvedHandle = fetchedProfile?.username?.takeIf { it.isNotBlank() }
                    ?: (resolvedName.filter { it.isLetterOrDigit() }.lowercase().take(20)
                        .ifBlank { "viber_${user.uid.take(6)}" })

                draftDisplayName = resolvedName
                draftUsername = resolvedHandle
                draftBio = fetchedProfile?.bio.orEmpty()

                val nameParts = resolvedName.split(" ")
                repository.updateRegistrationForm(
                    form.copy(
                        firstName = nameParts.firstOrNull().orEmpty(),
                        lastName = nameParts.drop(1).joinToString(" "),
                        email = user.email ?: form.email,
                        country = pendingCountry ?: form.country ?: CountryData.defaultCountry,
                        hasExplicitCountry = (pendingCountry != null || form.hasExplicitCountry)
                    )
                )

                // Save baseline auth record without locking country until user completes PersonalInfoScreen
                val partialProfile = FirestoreUserProfile(
                    userId = user.uid,
                    username = resolvedHandle,
                    displayName = resolvedName.ifBlank { null },
                    email = user.email,
                    avatarUrl = user.photoUrl?.toString(),
                    isOnline = true
                )
                firestoreRepo.saveUserProfile(partialProfile)
                repository.handleGoogleUserSignedIn(user, fetchedProfile, pendingCountry)
                startAuthenticatedListeners(user.uid)
                repository.setOnboardingStep(OnboardingStep.PERSONAL_INFO)
            }
        }
    }

    fun signOut(context: Context) {
        attachContext(context)
        viewModelScope.launch {
            try {
                firestoreUserRepo?.setOnlinePresence(false)
            } catch (_: Exception) {
            }
            AuthService(context).signOut(viewModelScope) {
                stopFirestoreListeners()
                _isCurrentUserCountryLocked.value = false
                _activeChatConversationId.value = null
                _selectedUserProfile.value = null
                _showActiveRoomModal.value = false
                repository.signOut()
            }
        }
    }

    fun listenToPosts() {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        postsJob?.cancel()
        postsJob = viewModelScope.launch {
            try {
                repo.observePosts().collect { posts ->
                    _firestorePosts.value = posts
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error fetching posts: ${e.message}")
            }
        }
    }

    fun observeCommentsForPost(postId: String): Flow<List<FirestoreComment>> {
        return firestorePostRepo?.observeComments(postId) ?: emptyFlow()
    }

    fun createPost(
        content: String,
        category: String = "General",
        postType: String = "standard",
        pollOptions: List<String> = emptyList()
    ) {
        if (Firebase.auth.currentUser == null) {
            showInAppNotification(AppStrings.t("sign_in", appLanguage.value))
            return
        }
        val repo = firestorePostRepo ?: return
        val user = currentUser.value
        viewModelScope.launch {
            try {
                val result = repo.createPost(
                    content = content,
                    authorName = user.name,
                    authorCountry = user.countryName,
                    authorFlag = user.flagEmoji,
                    category = category,
                    postType = postType,
                    pollOptions = pollOptions
                )
                if (result.isSuccess) {
                    awardVibeXp(15, "first_post")
                    showInAppNotification(AppStrings.t("notif_post_published", appLanguage.value))
                } else {
                    showInAppNotification("Unable to publish post.")
                }
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to create post: ${e.message}")
            }
        }
    }

    fun createMediaPost(
        context: Context,
        content: String,
        category: String = "General",
        imageUri: Uri? = null,
        videoUri: Uri? = null,
        externalVideoUrl: String? = null,
        postType: String = "standard",
        pollOptions: List<String> = emptyList(),
        onComplete: (errorMsg: String?) -> Unit = {}
    ) {
        attachContext(context)
        if (Firebase.auth.currentUser == null) {
            showInAppNotification(AppStrings.t("sign_in", appLanguage.value))
            onComplete("Sign in required")
            return
        }
        val repo = firestorePostRepo ?: return
        val user = currentUser.value
        val finalCaption = content.trim().ifBlank {
            when {
                imageUri != null -> "📷"
                videoUri != null || !externalVideoUrl.isNullOrBlank() -> "🎬"
                else -> ""
            }
        }
        if (finalCaption.isBlank()) {
            onComplete("Post content is empty.")
            return
        }

        viewModelScope.launch {
            _isMediaUploading.value = true
            try {
                var mediaType = "text"
                var mediaData: String? = null
                var mediaUrl: String? = null

                if (imageUri != null) {
                    val encoded = MediaAttachmentHelper.encodeImageDataUri(context, imageUri)
                    if (encoded == null) {
                        onComplete("Unable to compress selected photo.")
                        return@launch
                    }
                    mediaType = "image"
                    mediaData = encoded
                } else if (videoUri != null) {
                    when (val vidResult = MediaAttachmentHelper.prepareVideoAttachment(context, videoUri)) {
                        is VideoAttachmentResult.Ready -> {
                            mediaType = "video"
                            mediaData = vidResult.mediaData
                            mediaUrl = vidResult.mediaUrl
                        }
                        is VideoAttachmentResult.Failed -> {
                            onComplete(vidResult.message)
                            return@launch
                        }
                    }
                } else if (!externalVideoUrl.isNullOrBlank()) {
                    mediaType = "video"
                    mediaUrl = externalVideoUrl.trim()
                }

                val result = repo.createPost(
                    content = finalCaption,
                    authorName = user.name,
                    authorCountry = user.countryName,
                    authorFlag = user.flagEmoji,
                    category = category,
                    mediaType = mediaType,
                    mediaData = mediaData,
                    mediaUrl = mediaUrl,
                    postType = postType,
                    pollOptions = pollOptions
                )
                if (result.isSuccess) {
                    awardVibeXp(15, "first_post")
                    showInAppNotification(AppStrings.t("notif_post_published", appLanguage.value))
                    onComplete(null)
                } else {
                    onComplete("Unable to publish post.")
                }
            } catch (e: Exception) {
                onComplete(e.localizedMessage ?: "Failed to publish media post.")
            } finally {
                _isMediaUploading.value = false
            }
        }
    }

    fun voteOnPoll(post: FirestorePost, optionIndex: Int) {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        viewModelScope.launch {
            val res = repo.voteOnPoll(post.id, optionIndex, post.pollVotes)
            if (res.isSuccess) {
                awardVibeXp(5)
            }
        }
    }

    fun toggleLikePost(post: FirestorePost) {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        val currentlyLiked = post.likedUserIds.contains(uid)
        viewModelScope.launch {
            val res = repo.toggleLike(post.id, post.likesCount, currentlyLiked)
            if (res.isSuccess && !currentlyLiked && post.authorId != uid) {
                firestoreLearningRepo?.createNotificationForUser(
                    recipientId = post.authorId,
                    senderName = currentUser.value.name,
                    type = "post_like",
                    title = "${currentUser.value.name} liked your moment",
                    body = post.content.take(120),
                    targetId = post.id
                )
            }
        }
    }

    fun toggleSavePost(post: FirestorePost) {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        val isSaved = post.id in _savedPostIds.value
        viewModelScope.launch {
            if (isSaved) repo.unsavePost(post.id) else repo.savePost(post)
        }
    }

    fun deletePost(postId: String) {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        viewModelScope.launch {
            val result = repo.deletePost(postId)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_post_deleted", appLanguage.value))
            }
        }
    }

    fun addComment(postId: String, content: String, postAuthorId: String = "") {
        if (Firebase.auth.currentUser == null) return
        val repo = firestorePostRepo ?: return
        val user = currentUser.value
        viewModelScope.launch {
            try {
                val result = repo.addComment(postId, user.name, user.flagEmoji, content)
                if (result.isSuccess) {
                    awardVibeXp(5)
                    showInAppNotification(AppStrings.t("notif_comment_posted", appLanguage.value))
                    if (postAuthorId.isNotBlank() && postAuthorId != Firebase.auth.currentUser?.uid) {
                        firestoreLearningRepo?.createNotificationForUser(
                            recipientId = postAuthorId,
                            senderName = user.name,
                            type = "post_comment",
                            title = "${user.name} commented on your moment",
                            body = content.trim().take(120),
                            targetId = postId
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to add comment: ${e.message}")
            }
        }
    }

    fun listenToAllUsers() {
        if (Firebase.auth.currentUser == null) return
        val repo = firestoreUserRepo ?: return
        val currentUid = Firebase.auth.currentUser?.uid ?: return
        allUsersJob?.cancel()
        allUsersJob = viewModelScope.launch {
            try {
                repo.observeAllUsers().collect { firestoreUsers ->
                    repository.syncFirestoreUsers(firestoreUsers, currentUid)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing all users: ${e.message}")
            }
        }
    }

    fun listenToConversations() {
        if (Firebase.auth.currentUser == null) return
        val repo = firestoreChatRepo ?: return
        val currentUid = Firebase.auth.currentUser?.uid ?: return
        conversationsJob?.cancel()
        conversationsJob = viewModelScope.launch {
            try {
                repo.observeConversations().collect { firestoreConversations ->
                    repository.syncFirestoreConversations(firestoreConversations, currentUid)
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing conversations: ${e.message}")
            }
        }
    }

    fun setOnlinePresence(isOnline: Boolean) {
        if (Firebase.auth.currentUser == null) return
        val showPresence = currentUser.value.showOnlineStatus
        viewModelScope.launch {
            try {
                firestoreUserRepo?.setOnlinePresence(isOnline, showPresence)
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed to update online presence: ${e.message}")
            }
        }
    }

    fun listenToVoiceRooms() {
        if (Firebase.auth.currentUser == null) {
            _isVoiceRoomsLoading.value = false
            _voiceRoomsError.value = null
            return
        }
        val repo = firestoreVoiceRoomRepo ?: return
        voiceRoomsJob?.cancel()
        voiceRoomsJob = viewModelScope.launch {
            _isVoiceRoomsLoading.value = true
            _voiceRoomsError.value = null
            try {
                repo.observeActiveVoiceRooms().collect { rooms ->
                    _firestoreVoiceRooms.value = rooms
                    _isVoiceRoomsLoading.value = false
                    _voiceRoomsError.value = null
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error fetching voice rooms from Firestore: ${e.message}")
                _isVoiceRoomsLoading.value = false
                if (Firebase.auth.currentUser != null) {
                    _voiceRoomsError.value = e.localizedMessage ?: "Failed to load voice rooms"
                }
            }
        }
    }

    fun refreshVoiceRooms() {
        listenToVoiceRooms()
    }

    private fun listenToUserProfile(uid: String) {
        if (Firebase.auth.currentUser == null) return
        userProfileJob?.cancel()
        userProfileJob = viewModelScope.launch {
            try {
                firestoreUserRepo?.observeUserProfile(uid)?.collect { profile ->
                    if (profile != null) {
                        val hasLockedCountry = !profile.country.isNullOrBlank()
                        if (hasLockedCountry) {
                            _isCurrentUserCountryLocked.value = true
                        }
                        val matchedCountry = CountryData.allCountries.find {
                            it.code.equals(profile.countryCode, ignoreCase = true) ||
                                it.name.equals(profile.country, ignoreCase = true)
                        }
                        val noAvatar = profile.avatarData.isNullOrBlank() && profile.avatarUrl.isNullOrBlank()
                        repository.updateCurrentUser(
                            name = profile.displayName?.takeIf { it.isNotBlank() } ?: repository.currentUser.value.name,
                            bio = profile.bio ?: repository.currentUser.value.bio,
                            countryName = profile.country?.takeIf { it.isNotBlank() } ?: repository.currentUser.value.countryName,
                            flagEmoji = matchedCountry?.flagEmoji ?: repository.currentUser.value.flagEmoji,
                            interests = if (profile.interests.isNotEmpty()) profile.interests else repository.currentUser.value.interests,
                            languagesSpoken = if (profile.languages.isNotEmpty()) profile.languages else repository.currentUser.value.languagesSpoken,
                            countryCode = profile.countryCode?.takeIf { it.isNotBlank() } ?: matchedCountry?.code,
                            cityName = profile.city ?: repository.currentUser.value.cityName,
                            username = profile.username.takeIf { it.isNotBlank() },
                            avatarUrl = profile.avatarUrl,
                            avatarData = profile.avatarData,
                            clearAvatar = noAvatar,
                            avatarFrame = profile.avatarFrame,
                            nativeLanguages = profile.nativeLanguages,
                            learningLanguages = profile.learningLanguages,
                            learningLanguageLevels = profile.learningLanguageLevels,
                            proficiencyLevel = profile.proficiencyLevel ?: repository.currentUser.value.proficiencyLevel,
                            communicationGoal = profile.communicationGoal ?: repository.currentUser.value.communicationGoal,
                            isDiscoveryVisible = profile.isDiscoveryVisible,
                            allowDirectMessages = profile.allowDirectMessages,
                            showOnlineStatus = profile.showOnlineStatus,
                            lastSeenVisibility = profile.lastSeenVisibility.ifBlank { "everyone" },
                            lastSeenAtMs = profile.lastSeenAtMs,
                            vibeXp = profile.vibeXp,
                            vibeLevel = profile.vibeLevel.coerceAtLeast(1),
                            streakDays = profile.streakDays,
                            unlockedBadges = profile.unlockedBadges
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Error observing user profile: ${e.message}")
            }
        }
    }

    // Tab Navigation Actions
    fun selectTab(tab: NavigationTab) {
        _activeTab.value = tab
    }

    fun openChat(conversationId: String) {
        _activeChatConversationId.value = conversationId
        _aiSmartReplies.value = emptyList()
        repository.markConversationAsRead(conversationId)

        val uid = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            firestoreChatRepo?.markConversationMessagesRead(conversationId)
        }

        activeChatMessagesJob?.cancel()
        activeChatMessagesJob = viewModelScope.launch {
            try {
                firestoreChatRepo?.observeMessages(conversationId)?.collect { firestoreMessages ->
                    repository.syncFirestoreMessages(conversationId, firestoreMessages)
                    if (_autoTranslateChat.value) {
                        autoTranslateIncomingMessages(repository.messages.value[conversationId].orEmpty())
                    }
                    if (firestoreMessages.any { it.senderId != uid && !it.isRead }) {
                        firestoreChatRepo?.markConversationMessagesRead(conversationId)
                    }
                }
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed observing real-time messages: ${e.message}")
            }
        }
    }

    fun closeChat() {
        activeChatMessagesJob?.cancel()
        _activeChatConversationId.value = null
        _aiSmartReplies.value = emptyList()
    }

    fun openUserProfile(user: UserProfile) {
        val latest = repository.getUserById(user.id) ?: user
        _selectedUserProfile.value = latest
    }

    fun openUserProfileById(
        userId: String,
        fallbackName: String,
        fallbackCountry: String = "Global",
        fallbackFlag: String = "🌐"
    ) {
        val cleanId = userId.trim()
        if (cleanId.isBlank()) return
        val cached = repository.getUserById(cleanId)
        if (cached != null) {
            _selectedUserProfile.value = cached
            return
        }
        val countryObj = CountryData.allCountries.find {
            it.name.equals(fallbackCountry, ignoreCase = true) ||
                it.code.equals(fallbackCountry, ignoreCase = true)
        }
        val stub = UserProfile(
            id = cleanId,
            name = fallbackName.ifBlank { "Member" },
            username = fallbackName.filter { it.isLetterOrDigit() }.lowercase().take(18).ifBlank { "viber_${cleanId.take(5)}" },
            bio = "",
            countryName = countryObj?.name ?: fallbackCountry.ifBlank { "Global" },
            countryCode = countryObj?.code ?: "GL",
            flagEmoji = fallbackFlag.ifBlank { countryObj?.flagEmoji ?: "🌐" },
            interests = emptyList(),
            languagesSpoken = emptyList(),
            followersCount = repository.getFollowerProfiles(cleanId).size,
            followingCount = repository.getFollowingProfiles(cleanId).size,
            isFollowing = cleanId in followingIds.value,
            isOnline = false
        )
        _selectedUserProfile.value = stub

        val repo = firestoreUserRepo ?: return
        viewModelScope.launch {
            val fetched = repo.getUserProfile(cleanId).getOrNull() ?: return@launch
            val matchedCountry = CountryData.allCountries.find {
                it.code.equals(fetched.countryCode, ignoreCase = true) ||
                    it.name.equals(fetched.country, ignoreCase = true)
            }
            val resolved = UserProfile(
                id = fetched.userId.ifBlank { fetched.id.ifBlank { cleanId } },
                name = fetched.displayName?.takeIf { it.isNotBlank() } ?: fetched.username.ifBlank { fallbackName },
                username = fetched.username.ifBlank { stub.username },
                bio = fetched.bio.orEmpty(),
                countryName = fetched.country?.takeIf { it.isNotBlank() } ?: matchedCountry?.name ?: stub.countryName,
                countryCode = fetched.countryCode?.takeIf { it.isNotBlank() } ?: matchedCountry?.code ?: stub.countryCode,
                flagEmoji = matchedCountry?.flagEmoji ?: stub.flagEmoji,
                cityName = fetched.city.orEmpty(),
                avatarUrl = fetched.avatarUrl,
                avatarData = fetched.avatarData,
                avatarFrame = fetched.avatarFrame?.takeIf { it.isNotBlank() } ?: "none",
                vibeXp = fetched.vibeXp,
                vibeLevel = fetched.vibeLevel.coerceAtLeast(1),
                streakDays = fetched.streakDays,
                unlockedBadges = fetched.unlockedBadges,
                interests = fetched.interests,
                languagesSpoken = fetched.languages,
                nativeLanguages = fetched.nativeLanguages,
                learningLanguages = fetched.learningLanguages,
                learningLanguageLevels = fetched.learningLanguageLevels,
                proficiencyLevel = fetched.proficiencyLevel.orEmpty(),
                communicationGoal = fetched.communicationGoal.orEmpty(),
                followersCount = repository.getFollowerProfiles(cleanId).size,
                followingCount = repository.getFollowingProfiles(cleanId).size,
                connectionsCount = repository.getFollowerProfiles(cleanId).size,
                isOnline = fetched.isOnline && fetched.showOnlineStatus,
                showOnlineStatus = fetched.showOnlineStatus,
                lastSeenVisibility = fetched.lastSeenVisibility.ifBlank { "everyone" },
                lastSeenAtMs = fetched.lastSeenAtMs,
                isFollowing = cleanId in followingIds.value,
                isVerified = true,
                isDiscoveryVisible = fetched.isDiscoveryVisible,
                allowDirectMessages = fetched.allowDirectMessages
            )
            if (_selectedUserProfile.value?.id == cleanId) {
                _selectedUserProfile.value = resolved
            }
        }
    }

    fun closeUserProfile() {
        _selectedUserProfile.value = null
    }

    fun openFollowersSheet(user: UserProfile) {
        _followListSheet.value = FollowListSheetState(targetUser = user, isFollowers = true)
    }

    fun openFollowingSheet(user: UserProfile) {
        _followListSheet.value = FollowListSheetState(targetUser = user, isFollowers = false)
    }

    fun closeFollowListSheet() {
        _followListSheet.value = null
    }

    fun getUsersForFollowSheet(state: FollowListSheetState): List<UserProfile> {
        return if (state.isFollowers) {
            repository.getFollowerProfiles(state.targetUser.id)
        } else {
            repository.getFollowingProfiles(state.targetUser.id)
        }
    }

    fun setChatTranslateLanguage(language: String) {
        val clean = language.trim().takeIf { it.isNotEmpty() }
        _chatTranslateLanguage.value = clean
    }

    fun openActiveRoomModal() {
        if (activeRoom.value != null) {
            _showActiveRoomModal.value = true
        }
    }

    fun closeActiveRoomModal() {
        _showActiveRoomModal.value = false
    }

    fun openCreateRoomDialog() {
        _showCreateRoomDialog.value = true
    }

    fun closeCreateRoomDialog() {
        _showCreateRoomDialog.value = false
    }

    fun openEditProfileDialog() {
        _showEditProfileDialog.value = true
    }

    fun closeEditProfileDialog() {
        _showEditProfileDialog.value = false
    }

    fun openReportDialog(user: UserProfile) {
        _reportingUser.value = user
    }

    fun closeReportDialog() {
        _reportingUser.value = null
    }

    fun blockUser(userId: String) {
        repository.blockUser(userId)
        if (_selectedUserProfile.value?.id == userId) {
            _selectedUserProfile.value = null
        }
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = social.blockUser(userId)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_user_blocked", appLanguage.value))
            }
        }
    }

    fun unblockUser(userId: String) {
        repository.unblockUser(userId)
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = social.unblockUser(userId)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_user_unblocked", appLanguage.value))
            }
        }
    }

    fun submitReport(reason: String, details: String) {
        val user = _reportingUser.value
        _reportingUser.value = null
        if (user == null) return
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = social.submitReport("user", user.id, reason, details)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_report_submitted", appLanguage.value))
            }
        }
    }

    fun reportVoiceRoom(roomId: String, reason: String, details: String = "") {
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = social.submitReport("voice_room", roomId, reason, details)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_report_submitted", appLanguage.value))
            }
        }
    }

    fun updatePrivacySettings(
        isDiscoveryVisible: Boolean,
        allowDirectMessages: Boolean,
        showOnlineStatus: Boolean = currentUser.value.showOnlineStatus,
        lastSeenVisibility: String = currentUser.value.lastSeenVisibility
    ) {
        repository.updateCurrentUserPrivacy(
            isDiscoveryVisible = isDiscoveryVisible,
            allowDirectMessages = allowDirectMessages,
            showOnlineStatus = showOnlineStatus,
            lastSeenVisibility = lastSeenVisibility
        )
        val userRepo = firestoreUserRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            userRepo.updatePrivacySettings(
                isDiscoveryVisible = isDiscoveryVisible,
                allowDirectMessages = allowDirectMessages,
                showOnlineStatus = showOnlineStatus,
                lastSeenVisibility = lastSeenVisibility
            )
        }
    }

    fun sendFriendRequest(user: UserProfile) {
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = social.sendFriendRequest(
                receiverId = user.id,
                senderName = currentUser.value.name,
                receiverName = user.name
            )
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_friend_request_sent", appLanguage.value))
                firestoreLearningRepo?.createNotificationForUser(
                    recipientId = user.id,
                    senderName = currentUser.value.name,
                    type = "friend_request",
                    title = "${currentUser.value.name} sent a friend request",
                    body = "Tap Worldwide to respond",
                    targetId = result.getOrNull()
                )
            }
        }
    }

    fun acceptFriendRequest(requestId: String) {
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        val req = _incomingFriendRequests.value.firstOrNull { it.id == requestId }
        viewModelScope.launch {
            val result = social.respondToFriendRequest(requestId, accept = true)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_friend_accepted", appLanguage.value))
                if (req != null) {
                    firestoreLearningRepo?.createNotificationForUser(
                        recipientId = req.senderId,
                        senderName = currentUser.value.name,
                        type = "friend_accepted",
                        title = "${currentUser.value.name} accepted your friend request",
                        body = "You are now friends on VibeRoom!",
                        targetId = requestId
                    )
                }
            }
        }
    }

    fun declineFriendRequest(requestId: String) {
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            social.respondToFriendRequest(requestId, accept = false)
        }
    }

    fun toggleNearbyMode() {
        _isNearbyMode.update { !it }
    }

    fun setOnlyOnlineUsers(onlineOnly: Boolean) {
        _onlyOnlineUsers.value = onlineOnly
    }

    fun setSelectedCountryFilter(country: String) {
        _selectedCountryFilter.value = country
    }

    fun setLanguageMatchOnly(enabled: Boolean) {
        _languageMatchOnly.value = enabled
    }

    fun openLearningHub() {
        _showLearningHubModal.value = true
    }

    fun closeLearningHub() {
        _showLearningHubModal.value = false
    }

    fun openNotifications() {
        _showNotificationsDialog.value = true
    }

    fun closeNotifications() {
        _showNotificationsDialog.value = false
    }

    fun markNotificationRead(notificationId: String) {
        viewModelScope.launch {
            firestoreLearningRepo?.markNotificationRead(notificationId)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            firestoreLearningRepo?.markAllNotificationsRead()
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        viewModelScope.launch {
            try {
                preferencesStore?.saveNotificationsEnabled(enabled)
            } catch (_: Exception) {
            }
        }
    }

    fun setDailyGoalTarget(target: Int) {
        val clamped = target.coerceIn(1, 50)
        _dailyGoalTarget.value = clamped
        viewModelScope.launch {
            try {
                preferencesStore?.saveDailyGoalTarget(clamped)
            } catch (_: Exception) {
            }
        }
    }

    fun setDiscoveryRadius(radiusKm: Int) {
        repository.setDiscoveryRadius(radiusKm)
    }

    fun setLocationSharing(enabled: Boolean) {
        repository.setLocationSharing(enabled)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSelectedRoomCategory(category: RoomCategory) {
        _selectedRoomCategory.value = category
    }

    private fun resolveReceiverId(conversationId: String, currentUid: String): String {
        val fromConv = conversations.value.firstOrNull { it.id == conversationId }?.peerUser?.id
        if (!fromConv.isNullOrBlank()) return fromConv
        if (conversationId.startsWith("dm_")) {
            val parts = conversationId.removePrefix("dm_").split("_")
            val candidate = parts.firstOrNull { it != currentUid && it.isNotBlank() }
            if (!candidate.isNullOrBlank()) return candidate
        }
        return "peer"
    }

    fun sendMessage(
        conversationId: String,
        text: String,
        replyToMessageId: String? = null,
        replyToText: String? = null,
        replyToSenderName: String? = null
    ) {
        val cleanText = text.trim()
        if (cleanText.isBlank()) return
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            showInAppNotification(AppStrings.t("sign_in", appLanguage.value))
            return
        }
        val nowMs = System.currentTimeMillis()
        val dedupSig = "$conversationId|$cleanText"
        if (dedupSig == lastSentMessageSignature && (nowMs - lastSentMessageAtMs) < 900L) {
            return
        }
        lastSentMessageSignature = dedupSig
        lastSentMessageAtMs = nowMs

        val ttl = _chatDisappearingTtlMs.value
        val expiresAt = ttl.takeIf { it > 0L }?.let { nowMs + it }
        _aiSmartReplies.value = emptyList()
        val msgId = repository.sendMessage(
            conversationId = conversationId,
            text = cleanText,
            replyToMessageId = replyToMessageId,
            replyToText = replyToText,
            replyToSenderName = replyToSenderName,
            expiresAtMs = expiresAt
        )
        val receiverId = resolveReceiverId(conversationId, uid)
        val senderName = currentUser.value.name

        viewModelScope.launch {
            try {
                val res = firestoreChatRepo?.sendMessage(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    text = cleanText,
                    senderName = senderName,
                    messageId = msgId,
                    mediaType = "text",
                    replyToMessageId = replyToMessageId,
                    replyToText = replyToText,
                    replyToSenderName = replyToSenderName,
                    expiresAtMs = expiresAt
                )
                if (res?.isSuccess == true) {
                    awardVibeXp(3)
                }
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to send message via Firestore: ${e.message}")
            }
        }
    }

    fun forwardMessage(message: ChatMessage, targetConversationId: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        if (message.isDeleted) return
        val msgId = repository.forwardMessage(targetConversationId, message)
        val receiverId = resolveReceiverId(targetConversationId, uid)
        val senderName = currentUser.value.name
        val cleanText = message.text.ifBlank {
            when (message.mediaType) {
                "image" -> "📷 Photo"
                "voice" -> "🎙️ Voice message"
                else -> "Forwarded message"
            }
        }

        viewModelScope.launch {
            try {
                firestoreChatRepo?.sendMessage(
                    conversationId = targetConversationId,
                    receiverId = receiverId,
                    text = cleanText,
                    senderName = senderName,
                    messageId = msgId,
                    mediaType = message.mediaType,
                    audioDurationSec = message.audioDurationSec,
                    mediaData = message.mediaData,
                    mediaUrl = message.mediaUrl,
                    isForwarded = true
                )
                showInAppNotification(AppStrings.t("notif_forwarded", appLanguage.value))
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to forward message: ${e.message}")
            }
        }
    }

    fun editMessage(conversationId: String, messageId: String, newText: String) {
        if (newText.isBlank() || Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            firestoreChatRepo?.editMessage(conversationId, messageId, newText)
        }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            firestoreChatRepo?.softDeleteMessage(conversationId, messageId)
        }
    }

    fun toggleMessageReaction(
        conversationId: String,
        messageId: String,
        currentReactions: Map<String, String>,
        emoji: String
    ) {
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            firestoreChatRepo?.toggleMessageReaction(conversationId, messageId, currentReactions, emoji)
        }
    }

    fun pinMessage(conversationId: String, messageId: String?, messageText: String?) {
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            firestoreChatRepo?.pinMessage(conversationId, messageId, messageText)
        }
    }

    fun sendVoiceMessage(conversationId: String, durationSec: Int, mediaData: String? = null) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val boundedDuration = durationSec.coerceIn(1, 600)
        val msgId = repository.sendVoiceMessage(conversationId, boundedDuration, mediaData)
        val receiverId = resolveReceiverId(conversationId, uid)
        val senderName = currentUser.value.name

        viewModelScope.launch {
            try {
                firestoreChatRepo?.sendMessage(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    text = "🎙️ Voice message (${boundedDuration}s)",
                    senderName = senderName,
                    messageId = msgId,
                    mediaType = "voice",
                    audioDurationSec = boundedDuration,
                    mediaData = mediaData
                )
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to send voice message via Firestore: ${e.message}")
            }
        }
    }

    fun sendImageMessage(context: Context, conversationId: String, imageUri: Uri, caption: String = "") {
        attachContext(context)
        val uid = Firebase.auth.currentUser?.uid ?: return
        val receiverId = resolveReceiverId(conversationId, uid)
        val senderName = currentUser.value.name

        viewModelScope.launch {
            _isMediaUploading.value = true
            try {
                val encoded = MediaAttachmentHelper.encodeImageDataUri(context, imageUri)
                if (encoded == null) {
                    showInAppNotification("Could not compress selected image.")
                    return@launch
                }
                val msgId = repository.sendImageMessage(conversationId, caption, encoded)
                firestoreChatRepo?.sendMessage(
                    conversationId = conversationId,
                    receiverId = receiverId,
                    text = caption.ifBlank { "📷 Photo" },
                    senderName = senderName,
                    messageId = msgId,
                    mediaType = "image",
                    mediaData = encoded
                )
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to send image message: ${e.message}")
            } finally {
                _isMediaUploading.value = false
            }
        }
    }

    // --- 1-to-1 Free WebRTC Voice Calls ---

    fun startVoiceCall(peerUser: UserProfile, conversationId: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val callRepo = firestoreCallRepo ?: return
        val rtc = webRtcVoiceManager ?: return
        if (_activeCall.value != null) return

        val callId = "call_${UUID.randomUUID().toString().replace("-", "").take(20)}"
        val me = currentUser.value
        val placeholderCall = FirestoreVoiceCall(
            id = callId,
            callerId = uid,
            callerName = me.name,
            callerFlag = me.flagEmoji,
            calleeId = peerUser.id,
            calleeName = peerUser.name,
            conversationId = conversationId,
            status = "ringing"
        )
        _activeCall.value = placeholderCall
        _isCallMuted.value = false
        _isCallSpeakerOn.value = false
        _callDurationSec.value = 0

        rtc.startOutgoingCall(
            onOfferCreated = { offerSdp ->
                viewModelScope.launch {
                    callRepo.createOutgoingCall(
                        callId = callId,
                        calleeId = peerUser.id,
                        calleeName = peerUser.name,
                        callerName = me.name,
                        callerFlag = me.flagEmoji,
                        conversationId = conversationId,
                        offerSdp = offerSdp
                    )
                    watchActiveCall(callId, uid)
                }
            },
            onLocalIceCandidate = { sdpMid, sdpMLineIndex, sdp ->
                viewModelScope.launch {
                    callRepo.sendCallIceCandidate(callId, sdpMid, sdpMLineIndex, sdp)
                }
            }
        )
    }

    fun acceptIncomingCall(call: FirestoreVoiceCall) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val callRepo = firestoreCallRepo ?: return
        val rtc = webRtcVoiceManager ?: return
        val offerSdp = call.offerSdp ?: return

        _incomingCall.value = null
        _activeCall.value = call.copy(status = "connected")
        _isCallMuted.value = false
        _isCallSpeakerOn.value = false
        _callDurationSec.value = 0

        rtc.acceptIncomingCall(
            offerSdp = offerSdp,
            onAnswerCreated = { answerSdp ->
                viewModelScope.launch {
                    callRepo.answerCall(call.id, answerSdp)
                    watchActiveCall(call.id, uid)
                }
            },
            onLocalIceCandidate = { sdpMid, sdpMLineIndex, sdp ->
                viewModelScope.launch {
                    callRepo.sendCallIceCandidate(call.id, sdpMid, sdpMLineIndex, sdp)
                }
            }
        )
    }

    fun declineIncomingCall(call: FirestoreVoiceCall) {
        _incomingCall.value = null
        viewModelScope.launch {
            firestoreCallRepo?.updateCallStatus(call.id, "declined")
        }
    }

    fun endVoiceCall() {
        val callId = _activeCall.value?.id
        activeCallWatchJob?.cancel()
        activeCallCandidatesJob?.cancel()
        activeCallTimerJob?.cancel()
        webRtcVoiceManager?.endCall()
        _activeCall.value = null
        _isCallMuted.value = false
        _isCallSpeakerOn.value = false
        _callDurationSec.value = 0
        if (!callId.isNullOrBlank()) {
            viewModelScope.launch {
                firestoreCallRepo?.updateCallStatus(callId, "ended")
            }
        }
    }

    fun toggleCallMute() {
        val next = !_isCallMuted.value
        _isCallMuted.value = next
        webRtcVoiceManager?.setMicEnabled(!next)
    }

    fun toggleCallSpeaker() {
        val next = !_isCallSpeakerOn.value
        _isCallSpeakerOn.value = next
        webRtcVoiceManager?.setSpeakerphoneEnabled(next)
    }

    private fun watchActiveCall(callId: String, myUid: String) {
        val callRepo = firestoreCallRepo ?: return
        val rtc = webRtcVoiceManager ?: return
        val seenCandidateIds = mutableSetOf<String>()

        activeCallWatchJob?.cancel()
        activeCallWatchJob = viewModelScope.launch {
            callRepo.observeCall(callId).collect { updated ->
                if (updated == null) return@collect
                _activeCall.value = updated
                if (!updated.answerSdp.isNullOrBlank() && updated.callerId == myUid) {
                    rtc.applyCallAnswer(updated.answerSdp)
                }
                if (updated.status == "connected" && activeCallTimerJob?.isActive != true) {
                    activeCallTimerJob = viewModelScope.launch {
                        while (true) {
                            delay(1000)
                            _callDurationSec.update { it + 1 }
                        }
                    }
                }
                if (updated.status == "ended" || updated.status == "declined") {
                    activeCallWatchJob?.cancel()
                    activeCallCandidatesJob?.cancel()
                    activeCallTimerJob?.cancel()
                    rtc.endCall()
                    _activeCall.value = null
                    _callDurationSec.value = 0
                }
            }
        }

        activeCallCandidatesJob?.cancel()
        activeCallCandidatesJob = viewModelScope.launch {
            callRepo.observeCallCandidates(callId).collect { candidates ->
                candidates.forEach { cand ->
                    if (cand.senderId != myUid && seenCandidateIds.add(cand.id)) {
                        rtc.addCallRemoteIceCandidate(cand.sdpMid, cand.sdpMLineIndex, cand.sdp)
                    }
                }
            }
        }
    }

    fun startChatWithUser(user: UserProfile): Boolean {
        val hasExistingConversation = repository.conversations.value.any { it.peerUser.id == user.id }
        if (!hasExistingConversation && !user.allowDirectMessages) {
            showInAppNotification(AppStrings.t("dm_disabled_notice", appLanguage.value))
            return false
        }
        val conv = repository.getOrCreateConversationForUser(user)
        openChat(conv.id)
        return true
    }

    fun waveAtUser(user: UserProfile) {
        if (Firebase.auth.currentUser == null) return
        val hasExistingConversation = repository.conversations.value.any { it.peerUser.id == user.id }
        if (!hasExistingConversation && !user.allowDirectMessages) {
            showInAppNotification(AppStrings.t("dm_disabled_notice", appLanguage.value))
            return
        }
        val conv = repository.getOrCreateConversationForUser(user)
        val greeting = AppStrings.t("wave_greeting", appLanguage.value)
        sendMessage(conv.id, greeting)
        showInAppNotification("${AppStrings.t("notif_wave_sent", appLanguage.value)} ${user.name}")
    }

    fun toggleFollowUser(userId: String) {
        val wasFollowing = userId in repository.followingIds.value
        repository.toggleFollowUser(userId)
        val social = firestoreSocialRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = if (wasFollowing) social.unfollowUser(userId) else social.followUser(userId)
            if (result.isFailure) {
                repository.toggleFollowUser(userId)
            } else if (!wasFollowing) {
                firestoreLearningRepo?.createNotificationForUser(
                    recipientId = userId,
                    senderName = currentUser.value.name,
                    type = "follow",
                    title = "${currentUser.value.name} followed you",
                    body = "Check out their profile in Worldwide",
                    targetId = Firebase.auth.currentUser?.uid
                )
            }
        }
    }

    fun toggleJoinCommunity(communityId: String) {
        repository.toggleJoinCommunity(communityId)
    }

    fun toggleLikeCommunityPost(communityId: String, postId: String) {
        repository.toggleLikeCommunityPost(communityId, postId)
    }

    fun joinVoiceRoom(room: VoiceRoom) {
        val uid = Firebase.auth.currentUser?.uid
        val nowMs = System.currentTimeMillis()
        if (uid != null && uid in room.bannedIds) {
            showInAppNotification(AppStrings.t("vr_banned_notice", appLanguage.value))
            return
        }
        if (uid != null && room.isUserBanned(uid, nowMs)) {
            showInAppNotification(AppStrings.t("vr_temp_banned_notice", appLanguage.value))
            return
        }
        if (!room.lifecycleState.isDiscoverable() || !room.isLive) {
            showInAppNotification(AppStrings.t("vr_room_ended_notice", appLanguage.value))
            return
        }
        webRtcVoiceManager?.clearError()
        webRtcVoiceManager?.setHostMuted(false)
        isInitiatedLocalLeave = false
        lastNotifiedHostMuted = false
        _roomEvents.value = emptyList()
        _roomPolls.value = emptyList()
        _supporterLeaderboard.value = emptyList()
        processedRoomEventKeys.clear()
        pendingGiftQueue.clear()
        pendingEntranceQueue.clear()
        _currentGiftVisual.value = null
        _currentEntranceVisual.value = null
        roomJoinedAtMs = nowMs

        val hasMic = refreshMicPermissionStatus()
        repository.joinVoiceRoom(room)
        _micPermissionDeniedInRoom.value = false
        val uidLocal = uid
        val shouldStartAsSpeaker = uidLocal != null && (room.host.id == uidLocal || uidLocal in room.invitedSpeakerIds)
        val initialSeat = if (room.host.id == uidLocal) {
            0
        } else {
            room.seatAssignments.entries.firstOrNull { it.value == uidLocal }?.key ?: -1
        }
        val startMuted = !(shouldStartAsSpeaker && hasMic)
        pendingUnmuteAfterPermission = shouldStartAsSpeaker && !hasMic
        if (shouldStartAsSpeaker) {
            repository.setMySpeakerState(
                speaker = true,
                muted = startMuted,
                handRaised = false,
                seatIndex = initialSeat
            )
        }
        _showActiveRoomModal.value = true
        _roomAssistantOutput.value = room.aiSummary
        val repo = firestoreVoiceRoomRepo
        val me = currentUser.value
        if (repo != null && uid != null) {
            viewModelScope.launch {
                try {
                    val joinResult = repo.joinVoiceRoom(room.id)
                    if (joinResult.isFailure) {
                        isInitiatedLocalLeave = true
                        repository.leaveVoiceRoom()
                        _showActiveRoomModal.value = false
                        stopRoomWebRtcSync(room.id)
                        showInAppNotification(AppStrings.t("vr_room_ended_notice", appLanguage.value))
                        return@launch
                    }
                    awardVibeXp(5)
                    val entranceStyle = _defaultEntranceStyle.value
                    val enterEvent = VoiceRoomLiveEvent(
                        id = "local_${System.currentTimeMillis()}",
                        roomId = room.id,
                        type = "entrance",
                        senderId = uid,
                        senderName = me.name,
                        senderFlag = me.flagEmoji,
                        senderVipLevel = me.vibeLevel,
                        payloadId = entranceStyle.id,
                        text = entranceStyle.id,
                        timestampMs = System.currentTimeMillis()
                    )
                    handleNewRoomEventSideEffects(enterEvent)
                    repo.sendRoomEvent(enterEvent)
                } catch (e: Exception) {
                    Log.w("VibeRoomViewModel", "Failed to sync joinVoiceRoom to Firestore: ${e.message}")
                }
            }
            startRoomWebRtcSync(
                roomId = room.id,
                myUid = uid,
                isSpeaker = shouldStartAsSpeaker,
                isMuted = startMuted,
                seatIndex = initialSeat
            )
        }
    }

    fun leaveVoiceRoom(forceEndRoom: Boolean = false) {
        isInitiatedLocalLeave = true
        lastNotifiedHostMuted = false
        val room = activeRoom.value
        val currentUid = Firebase.auth.currentUser?.uid
        repository.leaveVoiceRoom()
        _showActiveRoomModal.value = false
        _roomAssistantOutput.value = null
        stopRoomWebRtcSync(room?.id)
        if (room != null) {
            val repo = firestoreVoiceRoomRepo
            if (repo != null && currentUid != null) {
                viewModelScope.launch {
                    try {
                        if (room.host.id == currentUid) {
                            val successorId = room.successorHostId
                                ?: if (!forceEndRoom) room.coHostIds.firstOrNull { id ->
                                    (room.speakers + room.listeners).any { it.id == id }
                                } else null
                            val successorProfile = if (!forceEndRoom && !successorId.isNullOrBlank()) {
                                (room.speakers + room.listeners).firstOrNull { it.id == successorId }
                                    ?: repository.getUserById(successorId)
                            } else null

                            if (!forceEndRoom && successorProfile != null) {
                                repo.sendRoomEvent(
                                    VoiceRoomLiveEvent(
                                        id = "sys_${System.currentTimeMillis()}",
                                        roomId = room.id,
                                        type = "system",
                                        senderId = currentUid,
                                        senderName = currentUser.value.name,
                                        text = "${successorProfile.name} is now host",
                                        timestampMs = System.currentTimeMillis()
                                    )
                                )
                                repo.transferHostAndLeave(
                                    roomId = room.id,
                                    newHostId = successorProfile.id,
                                    newHostName = successorProfile.name,
                                    newHostCountry = successorProfile.countryName,
                                    newHostFlag = successorProfile.flagEmoji
                                )
                            } else {
                                repo.endVoiceRoom(room.id, RoomLifecycleState.ENDED)
                            }
                        } else {
                            repo.leaveVoiceRoom(room.id)
                        }
                    } catch (e: Exception) {
                        Log.w("VibeRoomViewModel", "Failed to sync leaveVoiceRoom to Firestore: ${e.message}")
                    }
                }
            }
        }
    }

    fun endVoiceRoomNow() {
        leaveVoiceRoom(forceEndRoom = true)
    }

    fun refreshMicPermissionStatus(): Boolean {
        val ctx = appContext
        val granted = if (ctx != null) {
            try {
                ContextCompat.checkSelfPermission(
                    ctx,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            } catch (_: Throwable) {
                false
            }
        } else {
            _hasMicPermission.value
        }
        _hasMicPermission.value = granted
        if (granted) {
            _micPermissionDeniedInRoom.value = false
        }
        webRtcVoiceManager?.refreshMicPermission()
        return granted
    }

    fun onMicPermissionResult(granted: Boolean) {
        _hasMicPermission.value = granted
        val room = activeRoom.value
        val uid = Firebase.auth.currentUser?.uid
        val callRepo = firestoreCallRepo
        if (granted) {
            _micPermissionDeniedInRoom.value = false
            val shouldUnmuteNow = room != null && (
                pendingUnmuteAfterPermission || (isSpeaker.value && isMicMuted.value)
            )
            pendingUnmuteAfterPermission = false
            if (shouldUnmuteNow) {
                repository.setMySpeakerState(speaker = true, muted = false, handRaised = false)
            }
            val effectiveEnable = isSpeaker.value && !isMicMuted.value && !isHostMuted.value
            if (room != null && uid != null && callRepo != null) {
                webRtcVoiceManager?.onMicPermissionChanged(
                    granted = true,
                    shouldEnableMic = effectiveEnable,
                    myUid = uid,
                    onSignalToPeer = { toUid, type, sdp, mid, idx ->
                        viewModelScope.launch {
                            callRepo.sendRoomSignal(room.id, toUid, type, sdp, mid, idx)
                        }
                    }
                )
                val me = currentUser.value
                viewModelScope.launch {
                    callRepo.upsertRoomPeer(
                        roomId = room.id,
                        displayName = me.name,
                        isSpeaker = isSpeaker.value,
                        isMuted = if (isHostMuted.value) true else isMicMuted.value,
                        isHandRaised = isHandRaised.value,
                        seatIndex = mySeatIndex.value,
                        flagEmoji = me.flagEmoji,
                        avatarFrame = me.avatarFrame,
                        avatarColorHex = me.avatarColorHex
                    )
                }
            } else {
                webRtcVoiceManager?.refreshMicPermission()
                webRtcVoiceManager?.setMicEnabled(effectiveEnable)
            }
        } else {
            pendingUnmuteAfterPermission = false
            _micPermissionDeniedInRoom.value = true
            repository.setMySpeakerState(
                speaker = isSpeaker.value,
                muted = true,
                handRaised = isHandRaised.value
            )
            webRtcVoiceManager?.onMicPermissionChanged(
                granted = false,
                shouldEnableMic = false,
                myUid = uid.orEmpty(),
                onSignalToPeer = { _, _, _, _, _ -> }
            )
            if (room != null && uid != null && callRepo != null) {
                val me = currentUser.value
                viewModelScope.launch {
                    callRepo.upsertRoomPeer(
                        roomId = room.id,
                        displayName = me.name,
                        isSpeaker = isSpeaker.value,
                        isMuted = true,
                        isHandRaised = isHandRaised.value,
                        seatIndex = mySeatIndex.value,
                        flagEmoji = me.flagEmoji,
                        avatarFrame = me.avatarFrame,
                        avatarColorHex = me.avatarColorHex
                    )
                }
            }
            showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage.value))
        }
    }

    fun toggleMic() {
        val room = activeRoom.value
        val uid = Firebase.auth.currentUser?.uid
        if (isHostMuted.value || webRtcVoiceManager?.isHostMutedLocked() == true) {
            repository.setHostMutedState(true)
            webRtcVoiceManager?.setMicEnabled(false)
            showInAppNotification(AppStrings.t("vr_host_muted_banner", appLanguage.value))
            return
        }
        val isLiveFirestoreRoom = room != null && uid != null && firestoreCallRepo != null
        if (isLiveFirestoreRoom && !isSpeaker.value && room!!.host.id != uid) {
            if (room.isStageLocked && uid !in room.invitedSpeakerIds && !room.isModeratorOrHigher(uid)) {
                showInAppNotification(AppStrings.t("vr_stage_locked_notice", appLanguage.value))
                return
            }
            claimStageSeat(-1)
            return
        }
        val wantsToUnmute = !isSpeaker.value || isMicMuted.value
        val hasMic = refreshMicPermissionStatus()
        if (wantsToUnmute && !hasMic) {
            pendingUnmuteAfterPermission = true
            _micPermissionDeniedInRoom.value = true
            webRtcVoiceManager?.setMicEnabled(false)
            showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage.value))
            return
        }
        pendingUnmuteAfterPermission = false
        repository.toggleMic()
        val muted = isMicMuted.value
        val speaker = isSpeaker.value
        webRtcVoiceManager?.setMicEnabled(speaker && !muted && !isHostMuted.value && hasMic)
        if (room != null && uid != null) {
            val me = currentUser.value
            viewModelScope.launch {
                firestoreCallRepo?.upsertRoomPeer(
                    roomId = room.id,
                    displayName = me.name,
                    isSpeaker = speaker,
                    isMuted = muted,
                    isHandRaised = isHandRaised.value,
                    seatIndex = mySeatIndex.value,
                    flagEmoji = me.flagEmoji,
                    avatarFrame = me.avatarFrame,
                    avatarColorHex = me.avatarColorHex
                )
            }
        }
    }

    fun claimStageSeat(requestedSeatIndex: Int = -1) {
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        if (room.isStageLocked && !room.isModeratorOrHigher(uid) && uid !in room.invitedSpeakerIds) {
            if (!isHandRaised.value) toggleRaiseHand()
            showInAppNotification(AppStrings.t("vr_stage_locked_notice", appLanguage.value))
            return
        }
        val voiceRepo = firestoreVoiceRoomRepo ?: return
        val hasMic = refreshMicPermissionStatus()
        if (!hasMic) {
            _micPermissionDeniedInRoom.value = true
        }
        viewModelScope.launch {
            val isAuthorized = room.isModeratorOrHigher(uid) || uid in room.invitedSpeakerIds
            val result = voiceRepo.claimStageSeat(
                roomId = room.id,
                requestedSeatIndex = requestedSeatIndex,
                isStageLocked = room.isStageLocked,
                isModeratorOrInvited = isAuthorized
            )
            result.onSuccess { seat ->
                val muted = isHostMuted.value || !hasMic
                repository.setMySpeakerState(
                    speaker = true,
                    muted = muted,
                    handRaised = false,
                    seatIndex = seat
                )
                webRtcVoiceManager?.setMicEnabled(!muted)
                if (!hasMic) {
                    showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage.value))
                }
            }.onFailure { err ->
                showInAppNotification(
                    err.localizedMessage ?: AppStrings.t("vr_join_stage_failed", appLanguage.value)
                )
            }
        }
    }

    fun stepDownFromStage() {
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        if (uid == room.host.id) return
        repository.setMySpeakerState(speaker = false, muted = true, handRaised = false, seatIndex = -1)
        webRtcVoiceManager?.setMicEnabled(false)
        val me = currentUser.value
        viewModelScope.launch {
            firestoreCallRepo?.upsertRoomPeer(
                roomId = room.id,
                displayName = me.name,
                isSpeaker = false,
                isMuted = true,
                isHandRaised = false,
                seatIndex = -1,
                flagEmoji = me.flagEmoji,
                avatarFrame = me.avatarFrame,
                avatarColorHex = me.avatarColorHex
            )
        }
    }

    fun toggleRoomSpeakerphone() {
        val next = when (_audioRoute.value) {
            AudioRouteMode.SPEAKER -> AudioRouteMode.EARPIECE
            AudioRouteMode.EARPIECE -> if (_isBluetoothAvailable.value) AudioRouteMode.BLUETOOTH else AudioRouteMode.SPEAKER
            AudioRouteMode.BLUETOOTH -> AudioRouteMode.SPEAKER
        }
        setAudioRoute(next)
    }

    fun setAudioRoute(route: AudioRouteMode) {
        _audioRoute.value = route
        _isRoomSpeakerphone.value = (route == AudioRouteMode.SPEAKER)
        webRtcVoiceManager?.setAudioRoute(route)
    }

    fun toggleRaiseHand() {
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid
        if (!isHandRaised.value && room.isStageLocked && uid != null && !room.isModeratorOrHigher(uid) && uid !in room.invitedSpeakerIds) {
            showInAppNotification(AppStrings.t("vr_stage_locked_notice", appLanguage.value))
            return
        }
        repository.toggleRaiseHand()
        if (uid == null) return
        val me = currentUser.value
        viewModelScope.launch {
            firestoreCallRepo?.upsertRoomPeer(
                roomId = room.id,
                displayName = me.name,
                isSpeaker = isSpeaker.value,
                isMuted = isMicMuted.value,
                isHandRaised = isHandRaised.value,
                seatIndex = mySeatIndex.value,
                flagEmoji = me.flagEmoji,
                avatarFrame = me.avatarFrame,
                avatarColorHex = me.avatarColorHex
            )
        }
    }

    fun promotePeerToSpeaker(targetUid: String, preferredSeatIndex: Int = -1) {
        val room = activeRoom.value ?: return
        if (room.speakers.size >= room.maxAudioParticipants && room.speakers.none { it.id == targetUid }) {
            showInAppNotification(AppStrings.t("vr_stage_capacity_reached", appLanguage.value))
            return
        }
        val assignedSeat = if (preferredSeatIndex in 0..5 && !room.seatAssignments.containsKey(preferredSeatIndex)) {
            preferredSeatIndex
        } else {
            (0..5).firstOrNull { !room.seatAssignments.containsKey(it) } ?: 1
        }
        val nextInvited = room.invitedSpeakerIds + targetUid
        repository.updateActiveRoomMetadata(invitedSpeakerIds = nextInvited)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomRoles(room.id, invitedSpeakerIds = nextInvited.toList())
            firestoreCallRepo?.setPeerStageState(
                roomId = room.id,
                targetUid = targetUid,
                isSpeaker = true,
                seatIndex = assignedSeat,
                isHandRaised = false
            )
        }
    }

    fun demotePeerToListener(targetUid: String) {
        val room = activeRoom.value ?: return
        if (targetUid == room.host.id) return
        val nextInvited = room.invitedSpeakerIds - targetUid
        repository.updateActiveRoomMetadata(invitedSpeakerIds = nextInvited)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomRoles(room.id, invitedSpeakerIds = nextInvited.toList())
            firestoreCallRepo?.setPeerStageState(
                roomId = room.id,
                targetUid = targetUid,
                isSpeaker = false,
                seatIndex = -1,
                isHandRaised = false
            )
        }
    }

    fun mutePeerByHost(targetUid: String, shouldMute: Boolean) {
        val room = activeRoom.value ?: return
        viewModelScope.launch {
            firestoreCallRepo?.muteOrRestrictPeer(
                roomId = room.id,
                targetUid = targetUid,
                hostMuted = shouldMute
            )
            if (!shouldMute) {
                showInAppNotification(AppStrings.t("vr_mic_allowed_notice", appLanguage.value))
            }
        }
    }

    fun muteAllSpeakers() {
        val room = activeRoom.value ?: return
        val speakerIds = room.speakers.map { it.id }
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.muteAllSpeakers(room.id, speakerIds, room.host.id)
            showInAppNotification(AppStrings.t("vr_all_muted_notice", appLanguage.value))
        }
    }

    fun toggleStageLock() {
        val room = activeRoom.value ?: return
        val next = !room.isStageLocked
        repository.updateActiveRoomMetadata(isStageLocked = next)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomConfiguration(room.id, isStageLocked = next)
        }
    }

    fun setRoomBackgroundTheme(theme: RoomBackgroundTheme) {
        val room = activeRoom.value ?: return
        repository.updateActiveRoomMetadata(backgroundTheme = theme.id)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomConfiguration(room.id, backgroundTheme = theme.id)
        }
    }

    fun updatePinnedNotice(notice: String?) {
        val room = activeRoom.value ?: return
        val clean = notice?.trim()?.take(300)
        repository.updateActiveRoomMetadata(
            pinnedNotice = clean,
            clearPinnedNotice = clean.isNullOrBlank()
        )
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomConfiguration(
                roomId = room.id,
                pinnedNotice = clean,
                clearPinnedNotice = clean.isNullOrBlank()
            )
        }
    }

    fun setSuccessorHost(targetUid: String?) {
        val room = activeRoom.value ?: return
        val clean = targetUid?.takeIf { it.isNotBlank() && it != room.host.id }
        repository.updateActiveRoomMetadata(successorHostId = clean)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomConfiguration(room.id, successorHostId = clean.orEmpty())
        }
    }

    fun toggleCoHost(targetUid: String) {
        val room = activeRoom.value ?: return
        if (targetUid == room.host.id) return
        val next = if (targetUid in room.coHostIds) {
            room.coHostIds - targetUid
        } else {
            room.coHostIds + targetUid
        }
        repository.updateActiveRoomMetadata(coHostIds = next)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateCoHosts(room.id, next.toList())
        }
    }

    fun toggleRoomModerator(targetUid: String) {
        val room = activeRoom.value ?: return
        if (targetUid == room.host.id) return
        val next = if (targetUid in room.moderatorIds) {
            room.moderatorIds - targetUid
        } else {
            room.moderatorIds + targetUid
        }
        repository.updateActiveRoomMetadata(moderatorIds = next)
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.updateRoomRoles(room.id, moderatorIds = next.toList())
        }
    }

    fun kickOrBanUser(targetUid: String, banUser: Boolean, tempDurationMs: Long? = null) {
        val room = activeRoom.value ?: return
        if (targetUid == room.host.id) return
        val nextBanned = if (banUser) room.bannedIds + targetUid else room.bannedIds
        val nextCoHosts = room.coHostIds - targetUid
        val nextMods = room.moderatorIds - targetUid
        val nextInvited = room.invitedSpeakerIds - targetUid
        repository.updateActiveRoomMetadata(
            coHostIds = nextCoHosts,
            moderatorIds = nextMods,
            bannedIds = nextBanned,
            invitedSpeakerIds = nextInvited
        )
        viewModelScope.launch {
            if (tempDurationMs != null) {
                firestoreVoiceRoomRepo?.applyTemporaryOrPermanentBan(
                    roomId = room.id,
                    targetUid = targetUid,
                    currentBannedIds = room.bannedIds,
                    banDurationMs = tempDurationMs
                )
            } else if (banUser) {
                firestoreVoiceRoomRepo?.applyTemporaryOrPermanentBan(
                    roomId = room.id,
                    targetUid = targetUid,
                    currentBannedIds = room.bannedIds,
                    banDurationMs = null
                )
                firestoreVoiceRoomRepo?.updateRoomRoles(
                    roomId = room.id,
                    coHostIds = nextCoHosts.toList(),
                    moderatorIds = nextMods.toList(),
                    bannedIds = nextBanned.toList(),
                    invitedSpeakerIds = nextInvited.toList()
                )
            } else {
                firestoreVoiceRoomRepo?.kickPeerFromRoom(room.id, targetUid)
            }
        }
    }

    fun sendReaction(emoji: String) {
        repository.sendRoomReaction(emoji)
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        val me = currentUser.value
        val now = System.currentTimeMillis()
        processedRoomEventKeys.add("reaction_${uid}_${emoji}_$now")
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.sendRoomEvent(
                VoiceRoomLiveEvent(
                    id = "react_$now",
                    roomId = room.id,
                    type = "reaction",
                    senderId = uid,
                    senderName = me.name,
                    senderFlag = me.flagEmoji,
                    senderVipLevel = me.vibeLevel,
                    payloadId = emoji,
                    text = emoji,
                    timestampMs = now
                )
            )
        }
    }

    fun sendRoomChatMessage(text: String) {
        val clean = text.trim().take(500)
        if (clean.isEmpty()) return
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        val me = currentUser.value
        val now = System.currentTimeMillis()
        val event = VoiceRoomLiveEvent(
            id = "chat_$now",
            roomId = room.id,
            type = "chat",
            senderId = uid,
            senderName = me.name,
            senderFlag = me.flagEmoji,
            senderVipLevel = me.vibeLevel,
            text = clean,
            timestampMs = now
        )
        processedRoomEventKeys.add("chat_${uid}__$now")
        _roomEvents.update { (it + event).takeLast(60) }
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.sendRoomEvent(event)
        }
    }

    fun sendVoiceGift(gift: VoiceGift, recipient: UserProfile) {
        val room = activeRoom.value ?: return
        val me = currentUser.value
        val uid = Firebase.auth.currentUser?.uid
        if (gift.xpCost > 0 && me.vibeXp < gift.xpCost) {
            showInAppNotification(AppStrings.t("vr_insufficient_xp", appLanguage.value))
            return
        }
        val now = System.currentTimeMillis()
        val senderId = uid ?: me.id
        if (gift.xpCost > 0) {
            awardVibeXp(-gift.xpCost)
        }
        val event = VoiceRoomLiveEvent(
            id = "gift_$now",
            roomId = room.id,
            type = "gift",
            senderId = senderId,
            senderName = me.name,
            senderFlag = me.flagEmoji,
            senderVipLevel = me.vibeLevel,
            payloadId = gift.id,
            text = "${gift.emoji} ${gift.nameEn}",
            recipientId = recipient.id,
            recipientName = recipient.name,
            giftValueXp = gift.xpCost,
            timestampMs = now
        )
        handleNewRoomEventSideEffects(event)
        _roomEvents.update { (it + event).takeLast(60) }
        recomputeLeaderboard(_roomEvents.value)
        if (uid != null) {
            viewModelScope.launch {
                firestoreVoiceRoomRepo?.sendRoomEvent(event)
            }
        }
    }

    fun previewSoundEffect(effectId: String) {
        soundboardEngine.playEffect(effectId, volume = _soundEffectsVolume.value, bypassCooldown = true)
    }

    fun triggerSoundEffect(effectId: String) {
        val room = activeRoom.value ?: return
        if (!soundboardEngine.canTriggerNow(1200L)) {
            showInAppNotification(AppStrings.t("vr_sound_cooldown", appLanguage.value))
            return
        }
        val uid = Firebase.auth.currentUser?.uid
        val me = currentUser.value
        val now = System.currentTimeMillis()
        val senderId = uid ?: me.id
        val event = VoiceRoomLiveEvent(
            id = "snd_$now",
            roomId = room.id,
            type = "sound",
            senderId = senderId,
            senderName = me.name,
            senderFlag = me.flagEmoji,
            senderVipLevel = me.vibeLevel,
            payloadId = effectId,
            text = effectId,
            timestampMs = now
        )
        handleNewRoomEventSideEffects(event)
        if (uid != null) {
            viewModelScope.launch {
                firestoreVoiceRoomRepo?.sendRoomEvent(event)
            }
        }
    }

    fun stopSoundEffects() {
        soundboardEngine.stopAll()
    }

    fun toggleFavoriteSound(soundId: String) {
        val updated = if (soundId in _favoriteSoundIds.value) {
            _favoriteSoundIds.value - soundId
        } else {
            _favoriteSoundIds.value + soundId
        }
        _favoriteSoundIds.value = updated
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(favoriteSoundIds = updated)
        }
    }

    fun setSoundEffectsVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _soundEffectsVolume.value = clamped
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(soundEffectsVolume = clamped)
        }
    }

    fun setDefaultEntranceStyle(style: EntranceStyle) {
        _defaultEntranceStyle.value = style
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(defaultEntranceStyle = style.id)
        }
    }

    fun setEnableGiftEffects(enabled: Boolean) {
        _enableGiftEffects.value = enabled
        if (!enabled) {
            pendingGiftQueue.clear()
            _currentGiftVisual.value = null
        }
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(enableGiftEffects = enabled)
        }
    }

    fun setEnableEntranceAnimations(enabled: Boolean) {
        _enableEntranceAnimations.value = enabled
        if (!enabled) {
            pendingEntranceQueue.clear()
            _currentEntranceVisual.value = null
        }
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(enableEntranceAnimations = enabled)
        }
    }

    fun setLowPowerVisualMode(enabled: Boolean) {
        _lowPowerVisualMode.value = enabled
        viewModelScope.launch {
            preferencesStore?.saveVoiceRoomPreferences(lowPowerVisualMode = enabled)
        }
    }

    fun createRoomPoll(question: String, options: List<String>) {
        val room = activeRoom.value ?: return
        val repo = firestoreVoiceRoomRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val res = repo.createRoomPoll(room.id, question, options, currentUser.value.name)
            if (res.isFailure) {
                showInAppNotification(res.exceptionOrNull()?.localizedMessage ?: "Could not create poll.")
            }
        }
    }

    fun voteRoomPoll(pollId: String, optionIndex: Int) {
        val room = activeRoom.value ?: return
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.voteRoomPoll(room.id, pollId, optionIndex)
        }
    }

    fun closeRoomPoll(pollId: String) {
        val room = activeRoom.value ?: return
        viewModelScope.launch {
            firestoreVoiceRoomRepo?.closeRoomPoll(room.id, pollId)
        }
    }

    fun retryRoomAudioConnection() {
        val room = activeRoom.value ?: return
        val uid = Firebase.auth.currentUser?.uid ?: return
        webRtcVoiceManager?.clearError()
        val speakerNow = isSpeaker.value || room.host.id == uid
        val preservedSeat = when {
            room.host.id == uid -> 0
            mySeatIndex.value >= 0 -> mySeatIndex.value
            speakerNow -> 0
            else -> -1
        }
        startRoomWebRtcSync(
            roomId = room.id,
            myUid = uid,
            isSpeaker = speakerNow,
            isMuted = isMicMuted.value,
            seatIndex = preservedSeat
        )
    }

    private fun handleNewRoomEventSideEffects(event: VoiceRoomLiveEvent) {
        val dedupKey = "${event.type}_${event.senderId}_${event.payloadId}_${event.timestampMs}"
        if (!processedRoomEventKeys.add(dedupKey)) return
        when (event.type) {
            "gift" -> {
                val gift = VoiceGiftCatalog.findById(event.payloadId) ?: return
                if (_enableGiftEffects.value) {
                    enqueueGiftVisual(
                        ActiveGiftVisual(
                            eventId = dedupKey,
                            gift = gift,
                            senderName = event.senderName,
                            senderFlag = event.senderFlag,
                            recipientName = event.recipientName ?: "Stage"
                        )
                    )
                }
                if (_soundEffectsVolume.value > 0.05f) {
                    soundboardEngine.playEffect(gift.soundEffectId, _soundEffectsVolume.value * 0.7f, bypassCooldown = true)
                }
            }
            "entrance" -> {
                val style = EntranceStyle.fromId(event.payloadId)
                if (_enableEntranceAnimations.value) {
                    enqueueEntranceVisual(
                        ActiveEntranceVisual(
                            eventId = dedupKey,
                            userName = event.senderName,
                            userFlag = event.senderFlag,
                            userLevel = event.senderVipLevel,
                            style = style
                        )
                    )
                }
                if (_soundEffectsVolume.value > 0.05f && style.isVip) {
                    soundboardEngine.playEffect(style.soundEffectId, _soundEffectsVolume.value * 0.55f, bypassCooldown = true)
                }
            }
            "sound" -> {
                soundboardEngine.playEffect(event.payloadId, _soundEffectsVolume.value, bypassCooldown = true)
            }
            "reaction" -> {
                if (event.senderId != Firebase.auth.currentUser?.uid && event.payloadId.isNotBlank()) {
                    repository.sendRoomReaction(event.payloadId)
                }
            }
        }
    }

    private fun enqueueGiftVisual(visual: ActiveGiftVisual) {
        pendingGiftQueue.removeAll { it.eventId == visual.eventId }
        pendingGiftQueue.addLast(visual)
        val sorted = pendingGiftQueue.sortedByDescending { it.gift.priority }
        pendingGiftQueue.clear()
        sorted.forEach { pendingGiftQueue.addLast(it) }

        if (giftQueueRunnerJob?.isActive == true) return
        giftQueueRunnerJob = viewModelScope.launch {
            while (pendingGiftQueue.isNotEmpty()) {
                val next = pendingGiftQueue.removeFirst()
                _currentGiftVisual.value = next
                val durationMs = if (next.gift.isFullScreen) 3800L else 2400L
                delay(durationMs)
            }
            _currentGiftVisual.value = null
        }
    }

    private fun enqueueEntranceVisual(visual: ActiveEntranceVisual) {
        pendingEntranceQueue.addLast(visual)
        if (entranceQueueRunnerJob?.isActive == true) return
        entranceQueueRunnerJob = viewModelScope.launch {
            while (pendingEntranceQueue.isNotEmpty()) {
                val next = pendingEntranceQueue.removeFirst()
                _currentEntranceVisual.value = next
                delay(if (next.style.isVip) 3000L else 1900L)
            }
            _currentEntranceVisual.value = null
        }
    }

    private fun recomputeLeaderboard(events: List<VoiceRoomLiveEvent>) {
        val ranked = events
            .filter { it.type == "gift" }
            .groupBy { it.senderId }
            .map { (uid, list) ->
                val sample = list.last()
                SupporterRankEntry(
                    userId = uid,
                    userName = sample.senderName,
                    flagEmoji = sample.senderFlag,
                    totalXpGifted = list.sumOf { it.giftValueXp.coerceAtLeast(1) },
                    giftsCount = list.size
                )
            }
            .sortedWith(compareByDescending<SupporterRankEntry> { it.totalXpGifted }.thenByDescending { it.giftsCount })
            .take(10)
        _supporterLeaderboard.value = ranked
    }

    fun createRoom(
        title: String,
        description: String,
        category: RoomCategory,
        tags: List<String>,
        isPrivate: Boolean = false,
        backgroundTheme: String = RoomBackgroundTheme.AURORA.id,
        isStageLocked: Boolean = false
    ) {
        if (_isCreatingRoom.value) return
        val cleanTitle = title.trim()
        if (cleanTitle.isEmpty()) return

        val host = currentUser.value
        val repo = firestoreVoiceRoomRepo
        val uid = Firebase.auth.currentUser?.uid

        if (repo == null || uid == null) {
            val hasMic = refreshMicPermissionStatus()
            pendingUnmuteAfterPermission = !hasMic
            _micPermissionDeniedInRoom.value = false
            repository.createVoiceRoom(
                title = cleanTitle,
                description = description.trim(),
                category = category,
                tags = tags,
                isPrivate = isPrivate,
                backgroundTheme = backgroundTheme,
                isStageLocked = isStageLocked,
                startMuted = !hasMic
            )
            _showCreateRoomDialog.value = false
            _showActiveRoomModal.value = true
            _roomAssistantOutput.value = null
            return
        }

        _isCreatingRoom.value = true
        viewModelScope.launch {
            try {
                // Close any previous live rooms hosted by this user so duplicate phantom rooms never linger.
                repo.expireStaleHostRooms(uid, expireAllLive = true)

                val firestoreCreated = repo.createVoiceRoom(
                    title = cleanTitle,
                    description = description.trim(),
                    category = category,
                    hostUser = host,
                    tags = tags,
                    isPrivate = isPrivate,
                    backgroundTheme = backgroundTheme,
                    isStageLocked = isStageLocked
                )

                webRtcVoiceManager?.clearError()
                webRtcVoiceManager?.setHostMuted(false)
                isInitiatedLocalLeave = false
                lastNotifiedHostMuted = false
                _roomEvents.value = emptyList()
                _roomPolls.value = emptyList()
                _supporterLeaderboard.value = emptyList()
                processedRoomEventKeys.clear()
                roomJoinedAtMs = System.currentTimeMillis()
                _micPermissionDeniedInRoom.value = false

                val currentMicGranted = refreshMicPermissionStatus()
                pendingUnmuteAfterPermission = !currentMicGranted
                repository.joinVoiceRoom(firestoreCreated)
                repository.setMySpeakerState(
                    speaker = true,
                    muted = !currentMicGranted,
                    handRaised = false,
                    seatIndex = 0
                )
                _showCreateRoomDialog.value = false
                _showActiveRoomModal.value = true
                _roomAssistantOutput.value = null
                awardVibeXp(15, "stage_host")
                startRoomWebRtcSync(
                    roomId = firestoreCreated.id,
                    myUid = uid,
                    isSpeaker = true,
                    isMuted = !currentMicGranted,
                    seatIndex = 0
                )
            } catch (e: Exception) {
                Log.w("VibeRoomViewModel", "Failed to create voice room in Firestore: ${e.message}")
                showInAppNotification(e.localizedMessage ?: "Could not create voice room.")
            } finally {
                _isCreatingRoom.value = false
            }
        }
    }

    private fun startRoomWebRtcSync(
        roomId: String,
        myUid: String,
        isSpeaker: Boolean,
        isMuted: Boolean,
        seatIndex: Int = if (isSpeaker) 0 else -1
    ) {
        val callRepo = firestoreCallRepo ?: return
        val voiceRepo = firestoreVoiceRoomRepo
        val rtc = webRtcVoiceManager ?: return
        val preferredRoute = rtc.preferredJoinRoute()
        _audioRoute.value = preferredRoute
        _isRoomSpeakerphone.value = (preferredRoute == AudioRouteMode.SPEAKER)
        val me = currentUser.value
        val hasMicNow = refreshMicPermissionStatus()
        val initialMuted = isMuted || !hasMicNow

        roomPeersJob?.cancel()
        roomSignalsJob?.cancel()
        activeRoomDocJob?.cancel()
        roomHeartbeatJob?.cancel()
        roomEventsJob?.cancel()
        roomPollsJob?.cancel()

        viewModelScope.launch {
            callRepo.upsertRoomPeer(
                roomId = roomId,
                displayName = me.name,
                isSpeaker = isSpeaker,
                isMuted = initialMuted,
                isHandRaised = false,
                seatIndex = seatIndex,
                flagEmoji = me.flagEmoji,
                avatarFrame = me.avatarFrame,
                avatarColorHex = me.avatarColorHex
            )
        }

        roomHeartbeatJob = viewModelScope.launch {
            while (true) {
                delay(20_000L)
                callRepo.heartbeatRoomPeer(roomId)
                val current = activeRoom.value
                if (voiceRepo != null && current != null && current.id == roomId && current.host.id == myUid) {
                    val activeCount = (current.speakers.size + current.listeners.size).coerceAtLeast(1)
                    voiceRepo.sendHostHeartbeat(
                        roomId = roomId,
                        activeListenersCount = activeCount,
                        activeSpeakersCount = current.speakers.size.coerceIn(1, 6)
                    )
                }
            }
        }

        if (voiceRepo != null) {
            activeRoomDocJob = viewModelScope.launch {
                voiceRepo.observeSingleVoiceRoom(roomId).collect { remoteRoom ->
                    val currentTitle = activeRoom.value?.title ?: remoteRoom?.title.orEmpty()
                    if (remoteRoom == null || !remoteRoom.isLive || !remoteRoom.lifecycleState.isDiscoverable()) {
                        val shouldShowPoliteDialog = !isInitiatedLocalLeave && activeRoom.value?.id == roomId
                        repository.leaveVoiceRoom()
                        _showActiveRoomModal.value = false
                        stopRoomWebRtcSync(roomId)
                        if (shouldShowPoliteDialog) {
                            _roomEndedNoticeTitle.value = currentTitle
                        }
                    } else {
                        repository.syncRemoteActiveRoom(remoteRoom)
                        val nowMs = System.currentTimeMillis()
                        if (myUid in remoteRoom.bannedIds) {
                            showInAppNotification(AppStrings.t("vr_banned_notice", appLanguage.value))
                            leaveVoiceRoom()
                        } else if (remoteRoom.isUserBanned(myUid, nowMs)) {
                            showInAppNotification(AppStrings.t("vr_temp_banned_notice", appLanguage.value))
                            leaveVoiceRoom()
                        }
                    }
                }
            }

            roomEventsJob = viewModelScope.launch {
                voiceRepo.observeRoomEvents(roomId).collect { events ->
                    _roomEvents.value = events
                    recomputeLeaderboard(events)
                    events.forEach { ev ->
                        if (ev.timestampMs >= roomJoinedAtMs - 2500L) {
                            handleNewRoomEventSideEffects(ev)
                        }
                    }
                }
            }

            roomPollsJob = viewModelScope.launch {
                voiceRepo.observeRoomPolls(roomId).collect { polls ->
                    _roomPolls.value = polls
                }
            }
        }

        roomPeersJob = viewModelScope.launch {
            callRepo.observeRoomPeers(roomId)
                .combine(rtc.connectedPeerIds) { peers, connectedIds -> peers to connectedIds }
                .collect { (peers, connectedIds) ->
                    val micGranted = refreshMicPermissionStatus()
                    val nowMs = System.currentTimeMillis()
                    val myPeer = peers.firstOrNull { (it.userId.ifBlank { it.id }) == myUid }
                    val hostMutedNow = myPeer?.hostMuted == true
                    rtc.setHostMuted(hostMutedNow)
                    repository.setHostMutedState(hostMutedNow)
                    if (hostMutedNow && !lastNotifiedHostMuted) {
                        showInAppNotification(AppStrings.t("vr_host_muted_banner", appLanguage.value))
                    } else if (!hostMutedNow && lastNotifiedHostMuted) {
                        showInAppNotification(AppStrings.t("vr_mic_allowed_notice", appLanguage.value))
                    }
                    lastNotifiedHostMuted = hostMutedNow

                    repository.syncActiveRoomPeers(peers, myUid, connectedIds)

                    // Never allow remote peer promotion/doc state to unmute this device without RECORD_AUDIO or while hostMuted.
                    val speakerNow = this@VibeRoomViewModel.isSpeaker.value
                    var mutedNow = isMicMuted.value
                    if (hostMutedNow && !mutedNow) {
                        mutedNow = true
                        repository.setMySpeakerState(
                            speaker = speakerNow,
                            muted = true,
                            handRaised = false,
                            seatIndex = mySeatIndex.value
                        )
                    } else if (speakerNow && !mutedNow && !micGranted) {
                        mutedNow = true
                        pendingUnmuteAfterPermission = true
                        _micPermissionDeniedInRoom.value = true
                        repository.setMySpeakerState(
                            speaker = true,
                            muted = true,
                            handRaised = isHandRaised.value,
                            seatIndex = mySeatIndex.value
                        )
                        viewModelScope.launch {
                            callRepo.upsertRoomPeer(
                                roomId = roomId,
                                displayName = me.name,
                                isSpeaker = true,
                                isMuted = true,
                                isHandRaised = isHandRaised.value,
                                seatIndex = mySeatIndex.value,
                                flagEmoji = me.flagEmoji,
                                avatarFrame = me.avatarFrame,
                                avatarColorHex = me.avatarColorHex
                            )
                        }
                    }

                    val canCapture = speakerNow && !mutedNow && !hostMutedNow && micGranted
                    rtc.setMicEnabled(canCapture)
                    val remoteIds = peers
                        .filter { peer ->
                            val pid = peer.userId.ifBlank { peer.id }
                            val alive = peer.lastHeartbeatMs == 0L || (nowMs - peer.lastHeartbeatMs) <= 75_000L
                            pid.isNotBlank() && pid != myUid && pid != "guest_user" && alive
                        }
                        .map { it.userId.ifBlank { it.id } }
                    rtc.syncRoomPeers(
                        myUid = myUid,
                        remotePeerIds = remoteIds,
                        micEnabled = canCapture,
                        onSignalToPeer = { toUid, type, sdp, mid, idx ->
                            viewModelScope.launch {
                                callRepo.sendRoomSignal(roomId, toUid, type, sdp, mid, idx)
                            }
                        }
                    )
                }
        }

        roomSignalsJob = viewModelScope.launch {
            callRepo.clearRoomSignalsForUser(roomId, myUid)
            callRepo.observeRoomSignalsForUser(roomId, myUid).collect { signals ->
                signals.forEach { sig ->
                    rtc.handleRoomSignal(
                        myUid = myUid,
                        signal = sig,
                        onAnswerToPeer = { toUid, sdp ->
                            viewModelScope.launch {
                                callRepo.sendRoomSignal(roomId, toUid, "answer", sdp)
                            }
                        },
                        onIceToPeer = { toUid, sdpMid, idx, candSdp ->
                            viewModelScope.launch {
                                callRepo.sendRoomSignal(roomId, toUid, "ice", candSdp, sdpMid, idx)
                            }
                        }
                    )
                    callRepo.deleteRoomSignal(roomId, sig.id)
                }
            }
        }
    }

    private fun stopRoomWebRtcSync(roomId: String?) {
        roomPeersJob?.cancel()
        roomSignalsJob?.cancel()
        activeRoomDocJob?.cancel()
        roomHeartbeatJob?.cancel()
        roomEventsJob?.cancel()
        roomPollsJob?.cancel()
        giftQueueRunnerJob?.cancel()
        entranceQueueRunnerJob?.cancel()
        webRtcVoiceManager?.leaveRoomAudio()
        soundboardEngine.stopAll()
        pendingGiftQueue.clear()
        pendingEntranceQueue.clear()
        _currentGiftVisual.value = null
        _currentEntranceVisual.value = null
        _micPermissionDeniedInRoom.value = false
        pendingUnmuteAfterPermission = false
        val uid = Firebase.auth.currentUser?.uid
        if (!roomId.isNullOrBlank()) {
            viewModelScope.launch {
                if (uid != null) {
                    firestoreCallRepo?.clearRoomSignalsForUser(roomId, uid)
                }
                firestoreCallRepo?.removeRoomPeer(roomId)
            }
        }
    }

    fun saveCroppedAvatar(avatarData: String) {
        if (avatarData.isBlank()) return
        repository.setCurrentUserAvatarAndFrame(avatarData = avatarData, clearAvatar = false)
        val repo = firestoreUserRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = repo.updateAvatarAndFrame(avatarData = avatarData)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_profile_saved", appLanguage.value))
            }
        }
    }

    fun updateAvatarFrame(frameId: String) {
        val sanitized = frameId.trim().ifBlank { "none" }
        repository.setCurrentUserAvatarAndFrame(avatarFrame = sanitized)
        val repo = firestoreUserRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = repo.updateAvatarAndFrame(avatarFrame = sanitized)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_profile_saved", appLanguage.value))
            }
        }
    }

    fun removeAvatar() {
        repository.setCurrentUserAvatarAndFrame(avatarData = null, clearAvatar = true)
        val repo = firestoreUserRepo ?: return
        if (Firebase.auth.currentUser == null) return
        viewModelScope.launch {
            val result = repo.updateAvatarAndFrame(avatarData = null, clearAvatar = true)
            if (result.isSuccess) {
                showInAppNotification(AppStrings.t("notif_profile_saved", appLanguage.value))
            }
        }
    }

    fun updateProfile(
        name: String,
        bio: String,
        country: String,
        flag: String,
        interests: List<String>,
        languages: List<String>,
        city: String = currentUser.value.cityName,
        username: String = currentUser.value.username,
        nativeLanguages: List<String> = currentUser.value.nativeLanguages,
        learningLanguages: List<String> = currentUser.value.learningLanguages,
        proficiencyLevel: String = currentUser.value.proficiencyLevel,
        learningLanguageLevels: Map<String, Int> = currentUser.value.learningLanguageLevels
    ) {
        val lockedCountry = currentUser.value.countryName.takeIf {
            it.isNotBlank() && !it.equals("Global", ignoreCase = true)
        } ?: country
        val lockedCode = currentUser.value.countryCode.takeIf {
            it.isNotBlank() && !it.equals("GL", ignoreCase = true)
        }
        val sanitizedHandle = username.trim().removePrefix("@").lowercase()
            .filter { it.isLetterOrDigit() || it == '_' }
            .take(30)
            .ifBlank { currentUser.value.username }

        val sanitizedLevels = learningLanguageLevels
            .filterKeys { it.isNotBlank() }
            .mapValues { it.value.coerceIn(1, 5) }
        val resolvedLearning = if (learningLanguages.isNotEmpty()) {
            learningLanguages
        } else {
            sanitizedLevels.keys.toList()
        }
        val resolvedProficiency = proficiencyLevel.ifBlank {
            VibeCatalogs.summarizeProficiency(sanitizedLevels, appLanguage.value)
        }

        repository.updateCurrentUser(
            name = name,
            bio = bio,
            countryName = lockedCountry,
            flagEmoji = flag,
            interests = interests,
            languagesSpoken = languages,
            countryCode = lockedCode,
            cityName = city,
            username = sanitizedHandle,
            nativeLanguages = nativeLanguages,
            learningLanguages = resolvedLearning,
            learningLanguageLevels = sanitizedLevels,
            proficiencyLevel = resolvedProficiency
        )
        _showEditProfileDialog.value = false

        viewModelScope.launch {
            try {
                val user = Firebase.auth.currentUser
                val repo = firestoreUserRepo
                if (user != null && repo != null) {
                    val cleanHandle = sanitizedHandle.takeIf { it.isNotBlank() && it != "member" }
                        ?: (name.filter { it.isLetterOrDigit() }.lowercase().take(20).ifBlank { "viber_${user.uid.take(6)}" })
                    val curr = currentUser.value
                    val profile = FirestoreUserProfile(
                        userId = user.uid,
                        username = cleanHandle,
                        displayName = name,
                        email = user.email,
                        country = lockedCountry.takeIf { !it.equals("Global", ignoreCase = true) },
                        countryCode = lockedCode,
                        city = city.takeIf { it.isNotBlank() },
                        bio = bio,
                        avatarUrl = curr.avatarUrl,
                        avatarData = curr.avatarData,
                        avatarFrame = curr.avatarFrame,
                        vibeXp = curr.vibeXp,
                        vibeLevel = curr.vibeLevel,
                        streakDays = curr.streakDays,
                        unlockedBadges = curr.unlockedBadges,
                        interests = interests,
                        languages = languages,
                        nativeLanguages = nativeLanguages,
                        learningLanguages = resolvedLearning,
                        learningLanguageLevels = sanitizedLevels,
                        proficiencyLevel = resolvedProficiency.takeIf { it.isNotBlank() },
                        communicationGoal = curr.communicationGoal.takeIf { it.isNotBlank() },
                        isDiscoveryVisible = curr.isDiscoveryVisible,
                        allowDirectMessages = curr.allowDirectMessages,
                        showOnlineStatus = curr.showOnlineStatus,
                        lastSeenVisibility = curr.lastSeenVisibility,
                        lastSeenAtMs = curr.lastSeenAtMs,
                        isOnline = curr.showOnlineStatus
                    )
                    repo.saveUserProfile(profile)
                    showInAppNotification(AppStrings.t("notif_profile_saved", appLanguage.value))
                }
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Failed to sync profile update to Firestore: ${e.message}")
            }
        }
    }

    fun updateBioAndInterests(bio: String, interests: List<String>) {
        val curr = currentUser.value
        updateProfile(
            name = curr.name,
            bio = bio,
            country = curr.countryName,
            flag = curr.flagEmoji,
            interests = interests,
            languages = curr.languagesSpoken
        )
    }

    fun toggleDarkMode() {
        val nextMode = if (themeMode.value == ThemeMode.DARK) ThemeMode.LIGHT else ThemeMode.DARK
        setThemeMode(nextMode)
    }

    // --- 24h Stories ---

    fun createStory(text: String, category: String = "Daily", backgroundHex: Long = 0xFF6A1B9A) {
        if (Firebase.auth.currentUser == null) return
        val learning = firestoreLearningRepo ?: return
        val me = currentUser.value
        viewModelScope.launch {
            val res = learning.createStory(
                text = text,
                authorName = me.name,
                authorFlag = me.flagEmoji,
                authorCountry = me.countryName,
                category = category,
                backgroundHex = backgroundHex
            )
            if (res.isSuccess) {
                showInAppNotification(AppStrings.t("notif_post_published", appLanguage.value))
            }
        }
    }

    fun markStoryViewed(storyId: String) {
        viewModelScope.launch {
            firestoreLearningRepo?.markStoryViewed(storyId)
        }
    }

    fun reactToStory(story: FirestoreStory, emoji: String) {
        viewModelScope.launch {
            firestoreLearningRepo?.reactToStory(story.id, story.reactions, emoji)
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            firestoreLearningRepo?.deleteStory(storyId)
        }
    }

    // --- AI Language Coach, Translator, Exercises & Flashcards (via Firebase.ai) ---

    fun warmupAiCoach() {
        viewModelScope.launch(Dispatchers.IO) {
            aiLanguageService.warmup()
        }
    }

    fun clearAiCoach() {
        aiCoachJob?.cancel()
        aiCoachJob = null
        activeCoachRequestSignature = null
        _aiCoachTurns.value = emptyList()
        _aiCoachState.value = AiTaskState.Idle
    }

    fun cancelAiCoach() {
        aiCoachJob?.cancel()
        aiCoachJob = null
        activeCoachRequestSignature = null
        _aiCoachTurns.update { current ->
            current
                .filterNot { it.isStreaming && it.text.isBlank() }
                .map { if (it.isStreaming) it.copy(isStreaming = false) else it }
        }
        _aiCoachState.value = AiTaskState.Idle
    }

    fun sendAiCoachPrompt(prompt: String, targetLanguage: String, mode: String) {
        val clean = prompt.trim()
        if (clean.isBlank()) return
        val signature = "${mode.lowercase()}|${targetLanguage.lowercase()}|$clean"
        if (aiCoachJob?.isActive == true && activeCoachRequestSignature == signature) {
            return
        }
        aiCoachJob?.cancel()
        activeCoachRequestSignature = signature

        val priorHistory = _aiCoachTurns.value.filter { !it.isStreaming && it.text.isNotBlank() }
        val nowMs = System.currentTimeMillis()
        val userTurn = AiCoachTurn(id = "u_${nowMs}_${UUID.randomUUID().toString().take(4)}", isUser = true, text = clean)
        val assistantTurnId = "a_${nowMs}_${UUID.randomUUID().toString().take(4)}"
        _aiCoachTurns.update { current ->
            current.map { if (it.isStreaming) it.copy(isStreaming = false) else it } + userTurn
        }
        _aiCoachState.value = AiTaskState.Loading

        aiCoachJob = viewModelScope.launch {
            try {
                var latestText = ""
                var lastUiFlushMs = 0L
                val uiFlushIntervalMs = 48L

                aiLanguageService.streamCoachResponse(
                    userPrompt = clean,
                    targetLanguage = targetLanguage,
                    proficiencyLevel = currentUser.value.proficiencyLevel.ifBlank { "Intermediate" },
                    mode = mode,
                    learnerUiLanguage = appLanguage.value.nativeName,
                    history = priorHistory
                ).collect { accumulated ->
                    latestText = accumulated
                    val now = System.currentTimeMillis()
                    if (lastUiFlushMs == 0L || now - lastUiFlushMs >= uiFlushIntervalMs) {
                        lastUiFlushMs = now
                        _aiCoachState.value = AiTaskState.Streaming(accumulated)
                        _aiCoachTurns.update { current ->
                            val existingIndex = current.indexOfFirst { it.id == assistantTurnId }
                            if (existingIndex >= 0) {
                                current.toMutableList().apply {
                                    set(existingIndex, get(existingIndex).copy(text = accumulated, isStreaming = true))
                                }
                            } else {
                                current + AiCoachTurn(
                                    id = assistantTurnId,
                                    isUser = false,
                                    text = accumulated,
                                    isStreaming = true
                                )
                            }
                        }
                    }
                }
                if (latestText.isNotEmpty()) {
                    _aiCoachTurns.update { current ->
                        val existingIndex = current.indexOfFirst { it.id == assistantTurnId }
                        if (existingIndex >= 0) {
                            current.toMutableList().apply {
                                set(existingIndex, get(existingIndex).copy(text = latestText, isStreaming = false))
                            }
                        } else {
                            current + AiCoachTurn(
                                id = assistantTurnId,
                                isUser = false,
                                text = latestText,
                                isStreaming = false
                            )
                        }
                    }
                }
                _aiCoachState.value = AiTaskState.Success(Unit)
                preferencesStore?.recordLearningActivity()
            } catch (e: TimeoutCancellationException) {
                Log.e("VibeRoomViewModel", "AI Coach timed out", e)
                _aiCoachTurns.update { current -> current.filterNot { it.id == assistantTurnId && it.text.isBlank() }.map { it.copy(isStreaming = false) } }
                _aiCoachState.value = AiTaskState.Error("Request timed out after 35 seconds. Please try again.")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "AI Coach failed", e)
                _aiCoachTurns.update { current -> current.filterNot { it.id == assistantTurnId && it.text.isBlank() }.map { it.copy(isStreaming = false) } }
                _aiCoachState.value = AiTaskState.Error(e.localizedMessage ?: "AI Coach failed to respond.")
            } finally {
                if (activeCoachRequestSignature == signature) {
                    activeCoachRequestSignature = null
                }
            }
        }
    }

    fun dismissInlineTranslation(inlineItemKey: String) {
        _inlineTranslations.update { it - inlineItemKey }
        _inlineTranslationErrors.update { it - inlineItemKey }
    }

    fun autoTranslateIncomingMessages(messages: List<ChatMessage>) {
        if (!_autoTranslateChat.value) return
        val targetLang = _chatTranslateLanguage.value ?: effectiveDefaultTranslateLanguage()
        val candidates = messages
            .filter {
                !it.isMine &&
                    !it.isDeleted &&
                    it.mediaType == "text" &&
                    it.text.isNotBlank()
            }
            .takeLast(4)
        candidates.forEach { msg ->
            val key = "msg_${msg.id}"
            val existing = _inlineTranslations.value[key]
            val alreadyMatchesTarget = existing != null &&
                existing.targetLanguage.equals(targetLang, ignoreCase = true) &&
                existing.sourceText == msg.text.trim()
            if (!alreadyMatchesTarget &&
                key !in _inlineTranslatingKeys.value &&
                key !in _inlineTranslationErrors.value
            ) {
                translateText(
                    text = msg.text,
                    targetLanguage = targetLang,
                    inlineItemKey = key,
                    toggleIfAlreadyMatched = false
                )
            }
        }
    }

    fun translateText(
        text: String,
        targetLanguage: String,
        sourceLanguageHint: String = "Auto-detect",
        inlineItemKey: String? = null,
        toggleIfAlreadyMatched: Boolean = true
    ) {
        val clean = text.trim()
        if (clean.isBlank()) return
        val resolvedTarget = targetLanguage.trim().ifBlank { effectiveDefaultTranslateLanguage() }

        if (inlineItemKey != null) {
            val existing = _inlineTranslations.value[inlineItemKey]
            if (toggleIfAlreadyMatched &&
                existing != null &&
                existing.targetLanguage.equals(resolvedTarget, ignoreCase = true) &&
                existing.sourceText == clean
            ) {
                dismissInlineTranslation(inlineItemKey)
                return
            }
            if (inlineItemKey in _inlineTranslatingKeys.value) return
        }

        // Fast synchronous cache lookup to eliminate unnecessary Loading spinner flashes on repeated queries.
        val cached = aiLanguageService.getCachedTranslation(clean, resolvedTarget, sourceLanguageHint)
        if (cached != null) {
            if (inlineItemKey != null) {
                _inlineTranslations.update { it + (inlineItemKey to cached) }
                _inlineTranslationErrors.update { it - inlineItemKey }
            } else {
                _translationState.value = AiTaskState.Success(cached)
            }
            return
        }

        if (inlineItemKey != null) {
            _inlineTranslationErrors.update { it - inlineItemKey }
            _inlineTranslatingKeys.update { it + inlineItemKey }
        } else {
            manualTranslationJob?.cancel()
            _translationState.value = AiTaskState.Loading
        }

        val job = viewModelScope.launch {
            try {
                val res = if (inlineItemKey != null) {
                    autoTranslateSemaphore.withPermit {
                        aiLanguageService.translate(clean, resolvedTarget, sourceLanguageHint)
                    }
                } else {
                    aiLanguageService.translate(clean, resolvedTarget, sourceLanguageHint)
                }
                res.onSuccess { translated ->
                    _translationHistory.update { (listOf(translated) + it).take(30) }
                    if (inlineItemKey != null) {
                        _inlineTranslations.update { it + (inlineItemKey to translated) }
                        _inlineTranslationErrors.update { it - inlineItemKey }
                    } else {
                        _translationState.value = AiTaskState.Success(translated)
                    }
                    preferencesStore?.recordLearningActivity()
                    awardVibeXp(10, "polyglot")
                }.onFailure { err ->
                    val errMsg = err.localizedMessage ?: AppStrings.t("translation_error_retry", appLanguage.value)
                    if (inlineItemKey != null) {
                        _inlineTranslationErrors.update { it + (inlineItemKey to errMsg) }
                    } else {
                        _translationState.value = AiTaskState.Error(errMsg)
                    }
                }
            } finally {
                if (inlineItemKey != null) {
                    _inlineTranslatingKeys.update { it - inlineItemKey }
                }
            }
        }
        if (inlineItemKey == null) {
            manualTranslationJob = job
        }
    }

    fun generatePracticeExercise(targetLanguage: String, topic: String = "Daily Conversation") {
        exerciseJob?.cancel()
        _exerciseState.value = AiTaskState.Loading
        exerciseJob = viewModelScope.launch {
            val res = aiLanguageService.generatePracticeExercise(
                targetLanguage = targetLanguage,
                proficiencyLevel = currentUser.value.proficiencyLevel.ifBlank { "Intermediate" },
                topic = topic,
                learnerUiLanguage = appLanguage.value.nativeName
            )
            res.onSuccess { ex ->
                _exerciseState.value = AiTaskState.Success(ex)
            }.onFailure { err ->
                _exerciseState.value = AiTaskState.Error(err.localizedMessage ?: "Could not generate exercise.")
            }
        }
    }

    fun recordExerciseCompleted() {
        viewModelScope.launch {
            try {
                preferencesStore?.recordLearningActivity()
                awardVibeXp(10, "polyglot")
            } catch (_: Exception) {
            }
        }
    }

    fun saveVocabCard(
        wordOrPhrase: String,
        translation: String,
        language: String,
        exampleSentence: String = ""
    ) {
        val learning = firestoreLearningRepo ?: return
        viewModelScope.launch {
            val res = learning.saveVocabItem(wordOrPhrase, translation, language, exampleSentence)
            if (res.isSuccess) {
                preferencesStore?.recordLearningActivity()
                awardVibeXp(10, "polyglot")
                showInAppNotification("Saved to Vocabulary Flashcards")
            }
        }
    }

    fun advanceVocabMastery(item: FirestoreVocabItem) {
        val next = (item.masteryLevel % 5) + 1
        viewModelScope.launch {
            firestoreLearningRepo?.updateVocabMastery(item.id, next)
            preferencesStore?.recordLearningActivity()
        }
    }

    fun deleteVocabCard(vocabId: String) {
        viewModelScope.launch {
            firestoreLearningRepo?.deleteVocabItem(vocabId)
        }
    }

    // --- Account Deletion ---

    fun deleteAccount(context: Context) {
        attachContext(context)
        val authUser = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            try {
                if (repository.activeRoom.value != null) {
                    leaveVoiceRoom(forceEndRoom = true)
                }
                val repo = firestoreUserRepo ?: FirestoreUserRepository(context)
                val deleteDataResult = repo.deleteOwnAccountData()
                if (deleteDataResult.isFailure) {
                    val err = deleteDataResult.exceptionOrNull()
                    if (err is CancellationException) throw err
                    Log.e("VibeRoomViewModel", "Firestore account data deletion failed: ${err?.message}", err)
                    showInAppNotification(err?.localizedMessage ?: "Unable to delete account data.")
                    return@launch
                }
                authUser.delete().await()
                stopFirestoreListeners()
                AuthService(context).clearCredentialAndPendingState()
                preferencesStore?.clearUserProgress()
                _isCurrentUserCountryLocked.value = false
                _activeChatConversationId.value = null
                _selectedUserProfile.value = null
                _showActiveRoomModal.value = false
                repository.signOut()
                showInAppNotification(AppStrings.t("done", appLanguage.value))
            } catch (e: FirebaseAuthRecentLoginRequiredException) {
                showInAppNotification(AppStrings.t("reauth_required_delete", appLanguage.value))
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("VibeRoomViewModel", "Account deletion failed: ${e.message}", e)
                showInAppNotification(e.localizedMessage ?: "Unable to delete account.")
            }
        }
    }
}
