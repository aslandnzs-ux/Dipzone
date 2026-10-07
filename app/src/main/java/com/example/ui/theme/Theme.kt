package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DipzonDarkColorScheme = darkColorScheme(
    primary = DipzonPurplePrimary,
    onPrimary = DipzonTextPrimary,
    primaryContainer = DipzonPurpleDark,
    onPrimaryContainer = DipzonPurpleLight,
    secondary = DipzonPurpleGlow,
    onSecondary = DipzonTextPrimary,
    secondaryContainer = DipzonSurfaceVariant,
    onSecondaryContainer = DipzonPurpleLight,
    tertiary = DipzonAccentGold,
    onTertiary = DipzonBlack,
    background = DipzonBlack,
    onBackground = DipzonTextPrimary,
    surface = DipzonSurface,
    onSurface = DipzonTextPrimary,
    surfaceVariant = DipzonSurfaceVariant,
    onSurfaceVariant = DipzonTextSecondary,
    outline = DipzonBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Dipzon is a premium dark cinema platform
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DipzonDarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = DipzonBlack.toArgb()
                window.navigationBarColor = DipzonBlack.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
