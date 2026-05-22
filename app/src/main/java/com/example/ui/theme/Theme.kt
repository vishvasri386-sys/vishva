package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LovableColorScheme = darkColorScheme(
    primary = LovableNeonViolet,
    secondary = LovableNeonPink,
    tertiary = LovableNeonCyan,
    background = LovableDarkBackground,
    surface = LovableGlassCard,
    onPrimary = LovableTextPrimary,
    onSecondary = LovableTextPrimary,
    onBackground = LovableTextPrimary,
    onSurface = LovableTextPrimary
)

@Composable
fun MyApplicationTheme(
    isDarkPurple: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = LovableColorScheme.copy(
        background = if (isDarkPurple) LovableDarkBackground else LovableLightBackground
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
