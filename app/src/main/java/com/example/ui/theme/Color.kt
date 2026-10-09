package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF090C10)
val DarkSurface = Color(0xFF111622)
val DarkSurfaceElevated = Color(0xFF182030)
val DarkSurfaceHighlight = Color(0xFF222C42)

val ElectricCyan = Color(0xFF00E5FF)
val NeonBlue = Color(0xFF2979FF)
val CyberPurple = Color(0xFF7C4DFF)
val TurboGold = Color(0xFFFFB300)
val SuccessGreen = Color(0xFF00E676)
val ErrorRed = Color(0xFFFF5252)

val DarkTextPrimary = Color(0xFFF0F4F8)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextTertiary = Color(0xFF64748B)

val DarkBorder = Color(0xFF1E293B)

val FlzoDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFB8EAFF),
    secondary = NeonBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF0D47A1),
    onSecondaryContainer = Color(0xFFD1E4FF),
    tertiary = CyberPurple,
    onTertiary = Color.White,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    error = ErrorRed,
    onError = Color.White
)

val FlzoLightColorScheme = lightColorScheme(
    primary = Color(0xFF0288D1),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE1F5FE),
    secondary = Color(0xFF1565C0),
    background = Color(0xFFF8FAFC),
    surface = Color.White,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)
