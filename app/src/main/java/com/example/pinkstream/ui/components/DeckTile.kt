package com.example.pinkstream.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pinkstream.ui.icons.DeckIcon
import com.example.pinkstream.ui.theme.DeckShapes
import com.example.pinkstream.ui.theme.DeckTheme
import com.example.pinkstream.ui.theme.DeckType
import com.example.pinkstream.ui.theme.TileColor

/** The looks a GRID tile can have (DESIGN.md → Tile). */
enum class TileLook { NORMAL, ON_AIR, TOGGLE_ON, TOGGLE_OFF, NO_ACTION }

/**
 * One 94 dp deck tile: icon top left, label bottom left, optional pill top right and
 * status line under the label. Visual only; the caller adds click handling via [modifier].
 */
@Composable
fun DeckTile(
    label: String,
    icon: DeckIcon,
    color: TileColor,
    look: TileLook,
    modifier: Modifier = Modifier,
    offline: Boolean = false,
) {
    val colors = DeckTheme.colors
    val palette = colors.tile(color)

    val background = when (look) {
        TileLook.ON_AIR -> colors.primary
        TileLook.TOGGLE_OFF -> colors.toggleOff
        else -> palette.container
    }
    val iconTint = when (look) {
        TileLook.ON_AIR -> colors.onPrimary
        TileLook.TOGGLE_OFF -> colors.toggleOffIcon
        else -> palette.icon
    }
    val labelColor = when (look) {
        TileLook.ON_AIR -> colors.onPrimary
        TileLook.TOGGLE_OFF -> colors.ink
        else -> palette.label
    }
    val dashColor = colors.inkSecondary.copy(alpha = 0.4f)

    Column(
        modifier
            .fillMaxWidth()
            .height(94.dp)
            .alpha(if (offline) 0.45f else 1f)
            .softShadow(if (look == TileLook.ON_AIR) colors.onAirShadow else Color.Transparent, 10.dp, DeckShapes.tile)
            .clip(DeckShapes.tile)
            .background(background)
            .then(if (look == TileLook.TOGGLE_OFF) Modifier.border(1.dp, colors.toggleOffBorder, DeckShapes.tile) else Modifier)
            .then(if (look == TileLook.NO_ACTION) Modifier.dashedBorder(dashColor, 22.dp) else Modifier)
            .padding(start = 13.dp, top = 13.dp, end = 13.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Icon(
                painterResource(if (look == TileLook.TOGGLE_OFF) icon.offDrawable else icon.drawable),
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
            when (look) {
                TileLook.TOGGLE_ON -> TilePill("On", background = colors.pillOn, content = palette.icon)
                TileLook.TOGGLE_OFF -> TilePill("Off", background = colors.pillOff, content = colors.pillOffText)
                else -> Unit
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, style = DeckType.tileLabel, color = labelColor, maxLines = 2, overflow = TextOverflow.Ellipsis)
            when (look) {
                TileLook.ON_AIR -> Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HaloDot(color = colors.onPrimary, size = 6.dp, halo = colors.onPrimary.copy(alpha = 0.25f))
                    Text("On air", style = DeckType.tileStatus, color = colors.onPrimary)
                }
                TileLook.NO_ACTION -> Text("No action", style = DeckType.tileStatus, color = colors.inkSecondary)
                else -> Unit
            }
        }
    }
}

@Composable
private fun TilePill(label: String, background: Color, content: Color) {
    Box(
        Modifier
            .height(22.dp)
            .background(background, DeckShapes.pill)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = DeckType.pill, color = content)
    }
}
