package com.example

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.AuthService
import com.example.model.OnboardingStep
import com.example.ui.NavigationTab
import com.example.ui.VibeRoomViewModel
import com.example.ui.components.FollowersFollowingBottomSheet
import com.example.ui.components.ProfileDetailBottomSheet
import com.example.ui.components.ReportUserDialog
import com.example.ui.localization.AppStrings
import com.example.ui.navigation.FloatingActiveVoiceBar
import com.example.ui.navigation.VibeRoomBottomBar
import com.example.ui.navigation.VibeRoomNavigationRail
import com.example.ui.navigation.VibeRoutes
import com.example.ui.screens.auth.ChooseInterestsScreen
import com.example.ui.screens.auth.ChooseLanguagesScreen
import com.example.ui.screens.auth.CountryPickerBottomSheet
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.OtpVerificationScreen
import com.example.ui.screens.auth.PermissionIntroScreen
import com.example.ui.screens.auth.PersonalInfoScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.SplashScreen
import com.example.ui.screens.auth.WelcomeScreen
import com.example.ui.screens.chat.ActiveCallOverlay
import com.example.ui.screens.chat.ChatDetailScreen
import com.example.ui.screens.chat.ChatListScreen
import com.example.ui.screens.discover.DiscoverScreen
import com.example.ui.screens.feed.FeedScreen
import com.example.ui.screens.learning.LanguageLearningHubModal
import com.example.ui.screens.notifications.NotificationsDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.voiceroom.ActiveVoiceRoomModal
import com.example.ui.screens.voiceroom.VoiceRoomsScreen
import com.example.ui.theme.VibeRoomTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private val viewModel: VibeRoomViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        handleAuthIntent(intent)
        setContent {
            val appTheme by viewModel.appTheme.collectAsState()
            val themeMode by viewModel.themeMode.collectAsState()
            val accentColorHex by viewModel.accentColorHex.collectAsState()
            val appLanguage by viewModel.appLanguage.collectAsState()

            VibeRoomTheme(
                appTheme = appTheme,
                themeMode = themeMode,
                accentColorHex = accentColorHex
            ) {
                CompositionLocalProvider(
                    LocalLayoutDirection provides appLanguage.layoutDirection
                ) {
                    VibeRoomRoot(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAuthIntent(intent)
    }

    private fun handleAuthIntent(intent: Intent?) {
        val link = intent?.data?.toString()
        if (!link.isNullOrBlank()) {
            viewModel.handleIncomingEmailLink(link, applicationContext)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.setOnlinePresence(true)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setOnlinePresence(false)
    }
}

@Composable
fun VibeRoomRoot(viewModel: VibeRoomViewModel) {
    val context = LocalContext.current
    LaunchedEffect(context) {
        viewModel.attachContext(context)
    }

    val onboardingStep by viewModel.onboardingStep.collectAsState()
    val registrationForm by viewModel.registrationForm.collectAsState()
    val showCountryPicker by viewModel.showCountryPicker.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val isCountryLocked by viewModel.isCurrentUserCountryLocked.collectAsState()
    val isSavingProfile by viewModel.isSavingProfile.collectAsState()
    val authFlowMessage by viewModel.authFlowMessage.collectAsState()
    val incomingEmailLink by viewModel.incomingEmailLink.collectAsState()
    val isHandlingEmailLink by viewModel.isHandlingEmailLink.collectAsState()
    val emailVerifyReturnStep by viewModel.emailVerifyReturnStep.collectAsState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        when (onboardingStep) {
            OnboardingStep.SPLASH -> {
                SplashScreen(
                    appLanguage = appLanguage,
                    onContinue = {
                        if (isHandlingEmailLink || incomingEmailLink != null) return@SplashScreen
                        val existingUser = Firebase.auth.currentUser
                        if (existingUser != null) {
                            viewModel.restoreExistingSession(existingUser, context)
                        } else {
                            val activity = context as? Activity
                            if (activity != null) {
                                AuthService(context).attemptAutoSignIn(
                                    activity = activity,
                                    scope = scope,
                                    onSuccess = { signedInUser ->
                                        viewModel.restoreExistingSession(signedInUser, context)
                                    },
                                    onNoSavedAccount = {
                                        viewModel.goToOnboardingStep(OnboardingStep.WELCOME)
                                    }
                                )
                            } else {
                                viewModel.goToOnboardingStep(OnboardingStep.WELCOME)
                            }
                        }
                    }
                )
            }
            OnboardingStep.WELCOME -> {
                WelcomeScreen(
                    appLanguage = appLanguage,
                    onLanguageSelected = viewModel::setAppLanguage,
                    onCreateAccount = { viewModel.goToOnboardingStep(OnboardingStep.REGISTER) },
                    onSignIn = { viewModel.goToOnboardingStep(OnboardingStep.LOGIN) },
                    onGoogleSignInSuccess = { user ->
                        viewModel.onGoogleSignInSuccess(user, context)
                    }
                )
            }
            OnboardingStep.LOGIN -> {
                BackHandler { viewModel.goToOnboardingStep(OnboardingStep.WELCOME) }
                LoginScreen(
                    appLanguage = appLanguage,
                    onNavigateToRegister = { viewModel.goToOnboardingStep(OnboardingStep.REGISTER) },
                    onBack = { viewModel.goToOnboardingStep(OnboardingStep.WELCOME) },
                    onEmailLinkSent = { sentEmail ->
                        viewModel.updateRegistrationForm(registrationForm.copy(email = sentEmail))
                        viewModel.setEmailVerifyReturnStep(OnboardingStep.LOGIN)
                        viewModel.goToOnboardingStep(OnboardingStep.OTP_VERIFY)
                    },
                    onAuthenticatedUser = { user ->
                        viewModel.handleAuthenticatedUser(user, context)
                    }
                )
            }
            OnboardingStep.REGISTER -> {
                BackHandler { viewModel.goToOnboardingStep(OnboardingStep.WELCOME) }
                RegisterScreen(
                    form = registrationForm,
                    appLanguage = appLanguage,
                    onFormChange = viewModel::updateRegistrationForm,
                    onCountryClick = viewModel::openCountryPicker,
                    onBack = { viewModel.goToOnboardingStep(OnboardingStep.WELCOME) },
                    onEmailLinkSent = {
                        viewModel.setEmailVerifyReturnStep(OnboardingStep.REGISTER)
                        viewModel.goToOnboardingStep(OnboardingStep.OTP_VERIFY)
                    },
                    onNavigateToLogin = { viewModel.goToOnboardingStep(OnboardingStep.LOGIN) },
                    onGoogleSignInSuccess = { user ->
                        viewModel.onGoogleSignInSuccess(user, context)
                    }
                )
            }
            OnboardingStep.OTP_VERIFY -> {
                BackHandler { viewModel.goToOnboardingStep(emailVerifyReturnStep) }
                OtpVerificationScreen(
                    targetEmail = registrationForm.email,
                    appLanguage = appLanguage,
                    prefilledLink = incomingEmailLink.orEmpty(),
                    externalError = authFlowMessage,
                    onClearError = viewModel::clearAuthFlowMessage,
                    onBack = { viewModel.goToOnboardingStep(emailVerifyReturnStep) },
                    onVerifiedUser = { user ->
                        viewModel.handleAuthenticatedUser(user, context)
                    }
                )
            }
            OnboardingStep.PERSONAL_INFO -> {
                val initialName = "${registrationForm.firstName} ${registrationForm.lastName}".trim()
                PersonalInfoScreen(
                    initialName = initialName,
                    initialUsername = initialName.filter { it.isLetterOrDigit() }.lowercase().take(18),
                    selectedCountry = registrationForm.country,
                    isCountryLocked = isCountryLocked,
                    appLanguage = appLanguage,
                    onCountryClick = viewModel::openCountryPicker,
                    onContinue = { name, username, bio ->
                        viewModel.savePersonalInfoDraft(name, username, bio)
                        viewModel.goToOnboardingStep(OnboardingStep.CHOOSE_INTERESTS)
                    }
                )
            }
            OnboardingStep.CHOOSE_INTERESTS -> {
                BackHandler { viewModel.goToOnboardingStep(OnboardingStep.PERSONAL_INFO) }
                ChooseInterestsScreen(
                    appLanguage = appLanguage,
                    onContinue = { interests ->
                        viewModel.saveInterestsDraft(interests)
                        viewModel.goToOnboardingStep(OnboardingStep.CHOOSE_LANGUAGES)
                    }
                )
            }
            OnboardingStep.CHOOSE_LANGUAGES -> {
                BackHandler { viewModel.goToOnboardingStep(OnboardingStep.CHOOSE_INTERESTS) }
                ChooseLanguagesScreen(
                    appLanguage = appLanguage,
                    onContinue = { languages, learningLevels ->
                        viewModel.saveLanguagesDraft(languages, learningLevels)
                        viewModel.goToOnboardingStep(OnboardingStep.PERMISSION_INTRO)
                    }
                )
            }
            OnboardingStep.PERMISSION_INTRO -> {
                BackHandler { viewModel.goToOnboardingStep(OnboardingStep.CHOOSE_LANGUAGES) }
                PermissionIntroScreen(
                    appLanguage = appLanguage,
                    isSaving = isSavingProfile,
                    onFinish = { viewModel.completeInitialProfileSetup(context) }
                )
            }
            OnboardingStep.COMPLETED -> {
                VibeRoomApp(viewModel = viewModel)
            }
        }

        // Global Country Picker Bottom Sheet during registration/initial setup (blocked once locked)
        if (showCountryPicker && !isCountryLocked) {
            CountryPickerBottomSheet(
                selectedCountry = registrationForm.country,
                appLanguage = appLanguage,
                onCountrySelected = viewModel::selectCountry,
                onDismiss = viewModel::closeCountryPicker
            )
        }
    }
}

@Composable
fun VibeRoomApp(
    viewModel: VibeRoomViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val activeChatId by viewModel.activeChatConversationId.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val activeRoom by viewModel.activeRoom.collectAsState()
    val showActiveRoomModal by viewModel.showActiveRoomModal.collectAsState()
    val isMicMuted by viewModel.isMicMuted.collectAsState()
    val appLanguage by viewModel.appLanguage.collectAsState()
    val showLearningHubModal by viewModel.showLearningHubModal.collectAsState()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val incomingCall by viewModel.incomingCall.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val isCallMuted by viewModel.isCallMuted.collectAsState()
    val isCallSpeakerOn by viewModel.isCallSpeakerOn.collectAsState()
    val callDurationSec by viewModel.callDurationSec.collectAsState()
    val isCallMediaConnected by viewModel.isCallMediaConnected.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val selectedUserProfile by viewModel.selectedUserProfile.collectAsState()
    val followListSheet by viewModel.followListSheet.collectAsState()
    val reportingUser by viewModel.reportingUser.collectAsState()
    val incomingRequests by viewModel.incomingFriendRequests.collectAsState()
    val outgoingRequests by viewModel.outgoingFriendRequests.collectAsState()
    val followingIds by viewModel.followingIds.collectAsState()
    val firestorePosts by viewModel.firestorePosts.collectAsState()
    val filteredUsers by viewModel.filteredUsers.collectAsState()

    val unreadCount = conversations.sumOf { it.unreadCount }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isInChatDetail = currentRoute == VibeRoutes.CHAT_DETAIL
    val displayedTab = VibeRoutes.toTab(currentRoute) ?: activeTab

    LaunchedEffect(activeTab) {
        val targetRoute = VibeRoutes.fromTab(activeTab)
        if (currentRoute != targetRoute && currentRoute != VibeRoutes.CHAT_DETAIL) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    LaunchedEffect(activeChatId) {
        if (activeChatId != null) {
            val chatRoute = VibeRoutes.chatDetail(activeChatId!!)
            if (currentRoute != VibeRoutes.CHAT_DETAIL) {
                navController.navigate(chatRoute) {
                    launchSingleTop = true
                }
            }
        } else if (currentRoute == VibeRoutes.CHAT_DETAIL) {
            navController.popBackStack()
        }
    }

    LaunchedEffect(currentRoute) {
        val mappedTab = VibeRoutes.toTab(currentRoute)
        if (mappedTab != null && mappedTab != activeTab) {
            viewModel.selectTab(mappedTab)
        }
        if (currentRoute != VibeRoutes.CHAT_DETAIL && activeChatId != null) {
            viewModel.closeChat()
        }
    }

    val navigateToTab: (NavigationTab) -> Unit = { tab ->
        viewModel.selectTab(tab)
        val targetRoute = VibeRoutes.fromTab(tab)
        if (currentRoute != targetRoute) {
            navController.navigate(targetRoute) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val useNavigationRail = maxWidth >= 600.dp

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail && !isInChatDetail) {
                VibeRoomNavigationRail(
                    currentTab = displayedTab,
                    unreadChatsCount = unreadCount,
                    currentLanguage = appLanguage,
                    onTabSelected = navigateToTab,
                    modifier = Modifier
                        .fillMaxHeight()
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
            }

            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("viberoom_main_scaffold"),
                contentWindowInsets = if (isInChatDetail) WindowInsets(0, 0, 0, 0) else WindowInsets.statusBars,
                bottomBar = {
                    if (!isInChatDetail) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            if (!showActiveRoomModal) {
                                FloatingActiveVoiceBar(
                                    room = activeRoom,
                                    isMuted = isMicMuted,
                                    currentLanguage = appLanguage,
                                    onExpand = viewModel::openActiveRoomModal,
                                    onToggleMic = viewModel::toggleMic,
                                    onLeave = viewModel::leaveVoiceRoom
                                )
                            }

                            if (!useNavigationRail) {
                                VibeRoomBottomBar(
                                    currentTab = displayedTab,
                                    unreadChatsCount = unreadCount,
                                    currentLanguage = appLanguage,
                                    onTabSelected = navigateToTab
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = VibeRoutes.VOICE_ROOMS,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    composable(VibeRoutes.VOICE_ROOMS) {
                        VoiceRoomsScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.FEED) {
                        FeedScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.CHATS) {
                        ChatListScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.DISCOVER) {
                        DiscoverScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.PROFILE) {
                        ProfileScreen(viewModel = viewModel)
                    }
                    composable(
                        route = VibeRoutes.CHAT_DETAIL,
                        arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                    ) { backStack ->
                        val convId = backStack.arguments?.getString("conversationId").orEmpty()
                        ChatDetailScreen(
                            conversationId = convId,
                            viewModel = viewModel,
                            onBack = {
                                viewModel.closeChat()
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }

        val inAppNotification by viewModel.inAppNotification.collectAsState()
        inAppNotification?.let { notif ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 20.dp, end = 20.dp)
                    .align(Alignment.TopCenter)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 8.dp,
                    modifier = Modifier.clickable { viewModel.dismissInAppNotification() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = notif,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        if (showActiveRoomModal && activeRoom != null) {
            ActiveVoiceRoomModal(
                room = activeRoom!!,
                viewModel = viewModel,
                onMinimize = viewModel::closeActiveRoomModal,
                onLeave = viewModel::leaveVoiceRoom
            )
        }

        if (showLearningHubModal) {
            LanguageLearningHubModal(
                viewModel = viewModel,
                onDismiss = viewModel::closeLearningHub
            )
        }

        if (showNotificationsDialog) {
            NotificationsDialog(
                notifications = notifications,
                appLanguage = appLanguage,
                onMarkRead = viewModel::markNotificationRead,
                onMarkAllRead = viewModel::markAllNotificationsRead,
                onDismiss = viewModel::closeNotifications
            )
        }

        if (incomingCall != null || activeCall != null) {
            ActiveCallOverlay(
                incomingCall = incomingCall,
                activeCall = activeCall,
                currentUid = Firebase.auth.currentUser?.uid ?: currentUser.id,
                isMuted = isCallMuted,
                isSpeakerOn = isCallSpeakerOn,
                isMediaConnected = isCallMediaConnected,
                durationSec = callDurationSec,
                appLanguage = appLanguage,
                onAcceptCall = viewModel::acceptIncomingCall,
                onDeclineCall = viewModel::declineIncomingCall,
                onEndCall = viewModel::endVoiceCall,
                onToggleMute = viewModel::toggleCallMute,
                onToggleSpeaker = viewModel::toggleCallSpeaker,
                onPermissionDenied = {
                    viewModel.showInAppNotification(AppStrings.t("mic_permission_needed", appLanguage))
                }
            )
        }

        if (selectedUserProfile != null) {
            val target = selectedUserProfile!!
            val resolvedTarget = remember(target, filteredUsers, followingIds, currentUser) {
                if (target.id == currentUser.id) {
                    currentUser
                } else {
                    val live = viewModel.repository.getUserById(target.id) ?: target
                    live.copy(isFollowing = target.id in followingIds)
                }
            }
            val userPosts = remember(firestorePosts, resolvedTarget.id) {
                firestorePosts.filter { it.authorId == resolvedTarget.id }
            }
            val incomingMatch = incomingRequests.firstOrNull { it.senderId == resolvedTarget.id }
            val outgoingMatch = outgoingRequests.firstOrNull { it.receiverId == resolvedTarget.id }
            val isFriends = incomingMatch?.status == "accepted" || outgoingMatch?.status == "accepted"
            val isPendingOutgoing = outgoingMatch?.status == "pending"
            val pendingIncomingReqId = incomingMatch?.takeIf { it.status == "pending" }?.id

            ProfileDetailBottomSheet(
                user = resolvedTarget,
                isOwnProfile = (resolvedTarget.id == currentUser.id),
                userPosts = userPosts,
                appLanguage = appLanguage,
                isFriends = isFriends,
                isFriendRequestSent = isPendingOutgoing,
                pendingIncomingRequestId = pendingIncomingReqId,
                onDismiss = viewModel::closeUserProfile,
                onEditOwnProfile = {
                    viewModel.closeUserProfile()
                    viewModel.selectTab(NavigationTab.PROFILE)
                    viewModel.openEditProfileDialog()
                },
                onFollowersClick = { viewModel.openFollowersSheet(resolvedTarget) },
                onFollowingClick = { viewModel.openFollowingSheet(resolvedTarget) },
                onStartChat = {
                    if (viewModel.startChatWithUser(resolvedTarget)) {
                        viewModel.closeUserProfile()
                    }
                },
                onToggleFollow = { viewModel.toggleFollowUser(resolvedTarget.id) },
                onSendFriendRequest = { viewModel.sendFriendRequest(resolvedTarget) },
                onAcceptFriendRequest = { reqId -> viewModel.acceptFriendRequest(reqId) },
                onBlock = { viewModel.blockUser(resolvedTarget.id) },
                onReport = { viewModel.openReportDialog(resolvedTarget) }
            )
        }

        if (followListSheet != null) {
            val sheetState = followListSheet!!
            val followUsers = remember(sheetState, filteredUsers, followingIds, currentUser) {
                viewModel.getUsersForFollowSheet(sheetState)
            }
            val title = if (sheetState.isFollowers) {
                "${sheetState.targetUser.name} • ${AppStrings.t("followers", appLanguage)}"
            } else {
                "${sheetState.targetUser.name} • ${AppStrings.t("following", appLanguage)}"
            }

            FollowersFollowingBottomSheet(
                title = title,
                users = followUsers,
                followingIds = followingIds,
                currentUserId = currentUser.id,
                appLanguage = appLanguage,
                onDismiss = viewModel::closeFollowListSheet,
                onUserClick = { clickedMember ->
                    viewModel.closeFollowListSheet()
                    viewModel.openUserProfile(clickedMember)
                },
                onToggleFollow = viewModel::toggleFollowUser
            )
        }

        if (reportingUser != null) {
            ReportUserDialog(
                user = reportingUser!!,
                appLanguage = appLanguage,
                onDismiss = viewModel::closeReportDialog,
                onSubmit = { reason, details ->
                    viewModel.submitReport(reason, details)
                }
            )
        }
    }
}
