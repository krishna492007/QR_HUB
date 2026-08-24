package com.qr.hub.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.qr.hub.util.DarkPrimary
import com.qr.hub.util.DarkSecondary
import com.qr.hub.util.DarkSurface
import com.qr.hub.util.DarkTextPrimary
import com.qr.hub.util.DarkTextSecondary
import com.qr.hub.util.LightPrimary
import com.qr.hub.util.LightSecondary
import com.qr.hub.util.LightSurface
import com.qr.hub.util.LightTextPrimary
import com.qr.hub.util.LightTextSecondary

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    tertiary = DarkSecondary,
    background = DarkPrimary,
    surface = DarkSurface,
    onPrimary = DarkTextPrimary,
    onSecondary = DarkTextPrimary,
    onTertiary = DarkTextPrimary,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextPrimary,
    onError = DarkTextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = LightSecondary,
    background = LightPrimary,
    surface = LightSurface,
    onPrimary = LightTextPrimary,
    onSecondary = LightTextPrimary,
    onTertiary = LightTextPrimary,
    onBackground = LightTextPrimary,
    onSurface = LightTextPrimary,
    onError = LightTextPrimary
)

@Composable
fun QRHUBTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
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
        typography = Typography,
        content = content
    )
}
