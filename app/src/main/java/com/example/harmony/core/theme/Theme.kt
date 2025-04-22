package com.example.harmony.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Color.Primary,
    secondary = Color.Secondary,
    tertiary = Color.SecondaryVariant,
    background = Color.Background,
    surface = Color.Surface,
    error = Color.Error,
    onPrimary = Color.OnPrimary,
    onSecondary = Color.OnSecondary,
    onBackground = Color.OnBackground,
    onSurface = Color.OnSurface,
    onError = Color.OnError
)

private val DarkColorScheme = darkColorScheme(
    primary = Color.PrimaryDark,
    secondary = Color.SecondaryDark,
    tertiary = Color.SecondaryVariantDark,
    background = Color.BackgroundDark,
    surface = Color.SurfaceDark,
    error = Color.ErrorDark,
    onPrimary = Color.OnPrimaryDark,
    onSecondary = Color.OnSecondaryDark,
    onBackground = Color.OnBackgroundDark,
    onSurface = Color.OnSurfaceDark,
    onError = Color.OnErrorDark
)

@Composable
fun HarmonyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            view.setBackgroundColor(colorScheme.primary.toArgb())
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes(
            small = RoundedCornerShape(4.dp),
            medium = RoundedCornerShape(8.dp),
            large = RoundedCornerShape(12.dp)
        ),
        content = content
    )
}