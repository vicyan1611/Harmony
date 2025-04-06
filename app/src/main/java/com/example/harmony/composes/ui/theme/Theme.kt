package com.example.harmony.composes.ui.theme

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Gray90,
    onPrimary = Color.White,
    secondary = Gray80,
    onSecondary = Color.White,
    tertiaryContainer = Gray70,
    onTertiaryContainer = Gray20,
    error = Red80,
    onError = Color.White,
    background = Color.Black,
    onBackground = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun HarmonyTheme(
    isLightMode: Boolean = !isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor -> {
//            val context = LocalContext.current
//            if (isLightMode) dynamicLightColorScheme(context) else dynamicDarkColorScheme(context)
//        }
//
//        isLightMode -> LightColorScheme
//        else -> DarkColorScheme
//    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}