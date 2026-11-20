package com.example.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark")
}

enum class AppTheme(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val defaultIsDark: Boolean,
    val swatchHex: Long
) {
    PURE_WHITE("pure_white", "Pure White", "🤍", false, 0xFF2563EB),
    SOFT_LIGHT("soft_light", "Soft Light", "🌤️", false, 0xFF5B4BDB),
    MIDNIGHT_BLACK("midnight_black", "Midnight Black", "🌑", true, 0xFF8C52FF),
    DEEP_NAVY("deep_navy", "Deep Navy", "🌌", true, 0xFF38BDF8),
    GRAPHITE_DARK("graphite_dark", "Graphite Dark", "🪨", true, 0xFFA78BFA),
    AMOLED_BLACK("amoled_black", "AMOLED Black", "🖤", true, 0xFF00E5FF),
    LAVENDER("lavender", "Lavender", "💜", true, 0xFF8B5CF6),
    OCEAN_BLUE("ocean_blue", "Ocean Blue", "🌊", true, 0xFF0284C7),
    EMERALD("emerald", "Emerald", "🌿", true, 0xFF10B981),
    ROSE("rose", "Rose", "🌹", true, 0xFFE11D48);

    companion object {
        fun fromStoredName(raw: String?): AppTheme {
            if (raw.isNullOrBlank()) return PURE_WHITE
            entries.firstOrNull { it.name == raw || it.id == raw }?.let { return it }
            return when (raw) {
                "NEON_VIBE" -> LAVENDER
                "SUNSET_GLOW" -> ROSE
                "OCEAN_MIDNIGHT" -> DEEP_NAVY
                "EMERALD_NIGHT" -> EMERALD
                else -> PURE_WHITE
            }
        }
    }
}

data class AccentPreset(
    val id: String,
    val hex: Long?,
    val labelKey: String
)

val AccentColorPresets = listOf(
    AccentPreset("default", null, "accent_default"),
    AccentPreset("violet", 0xFF7C3AED, "accent_violet"),
    AccentPreset("sky", 0xFF0284C7, "accent_sky"),
    AccentPreset("emerald", 0xFF059669, "accent_emerald"),
    AccentPreset("rose", 0xFFE11D48, "accent_rose"),
    AccentPreset("amber", 0xFFD97706, "accent_amber"),
    AccentPreset("indigo", 0xFF4F46E5, "accent_indigo"),
    AccentPreset("teal", 0xFF0D9488, "accent_teal")
)

fun buildSchemeForTheme(
    appTheme: AppTheme,
    isDark: Boolean,
    customAccentHex: Long? = null
): ColorScheme {
    val baseScheme = if (isDark) darkSchemeFor(appTheme) else lightSchemeFor(appTheme)
    if (customAccentHex == null) return baseScheme

    val accent = Color(customAccentHex)
    val onAccent = if (accent.luminance() > 0.45f) Color(0xFF0F172A) else Color.White
    val container = if (isDark) {
        lerp(baseScheme.surface, accent, 0.24f)
    } else {
        lerp(Color.White, accent, 0.14f)
    }
    val onContainer = if (isDark) {
        lerp(accent, Color.White, 0.72f)
    } else {
        lerp(accent, Color.Black, 0.52f)
    }
    return baseScheme.copy(
        primary = accent,
        onPrimary = onAccent,
        primaryContainer = container,
        onPrimaryContainer = onContainer
    )
}

