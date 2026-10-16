package com.example.pinkstream.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

/**
 * A coloured drop shadow, approximating the CSS shadows in the mockups.
 * Android fades shadows by its own alpha (about 20 %), so the token's alpha is
 * scaled up to land near the mockup strength. Transparent = no shadow.
 */
fun Modifier.softShadow(color: Color, elevation: Dp, shape: Shape): Modifier =
    if (color.alpha == 0f) {
        this
    } else {
        val strong = color.copy(alpha = (color.alpha * 3f).coerceAtMost(1f))
        shadow(elevation = elevation, shape = shape, clip = false, ambientColor = strong, spotColor = strong)
    }
