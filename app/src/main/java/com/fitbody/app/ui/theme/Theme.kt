package com.fitbody.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 健康主题：绿色为主，避免大面积紫蓝
val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF2E7D32)
val Green200 = Color(0xFFA5D6A7)
val Orange500 = Color(0xFFFF9800)
val GreyBg = Color(0xFFF5F6F5)

private val LightColors = lightColorScheme(
    primary = Green700,
    onPrimary = Color.White,
    primaryContainer = Green200,
    secondary = Orange500,
    background = GreyBg,
    surface = Color.White
)

private val DarkColors = darkColorScheme(
    primary = Green200,
    secondary = Orange500
)

@Composable
fun FitBodyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else DarkColors,
        content = content
    )
}
