package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// VibeRoom Semantic & Legacy Brand Tokens
val VibePurplePrimary = Color(0xFF7C4DFF)
val VibePurpleLight = Color(0xFFB388FF)
val VibePurpleDark = Color(0xFF5E35B1)
val VibePurpleVariant = Color(0xFF4527A0)

val VibeCyan = Color(0xFF00B8D4)
val VibeCyanDark = Color(0xFF00838F)

val VibePink = Color(0xFFE91E63)
val VibeOrange = Color(0xFFFF9100)
val VibeGreen = Color(0xFF10B981)

// Light Surfaces
val VibeLightBackground = Color(0xFFF8F9FC)
val VibeLightSurface = Color(0xFFFFFFFF)
val VibeLightSurfaceVariant = Color(0xFFEEF1F7)
val VibeLightSurfaceElevated = Color(0xFFE4E8F2)
val VibeLightOutline = Color(0xFFD0D7E6)

// Dark Surfaces
val VibeDarkBackground = Color(0xFF0E0918)
val VibeDarkSurface = Color(0xFF181227)
val VibeDarkSurfaceVariant = Color(0xFF251C3A)
val VibeDarkSurfaceElevated = Color(0xFF31254C)
val VibeDarkOutline = Color(0xFF443668)

// Gradients
val VibeHeroGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF7C4DFF), Color(0xFF5B36D6), Color(0xFF391C94))
)

val VibeCardGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF23153C), Color(0xFF160B28))
)

val VibeVoicePulseGradient = Brush.radialGradient(
    colors = listOf(Color(0x997C4DFF), Color(0x3300B8D4), Color(0x00000000))
)
