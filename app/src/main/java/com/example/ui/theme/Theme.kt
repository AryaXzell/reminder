package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = iOSBlue,
    secondary = iOSSilver,
    tertiary = iOSOrange,
    background = iOSLightBackground,
    surface = iOSCardBackground,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Ignore dark theme to match screenshots exactly or allow light-only theme for fidelity
    dynamicColor: Boolean = false, // Force disable dynamic material coloring
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
