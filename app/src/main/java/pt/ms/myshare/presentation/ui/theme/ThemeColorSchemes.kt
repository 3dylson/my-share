package pt.ms.myshare.presentation.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

internal val LightColorScheme = lightColorScheme(
    primary = MySharePrimary,
    onPrimary = MyShareOnPrimary,
    primaryContainer = MySharePrimaryContainer,
    onPrimaryContainer = MyShareOnPrimaryContainer,
    secondary = MyShareSecondary,
    onSecondary = MyShareOnSecondary,
    background = MyShareBackground,
    onBackground = MyShareOnBackground,
    surface = MyShareSurface,
    onSurface = MyShareOnSurface,
    surfaceVariant = MyShareSurfaceVariant,
    onSurfaceVariant = MyShareOnSurfaceVariant,
    outline = MyShareOutline,
    error = MyShareError,
    onError = MyShareOnPrimary
)

internal val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFA5B4FC),
    onPrimary = Color(0xFF111827),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = MyShareSecondary,
    onSecondary = MyShareOnSecondary,
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFD1D5DB),
    outline = Color(0xFF4B5563),
    error = Color(0xFFFCA5A5),
    onError = Color(0xFF7F1D1D)
)
