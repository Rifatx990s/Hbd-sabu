package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val RpgGlassColorScheme = lightColorScheme(
    primary = PinkHighlight,
    secondary = ActionBg,
    tertiary = GlassBorder,
    background = GradientEnd,
    surface = GlassSurface,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = TextMain,
    onBackground = TextMain,
    onSurface = TextMain,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, 
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RpgGlassColorScheme,
        typography = Typography,
        content = content
    )
}
