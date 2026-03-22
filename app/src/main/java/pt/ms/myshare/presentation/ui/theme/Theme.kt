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
    primary = MySharePrimaryContainer,
    onPrimary = MyShareOnPrimaryContainer,
    secondary = MyShareSecondary,
    onSecondary = MyShareOnSecondary,
    background = androidx.compose.ui.graphics.Color(0xFF0E1418),
    onBackground = androidx.compose.ui.graphics.Color(0xFFE8F1F5),
    surface = androidx.compose.ui.graphics.Color(0xFF121A1F),
    onSurface = androidx.compose.ui.graphics.Color(0xFFE8F1F5),
    surfaceVariant = androidx.compose.ui.graphics.Color(0xFF243239),
    onSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFC1CAD0),
    outline = androidx.compose.ui.graphics.Color(0xFF8A9398),
    error = androidx.compose.ui.graphics.Color(0xFFF2B8B5),
    onError = androidx.compose.ui.graphics.Color(0xFF601410)
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
