package pt.ms.myshare.presentation.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.view.Window

private val LightColorScheme = lightColorScheme(
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

private val DarkColorScheme = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFFA5B4FC),
    onPrimary = androidx.compose.ui.graphics.Color(0xFF111827),
    primaryContainer = androidx.compose.ui.graphics.Color(0xFF3730A3),
    onPrimaryContainer = androidx.compose.ui.graphics.Color(0xFFE0E7FF),
    secondary = MyShareSecondary,
    onSecondary = MyShareOnSecondary,
    background = androidx.compose.ui.graphics.Color(0xFF0B1120),
    onBackground = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
    surface = androidx.compose.ui.graphics.Color(0xFF111827),
    onSurface = androidx.compose.ui.graphics.Color(0xFFF8FAFC),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF1F2937),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFD1D5DB),
    outline = androidx.compose.ui.graphics.Color(0xFF4B5563),
    error = androidx.compose.ui.graphics.Color(0xFFFCA5A5),
    onError = androidx.compose.ui.graphics.Color(0xFF7F1D1D)
)

@Composable
fun MyShareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            setStatusBarColor(window, colorScheme.background.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

@Suppress("DEPRECATION")
private fun setStatusBarColor(window: Window, color: Int) {
    window.statusBarColor = color
}
