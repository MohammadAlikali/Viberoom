package com.example.ui.screens.auth

import android.util.Patterns
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AuthService
import com.example.data.CountryData
import com.example.model.Country
import com.example.model.RegistrationForm
import com.example.ui.components.CategorizedInterestsPicker
import com.example.ui.components.GoogleSignInButton
import com.example.ui.components.InterestChip
import com.example.ui.components.LearningLanguagesRankPicker
import com.example.ui.components.SpokenLanguagesMenuPicker
import com.example.ui.localization.AppLanguage
import com.example.ui.localization.AppStrings
import com.example.ui.theme.VibeCyan
import com.example.ui.theme.VibePink
import com.example.ui.theme.VibePurplePrimary
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onContinue: () -> Unit
) {
    LaunchedEffect(Unit) {
        delay(650)
        onContinue()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .testTag("splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(VibePurplePrimary, VibeCyan))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VR",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "VIBE ROOM",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.t("splash_subtitle", appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = AppStrings.t("auto_signing_in", appLanguage),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onLanguageSelected: (AppLanguage) -> Unit = {},
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onGoogleSignInSuccess: ((FirebaseUser) -> Unit)? = null
) {
    var googleAuthError by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("welcome_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Language Selector Pills at Top
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AppLanguage.entries.forEach { lang ->
                val isSelected = appLanguage == lang
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLanguageSelected(lang) }
                        .testTag("welcome_lang_${lang.code}")
                ) {
                    Text(
                        text = lang.nativeName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Hero Logo & Title
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(VibePurplePrimary, VibeCyan))),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "VR", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = AppStrings.t("welcome_title", appLanguage),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = AppStrings.t("welcome_subtitle", appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Concise Feature Highlights
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            } else null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                WelcomeFeatureRow(
                    "🌍",
                    AppStrings.t("welcome_feat_countries_title", appLanguage),
                    AppStrings.t("welcome_feat_countries_desc", appLanguage)
                )
                WelcomeFeatureRow(
                    "🎙️",
                    AppStrings.t("welcome_feat_voice_title", appLanguage),
                    AppStrings.t("welcome_feat_voice_desc", appLanguage)
                )
                WelcomeFeatureRow(
                    "💬",
                    AppStrings.t("welcome_feat_chat_title", appLanguage),
                    AppStrings.t("welcome_feat_chat_desc", appLanguage)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (googleAuthError != null) {
                Text(
                    text = googleAuthError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            GoogleSignInButton(
                label = AppStrings.t("google_signin", appLanguage),
                testTag = "welcome_google_signin_button",
                onSuccess = { user ->
                    googleAuthError = null
                    onGoogleSignInSuccess?.invoke(user) ?: onSignIn()
                },
                onError = { err ->
                    googleAuthError = err
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
                Text(
                    text = AppStrings.t("or", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCreateAccount,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_create_account_button")
            ) {
                Text(
                    text = AppStrings.t("create_account_email", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onSignIn,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("welcome_signin_button")
            ) {
                Text(
                    text = AppStrings.t("already_have_account", appLanguage),
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = AppStrings.t("terms_footer", appLanguage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WelcomeFeatureRow(emoji: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = emoji, fontSize = 22.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun RegisterScreen(
    form: RegistrationForm,
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onFormChange: (RegistrationForm) -> Unit,
    onCountryClick: () -> Unit,
    onBack: () -> Unit,
    onEmailLinkSent: () -> Unit,
    onNavigateToLogin: (() -> Unit)? = null,
    onGoogleSignInSuccess: ((FirebaseUser) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember(context) { AuthService(context) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var googleAuthError by remember { mutableStateOf<String?>(null) }
    var isSending by remember { mutableStateOf(false) }

    val country = form.country ?: CountryData.defaultCountry

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("register_screen")
    ) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("register_back_button")) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = AppStrings.t("back", appLanguage)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = AppStrings.t("create_your_account", appLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = AppStrings.t("register_subtitle", appLanguage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(18.dp))

        if (googleAuthError != null) {
            Text(
                text = googleAuthError!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        GoogleSignInButton(
            label = AppStrings.t("google_signin", appLanguage),
            testTag = "register_google_signin_button",
            onSuccess = { user ->
                googleAuthError = null
                onGoogleSignInSuccess?.invoke(user)
            },
            onError = { err ->
                googleAuthError = err
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
            Text(
                text = AppStrings.t("or_email_register", appLanguage),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = form.firstName,
                onValueChange = { onFormChange(form.copy(firstName = it)) },
                label = { Text(AppStrings.t("first_name", appLanguage)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("register_first_name")
            )
            OutlinedTextField(
                value = form.lastName,
                onValueChange = { onFormChange(form.copy(lastName = it)) },
                label = { Text(AppStrings.t("last_name", appLanguage)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("register_last_name")
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = form.email,
            onValueChange = { onFormChange(form.copy(email = it)) },
            label = { Text(AppStrings.t("email_address", appLanguage)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_email")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Permanent Country Selector
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCountryClick() }
                .testTag("country_select_button")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = country.flagEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = AppStrings.t("country_region", appLanguage),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Filled.Lock,
                                contentDescription = "Permanent",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Text(text = country.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = AppStrings.t("country_lock_warning", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = country.dialCode,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(imageVector = Icons.Filled.KeyboardArrowDown, contentDescription = null)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = form.phone,
            onValueChange = { onFormChange(form.copy(phone = it)) },
            label = { Text(AppStrings.t("phone_optional", appLanguage)) },
            leadingIcon = {
                Text(
                    text = "${country.dialCode} ",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("register_phone")
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onFormChange(form.copy(termsAccepted = !form.termsAccepted)) }
        ) {
            Checkbox(
                checked = form.termsAccepted,
                onCheckedChange = { onFormChange(form.copy(termsAccepted = it)) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("terms_checkbox")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.t("terms_accept", appLanguage),
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val cleanEmail = form.email.trim()
                val validEmail = cleanEmail.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()
                when {
                    form.firstName.isBlank() || !validEmail -> {
                        errorMessage = AppStrings.t("err_enter_name_email", appLanguage)
                    }
                    !form.termsAccepted -> {
                        errorMessage = AppStrings.t("err_accept_terms", appLanguage)
                    }
                    else -> {
                        errorMessage = null
                        isSending = true
                        onFormChange(form.copy(country = country, hasExplicitCountry = true))
                        authService.savePendingEmailRegistration(
                            email = cleanEmail,
                            firstName = form.firstName,
                            lastName = form.lastName,
                            countryName = country.name,
                            countryCode = country.code
                        )
                        authService.sendSignInEmailLink(
                            email = cleanEmail,
                            scope = coroutineScope,
                            onSent = {
                                isSending = false
                                onEmailLinkSent()
                            },
                            onError = { rawErr ->
                                isSending = false
                                val lower = rawErr.lowercase()
                                errorMessage = if (lower.contains("operation-not-allowed") ||
                                    lower.contains("not enabled") ||
                                    lower.contains("unsupported")
                                ) {
                                    AppStrings.t("err_email_link_disabled", appLanguage)
                                } else {
                                    rawErr
                                }
                            }
                        )
                    }
                }
            },
            enabled = !isSending,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("register_continue_button")
        ) {
            if (isSending) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = AppStrings.t("send_email_link_btn", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = AppStrings.t("already_registered_signin", appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = AppStrings.t("sign_in", appLanguage),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable { onNavigateToLogin?.invoke() }
                    .padding(4.dp)
                    .testTag("register_goto_login_link")
            )
        }
    }
}

@Composable
fun OtpVerificationScreen(
    targetEmail: String,
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    prefilledLink: String = "",
    externalError: String? = null,
    onClearError: () -> Unit = {},
    onBack: () -> Unit,
    onVerifiedUser: (FirebaseUser) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authService = remember(context) { AuthService(context) }

    var emailInput by remember(targetEmail) {
        mutableStateOf(targetEmail.ifBlank { authService.getPendingEmail().orEmpty() })
    }
    var pastedLink by remember(prefilledLink) { mutableStateOf(prefilledLink) }
    var resendSeconds by remember { mutableIntStateOf(60) }
    var isVerifying by remember { mutableStateOf(false) }
    var errorMessage by remember(externalError) { mutableStateOf(externalError) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resendSeconds) {
        if (resendSeconds > 0) {
            delay(1000)
            resendSeconds--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
            .testTag("otp_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = AppStrings.t("back", appLanguage)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MarkEmailRead,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = AppStrings.t("email_verify_title", appLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = AppStrings.t("email_verify_desc", appLanguage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        if (emailInput.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = emailInput,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = AppStrings.t("email_verify_instructions", appLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(14.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (targetEmail.isBlank()) {
            OutlinedTextField(
                value = emailInput,
                onValueChange = { emailInput = it },
                label = { Text(AppStrings.t("email_address", appLanguage)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedTextField(
            value = pastedLink,
            onValueChange = {
                pastedLink = it
                errorMessage = null
                onClearError()
            },
            label = { Text(AppStrings.t("paste_email_link_label", appLanguage)) },
            placeholder = { Text("https://...") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("otp_input_field")
        )

        if (infoMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = infoMessage!!,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (resendSeconds > 0) {
            Text(
                text = "${AppStrings.t("resend_in", appLanguage)} ${resendSeconds}s",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            TextButton(
                onClick = {
                    val clean = emailInput.trim()
                    if (clean.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(clean).matches()) {
                        errorMessage = AppStrings.t("err_invalid_email", appLanguage)
                    } else {
                        authService.sendSignInEmailLink(
                            email = clean,
                            scope = coroutineScope,
                            onSent = {
                                resendSeconds = 60
                                errorMessage = null
                                infoMessage = AppStrings.t("email_link_sent_info", appLanguage)
                            },
                            onError = { err ->
                                errorMessage = err
                            }
                        )
                    }
                }
            ) {
                Text(
                    text = AppStrings.t("resend_email_link", appLanguage),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                val cleanEmail = emailInput.trim()
                val cleanLink = pastedLink.trim()
                if (cleanEmail.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
                    errorMessage = AppStrings.t("err_invalid_email", appLanguage)
                    return@Button
                }
                if (!authService.isSignInWithEmailLink(cleanLink)) {
                    errorMessage = AppStrings.t("email_verify_instructions", appLanguage)
                    return@Button
                }
                isVerifying = true
                errorMessage = null
                authService.completeSignInWithEmailLink(
                    email = cleanEmail,
                    emailLink = cleanLink,
                    scope = coroutineScope,
                    onSuccess = { user ->
                        isVerifying = false
                        onVerifiedUser(user)
                    },
                    onError = { err ->
                        isVerifying = false
                        errorMessage = err
                    }
                )
            },
            enabled = !isVerifying && pastedLink.isNotBlank() && emailInput.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("otp_verify_button")
        ) {
            if (isVerifying) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = AppStrings.t("verify_and_continue", appLanguage),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        GoogleSignInButton(
            label = AppStrings.t("google_signin", appLanguage),
            testTag = "otp_google_signin_button",
            onSuccess = { user ->
                onVerifiedUser(user)
            },
            onError = { err ->
                errorMessage = err
            }
        )
    }
}

@Composable
fun PersonalInfoScreen(
    initialName: String = "",
    initialUsername: String = "",
    selectedCountry: Country? = null,
    isCountryLocked: Boolean = false,
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onCountryClick: () -> Unit = {},
    onContinue: (displayName: String, username: String, bio: String) -> Unit
) {
    var displayName by remember(initialName) { mutableStateOf(initialName) }
    var username by remember(initialUsername) { mutableStateOf(initialUsername) }
    var bio by remember { mutableStateOf("") }

    val country = selectedCountry ?: CountryData.defaultCountry

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .testTag("personal_info_screen")
    ) {
        Text(
            text = AppStrings.t("setup_profile_title", appLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = AppStrings.t("setup_profile_subtitle", appLanguage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(22.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text(AppStrings.t("display_name", appLanguage)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_display_name_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { username = it.lowercase().replace(" ", "_") },
            label = { Text(AppStrings.t("unique_username", appLanguage)) },
            leadingIcon = {
                Text(
                    "@",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_username_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Country Selector (locked if already persisted in Firestore)
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (!isCountryLocked) Modifier.clickable { onCountryClick() } else Modifier
                )
                .testTag("personal_info_country_selector")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = country.flagEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = AppStrings.t("country_region", appLanguage),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = country.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isCountryLocked) {
                                AppStrings.t("country_locked_note", appLanguage)
                            } else {
                                AppStrings.t("country_lock_warning", appLanguage)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Icon(
                    imageVector = if (isCountryLocked) Icons.Filled.Lock else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = bio,
            onValueChange = { if (it.length <= 300) bio = it },
            label = { Text(AppStrings.t("short_bio", appLanguage)) },
            maxLines = 3,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile_bio_input")
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = AppStrings.t("privacy_18_notice", appLanguage),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(26.dp))

        Button(
            onClick = { onContinue(displayName.trim(), username.trim(), bio.trim()) },
            enabled = displayName.isNotBlank() && username.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("personal_info_continue")
        ) {
            Text(
                text = AppStrings.t("next_interests", appLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChooseInterestsScreen(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onContinue: (List<String>) -> Unit
) {
    val selected = remember { mutableStateListOf("Music", "Languages", "Culture") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(24.dp)
            .testTag("choose_interests_screen")
    ) {
        Text(
            text = AppStrings.t("select_interests_title", appLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "${AppStrings.t("select_interests_desc", appLanguage)} (${selected.size})",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            CategorizedInterestsPicker(
                selectedInterests = selected.toList(),
                onToggleInterest = { picked ->
                    val existing = selected.firstOrNull { it.equals(picked, ignoreCase = true) }
                    if (existing != null) selected.remove(existing) else selected.add(picked)
                },
                appLanguage = appLanguage,
                containerTag = "onboarding_interests_picker"
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = { onContinue(selected.toList()) },
            enabled = selected.size >= 3,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("interests_continue_button")
        ) {
            Text(
                text = "${AppStrings.t("next_languages", appLanguage)} (${selected.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun ChooseLanguagesScreen(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    onContinue: (languages: List<String>, learningLevels: Map<String, Int>) -> Unit
) {
    val initialSpoken = when (appLanguage) {
        AppLanguage.DARI_PERSIAN -> listOf("Dari / Persian")
        AppLanguage.PASHTO -> listOf("Pashto")
        AppLanguage.ENGLISH -> listOf("English")
    }
    val initialLearning = when (appLanguage) {
        AppLanguage.ENGLISH -> mapOf("Dari / Persian" to 1)
        AppLanguage.DARI_PERSIAN -> mapOf("English" to 2)
        AppLanguage.PASHTO -> mapOf("English" to 2)
    }

    val spokenSelected = remember { mutableStateListOf<String>().apply { addAll(initialSpoken) } }
    val learningLevels = remember { mutableStateMapOf<String, Int>().apply { putAll(initialLearning) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(24.dp)
            .testTag("choose_languages_screen")
    ) {
        Text(
            text = AppStrings.t("choose_languages_title", appLanguage),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = AppStrings.t("choose_languages_desc", appLanguage),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            SpokenLanguagesMenuPicker(
                title = AppStrings.t("spoken_languages_menu", appLanguage),
                selectedLanguages = spokenSelected.toList(),
                onToggleLanguage = { picked ->
                    val existing = spokenSelected.firstOrNull { it.equals(picked, ignoreCase = true) }
                    if (existing != null) spokenSelected.remove(existing) else spokenSelected.add(picked)
                },
                appLanguage = appLanguage,
                menuButtonTag = "onboarding_spoken_languages_menu"
            )

            LearningLanguagesRankPicker(
                learningLanguageLevels = learningLevels.toMap(),
                onSetLanguageRank = { lang, rank ->
                    val existingKey = learningLevels.keys.firstOrNull { it.equals(lang, ignoreCase = true) }
                    if (existingKey != null && existingKey != lang) learningLevels.remove(existingKey)
                    learningLevels[lang] = rank.coerceIn(1, 5)
                },
                onRemoveLanguage = { lang ->
                    val existingKey = learningLevels.keys.firstOrNull { it.equals(lang, ignoreCase = true) }
                    if (existingKey != null) learningLevels.remove(existingKey)
                },
                appLanguage = appLanguage,
                menuButtonTag = "onboarding_learning_languages_menu"
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        val combinedLanguages = (spokenSelected + learningLevels.keys).distinct()
        Button(
            onClick = { onContinue(combinedLanguages, learningLevels.toMap()) },
            enabled = combinedLanguages.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("languages_continue_button")
        ) {
            Text(
                text = AppStrings.t("next_finish", appLanguage),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun PermissionIntroScreen(
    appLanguage: AppLanguage = AppLanguage.DARI_PERSIAN,
    isSaving: Boolean = false,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars.union(WindowInsets.displayCutout))
            .padding(24.dp)
            .testTag("permission_intro_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "🎉", fontSize = 52.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = AppStrings.t("almost_ready", appLanguage),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = AppStrings.t("permissions_desc", appLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
                } else null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    PermissionExplanationItem(
                        icon = Icons.Filled.Lock,
                        title = AppStrings.t("country_region", appLanguage),
                        subtitle = AppStrings.t("country_locked_note", appLanguage)
                    )
                    PermissionExplanationItem(
                        icon = Icons.Filled.Mic,
                        title = AppStrings.t("nav_rooms", appLanguage),
                        subtitle = AppStrings.t("voice_rooms_subtitle", appLanguage)
                    )
                    PermissionExplanationItem(
                        icon = Icons.Filled.Notifications,
                        title = AppStrings.t("nav_messages", appLanguage),
                        subtitle = AppStrings.t("messages_subtitle", appLanguage)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFinish,
            enabled = !isSaving,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("finish_onboarding_button")
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(AppStrings.t("saving_profile", appLanguage), fontWeight = FontWeight.Bold)
            } else {
                Text(AppStrings.t("enter_viberoom", appLanguage), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun PermissionExplanationItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
