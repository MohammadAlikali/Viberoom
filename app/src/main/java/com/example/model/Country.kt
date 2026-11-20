package com.example.model

data class Country(
    val code: String,       // ISO 2-letter (e.g. "AF", "US", "GB")
    val name: String,       // e.g. "Afghanistan", "United States"
    val dialCode: String,   // e.g. "+93", "+1", "+44"
    val flagEmoji: String   // e.g. "🇦🇫", "🇺🇸"
)
