package com.example.pinkstream.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A small status dot with an optional ring around it (CSS `box-shadow: 0 0 0 3px`).
 * Like the CSS ring, the halo is drawn outside the dot and takes no layout space.
 */
@Composable
fun HaloDot(color: Color, size: Dp, halo: Color = Color.Transparent, haloWidth: Dp = 3.dp) {
    Box(
        Modifier
            .size(size)
            .drawBehind {
                val radius = this.size.minDimension / 2
                if (halo.alpha > 0f) drawCircle(halo, radius = radius + haloWidth.toPx())
                drawCircle(color, radius = radius)
            },
    )
}
