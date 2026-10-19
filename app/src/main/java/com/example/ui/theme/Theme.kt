package com.example.ui.theme

import android.app.Activity
import android.graphics.Color as AndroidColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AtmosphereColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DeepMidnight,
    primaryContainer = SlateNavy,
    onPrimaryContainer = Color.White,
    secondary = SolarAmber,
    onSecondary = DeepMidnight,
    tertiary = GlacialTeal,
    background = DeepMidnight,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = AndroidColor.TRANSPARENT
                window.navigationBarColor = AndroidColor.TRANSPARENT
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = AtmosphereColorScheme,
        typography = Typography,
        content = content
    )
}
