package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.data.ConfiguracaoIdentidadeClube

val LocalClubIdentityConfig = staticCompositionLocalOf { ConfiguracaoIdentidadeClube() }

fun parseHexColorSafe(hex: String, fallback: Color): Color {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> {
                val rgb = clean.toLong(16)
                Color(0xFF000000L or rgb)
            }
            8 -> {
                val argb = clean.toLong(16)
                Color(argb)
            }
            else -> fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

private fun darkenColor(color: Color, factor: Float = 0.28f): Color {
    return Color(
        red = (color.red * factor).coerceIn(0f, 1f),
        green = (color.green * factor).coerceIn(0f, 1f),
        blue = (color.blue * factor).coerceIn(0f, 1f),
        alpha = 1f
    )
}

@Composable
fun MyApplicationTheme(
    clubIdentity: ConfiguracaoIdentidadeClube = ConfiguracaoIdentidadeClube(),
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val dynamicPrimary = parseHexColorSafe(clubIdentity.corPrimariaHex, IronPrimary)
    val dynamicSecondary = parseHexColorSafe(clubIdentity.corSecundariaHex, IronSecondary)
    val dynamicTertiary = parseHexColorSafe(clubIdentity.corDestaqueSosHex, IronTertiary)
    val dynamicBackground = parseHexColorSafe(clubIdentity.corFundoHex, IronBackground)
    val dynamicSurface = parseHexColorSafe(clubIdentity.corSuperficieHex, IronSurface)

    val dynamicOnPrimary = if (dynamicPrimary.luminance() > 0.45f) Color(0xFF14100C) else Color.White
    val dynamicOnSecondary = if (dynamicSecondary.luminance() > 0.45f) Color(0xFF281800) else Color.White

    val dynamicScheme = darkColorScheme(
        primary = dynamicPrimary,
        onPrimary = dynamicOnPrimary,
        primaryContainer = darkenColor(dynamicPrimary, 0.28f),
        onPrimaryContainer = IronOnPrimaryContainer,
        secondary = dynamicSecondary,
        onSecondary = dynamicOnSecondary,
        secondaryContainer = darkenColor(dynamicSecondary, 0.28f),
        onSecondaryContainer = IronOnSecondaryContainer,
        tertiary = dynamicTertiary,
        onTertiary = IronOnTertiary,
        tertiaryContainer = darkenColor(dynamicTertiary, 0.30f),
        onTertiaryContainer = IronOnTertiaryContainer,
        background = dynamicBackground,
        onBackground = IronOnBackground,
        surface = dynamicSurface,
        onSurface = IronOnSurface,
        surfaceVariant = IronSurfaceVariant,
        onSurfaceVariant = IronOnSurfaceVariant,
        outline = IronOutline
    )

    CompositionLocalProvider(LocalClubIdentityConfig provides clubIdentity) {
        MaterialTheme(
            colorScheme = dynamicScheme,
            typography = Typography,
            content = content
        )
    }
}
