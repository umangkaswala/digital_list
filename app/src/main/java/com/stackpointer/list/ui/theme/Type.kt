package com.stackpointer.list.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stackpointer.list.R

// Roboto Flex, with Roboto (the platform default) as the fallback, per DESIGN_TOKENS.md.
val RobotoFlex = FontFamily(Font(R.font.roboto_flex))

// Material3's Typography data class has 15 fixed slots. The design only exercises 12
// style/size combinations (see DESIGN_TOKENS.md's table) and calls two of them out as
// "emphasized" twins of a base style at the same metrics but a heavier weight, used at
// specific call sites rather than as theme-wide replacements — those two live below as
// separate TextStyle constants rather than forced into the fixed Typography slots.
// Slots the design never specifies (displayLarge/Medium, headlineSmall, titleSmall) keep
// the M3 baseline metrics with the family swapped to Roboto Flex, for consistency.
val DigitalListTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = RobotoFlex),
        displayMedium = base.displayMedium.copy(fontFamily = RobotoFlex),
        displaySmall = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp,
        ),
        headlineLarge = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp,
        ),
        headlineMedium = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
        headlineSmall = base.headlineSmall.copy(fontFamily = RobotoFlex),
        titleLarge = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp,
        ),
        titleMedium = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp,
        ),
        titleSmall = base.titleSmall.copy(fontFamily = RobotoFlex),
        bodyLarge = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp,
        ),
        bodyMedium = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp,
        ),
        bodySmall = base.bodySmall.copy(fontFamily = RobotoFlex),
        labelLarge = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp,
        ),
        labelMedium = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp,
        ),
        labelSmall = TextStyle(
            fontFamily = RobotoFlex,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp,
        ),
    )
}

/** Bucket headers, active day header. Same metrics as [Typography.titleMedium], weight 700. */
val TitleMediumEmphasized = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = FontWeight.Bold,
    fontSize = 16.sp,
    lineHeight = 24.sp,
    letterSpacing = 0.15.sp,
)

/** Primary action in a dialog. Same metrics as [Typography.labelLarge], weight 700. */
val LabelLargeEmphasized = TextStyle(
    fontFamily = RobotoFlex,
    fontWeight = FontWeight.Bold,
    fontSize = 14.sp,
    lineHeight = 20.sp,
    letterSpacing = 0.1.sp,
)