private fun lightSchemeFor(theme: AppTheme): ColorScheme = when (theme) {
    AppTheme.PURE_WHITE -> lightColorScheme(
        primary = Color(0xFF2563EB),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDBEAFE),
        onPrimaryContainer = Color(0xFF1E3A8A),
        secondary = Color(0xFF0284C7),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF0C4A6E),
        tertiary = Color(0xFF059669),
        onTertiary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFCBD5E1),
        outlineVariant = Color(0xFFE2E8F0)
    )
    AppTheme.SOFT_LIGHT -> lightColorScheme(
        primary = Color(0xFF5B4BDB),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEAE7FF),
        onPrimaryContainer = Color(0xFF2E2287),
        secondary = Color(0xFF0EA5E9),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF075985),
        tertiary = Color(0xFFEC4899),
        onTertiary = Color.White,
        background = Color(0xFFF6F5FA),
        onBackground = Color(0xFF181826),
        surface = Color(0xFFFCFCFE),
        onSurface = Color(0xFF181826),
        surfaceVariant = Color(0xFFECEBF5),
        onSurfaceVariant = Color(0xFF52526B),
        outline = Color(0xFFD2D0E3),
        outlineVariant = Color(0xFFE4E2F0)
    )
    AppTheme.MIDNIGHT_BLACK -> lightColorScheme(
        primary = Color(0xFF6D28D9),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEDE9FE),
        onPrimaryContainer = Color(0xFF4C1D95),
        secondary = Color(0xFF0284C7),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF0C4A6E),
        tertiary = Color(0xFFDB2777),
        onTertiary = Color.White,
        background = Color(0xFFF7F7FB),
        onBackground = Color(0xFF111827),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF111827),
        surfaceVariant = Color(0xFFECECF4),
        onSurfaceVariant = Color(0xFF4B5563),
        outline = Color(0xFFD1D5DB),
        outlineVariant = Color(0xFFE5E7EB)
    )
    AppTheme.DEEP_NAVY -> lightColorScheme(
        primary = Color(0xFF1D4ED8),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFDBEAFE),
        onPrimaryContainer = Color(0xFF1E3A8A),
        secondary = Color(0xFF0284C7),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF075985),
        tertiary = Color(0xFF0D9488),
        onTertiary = Color.White,
        background = Color(0xFFF2F6FC),
        onBackground = Color(0xFF0F172A),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0F172A),
        surfaceVariant = Color(0xFFE4ECF7),
        onSurfaceVariant = Color(0xFF475569),
        outline = Color(0xFFC6D4E8),
        outlineVariant = Color(0xFFDCE5F2)
    )
    AppTheme.GRAPHITE_DARK -> lightColorScheme(
        primary = Color(0xFF5865F2),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE3E6FE),
        onPrimaryContainer = Color(0xFF283299),
        secondary = Color(0xFF0D9488),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCCFBF1),
        onSecondaryContainer = Color(0xFF115E59),
        tertiary = Color(0xFFE11D48),
        onTertiary = Color.White,
        background = Color(0xFFF3F4F6),
        onBackground = Color(0xFF18181B),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF18181B),
        surfaceVariant = Color(0xFFE5E7EB),
        onSurfaceVariant = Color(0xFF52525B),
        outline = Color(0xFFD4D4D8),
        outlineVariant = Color(0xFFE4E4E7)
    )
    AppTheme.AMOLED_BLACK -> lightColorScheme(
        primary = Color(0xFF0F172A),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE2E8F0),
        onPrimaryContainer = Color(0xFF0F172A),
        secondary = Color(0xFF0284C7),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFE0F2FE),
        onSecondaryContainer = Color(0xFF0C4A6E),
        tertiary = Color(0xFF7C3AED),
        onTertiary = Color.White,
        background = Color(0xFFFFFFFF),
        onBackground = Color(0xFF09090B),
        surface = Color(0xFFFAFAFA),
        onSurface = Color(0xFF09090B),
        surfaceVariant = Color(0xFFF4F4F5),
        onSurfaceVariant = Color(0xFF3F3F46),
        outline = Color(0xFFD4D4D8),
        outlineVariant = Color(0xFFE4E4E7)
    )
    AppTheme.LAVENDER -> lightColorScheme(
        primary = Color(0xFF7C3AED),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEDE9FE),
        onPrimaryContainer = Color(0xFF4C1D95),
        secondary = Color(0xFF9333EA),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF3E8FF),
        onSecondaryContainer = Color(0xFF581C87),
        tertiary = Color(0xFFDB2777),
        onTertiary = Color.White,
        background = Color(0xFFFAF7FF),
        onBackground = Color(0xFF1E1333),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1E1333),
        surfaceVariant = Color(0xFFF1EAFF),
        onSurfaceVariant = Color(0xFF5B4B7A),
        outline = Color(0xFFD9C9FA),
        outlineVariant = Color(0xFFEAE0FD)
    )
    AppTheme.OCEAN_BLUE -> lightColorScheme(
        primary = Color(0xFF0284C7),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFE0F2FE),
        onPrimaryContainer = Color(0xFF0C4A6E),
        secondary = Color(0xFF0891B2),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCFFAFE),
        onSecondaryContainer = Color(0xFF164E63),
        tertiary = Color(0xFF0D9488),
        onTertiary = Color.White,
        background = Color(0xFFF2FAFF),
        onBackground = Color(0xFF0B2239),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF0B2239),
        surfaceVariant = Color(0xFFE3F2FD),
        onSurfaceVariant = Color(0xFF3A5A78),
        outline = Color(0xFFBAD9F0),
        outlineVariant = Color(0xFFD7EBFA)
    )
    AppTheme.EMERALD -> lightColorScheme(
        primary = Color(0xFF059669),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFD1FAE5),
        onPrimaryContainer = Color(0xFF064E3B),
        secondary = Color(0xFF0D9488),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFCCFBF1),
        onSecondaryContainer = Color(0xFF134E4A),
        tertiary = Color(0xFF16A34A),
        onTertiary = Color.White,
        background = Color(0xFFF3FBF7),
        onBackground = Color(0xFF082419),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF082419),
        surfaceVariant = Color(0xFFE3F5EC),
        onSurfaceVariant = Color(0xFF33604D),
        outline = Color(0xFFB5DEC9),
        outlineVariant = Color(0xFFD5EFE1)
    )
    AppTheme.ROSE -> lightColorScheme(
        primary = Color(0xFFE11D48),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE4E6),
        onPrimaryContainer = Color(0xFF881337),
        secondary = Color(0xFFDB2777),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFCE7F3),
        onSecondaryContainer = Color(0xFF831843),
        tertiary = Color(0xFFF97316),
        onTertiary = Color.White,
        background = Color(0xFFFFF7F8),
        onBackground = Color(0xFF2B0F17),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF2B0F17),
        surfaceVariant = Color(0xFFFCECEF),
        onSurfaceVariant = Color(0xFF70424E),
        outline = Color(0xFFF2C4CD),
        outlineVariant = Color(0xFFF9DCE2)
    )
}

