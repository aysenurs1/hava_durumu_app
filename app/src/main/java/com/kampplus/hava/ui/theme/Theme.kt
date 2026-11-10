package com.kampplus.hava.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

val LightColors = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    background = LightBackground,
    surface = LightSurface,
    onBackground = LightOnBackground,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurface.copy(alpha = 0.88f),
    onSurfaceVariant = LightOnSurface.copy(alpha = 0.78f),
    outline = LightOnSurface.copy(alpha = 0.55f),
    primaryContainer = LightPrimary.copy(alpha = 0.25f),
    onPrimaryContainer = LightOnSurface
)

@Composable
fun HavaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}
