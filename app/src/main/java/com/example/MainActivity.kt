package com.example

import android.app.Activity
import android.content.Intent
import android.os.Build
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.example.ui.screens.profile.SettingsScreen
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        handleAuthIntent(intent)
        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val accentColorHex by viewModel.accentColorHex.collectAsStateWithLifecycle()
            val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()

            VibeRoomTheme(
                appTheme = appTheme,
                themeMode = themeMode,
                accentColorHex = accentColorHex,
                appLanguage = appLanguage
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

    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val registrationForm by viewModel.registrationForm.collectAsStateWithLifecycle()
    val showCountryPicker by viewModel.showCountryPicker.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val isCountryLocked by viewModel.isCurrentUserCountryLocked.collectAsStateWithLifecycle()
    val isSavingProfile by viewModel.isSavingProfile.collectAsStateWithLifecycle()
    val authFlowMessage by viewModel.authFlowMessage.collectAsStateWithLifecycle()
    val incomingEmailLink by viewModel.incomingEmailLink.collectAsStateWithLifecycle()
    val isHandlingEmailLink by viewModel.isHandlingEmailLink.collectAsStateWithLifecycle()
    val emailVerifyReturnStep by viewModel.emailVerifyReturnStep.collectAsStateWithLifecycle()
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
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val activeChatId by viewModel.activeChatConversationId.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val activeRoom by viewModel.activeRoom.collectAsStateWithLifecycle()
    val showActiveRoomModal by viewModel.showActiveRoomModal.collectAsStateWithLifecycle()
    val isMicMuted by viewModel.isMicMuted.collectAsStateWithLifecycle()
    val appLanguage by viewModel.appLanguage.collectAsStateWithLifecycle()
    val showLearningHubModal by viewModel.showLearningHubModal.collectAsStateWithLifecycle()
    val showNotificationsDialog by viewModel.showNotificationsDialog.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val incomingCall by viewModel.incomingCall.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val isCallMuted by viewModel.isCallMuted.collectAsStateWithLifecycle()
    val isCallSpeakerOn by viewModel.isCallSpeakerOn.collectAsStateWithLifecycle()
    val callDurationSec by viewModel.callDurationSec.collectAsStateWithLifecycle()
    val isCallMediaConnected by viewModel.isCallMediaConnected.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val selectedUserProfile by viewModel.selectedUserProfile.collectAsStateWithLifecycle()
    val followListSheet by viewModel.followListSheet.collectAsStateWithLifecycle()
    val reportingUser by viewModel.reportingUser.collectAsStateWithLifecycle()
    val incomingRequests by viewModel.incomingFriendRequests.collectAsStateWithLifecycle()
    val outgoingRequests by viewModel.outgoingFriendRequests.collectAsStateWithLifecycle()
    val followingIds by viewModel.followingIds.collectAsStateWithLifecycle()
    val firestorePosts by viewModel.firestorePosts.collectAsStateWithLifecycle()
    val filteredUsers by viewModel.filteredUsers.collectAsStateWithLifecycle()

    val unreadCount = conversations.sumOf { it.unreadCount }
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val isInChatDetail = currentRoute == VibeRoutes.CHAT_DETAIL
    val isInSettings = currentRoute == VibeRoutes.SETTINGS
    val displayedTab = when {
        isInChatDetail -> NavigationTab.CHATS
        isInSettings -> NavigationTab.PROFILE
        else -> VibeRoutes.toTab(currentRoute) ?: activeTab
    }

    LaunchedEffect(activeTab) {
        val routeNow = navController.currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
        val targetRoute = VibeRoutes.fromTab(activeTab)
        if (routeNow != targetRoute && routeNow != VibeRoutes.CHAT_DETAIL && routeNow != VibeRoutes.SETTINGS) {
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
        val routeNow = navController.currentBackStackEntry?.destination?.route ?: return@LaunchedEffect
        if (activeChatId != null) {
            val chatRoute = VibeRoutes.chatDetail(activeChatId!!)
            if (routeNow != VibeRoutes.CHAT_DETAIL) {
                navController.navigate(chatRoute) {
                    launchSingleTop = true
                }
            }
        } else if (routeNow == VibeRoutes.CHAT_DETAIL) {
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
        if (navController.currentBackStackEntry?.destination?.route != null && currentRoute != targetRoute) {
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
        val useTwoPaneChat = maxWidth >= 840.dp
        val isFullScreenSinglePaneChat = isInChatDetail && !useTwoPaneChat
        val isFullScreenSettings = isInSettings && !useNavigationRail
        val hideChrome = isFullScreenSinglePaneChat || isFullScreenSettings

        Row(modifier = Modifier.fillMaxSize()) {
            if (useNavigationRail && !hideChrome) {
                VibeRoomNavigationRail(
                    currentTab = displayedTab,
                    unreadChatsCount = unreadCount,
                    currentLanguage = appLanguage,
                    onTabSelected = navigateToTab,
                    modifier = Modifier.fillMaxHeight()
                )
            }

            val outerScaffoldInsets = when {
                hideChrome -> WindowInsets(0, 0, 0, 0)
                useNavigationRail -> WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.End + WindowInsetsSides.Bottom
                )
                else -> WindowInsets.safeDrawing.only(
                    WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                )
            }

            Scaffold(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .testTag("viberoom_main_scaffold"),
                contentWindowInsets = outerScaffoldInsets,
                bottomBar = {
                    if (!hideChrome) {
                        if (useNavigationRail) {
                            if (!showActiveRoomModal) {
                                FloatingActiveVoiceBar(
                                    room = activeRoom,
                                    isMuted = isMicMuted,
                                    currentLanguage = appLanguage,
                                    onExpand = viewModel::openActiveRoomModal,
                                    onToggleMic = viewModel::toggleMic,
                                    onLeave = viewModel::leaveVoiceRoom,
                                    barInsets = WindowInsets.safeDrawing.only(
                                        WindowInsetsSides.Bottom + WindowInsetsSides.End
                                    )
                                )
                            }
                        } else {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                if (!showActiveRoomModal) {
                                    FloatingActiveVoiceBar(
                                        room = activeRoom,
                                        isMuted = isMicMuted,
                                        currentLanguage = appLanguage,
                                        onExpand = viewModel::openActiveRoomModal,
                                        onToggleMic = viewModel::toggleMic,
                                        onLeave = viewModel::leaveVoiceRoom,
                                        barInsets = WindowInsets.safeDrawing.only(
                                            WindowInsetsSides.Horizontal
                                        )
                                    )
                                }

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
                val initialDestination = remember { VibeRoutes.fromTab(activeTab) }
                NavHost(
                    navController = navController,
                    startDestination = initialDestination,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .consumeWindowInsets(innerPadding)
                ) {
                    composable(VibeRoutes.VOICE_ROOMS) {
                        VoiceRoomsScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.FEED) {
                        FeedScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.CHATS) {
                        if (useTwoPaneChat) {
                            TwoPaneChatLayout(
                                viewModel = viewModel,
                                selectedConversationId = activeChatId,
                                appLanguage = appLanguage,
                                onBackFromDetail = { viewModel.closeChat() }
                            )
                        } else {
                            ChatListScreen(viewModel = viewModel)
                        }
                    }
                    composable(VibeRoutes.DISCOVER) {
                        DiscoverScreen(viewModel = viewModel)
                    }
                    composable(VibeRoutes.PROFILE) {
                        ProfileScreen(
                            viewModel = viewModel,
                            onOpenSettings = {
                                navController.navigate(VibeRoutes.SETTINGS) {
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                    composable(VibeRoutes.SETTINGS) {
                        SettingsScreen(
                            viewModel = viewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = VibeRoutes.CHAT_DETAIL,
                        arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
                    ) { backStack ->
                        val convId = backStack.arguments?.getString("conversationId").orEmpty()
                        if (useTwoPaneChat) {
                            TwoPaneChatLayout(
                                viewModel = viewModel,
                                selectedConversationId = activeChatId ?: convId,
                                appLanguage = appLanguage,
                                onBackFromDetail = {
                                    viewModel.closeChat()
                                    navController.popBackStack()
                                }
                            )
                        } else {
                            ChatDetailScreen(
                                conversationId = convId,
                                viewModel = viewModel,
                                showBackButton = true,
                                onBack = {
                                    viewModel.closeChat()
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }

        val inAppNotification by viewModel.inAppNotification.collectAsStateWithLifecycle()
        inAppNotification?.let { notif ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .displayCutoutPadding()
                    .padding(top = 8.dp, start = 20.dp, end = 20.dp)
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

        val roomEndedNoticeTitle by viewModel.roomEndedNoticeTitle.collectAsStateWithLifecycle()
        roomEndedNoticeTitle?.let { endedTitle ->
            val cleanTitle = endedTitle.trim().ifBlank { AppStrings.t("voice_rooms", appLanguage) }
            val bodyTemplate = AppStrings.t("vr_room_ended_polite_body", appLanguage)
            val bodyText = if (bodyTemplate.contains("%s")) {
                bodyTemplate.replace("%s", cleanTitle)
            } else {
                "$cleanTitle — $bodyTemplate"
            }
            AlertDialog(
                onDismissRequest = viewModel::dismissRoomEndedNotice,
                modifier = Modifier.testTag("room_ended_dialog"),
                title = {
                    Text(
                        text = AppStrings.t("vr_room_ended_title", appLanguage),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = bodyText)
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::dismissRoomEndedNotice,
                        modifier = Modifier.testTag("room_ended_ok_button")
                    ) {
                        Text(AppStrings.t("vr_ok_return", appLanguage))
                    }
                }
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

@Composable
private fun TwoPaneChatLayout(
    viewModel: VibeRoomViewModel,
    selectedConversationId: String?,
    appLanguage: com.example.ui.localization.AppLanguage,
    onBackFromDetail: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .testTag("two_pane_chat_layout")
    ) {
        ChatListScreen(
            viewModel = viewModel,
            selectedConversationId = selectedConversationId,
            modifier = Modifier
                .width(340.dp)
                .fillMaxHeight()
        )
        VerticalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (!selectedConversationId.isNullOrBlank()) {
                ChatDetailScreen(
                    conversationId = selectedConversationId,
                    viewModel = viewModel,
                    showBackButton = false,
                    onBack = onBackFromDetail
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier
                        .padding(32.dp)
                        .testTag("two_pane_chat_empty")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = AppStrings.t("select_chat_hint_title", appLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = AppStrings.t("select_chat_hint_desc", appLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
