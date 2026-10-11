package com.example.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val OswaldFontFamily = FontFamily(
    Font(R.font.oswald, FontWeight.Normal),
    Font(R.font.oswald, FontWeight.Bold)
)

val PlusJakartaFontFamily = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.Bold)
)

val JetBrainsMonoFontFamily = FontFamily(
    Font(R.font.jetbrains_mono, FontWeight.Normal),
    Font(R.font.jetbrains_mono, FontWeight.Bold)
)

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = OswaldFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.3.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = OswaldFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.25.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = OswaldFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = OswaldFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    titleLarge = TextStyle(
        fontFamily = OswaldFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.2.sp
    ),
    titleMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 19.sp,
        letterSpacing = 0.1.sp
    ),
    titleSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp,
        letterSpacing = 0.1.sp
    ),
    bodySmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.15.sp
    ),
    labelMedium = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontFamily = PlusJakartaFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.1.sp
    )
)
