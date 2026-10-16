package com.example.pinkstream.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** Corner sizes from DESIGN.md → Shapes and spacing. */
object DeckShapes {
    val tile = RoundedCornerShape(22.dp)
    val hero = RoundedCornerShape(26.dp)
    val heroIconBox = RoundedCornerShape(18.dp)
    val settingsCard = RoundedCornerShape(24.dp)
    val listCard = RoundedCornerShape(22.dp)
    val previewStage = RoundedCornerShape(26.dp)
    val primaryButton = RoundedCornerShape(18.dp)
    val tonalButton = RoundedCornerShape(16.dp)
    val actionRow = RoundedCornerShape(18.dp)
    val editField = RoundedCornerShape(16.dp)
    val settingsField = RoundedCornerShape(14.dp)
    val searchField = RoundedCornerShape(16.dp)
    val iconChoice = RoundedCornerShape(15.dp)
    val settingsIconTile = RoundedCornerShape(11.dp)
    val segmentTrack = RoundedCornerShape(14.dp)
    val segmentOption = RoundedCornerShape(11.dp)
    val sheet = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)
    val sheetHandle = RoundedCornerShape(3.dp)
    val pickerRow = RoundedCornerShape(16.dp)
    val pickerIconBox = RoundedCornerShape(13.dp)
    val snackbar = RoundedCornerShape(16.dp)
    val pill = RoundedCornerShape(percent = 50)   // chips, pills, connection chip
}

// Material 3 components pick their corners from these slots (dialogs use extraLarge).
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
)
