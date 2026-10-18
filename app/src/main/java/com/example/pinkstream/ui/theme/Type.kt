package com.example.pinkstream.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.example.pinkstream.R

/** Page tabs, screen titles and sheet titles. */
val InstrumentSerif = FontFamily(
    Font(R.font.instrument_serif_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.instrument_serif_italic, FontWeight.Normal, FontStyle.Italic),
)

/** Everything else. */
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

// Centre the text inside its line height and don't trim, like CSS line-height does,
// so sizes from the mockup HTML line up.
private val CssLineHeight = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun serif(size: Float, lineHeight: Float) = TextStyle(
    fontFamily = InstrumentSerif,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    lineHeightStyle = CssLineHeight,
)

private fun sans(size: Float, lineHeight: Float, weight: FontWeight) = TextStyle(
    fontFamily = PlusJakartaSans,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    lineHeightStyle = CssLineHeight,
)

/**
 * The type scale from DESIGN.md. Styles carry no colour; screens pick it from the theme.
 * Section labels: write them in sentence case and pass `text.uppercase()`.
 */
object DeckType {
    val screenTitle = serif(40f, 40f)
    val sheetTitle = serif(32f, 34f)
    val sectionLabel = sans(11.5f, 14f, FontWeight.SemiBold).copy(letterSpacing = 0.14.em)
    val sheetSubtitle = sans(13f, 16f, FontWeight.Medium)
    val pillLarge = sans(13f, 16f, FontWeight.SemiBold)
    val rowTitle = sans(15f, 19f, FontWeight.SemiBold)
    val rowSubtitle = sans(12.5f, 16f, FontWeight.Medium)
    val field = sans(16f, 20f, FontWeight.Medium)
    val fieldSettings = sans(15.5f, 20f, FontWeight.Medium)
    val fieldLabel = sans(12.5f, 15f, FontWeight.SemiBold)
    val button = sans(16f, 20f, FontWeight.SemiBold)
    val buttonSmall = sans(15f, 19f, FontWeight.SemiBold)
    val caption = sans(12.5f, 17f, FontWeight.Medium)
    val helper = sans(12f, 17f, FontWeight.Medium)
    val dockLabel = sans(12f, 15f, FontWeight.Medium)   // selected tab: copy(fontWeight = FontWeight.SemiBold)
}

// Material 3 components (dialogs, snackbars, text fields) read MaterialTheme.typography.
// Keep Material's sizes but switch every style to Plus Jakarta Sans.
private val Default = Typography()

val Typography = Typography(
    displayLarge = Default.displayLarge.copy(fontFamily = PlusJakartaSans),
    displayMedium = Default.displayMedium.copy(fontFamily = PlusJakartaSans),
    displaySmall = Default.displaySmall.copy(fontFamily = PlusJakartaSans),
    headlineLarge = Default.headlineLarge.copy(fontFamily = PlusJakartaSans),
    headlineMedium = Default.headlineMedium.copy(fontFamily = PlusJakartaSans),
    headlineSmall = Default.headlineSmall.copy(fontFamily = PlusJakartaSans),
    titleLarge = Default.titleLarge.copy(fontFamily = PlusJakartaSans),
    titleMedium = Default.titleMedium.copy(fontFamily = PlusJakartaSans),
    titleSmall = Default.titleSmall.copy(fontFamily = PlusJakartaSans),
    bodyLarge = Default.bodyLarge.copy(fontFamily = PlusJakartaSans),
    bodyMedium = Default.bodyMedium.copy(fontFamily = PlusJakartaSans),
    bodySmall = Default.bodySmall.copy(fontFamily = PlusJakartaSans),
    labelLarge = Default.labelLarge.copy(fontFamily = PlusJakartaSans),
    labelMedium = Default.labelMedium.copy(fontFamily = PlusJakartaSans),
    labelSmall = Default.labelSmall.copy(fontFamily = PlusJakartaSans),
)
