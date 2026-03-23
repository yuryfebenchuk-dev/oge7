package com.example.bankingapp.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF0046FF),
    secondary = Color(0xFF00A19A),
    background = Color(0xFFF5F7FB),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8CA9FF),
    secondary = Color(0xFF6EE7DF),
)

@Composable
fun BankingAppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
