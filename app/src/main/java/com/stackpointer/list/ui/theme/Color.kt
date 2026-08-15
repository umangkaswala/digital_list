package com.stackpointer.list.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Seed #29A87C. Roles below are the ones DESIGN_TOKENS.md specifies explicitly; every other
// role keeps the Compose Material3 baseline default. That's fine in practice — minSdk 31
// means dynamic color (Theme.kt) is always available at runtime, so this scheme only renders
// in @Preview and on the rare device where dynamic color is unsupported.

val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006C4C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89F8C7),
    onPrimaryContainer = Color(0xFF002114),
    secondary = Color(0xFF4C6358),
    secondaryContainer = Color(0xFFCEE9D9),
    onSecondaryContainer = Color(0xFF092017),
    tertiary = Color(0xFF3D6373),
    tertiaryContainer = Color(0xFFC1E8FB),
    onTertiaryContainer = Color(0xFF001F29),
    error = Color(0xFFBA1A1A),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    surface = Color(0xFFF5FBF6),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFEFF5F0),
    surfaceContainer = Color(0xFFE9EFEA),
    surfaceContainerHigh = Color(0xFFE4EAE5),
    surfaceContainerHighest = Color(0xFFDEE4DF),
    onSurface = Color(0xFF171D19),
    onSurfaceVariant = Color(0xFF3F4942),
    outline = Color(0xFF6F7A73),
    outlineVariant = Color(0xFFBFC9C1),
)

val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF6CDBAC),
    onPrimary = Color(0xFF003824),
    primaryContainer = Color(0xFF005236),
    onPrimaryContainer = Color(0xFF89F8C7),
    surfaceContainer = Color(0xFF1B211D),
    onSurface = Color(0xFFDDE5DE),
    onSurfaceVariant = Color(0xFFBFC9C1),
    outline = Color(0xFF89938B),
)