private fun darkSchemeFor(theme: AppTheme): ColorScheme = when (theme) {
    AppTheme.PURE_WHITE -> darkColorScheme(
        primary = Color(0xFF60A5FA),
        onPrimary = Color(0xFF0B1120),
        primaryContainer = Color(0xFF1E3A5F),
        onPrimaryContainer = Color(0xFFDBEAFE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF082F49),
        secondaryContainer = Color(0xFF14364B),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = Color(0xFF34D399),
        onTertiary = Color(0xFF022C22),
        background = Color(0xFF0F172A),
        onBackground = Color(0xFFF8FAFC),
        surface = Color(0xFF162033),
        onSurface = Color(0xFFF8FAFC),
        surfaceVariant = Color(0xFF22304A),
        onSurfaceVariant = Color(0xFFCBD5E1),
        outline = Color(0xFF334155),
        outlineVariant = Color(0xFF243247)
    )
    AppTheme.SOFT_LIGHT -> darkColorScheme(
        primary = Color(0xFFA5B4FC),
        onPrimary = Color(0xFF1E1B4B),
        primaryContainer = Color(0xFF2E2A63),
        onPrimaryContainer = Color(0xFFE0E7FF),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF082F49),
        secondaryContainer = Color(0xFF16354B),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = Color(0xFFF472B6),
        onTertiary = Color(0xFF500724),
        background = Color(0xFF141421),
        onBackground = Color(0xFFF3F4FA),
        surface = Color(0xFF1D1D2E),
        onSurface = Color(0xFFF3F4FA),
        surfaceVariant = Color(0xFF292940),
        onSurfaceVariant = Color(0xFFC6C6DC),
        outline = Color(0xFF3F3F5E),
        outlineVariant = Color(0xFF2E2E47)
    )
    AppTheme.MIDNIGHT_BLACK -> darkColorScheme(
        primary = Color(0xFF8C52FF),
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2D1E52),
        onPrimaryContainer = Color(0xFFDCCEFF),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF06253A),
        secondaryContainer = Color(0xFF123045),
        onSecondaryContainer = Color(0xFFBAE6FD),
        tertiary = Color(0xFFFF5286),
        onTertiary = Color.White,
        background = Color(0xFF090910),
        onBackground = Color(0xFFF4F4F9),
        surface = Color(0xFF13131E),
        onSurface = Color(0xFFF4F4F9),
        surfaceVariant = Color(0xFF1E1E2E),
        onSurfaceVariant = Color(0xFFBDBDD4),
        outline = Color(0xFF32324A),
        outlineVariant = Color(0xFF242438)
    )
    AppTheme.DEEP_NAVY -> darkColorScheme(
        primary = Color(0xFF38BDF8),
        onPrimary = Color(0xFF062033),
        primaryContainer = Color(0xFF173A54),
        onPrimaryContainer = Color(0xFFBAE6FD),
        secondary = Color(0xFF60A5FA),
        onSecondary = Color(0xFF0B1F3A),
        secondaryContainer = Color(0xFF19304D),
        onSecondaryContainer = Color(0xFFDBEAFE),
        tertiary = Color(0xFF2DD4BF),
        onTertiary = Color(0xFF042F2E),
        background = Color(0xFF0E1621),
        onBackground = Color(0xFFEAF2FA),
        surface = Color(0xFF17212B),
        onSurface = Color(0xFFEAF2FA),
        surfaceVariant = Color(0xFF202D3B),
        onSurfaceVariant = Color(0xFFA7BACD),
        outline = Color(0xFF2E4054),
        outlineVariant = Color(0xFF223142)
    )
    AppTheme.GRAPHITE_DARK -> darkColorScheme(
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF1E103D),
        primaryContainer = Color(0xFF312A4A),
        onPrimaryContainer = Color(0xFFE5DEFF),
        secondary = Color(0xFF34D399),
        onSecondary = Color(0xFF052E20),
        secondaryContainer = Color(0xFF1B3A31),
        onSecondaryContainer = Color(0xFFA7F3D0),
        tertiary = Color(0xFFF472B6),
        onTertiary = Color(0xFF420A26),
        background = Color(0xFF141518),
        onBackground = Color(0xFFF4F4F5),
        surface = Color(0xFF1E2025),
        onSurface = Color(0xFFF4F4F5),
        surfaceVariant = Color(0xFF2A2D34),
        onSurfaceVariant = Color(0xFFC2C6D0),
        outline = Color(0xFF3E424D),
        outlineVariant = Color(0xFF2E313A)
    )
    AppTheme.AMOLED_BLACK -> darkColorScheme(
        primary = Color(0xFF00E5FF),
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF062C33),
        onPrimaryContainer = Color(0xFFA5F3FC),
        secondary = Color(0xFF8C52FF),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF251646),
        onSecondaryContainer = Color(0xFFDDD6FE),
        tertiary = Color(0xFF10B981),
        onTertiary = Color.Black,
        background = Color(0xFF000000),
        onBackground = Color(0xFFFAFAFA),
        surface = Color(0xFF0B0C0F),
        onSurface = Color(0xFFFAFAFA),
        surfaceVariant = Color(0xFF16181E),
        onSurfaceVariant = Color(0xFFBEBFC8),
        outline = Color(0xFF292C36),
        outlineVariant = Color(0xFF1C1E26)
    )
    AppTheme.LAVENDER -> darkColorScheme(
        primary = Color(0xFFA78BFA),
        onPrimary = Color(0xFF1F0E47),
        primaryContainer = Color(0xFF34225C),
        onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFF38BDF8),
        onSecondary = Color(0xFF082F49),
        secondaryContainer = Color(0xFF16354A),
        onSecondaryContainer = Color(0xFFE0F2FE),
        tertiary = Color(0xFFF472B6),
        onTertiary = Color(0xFF4C0526),
        background = Color(0xFF10091D),
        onBackground = Color(0xFFF3EEFD),
        surface = Color(0xFF1A112C),
        onSurface = Color(0xFFF3EEFD),
        surfaceVariant = Color(0xFF271B3F),
        onSurfaceVariant = Color(0xFFD0C2EC),
        outline = Color(0xFF433269),
        outlineVariant = Color(0xFF30224D)
    )
    AppTheme.OCEAN_BLUE -> darkColorScheme(
        primary = Color(0xFF00B4D8),
        onPrimary = Color(0xFF04222B),
        primaryContainer = Color(0xFF083548),
        onPrimaryContainer = Color(0xFF90E0EF),
        secondary = Color(0xFF48CAE4),
        onSecondary = Color(0xFF042630),
        secondaryContainer = Color(0xFF08384D),
        onSecondaryContainer = Color(0xFFCAF0F8),
        tertiary = Color(0xFF06D6A0),
        onTertiary = Color(0xFF03291E),
        background = Color(0xFF05101B),
        onBackground = Color(0xFFE2F3F8),
        surface = Color(0xFF0D1D2E),
        onSurface = Color(0xFFE2F3F8),
        surfaceVariant = Color(0xFF182C42),
        onSurfaceVariant = Color(0xFFA7D4E6),
        outline = Color(0xFF2B496B),
        outlineVariant = Color(0xFF1B334D)
    )
    AppTheme.EMERALD -> darkColorScheme(
        primary = Color(0xFF10B981),
        onPrimary = Color(0xFF022C1E),
        primaryContainer = Color(0xFF0B3B2A),
        onPrimaryContainer = Color(0xFFA7F3D0),
        secondary = Color(0xFF34D399),
        onSecondary = Color(0xFF022C22),
        secondaryContainer = Color(0xFF103F32),
        onSecondaryContainer = Color(0xFFD1FAE5),
        tertiary = Color(0xFF2DD4BF),
        onTertiary = Color(0xFF042F2E),
        background = Color(0xFF06110C),
        onBackground = Color(0xFFE8F6EE),
        surface = Color(0xFF0E1F17),
        onSurface = Color(0xFFE8F6EE),
        surfaceVariant = Color(0xFF173024),
        onSurfaceVariant = Color(0xFFA3D9BE),
        outline = Color(0xFF26523D),
        outlineVariant = Color(0xFF193829)
    )
    AppTheme.ROSE -> darkColorScheme(
        primary = Color(0xFFFB7185),
        onPrimary = Color(0xFF420615),
        primaryContainer = Color(0xFF4D1323),
        onPrimaryContainer = Color(0xFFFFE4E6),
        secondary = Color(0xFFF472B6),
        onSecondary = Color(0xFF4A0627),
        secondaryContainer = Color(0xFF471632),
        onSecondaryContainer = Color(0xFFFCE7F3),
        tertiary = Color(0xFFFB923C),
        onTertiary = Color(0xFF431407),
        background = Color(0xFF14090D),
        onBackground = Color(0xFFFCEFF2),
        surface = Color(0xFF211016),
        onSurface = Color(0xFFFCEFF2),
        surfaceVariant = Color(0xFF311922),
        onSurfaceVariant = Color(0xFFEBC4CE),
        outline = Color(0xFF592D3C),
        outlineVariant = Color(0xFF3D1E29)
    )
}

