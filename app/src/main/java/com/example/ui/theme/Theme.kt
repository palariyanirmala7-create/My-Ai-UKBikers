package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SafetySaffronAmber,
    secondary = LakeTealSecondary,
    tertiary = ForestGreenPrimary,
    background = DarkSlateBg,
    surface = DarkSurface,
    onPrimary = DarkSlateBg,
    onSecondary = HighVisOnPrimary,
    onBackground = LightMintBg,
    onSurface = LightMintBg
)

private val LightColorScheme = lightColorScheme(
    primary = ForestGreenPrimary,
    secondary = LakeTealSecondary,
    tertiary = SafetySaffronAmber,
    background = LightMintBg,
    surface = LightSurface,
    onPrimary = HighVisOnPrimary,
    onSecondary = HighVisOnPrimary,
    onBackground = ForestGreenDark,
    onSurface = ForestGreenDark
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to enforce our gorgeous customized branding!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
