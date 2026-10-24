package com.findme.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

enum class AppFont(
    val fontFamily: FontFamily,
    val titleWeight: FontWeight,
    val bodyWeight: FontWeight,
    val titleLetterSpacing: TextUnit,
    val bodyLetterSpacing: TextUnit,
    val fontSizeDelta: Int
) {
    OPEN_SANS(
        fontFamily = FontFamily.SansSerif,
        titleWeight = FontWeight.SemiBold,
        bodyWeight = FontWeight.Normal,
        titleLetterSpacing = 0.sp,
        bodyLetterSpacing = 0.15.sp,
        fontSizeDelta = 0
    ),
    MONTSERRAT(
        fontFamily = FontFamily.SansSerif,
        titleWeight = FontWeight.ExtraBold,
        bodyWeight = FontWeight.Medium,
        titleLetterSpacing = 1.2.sp,
        bodyLetterSpacing = 0.6.sp,
        fontSizeDelta = 0
    ),
    LATO(
        fontFamily = FontFamily.Serif,
        titleWeight = FontWeight.Medium,
        bodyWeight = FontWeight.Light,
        titleLetterSpacing = 0.2.sp,
        bodyLetterSpacing = 0.25.sp,
        fontSizeDelta = 1
    ),
    INTER(
        fontFamily = FontFamily.Monospace,
        titleWeight = FontWeight.Bold,
        bodyWeight = FontWeight.Normal,
        titleLetterSpacing = (-0.4).sp,
        bodyLetterSpacing = (-0.2).sp,
        fontSizeDelta = -1
    )
}

fun createAppTypography(appFont: AppFont): Typography {
    val family = appFont.fontFamily
    val d = appFont.fontSizeDelta

    return Typography(
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (30 + d).sp,
            lineHeight = (38 + d).sp,
            letterSpacing = appFont.titleLetterSpacing
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (24 + d).sp,
            lineHeight = (32 + d).sp,
            letterSpacing = appFont.titleLetterSpacing
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (20 + d).sp,
            lineHeight = (28 + d).sp,
            letterSpacing = appFont.titleLetterSpacing
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (20 + d).sp,
            lineHeight = (26 + d).sp,
            letterSpacing = appFont.titleLetterSpacing
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (16 + d).sp,
            lineHeight = (24 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        titleSmall = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (14 + d).sp,
            lineHeight = (20 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = appFont.bodyWeight,
            fontSize = (16 + d).sp,
            lineHeight = (24 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = appFont.bodyWeight,
            fontSize = (14 + d).sp,
            lineHeight = (20 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        bodySmall = TextStyle(
            fontFamily = family,
            fontWeight = appFont.bodyWeight,
            fontSize = (12 + d).sp,
            lineHeight = (16 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = appFont.titleWeight,
            fontSize = (14 + d).sp,
            lineHeight = (20 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = appFont.bodyWeight,
            fontSize = (12 + d).sp,
            lineHeight = (16 + d).sp,
            letterSpacing = appFont.bodyLetterSpacing
        )
    )
}