@Composable
fun VibeRoomTheme(
    appTheme: AppTheme = AppTheme.PURE_WHITE,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColorHex: Long? = null,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val resolvedIsDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> systemDark
    }

    val targetScheme = buildSchemeForTheme(
        appTheme = appTheme,
        isDark = resolvedIsDark,
        customAccentHex = accentColorHex
    )

    val animSpec = tween<Color>(durationMillis = 240)
    val primary by animateColorAsState(targetScheme.primary, animSpec, label = "c_primary")
    val onPrimary by animateColorAsState(targetScheme.onPrimary, animSpec, label = "c_onPrimary")
    val primaryContainer by animateColorAsState(targetScheme.primaryContainer, animSpec, label = "c_primaryContainer")
    val onPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, animSpec, label = "c_onPrimaryContainer")
    val secondary by animateColorAsState(targetScheme.secondary, animSpec, label = "c_secondary")
    val onSecondary by animateColorAsState(targetScheme.onSecondary, animSpec, label = "c_onSecondary")
    val secondaryContainer by animateColorAsState(targetScheme.secondaryContainer, animSpec, label = "c_secondaryContainer")
    val onSecondaryContainer by animateColorAsState(targetScheme.onSecondaryContainer, animSpec, label = "c_onSecondaryContainer")
    val tertiary by animateColorAsState(targetScheme.tertiary, animSpec, label = "c_tertiary")
    val onTertiary by animateColorAsState(targetScheme.onTertiary, animSpec, label = "c_onTertiary")
    val background by animateColorAsState(targetScheme.background, animSpec, label = "c_background")
    val onBackground by animateColorAsState(targetScheme.onBackground, animSpec, label = "c_onBackground")
    val surface by animateColorAsState(targetScheme.surface, animSpec, label = "c_surface")
    val onSurface by animateColorAsState(targetScheme.onSurface, animSpec, label = "c_onSurface")
    val surfaceVariant by animateColorAsState(targetScheme.surfaceVariant, animSpec, label = "c_surfaceVariant")
    val onSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, animSpec, label = "c_onSurfaceVariant")
    val outline by animateColorAsState(targetScheme.outline, animSpec, label = "c_outline")
    val outlineVariant by animateColorAsState(targetScheme.outlineVariant, animSpec, label = "c_outlineVariant")

    val animatedScheme = targetScheme.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        tertiary = tertiary,
        onTertiary = onTertiary,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariant,
        outline = outline,
        outlineVariant = outlineVariant
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !resolvedIsDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !resolvedIsDark
            }
        }
    }

    MaterialTheme(
        colorScheme = animatedScheme,
        typography = VibeRoomTypography,
        content = content
    )
}
