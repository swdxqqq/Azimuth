package com.example.azimuth_mobile.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    background = LightBackground,
    surface = LightCard,
    primary = LightAccent,
    onPrimary = LightText,
    onBackground = LightText,
    onSurface = LightText,
    secondary = LightSecondary,
    outline = LightBorder
)

private val DarkColorScheme = darkColorScheme(
    background = DarkBackground,
    surface = DarkCard,
    primary = DarkAccent,
    onPrimary = DarkText,
    onBackground = DarkText,
    onSurface = DarkText,
    secondary = DarkSecondary,
    outline = DarkBorder
)

@Composable
fun AzimuthmobileTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
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
        typography = AzimuthTypography,
        content = content
    )
}