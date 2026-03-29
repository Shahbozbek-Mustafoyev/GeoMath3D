package com.geomath3d.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Dark palette (primary UI) ──────────────────────────────────────────────
val BgDark        = Color(0xFF0D0F14)
val SurfaceDark   = Color(0xFF161A22)
val Surface2Dark  = Color(0xFF1E2330)
val BorderDark    = Color(0xFF2A3145)
val Accent        = Color(0xFF6C63FF)
val AccentGreen   = Color(0xFF00D4AA)
val AccentOrange  = Color(0xFFFF9F43)
val TextPrimary   = Color(0xFFF0F2F8)
val TextMuted     = Color(0xFF8892A4)

private val DarkColors = darkColorScheme(
    primary          = Accent,
    onPrimary        = Color.White,
    secondary        = AccentGreen,
    onSecondary      = Color.White,
    tertiary         = AccentOrange,
    background       = BgDark,
    surface          = SurfaceDark,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    surfaceVariant   = Surface2Dark,
    outline          = BorderDark,
)

private val LightColors = lightColorScheme(
    primary          = Accent,
    onPrimary        = Color.White,
    secondary        = Color(0xFF00A884),
    onSecondary      = Color.White,
    tertiary         = AccentOrange,
    background       = Color(0xFFF4F5F9),
    surface          = Color.White,
    onBackground     = Color(0xFF1A1D26),
    onSurface        = Color(0xFF1A1D26),
    surfaceVariant   = Color(0xFFEEF0F6),
    outline          = Color(0xFFD0D4E0),
)

@Composable
fun GeoMath3DTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}
