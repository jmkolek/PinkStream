package com.example.pinkstream.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.icons.DeckKeyArt
import com.example.pinkstream.ui.theme.DeckTheme

/**
 * The outline of the key inside its image: the art has a 2 % transparent margin and
 * corners of about 22 % (README.md → Size). Shadows and the press ring follow it.
 */
object KeyShape : Shape {
    const val MARGIN = 0.02f
    const val CORNER = 0.22f

    fun rect(size: Size): RoundRect {
        val inset = size.width * MARGIN
        val radius = (size.width - 2 * inset) * CORNER
        return RoundRect(inset, inset, size.width - inset, size.height - inset, CornerRadius(radius))
    }

    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline =
        Outline.Rounded(rect(size))
}

/**
 * One key's art: the on or off image with its two shadows (a hard 3 dp edge and a
 * soft glow, README.md → Shadows). Switching between on and off crossfades in 200 ms.
 * The image already has its own corners and margin, so nothing is clipped or drawn behind it.
 * [lifted] (a key being dragged, mockup 02) swaps both shadows for one deep rose shadow.
 */
@Composable
fun KeyArtImage(art: DeckKeyArt, on: Boolean, modifier: Modifier = Modifier, lifted: Boolean = false) {
    val colors = DeckTheme.colors
    val edgeColor = when {
        lifted -> Color.Transparent
        on -> colors.keyEdgeOn
        else -> colors.keyEdgeOff
    }
    val glowColor = when {
        lifted -> colors.keyLifted
        on -> colors.keyGlowOn
        else -> colors.keyGlowOff
    }
    val edge by animateColorAsState(edgeColor, tween(200), label = "edge")
    val glow by animateColorAsState(glowColor, tween(200), label = "glow")
    val elevation by animateDpAsState(if (lifted) 22.dp else 8.dp, tween(200), label = "elevation")
    Box(
        modifier
            .aspectRatio(1f)
            .softShadow(glow, elevation, KeyShape)
            .keyEdge(edge),
    ) {
        // Crossfade fades out the old drawable and fades in the new one when it changes.
        Crossfade(targetState = if (on) art.on else art.off, animationSpec = tween(200), label = "art") { drawable ->
            Image(painterResource(drawable), contentDescription = null, modifier = Modifier.fillMaxSize())
        }
    }
}

/** The hard shadow: the key's outline in [color], 3 dp lower, drawn behind the image. */
fun Modifier.keyEdge(color: Color): Modifier = drawBehind {
    val rect = KeyShape.rect(size)
    drawRoundRect(
        color = color,
        topLeft = Offset(rect.left, rect.top + 3.dp.toPx()),
        size = Size(rect.width, rect.height),
        cornerRadius = CornerRadius(rect.topLeftCornerRadius.x),
    )
}

/** The dashed "+" slot that edit layout mode shows after a page's keys. */
@Composable
fun EmptySlotImage(modifier: Modifier = Modifier) {
    Image(painterResource(DeckKeyArt.EMPTY_SLOT), contentDescription = null, modifier = modifier.aspectRatio(1f))
}
