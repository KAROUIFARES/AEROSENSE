package com.example.aerosense_androidapp_dashboard.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = AeroPrimary,
    onPrimary = DarkBackground,
    primaryContainer = AeroPrimaryVariant,
    secondary = AeroSecondary,
    onSecondary = DarkBackground,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCardBg,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    outline = DarkCardBorder
)

private val LightColorScheme = lightColorScheme(
    primary = AeroPrimaryVariant,
    onPrimary = LightSurface,
    primaryContainer = AeroPrimary,
    secondary = AeroSecondary,
    onSecondary = LightSurface,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightCardBg,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    outline = LightCardBorder
)

@Composable
fun AeroSense_AndroidAPP_DashboardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
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