package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0F2FE),
    onPrimaryContainer = Color(0xFF0369A1),
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightOutline,
    outlineVariant = Color(0xFFE2E8F0),
    secondary = Color(0xFF334155),
    onSecondary = Color.White
)

val DarkSlateColorScheme = darkColorScheme(
    primary = DarkSlatePrimary,
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF0C4A6E),
    onPrimaryContainer = Color(0xFFBAE6FD),
    background = DarkSlateBg,
    onBackground = DarkSlateTextPrimary,
    surface = DarkSlateSurface,
    onSurface = DarkSlateTextPrimary,
    surfaceVariant = DarkSlateSurfaceVariant,
    onSurfaceVariant = DarkSlateTextSecondary,
    outline = DarkSlateOutline,
    outlineVariant = Color(0xFF334155),
    secondary = Color(0xFF94A3B8),
    onSecondary = Color(0xFF0F172A)
)

val OledColorScheme = darkColorScheme(
    primary = OledPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF262626),
    onPrimaryContainer = Color(0xFFFDE68A),
    background = OledBg,
    onBackground = OledTextPrimary,
    surface = OledSurface,
    onSurface = OledTextPrimary,
    surfaceVariant = OledSurfaceVariant,
    onSurfaceVariant = OledTextSecondary,
    outline = OledOutline,
    outlineVariant = Color(0xFF1E1E1E),
    secondary = Color(0xFFA1A1AA),
    onSecondary = Color.Black
)

val SepiaColorScheme = lightColorScheme(
    primary = SepiaPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFDE68A),
    onPrimaryContainer = Color(0xFF78350F),
    background = SepiaBg,
    onBackground = SepiaTextPrimary,
    surface = SepiaSurface,
    onSurface = SepiaTextPrimary,
    surfaceVariant = SepiaSurfaceVariant,
    onSurfaceVariant = SepiaTextSecondary,
    outline = SepiaOutline,
    outlineVariant = Color(0xFFD5C7B0),
    secondary = Color(0xFF5C4E3D),
    onSecondary = Color.White
)

val SapphireColorScheme = darkColorScheme(
    primary = SapphirePrimary,
    onPrimary = Color(0xFF082F49),
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    background = SapphireBg,
    onBackground = SapphireTextPrimary,
    surface = SapphireSurface,
    onSurface = SapphireTextPrimary,
    surfaceVariant = SapphireSurfaceVariant,
    onSurfaceVariant = SapphireTextSecondary,
    outline = SapphireOutline,
    outlineVariant = Color(0xFF1E2D4F),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF030712)
)

val EmeraldColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF065F46),
    onPrimaryContainer = Color(0xFFA7F3D0),
    background = EmeraldBg,
    onBackground = EmeraldTextPrimary,
    surface = EmeraldSurface,
    onSurface = EmeraldTextPrimary,
    surfaceVariant = EmeraldSurfaceVariant,
    onSurfaceVariant = EmeraldTextSecondary,
    outline = EmeraldOutline,
    outlineVariant = Color(0xFF133F2E),
    secondary = Color(0xFF6EE7B7),
    onSecondary = Color(0xFF022C22)
)

val AmethystColorScheme = darkColorScheme(
    primary = AmethystPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF581C87),
    onPrimaryContainer = Color(0xFFF3E8FF),
    background = AmethystBg,
    onBackground = AmethystTextPrimary,
    surface = AmethystSurface,
    onSurface = AmethystTextPrimary,
    surfaceVariant = AmethystSurfaceVariant,
    onSurfaceVariant = AmethystTextSecondary,
    outline = AmethystOutline,
    outlineVariant = Color(0xFF332746),
    secondary = Color(0xFFBA68C8),
    onSecondary = Color.White
)

val CrimsonColorScheme = darkColorScheme(
    primary = CrimsonPrimary,
    onPrimary = Color(0xFF4C0519),
    primaryContainer = Color(0xFF881337),
    onPrimaryContainer = Color(0xFFFFE4E6),
    background = CrimsonBg,
    onBackground = CrimsonTextPrimary,
    surface = CrimsonSurface,
    onSurface = CrimsonTextPrimary,
    surfaceVariant = CrimsonSurfaceVariant,
    onSurfaceVariant = CrimsonTextSecondary,
    outline = CrimsonOutline,
    outlineVariant = Color(0xFF441824),
    secondary = Color(0xFFFDA4AF),
    onSecondary = Color(0xFF3A0B17)
)

@Composable
fun NovelDeskTheme(
    themeMode: String = "AMETHYST",
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val colorScheme = when (themeMode.uppercase()) {
        "LIGHT" -> LightColorScheme
        "DARK" -> AmethystColorScheme // Obsidian purple with neon pink accents matching screenshots
        "OLED" -> OledColorScheme
        "SEPIA" -> SepiaColorScheme
        "SAPPHIRE" -> SapphireColorScheme
        "EMERALD" -> EmeraldColorScheme
        "AMETHYST" -> AmethystColorScheme
        "CRIMSON" -> CrimsonColorScheme
        "DARK_SLATE" -> DarkSlateColorScheme
        else -> if (isSystemDark) AmethystColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
