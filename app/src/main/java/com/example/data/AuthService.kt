package com.example.data

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class AuthService(private val context: Context) {
    private val credentialManager = CredentialManager.create(context)
    val auth: FirebaseAuth get() = Firebase.auth
    val currentUser: FirebaseUser? get() = auth.currentUser

    /**
     * Attempts silent/automatic sign-in using any Google account already authorized/saved on the device.
     * Falls back immediately to [onNoSavedAccount] if no saved credential is available or auto-select is dismissed.
     */
    fun attemptAutoSignIn(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onNoSavedAccount: () -> Unit
    ) {
        val existingUser = auth.currentUser
        if (existingUser != null) {
            onSuccess(existingUser)
            return
        }
        val serverClientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (_: Exception) {
            onNoSavedAccount()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = withTimeoutOrNull(2500L) {
                    credentialManager.getCredential(activity, request)
                }
                val credential = result?.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onNoSavedAccount()
                    }
                } else {
                    onNoSavedAccount()
                }
            } catch (e: NoCredentialException) {
                Log.d("AuthService", "No previously authorized device account; showing login options.")
                onNoSavedAccount()
            } catch (e: GetCredentialCancellationException) {
                Log.d("AuthService", "Auto sign-in dismissed; showing manual login options.")
                onNoSavedAccount()
            } catch (e: Exception) {
                Log.w("AuthService", "Auto sign-in skipped: ${e.message}")
                onNoSavedAccount()
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit = {}
    ) {
        val serverClientId = try {
            activity.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            onError("Google Sign-In configuration missing: default_web_client_id not found")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = serverClientId).build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(signInOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    val authResult = Firebase.auth.signInWithCredential(authCredential).await()
                    val user = authResult.user
                    if (user != null) {
                        onSuccess(user)
                    } else {
                        onError("Authentication succeeded but user profile was empty")
                    }
                } else {
                    onError("Unexpected credential type received")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthService", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
                onCancelled()
            } catch (e: NoCredentialException) {
                Log.w("AuthService", "No Google account found on device", e)
                onError("No Google credentials available on this device.")
            } catch (e: Exception) {
                Log.e("AuthService", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Google Sign-In failed")
            }
        }
    }

    fun signOut(
        scope: CoroutineScope,
        onComplete: () -> Unit = {}
    ) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("AuthService", "Failed to clear credential state: ${e.message}")
                } finally {
                    auth.signOut()
                    onComplete()
                }
            }
        }
    }

    private val prefs by lazy {
        context.applicationContext.getSharedPreferences("viberoom_auth_prefs", Context.MODE_PRIVATE)
    }

    fun savePendingEmailRegistration(
        email: String,
        firstName: String = "",
        lastName: String = "",
        countryName: String = "",
        countryCode: String = ""
    ) {
        prefs.edit()
            .putString("pending_email", email.trim())
            .putString("pending_first_name", firstName.trim())
            .putString("pending_last_name", lastName.trim())
            .putString("pending_country_name", countryName.trim())
            .putString("pending_country_code", countryCode.trim())
            .apply()
    }

    fun getPendingEmail(): String? = prefs.getString("pending_email", null)?.takeIf { it.isNotBlank() }

    fun getPendingCountryCode(): String? = prefs.getString("pending_country_code", null)?.takeIf { it.isNotBlank() }

    fun getPendingCountryName(): String? = prefs.getString("pending_country_name", null)?.takeIf { it.isNotBlank() }

    fun getPendingFullName(): String {
        val f = prefs.getString("pending_first_name", "").orEmpty().trim()
        val l = prefs.getString("pending_last_name", "").orEmpty().trim()
        return "$f $l".trim()
    }

    fun clearPendingRegistration() {
        prefs.edit().clear().apply()
    }

    suspend fun clearCredentialAndPendingState() {
        clearPendingRegistration()
        withContext(NonCancellable) {
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w("AuthService", "Failed to clear credential state: ${e.message}")
            }
        }
    }

    fun sendSignInEmailLink(
        email: String,
        scope: CoroutineScope,
        onSent: () -> Unit,
        onError: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        val packageName = context.packageName
        val projectId = try {
            context.getString(R.string.project_id)
        } catch (_: Exception) {
            "gen-lang-client-0418144928"
        }

        val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://$projectId.firebaseapp.com/__/auth/action")
            .setHandleCodeInApp(true)
            .setAndroidPackageName(packageName, true, null)
            .build()

        scope.launch {
            try {
                auth.sendSignInLinkToEmail(cleanEmail, actionCodeSettings).await()
                prefs.edit().putString("pending_email", cleanEmail).apply()
                onSent()
            } catch (e: com.google.firebase.auth.FirebaseAuthException) {
                Log.w("AuthService", "sendSignInLinkToEmail failed [${e.errorCode}]: ${e.message}", e)
                val msg = when (e.errorCode) {
                    "ERROR_OPERATION_NOT_ALLOWED" -> "operation-not-allowed: Enable Email link (passwordless sign-in) in Firebase Authentication."
                    else -> e.localizedMessage ?: "Email Link sign-in could not be sent."
                }
                onError(msg)
            } catch (e: Exception) {
                Log.w("AuthService", "sendSignInLinkToEmail failed: ${e.message}", e)
                val msg = e.localizedMessage ?: "Email Link sign-in could not be sent."
                onError(msg)
            }
        }
    }

    fun isSignInWithEmailLink(link: String?): Boolean {
        if (link.isNullOrBlank()) return false
        return try {
            auth.isSignInWithEmailLink(link)
        } catch (_: Exception) {
            false
        }
    }

    fun completeSignInWithEmailLink(
        email: String,
        emailLink: String,
        scope: CoroutineScope,
        onSuccess: (FirebaseUser) -> Unit,
        onError: (String) -> Unit
    ) {
        scope.launch {
            try {
                val result = auth.signInWithEmailLink(email.trim(), emailLink.trim()).await()
                val user = result.user
                if (user != null) {
                    onSuccess(user)
                } else {
                    onError("Verification succeeded, but account session is empty.")
                }
            } catch (e: Exception) {
                Log.e("AuthService", "signInWithEmailLink failed: ${e.message}", e)
                onError(e.localizedMessage ?: "Invalid or expired verification link.")
            }
        }
    }
}
