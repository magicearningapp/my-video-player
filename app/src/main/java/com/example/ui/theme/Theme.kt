package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = VideoCyanPrimary,
    onPrimary = VideoDarkBackground,
    primaryContainer = VideoCyanPrimaryContainer,
    onPrimaryContainer = VideoTextPrimary,
    secondary = VideoIndigoSecondary,
    onSecondary = VideoDarkBackground,
    background = VideoDarkBackground,
    onBackground = VideoTextPrimary,
    surface = VideoDarkSurface,
    onSurface = VideoTextPrimary,
    surfaceVariant = VideoDarkSurfaceElevated,
    onSurfaceVariant = VideoTextSecondary,
    outline = VideoDarkSurfaceBorder
)

private val LightColorScheme = lightColorScheme(
    primary = VideoCyanPrimaryContainer,
    onPrimary = VideoTextPrimary,
    background = VideoDarkBackground,
    surface = VideoDarkSurface,
    onSurface = VideoTextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
