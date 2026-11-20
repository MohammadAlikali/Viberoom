package com.example.model

enum class OnboardingStep {
    SPLASH,
    WELCOME,
    LOGIN,
    REGISTER,
    OTP_VERIFY,
    PERSONAL_INFO,
    CHOOSE_INTERESTS,
    CHOOSE_LANGUAGES,
    PERMISSION_INTRO,
    COMPLETED
}

data class RegistrationForm(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val country: Country? = null,
    val hasExplicitCountry: Boolean = false,
    val termsAccepted: Boolean = false
)
