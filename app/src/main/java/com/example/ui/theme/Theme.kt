package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Premium cyberpunk dark theme default for FLZO Share
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) FlzoDarkColorScheme else FlzoLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
